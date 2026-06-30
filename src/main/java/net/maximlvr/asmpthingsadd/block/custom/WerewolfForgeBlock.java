package net.maximlvr.asmpthingsadd.block.custom;

import com.mojang.serialization.MapCodec;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfGame;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfTurnHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

public class WerewolfForgeBlock extends Block {
    public static final MapCodec<WerewolfForgeBlock> CODEC = simpleCodec(WerewolfForgeBlock::new);
    private static final VoxelShape SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    public WerewolfForgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<WerewolfForgeBlock> codec() {
        return CODEC;
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

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        if (WerewolfTurnHandler.get().canCraftLittleGirlCape()
                && WerewolfTurnHandler.get().craftLittleGirlCape(serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (has(serverPlayer, Items.STICK, 1) && has(serverPlayer, Items.COAL, 1)) {
            consume(serverPlayer, Items.STICK, 1);
            consume(serverPlayer, Items.COAL, 1);
            serverPlayer.getInventory().add(new ItemStack(Items.TORCH));
            serverPlayer.sendSystemMessage(Component.literal("[LG] Tu fabriques une torche."));
            return InteractionResult.SUCCESS;
        }

        if (has(serverPlayer, Items.COAL, 1) && has(serverPlayer, Items.IRON_INGOT, 1)) {
            consume(serverPlayer, Items.COAL, 1);
            consume(serverPlayer, Items.IRON_INGOT, 1);
            WerewolfGame.get().tryCraftBullet(serverPlayer);
            return InteractionResult.SUCCESS;
        }

        serverPlayer.sendSystemMessage(Component.literal("[LG] Forge : il faut stick + charbon ou charbon + fer."));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack,
                                              BlockState state,
                                              Level level,
                                              BlockPos pos,
                                              Player player,
                                              net.minecraft.world.InteractionHand hand,
                                              BlockHitResult hitResult) {
        useWithoutItem(state, level, pos, player, hitResult);
        return ItemInteractionResult.SUCCESS;
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

    private boolean consume(ServerPlayer player, net.minecraft.world.item.Item item, int count) {
        player.getInventory().clearOrCountMatchingItems(stack -> stack.is(item), count, player.inventoryMenu.getCraftSlots());
        return true;
    }

    private boolean has(ServerPlayer player, net.minecraft.world.item.Item item, int count) {
        return player.getInventory().countItem(item) >= count;
    }
}
