package net.maximlvr.asmpthingsadd.item.custom;

import net.maximlvr.asmpthingsadd.block.entity.WerewolfPedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WerewolfCompassItem extends CompassItem {
    private static final int SEARCH_CHUNK_RADIUS = 8;
    private static final int AUTO_REFRESH_TICKS = 20;

    public WerewolfCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }

        boolean success = assignRandomTarget(stack, serverLevel, serverPlayer, true);

        if (success) {
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (level.isClientSide()) {
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(entity instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // On évite de scanner tout le temps si la boussole est juste dans l'inventaire.
        boolean isInOffhand = serverPlayer.getOffhandItem() == stack;

        if (!isSelected && !isInOffhand) {
            return;
        }

        if (level.getGameTime() % AUTO_REFRESH_TICKS != 0) {
            return;
        }

        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);

        if (tracker == null || tracker.target().isEmpty()) {
            assignRandomTarget(stack, serverLevel, serverPlayer, false);
            return;
        }

        GlobalPos currentTarget = tracker.target().get();

        if (!isTargetStillValid(serverLevel, currentTarget)) {
            assignRandomTarget(stack, serverLevel, serverPlayer, true);
        }
    }

    private boolean assignRandomTarget(ItemStack compassStack,
                                       ServerLevel level,
                                       ServerPlayer player,
                                       boolean announce) {
        List<WerewolfPedestalBlockEntity> candidates = findFilledPedestalsAroundPlayer(level, player);

        if (candidates.isEmpty()) {
            compassStack.remove(DataComponents.LODESTONE_TRACKER);

            if (announce) {
                player.displayClientMessage(
                        Component.literal("Boussole LG : aucun item trouvé dans un rayon de "
                                + SEARCH_CHUNK_RADIUS + " chunks."),
                        true
                );
            }

            return false;
        }

        WerewolfPedestalBlockEntity target = candidates.get(level.random.nextInt(candidates.size()));
        BlockPos targetPos = target.getBlockPos();

        compassStack.set(
                DataComponents.LODESTONE_TRACKER,
                new LodestoneTracker(
                        Optional.of(GlobalPos.of(level.dimension(), targetPos)),
                        false
                )
        );

        if (announce) {
            String itemName = target.getDisplayedItem().getHoverName().getString();

            player.displayClientMessage(
                    Component.literal("Boussole LG : cible trouvée -> " + itemName),
                    true
            );
        }

        return true;
    }

    private boolean isTargetStillValid(ServerLevel level, GlobalPos target) {
        if (!target.dimension().equals(level.dimension())) {
            return false;
        }

        BlockPos pos = target.pos();

        if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return false;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof WerewolfPedestalBlockEntity pedestal)) {
            return false;
        }

        return pedestal.hasDisplayedItem();
    }

    private List<WerewolfPedestalBlockEntity> findFilledPedestalsAroundPlayer(ServerLevel level, ServerPlayer player) {
        List<WerewolfPedestalBlockEntity> pedestals = new ArrayList<>();

        int centerChunkX = player.chunkPosition().x;
        int centerChunkZ = player.chunkPosition().z;

        for (int chunkX = centerChunkX - SEARCH_CHUNK_RADIUS; chunkX <= centerChunkX + SEARCH_CHUNK_RADIUS; chunkX++) {
            for (int chunkZ = centerChunkZ - SEARCH_CHUNK_RADIUS; chunkZ <= centerChunkZ + SEARCH_CHUNK_RADIUS; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }

                LevelChunk chunk = level.getChunk(chunkX, chunkZ);

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockEntity blockEntity = entry.getValue();

                    if (blockEntity instanceof WerewolfPedestalBlockEntity pedestal && pedestal.hasDisplayedItem()) {
                        pedestals.add(pedestal);
                    }
                }
            }
        }

        return pedestals;
    }
}
