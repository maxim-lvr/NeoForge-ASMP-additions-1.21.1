package net.maximlvr.asmpthingsadd.network.payload;

import net.maximlvr.asmpthingsadd.AsmpThingsModAdd;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SelectCrystalBallPlayerPayload(String playerUuid) implements CustomPacketPayload {
    public static final Type<SelectCrystalBallPlayerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AsmpThingsModAdd.MOD_ID, "select_crystal_ball_player"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectCrystalBallPlayerPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    SelectCrystalBallPlayerPayload::playerUuid,
                    SelectCrystalBallPlayerPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
