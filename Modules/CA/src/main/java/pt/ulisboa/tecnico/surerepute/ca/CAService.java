package pt.ulisboa.tecnico.surerepute.ca;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response.Status;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.surerepute.SecurityManager;
import pt.ulisboa.tecnico.surerepute.error.APIException;

import java.io.IOException;
import java.security.GeneralSecurityException;

@ApplicationScoped
public class CAService {

  private static final Logger logger = LoggerFactory.getLogger(CAService.class.getName());

  public byte[] signCSR(byte[] csr) {
    logger.info("SigningCSR Request Received!");
    try {
      PKCS10CertificationRequest pkcs10Csr = new PKCS10CertificationRequest(csr);
      String id = pkcs10Csr.getSubject().getRDNs(BCStyle.OU)[0].getFirst().getValue().toString();
      logger.info("Identifier Of the CSR: " + id);
      return createCertificate(id, csr);
    } catch (IOException | GeneralSecurityException | OperatorCreationException e) {
      logger.error("Exception: ", e);
      throw new APIException(Status.BAD_REQUEST, "Invalid CSR!");
    }
  }

  private byte[] createCertificate(String id, byte[] csr)
      throws IOException, GeneralSecurityException, OperatorCreationException {
    JcaPKCS10CertificationRequest pkcs10Csr = new JcaPKCS10CertificationRequest(csr);
    return SecurityManager.getInstance().signCsr(id, pkcs10Csr).getEncoded();
  }
}
