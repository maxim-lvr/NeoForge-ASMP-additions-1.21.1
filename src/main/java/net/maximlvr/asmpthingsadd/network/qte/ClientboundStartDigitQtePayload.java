package net.maximlvr.asmpthingsadd.network.qte;

import net.maximlvr.asmpthingsadd.AsmpThingsModAdd;
import net.maximlvr.asmpthingsadd.client.qte.ClientQteHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundStartDigitQtePayload(
        BlockPos stationPos,
        int qteId,
        int seed,
        int revealTicks,
        int timeLimitTicks
) implements CustomPacketPayload {

    public static final Type<ClientboundStartDigitQtePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AsmpThingsModAdd.MOD_ID, "start_digit_qte"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundStartDigitQtePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ClientboundStartDigitQtePayload::stationPos,
                    ByteBufCodecs.VAR_INT,
                    ClientboundStartDigitQtePayload::qteId,
                    ByteBufCodecs.VAR_INT,
                    ClientboundStartDigitQtePayload::seed,
                    ByteBufCodecs.VAR_INT,
                    ClientboundStartDigitQtePayload::revealTicks,
                    ByteBufCodecs.VAR_INT,
                    ClientboundStartDigitQtePayload::timeLimitTicks,
                    ClientboundStartDigitQtePayload::new
            );

    public static void handle(ClientboundStartDigitQtePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientQteHandler.openDigitQte(payload));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}