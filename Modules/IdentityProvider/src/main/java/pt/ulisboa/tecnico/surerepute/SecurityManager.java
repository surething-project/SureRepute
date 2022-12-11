package pt.ulisboa.tecnico.surerepute;

import com.google.protobuf.ByteString;
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
import pt.ulisboa.tecnico.surerepute.ca.contract.CAOuterClass.Certificate;
import pt.ulisboa.tecnico.surerepute.ca.contract.CAOuterClass.SignCSRRequest;
import pt.ulisboa.tecnico.surerepute.error.RestTemplateErrorHandler;
import pt.ulisboa.tecnico.surerepute.sip.contract.SureReputeSIPOuterClass.GetPublicKeyResponse;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static pt.ulisboa.tecnico.surerepute.IdentityProviderServer.props;

public class SecurityManager {

  private static final Logger logger = LoggerFactory.getLogger(SecurityManager.class.getName());

  private static SecurityManager instance;
  private final String alias;
  private final char[] password;
  private final Map<String, PublicKey> serverPublicKeys = new HashMap<>();
  private final RestTemplate restTemplate;
  private KeyPair keyPair;
  private KeyStore keyStore;

  private SecurityManager()
      throws GeneralSecurityException, IOException, OperatorCreationException {
    HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
    httpRequestFactory.setConnectTimeout(2000);
    this.restTemplate = new RestTemplate(httpRequestFactory);
    restTemplate.setMessageConverters(List.of(new ProtobufHttpMessageConverter()));
    restTemplate.setErrorHandler(new RestTemplateErrorHandler());

    this.alias = props.getProperty("ID") + "Cert";
    this.password = props.getProperty("KEYSTORE_PWD").toCharArray();
    try (InputStream in = new FileInputStream(getResourceName("ID", ".p12"))) {
      logger.info("Loading an existing KeyStore!");
      loadKeyStore(in);
      logger.info("Checking Certificate Validity!");
      getCertificate().checkValidity();
      logger.info("Certificate is Valid!");
      loadKeypair();
      getServersPublicKey();
      return;
    } catch (CertificateExpiredException | CertificateNotYetValidException e) {
      logger.error("Invalid Certificate!");
    } catch (FileNotFoundException e) {
      logger.info("No KeyStore found!");
    }
    logger.info("Generating a new KeyStore!");
    generateKeyStore();
    getServersPublicKey();
  }

  public static SecurityManager getInstance() {
    try {
      if (instance == null) instance = new SecurityManager();
    } catch (GeneralSecurityException | OperatorCreationException | IOException e) {
      logger.error("Exception: ", e);
      System.exit(0);
    }
    return instance;
  }

  public static String getResourceName(String filename, String extension) {
    return props.getProperty("RESOURCES_PATH") + props.getProperty(filename) + extension;
  }

  public SSLContextConfigurator getSSLContext() {
    SSLContextConfigurator sslContext = new SSLContextConfigurator();
    sslContext.setKeyStoreFile(getResourceName("ID", ".p12"));
    sslContext.setKeyStorePass(props.getProperty("KEYSTORE_PWD"));
    sslContext.setKeyStoreType("PKCS12");
    return sslContext;
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

  public byte[] getCSR() throws IOException, OperatorCreationException {
    X500Principal subject = new X500Principal(createSubject());
    ContentSigner signGen =
        new JcaContentSignerBuilder("SHA256withRSA").build(this.keyPair.getPrivate());
    JcaPKCS10CertificationRequestBuilder builder =
        new JcaPKCS10CertificationRequestBuilder(subject, this.keyPair.getPublic());
    PKCS10CertificationRequest csr = builder.build(signGen);
    return csr.getEncoded();
  }

  public void generateKeyStore()
      throws GeneralSecurityException, IOException, OperatorCreationException {
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

  public X509Certificate generateCertificate()
      throws GeneralSecurityException, IOException, OperatorCreationException {
    logger.info("Generating a new CSR!");
    byte[] csr = this.getCSR();

    logger.info("Requesting a Crt to the CA!");
    SignCSRRequest signCSRRequest =
        SignCSRRequest.newBuilder().setCsr(ByteString.copyFrom(csr)).build();

    Certificate certificate =
        this.restTemplate.postForObject(
            props.getProperty("CA_URL") + "/ca", signCSRRequest, Certificate.class);
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

  public void getServersPublicKey()
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    logger.info("GetServersPublicKey");
    Properties properties = new Properties();
    properties.load(this.getClass().getResourceAsStream("/url.properties"));
    for (String key : properties.stringPropertyNames()) {
      String serverUrl = properties.getProperty(key);
      GetPublicKeyResponse publicKeyResponse = null;
      try {
        publicKeyResponse =
            this.restTemplate.getForObject(serverUrl + "/key", GetPublicKeyResponse.class);
      } catch (Exception e) {
        logger.error(key + " is offline! Public key not received! ");
      }

      if (publicKeyResponse != null) {
        logger.info(key + " is online! Public key received!");
        this.serverPublicKeys.put(
            key,
            KeyFactory.getInstance("RSA")
                .generatePublic(
                    new X509EncodedKeySpec(publicKeyResponse.getPublicKey().toByteArray())));
      }
    }
    if (this.serverPublicKeys.isEmpty()) {
      logger.error("No Servers available! Shutting Down...");
      System.exit(0);
    }
  }

  public PublicKey getServerPublicKey(String serverId) {
    return this.serverPublicKeys.get(serverId);
  }

  private String createSubject() {
    return "CN=localhost, O=surething, OU=IdentityProvider, C=PT";
  }
}
