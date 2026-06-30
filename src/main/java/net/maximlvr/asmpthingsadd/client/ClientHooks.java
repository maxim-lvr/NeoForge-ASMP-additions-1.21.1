package net.maximlvr.asmpthingsadd.client;


import net.maximlvr.asmpthingsadd.client.screen.CrystalBallScreen;
import net.maximlvr.asmpthingsadd.client.screen.WerewolfKillTargetScreen;
import net.maximlvr.asmpthingsadd.client.screen.WitchPotionTargetScreen;
import net.minecraft.client.Minecraft;

public class ClientHooks {


    public static void openCrystalBallScreen(String playersData) {
        Minecraft.getInstance().setScreen(new CrystalBallScreen(playersData));
    }

    public static void openWitchPotionTargetScreen(String playersData, String potionKind) {
        Minecraft.getInstance().setScreen(new WitchPotionTargetScreen(playersData, potionKind));
    }

    public static void openWerewolfKillTargetScreen(String playersData) {
        Minecraft.getInstance().setScreen(new WerewolfKillTargetScreen(playersData));
    }

    public static void setWerewolfGameState(boolean active, boolean night) {
        WerewolfClientState.setGameState(active, night);
    }
}
