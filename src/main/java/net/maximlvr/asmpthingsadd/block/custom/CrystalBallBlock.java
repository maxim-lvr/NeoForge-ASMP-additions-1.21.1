package net.maximlvr.asmpthingsadd.block.custom;

import com.mojang.serialization.MapCodec;
import net.maximlvr.asmpthingsadd.network.payload.OpenCrystalBallPayload;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfGame;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfRole;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;

public class CrystalBallBlock extends Block {
    public static final MapCodec<CrystalBallBlock> CODEC = simpleCodec(CrystalBallBlock::new);
    private static final VoxelShape SHAPE = box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D);

    public CrystalBallBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<CrystalBallBlock> codec() {
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

        WerewolfGame game = WerewolfGame.get();

        if (!game.isRunning()) {
            serverPlayer.sendSystemMessage(Component.literal("[LG] La boule de cristal ne montre rien hors partie."));
            return InteractionResult.SUCCESS;
        }

        if (game.getRole(serverPlayer).orElse(null) != WerewolfRole.VOYANTE) {
            serverPlayer.sendSystemMessage(Component.literal("[LG] Seule la voyante peut lire la boule de cristal."));
            return InteractionResult.SUCCESS;
        }

        PacketDistributor.sendToPlayer(serverPlayer, new OpenCrystalBallPayload(game.encodeLivingPlayers(serverPlayer.server)));
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
