package net.maximlvr.asmpthingsadd.client.screen;

import net.maximlvr.asmpthingsadd.network.qte.ServerboundGridQteResultPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GridMemoryQteScreen extends Screen {
    private static final int GRID_SIZE = 4;
    private static final int TARGET_COUNT = 6;
    private static final int CELL_SIZE = 34;
    private static final int GAP = 6;

    private final BlockPos stationPos;
    private final int qteId;
    private final int revealTicks;
    private final int timeLimitTicks;
    private final List<Integer> targets = new ArrayList<>();
    private final Set<Integer> clicked = new HashSet<>();

    private int age = 0;
    private boolean finished = false;

    public GridMemoryQteScreen(BlockPos stationPos,
                               int qteId,
                               int seed,
                               int revealTicks,
                               int timeLimitTicks) {
        super(Component.literal("QTE Mémoire"));
        this.stationPos = stationPos;
        this.qteId = qteId;
        this.revealTicks = revealTicks;
        this.timeLimitTicks = timeLimitTicks;

        generateTargets(seed);
    }

    private void generateTargets(int seed) {
        List<Integer> all = new ArrayList<>();

        for (int i = 0; i < GRID_SIZE * GRID_SIZE; i++) {
            all.add(i);
        }

        Random random = new Random(seed);

        while (targets.size() < TARGET_COUNT && !all.isEmpty()) {
            int index = random.nextInt(all.size());
            targets.add(all.remove(index));
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
        // Désactive complètement le blur vanilla du screen.
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        int gridWidth = GRID_SIZE * CELL_SIZE + (GRID_SIZE - 1) * GAP;
        int startX = (this.width - gridWidth) / 2;
        int startY = (this.height - gridWidth) / 2;

        String title;

        if (age <= revealTicks) {
            title = "Mémorise les 6 cases blanches";
        } else {
            int remainingTicks = Math.max(0, revealTicks + timeLimitTicks - age);
            title = "Reclique les cases - " + (remainingTicks / 20) + "s";
        }

        guiGraphics.drawCenteredString(this.font, title, this.width / 2, startY - 30, 0xFFFFFF);

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int index = row * GRID_SIZE + col;

                int x = startX + col * (CELL_SIZE + GAP);
                int y = startY + row * (CELL_SIZE + GAP);

                int color = 0xFF333333;

                if (age <= revealTicks && targets.contains(index)) {
                    color = 0xFFFFFFFF;
                } else if (clicked.contains(index)) {
                    color = 0xFF66CC66;
                }

                guiGraphics.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, color);
                guiGraphics.fill(x, y, x + CELL_SIZE, y + 2, 0xFFFFFFFF);
                guiGraphics.fill(x, y, x + 2, y + CELL_SIZE, 0xFFFFFFFF);
                guiGraphics.fill(x + CELL_SIZE - 2, y, x + CELL_SIZE, y + CELL_SIZE, 0xFF000000);
                guiGraphics.fill(x, y + CELL_SIZE - 2, x + CELL_SIZE, y + CELL_SIZE, 0xFF000000);
            }
        }

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (finished) {
            return true;
        }

        if (age <= revealTicks) {
            return true;
        }

        int clickedIndex = getClickedCell(mouseX, mouseY);

        if (clickedIndex < 0) {
            return true;
        }

        if (!targets.contains(clickedIndex)) {
            fail();
            return true;
        }

        clicked.add(clickedIndex);

        if (clicked.size() >= TARGET_COUNT) {
            succeed();
        }

        return true;
    }

    private int getClickedCell(double mouseX, double mouseY) {
        int gridWidth = GRID_SIZE * CELL_SIZE + (GRID_SIZE - 1) * GAP;
        int startX = (this.width - gridWidth) / 2;
        int startY = (this.height - gridWidth) / 2;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int index = row * GRID_SIZE + col;

                int x = startX + col * (CELL_SIZE + GAP);
                int y = startY + row * (CELL_SIZE + GAP);

                if (mouseX >= x && mouseX <= x + CELL_SIZE && mouseY >= y && mouseY <= y + CELL_SIZE) {
                    return index;
                }
            }
        }

        return -1;
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