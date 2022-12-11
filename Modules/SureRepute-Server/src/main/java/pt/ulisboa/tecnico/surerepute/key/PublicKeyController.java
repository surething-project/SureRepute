package pt.ulisboa.tecnico.surerepute.key;

import com.google.protobuf.ByteString;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import pt.ulisboa.tecnico.surerepute.sip.contract.SureReputeSIPOuterClass.GetPublicKeyResponse;
import static pt.ulisboa.tecnico.surerepute.SureReputeServer.APPLICATION_PROTOBUF;

@Path("/key")
public class PublicKeyController {
  @Inject private PublicKeyService publicKeyService;

  @GET
  @Produces(APPLICATION_PROTOBUF)
  public GetPublicKeyResponse getPublicKey() {
    return GetPublicKeyResponse.newBuilder()
        .setPublicKey(ByteString.copyFrom(publicKeyService.getPublicKey()))
        .build();
  }
}
