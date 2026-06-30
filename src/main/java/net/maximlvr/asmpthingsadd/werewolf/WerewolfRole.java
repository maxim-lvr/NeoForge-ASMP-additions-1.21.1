package net.maximlvr.asmpthingsadd.werewolf;

public enum WerewolfRole {
    LOUP_GAROU(1, "Loup garou"),
    CUPIDON(2, "Cupidon"),
    CHASSEUR(3, "Chasseur"),
    SORCIERE(4, "Sorciere"),
    PETITE_FILLE(5, "Petite fille"),
    VOYANTE(6, "Voyante"),
    VILLAGEOIS(7, "Villageois");

    private final int cardType;
    private final String displayName;

    WerewolfRole(int cardType, String displayName) {
        this.cardType = cardType;
        this.displayName = displayName;
    }

    public int cardType() {
        return cardType;
    }

    public String displayName() {
        return displayName;
    }
}
