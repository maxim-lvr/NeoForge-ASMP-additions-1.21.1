package net.maximlvr.asmpthingsadd.client;

import net.maximlvr.asmpthingsadd.AsmpThingsModAdd;
import net.maximlvr.asmpthingsadd.block.entity.ModBlockEntities;
import net.maximlvr.asmpthingsadd.client.renderer.WerewolfPedestalRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;


@EventBusSubscriber(
        modid = AsmpThingsModAdd.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ModBlockEntityRenderers {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.WEREWOLF_PEDESTAL.get(),
                WerewolfPedestalRenderer::new
        );
    }
}