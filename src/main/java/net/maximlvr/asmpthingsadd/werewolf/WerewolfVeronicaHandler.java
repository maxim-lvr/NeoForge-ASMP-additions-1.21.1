package net.maximlvr.asmpthingsadd.werewolf;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class WerewolfVeronicaHandler {
    public static final String VERONICA_NAME = "Veronica";

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Entity target = event.getTarget();

        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }

        if (!isVeronica(target)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (!isDay(level)) {
            player.sendSystemMessage(Component.literal("<Veronica> Reviens me voir pendant la journée.")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        WerewolfGame game = WerewolfGame.get();
        String partnerName = game.getPartnerName(player.server, player).orElse(null);

        if (partnerName == null) {
            player.sendSystemMessage(Component.literal("<Veronica> Salut ma(on) petit(e) cochon(ne) du sexe hard. Tu veux que je te lustre la fourche ou les nichons ? J'espère que cette fois tu n'as pas les mains pleine de terre..."));
            player.sendSystemMessage(Component.literal("<Veronica> Suis moi dans la grange je vais te faire montrer ma rondelle."));
            return;
        }

        player.sendSystemMessage(Component.literal("<Veronica> ESPECE DE SALE MERDE !!! T'AS PAS HONTE ? Remballe tes couilles (tes nichons) et fourre toi les dans le cul. Je sais que tu t'es fiancé(e) à " + partnerName + ". Oublie moi maintenant GROS(SE) CON(NE).")
                .withStyle(ChatFormatting.RED));
    }

    private boolean isVeronica(Entity entity) {
        return entity.hasCustomName()
                && VERONICA_NAME.equalsIgnoreCase(entity.getCustomName().getString());
    }

    private boolean isDay(Level level) {
        long dayTime = level.getDayTime() % 24000L;
        return dayTime >= 0L && dayTime < 12000L;
    }
}
