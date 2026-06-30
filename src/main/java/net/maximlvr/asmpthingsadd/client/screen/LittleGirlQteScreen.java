package net.maximlvr.asmpthingsadd.client.screen;

import net.maximlvr.asmpthingsadd.network.qte.ServerboundLittleGirlQteResultPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public class LittleGirlQteScreen extends Screen {
    private final int qteId;
    private final String sequence;
    private final int timeLimitTicks;

    private String typed = "";
    private int age;
    private boolean finished;

    public LittleGirlQteScreen(int qteId, String sequence, int timeLimitTicks) {
        super(Component.literal("Petite fille"));
        this.qteId = qteId;
        this.sequence = sequence.toUpperCase(Locale.ROOT);
        this.timeLimitTicks = timeLimitTicks;
    }

    @Override
    public void tick() {
        super.tick();

        if (finished) {
            return;
        }

        age++;

        if (age > timeLimitTicks) {
            fail();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
        // Le QTE doit rester lisible sans pause/blur vanilla.
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int remainingTicks = Math.max(0, timeLimitTicks - age);

        guiGraphics.fill(0, 0, this.width, this.height, 0x77000000);
        guiGraphics.drawCenteredString(this.font, "Petite fille", centerX, centerY - 54, 0xD7B8FF);
        guiGraphics.drawCenteredString(this.font, "Tape les deux lettres", centerX, centerY - 30, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, spaced(sequence), centerX, centerY - 4, 0xFFFF55);
        guiGraphics.drawCenteredString(this.font, spaced(typed), centerX, centerY + 24, 0x55FF55);
        guiGraphics.drawCenteredString(this.font, (remainingTicks / 20) + "s", centerX, centerY + 48, 0xFF7777);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (finished || !Character.isLetter(codePoint)) {
            return true;
        }

        typed += Character.toUpperCase(codePoint);

        if (!sequence.startsWith(typed)) {
            fail();
            return true;
        }

        if (typed.length() >= sequence.length()) {
            succeed();
        }

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (finished) {
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            fail();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private String spaced(String value) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            if (i > 0) {
                builder.append(" ");
            }

            builder.append(value.charAt(i));
        }

        return builder.toString();
    }

    private void succeed() {
        finish(true);
    }

    private void fail() {
        finish(false);
    }

    private void finish(boolean success) {
        if (finished) {
            return;
        }

        finished = true;
        PacketDistributor.sendToServer(new ServerboundLittleGirlQteResultPayload(qteId, success));
        this.minecraft.setScreen(null);
    }

    @Override
    public void onClose() {
        if (!finished) {
            fail();
            return;
        }

        super.onClose();
    }
}
