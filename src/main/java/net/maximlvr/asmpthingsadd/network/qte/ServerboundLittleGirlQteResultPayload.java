package net.maximlvr.asmpthingsadd.network.qte;

import net.maximlvr.asmpthingsadd.AsmpThingsModAdd;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfTurnHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundLittleGirlQteResultPayload(
        int qteId,
        boolean success
) implements CustomPacketPayload {

    public static final Type<ServerboundLittleGirlQteResultPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AsmpThingsModAdd.MOD_ID, "little_girl_qte_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundLittleGirlQteResultPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ServerboundLittleGirlQteResultPayload::qteId,
                    ByteBufCodecs.BOOL,
                    ServerboundLittleGirlQteResultPayload::success,
                    ServerboundLittleGirlQteResultPayload::new
            );

    public static void handle(ServerboundLittleGirlQteResultPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                WerewolfTurnHandler.get().handleLittleGirlQteResult(serverPlayer, payload.qteId(), payload.success());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
