package net.maximlvr.asmpthingsadd.werewolf;

import net.maximlvr.asmpthingsadd.item.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WerewolfLootPool {

    private WerewolfLootPool() {
    }

    public static List<ItemStack> createMandatoryDailyLoot() {
        List<ItemStack> loot = new ArrayList<>();

        loot.add(new ItemStack(ModItems.WEREWOLF_RED_BASE_POTION.get()));
        loot.add(new ItemStack(ModItems.WEREWOLF_GREEN_BASE_POTION.get()));
        loot.add(new ItemStack(ModItems.WEREWOLF_MANDRAKE_ROOT.get()));
        loot.add(new ItemStack(ModItems.WEREWOLF_SILVER_DUST.get()));
        loot.add(new ItemStack(ModItems.WEREWOLF_NIGHTSHADE.get()));
        loot.add(new ItemStack(ModItems.WEREWOLF_RAVEN_FEATHER.get()));
        loot.add(new ItemStack(ModItems.WEREWOLF_BONE_ASH.get()));
        loot.add(new ItemStack(ModItems.WEREWOLF_MOON_WATER.get()));
        loot.add(new ItemStack(ModItems.WEREWOLF_BLOOD_BERRY.get()));
        loot.add(new ItemStack(ModItems.WEREWOLF_WOLFSBANE_LEAF.get()));
        loot.add(new ItemStack(Items.LEATHER));
        loot.add(new ItemStack(Items.LEATHER));
        loot.add(new ItemStack(Items.LEATHER));

        return loot;
    }

    public static List<ItemStack> createRandomizedLootForPedestals(int pedestalCount, RandomSource random) {
        List<ItemStack> mandatoryLoot = createMandatoryDailyLoot();

        if (pedestalCount < mandatoryLoot.size()) {
            throw new IllegalArgumentException(
                    "Pas assez de pedestals. Il faut au moins " + mandatoryLoot.size()
                            + " pedestals, mais il y en a seulement " + pedestalCount
            );
        }

        List<ItemStack> result = new ArrayList<>();

        for (ItemStack stack : mandatoryLoot) {
            result.add(stack.copy());
        }

        while (result.size() < pedestalCount) {
            result.add(ItemStack.EMPTY);
        }

        Collections.shuffle(result, new java.util.Random(random.nextLong()));

        return result;
    }
}
