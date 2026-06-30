package net.maximlvr.asmpthingsadd;

import net.maximlvr.asmpthingsadd.block.ModBlocks;
import net.maximlvr.asmpthingsadd.block.entity.ModBlockEntities;
import net.maximlvr.asmpthingsadd.component.ModDataComponents;
import net.maximlvr.asmpthingsadd.entity.ModEntities;
import net.maximlvr.asmpthingsadd.item.ModCreativeModeTabs;
import net.maximlvr.asmpthingsadd.item.ModItems;
import net.maximlvr.asmpthingsadd.network.ModNetworking;
import net.maximlvr.asmpthingsadd.werewolf.*;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AsmpThingsModAdd.MOD_ID)
public class AsmpThingsModAdd {

    public static final String MOD_ID = "asmpthingsmodadd";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AsmpThingsModAdd(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new WerewolfCommands());
        NeoForge.EVENT_BUS.register(new WerewolfChatHandler());
        NeoForge.EVENT_BUS.register(new WerewolfGatheringHandler());
        NeoForge.EVENT_BUS.register(new WerewolfPlayerConnectionHandler());
        NeoForge.EVENT_BUS.register(new WerewolfPlayerRuleHandler());
        NeoForge.EVENT_BUS.register(WerewolfTurnHandler.get());
        NeoForge.EVENT_BUS.register(new WerewolfVeronicaHandler());

        ModCreativeModeTabs.register(modEventBus);

        ModItems.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModNetworking.register(modEventBus);

        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }


    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

}
