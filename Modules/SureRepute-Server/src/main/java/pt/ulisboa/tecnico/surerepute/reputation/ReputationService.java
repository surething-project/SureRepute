package pt.ulisboa.tecnico.surerepute.reputation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response.Status;
import org.glassfish.grizzly.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pt.ulisboa.tecnico.surerepute.SecurityManager;
import pt.ulisboa.tecnico.surerepute.common.BehaviorReport;
import pt.ulisboa.tecnico.surerepute.common.BehaviorReport.Report;
import pt.ulisboa.tecnico.surerepute.common.ServerAPIRequests;
import pt.ulisboa.tecnico.surerepute.common.SureReputeRepository;
import pt.ulisboa.tecnico.surerepute.error.APIException;
import pt.ulisboa.tecnico.surerepute.ss.contract.SureReputeSSOuterClass.NewPseudonymResponse;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static pt.ulisboa.tecnico.surerepute.SureReputeServer.getDb;
import static pt.ulisboa.tecnico.surerepute.SureReputeServer.serverUrls;
import static pt.ulisboa.tecnico.surerepute.common.BehaviorReport.calculateReputationScore;
import static pt.ulisboa.tecnico.surerepute.common.BehaviorReport.newInitialBehavior;

@ApplicationScoped
public class ReputationService {

  private static final Logger logger = LoggerFactory.getLogger(ReputationService.class.getName());
  private final SureReputeRepository sureReputeRepository = SureReputeRepository.getInstance();
  private final ServerAPIRequests serverAPIRequests = ServerAPIRequests.getInstance();

  public double getReputationScore(byte[] encryptedPseudonym, int tries) {
    logger.info("Client requested the reputation score of an user!");
    String pseudonym = decryptPseudonym(encryptedPseudonym);
    try (Connection con = getDb()) {
      con.setAutoCommit(false);
      Pair<Double, Double> pseudonymBehavior = sureReputeRepository.hasPseudonym(con, pseudonym);
      if (pseudonymBehavior == null) {
        pseudonymBehavior = registerPseudonym(con, pseudonym);
      } else {
        logger.info("Pseudonym Already known!");
      }
      con.commit();
      return calculateReputationScore(pseudonymBehavior.getFirst(), pseudonymBehavior.getSecond());
    } catch (SQLException e) {
      logger.error("Exception: ", e);
      if (tries == 0)
        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, "Error on the database");
      return getReputationScore(encryptedPseudonym, tries - 1);
    } catch (InterruptedException e) {
      logger.error("Exception: ", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error!");
    }
  }

  public double reportBehavior(byte[] encryptedPseudonym, Report report, int tries) {
    logger.info("Client sent a behavior report about an user!");
    String pseudonym = decryptPseudonym(encryptedPseudonym);
    try (Connection con = getDb()) {
      con.setAutoCommit(false);
      Pair<Double, Double> pseudonymBehavior = sureReputeRepository.hasPseudonym(con, pseudonym);
      if (pseudonymBehavior == null)
        pseudonymBehavior = registerPseudonym(con, pseudonym);
      String serverId = sureReputeRepository.isFollower(con, pseudonym);

      if (!serverId.isEmpty()) {
        pseudonymBehavior = serverAPIRequests.forwardReportRequest(pseudonym, serverId, report);
      } else {
        BehaviorReport.updateBehavior(pseudonymBehavior, report);
        List<String> followers = sureReputeRepository.getFollowers(con, pseudonym);
        if (!followers.isEmpty())
          serverAPIRequests.forwardScoreDetailsRequest(pseudonym, followers, pseudonymBehavior);
      }

      sureReputeRepository.updateBehavior(
          con, pseudonym, pseudonymBehavior.getFirst(), pseudonymBehavior.getSecond());
      con.commit();
      return calculateReputationScore(pseudonymBehavior.getFirst(), pseudonymBehavior.getSecond());
    } catch (SQLException e) {
      logger.error("Exception: ", e);
      if (tries == 0) throw new APIException(Status.INTERNAL_SERVER_ERROR, "Error on the database");
      return reportBehavior(encryptedPseudonym, report, tries - 1);
    } catch (InterruptedException e) {
      logger.error("Exception: ", e);
      throw new APIException(Status.INTERNAL_SERVER_ERROR, "Internal error!");
    }
  }

  private Pair<Double, Double> registerPseudonym(Connection con, String pseudonym)
      throws SQLException, InterruptedException {
    logger.info("Pseudonym is unknown to this server!");
    AtomicReference<NewPseudonymResponse> newPseudonymResponse = new AtomicReference<>();
    Pair<Double, Double> behavior = newInitialBehavior();
    if (serverUrls.size() != 0) {
      logger.info("Broadcasting all known servers to verify if they have this pseudonym!");
      serverAPIRequests.hasPseudonymRequest(pseudonym, newPseudonymResponse);
    }
    if (newPseudonymResponse.get() == null) {
      logger.info("No server has this pseudonym!");
      sureReputeRepository.storePseudonym(con, pseudonym, behavior);
    } else {
      logger.info(
          "Server with id " + newPseudonymResponse.get().getLeaderId() + " has the pseudonym!");
      behavior =
          new Pair<>(
              newPseudonymResponse.get().getPositiveBehavior(),
              newPseudonymResponse.get().getNegativeBehavior());
      sureReputeRepository.storePseudonym(con, pseudonym, behavior);
      sureReputeRepository.storePseudonymLeader(
          con, pseudonym, newPseudonymResponse.get().getLeaderId());
    }
    return behavior;
  }

  @NotNull
  private String decryptPseudonym(byte[] encryptedPseudonym) {
    logger.info("Decrypting the Pseudonym!");
    String pseudonym;
    try {
      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init(Cipher.DECRYPT_MODE, SecurityManager.getInstance().getPrivateKey());
      pseudonym = new String(cipher.doFinal(encryptedPseudonym));
    } catch (GeneralSecurityException e) {
      logger.error("Exception: ", e);
      throw new APIException(Status.BAD_REQUEST, "Invalid Encryption for Pseudonym");
    }
    logger.info("Pseudonym=" + pseudonym);
    return pseudonym;
  }
}
