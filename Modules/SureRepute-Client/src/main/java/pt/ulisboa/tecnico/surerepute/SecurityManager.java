package pt.ulisboa.tecnico.surerepute;

import com.google.protobuf.ByteString;
import org.apache.http.impl.client.HttpClients;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import pt.ulisboa.tecnico.surerepute.ca.contract.CAOuterClass;
import pt.ulisboa.tecnico.surerepute.error.RestTemplateErrorHandler;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.List;
import java.util.Map;

public class SecurityManager {

  private static final Logger logger = LoggerFactory.getLogger(SecurityManager.class.getName());

  public Map<String, String> props;
  private final String alias;
  private final char[] password;
  private KeyPair keyPair;
  private KeyStore keyStore;

  SecurityManager(Map<String, String> props) throws GeneralSecurityException, IOException {
    this.props = props;
    this.alias = props.get("ID") + "Cert";
    this.password = props.get("KEYSTORE_PWD").toCharArray();
    try (InputStream in = new FileInputStream(getResourceName("ID", ".p12"))) {
      logger.info("Loading an existing KeyStore!");
      loadKeyStore(in);
      logger.info("Checking Certificate Validity!");
      getCertificate().checkValidity();
      logger.info("Certificate is Valid!");
      loadKeypair();
      return;
    } catch (CertificateExpiredException | CertificateNotYetValidException e) {
      logger.error("Invalid Certificate!");
    } catch (FileNotFoundException e) {
      logger.info("No KeyStore found!");
    }
    logger.info("Generating a new KeyStore!");
    generateKeyStore();
  }

  public String getResourceName(String filename, String extension) {
    return props.get("RESOURCES_PATH") + props.get(filename) + extension;
  }

  private SSLContextConfigurator getSSLContext() {
    SSLContextConfigurator sslContext = new SSLContextConfigurator();
    sslContext.setKeyStoreFile(getResourceName("ID", ".p12"));
    sslContext.setKeyStorePass(props.get("KEYSTORE_PWD"));
    sslContext.setKeyStoreType("PKCS12");
    return sslContext;
  }

  public RestTemplate getRestTemplate() {
    SSLContextConfigurator sslContextConfigurator = getSSLContext();
    HttpComponentsClientHttpRequestFactory httpRequestFactory =
        new HttpComponentsClientHttpRequestFactory();
    httpRequestFactory.setConnectTimeout(2000);
    httpRequestFactory.setHttpClient(
        HttpClients.custom().setSSLContext(sslContextConfigurator.createSSLContext(false)).build());
    RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
    restTemplate.setErrorHandler(new RestTemplateErrorHandler());
    restTemplate.setMessageConverters(List.of(new ProtobufHttpMessageConverter()));
    return restTemplate;
  }

  private void loadKeypair() throws GeneralSecurityException {
    PrivateKey privateKey = (PrivateKey) this.keyStore.getKey(this.alias, this.password);
    PublicKey publicKey = getCertificate().getPublicKey();
    this.keyPair = new KeyPair(publicKey, privateKey);
  }

  private void generateKeypair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(4096, new SecureRandom());
    this.keyPair = keyGen.generateKeyPair();
  }

  public byte[] getCSR() {
    try {
      X500Principal subject = new X500Principal(createSubject());
      ContentSigner signGen =
          new JcaContentSignerBuilder("SHA256withRSA").build(this.keyPair.getPrivate());
      JcaPKCS10CertificationRequestBuilder builder =
          new JcaPKCS10CertificationRequestBuilder(subject, this.keyPair.getPublic());
      PKCS10CertificationRequest csr = builder.build(signGen);
      return csr.getEncoded();
    } catch (IOException | OperatorCreationException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public void generateKeyStore() throws GeneralSecurityException, IOException {
    generateKeypair();
    loadKeyStore(null);
    X509Certificate crt = generateCertificate();
    logger.info("Crt Received!");
    FileOutputStream fos = new FileOutputStream(getResourceName("ID", ".p12"));
    keyStore.setKeyEntry(
        this.alias, this.keyPair.getPrivate(), this.password, new X509Certificate[] {crt});
    keyStore.store(fos, password);
    logger.info("KeyStore Created!");
    fos.close();
  }

  public X509Certificate generateCertificate() throws GeneralSecurityException {
    logger.info("Generating a new CSR!");
    byte[] csr = this.getCSR();

    logger.info("Requesting a Crt to the CA!");
    HttpComponentsClientHttpRequestFactory httpRequestFactory =
        new HttpComponentsClientHttpRequestFactory();
    httpRequestFactory.setConnectTimeout(2000);
    RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
    restTemplate.setMessageConverters(List.of(new ProtobufHttpMessageConverter()));
    CAOuterClass.SignCSRRequest signCSRRequest =
        CAOuterClass.SignCSRRequest.newBuilder().setCsr(ByteString.copyFrom(csr)).build();

    CAOuterClass.Certificate certificate =
        restTemplate.postForObject(
            props.get("CA_URL") + "/ca", signCSRRequest, CAOuterClass.Certificate.class);
    assert certificate != null;
    return toX509Certificate(certificate.getCertificate().toByteArray());
  }

  public X509Certificate getCertificate() throws GeneralSecurityException {
    return (X509Certificate) keyStore.getCertificate(this.alias);
  }

  public void loadKeyStore(InputStream in) throws GeneralSecurityException, IOException {
    keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(in, this.password);
  }

  public X509Certificate toX509Certificate(byte[] crt) throws CertificateException {
    CertificateFactory fact = CertificateFactory.getInstance("X.509");
    InputStream in = new ByteArrayInputStream(crt);
    return (X509Certificate) fact.generateCertificate(in);
  }

  private String createSubject() {
    return "CN=localhost, O=surething, OU=SureReputeClient, C=PT";
  }
}
