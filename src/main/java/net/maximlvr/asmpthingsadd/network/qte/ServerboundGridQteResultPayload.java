package net.maximlvr.asmpthingsadd.network.qte;

import net.maximlvr.asmpthingsadd.block.entity.WerewolfBrewingStationBlockEntity;
import net.maximlvr.asmpthingsadd.AsmpThingsModAdd;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundGridQteResultPayload(
        BlockPos stationPos,
        int qteId,
        boolean success
) implements CustomPacketPayload {

    public static final Type<ServerboundGridQteResultPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AsmpThingsModAdd.MOD_ID, "grid_qte_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundGridQteResultPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ServerboundGridQteResultPayload::stationPos,
                    ByteBufCodecs.VAR_INT,
                    ServerboundGridQteResultPayload::qteId,
                    ByteBufCodecs.BOOL,
                    ServerboundGridQteResultPayload::success,
                    ServerboundGridQteResultPayload::new
            );

    public static void handle(ServerboundGridQteResultPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            BlockEntity blockEntity = serverPlayer.level().getBlockEntity(payload.stationPos());

            if (!(blockEntity instanceof WerewolfBrewingStationBlockEntity station)) {
                return;
            }

            station.handleGridQteResult(serverPlayer, payload.qteId(), payload.success());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}