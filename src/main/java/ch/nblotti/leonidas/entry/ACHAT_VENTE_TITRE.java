package ch.nblotti.leonidas.entry;

public enum ACHAT_VENTE_TITRE {
  ACHAT(1), VENTE(2),ZERO(3);

  final int type;

  private ACHAT_VENTE_TITRE(int type) {
    this.type = type;
  }

  public static ACHAT_VENTE_TITRE fromType(int type) {
    for (ACHAT_VENTE_TITRE ACHATVENTE : ACHAT_VENTE_TITRE.values()) {
      if (ACHATVENTE.type == type)
        return ACHATVENTE;
    }
    throw new IllegalStateException();
  }
}

