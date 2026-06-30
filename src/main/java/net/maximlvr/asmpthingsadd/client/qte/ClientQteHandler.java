package net.maximlvr.asmpthingsadd.client.qte;

import net.maximlvr.asmpthingsadd.client.screen.GridMemoryQteScreen;
import net.maximlvr.asmpthingsadd.network.qte.ClientboundStartGridQtePayload;
import net.maximlvr.asmpthingsadd.client.screen.DigitMemoryQteScreen;
import net.maximlvr.asmpthingsadd.network.qte.ClientboundStartDigitQtePayload;
import net.maximlvr.asmpthingsadd.client.screen.ColorMemoryQteScreen;
import net.maximlvr.asmpthingsadd.network.qte.ClientboundStartColorQtePayload;
import net.maximlvr.asmpthingsadd.client.screen.LittleGirlQteScreen;
import net.maximlvr.asmpthingsadd.network.qte.ClientboundStartLittleGirlQtePayload;
import net.minecraft.client.Minecraft;

public final class ClientQteHandler {

    private ClientQteHandler() {
    }

    public static void openGridQte(ClientboundStartGridQtePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();

        minecraft.setScreen(new GridMemoryQteScreen(
                payload.stationPos(),
                payload.qteId(),
                payload.seed(),
                payload.revealTicks(),
                payload.timeLimitTicks()
        ));
    }

    public static void openDigitQte(ClientboundStartDigitQtePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();

        minecraft.setScreen(new DigitMemoryQteScreen(
                payload.stationPos(),
                payload.qteId(),
                payload.seed(),
                payload.revealTicks(),
                payload.timeLimitTicks()
        ));
    }

    public static void openColorQte(ClientboundStartColorQtePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();

        minecraft.setScreen(new ColorMemoryQteScreen(
                payload.stationPos(),
                payload.qteId(),
                payload.seed(),
                payload.revealTicks(),
                payload.timeLimitTicks()
        ));
    }

    public static void openLittleGirlQte(ClientboundStartLittleGirlQtePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();

        minecraft.setScreen(new LittleGirlQteScreen(
                payload.qteId(),
                payload.sequence(),
                payload.timeLimitTicks()
        ));
    }
}
