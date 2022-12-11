package pt.ulisboa.tecnico.surerepute.common;

import jakarta.ws.rs.core.Response.Status;
import org.glassfish.grizzly.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import pt.ulisboa.tecnico.surerepute.SecurityManager;
import pt.ulisboa.tecnico.surerepute.error.APIException;
import pt.ulisboa.tecnico.surerepute.ss.contract.SureReputeSSOuterClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static pt.ulisboa.tecnico.surerepute.SureReputeServer.*;

public class ServerAPIRequests {

  private static final Logger logger = LoggerFactory.getLogger(ServerAPIRequests.class.getName());
  private static ServerAPIRequests instance;
  private final RestTemplate restTemplate;

  public ServerAPIRequests() {
    this.restTemplate = SecurityManager.getInstance().getRestTemplate();
  }

  public static ServerAPIRequests getInstance() {
    if (instance == null) instance = new ServerAPIRequests();
    return instance;
  }

  public Pair<Double, Double> forwardReportRequest(
      String pseudonym, String serverId, BehaviorReport.Report report) {
    logger.info("Forwarding report to leader with id " + serverId);
    SureReputeSSOuterClass.ForwardReportRequest forwardReportRequest =
        SureReputeSSOuterClass.ForwardReportRequest.newBuilder()
            .setPseudonym(pseudonym)
            .setFollowerId(id)
            .setReport(BehaviorReport.toProto(report))
            .build();
    SureReputeSSOuterClass.ScoreDetails reputationBehavior =
        restTemplate.postForObject(
            serverUrls.get(serverId) + "/shared/report",
            forwardReportRequest,
            SureReputeSSOuterClass.ScoreDetails.class);
    if (reputationBehavior == null)
      throw new APIException(Status.INTERNAL_SERVER_ERROR, "Cannot submit report right now!");
    return new Pair<>(
        reputationBehavior.getPositiveBehavior(), reputationBehavior.getNegativeBehavior());
  }

  public void hasPseudonymRequest(
      String pseudonym,
      AtomicReference<SureReputeSSOuterClass.NewPseudonymResponse> newPseudonymResponse)
      throws InterruptedException {

    SureReputeSSOuterClass.NewPseudonymRequest pseudonymRequest =
        SureReputeSSOuterClass.NewPseudonymRequest.newBuilder()
            .setPseudonym(pseudonym)
            .setFollowerId(id)
            .build();
    Object lockObject = new Object();
    List<Thread> threads = new ArrayList<>();
    AtomicInteger finishedThreads = new AtomicInteger(serverUrls.size());
    for (Map.Entry<String, String> urls : serverUrls.entrySet()) {
      threads.add(
          new Thread(
              () -> {
                try {
                  SureReputeSSOuterClass.NewPseudonymResponse local =
                      restTemplate.postForObject(
                          urls.getValue() + "/shared/pseudonym",
                          pseudonymRequest,
                          SureReputeSSOuterClass.NewPseudonymResponse.class);
                  synchronized (lockObject) {
                    if (local != null && !local.getLeaderId().isEmpty())
                      newPseudonymResponse.set(local);
                  }
                } catch (APIException e) {
                  if (e.getResponse().getStatus() == Status.NOT_FOUND.getStatusCode())
                    logger.error("Pseudonym not known by " + urls.getKey() + "!");
                  else {
                    logger.error("Exception: ", e);
                  }
                } catch (ResourceAccessException e) {
                  logger.error("Server " + urls.getKey() + " is offline!");
                } finally {
                  synchronized (lockObject) {
                    finishedThreads.getAndDecrement();
                    lockObject.notify();
                  }
                }
              }));
    }
    synchronized (lockObject) {
      threads.forEach(Thread::start);
      while (finishedThreads.get() != 0) lockObject.wait();
    }
  }

  public void forwardScoreDetailsRequest(
      String pseudonym, List<String> followers, Pair<Double, Double> pseudonymBehavior) {
    logger.info("Forwarding score details to all followers!");
    SureReputeSSOuterClass.ScoreDetails scoreDetails =
        SureReputeSSOuterClass.ScoreDetails.newBuilder()
            .setPositiveBehavior(pseudonymBehavior.getFirst())
            .setNegativeBehavior(pseudonymBehavior.getSecond())
            .build();
    SureReputeSSOuterClass.ForwardScoreDetailsRequest request =
        SureReputeSSOuterClass.ForwardScoreDetailsRequest.newBuilder()
            .setPseudonym(pseudonym)
            .setScoreDetails(scoreDetails)
            .build();
    followers.forEach(
        (follower) ->
            new Thread(
                    () ->
                        restTemplate.postForObject(
                            serverUrls.get(follower) + "/shared/behavior", request, Void.class))
                .start());
  }
}
