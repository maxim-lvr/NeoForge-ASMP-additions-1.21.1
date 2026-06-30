package net.maximlvr.asmpthingsadd.network.qte;

import net.maximlvr.asmpthingsadd.client.qte.ClientQteHandler;
import net.maximlvr.asmpthingsadd.AsmpThingsModAdd;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundStartLittleGirlQtePayload(
        int qteId,
        String sequence,
        int timeLimitTicks
) implements CustomPacketPayload {

    public static final Type<ClientboundStartLittleGirlQtePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AsmpThingsModAdd.MOD_ID, "start_little_girl_qte"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundStartLittleGirlQtePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ClientboundStartLittleGirlQtePayload::qteId,
                    ByteBufCodecs.STRING_UTF8,
                    ClientboundStartLittleGirlQtePayload::sequence,
                    ByteBufCodecs.VAR_INT,
                    ClientboundStartLittleGirlQtePayload::timeLimitTicks,
                    ClientboundStartLittleGirlQtePayload::new
            );

    public static void handle(ClientboundStartLittleGirlQtePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientQteHandler.openLittleGirlQte(payload));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
