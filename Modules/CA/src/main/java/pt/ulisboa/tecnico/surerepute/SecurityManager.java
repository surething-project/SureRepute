package pt.ulisboa.tecnico.surerepute;

import jakarta.ws.rs.core.Response;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.utils.Pair;
import org.glassfish.jersey.internal.guava.InetAddresses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.surerepute.error.APIException;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.*;

import static pt.ulisboa.tecnico.surerepute.CAServer.props;

public class SecurityManager {

  private static final Logger logger = LoggerFactory.getLogger(SecurityManager.class.getName());

  private static SecurityManager instance;
  private final String alias;
  private final char[] password;
  private KeyPair keyPair;
  private KeyStore keyStore;
  private X509Certificate crt;

  private SecurityManager()
      throws GeneralSecurityException, IOException, OperatorCreationException {
    this.alias = props.getProperty("ID") + "Cert";
    this.password = props.getProperty("KEYSTORE_PWD").toCharArray();
    try (InputStream in = new FileInputStream(getResourceName("ID", ".p12"))) {
      logger.info("Loading an existing KeyStore!");
      loadKeyStore(in);
      logger.info("Checking Certificate Validity!");
      this.crt = getCertificate();
      crt.checkValidity();
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

  public static SecurityManager getInstance() {
    try {
      if (instance == null) instance = new SecurityManager();
    } catch (GeneralSecurityException | IOException | OperatorCreationException e) {
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

  public void setupCACrt()
      throws CertificateException, OperatorCreationException, IOException,
          NoSuchAlgorithmException {
    logger.info("Generating a new Self Signed Certificate!");
    X500Principal subject = new X500Principal(createSubject());
    JcaX509v3CertificateBuilder certificateBuilder = setupSelfCSRCertificateBuilder(subject);
    JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();
    certificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
    certificateBuilder.addExtension(
        Extension.subjectKeyIdentifier,
        false,
        extensionUtils.createSubjectKeyIdentifier(this.keyPair.getPublic()));
    certificateBuilder.addExtension(
        Extension.subjectAlternativeName, false, this.getGeneralNameExtension("SureReputeCA"));
    ContentSigner signGen =
        new JcaContentSignerBuilder("SHA256WithRSA").build(this.keyPair.getPrivate());
    this.crt =
        new JcaX509CertificateConverter()
            .setProvider(new BouncyCastleProvider())
            .getCertificate(certificateBuilder.build(signGen));
    this.writeCrtToFile(getResourceName("ID", ".crt"));
  }

  public X509Certificate signCsr(String id, JcaPKCS10CertificationRequest csr)
      throws GeneralSecurityException, OperatorCreationException, IOException {
    logger.info("Signing a Certificate!");
    JcaX509v3CertificateBuilder certificateBuilder = setupCSRCertificateBuilder(csr);
    JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();
    certificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
    certificateBuilder.addExtension(
        Extension.authorityKeyIdentifier,
        false,
        extensionUtils.createAuthorityKeyIdentifier(this.crt));
    certificateBuilder.addExtension(
        Extension.subjectKeyIdentifier,
        false,
        extensionUtils.createSubjectKeyIdentifier(csr.getSubjectPublicKeyInfo()));
    certificateBuilder.addExtension(
        Extension.subjectAlternativeName, false, this.verifyCommonNames(id, csr));
    ContentSigner signGen =
        new JcaContentSignerBuilder("SHA256WithRSA").build(this.keyPair.getPrivate());
    return new JcaX509CertificateConverter()
        .setProvider(new BouncyCastleProvider())
        .getCertificate(certificateBuilder.build(signGen));
  }

  public JcaX509v3CertificateBuilder setupCSRCertificateBuilder(JcaPKCS10CertificationRequest csr)
      throws NoSuchAlgorithmException, InvalidKeyException {
    Pair<Date, Date> interval = getIntervalValidity(1);
    BigInteger rootSerialNum = new BigInteger(Long.toString(new SecureRandom().nextLong()));
    return new JcaX509v3CertificateBuilder(
        this.crt,
        rootSerialNum,
        interval.getFirst(),
        interval.getSecond(),
        csr.getSubject(),
        csr.getPublicKey());
  }

  public JcaX509v3CertificateBuilder setupSelfCSRCertificateBuilder(X500Principal subject) {
    Pair<Date, Date> interval = getIntervalValidity(10);
    BigInteger rootSerialNum = new BigInteger(Long.toString(new SecureRandom().nextLong()));
    return new JcaX509v3CertificateBuilder(
        subject,
        rootSerialNum,
        interval.getFirst(),
        interval.getSecond(),
        subject,
        this.keyPair.getPublic());
  }

  public Pair<Date, Date> getIntervalValidity(int years) {
    Date startDate = new Date(System.currentTimeMillis());
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(startDate);
    calendar.add(Calendar.YEAR, years);
    return new Pair<>(startDate, calendar.getTime());
  }

  public void generateKeyStore()
      throws GeneralSecurityException, IOException, OperatorCreationException {
    generateKeypair();
    loadKeyStore(null);
    this.setupCACrt();
    logger.info("Crt Created!");
    FileOutputStream fos = new FileOutputStream(getResourceName("ID", ".p12"));
    keyStore.setKeyEntry(
        this.alias, this.keyPair.getPrivate(), this.password, new X509Certificate[] {this.crt});
    keyStore.store(fos, password);
    logger.info("KeyStore Created!");
    fos.close();
  }

  public X509Certificate getCertificate() throws GeneralSecurityException {
    return (X509Certificate) keyStore.getCertificate(this.alias);
  }

  public void loadKeyStore(InputStream in) throws GeneralSecurityException, IOException {
    keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(in, this.password);
  }

  public void writeCrtToFile(String path) throws IOException {
    FileWriter fw = new FileWriter(path);
    try (PemWriter pw = new PemWriter(fw)) {
      PemObjectGenerator gen = new JcaMiscPEMGenerator(crt);
      pw.writeObject(gen);
    }
  }

  private String createSubject() {
    return "CN=localhost, O=surething, OU=SureReputeCA, C=PT";
  }

  private GeneralNames verifyCommonNames(String id, JcaPKCS10CertificationRequest csr) {
    GeneralNames generalNames = this.getGeneralNameExtension(id);
    Set<GeneralName> generalNameSet = Set.of(generalNames.getNames());
    String cn = csr.getSubject().getRDNs(BCStyle.CN)[0].getFirst().getValue().toString();
    GeneralName cnName =
        InetAddresses.isUriInetAddress(cn)
            ? new GeneralName(GeneralName.iPAddress, cn)
            : new GeneralName(GeneralName.dNSName, cn);

    if (!generalNameSet.contains(cnName)) {
      logger.error("Invalid CN");
      throw new APIException(Response.Status.BAD_REQUEST, "Invalid CSR!");
    }
    return generalNames;
  }

  private GeneralNames getGeneralNameExtension(String id) {
    List<GeneralName> subjectAltNames = new ArrayList<>();
    subjectAltNames.add(new GeneralName(GeneralName.dNSName, "localhost"));
    switch (id) {
      case "SureReputeCA":
        subjectAltNames.add(new GeneralName(GeneralName.dNSName, "ca.surething-surerepute.com"));
        break;
      case "SureReputeServer":
        subjectAltNames.add(
            new GeneralName(GeneralName.dNSName, "*.server.surething-surerepute.com"));
        break;
      case "IdentityProvider":
        subjectAltNames.add(new GeneralName(GeneralName.dNSName, "ip.surething-surerepute.com"));
        break;
      case "SureReputeClient":
        subjectAltNames.add(
            new GeneralName(GeneralName.dNSName, "*.client.surething-surerepute.com"));
        break;
      default:
        logger.error("Invalid Id in CSR: " + id);
        throw new APIException(Response.Status.BAD_REQUEST, "Invalid CSR!");
    }
    return new GeneralNames(subjectAltNames.toArray(GeneralName[]::new));
  }
}
