package pt.ulisboa.tecnico.surerepute.common;

import org.glassfish.grizzly.utils.Pair;
import pt.ulisboa.tecnico.surerepute.cs.contract.SureReputeCSOuterClass;
import pt.ulisboa.tecnico.surerepute.ss.contract.SureReputeSSOuterClass;

public class BehaviorReport {

  public static final double INITIAL_NEGATIVE_BEHAVIOR = 10;
  public static final double INITIAL_POSITIVE_BEHAVIOR = 5;
  public static final double FORGETTING_WEIGHT_NEGATIVE = 0.98;
  public static final double FORGETTING_WEIGHT_POSITIVE = 0.92;

  public static SureReputeSSOuterClass.Report toProto(Report report) {
    return SureReputeSSOuterClass.Report.forNumber(report.getValue());
  }

  public static Report toDomain(SureReputeSSOuterClass.Report report) {
    return Report.valueOf(report.getNumber());
  }

  public static Report toDomain(SureReputeCSOuterClass.Report report) {
    return Report.valueOf(report.getNumber());
  }

  public static Pair<Double, Double> newInitialBehavior() {
    return new Pair<>(INITIAL_POSITIVE_BEHAVIOR, INITIAL_NEGATIVE_BEHAVIOR);
  }

  public static double calculateReputationScore(double positiveBehavior, double negativeBehavior) {
    return (positiveBehavior + 1) / (positiveBehavior + negativeBehavior + 2);
  }

  public static void updateBehavior(Pair<Double, Double> behavior, Report report) {
    switch (report) {
      case INTENTIONALLY_MALICIOUS_CRITICAL:
        behavior.setFirst(behavior.getFirst() * FORGETTING_WEIGHT_POSITIVE);
        behavior.setSecond(behavior.getSecond() * FORGETTING_WEIGHT_NEGATIVE + 2);
        break;
      case INTENTIONALLY_MALICIOUS:
        behavior.setFirst(behavior.getFirst() * FORGETTING_WEIGHT_POSITIVE);
        behavior.setSecond(behavior.getSecond() * FORGETTING_WEIGHT_NEGATIVE + 1);
        break;
      case ACCIDENTALLY_MALICIOUS:
        behavior.setFirst(behavior.getFirst() * FORGETTING_WEIGHT_POSITIVE);
        behavior.setSecond(behavior.getSecond() * FORGETTING_WEIGHT_NEGATIVE + 0.5);
        break;
      case WELL_BEHAVED:
        behavior.setFirst(behavior.getFirst() * FORGETTING_WEIGHT_POSITIVE + 1);
        behavior.setSecond(behavior.getSecond() * FORGETTING_WEIGHT_NEGATIVE);
    }
  }

  public enum Report {
    INTENTIONALLY_MALICIOUS_CRITICAL(0),
    INTENTIONALLY_MALICIOUS(1),
    ACCIDENTALLY_MALICIOUS(2),
    WELL_BEHAVED(3),
    UNRECOGNIZED(-1);

    private final int value;

    Report(int value) {
      this.value = value;
    }

    public static Report valueOf(int value) {
      switch (value) {
        case 0:
          return Report.INTENTIONALLY_MALICIOUS_CRITICAL;
        case 1:
          return Report.INTENTIONALLY_MALICIOUS;
        case 2:
          return Report.ACCIDENTALLY_MALICIOUS;
        case 3:
          return Report.WELL_BEHAVED;
        default:
          return Report.UNRECOGNIZED;
      }
    }

    public int getValue() {
      return this.value;
    }
  }
}
