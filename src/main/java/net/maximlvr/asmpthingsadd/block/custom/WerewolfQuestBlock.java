package net.maximlvr.asmpthingsadd.block.custom;

import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WerewolfQuestBlock extends Block {
    public static final MapCodec<WerewolfQuestBlock> CODEC = simpleCodec(WerewolfQuestBlock::new);
    private static final VoxelShape SHAPE = box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public WerewolfQuestBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<WerewolfQuestBlock> codec() {
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

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.is(Items.TORCH)) {
            WerewolfTurnHandler.get().depositTorchWolfPenalty(serverPlayer, stack);
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.is(Items.LEATHER_CHESTPLATE)) {
            WerewolfTurnHandler.get().depositLittleGirlCape(serverPlayer, stack);
            return ItemInteractionResult.SUCCESS;
        }

        serverPlayer.sendSystemMessage(Component.literal("[LG] Il faut une torche ou une cape en cuir pour activer cette quete."));
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state,
                                               Level level,
                                               BlockPos pos,
                                               Player player,
                                               BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal("[LG] Il faut une torche ou une cape en cuir pour activer cette quete."));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state,
                                  BlockGetter level,
                                  BlockPos pos,
                                  CollisionContext context) {
        return SHAPE;
    }
}
