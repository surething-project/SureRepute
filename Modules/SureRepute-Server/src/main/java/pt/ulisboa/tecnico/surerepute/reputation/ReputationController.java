package pt.ulisboa.tecnico.surerepute.reputation;

import com.google.protobuf.ByteString;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.apache.commons.math3.util.Precision;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pt.ulisboa.tecnico.surerepute.common.BehaviorReport;
import pt.ulisboa.tecnico.surerepute.cs.contract.SureReputeCSOuterClass.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static pt.ulisboa.tecnico.surerepute.SureReputeServer.APPLICATION_PROTOBUF;

@Path("/reputation")
public class ReputationController {
  @Inject private ReputationService reputationService;

  @POST
  @Path("/score")
  @Consumes(APPLICATION_PROTOBUF)
  @Produces(APPLICATION_PROTOBUF)
  public ReputationScore getReputationScore(GetReputationScoreRequest request) {
    return ReputationScore.newBuilder()
        .setEncryptedPseudonym(request.getEncryptedPseudonym())
        .setScore(
            Precision.round(
                reputationService.getReputationScore(
                    request.getEncryptedPseudonym().toByteArray(), 3),
                2))
        .build();
  }

  @POST
  @Path("/scores")
  @Consumes(APPLICATION_PROTOBUF)
  @Produces(APPLICATION_PROTOBUF)
  public ReputationScores getReputationScore(GetReputationScoresRequest request) {
    ReputationScores.Builder reputationScoresBuilder = ReputationScores.newBuilder();
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    for (ByteString encryptedPseudonym : request.getEncryptedPseudonymsList()) {
      executorService.execute(
          () -> {
            ReputationScore reputationScore =
                ReputationScore.newBuilder()
                    .setEncryptedPseudonym(encryptedPseudonym)
                    .setScore(
                        Precision.round(
                            reputationService.getReputationScore(
                                encryptedPseudonym.toByteArray(), 3),
                            2))
                    .build();
            synchronized (reputationScoresBuilder) {
              reputationScoresBuilder.addScores(reputationScore);
            }
          });
    }
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) throw new InterruptedException();
    } catch (InterruptedException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error!");
    }
    return reputationScoresBuilder.build();
  }

  @POST
  @Path("/report")
  @Consumes(APPLICATION_PROTOBUF)
  @Produces(APPLICATION_PROTOBUF)
  public ReputationScore reportBehavior(ReportBehaviorRequest request) {
    return ReputationScore.newBuilder()
        .setEncryptedPseudonym(request.getEncryptedPseudonym())
        .setScore(
            Precision.round(
                reputationService.reportBehavior(
                    request.getEncryptedPseudonym().toByteArray(),
                    BehaviorReport.toDomain(request.getReport()),
                    3),
                2))
        .build();
  }

  @POST
  @Path("/reports")
  @Consumes(APPLICATION_PROTOBUF)
  @Produces(APPLICATION_PROTOBUF)
  public ReputationScores reportBehaviors(ReportBehaviorsRequest request) {
    ReputationScores.Builder reputationScoresBuilder = ReputationScores.newBuilder();
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    for (ReportBehaviorRequest behaviorRequest : request.getReportBehaviorsList()) {
      executorService.execute(
          () -> {
            ReputationScore reputationScore =
                ReputationScore.newBuilder()
                    .setEncryptedPseudonym(behaviorRequest.getEncryptedPseudonym())
                    .setScore(
                        Precision.round(
                            reputationService.reportBehavior(
                                behaviorRequest.getEncryptedPseudonym().toByteArray(),
                                BehaviorReport.toDomain(behaviorRequest.getReport()),
                                3),
                            2))
                    .build();
            synchronized (reputationScoresBuilder) {
              reputationScoresBuilder.addScores(reputationScore);
            }
          });
    }
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) throw new InterruptedException();
    } catch (InterruptedException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error!");
    }
    return reputationScoresBuilder.build();
  }

  @GET
  @Path("/ping")
  public PINGResponse ping() {
    return PINGResponse.newBuilder().setWorking(true).build();
  }
}
