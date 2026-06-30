package net.maximlvr.asmpthingsadd.client;

import net.maximlvr.asmpthingsadd.AsmpThingsModAdd;
import net.maximlvr.asmpthingsadd.component.ModDataComponents;
import net.maximlvr.asmpthingsadd.item.ModItems;
import net.maximlvr.asmpthingsadd.AsmpThingsModAdd;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.LodestoneTracker;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class ModItemProperties {

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    ModItems.ROLE_CARD.get(),
                    ResourceLocation.fromNamespaceAndPath(AsmpThingsModAdd.MOD_ID, "role_view"),
                    (stack, level, entity, seed) -> {
                        Minecraft mc = Minecraft.getInstance();

                        if (mc.player == null) return 0.0f;

                        // Si la carte est rendue chez un autre joueur, on masque le rôle
                        if (entity != null && entity != mc.player) return 0.0f;

                        int roleType = stack.getOrDefault(ModDataComponents.ROLE_CARD_TYPE.get(), 0);

                        return switch (roleType) {
                            case 1 -> 0.1f; // loup
                            case 2 -> 0.2f; // cupidon
                            case 3 -> 0.3f; // chasseur
                            case 4 -> 0.4f; // sorciere
                            case 5 -> 0.5f; // petite fille
                            case 6 -> 0.6f; // voyante
                            case 7 -> 0.7f; // villageois
                            default -> 0.0f; // base
                        };
                    }
            );

            ItemProperties.register(
                    ModItems.WEREWOLF_COMPASS.get(),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "angle"),
                    new CompassItemPropertyFunction((clientLevel, stack, entity) -> {
                        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);

                        if (tracker == null || tracker.target().isEmpty()) {
                            return null;
                        }

                        return tracker.target().get();
                    })
            );

        });
    }
}
