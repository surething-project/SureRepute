package pt.ulisboa.tecnico.surerepute.identityprovider;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.surerepute.IdentityProviderServer;
import pt.ulisboa.tecnico.surerepute.SecurityManager;
import pt.ulisboa.tecnico.surerepute.error.APIException;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static pt.ulisboa.tecnico.surerepute.IdentityProviderServer.getDb;

@ApplicationScoped
public class IdentityProviderService {
  private static final Logger logger =
      LoggerFactory.getLogger(IdentityProviderServer.class.getName());
  private final IdentityProviderRepository identityProviderRepository =
      IdentityProviderRepository.getInstance();

  public byte[] getEncryptedPseudonym(String userId, String serverId) {
    logger.info(
        "GetEncryptedPseudonym Request with User: " + userId + ", and ServerId: " + serverId);
    return encryptPseudonym(serverId, getPseudonym(userId, 3));
  }

  private String getPseudonym(String userId, int tries) {
    try (Connection con = getDb()) {
      con.setAutoCommit(false);
      String pseudonym = identityProviderRepository.getPseudonym(con, userId);
      if (pseudonym.isEmpty()) {
        pseudonym = UUID.randomUUID().toString();
        identityProviderRepository.storePseudonym(con, userId, pseudonym);
      }
      con.commit();
      return pseudonym;
    } catch (SQLException e) {
      logger.error("Exception: ", e);
      if (tries == 0) throw new APIException(Status.INTERNAL_SERVER_ERROR, "Error in the database!");
      return getPseudonym(userId, tries - 1);
    }
  }

  private byte[] encryptPseudonym(String serverId, String pseudonym) {
    try {
      PublicKey publicKey = SecurityManager.getInstance().getServerPublicKey(serverId);

      if (publicKey == null) {
        logger.error("No Public Key was found for the received serverId!");
        throw new APIException(
            Status.NOT_FOUND, "No Public Key was found for the received serverId!");
      }

      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init(Cipher.ENCRYPT_MODE, publicKey);
      cipher.update(pseudonym.getBytes(StandardCharsets.UTF_8));
      return cipher.doFinal();
    } catch (GeneralSecurityException e) {
      logger.error("Exception: ", e);
      throw new APIException(
          Status.INTERNAL_SERVER_ERROR, "Error encrypting the pseudonym with server public key");
    }
  }
}
