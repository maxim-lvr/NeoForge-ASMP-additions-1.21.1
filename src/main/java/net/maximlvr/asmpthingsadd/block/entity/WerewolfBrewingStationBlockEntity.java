package net.maximlvr.asmpthingsadd.block.entity;

import net.maximlvr.asmpthingsadd.network.qte.ClientboundStartColorQtePayload;
import net.maximlvr.asmpthingsadd.network.qte.ClientboundStartGridQtePayload;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfBrewingRecipeManager;
import net.maximlvr.asmpthingsadd.werewolf.WerewolfPotionKind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.maximlvr.asmpthingsadd.network.qte.ClientboundStartDigitQtePayload;

import java.util.List;
import java.util.UUID;

public class WerewolfBrewingStationBlockEntity extends BlockEntity {
    private static final String TAG_SELECTED_KIND = "SelectedKind";
    private static final String TAG_CURRENT_KIND = "CurrentKind";
    private static final String TAG_CURRENT_STEP = "CurrentStep";
    private static final String TAG_COMPLETED_MASK = "CompletedMask";

    private WerewolfPotionKind selectedKind = WerewolfPotionKind.PROTECTION;
    private WerewolfPotionKind currentKind = null;
    private int currentStep = 0;
    private int completedIngredientMask = 0;

    private boolean waitingForQte = false;
    private int activeQteId = -1;
    private UUID activePlayerId = null;
    private InteractionHand pendingHand = InteractionHand.MAIN_HAND;
    private ItemStack pendingExpected = ItemStack.EMPTY;

    public WerewolfBrewingStationBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.WEREWOLF_BREWING_STATION.get(), pos, blockState);
    }

    public void toggleSelectedPotion(ServerPlayer player) {
        if (waitingForQte) {
            player.displayClientMessage(Component.literal("Station LG : un mini-jeu est déjà en cours."), true);
            return;
        }

        if (currentKind != null) {
            sendCurrentProgress(player);
            return;
        }

        selectedKind = selectedKind.next();
        setChanged();

        player.displayClientMessage(
                Component.literal("Station LG : potion sélectionnée -> " + selectedKind.displayName()),
                true
        );
    }

    public void tryInsertIngredient(ServerPlayer player, InteractionHand hand, ItemStack heldStack) {
        if (level == null || level.isClientSide()) {
            return;
        }

        if (waitingForQte) {
            player.displayClientMessage(Component.literal("Station LG : termine le mini-jeu en cours."), true);
            return;
        }

        WerewolfPotionKind heldKind = getBasePotionKind(heldStack);

        if (heldKind == null) {
            player.displayClientMessage(Component.literal("Station LG : utilise une potion de base rouge ou verte."), true);
            return;
        }

        WerewolfPotionKind recipeKind = currentKind == null ? heldKind : currentKind;

        if (currentKind != null && currentKind != heldKind) {
            player.displayClientMessage(Component.literal("Station LG : une autre potion est deja en cours ici."), true);
            return;
        }

        List<ItemStack> recipe = WerewolfBrewingRecipeManager.getRecipe(recipeKind, level.random);

        if (currentStep < 0 || currentStep > recipe.size()) {
            reset();
            player.displayClientMessage(Component.literal("Station LG : progression invalide, reset."), true);
            return;
        }

        ItemStack expected = selectNextIngredient(player, recipe);

        if (expected.isEmpty()) {
            player.sendSystemMessage(Component.literal("Station LG : il manque l'un des items suivants : "
                    + formatMissingIngredients(recipe)));
            return;
        }

        if (currentKind == null) {
            currentKind = heldKind;
        }

        startRandomQte(player, hand, expected);
    }

    private void startRandomQte(ServerPlayer player, InteractionHand hand, ItemStack expected) {
        if (level == null) {
            return;
        }

        waitingForQte = true;
        activePlayerId = player.getUUID();
        pendingHand = hand;
        pendingExpected = expected.copyWithCount(1);
        activeQteId = level.random.nextInt(Integer.MAX_VALUE);

        int seed = level.random.nextInt();
        int revealTicks = 80;
        int timeLimitTicks = 200;

        int qteType = level.random.nextInt(3);

        if (qteType == 0) {
            PacketDistributor.sendToPlayer(
                    player,
                    new ClientboundStartGridQtePayload(
                            worldPosition,
                            activeQteId,
                            seed,
                            revealTicks,
                            timeLimitTicks
                    )
            );

            player.displayClientMessage(
                    Component.literal("Station LG : mini-jeu grille lancé pour "
                            + expected.getHoverName().getString()),
                    true
            );
        } else if (qteType == 1) {
            PacketDistributor.sendToPlayer(
                    player,
                    new ClientboundStartDigitQtePayload(
                            worldPosition,
                            activeQteId,
                            seed,
                            revealTicks,
                            timeLimitTicks
                    )
            );

            player.displayClientMessage(
                    Component.literal("Station LG : mini-jeu chiffres lancé pour "
                            + expected.getHoverName().getString()),
                    true
            );
        } else {
            PacketDistributor.sendToPlayer(
                    player,
                    new ClientboundStartColorQtePayload(
                            worldPosition,
                            activeQteId,
                            seed,
                            revealTicks,
                            timeLimitTicks
                    )
            );

            player.displayClientMessage(
                    Component.literal("Station LG : mini-jeu couleurs lancé pour "
                            + expected.getHoverName().getString()),
                    true
            );
        }

        setChanged();
    }

    public void handleGridQteResult(ServerPlayer player, int qteId, boolean success) {
        if (level == null || level.isClientSide()) {
            return;
        }

        if (!waitingForQte || qteId != activeQteId) {
            player.displayClientMessage(Component.literal("Station LG : résultat QTE expiré."), true);
            return;
        }

        if (activePlayerId == null || !activePlayerId.equals(player.getUUID())) {
            player.displayClientMessage(Component.literal("Station LG : ce mini-jeu ne t'appartient pas."), true);
            return;
        }

        waitingForQte = false;
        activeQteId = -1;
        activePlayerId = null;

        if (!success) {
            clearPendingQte();
            player.displayClientMessage(Component.literal("Station LG : mini-jeu raté, étape annulée."), true);
            return;
        }

        ItemStack basePotion = player.getItemInHand(pendingHand);

        if (currentKind == null || getBasePotionKind(basePotion) != currentKind) {
            clearPendingQte();
            player.displayClientMessage(Component.literal("Station LG : potion de base changée, étape annulée."), true);
            return;
        }

        if (!player.isCreative() && !consumeInventoryItem(player, pendingExpected)) {
            clearPendingQte();
            player.displayClientMessage(Component.literal("Station LG : ingrédient introuvable, étape annulée."), true);
            return;
        }

        List<ItemStack> recipe = WerewolfBrewingRecipeManager.getRecipe(currentKind, level.random);
        markIngredientDone(recipe, pendingExpected);
        currentStep = Integer.bitCount(completedIngredientMask);

        if (currentStep >= recipe.size()) {
            ItemStack result = WerewolfBrewingRecipeManager.getResult(currentKind);

            if (!player.isCreative()) {
                basePotion.shrink(1);
            }

            if (!player.addItem(result.copy())) {
                player.drop(result.copy(), false);
            }

            player.displayClientMessage(
                    Component.literal("Station LG : potion terminée -> "
                            + result.getHoverName().getString()),
                    false
            );

            reset();
            return;
        }

        player.displayClientMessage(
                Component.literal("Station LG : étape validée. Clique encore avec la potion de base pour continuer. "
                        + currentStep + "/" + recipe.size()),
                false
        );

        clearPendingQte();
        setChanged();
    }

    private void sendCurrentProgress(ServerPlayer player) {
        if (level == null) {
            return;
        }

        WerewolfPotionKind recipeKind = currentKind == null ? selectedKind : currentKind;
        List<ItemStack> recipe = WerewolfBrewingRecipeManager.getRecipe(recipeKind, level.random);

        if (currentKind == null) {
            player.displayClientMessage(
                    Component.literal("Station LG : potion sélectionnée -> "
                            + selectedKind.displayName()
                            + ". Premier ingrédient : "
                            + recipe.get(0).getHoverName().getString()),
                    false
            );
            return;
        }

        if (currentStep >= recipe.size()) {
            player.displayClientMessage(Component.literal("Station LG : potion presque terminée."), false);
            return;
        }

        player.displayClientMessage(
                Component.literal("Station LG : potion en cours -> "
                        + currentKind.displayName()
                        + ". Étape "
                        + (currentStep + 1)
                        + "/"
                        + recipe.size()
                        + ". Ingredients restants : "
                        + formatMissingIngredients(recipe)),
                false
        );
    }

    private void reset() {
        currentKind = null;
        currentStep = 0;
        completedIngredientMask = 0;
        waitingForQte = false;
        activeQteId = -1;
        activePlayerId = null;
        pendingExpected = ItemStack.EMPTY;
        pendingHand = InteractionHand.MAIN_HAND;
        setChanged();
    }

    private void clearPendingQte() {
        waitingForQte = false;
        activeQteId = -1;
        activePlayerId = null;
        pendingExpected = ItemStack.EMPTY;
        pendingHand = InteractionHand.MAIN_HAND;
        setChanged();
    }

    private WerewolfPotionKind getBasePotionKind(ItemStack stack) {
        if (WerewolfBrewingRecipeManager.matches(stack, WerewolfBrewingRecipeManager.getBasePotion(WerewolfPotionKind.PROTECTION))) {
            return WerewolfPotionKind.PROTECTION;
        }

        if (WerewolfBrewingRecipeManager.matches(stack, WerewolfBrewingRecipeManager.getBasePotion(WerewolfPotionKind.DEATH))) {
            return WerewolfPotionKind.DEATH;
        }

        return null;
    }

    private ItemStack selectNextIngredient(ServerPlayer player, List<ItemStack> recipe) {
        List<ItemStack> available = new java.util.ArrayList<>();

        for (int i = 0; i < recipe.size(); i++) {
            if ((completedIngredientMask & (1 << i)) != 0) {
                continue;
            }

            ItemStack ingredient = recipe.get(i);

            if (hasInventoryItem(player, ingredient)) {
                available.add(ingredient);
            }
        }

        if (available.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return available.get(level.random.nextInt(available.size()));
    }

    private boolean hasInventoryItem(ServerPlayer player, ItemStack expected) {
        for (ItemStack stack : player.getInventory().items) {
            if (WerewolfBrewingRecipeManager.matches(stack, expected)) {
                return true;
            }
        }

        return false;
    }

    private boolean consumeInventoryItem(ServerPlayer player, ItemStack expected) {
        for (ItemStack stack : player.getInventory().items) {
            if (WerewolfBrewingRecipeManager.matches(stack, expected)) {
                stack.shrink(1);
                return true;
            }
        }

        return false;
    }

    private void markIngredientDone(List<ItemStack> recipe, ItemStack ingredient) {
        for (int i = 0; i < recipe.size(); i++) {
            if (WerewolfBrewingRecipeManager.matches(recipe.get(i), ingredient)) {
                completedIngredientMask |= 1 << i;
                return;
            }
        }
    }

    private String formatMissingIngredients(List<ItemStack> recipe) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < recipe.size(); i++) {
            if ((completedIngredientMask & (1 << i)) != 0) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append(", ");
            }

            builder.append(recipe.get(i).getHoverName().getString());
        }

        return builder.toString();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putString(TAG_SELECTED_KIND, selectedKind.name());
        tag.putString(TAG_CURRENT_KIND, currentKind == null ? "" : currentKind.name());
        tag.putInt(TAG_CURRENT_STEP, currentStep);
        tag.putInt(TAG_COMPLETED_MASK, completedIngredientMask);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        try {
            selectedKind = WerewolfPotionKind.valueOf(tag.getString(TAG_SELECTED_KIND));
        } catch (Exception ignored) {
            selectedKind = WerewolfPotionKind.PROTECTION;
        }

        String currentKindName = tag.getString(TAG_CURRENT_KIND);

        if (currentKindName == null || currentKindName.isBlank()) {
            currentKind = null;
        } else {
            try {
                currentKind = WerewolfPotionKind.valueOf(currentKindName);
            } catch (Exception ignored) {
                currentKind = null;
            }
        }

        currentStep = tag.getInt(TAG_CURRENT_STEP);
        completedIngredientMask = tag.getInt(TAG_COMPLETED_MASK);

        waitingForQte = false;
        activeQteId = -1;
        activePlayerId = null;
        pendingExpected = ItemStack.EMPTY;
        pendingHand = InteractionHand.MAIN_HAND;
    }
}
