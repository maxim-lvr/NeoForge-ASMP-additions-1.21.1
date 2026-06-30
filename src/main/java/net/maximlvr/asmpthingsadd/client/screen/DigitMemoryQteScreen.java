package net.maximlvr.asmpthingsadd.client.screen;

import net.maximlvr.asmpthingsadd.network.qte.ServerboundGridQteResultPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class DigitMemoryQteScreen extends Screen {
    private static final int DIGIT_COUNT = 6;

    private final BlockPos stationPos;
    private final int qteId;
    private final int revealTicks;
    private final int timeLimitTicks;
    private final String sequence;

    private String typed = "";
    private int age = 0;
    private boolean finished = false;

    public DigitMemoryQteScreen(BlockPos stationPos,
                                int qteId,
                                int seed,
                                int revealTicks,
                                int timeLimitTicks) {
        super(Component.literal("QTE Chiffres"));

        this.stationPos = stationPos;
        this.qteId = qteId;
        this.revealTicks = revealTicks;
        this.timeLimitTicks = timeLimitTicks;
        this.sequence = generateSequence(seed);
    }

    private String generateSequence(int seed) {
        Random random = new Random(seed);
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < DIGIT_COUNT; i++) {
            builder.append(random.nextInt(10));
        }

        return builder.toString();
    }

    @Override
    public void tick() {
        super.tick();

        if (finished) {
            return;
        }

        age++;

        if (age > revealTicks + timeLimitTicks) {
            fail();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
        // Pas de blur vanilla.
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        guiGraphics.fill(0, 0, this.width, this.height, 0x66000000);

        if (age <= revealTicks) {
            guiGraphics.drawCenteredString(
                    this.font,
                    "Mémorise la suite",
                    centerX,
                    centerY - 50,
                    0xFFFFFF
            );

            guiGraphics.drawCenteredString(
                    this.font,
                    spaced(sequence),
                    centerX,
                    centerY - 15,
                    0xFFFF55
            );
        } else {
            int remainingTicks = Math.max(0, revealTicks + timeLimitTicks - age);

            guiGraphics.drawCenteredString(
                    this.font,
                    "Retape les 6 chiffres - " + (remainingTicks / 20) + "s",
                    centerX,
                    centerY - 50,
                    0xFFFFFF
            );

            guiGraphics.drawCenteredString(
                    this.font,
                    spaced(typed),
                    centerX,
                    centerY - 15,
                    0x55FF55
            );

            guiGraphics.drawCenteredString(
                    this.font,
                    "Utilise les touches 0-9",
                    centerX,
                    centerY + 20,
                    0xAAAAAA
            );
        }
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

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (finished) {
            return true;
        }

        if (age <= revealTicks) {
            return true;
        }

        if (!Character.isDigit(codePoint)) {
            return true;
        }

        typed += codePoint;

        if (!sequence.startsWith(typed)) {
            fail();
            return true;
        }

        if (typed.length() >= DIGIT_COUNT) {
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

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !typed.isEmpty() && age > revealTicks) {
            typed = typed.substring(0, typed.length() - 1);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void succeed() {
        if (finished) {
            return;
        }

        finished = true;

        PacketDistributor.sendToServer(new ServerboundGridQteResultPayload(
                stationPos,
                qteId,
                true
        ));

        this.minecraft.setScreen(null);
    }

    private void fail() {
        if (finished) {
            return;
        }

        finished = true;

        PacketDistributor.sendToServer(new ServerboundGridQteResultPayload(
                stationPos,
                qteId,
                false
        ));

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