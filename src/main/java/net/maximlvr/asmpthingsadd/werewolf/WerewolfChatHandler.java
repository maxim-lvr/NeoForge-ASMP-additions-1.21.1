package net.maximlvr.asmpthingsadd.werewolf;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;

public class WerewolfChatHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onServerChat(ServerChatEvent event) {
        WerewolfGame game = WerewolfGame.get();

        if (!game.isRunning()) {
            return;
        }

        event.setCanceled(true);

        ServerPlayer player = event.getPlayer();
        player.server.getPlayerList()
                .getPlayers()
                .stream()
                .filter(game::isAdmin)
                .forEach(admin -> admin.sendSystemMessage(Component.literal("<" + player.getGameProfile().getName() + "> " + event.getRawText())));
    }
}
