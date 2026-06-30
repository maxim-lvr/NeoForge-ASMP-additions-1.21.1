package net.maximlvr.asmpthingsadd.block.entity;

import net.maximlvr.asmpthingsadd.AsmpThingsModAdd;
import net.maximlvr.asmpthingsadd.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AsmpThingsModAdd.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WerewolfPedestalBlockEntity>> WEREWOLF_PEDESTAL =
            BLOCK_ENTITY_TYPES.register("werewolf_pedestal", () ->
                    BlockEntityType.Builder.of(
                            WerewolfPedestalBlockEntity::new,
                            ModBlocks.WEREWOLF_PEDESTAL.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WerewolfBrewingStationBlockEntity>> WEREWOLF_BREWING_STATION =
            BLOCK_ENTITY_TYPES.register("werewolf_brewing_station", () ->
                    BlockEntityType.Builder.of(
                            WerewolfBrewingStationBlockEntity::new,
                            ModBlocks.WEREWOLF_BREWING_STATION.get()
                    ).build(null)
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}