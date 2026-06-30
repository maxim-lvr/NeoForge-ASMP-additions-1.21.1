package net.maximlvr.asmpthingsadd.component;

import com.mojang.serialization.Codec;
import net.maximlvr.asmpthingsadd.AsmpThingsModAdd;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(
                    Registries.DATA_COMPONENT_TYPE,
                    AsmpThingsModAdd.MOD_ID
            );


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ROLE_CARD_TYPE =
            DATA_COMPONENTS.registerComponentType("role_card_type",
                    builder -> builder
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
            );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}