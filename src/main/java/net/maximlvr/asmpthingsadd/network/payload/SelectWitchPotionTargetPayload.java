package net.maximlvr.asmpthingsadd.network.payload;

import net.maximlvr.asmpthingsadd.AsmpThingsModAdd;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SelectWitchPotionTargetPayload(String playerUuid, String potionKind) implements CustomPacketPayload {
    public static final Type<SelectWitchPotionTargetPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AsmpThingsModAdd.MOD_ID, "select_witch_potion_target"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectWitchPotionTargetPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    SelectWitchPotionTargetPayload::playerUuid,
                    ByteBufCodecs.STRING_UTF8,
                    SelectWitchPotionTargetPayload::potionKind,
                    SelectWitchPotionTargetPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
