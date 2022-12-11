package pt.ulisboa.tecnico.surerepute;

import pt.ulisboa.tecnico.surerepute.error.SureReputeClientException;

import java.util.Scanner;

public class CommandReader {

  private static final Scanner scanner = new Scanner(System.in);
  private final CommandExecutor commandExecutor = new CommandExecutor();

  public void run() {
    int option = -1;
    do {
      try {
        displayMenu();
        System.out.print(">>> ");
        option = Integer.parseInt(scanner.nextLine());
        System.out.println(parse(option));
      } catch (NumberFormatException e) {
        System.out.println("Invalid Command!");
      } catch (SureReputeClientException e) {
        System.out.println("Received Exception: " + e.getMessage());
      }
    } while (option != 3);
  }

  private String parse(int option) throws SureReputeClientException {
    switch (option) {
      case 0:
        return String.format("%.2f", getScore());
      case 1:
        return String.format("%.2f", reportBehavior());
      case 2:
        return parseTesting();
      case 3:
        return "Goodbye!";
      default:
        return "Command Not Available!";
    }
  }

  public String parseTesting() {
    displayTesting();
    System.out.print(">>> ");
    int opt = Integer.parseInt(scanner.nextLine());
    switch (opt) {
      case 0:
        commandExecutor.initialScoreDetailsTest();
        break;
      case 1:
        commandExecutor.forgettingWeightsTest();
        break;
      case 2:
        commandExecutor.reportsDiscrepancyTest();
        break;
      case 3:
        commandExecutor.shareSequentialTest();
        break;
      case 4:
        commandExecutor.shareThreadsTest();
        break;
      default:
        return "Command Not Available!";
    }
    return "Test Completed!";
  }

  public Double getScore() throws SureReputeClientException {
    displayGetUser();
    System.out.print(">>> ");
    String userId = scanner.nextLine();
    return commandExecutor.getScore(userId);
  }

  public Double reportBehavior() throws SureReputeClientException {
    displayGetUser();
    System.out.print(">>> ");
    String userId = scanner.nextLine();
    displayBehaviorReport();
    displayGetReport();
    int opt = Integer.parseInt(scanner.nextLine());
    return commandExecutor.reportBehavior(userId, opt);
  }

  private void displayMenu() {
    System.out.println("============== Menu ==============");
    System.out.println("| 0 - Get Score                  |");
    System.out.println("| 1 - Report Behavior            |");
    System.out.println("| 2 - Testing                    |");
    System.out.println("| 3 - Exit                       |");
    System.out.println("==================================");
  }

  private void displayGetUser() {
    System.out.println("Please enter a user");
  }

  private void displayGetReport() {
    System.out.println("Please enter a Behavior Report:");
  }

  private void displayBehaviorReport() {
    System.out.println("=========== Behavior Report ============");
    System.out.println("| 0 - INTENTIONALLY_MALICIOUS_CRITICAL |");
    System.out.println("| 1 - INTENTIONALLY_MALICIOUS          |");
    System.out.println("| 2 - ACCIDENTALLY_MALICIOUS           |");
    System.out.println("| 3 - WELL_BEHAVED                     |");
    System.out.println("========================================");
  }

  private void displayTesting() {
    System.out.println("============ Testing ==============");
    System.out.println("| 0 - Initial Score Details Test  |");
    System.out.println("| 1 - Forgetting Weights Test     |");
    System.out.println("| 2 - Reports Discrepancy Test    |");
    System.out.println("| 3 - Share Sequentially Test     |");
    System.out.println("| 4 - Share Threads Test          |");
    System.out.println("===================================");
  }
}
