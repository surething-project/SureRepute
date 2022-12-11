package pt.ulisboa.tecnico.surerepute.shared;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response.Status;
import org.glassfish.grizzly.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.surerepute.common.BehaviorReport;
import pt.ulisboa.tecnico.surerepute.common.ServerAPIRequests;
import pt.ulisboa.tecnico.surerepute.common.SureReputeRepository;
import pt.ulisboa.tecnico.surerepute.error.APIException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static pt.ulisboa.tecnico.surerepute.SureReputeServer.getDb;

@ApplicationScoped
public class SharedService {

  private static final Logger logger = LoggerFactory.getLogger(SharedService.class.getName());
  private final SureReputeRepository sureReputeRepository = SureReputeRepository.getInstance();
  private final ServerAPIRequests serverAPIRequests = ServerAPIRequests.getInstance();

  public Pair<Double, Double> hasPseudonym(String pseudonym, String followerId) {
    logger.info("Another server requested if the pseudonym is known!");
    try (Connection con = getDb()) {
      Pair<Double, Double> pseudonymBehavior = sureReputeRepository.hasPseudonym(con, pseudonym);
      if (pseudonymBehavior != null) {
        String leader_id = sureReputeRepository.isFollower(con, pseudonym);
        if (!leader_id.isEmpty()) {
          logger.error("Pseudonym is known but is not the leader!");
          throw new APIException(Status.FOUND, "Pseudonym is not known!");
        }
        logger.info("Pseudonym is known!");
        sureReputeRepository.storePseudonymFollower(con, pseudonym, followerId);
        return pseudonymBehavior;
      } else {
        logger.error("Pseudonym is not known!");
        throw new APIException(Status.NOT_FOUND, "Pseudonym is not known!");
      }
    } catch (SQLException e) {
      logger.error("Exception: ", e);
      throw new APIException(Status.INTERNAL_SERVER_ERROR, "Error in the database!");
    }
  }

  public Pair<Double, Double> forwardedReport(
      String pseudonym, String followerId, BehaviorReport.Report report, int tries) {
    logger.info("Another server (follower) has forwarded a report for a pseudonym!");
    try (Connection con = getDb()) {
      con.setAutoCommit(false);
      Pair<Double, Double> pseudonymBehavior = sureReputeRepository.hasPseudonym(con, pseudonym);
      if (pseudonymBehavior == null)
        throw new APIException(Status.NOT_FOUND, "Pseudonym not found!");
      BehaviorReport.updateBehavior(pseudonymBehavior, report);
      sureReputeRepository.updateBehavior(
          con, pseudonym, pseudonymBehavior.getFirst(), pseudonymBehavior.getSecond());
      List<String> followers = sureReputeRepository.getFollowers(con, pseudonym);
      followers.remove(followerId);
      if (!followers.isEmpty())
        serverAPIRequests.forwardScoreDetailsRequest(pseudonym, followers, pseudonymBehavior);
      con.commit();
      return pseudonymBehavior;
    } catch (SQLException e) {
      logger.error("Exception: ", e);
      if (tries == 0) throw new APIException(Status.INTERNAL_SERVER_ERROR, "Error on the database");
      return forwardedReport(pseudonym, followerId, report, tries - 1);
    }
  }

  public void updateBehavior(String pseudonym, Double positiveBehavior, Double negativeBehavior) {
    logger.info(
        "Another server (leader) has forwarded the updated behavior details for a pseudonym!");
    try (Connection con = getDb()) {
      sureReputeRepository.updateBehavior(con, pseudonym, positiveBehavior, negativeBehavior);
    } catch (SQLException e) {
      logger.error("Exception: ", e);
      throw new APIException(Status.INTERNAL_SERVER_ERROR, "Error on the database");
    }
  }
}
