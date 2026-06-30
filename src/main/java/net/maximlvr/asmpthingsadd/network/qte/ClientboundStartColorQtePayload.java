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

public record ClientboundStartColorQtePayload(
        BlockPos stationPos,
        int qteId,
        int seed,
        int revealTicks,
        int timeLimitTicks
) implements CustomPacketPayload {

    public static final Type<ClientboundStartColorQtePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AsmpThingsModAdd.MOD_ID, "start_color_qte"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundStartColorQtePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ClientboundStartColorQtePayload::stationPos,
                    ByteBufCodecs.VAR_INT,
                    ClientboundStartColorQtePayload::qteId,
                    ByteBufCodecs.VAR_INT,
                    ClientboundStartColorQtePayload::seed,
                    ByteBufCodecs.VAR_INT,
                    ClientboundStartColorQtePayload::revealTicks,
                    ByteBufCodecs.VAR_INT,
                    ClientboundStartColorQtePayload::timeLimitTicks,
                    ClientboundStartColorQtePayload::new
            );

    public static void handle(ClientboundStartColorQtePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientQteHandler.openColorQte(payload));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}