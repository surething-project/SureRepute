package pt.ulisboa.tecnico.surerepute;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
@Disabled
public class GetReputationTest {

  private static final Logger logger = LoggerFactory.getLogger(GetReputationTest.class.getName());
  public SureReputeClient sureReputeClient;
  public SureReputeClient sureReputeClient2;
  public String user1;
  public String user2;
  public String user3;

  @BeforeAll
  public void oneTimeSetup() {
    this.sureReputeClient = new SureReputeClient();
    this.sureReputeClient2 = new SureReputeClient();
    this.sureReputeClient2.setProperty("SERVER_URL", "https://localhost:9095/v1");
    this.sureReputeClient2.setProperty("SERVER_ID", "SureReputeServer2");
    this.user1 = UUID.randomUUID().toString();
    this.user2 = UUID.randomUUID().toString();
    this.user3 = UUID.randomUUID().toString();
  }

  @Test
  @Disabled
  @Order(1)
  public void getScoreOfNewUser() {
    logger.info("GetScoreOfNewUser Test");
    logger.info("Requesting a Reputation Score of a new user:" + this.user1);
    Double score = this.sureReputeClient.getReputationScore(this.user1);
    logger.info(String.format("Score:%.2f%n", score));
    assertEquals(0.35, score);
  }

  @Test
  @Disabled
  @Order(2)
  public void getScoreOf2NewUsers() {
    logger.info("GetScoreOf2NewUsers Test");
    Map<String, Double> scores = this.sureReputeClient.getReputationScores(List.of(this.user1, this.user3));
    scores.forEach((key, value) -> logger.info(String.format("User:%s, Score:%.2f", key, value)));
    assertEquals(0.35, scores.get(this.user1));
    assertEquals(0.35, scores.get(this.user3));
  }

  @Test
  @Disabled
  @Order(3)
  public void report2GoodBehavior() {
    logger.info("Report2GoodBehavior Test");
    Map<String, Double> scores = this.sureReputeClient.reportBehaviors(Map.of(this.user1, Report.WELL_BEHAVED, this.user3, Report.WELL_BEHAVED));
    scores.forEach((key, value) -> logger.info(String.format("User:%s, Score:%.2f%n", key, value)));
    assertEquals(0.38, scores.get(this.user1));
    assertEquals(0.38, scores.get(this.user3));
  }

  @Test
  @Disabled
  @Order(4)
  public void reportMultipleGoodBehavior() {
    logger.info("reportMultipleGoodBehavior Test");
    logger.info("Reporting Good behavior for user: " + this.user1);
    this.sureReputeClient.reportBehavior(this.user1, Report.WELL_BEHAVED);
    logger.info("Reporting Good behavior for user: " + this.user1);
    this.sureReputeClient.reportBehavior(this.user1, Report.WELL_BEHAVED);
    logger.info("Reporting Good behavior for user: " + this.user1);
    this.sureReputeClient.reportBehavior(this.user1, Report.WELL_BEHAVED);
    logger.info("Reporting Good behavior for user: " + this.user1);
    this.sureReputeClient.reportBehavior(this.user1, Report.WELL_BEHAVED);
    logger.info("Reporting Good behavior for user: " + this.user1);
    this.sureReputeClient.reportBehavior(this.user1, Report.WELL_BEHAVED);
    logger.info("Reporting Good behavior for user: " + this.user1);
    this.sureReputeClient.reportBehavior(this.user1, Report.WELL_BEHAVED);
    logger.info("Reporting Good behavior for user: " + this.user1);
    this.sureReputeClient.reportBehavior(this.user1, Report.WELL_BEHAVED);
    logger.info("Reporting Good behavior for user: " + this.user1);
    this.sureReputeClient.reportBehavior(this.user1, Report.WELL_BEHAVED);
    logger.info("Reporting Good behavior for user: " + this.user1);
    double score = this.sureReputeClient.reportBehavior(this.user1, Report.WELL_BEHAVED);
    logger.info(String.format("New Score:%.2f%n", score));
    assertEquals(0.53, score);
  }

  @Test
  @Disabled
  @Order(5)
  public void reportCriticallyMaliciousBehavior() {
    logger.info("ReportBadBehavior Test");
    logger.info("Reporting bad behavior for user: " + this.user1);
    Double score =
        this.sureReputeClient.reportBehavior(this.user1, Report.INTENTIONALLY_MALICIOUS_CRITICAL);
    logger.info(String.format("New Score:%.2f%n", score));
    assertEquals(0.46, score);
  }

  @Test
  @Disabled
  @Order(6)
  public void reportIntentionallyMaliciousBehavior() {
    logger.info("ReportIntentionallyMaliciousBehavior Test");
    logger.info("Reporting bad behavior for user: " + this.user1);
    Double score = this.sureReputeClient.reportBehavior(this.user1, Report.INTENTIONALLY_MALICIOUS);
    logger.info(String.format("New Score:%.2f%n", score));
    assertEquals(0.43, score);
  }

  @Test
  @Disabled
  @Order(7)
  public void reportInOneServerGetInTheOtherServer() {
    logger.info("ReportInOneServerGetInTheOtherServer Test");
    logger.info("Reporting Good behavior for user " + this.user2 + " in SureReputeServer1:");
    Double score1 = this.sureReputeClient.reportBehavior(this.user2, Report.WELL_BEHAVED);
    logger.info(String.format("Score1:%.2f%n", score1));
    logger.info("Getting the score of user " + this.user2 + " in SureReputeServer2");
    Double score2 = this.sureReputeClient2.getReputationScore(this.user2);
    logger.info(String.format("Score2:%.2f%n", score2));
    logger.info("Asserting SureReputeServer1 and SureReputeServer2 have the same score for " + this.user2);
    assertEquals(score1, score2);
  }

  @Test
  @Disabled
  @Order(8)
  public void reportInTwoServerGetInTwoServers() {
    logger.info("ReportInTwoServerGetInTwoServers Test");
    logger.info("Reporting Good behavior for user " + this.user2 + " in SureReputeServer1:");
    Double score1 = this.sureReputeClient.reportBehavior(this.user2, Report.WELL_BEHAVED);
    logger.info(String.format("Score1:%.2f%n", score1));
    logger.info("Reporting Good behavior for user " + this.user2 + " in SureReputeServer2:");
    Double score2 = this.sureReputeClient2.reportBehavior(this.user2, Report.WELL_BEHAVED);
    logger.info(String.format("Score2:%.2f%n", score2));
    logger.info("Getting the score of " + this.user2 + " in SureReputeServer1");
    score1 = this.sureReputeClient.getReputationScore(this.user2);
    logger.info(String.format("Score1:%.2f%n", score1));
    logger.info("Asserting SureReputeServer1 and SureReputeServer2 have the same score for " + this.user2);
    assertEquals(score1, score2);
  }

  @AfterAll
  public void cleanup() {}
}
