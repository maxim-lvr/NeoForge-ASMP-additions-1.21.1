package net.maximlvr.asmpthingsadd.item.custom;

import net.maximlvr.asmpthingsadd.network.payload.OpenWerewolfKillTargetPayload;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfGame;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfRole;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfTurnHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class RoleCardItem extends Item {
    public RoleCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }

        WerewolfGame game = WerewolfGame.get();

        if (game.getRole(serverPlayer).orElse(null) != WerewolfRole.LOUP_GAROU) {
            return InteractionResultHolder.pass(stack);
        }

        if (!WerewolfTurnHandler.get().canWerewolfChooseTarget(serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        PacketDistributor.sendToPlayer(serverPlayer, new OpenWerewolfKillTargetPayload(game.encodeLivingPlayers(serverPlayer.server)));
        return InteractionResultHolder.success(stack);
    }
}
