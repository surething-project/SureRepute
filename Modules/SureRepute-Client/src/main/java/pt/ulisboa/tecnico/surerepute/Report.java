package pt.ulisboa.tecnico.surerepute;

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
