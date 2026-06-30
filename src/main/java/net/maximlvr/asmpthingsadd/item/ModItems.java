package net.maximlvr.asmpthingsadd.item;


import net.maximlvr.asmpthingsadd.AsmpThingsModAdd;
import net.maximlvr.asmpthingsadd.component.ModDataComponents;
import net.maximlvr.asmpthingsadd.item.custom.RoleCardItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.maximlvr.asmpthingsadd.item.custom.WerewolfCompassItem;
import net.maximlvr.asmpthingsadd.item.custom.WerewolfPotionItem;


public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AsmpThingsModAdd.MOD_ID);

    public static final DeferredItem<Item> ROLE_CARD = ITEMS.register("role_card",
            () -> new RoleCardItem(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.ROLE_CARD_TYPE.get(), 0)));

    public static final DeferredItem<Item> WEREWOLF_COMPASS = ITEMS.register("werewolf_compass",
            () -> new WerewolfCompassItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> WEREWOLF_PROTECTION_POTION = ITEMS.register("werewolf_protection_potion",
            () -> new WerewolfPotionItem(new Item.Properties().stacksTo(1)
                    .component(DataComponents.POTION_CONTENTS, new PotionContents(Potions.HEALING)), WerewolfPotionItem.Kind.PROTECTION, true));

    public static final DeferredItem<Item> WEREWOLF_DEATH_POTION = ITEMS.register("werewolf_death_potion",
            () -> new WerewolfPotionItem(new Item.Properties().stacksTo(1)
                    .component(DataComponents.POTION_CONTENTS, new PotionContents(Potions.POISON)), WerewolfPotionItem.Kind.POISON, true));

    public static final DeferredItem<Item> WEREWOLF_RED_BASE_POTION = ITEMS.register("werewolf_red_base_potion",
            () -> new WerewolfPotionItem(new Item.Properties().stacksTo(1)
                    .component(DataComponents.POTION_CONTENTS, new PotionContents(Potions.HEALING)), WerewolfPotionItem.Kind.PROTECTION, false));

    public static final DeferredItem<Item> WEREWOLF_GREEN_BASE_POTION = ITEMS.register("werewolf_green_base_potion",
            () -> new WerewolfPotionItem(new Item.Properties().stacksTo(1)
                    .component(DataComponents.POTION_CONTENTS, new PotionContents(Potions.POISON)), WerewolfPotionItem.Kind.POISON, false));

    public static final DeferredItem<Item> WEREWOLF_MANDRAKE_ROOT = ITEMS.register("werewolf_mandrake_root",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> WEREWOLF_SILVER_DUST = ITEMS.register("werewolf_silver_dust",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> WEREWOLF_NIGHTSHADE = ITEMS.register("werewolf_nightshade",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> WEREWOLF_RAVEN_FEATHER = ITEMS.register("werewolf_raven_feather",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> WEREWOLF_BONE_ASH = ITEMS.register("werewolf_bone_ash",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> WEREWOLF_MOON_WATER = ITEMS.register("werewolf_moon_water",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> WEREWOLF_BLOOD_BERRY = ITEMS.register("werewolf_blood_berry",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> WEREWOLF_WOLFSBANE_LEAF = ITEMS.register("werewolf_wolfsbane_leaf",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> WEREWOLF_BULLET = ITEMS.register("werewolf_bullet",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
