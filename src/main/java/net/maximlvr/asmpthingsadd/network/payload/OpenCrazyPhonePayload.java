package net.maximlvr.asmpthingsadd.network.payload;

import net.maximlvr.asmpthingsadd.AsmpThingsModAdd;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenCrazyPhonePayload(boolean mainHand) implements CustomPacketPayload {
    public static final Type<OpenCrazyPhonePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AsmpThingsModAdd.MOD_ID, "open_crazy_phone"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCrazyPhonePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    OpenCrazyPhonePayload::mainHand,
                    OpenCrazyPhonePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
