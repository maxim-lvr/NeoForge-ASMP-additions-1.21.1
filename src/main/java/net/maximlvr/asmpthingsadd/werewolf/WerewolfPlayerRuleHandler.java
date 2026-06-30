package net.maximlvr.asmpthingsadd.werewolf;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class WerewolfPlayerRuleHandler {
    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        Player player = event.getPlayer();

        if (!(player instanceof ServerPlayer serverPlayer) || !WerewolfGame.get().isRunning()
                || !WerewolfGame.get().isPlayer(serverPlayer)) {
            return;
        }

        ItemStack stack = event.getEntity().getItem().copy();
        event.setCanceled(true);

        if (!stack.isEmpty()) {
            player.getInventory().add(stack);
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && WerewolfGame.get().isRunning()
                && WerewolfGame.get().isPlayer(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WerewolfGame.get().handlePlayerDeath(player.server, player);
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        WerewolfGame.get().tickPendingEliminations(event.getServer());
    }
}
