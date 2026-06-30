package net.maximlvr.asmpthingsadd.client.screen;

import net.maximlvr.asmpthingsadd.network.qte.ServerboundGridQteResultPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ColorMemoryQteScreen extends Screen {
    private static final int SEQUENCE_LENGTH = 6;

    private static final int RED = 0;
    private static final int BLUE = 1;
    private static final int GREEN = 2;
    private static final int YELLOW = 3;

    private final BlockPos stationPos;
    private final int qteId;
    private final int revealTicks;
    private final int timeLimitTicks;
    private final List<Integer> sequence = new ArrayList<>();

    private int age = 0;
    private int progress = 0;
    private boolean finished = false;

    public ColorMemoryQteScreen(BlockPos stationPos,
                                int qteId,
                                int seed,
                                int revealTicks,
                                int timeLimitTicks) {
        super(Component.literal("QTE Couleurs"));

        this.stationPos = stationPos;
        this.qteId = qteId;
        this.revealTicks = revealTicks;
        this.timeLimitTicks = timeLimitTicks;

        generateSequence(seed);
    }

    private void generateSequence(int seed) {
        Random random = new Random(seed);

        for (int i = 0; i < SEQUENCE_LENGTH; i++) {
            sequence.add(random.nextInt(4));
        }
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
        guiGraphics.fill(0, 0, this.width, this.height, 0x66000000);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (age <= revealTicks) {
            guiGraphics.drawCenteredString(
                    this.font,
                    "Mémorise la suite de couleurs",
                    centerX,
                    centerY - 80,
                    0xFFFFFF
            );

            drawSequence(guiGraphics, centerX, centerY - 35);
        } else {
            int remainingTicks = Math.max(0, revealTicks + timeLimitTicks - age);

            guiGraphics.drawCenteredString(
                    this.font,
                    "Reproduis la suite - " + (remainingTicks / 20) + "s",
                    centerX,
                    centerY - 105,
                    0xFFFFFF
            );

            guiGraphics.drawCenteredString(
                    this.font,
                    "Progression : " + progress + "/" + SEQUENCE_LENGTH,
                    centerX,
                    centerY - 85,
                    0xAAAAAA
            );

            drawButtons(guiGraphics, centerX, centerY);
        }
    }

    private void drawSequence(GuiGraphics guiGraphics, int centerX, int y) {
        int size = 28;
        int gap = 8;
        int totalWidth = SEQUENCE_LENGTH * size + (SEQUENCE_LENGTH - 1) * gap;
        int startX = centerX - totalWidth / 2;

        for (int i = 0; i < sequence.size(); i++) {
            int colorIndex = sequence.get(i);
            int x = startX + i * (size + gap);

            guiGraphics.fill(x, y, x + size, y + size, getColor(colorIndex));
            guiGraphics.fill(x, y, x + size, y + 2, 0xFFFFFFFF);
            guiGraphics.fill(x, y, x + 2, y + size, 0xFFFFFFFF);
            guiGraphics.fill(x + size - 2, y, x + size, y + size, 0xFF000000);
            guiGraphics.fill(x, y + size - 2, x + size, y + size, 0xFF000000);
        }
    }

    private void drawButtons(GuiGraphics guiGraphics, int centerX, int centerY) {
        int size = 54;
        int gap = 12;

        int startX = centerX - size - gap / 2;
        int startY = centerY - size - gap / 2;

        drawButton(guiGraphics, startX, startY, size, RED, "R");
        drawButton(guiGraphics, startX + size + gap, startY, size, BLUE, "B");
        drawButton(guiGraphics, startX, startY + size + gap, size, GREEN, "V");
        drawButton(guiGraphics, startX + size + gap, startY + size + gap, size, YELLOW, "J");
    }

    private void drawButton(GuiGraphics guiGraphics, int x, int y, int size, int colorIndex, String label) {
        guiGraphics.fill(x, y, x + size, y + size, getColor(colorIndex));
        guiGraphics.fill(x, y, x + size, y + 2, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 2, y + size, 0xFFFFFFFF);
        guiGraphics.fill(x + size - 2, y, x + size, y + size, 0xFF000000);
        guiGraphics.fill(x, y + size - 2, x + size, y + size, 0xFF000000);

        guiGraphics.drawCenteredString(
                this.font,
                label,
                x + size / 2,
                y + size / 2 - 4,
                0xFFFFFFFF
        );
    }

    private int getColor(int colorIndex) {
        return switch (colorIndex) {
            case RED -> 0xFFCC3333;
            case BLUE -> 0xFF3366CC;
            case GREEN -> 0xFF33AA55;
            case YELLOW -> 0xFFE6CC33;
            default -> 0xFFFFFFFF;
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (finished) {
            return true;
        }

        if (age <= revealTicks) {
            return true;
        }

        int clickedColor = getClickedColor(mouseX, mouseY);

        if (clickedColor < 0) {
            return true;
        }

        checkColor(clickedColor);

        return true;
    }

    private int getClickedColor(double mouseX, double mouseY) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int size = 54;
        int gap = 12;

        int startX = centerX - size - gap / 2;
        int startY = centerY - size - gap / 2;

        if (isInside(mouseX, mouseY, startX, startY, size)) {
            return RED;
        }

        if (isInside(mouseX, mouseY, startX + size + gap, startY, size)) {
            return BLUE;
        }

        if (isInside(mouseX, mouseY, startX, startY + size + gap, size)) {
            return GREEN;
        }

        if (isInside(mouseX, mouseY, startX + size + gap, startY + size + gap, size)) {
            return YELLOW;
        }

        return -1;
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int size) {
        return mouseX >= x && mouseX <= x + size && mouseY >= y && mouseY <= y + size;
    }

    private void checkColor(int color) {
        if (progress < 0 || progress >= sequence.size()) {
            fail();
            return;
        }

        int expected = sequence.get(progress);

        if (color != expected) {
            fail();
            return;
        }

        progress++;

        if (progress >= SEQUENCE_LENGTH) {
            succeed();
        }
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

        if (age <= revealTicks) {
            return true;
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_R -> checkColor(RED);
            case GLFW.GLFW_KEY_B -> checkColor(BLUE);
            case GLFW.GLFW_KEY_V, GLFW.GLFW_KEY_G -> checkColor(GREEN);
            case GLFW.GLFW_KEY_J, GLFW.GLFW_KEY_Y -> checkColor(YELLOW);
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }

        return true;
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