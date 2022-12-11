package pt.ulisboa.tecnico.surerepute.identityprovider;

import com.google.protobuf.ByteString;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pt.ulisboa.tecnico.surerepute.identity.provider.contract.IdentityProviderOuterClass.GetEncryptedPseudonymRequest;
import pt.ulisboa.tecnico.surerepute.identity.provider.contract.IdentityProviderOuterClass.GetEncryptedPseudonymResponse;
import pt.ulisboa.tecnico.surerepute.identity.provider.contract.IdentityProviderOuterClass.GetEncryptedPseudonymsRequest;
import pt.ulisboa.tecnico.surerepute.identity.provider.contract.IdentityProviderOuterClass.GetEncryptedPseudonymsResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static pt.ulisboa.tecnico.surerepute.IdentityProviderServer.APPLICATION_PROTOBUF;

@Path("")
public class IdentityProviderController {

  @Inject private IdentityProviderService identityProviderService;

  @POST
  @Path("/pseudonym")
  @Consumes(APPLICATION_PROTOBUF)
  @Produces(APPLICATION_PROTOBUF)
  public GetEncryptedPseudonymResponse getEncryptedPseudonym(GetEncryptedPseudonymRequest request) {
    return GetEncryptedPseudonymResponse.newBuilder()
        .setEncryptedPseudonym(
            ByteString.copyFrom(
                identityProviderService.getEncryptedPseudonym(
                    request.getUserId(), request.getServerId())))
        .build();
  }

  @POST
  @Path("/pseudonyms")
  @Consumes(APPLICATION_PROTOBUF)
  @Produces(APPLICATION_PROTOBUF)
  public GetEncryptedPseudonymsResponse getEncryptedPseudonyms(
      GetEncryptedPseudonymsRequest request) {
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    GetEncryptedPseudonymsResponse.Builder encryptedPseudonymsBuilder =
        GetEncryptedPseudonymsResponse.newBuilder();
    for (String userId : request.getUserIdList()) {
      executorService.execute(
          () -> {
            ByteString encryptedPseudonym =
                ByteString.copyFrom(
                    identityProviderService.getEncryptedPseudonym(userId, request.getServerId()));
            synchronized (encryptedPseudonymsBuilder) {
              encryptedPseudonymsBuilder.putEncryptedPseudonyms(userId, encryptedPseudonym);
            }
          });
    }
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) throw new InterruptedException();
    } catch (InterruptedException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error!");
    }
    return encryptedPseudonymsBuilder.build();
  }
}
