package pt.ulisboa.tecnico.surerepute;

import pt.ulisboa.tecnico.surerepute.error.SureReputeClientException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CommandExecutor {

  private static final String resourcePath = "src/main/resources/";
  private final SureReputeClient sureReputeClient = new SureReputeClient();

  public Double getScore(String userId) throws SureReputeClientException {
    return sureReputeClient.getReputationScore(userId);
  }

  public Double reportBehavior(String userId, int reportValue) throws SureReputeClientException {
    Report report = Report.valueOf(reportValue);
    return sureReputeClient.reportBehavior(userId, report);
  }

  public void initialScoreDetailsTest() {
    int[] reportsRange = {5, 10, 50, 100};
    double[] badBehaviorRange = {0.01, 0.02, 0.1, 0.2, 0.4, 0.6, 0.8, 1};
    String[] orderRange = {"start"};
    try (PrintWriter pw =
        new PrintWriter(new FileWriter(resourcePath + "initialDetails.txt", true))) {
      testExecutor(reportsRange, badBehaviorRange, orderRange, Report.INTENTIONALLY_MALICIOUS, pw);
    } catch (IOException e) {
      throw new SureReputeClientException(e.getMessage());
    }
  }

  public void forgettingWeightsTest() {
    int[] reportsRange = {5, 10, 50, 100};
    double[] badBehaviorRange = {0.01, 0.02, 0.1, 0.2, 0.4, 0.6, 0.8};
    String[] orderRange = {"start", "finish"};
    try (PrintWriter pw =
                 new PrintWriter(new FileWriter(resourcePath + "forgettingWeights.txt", true))) {
      testExecutor(reportsRange, badBehaviorRange, orderRange, Report.INTENTIONALLY_MALICIOUS,pw);
    } catch (IOException e) {
      throw new SureReputeClientException(e.getMessage());
    }
  }

  public void shareSequentialTest() {
    int[] serverRange = {1, 2, 4};
    int totalReports = 80;
    String[] serverUrls = {
      "https://localhost:9092/v1",
      "https://localhost:9095/v1",
      "https://localhost:9082/v1",
      "https://localhost:9085/v1"
    };
    String[] serverIds = {
      "SureReputeServer1", "SureReputeServer2", "SureReputeServer3", "SureReputeServer4"
    };
    try (PrintWriter pw =
        new PrintWriter(new FileWriter(resourcePath + "shareSequential.txt", true))) {
      for (int totalServer : serverRange) {
        String userId = UUID.randomUUID().toString();
        for (int i = 0; i < totalServer; i++) {
          sureReputeClient.setProperty("SERVER_URL", serverUrls[i]);
          sureReputeClient.setProperty("SERVER_ID", serverIds[i]);
          int badBehavior = (totalReports / 2) / totalServer;
          List<Report> reports =
              new ArrayList<>(Collections.nCopies(badBehavior, Report.INTENTIONALLY_MALICIOUS));
          multipleReports(userId, reports);
        }
        for (int i = 0; i < totalServer; i++) {
          sureReputeClient.setProperty("SERVER_URL", serverUrls[i]);
          sureReputeClient.setProperty("SERVER_ID", serverIds[i]);
          int goodReports = (totalReports / 2) / totalServer;
          List<Report> reports =
              new ArrayList<>(Collections.nCopies(goodReports, Report.WELL_BEHAVED));
          multipleReports(userId, reports);
        }
        for (int i = 0; i < totalServer; i++) {
          sureReputeClient.setProperty("SERVER_URL", serverUrls[i]);
          sureReputeClient.setProperty("SERVER_ID", serverIds[i]);
          pw.print(sureReputeClient.getReputationScore(userId) + ", ");
        }
        pw.println();
      }
    } catch (IOException e) {
      throw new SureReputeClientException(e.getMessage());
    }
  }

  public void shareThreadsTest() {
    int[] serverRange = {1, 2, 4};
    int totalReports = 40;
    String[] serverUrls = {
      "https://localhost:9092/v1",
      "https://localhost:9095/v1",
      "https://localhost:9082/v1",
      "https://localhost:9085/v1"
    };
    String[] serverIds = {
      "SureReputeServer1", "SureReputeServer2", "SureReputeServer3", "SureReputeServer4"
    };
    try (PrintWriter pw =
        new PrintWriter(new FileWriter(resourcePath + "shareThreads.txt", true))) {
      for (int totalServer : serverRange) {
        String userId = UUID.randomUUID().toString();

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < totalServer; i++) {
          int threadI = i;
          Thread thread =
              new Thread(
                  () -> {
                    SureReputeClient localSureReputeClient = new SureReputeClient();
                    localSureReputeClient.setProperty("SERVER_URL", serverUrls[threadI]);
                    localSureReputeClient.setProperty("SERVER_ID", serverIds[threadI]);
                    List<Report> reports =
                        new ArrayList<>(Collections.nCopies(totalReports, Report.WELL_BEHAVED));
                    multipleReports(localSureReputeClient, userId, reports);
                  });
          threads.add(thread);
          thread.start();
        }

        for (Thread thread : threads) thread.join();

        for (int i = 0; i < totalServer; i++) {
          sureReputeClient.setProperty("SERVER_URL", serverUrls[i]);
          sureReputeClient.setProperty("SERVER_ID", serverIds[i]);
          pw.print(sureReputeClient.getReputationScore(userId) + ", ");
        }
        pw.println();
      }
    } catch (IOException | InterruptedException e) {
      throw new SureReputeClientException(e.getMessage());
    }
  }

  public void reportsDiscrepancyTest() {
    Report[] reportType = {
            Report.INTENTIONALLY_MALICIOUS,
            Report.ACCIDENTALLY_MALICIOUS,
            Report.INTENTIONALLY_MALICIOUS_CRITICAL
    };
    int[] reportsRange = {5, 10, 50, 100};
    double[] badBehaviorRange = {0.01, 0.02, 0.1, 0.2, 0.4, 0.6, 0.8, 1};
    String[] orderRange = {"start"};
    try (PrintWriter pw =
                 new PrintWriter(new FileWriter(resourcePath + "reportDiscrepancy.txt", true))) {
      for (Report report : reportType) {
        testExecutor(reportsRange, badBehaviorRange, orderRange, report, pw);
        pw.println("---------");
      }
    } catch (IOException e) {
      throw new SureReputeClientException(e.getMessage());
    }
  }

  private void testExecutor(int[] reportsRange, double[] badBehaviorRange, String[] orderRange, Report reportType, PrintWriter pw) {
    for (int totalReports : reportsRange) {
      pw.println("#Reports : " + totalReports);
      for (double badBehaviorPercentage : badBehaviorRange) {
        int badBehavior = (int) Math.round(totalReports * badBehaviorPercentage);
        if (totalReports * badBehaviorPercentage != badBehavior) continue;
        boolean isFirst = true;
        for (String order : orderRange) {
          if (isFirst) isFirst = false;
          else pw.println(",");
          String userId = UUID.randomUUID().toString();
          double score =
              multipleReports(userId, constructReports(badBehavior, totalReports, order, reportType));
          pw.print(score);
        }
        pw.println();
      }
    }
  }

  private List<Report> constructReports(int badBehavior, int totalReports, String order, Report type) {
    List<Report> reports = new ArrayList<>();
    switch (order) {
      case "start":
        reports.addAll(Collections.nCopies(badBehavior, type));
        reports.addAll(Collections.nCopies(totalReports - badBehavior, Report.WELL_BEHAVED));
        return reports;
      case "finish":
        reports.addAll(Collections.nCopies(totalReports - badBehavior, Report.WELL_BEHAVED));
        reports.addAll(Collections.nCopies(badBehavior, type));
        return reports;
      default:
        return reports;
    }
  }

  private double multipleReports(String userId, List<Report> reports) {
    for (Report report : reports) {
      sureReputeClient.reportBehavior(userId, report);
    }
    return sureReputeClient.getReputationScore(userId);
  }

  private void multipleReports(
      SureReputeClient localSureReputeClient, String userId, List<Report> reports) {
    for (Report report : reports) {
      localSureReputeClient.reportBehavior(userId, report);
    }
    localSureReputeClient.getReputationScore(userId);
  }
}
