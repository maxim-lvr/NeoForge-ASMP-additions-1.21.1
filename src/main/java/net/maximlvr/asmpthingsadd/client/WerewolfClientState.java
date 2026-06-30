package net.maximlvr.asmpthingsadd.client;

public final class WerewolfClientState {
    private static boolean gameActive;
    private static boolean night;

    private WerewolfClientState() {
    }

    public static boolean isGameActive() {
        return gameActive;
    }

    public static boolean isNight() {
        return gameActive && night;
    }

    public static void setGameState(boolean active, boolean nightState) {
        gameActive = active;
        night = active && nightState;
    }
}
