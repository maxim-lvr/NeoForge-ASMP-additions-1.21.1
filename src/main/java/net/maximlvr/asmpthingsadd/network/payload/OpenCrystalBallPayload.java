package net.maximlvr.asmpthingsadd.network.payload;

import net.maximlvr.asmpthingsadd.AsmpThingsModAdd;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenCrystalBallPayload(String playersData) implements CustomPacketPayload {
    public static final Type<OpenCrystalBallPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AsmpThingsModAdd.MOD_ID, "open_crystal_ball"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCrystalBallPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    OpenCrystalBallPayload::playersData,
                    OpenCrystalBallPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
