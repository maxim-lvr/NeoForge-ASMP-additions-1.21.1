package net.maximlvr.asmpthingsadd.item;

import net.maximlvr.asmpthingsadd.AsmpThingsModAdd;
import net.maximlvr.asmpthingsadd.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AsmpThingsModAdd.MOD_ID);


    public static final Supplier<CreativeModeTab> ASMP_ITEMS_TAB = CREATIVE_MODE_TAB.register("asmp_item_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.ROLE_CARD.get()))
                    .title(Component.translatable("creativetab.asmpthingsmodadd.asmp_item"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(ModItems.ROLE_CARD);
                        output.accept(ModItems.WEREWOLF_BULLET);
                        output.accept(ModItems.WEREWOLF_RED_BASE_POTION);
                        output.accept(ModItems.WEREWOLF_GREEN_BASE_POTION);
                        output.accept(ModItems.WEREWOLF_PROTECTION_POTION);
                        output.accept(ModItems.WEREWOLF_DEATH_POTION);
                        output.accept(ModItems.WEREWOLF_MANDRAKE_ROOT);
                        output.accept(ModItems.WEREWOLF_SILVER_DUST);
                        output.accept(ModItems.WEREWOLF_NIGHTSHADE);
                        output.accept(ModItems.WEREWOLF_RAVEN_FEATHER);
                        output.accept(ModItems.WEREWOLF_BONE_ASH);
                        output.accept(ModItems.WEREWOLF_MOON_WATER);
                        output.accept(ModItems.WEREWOLF_BLOOD_BERRY);
                        output.accept(ModItems.WEREWOLF_WOLFSBANE_LEAF);
                        output.accept(ModBlocks.WEREWOLF_PEDESTAL);
                        output.accept(ModBlocks.WEREWOLF_BREWING_STATION);
                        output.accept(ModBlocks.CRYSTAL_BALL);
                        output.accept(ModBlocks.WEREWOLF_OAK_LOG);
                        output.accept(ModBlocks.WEREWOLF_COAL_ORE);
                        output.accept(ModBlocks.WEREWOLF_IRON_ORE);
                        output.accept(ModBlocks.WEREWOLF_FORGE);
                        output.accept(ModBlocks.WEREWOLF_QUEST_BLOCK);

                    }).build());

    public static final Supplier<CreativeModeTab> HOSPITAL_ITEM_TAB = CREATIVE_MODE_TAB.register("hospital_item_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.ANAESTHETIC_MACHINE_BLOCK.get()))
                    .title(Component.translatable("creativetab.asmpthingsmodadd.hospital_item"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(ModBlocks.ANAESTHETIC_MACHINE_BLOCK);
                        output.accept(ModBlocks.ANALYTICAL_BALANCE_BLOCK);
                        output.accept(ModBlocks.BLOCK_OF_STAINLESS_STEEL_BLOCK);
                        output.accept(ModBlocks.BABYBLUEXTILES_BLOCK);
                        output.accept(ModBlocks.BEDSIDE_HEAD_UNIT);
                        output.accept(ModBlocks.EPOS_CASHIER_SYSTEM_BLOCK);
                        output.accept(ModBlocks.ILLUMINATED_PHARMACY_SIGN_BLOCK);
                        output.accept(ModBlocks.COMPUTER_CT_SCAN_VIEWER_BLOCK);
                        output.accept(ModBlocks.COMPUTER_X_RAY_VIEWER_BLOCK);
                        output.accept(ModBlocks.OPERATING_TABLE_BLOCK);
                        output.accept(ModBlocks.PHARMACY_COUNTER_STOCKED_BLOCK);
                        output.accept(ModBlocks.REFRIDGERATED_CENTRIFUGE_BLOCK);
                        output.accept(ModBlocks.ULTRASOUND_BLOCK);
                        output.accept(ModBlocks.SURGICAL_WORKSTATION_BLOCK);
                        output.accept(ModBlocks.SURGICAL_INSTRUMENTS_BLOCK);
                        output.accept(ModBlocks.SURGICAL_LAMP_BLOCK);
                        output.accept(ModBlocks.SURGICAL_INSTRUMENT_TROLLEY_BLOCK);
                        output.accept(ModBlocks.SURGICAL_TROLLEY_BLOCK);

                        output.accept(ModBlocks.X_RAY_MACHINE_BLOCK);
                        output.accept(ModBlocks.X_RAY_LIGHT_BLOCK);
                        output.accept(ModBlocks.X_RAY_BUCKY_STAND_BLOCK);
                        output.accept(ModBlocks.X_RAY_LIGHT_BOX_ARMS_BLOCK);
                        output.accept(ModBlocks.X_RAY_LIGHT_BOX_BLANK_BLOCK);
                        output.accept(ModBlocks.X_RAY_LIGHT_BOX_CHEST_BLOCK);
                        output.accept(ModBlocks.X_RAY_LIGHT_BOX_FEET_BLOCK);
                        output.accept(ModBlocks.X_RAY_LIGHT_BOX_NECK_BLOCK);
                        output.accept(ModBlocks.WARD_RESUS_TROLLEY_BLOCK);
                        output.accept(ModBlocks.WARD_STORAGE_DRAWERS_BLOCK);
                        output.accept(ModBlocks.HOSPITAL_BED_BLOCK);
                        output.accept(ModBlocks.IV_STAND_BLOCK);
                        output.accept(ModBlocks.HEART_RATE_MONITOR_BLOCK);
                        output.accept(ModBlocks.HAND_SANITISER_DISPENSER_BLOCK);


                    }).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
