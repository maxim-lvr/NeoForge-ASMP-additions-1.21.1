package net.maximlvr.asmpthingsadd.block.custom;

import com.mojang.serialization.MapCodec;
import net.maximlvr.asmpthingsadd.block.entity.WerewolfPedestalBlockEntity;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfGame;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfRole;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfTurnHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class WerewolfPedestalBlock extends BaseEntityBlock {
    public static final MapCodec<WerewolfPedestalBlock> CODEC = simpleCodec(WerewolfPedestalBlock::new);

    private static final VoxelShape SHAPE = box(
            0.0D, 0.0D, 0.0D,
            16.0D, 16.0D, 16.0D
    );

    public WerewolfPedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<WerewolfPedestalBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack,
                                              BlockState state,
                                              Level level,
                                              BlockPos pos,
                                              Player player,
                                              InteractionHand hand,
                                              BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof WerewolfPedestalBlockEntity pedestalBlockEntity)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (pedestalBlockEntity.hasDisplayedItem()) {
            handleTakeDisplayedItem(pedestalBlockEntity, serverPlayer);
            return ItemInteractionResult.SUCCESS;
        }

        serverPlayer.displayClientMessage(Component.literal("[LG] Les items de pedestal sont geres par /lg init."), true);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state,
                                               Level level,
                                               BlockPos pos,
                                               Player player,
                                               BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof WerewolfPedestalBlockEntity pedestalBlockEntity)) {
            return InteractionResult.PASS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        if (!pedestalBlockEntity.hasDisplayedItem()) {
            return InteractionResult.PASS;
        }

        handleTakeDisplayedItem(pedestalBlockEntity, serverPlayer);
        return InteractionResult.SUCCESS;
    }

    private void handleTakeDisplayedItem(WerewolfPedestalBlockEntity pedestalBlockEntity, ServerPlayer serverPlayer) {
        ItemStack displayedItem = pedestalBlockEntity.getDisplayedItem();

        if (displayedItem.is(Items.LEATHER)) {
            if (WerewolfTurnHandler.get().collectSharedLeather(serverPlayer)) {
                pedestalBlockEntity.removeDisplayedItem();
            }
            return;
        }

        if (WerewolfGame.get().getRole(serverPlayer).orElse(null) != WerewolfRole.SORCIERE) {
            serverPlayer.displayClientMessage(Component.literal("[LG] Seule la sorciere peut recuperer ces items."), true);
            return;
        }

        ItemStack removedItem = pedestalBlockEntity.removeDisplayedItem();

        if (!serverPlayer.addItem(removedItem.copy())) {
            pedestalBlockEntity.setDisplayedItem(removedItem);
            serverPlayer.displayClientMessage(Component.literal("[LG] Inventaire plein."), true);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state,
                                  BlockGetter level,
                                  BlockPos pos,
                                  CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WerewolfPedestalBlockEntity(pos, state);
    }
}
