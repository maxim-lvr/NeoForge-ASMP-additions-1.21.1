package net.maximlvr.asmpthingsadd.werewolf;

import net.maximlvr.asmpthingsadd.block.entity.WerewolfPedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WerewolfPedestalManager {
    private static final WerewolfPedestalManager INSTANCE = new WerewolfPedestalManager();
    private static final int INIT_CHUNK_RADIUS = 8;

    private ResourceKey<Level> dimension;
    private final List<BlockPos> pedestalPositions = new ArrayList<>();

    private WerewolfPedestalManager() {
    }

    public static WerewolfPedestalManager get() {
        return INSTANCE;
    }

    public InitResult initAround(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<WerewolfPedestalBlockEntity> pedestals = findPedestalsAroundPlayer(level, player);
        List<ItemStack> loot = WerewolfLootPool.createMandatoryDailyLoot();

        if (pedestals.size() < loot.size()) {
            return new InitResult(false, pedestals.size(), 0, loot.size());
        }

        dimension = level.dimension();
        pedestalPositions.clear();

        for (WerewolfPedestalBlockEntity pedestal : pedestals) {
            pedestalPositions.add(pedestal.getBlockPos().immutable());
            pedestal.setDisplayedItem(ItemStack.EMPTY);
        }

        List<ItemStack> randomizedLoot = WerewolfLootPool.createRandomizedLootForPedestals(pedestals.size(), level.random);
        int placed = 0;

        for (int i = 0; i < pedestals.size(); i++) {
            ItemStack stack = randomizedLoot.get(i);
            pedestals.get(i).setDisplayedItem(stack);

            if (!stack.isEmpty()) {
                placed++;
            }
        }

        return new InitResult(true, pedestals.size(), placed, loot.size());
    }

    public int shuffleRemainingItems(MinecraftServer server) {
        if (dimension == null || pedestalPositions.isEmpty()) {
            return 0;
        }

        ServerLevel level = server.getLevel(dimension);

        if (level == null) {
            return 0;
        }

        List<WerewolfPedestalBlockEntity> pedestals = getRegisteredPedestals(level);
        List<ItemStack> remainingItems = new ArrayList<>();

        for (WerewolfPedestalBlockEntity pedestal : pedestals) {
            if (pedestal.hasDisplayedItem()) {
                remainingItems.add(pedestal.removeDisplayedItem());
            }
        }

        if (remainingItems.isEmpty()) {
            return 0;
        }

        Collections.shuffle(pedestals, new java.util.Random(level.random.nextLong()));
        Collections.shuffle(remainingItems, new java.util.Random(level.random.nextLong()));

        for (int i = 0; i < remainingItems.size() && i < pedestals.size(); i++) {
            pedestals.get(i).setDisplayedItem(remainingItems.get(i));
        }

        return remainingItems.size();
    }

    private List<WerewolfPedestalBlockEntity> getRegisteredPedestals(ServerLevel level) {
        List<WerewolfPedestalBlockEntity> pedestals = new ArrayList<>();

        pedestalPositions.removeIf(pos -> {
            if (!(level.getBlockEntity(pos) instanceof WerewolfPedestalBlockEntity pedestal)) {
                return true;
            }

            pedestals.add(pedestal);
            return false;
        });

        return pedestals;
    }

    private List<WerewolfPedestalBlockEntity> findPedestalsAroundPlayer(ServerLevel level, ServerPlayer player) {
        List<WerewolfPedestalBlockEntity> pedestals = new ArrayList<>();
        int centerChunkX = player.chunkPosition().x;
        int centerChunkZ = player.chunkPosition().z;

        for (int chunkX = centerChunkX - INIT_CHUNK_RADIUS; chunkX <= centerChunkX + INIT_CHUNK_RADIUS; chunkX++) {
            for (int chunkZ = centerChunkZ - INIT_CHUNK_RADIUS; chunkZ <= centerChunkZ + INIT_CHUNK_RADIUS; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }

                LevelChunk chunk = level.getChunk(chunkX, chunkZ);

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof WerewolfPedestalBlockEntity pedestal) {
                        pedestals.add(pedestal);
                    }
                }
            }
        }

        return pedestals;
    }

    public record InitResult(boolean success, int pedestalCount, int placedItems, int requiredItems) {
    }
}
