package net.maximlvr.asmpthingsadd.werewolf;

import net.maximlvr.asmpthingsadd.item.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class WerewolfBrewingRecipeManager {
    private WerewolfBrewingRecipeManager() {
    }

    public static void rerollAll(RandomSource random) {
    }

    public static List<ItemStack> getRecipe(WerewolfPotionKind kind, RandomSource random) {
        return switch (kind) {
            case PROTECTION -> createRecipe(
                    ModItems.WEREWOLF_MANDRAKE_ROOT,
                    ModItems.WEREWOLF_SILVER_DUST,
                    ModItems.WEREWOLF_MOON_WATER,
                    ModItems.WEREWOLF_RAVEN_FEATHER
            );
            case DEATH -> createRecipe(
                    ModItems.WEREWOLF_NIGHTSHADE,
                    ModItems.WEREWOLF_BONE_ASH,
                    ModItems.WEREWOLF_BLOOD_BERRY,
                    ModItems.WEREWOLF_WOLFSBANE_LEAF
            );
        };
    }

    public static ItemStack getBasePotion(WerewolfPotionKind kind) {
        return switch (kind) {
            case PROTECTION -> new ItemStack(ModItems.WEREWOLF_RED_BASE_POTION.get());
            case DEATH -> new ItemStack(ModItems.WEREWOLF_GREEN_BASE_POTION.get());
        };
    }

    public static ItemStack getResult(WerewolfPotionKind kind) {
        return switch (kind) {
            case PROTECTION -> new ItemStack(ModItems.WEREWOLF_PROTECTION_POTION.get());
            case DEATH -> new ItemStack(ModItems.WEREWOLF_DEATH_POTION.get());
        };
    }

    public static String formatRecipes(RandomSource random) {
        return "Protection: " + getBasePotion(WerewolfPotionKind.PROTECTION).getHoverName().getString()
                + " + " + formatRecipe(getRecipe(WerewolfPotionKind.PROTECTION, random))
                + "\nPoison: " + getBasePotion(WerewolfPotionKind.DEATH).getHoverName().getString()
                + " + " + formatRecipe(getRecipe(WerewolfPotionKind.DEATH, random));
    }

    @SafeVarargs
    private static List<ItemStack> createRecipe(Supplier<? extends Item>... items) {
        List<ItemStack> result = new ArrayList<>();

        for (Supplier<? extends Item> item : items) {
            result.add(new ItemStack(item.get()));
        }

        return result;
    }

    private static String formatRecipe(List<ItemStack> recipe) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < recipe.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }

            builder.append(recipe.get(i).getHoverName().getString());
        }

        return builder.toString();
    }

    public static boolean matches(ItemStack input, ItemStack expected) {
        if (input.isEmpty() || expected.isEmpty()) {
            return false;
        }

        return ItemStack.isSameItemSameComponents(input.copyWithCount(1), expected.copyWithCount(1));
    }
}
