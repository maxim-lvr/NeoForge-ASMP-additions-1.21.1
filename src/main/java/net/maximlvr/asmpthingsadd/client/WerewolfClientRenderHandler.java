package net.maximlvr.asmpthingsadd.client;

import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.util.TriState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WerewolfClientRenderHandler {
    private final Map<UUID, HeldItems> hiddenHeldItems = new HashMap<>();

    @SubscribeEvent
    public void onRenderNameTag(RenderNameTagEvent event) {
        if (WerewolfClientState.isGameActive() && event.getEntity() instanceof Player) {
            event.setCanRender(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public void onRenderFog(ViewportEvent.RenderFog event) {
        if (!WerewolfClientState.isNight()) {
            return;
        }

        event.setNearPlaneDistance(0.05F);
        event.setFarPlaneDistance(140.0F);
        event.setFogShape(FogShape.CYLINDER);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (!WerewolfClientState.isNight()) {
            return;
        }

        event.setRed(0.22F);
        event.setGreen(0.22F);
        event.setBlue(0.24F);
    }

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (!WerewolfClientState.isGameActive()) {
            return;
        }

        Player player = event.getEntity();

        if (Minecraft.getInstance().player == null || player.getUUID().equals(Minecraft.getInstance().player.getUUID())) {
            return;
        }

        hiddenHeldItems.put(player.getUUID(), new HeldItems(
                player.getMainHandItem().copy(),
                player.getOffhandItem().copy()
        ));
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        HeldItems heldItems = hiddenHeldItems.remove(event.getEntity().getUUID());

        if (heldItems == null) {
            return;
        }

        event.getEntity().setItemInHand(InteractionHand.MAIN_HAND, heldItems.mainHand());
        event.getEntity().setItemInHand(InteractionHand.OFF_HAND, heldItems.offHand());
    }

    private record HeldItems(ItemStack mainHand, ItemStack offHand) {
    }
}
