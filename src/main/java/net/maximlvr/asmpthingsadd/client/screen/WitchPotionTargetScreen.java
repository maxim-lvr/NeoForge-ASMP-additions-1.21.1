package net.maximlvr.asmpthingsadd.client.screen;

import com.mojang.authlib.GameProfile;
import net.maximlvr.asmpthingsadd.network.payload.SelectWitchPotionTargetPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WitchPotionTargetScreen extends Screen {
    private static final int SLOT_SIZE = 44;
    private static final int HEAD_SIZE = 24;
    private static final int COLS = 5;

    private final List<PlayerEntry> players;
    private final String potionKind;

    public WitchPotionTargetScreen(String playersData, String potionKind) {
        super(Component.literal("Potion de sorciere"));
        this.players = parsePlayers(playersData);
        this.potionKind = potionKind;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        int rows = Math.max(1, (int) Math.ceil(players.size() / (double) COLS));
        int gridWidth = COLS * SLOT_SIZE;
        int gridHeight = rows * SLOT_SIZE;
        int startX = (width - gridWidth) / 2;
        int startY = (height - gridHeight) / 2 + 12;

        for (int i = 0; i < players.size(); i++) {
            PlayerEntry entry = players.get(i);
            int col = i % COLS;
            int row = i / COLS;
            int x = startX + col * SLOT_SIZE;
            int y = startY + row * SLOT_SIZE;

            addRenderableWidget(Button.builder(Component.empty(), button -> {
                        PacketDistributor.sendToServer(new SelectWitchPotionTargetPayload(entry.uuid(), potionKind));
                        onClose();
                    })
                    .bounds(x, y, SLOT_SIZE - 4, SLOT_SIZE - 4)
                    .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(entry.name())))
                    .build());
        }
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(font, title, width / 2, 24, 0xD7B7FF);

        if (players.isEmpty()) {
            guiGraphics.drawCenteredString(
                    font,
                    Component.literal("Aucun joueur vivant dans la partie").withStyle(ChatFormatting.GRAY),
                    width / 2,
                    height / 2,
                    0xFFFFFF
            );
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderHeads(guiGraphics);
    }

    private void renderHeads(GuiGraphics guiGraphics) {
        int rows = Math.max(1, (int) Math.ceil(players.size() / (double) COLS));
        int gridWidth = COLS * SLOT_SIZE;
        int gridHeight = rows * SLOT_SIZE;
        int startX = (width - gridWidth) / 2;
        int startY = (height - gridHeight) / 2 + 12;

        for (int i = 0; i < players.size(); i++) {
            PlayerEntry entry = players.get(i);
            int col = i % COLS;
            int row = i / COLS;
            int slotX = startX + col * SLOT_SIZE;
            int slotY = startY + row * SLOT_SIZE;
            int headX = slotX + (SLOT_SIZE - HEAD_SIZE) / 2 - 2;
            int headY = slotY + 5;

            guiGraphics.renderItem(createPlayerHead(entry), headX, headY);
            guiGraphics.drawCenteredString(font, trimName(entry.name()), slotX + SLOT_SIZE / 2 - 2, slotY + 28, 0xFFFFFF);
        }
    }

    private ItemStack createPlayerHead(PlayerEntry entry) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);

        try {
            GameProfile profile = new GameProfile(UUID.fromString(entry.uuid()), entry.name());
            head.set(DataComponents.PROFILE, new ResolvableProfile(profile));
        } catch (IllegalArgumentException ignored) {
        }

        return head;
    }

    private String trimName(String name) {
        return name.length() <= 8 ? name : name.substring(0, 7) + ".";
    }

    private List<PlayerEntry> parsePlayers(String playersData) {
        List<PlayerEntry> parsed = new ArrayList<>();

        if (playersData == null || playersData.isBlank()) {
            return parsed;
        }

        for (String line : playersData.split("\n")) {
            String[] parts = line.split("\\|", 2);

            if (parts.length == 2 && !parts[0].isBlank() && !parts[1].isBlank()) {
                parsed.add(new PlayerEntry(parts[0], parts[1]));
            }
        }

        return parsed;
    }

    private record PlayerEntry(String uuid, String name) {
    }
}
