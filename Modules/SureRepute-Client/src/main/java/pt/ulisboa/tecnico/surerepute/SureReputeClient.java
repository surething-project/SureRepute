package pt.ulisboa.tecnico.surerepute;

import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pt.ulisboa.tecnico.surerepute.cs.contract.SureReputeCSOuterClass;
import pt.ulisboa.tecnico.surerepute.cs.contract.SureReputeCSOuterClass.*;
import pt.ulisboa.tecnico.surerepute.error.SureReputeClientException;
import pt.ulisboa.tecnico.surerepute.identity.provider.contract.IdentityProviderOuterClass.GetEncryptedPseudonymRequest;
import pt.ulisboa.tecnico.surerepute.identity.provider.contract.IdentityProviderOuterClass.GetEncryptedPseudonymResponse;
import pt.ulisboa.tecnico.surerepute.identity.provider.contract.IdentityProviderOuterClass.GetEncryptedPseudonymsRequest;
import pt.ulisboa.tecnico.surerepute.identity.provider.contract.IdentityProviderOuterClass.GetEncryptedPseudonymsResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SureReputeClient {

  public final Map<String, String> props = new HashMap<>();
  public final EncryptedPseudonymsQueue storedEncryptedPseudonyms =
          new EncryptedPseudonymsQueue(1000);
  private static final Logger logger = LoggerFactory.getLogger(SureReputeClient.class.getName());
  private RestTemplate restTemplate;
  private boolean isOnline = true;

  public static SureReputeClient get() {
    return SureReputeClientHolder.INSTANCE;
  }

  public SureReputeClient() {
    SecurityManager securityManager;
    try {
      Properties properties = new Properties();
      properties.load(SureReputeClient.class.getResourceAsStream("/client.properties"));
      for (String key : properties.stringPropertyNames()) {
        if (key.equals(properties.getProperty("ID"))) continue;
        String value = properties.getProperty(key);
        props.put(key, value);
      }
      securityManager = new SecurityManager(props);
    } catch (IOException
        | GeneralSecurityException
        | NullPointerException
        | RestClientException e) {
      logger.error("Can't create the certificate! Turning of Communication with Server!");
      isOnline = false;
      return;
    }
    this.restTemplate = securityManager.getRestTemplate();
    new Timer().scheduleAtFixedRate(new OnlineServerVerifier(), 0, TimeUnit.SECONDS.toMillis(30));
  }

  public void setProperty(String key, String value) {
    props.put(key, value);
  }


  public Double getReputationScore(String userId) {
    try {
      if (!isOnline) throw new SureReputeClientException("Error");
      GetReputationScoreRequest reputationScoreRequest =
              GetReputationScoreRequest.newBuilder()
                      .setEncryptedPseudonym(getEncryptedPseudonym(userId))
                      .build();
      logger.info("Getting Score from SureRepute!");
      ReputationScore reputationScore =
              this.restTemplate.postForObject(
                      props.get("SERVER_URL") + "/reputation/score",
                      reputationScoreRequest,
                      ReputationScore.class);
      if (reputationScore == null) return 0.0;
      return reputationScore.getScore();
    } catch (SureReputeClientException | RestClientException | NullPointerException e) {
      // logger.error("Exception:", e);
      logger.error("Could not get score! Returning the default Score!");
      return Double.parseDouble(props.get("default_score"));
    }
  }

  public Map<String, Double> getReputationScores(List<String> userIdList) {
    try {
      if (!isOnline) throw new SureReputeClientException("Error");
      Map<ByteString, String> encryptedPseudonyms =
              getEncryptedPseudonyms(new HashSet<>(userIdList));
      GetReputationScoresRequest reputationScoresRequest =
              GetReputationScoresRequest.newBuilder()
                      .addAllEncryptedPseudonyms(encryptedPseudonyms.keySet())
                      .build();
      logger.info("Getting Scores from SureRepute!");
      ReputationScores reputationScores =
              this.restTemplate.postForObject(
                      props.get("SERVER_URL") + "/reputation/scores",
                      reputationScoresRequest,
                      ReputationScores.class);
      assert reputationScores != null;
      return reputationScores.getScoresList().stream()
              .collect(
                      Collectors.toMap(
                              y -> encryptedPseudonyms.get(y.getEncryptedPseudonym()),
                              ReputationScore::getScore));
    } catch (SureReputeClientException | RestClientException | NullPointerException e) {
      // logger.error("Exception:", e);
      logger.error("Could not get score! Returning default Score!");
      return userIdList.stream().collect(Collectors.toMap(s -> s, s -> 0.5));
    }
  }

  public Double reportBehavior(String userId, Report report) {
    try {
      if (!isOnline) throw new SureReputeClientException("Error");
      ReportBehaviorRequest reportBehaviorRequest =
              ReportBehaviorRequest.newBuilder()
                      .setEncryptedPseudonym(getEncryptedPseudonym(userId))
                      .setReport(SureReputeCSOuterClass.Report.valueOf(report.toString()))
                      .build();
      logger.info("Submitting a score to SureRepute!");
      ReputationScore reputationScore =
              this.restTemplate.postForObject(
                      props.get("SERVER_URL") + "/reputation/report",
                      reportBehaviorRequest,
                      ReputationScore.class);
      if (reputationScore == null) return 0.0;
      return reputationScore.getScore();
    } catch (SureReputeClientException | RestClientException | NullPointerException e) {
      // logger.error("Exception:", e);
      logger.error("Report not submitted! Returning the default Score!");
      return Double.parseDouble(props.get("default_score"));
    }
  }

  public Map<String, Double> reportBehaviors(Map<String, Report> userReports) {
    try {
      if (!isOnline) throw new SureReputeClientException("Error");
      ReportBehaviorsRequest.Builder reportBehaviorsRequest = ReportBehaviorsRequest.newBuilder();
      Map<ByteString, String> encryptedPseudonyms = getEncryptedPseudonyms(userReports.keySet());
      encryptedPseudonyms.forEach(
              (encryptedPseudonym, userId) ->
                      reportBehaviorsRequest.addReportBehaviors(
                              ReportBehaviorRequest.newBuilder()
                                      .setEncryptedPseudonym(encryptedPseudonym)
                                      .setReport(
                                              SureReputeCSOuterClass.Report.valueOf(
                                                      userReports.get(userId).toString()))));

      logger.info("Submitting a score to SureRepute!");
      ReputationScores reputationScores =
              this.restTemplate.postForObject(
                      props.get("SERVER_URL") + "/reputation/reports",
                      reportBehaviorsRequest.build(),
                      ReputationScores.class);
      assert reputationScores != null;
      return reputationScores.getScoresList().stream()
              .collect(
                      Collectors.toMap(
                              y -> encryptedPseudonyms.get(y.getEncryptedPseudonym()),
                              ReputationScore::getScore));
    } catch (SureReputeClientException | RestClientException | NullPointerException e) {
      // logger.error("Exception:", e);
      logger.error("Report not submitted! Returning the default Score!");
      return userReports.keySet().stream().collect(Collectors.toMap(s -> s, s -> 0.5));
    }
  }

  public void reportBehaviorNonBlocking(String userId, Report report) {
    new Thread(() -> reportBehavior(userId, report)).start();
  }

  public void reportBehaviorsNonBlocking(Map<String, Report> userReports) {
    new Thread(() -> reportBehaviors(userReports)).start();
  }

  private ByteString getEncryptedPseudonym(String userId) {
    ByteString encryptedPseudonym = this.storedEncryptedPseudonyms.get(userId);
    if (encryptedPseudonym != null) return encryptedPseudonym;
    logger.info("Getting encrypted pseudonym from identity provider");
    GetEncryptedPseudonymRequest encryptedPseudonymRequest =
            GetEncryptedPseudonymRequest.newBuilder()
                    .setUserId(userId)
                    .setServerId(props.get("SERVER_ID"))
                    .build();
    GetEncryptedPseudonymResponse encryptedPseudonymResponse =
            this.restTemplate.postForObject(
                    props.get("IDENTITY_PROVIDER_URL") + "/pseudonym",
                    encryptedPseudonymRequest,
                    GetEncryptedPseudonymResponse.class);
    assert encryptedPseudonymResponse != null;
    this.storedEncryptedPseudonyms.put(userId, encryptedPseudonymResponse.getEncryptedPseudonym());
    return encryptedPseudonymResponse.getEncryptedPseudonym();
  }

  private Map<ByteString, String> getEncryptedPseudonyms(Set<String> userIdList) {
    Map<ByteString, String> encryptPseudonyms = new LinkedHashMap<>();
    List<String> newUsers = new ArrayList<>();

    userIdList.forEach(
            userId -> {
              ByteString encryptedPseudonym = this.storedEncryptedPseudonyms.get(userId);
              if (encryptedPseudonym == null) newUsers.add(userId);
              else encryptPseudonyms.put(encryptedPseudonym, userId);
            });

    if (encryptPseudonyms.size() == userIdList.size()) return encryptPseudonyms;

    logger.info("Getting encrypted pseudonyms from identity provider");
    GetEncryptedPseudonymsRequest encryptedPseudonymsRequest =
            GetEncryptedPseudonymsRequest.newBuilder()
                    .addAllUserId(newUsers)
                    .setServerId(props.get("SERVER_ID"))
                    .build();
    GetEncryptedPseudonymsResponse getEncryptedPseudonyms =
            this.restTemplate.postForObject(
                    props.get("IDENTITY_PROVIDER_URL") + "/pseudonyms",
                    encryptedPseudonymsRequest,
                    GetEncryptedPseudonymsResponse.class);

    assert getEncryptedPseudonyms != null;
    getEncryptedPseudonyms
            .getEncryptedPseudonymsMap()
            .forEach(
                    (userId, encryptPseudonym) -> {
                      this.storedEncryptedPseudonyms.put(userId, encryptPseudonym);
                      encryptPseudonyms.put(encryptPseudonym, userId);
                    });
    return encryptPseudonyms;
  }

  private static class SureReputeClientHolder {
    private static final SureReputeClient INSTANCE = new SureReputeClient();
  }

  public class OnlineServerVerifier extends TimerTask {
    @Override
    public void run() {
      try {
        PINGResponse pingResponse =
                restTemplate.getForObject(
                        props.get("SERVER_URL") + "/reputation/ping", PINGResponse.class);
        assert pingResponse != null;
        if (pingResponse.getWorking()) {
          logger.info("Server Is Online");
          isOnline = true;
        }
      } catch (SureReputeClientException | RestClientException | NullPointerException e) {
        // logger.error("Exception", e);
        logger.info("Server is Offline!");
        isOnline = false;
      }
    }
  }
}
