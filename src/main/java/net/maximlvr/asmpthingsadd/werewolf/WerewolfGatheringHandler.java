package net.maximlvr.asmpthingsadd.werewolf;

import net.maximlvr.asmpthingsadd.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class WerewolfGatheringHandler {
    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = player.level().getBlockState(pos);
        WerewolfGatheringResource resource = getResource(state.getBlock());

        if (resource == null) {
            return;
        }

        event.setCanceled(true);

        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) {
            return;
        }

        WerewolfGame.GatheringResult result = WerewolfGame.get().hitGatheringBlock(player, resource);

        switch (result.status()) {
            case PROGRESS -> player.displayClientMessage(Component.literal(
                    "[LG] " + resource.displayName() + " : " + result.hits() + "/10"
            ), true);
            case GATHERED -> player.sendSystemMessage(Component.literal(
                    "[LG] Tu recuperes : " + resource.reward().getDefaultInstance().getHoverName().getString()
            ));
            case ALREADY_GATHERED -> player.displayClientMessage(Component.literal(
                    "[LG] Tu as deja recupere cette ressource pendant ce tour."
            ), true);
            case INACTIVE -> player.displayClientMessage(Component.literal(
                    "[LG] La recolte est disponible uniquement pendant une partie."
            ), true);
        }
    }

    private WerewolfGatheringResource getResource(Block block) {
        if (block == ModBlocks.WEREWOLF_OAK_LOG.get()) {
            return WerewolfGatheringResource.WOOD;
        }

        if (block == ModBlocks.WEREWOLF_COAL_ORE.get()) {
            return WerewolfGatheringResource.COAL;
        }

        if (block == ModBlocks.WEREWOLF_IRON_ORE.get()) {
            return WerewolfGatheringResource.IRON;
        }

        return null;
    }
}
