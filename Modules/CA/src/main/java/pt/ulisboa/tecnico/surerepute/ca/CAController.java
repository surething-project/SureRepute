package pt.ulisboa.tecnico.surerepute.ca;

import com.google.protobuf.ByteString;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import pt.ulisboa.tecnico.surerepute.ca.contract.CAOuterClass.Certificate;
import pt.ulisboa.tecnico.surerepute.ca.contract.CAOuterClass.SignCSRRequest;

import static pt.ulisboa.tecnico.surerepute.CAServer.APPLICATION_PROTOBUF;

@Path("/ca")
public class CAController {

  @Inject private CAService caService;

  @POST
  @Consumes(APPLICATION_PROTOBUF)
  @Produces(APPLICATION_PROTOBUF)
  public Certificate signCSR(SignCSRRequest request) {
    return Certificate.newBuilder()
        .setCertificate(ByteString.copyFrom(caService.signCSR(request.getCsr().toByteArray())))
        .build();
  }
}
