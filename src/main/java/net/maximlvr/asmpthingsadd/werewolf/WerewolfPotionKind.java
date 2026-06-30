package net.maximlvr.asmpthingsadd.werewolf;

public enum WerewolfPotionKind {
    PROTECTION,
    DEATH;

    public WerewolfPotionKind next() {
        return this == PROTECTION ? DEATH : PROTECTION;
    }

    public String displayName() {
        return switch (this) {
            case PROTECTION -> "Protection";
            case DEATH -> "Mort";
        };
    }
}