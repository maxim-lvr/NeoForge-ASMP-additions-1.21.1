package net.maximlvr.asmpthingsadd.item.custom;

import net.maximlvr.asmpthingsadd.network.payload.OpenWitchPotionTargetPayload;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfGame;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfTurnHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class WerewolfPotionItem extends Item {
    private final Kind kind;
    private final boolean finished;

    public WerewolfPotionItem(Properties properties, Kind kind, boolean finished) {
        super(properties);
        this.kind = kind;
        this.finished = finished;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!finished) {
            serverPlayer.sendSystemMessage(Component.literal("[LG] Utilise cette potion de base sur la station de sorciere."));
            return InteractionResultHolder.fail(stack);
        }

        String potionKind = kind.serializedName();

        if (!WerewolfTurnHandler.get().canOpenWitchPotion(serverPlayer, potionKind)) {
            return InteractionResultHolder.fail(stack);
        }

        PacketDistributor.sendToPlayer(
                serverPlayer,
                new OpenWitchPotionTargetPayload(WerewolfGame.get().encodeLivingPlayers(serverPlayer.server), potionKind)
        );
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return finished || super.isFoil(stack);
    }

    public enum Kind {
        PROTECTION("protection"),
        POISON("poison");

        private final String serializedName;

        Kind(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }
    }
}
