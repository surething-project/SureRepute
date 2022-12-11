package pt.ulisboa.tecnico.surerepute.shared;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.glassfish.grizzly.utils.Pair;
import pt.ulisboa.tecnico.surerepute.common.BehaviorReport;
import pt.ulisboa.tecnico.surerepute.ss.contract.SureReputeSSOuterClass.*;

import static pt.ulisboa.tecnico.surerepute.SureReputeServer.APPLICATION_PROTOBUF;
import static pt.ulisboa.tecnico.surerepute.SureReputeServer.id;

@Path("/shared")
public class SharedController {

  @Inject private SharedService sharedService;

  @POST
  @Path("/pseudonym")
  @Consumes(APPLICATION_PROTOBUF)
  @Produces(APPLICATION_PROTOBUF)
  public NewPseudonymResponse hasPseudonym(NewPseudonymRequest request) {
    Pair<Double, Double> pseudonymBehavior =
        sharedService.hasPseudonym(request.getPseudonym(), request.getFollowerId());
    return NewPseudonymResponse.newBuilder()
        .setPositiveBehavior(pseudonymBehavior.getFirst())
        .setNegativeBehavior(pseudonymBehavior.getSecond())
        .setLeaderId(id)
        .build();
  }

  @POST
  @Path("/report")
  @Produces(APPLICATION_PROTOBUF)
  @Consumes(APPLICATION_PROTOBUF)
  public ScoreDetails forwardedReport(ForwardReportRequest request) {
    Pair<Double, Double> behavior =
        sharedService.forwardedReport(
            request.getPseudonym(),
            request.getFollowerId(),
            BehaviorReport.toDomain(request.getReport()),
            3);
    return ScoreDetails.newBuilder()
        .setPositiveBehavior(behavior.getFirst())
        .setNegativeBehavior(behavior.getSecond())
        .build();
  }

  @POST
  @Path("/behavior")
  @Consumes(APPLICATION_PROTOBUF)
  public void forwardScoreDetails(ForwardScoreDetailsRequest request) {
    sharedService.updateBehavior(
        request.getPseudonym(),
        request.getScoreDetails().getPositiveBehavior(),
        request.getScoreDetails().getNegativeBehavior());
  }
}
