package ch.nblotti.leonidas.entry;

public enum ACHAT_VENTE {
  ACHAT(1), VENTE(2),ZERO(3);

  final int type;

  private ACHAT_VENTE(int type) {
    this.type = type;
  }

  public static ACHAT_VENTE fromType(int type) {
    for (ACHAT_VENTE ACHATVENTE : ACHAT_VENTE.values()) {
      if (ACHATVENTE.type == type)
        return ACHATVENTE;
    }
    throw new IllegalStateException();
  }
}

