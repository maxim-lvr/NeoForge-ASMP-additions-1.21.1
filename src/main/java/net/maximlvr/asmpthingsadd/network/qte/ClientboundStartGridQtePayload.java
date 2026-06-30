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

public record ClientboundStartGridQtePayload(
        BlockPos stationPos,
        int qteId,
        int seed,
        int revealTicks,
        int timeLimitTicks
) implements CustomPacketPayload {

    public static final Type<ClientboundStartGridQtePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AsmpThingsModAdd.MOD_ID, "start_grid_qte"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundStartGridQtePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ClientboundStartGridQtePayload::stationPos,
                    ByteBufCodecs.VAR_INT,
                    ClientboundStartGridQtePayload::qteId,
                    ByteBufCodecs.VAR_INT,
                    ClientboundStartGridQtePayload::seed,
                    ByteBufCodecs.VAR_INT,
                    ClientboundStartGridQtePayload::revealTicks,
                    ByteBufCodecs.VAR_INT,
                    ClientboundStartGridQtePayload::timeLimitTicks,
                    ClientboundStartGridQtePayload::new
            );

    public static void handle(ClientboundStartGridQtePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientQteHandler.openGridQte(payload));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}