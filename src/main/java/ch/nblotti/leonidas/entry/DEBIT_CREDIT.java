package ch.nblotti.leonidas.entry;

public enum DEBIT_CREDIT {
  CRDT(1), DBIT(2),ZERO(3);

  final int type;

  private DEBIT_CREDIT(int type) {
    this.type = type;
  }

  public static DEBIT_CREDIT fromType(int type) {
    for (DEBIT_CREDIT debit_credit : DEBIT_CREDIT.values()) {
      if (debit_credit.type == type)
        return debit_credit;
    }
    throw new IllegalStateException();
  }
}

