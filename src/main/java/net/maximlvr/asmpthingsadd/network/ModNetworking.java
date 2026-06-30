package net.maximlvr.asmpthingsadd.network;

import net.maximlvr.asmpthingsadd.component.ModDataComponents;
import net.maximlvr.asmpthingsadd.item.ModItems;
import net.maximlvr.asmpthingsadd.network.payload.OpenCrystalBallPayload;
import net.maximlvr.asmpthingsadd.network.payload.OpenWitchPotionTargetPayload;
import net.maximlvr.asmpthingsadd.network.payload.OpenWerewolfKillTargetPayload;
import net.maximlvr.asmpthingsadd.network.payload.SelectCrystalBallPlayerPayload;
import net.maximlvr.asmpthingsadd.network.payload.SelectWitchPotionTargetPayload;
import net.maximlvr.asmpthingsadd.network.payload.SelectWerewolfKillTargetPayload;
import net.maximlvr.asmpthingsadd.network.payload.WerewolfGameStatePayload;
import net.maximlvr.asmpthingsadd.network.qte.ClientboundStartDigitQtePayload;
import net.maximlvr.asmpthingsadd.network.qte.ClientboundStartGridQtePayload;
import net.maximlvr.asmpthingsadd.network.qte.ClientboundStartLittleGirlQtePayload;
import net.maximlvr.asmpthingsadd.network.qte.ServerboundGridQteResultPayload;
import net.maximlvr.asmpthingsadd.network.qte.ServerboundLittleGirlQteResultPayload;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfGame;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfTurnHandler;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfRole;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.maximlvr.asmpthingsadd.network.qte.ClientboundStartColorQtePayload;

import java.util.UUID;


public class ModNetworking {

    public static void register(IEventBus eventBus) {
        eventBus.addListener(ModNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");


        registrar.playToClient(
                OpenCrystalBallPayload.TYPE,
                OpenCrystalBallPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() ->
                            net.maximlvr.asmpthingsadd.client.ClientHooks.openCrystalBallScreen(payload.playersData())
                    );
                }
        );

        registrar.playToClient(
                OpenWitchPotionTargetPayload.TYPE,
                OpenWitchPotionTargetPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        net.maximlvr.asmpthingsadd.client.ClientHooks.openWitchPotionTargetScreen(payload.playersData(), payload.potionKind())
                )
        );

        registrar.playToClient(
                OpenWerewolfKillTargetPayload.TYPE,
                OpenWerewolfKillTargetPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        net.maximlvr.asmpthingsadd.client.ClientHooks.openWerewolfKillTargetScreen(payload.playersData())
                )
        );

        registrar.playToClient(
                WerewolfGameStatePayload.TYPE,
                WerewolfGameStatePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        net.maximlvr.asmpthingsadd.client.ClientHooks.setWerewolfGameState(payload.active(), payload.night())
                )
        );

        registrar.playToServer(
                SelectCrystalBallPlayerPayload.TYPE,
                SelectCrystalBallPlayerPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (!(context.player() instanceof ServerPlayer player)) {
                            return;
                        }

                        revealCrystalBallRole(player, payload.playerUuid());
                    });
                }
        );

        registrar.playToServer(
                SelectWitchPotionTargetPayload.TYPE,
                SelectWitchPotionTargetPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer player) {
                            applyWitchPotion(player, payload.playerUuid(), payload.potionKind());
                        }
                    });
                }
        );

        registrar.playToServer(
                SelectWerewolfKillTargetPayload.TYPE,
                SelectWerewolfKillTargetPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        selectWerewolfTarget(player, payload.playerUuid());
                    }
                })
        );



        registrar.playToClient(
                ClientboundStartGridQtePayload.TYPE,
                ClientboundStartGridQtePayload.STREAM_CODEC,
                ClientboundStartGridQtePayload::handle
        );

        registrar.playToServer(
                ServerboundGridQteResultPayload.TYPE,
                ServerboundGridQteResultPayload.STREAM_CODEC,
                ServerboundGridQteResultPayload::handle
        );

        registrar.playToClient(
                ClientboundStartDigitQtePayload.TYPE,
                ClientboundStartDigitQtePayload.STREAM_CODEC,
                ClientboundStartDigitQtePayload::handle
        );

        registrar.playToClient(
                ClientboundStartColorQtePayload.TYPE,
                ClientboundStartColorQtePayload.STREAM_CODEC,
                ClientboundStartColorQtePayload::handle
        );

        registrar.playToClient(
                ClientboundStartLittleGirlQtePayload.TYPE,
                ClientboundStartLittleGirlQtePayload.STREAM_CODEC,
                ClientboundStartLittleGirlQtePayload::handle
        );

        registrar.playToServer(
                ServerboundLittleGirlQteResultPayload.TYPE,
                ServerboundLittleGirlQteResultPayload.STREAM_CODEC,
                ServerboundLittleGirlQteResultPayload::handle
        );

    }

    private static String sanitize(String value, int maxLength) {
        String trimmed = value == null ? "" : value.trim();

        if (trimmed.length() <= maxLength) {
            return trimmed;
        }

        return trimmed.substring(0, maxLength);
    }



    private static void revealCrystalBallRole(ServerPlayer player, String uuidText) {
        WerewolfGame game = WerewolfGame.get();

        if (!game.isRunning()) {
            player.sendSystemMessage(Component.literal("[LG] La partie n'est pas lancee."));
            return;
        }

        if (game.getRole(player).orElse(null) != WerewolfRole.VOYANTE) {
            player.sendSystemMessage(Component.literal("[LG] Seule la voyante peut utiliser la boule de cristal."));
            return;
        }

        UUID targetUuid;

        try {
            targetUuid = UUID.fromString(uuidText);
        } catch (IllegalArgumentException exception) {
            player.sendSystemMessage(Component.literal("[LG] Vision invalide."));
            return;
        }

        WerewolfRole role = game.getRole(targetUuid).orElse(null);
        ServerPlayer target = player.server.getPlayerList().getPlayer(targetUuid);

        if (role == null || target == null) {
            player.sendSystemMessage(Component.literal("[LG] Ce joueur n'est pas dans la partie."));
            return;
        }

        player.sendSystemMessage(Component.literal("[LG] Vision : " + target.getGameProfile().getName()
                        + " est " + role.displayName() + ".")
                .withStyle(ChatFormatting.AQUA));
    }

    private static void applyWitchPotion(ServerPlayer player, String uuidText, String potionKind) {
        UUID targetUuid;

        try {
            targetUuid = UUID.fromString(uuidText);
        } catch (IllegalArgumentException exception) {
            player.sendSystemMessage(Component.literal("[LG] Cible invalide."));
            return;
        }

        WerewolfTurnHandler.get().applyWitchPotion(player, targetUuid, potionKind);
    }

    private static void selectWerewolfTarget(ServerPlayer player, String uuidText) {
        if (uuidText == null || uuidText.isBlank()) {
            WerewolfTurnHandler.get().selectNoWerewolfTarget(player);
            return;
        }

        UUID targetUuid;

        try {
            targetUuid = UUID.fromString(uuidText);
        } catch (IllegalArgumentException exception) {
            player.sendSystemMessage(Component.literal("[LG] Cible invalide."));
            return;
        }

        WerewolfTurnHandler.get().selectWerewolfTarget(player, targetUuid);
    }


}
