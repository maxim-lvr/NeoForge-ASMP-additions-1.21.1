package net.maximlvr.asmpthingsadd.werewolf;

import net.maximlvr.asmpthingsadd.network.qte.ClientboundStartLittleGirlQtePayload;
import net.maximlvr.asmpthingsadd.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class WerewolfTurnHandler {
    private static final WerewolfTurnHandler INSTANCE = new WerewolfTurnHandler();
    private static final int SECOND = 20;
    private static final int TURN_TICKS = 60 * SECOND;
    private static final int LITTLE_GIRL_QTE_TICKS = 3 * SECOND;
    private static final int[] LITTLE_GIRL_QTE_OFFSETS = {15 * SECOND, 30 * SECOND, 45 * SECOND};

    private enum NightSequencePhase {
        NONE,
        SEER,
        WOLVES,
        WITCH
    }

    private final Random random = new Random();

    private int dayDiscussionEndTick = -1;
    private int dayActionEndTick = -1;
    private int lastTimerMessageTick = -1;

    private boolean wolfTurnActive;
    private boolean seerTurnActive;
    private boolean witchTurnActive;
    private UUID littleGirlUuid;
    private int currentTurnStartTick;
    private int currentTurnEndTick;
    private int nextLittleGirlQteIndex;
    private int activeLittleGirlQteId = -1;
    private int activeLittleGirlQteExpireTick = -1;
    private String activeLittleGirlSequence = "";
    private int littleGirlBonusQteTicks;
    private boolean littleGirlQuestReadyNextNight;
    private boolean wolfTorchPenaltyReadyNextNight;
    private boolean witchPotionUsedThisTurn;
    private NightSequencePhase nightSequencePhase = NightSequencePhase.NONE;
    private final Map<UUID, UUID> werewolfVotes = new HashMap<>();
    private UUID pendingWerewolfVictim;
    private int sharedLeatherPieces;

    private WerewolfTurnHandler() {
    }

    public static WerewolfTurnHandler get() {
        return INSTANCE;
    }

    public boolean startDayInfo(MinecraftServer server, int discussionSeconds, int actionSeconds) {
        if (!WerewolfGame.get().isRunning()) {
            return false;
        }

        int now = server.getTickCount();
        dayDiscussionEndTick = now + Math.max(0, discussionSeconds) * SECOND;
        dayActionEndTick = dayDiscussionEndTick + Math.max(0, actionSeconds) * SECOND;
        lastTimerMessageTick = -1;
        stopWolfTurn(server);
        stopRoleTurns();
        nightSequencePhase = NightSequencePhase.NONE;

        WerewolfGame.get().forEachParticipant(server, player -> player.sendSystemMessage(Component.literal(
                "[LG] Jour : discussion " + discussionSeconds + "s, action " + actionSeconds + "s."
        ).withStyle(ChatFormatting.YELLOW)));
        return true;
    }

    public boolean startWolfTurn(MinecraftServer server) {
        if (!WerewolfGame.get().isRunning()) {
            return false;
        }

        int now = server.getTickCount();
        stopRoleTurns();
        nightSequencePhase = NightSequencePhase.NONE;
        wolfTurnActive = true;
        werewolfVotes.clear();
        pendingWerewolfVictim = null;
        currentTurnStartTick = now;
        int wolfTurnTicks = TURN_TICKS;
        if (wolfTorchPenaltyReadyNextNight) {
            wolfTurnTicks -= 5 * SECOND;
            wolfTorchPenaltyReadyNextNight = false;
            WerewolfGame.get().forEachParticipant(server, player ->
                    player.sendSystemMessage(Component.literal("[LG] La torche brule la nuit : tour des loups reduit a 55s.")
                            .withStyle(ChatFormatting.YELLOW)));
        }

        currentTurnEndTick = now + wolfTurnTicks;
        final int finalWolfTurnTicks = wolfTurnTicks;
        nextLittleGirlQteIndex = 0;
        activeLittleGirlQteId = -1;
        activeLittleGirlQteExpireTick = -1;
        activeLittleGirlSequence = "";
        littleGirlBonusQteTicks = 0;
        littleGirlUuid = null;

        WerewolfGame.get().findPlayerByRole(server, WerewolfRole.PETITE_FILLE).ifPresent(player -> {
            littleGirlUuid = player.getUUID();
            if (littleGirlQuestReadyNextNight) {
                littleGirlBonusQteTicks = SECOND;
                littleGirlQuestReadyNextNight = false;
                player.sendSystemMessage(Component.literal("[LG] Petite fille : une quete a ete validee le jour precedent. +1s sur tes QTE cette nuit.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, TURN_TICKS, 0, false, false, false));
            player.sendSystemMessage(Component.literal("[LG] Tour des loups : tu es invisible pendant 1 minute.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        });

        WerewolfGame.get().forEachParticipant(server, player ->
                player.displayClientMessage(Component.literal("[LG] Tour des loups : " + (finalWolfTurnTicks / SECOND) + "s restantes."), true));
        return true;
    }

    public boolean startSeerTurn(MinecraftServer server) {
        if (!WerewolfGame.get().isRunning()) {
            return false;
        }

        stopWolfTurn(server);
        stopRoleTurns();
        nightSequencePhase = NightSequencePhase.NONE;
        seerTurnActive = true;
        currentTurnStartTick = server.getTickCount();
        currentTurnEndTick = currentTurnStartTick + TURN_TICKS;

        WerewolfGame.get().findPlayerByRole(server, WerewolfRole.VOYANTE).ifPresent(player ->
                player.sendSystemMessage(Component.literal("[LG] Tour de la voyante : utilise la boule de cristal.")
                        .withStyle(ChatFormatting.AQUA)));
        return true;
    }

    public boolean canWerewolfChooseTarget(ServerPlayer player) {
        if (!wolfTurnActive || WerewolfGame.get().getRole(player).orElse(null) != WerewolfRole.LOUP_GAROU) {
            player.sendSystemMessage(Component.literal("[LG] Les loups choisissent uniquement pendant leur tour."));
            return false;
        }

        return true;
    }

    public void selectWerewolfTarget(ServerPlayer wolf, UUID targetUuid) {
        if (!canWerewolfChooseTarget(wolf)) {
            return;
        }

        ServerPlayer target = wolf.server.getPlayerList().getPlayer(targetUuid);

        if (target == null || !WerewolfGame.get().getRole(targetUuid).isPresent()) {
            wolf.sendSystemMessage(Component.literal("[LG] Cible invalide."));
            return;
        }

        werewolfVotes.put(wolf.getUUID(), targetUuid);
        pendingWerewolfVictim = null;
        wolf.sendSystemMessage(Component.literal("[LG] Tu as choisi " + target.getGameProfile().getName() + ".")
                .withStyle(ChatFormatting.RED));
    }

    public void selectNoWerewolfTarget(ServerPlayer wolf) {
        if (!canWerewolfChooseTarget(wolf)) {
            return;
        }

        werewolfVotes.remove(wolf.getUUID());
        pendingWerewolfVictim = null;
        wolf.sendSystemMessage(Component.literal("[LG] Tu ne choisis personne cette nuit.")
                .withStyle(ChatFormatting.GRAY));
    }

    public boolean revealWerewolfVictim(MinecraftServer server) {
        if (pendingWerewolfVictim == null && !werewolfVotes.isEmpty()) {
            pendingWerewolfVictim = pickWerewolfVictim();
        }

        if (pendingWerewolfVictim == null) {
            WerewolfGame.get().forEachParticipant(server, player ->
                    player.sendSystemMessage(Component.literal("[LG] Les loups n'ont tue personne cette nuit.")));
            werewolfVotes.clear();
            return false;
        }

        ServerPlayer target = server.getPlayerList().getPlayer(pendingWerewolfVictim);
        pendingWerewolfVictim = null;
        werewolfVotes.clear();

        if (target == null) {
            WerewolfGame.get().forEachParticipant(server, player ->
                    player.sendSystemMessage(Component.literal("[LG] La victime des loups est introuvable.")));
            return false;
        }

        WerewolfGame.get().kill(server, target);
        return true;
    }

    public boolean startWitchTurn(MinecraftServer server) {
        if (!WerewolfGame.get().isRunning()) {
            return false;
        }

        stopWolfTurn(server);
        stopRoleTurns();
        nightSequencePhase = NightSequencePhase.NONE;
        witchTurnActive = true;
        witchPotionUsedThisTurn = false;
        currentTurnStartTick = server.getTickCount();
        currentTurnEndTick = currentTurnStartTick + TURN_TICKS;

        WerewolfGame.get().findPlayerByRole(server, WerewolfRole.SORCIERE).ifPresent(player -> {
            int protection = player.getInventory().countItem(ModItems.WEREWOLF_PROTECTION_POTION.get());
            int death = player.getInventory().countItem(ModItems.WEREWOLF_DEATH_POTION.get());

            if (protection <= 0 && death <= 0) {
                player.sendSystemMessage(Component.literal("[LG] Tour de la sorciere : tu n'as aucune potion sur toi.")
                        .withStyle(ChatFormatting.GRAY));
                return;
            }

            player.sendSystemMessage(Component.literal("[LG] Tour de la sorciere : "
                    + protection + " potion(s) de protection, " + death
                    + " potion(s) de mort. Une potion maximum ce tour.")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        });
        return true;
    }

    public void startNightSequence(MinecraftServer server, int nightNumber) {
        stopWolfTurn(server);
        stopRoleTurns();
        nightSequencePhase = NightSequencePhase.NONE;

        if (nightNumber <= 1) {
            WerewolfGame.get().findPlayerByRole(server, WerewolfRole.CUPIDON).ifPresent(player ->
                    player.sendSystemMessage(Component.literal("[LG] Nuit 1 : choisis les amoureux avec /lg couple <joueur1> <joueur2>.")
                            .withStyle(ChatFormatting.LIGHT_PURPLE)));
            return;
        }

        startNextNightSequencePhase(server, NightSequencePhase.SEER);
    }

    public boolean collectSharedLeather(ServerPlayer player) {
        if (!WerewolfGame.get().isRunning()) {
            player.sendSystemMessage(Component.literal("[LG] La partie n'est pas lancee."));
            return false;
        }

        if (sharedLeatherPieces >= 3) {
            player.sendSystemMessage(Component.literal("[LG] Les 3 morceaux de cuir sont deja trouves."));
            return false;
        }

        sharedLeatherPieces++;
        WerewolfGame.get().forEachParticipant(player.server, participant ->
                participant.sendSystemMessage(Component.literal("[LG] Quete petite fille : morceau de cuir "
                        + sharedLeatherPieces + "/3.")));
        return true;
    }

    public boolean craftLittleGirlCape(ServerPlayer player) {
        if (!WerewolfGame.get().isRunning()) {
            player.sendSystemMessage(Component.literal("[LG] La partie n'est pas lancee."));
            return false;
        }

        if (sharedLeatherPieces < 3) {
            player.sendSystemMessage(Component.literal("[LG] Il faut trouver les 3 morceaux de cuir avant de crafter la cape."));
            return false;
        }

        sharedLeatherPieces = 0;
        player.getInventory().add(new ItemStack(net.minecraft.world.item.Items.LEATHER_CHESTPLATE));
        player.sendSystemMessage(Component.literal("[LG] Tu fabriques une cape pour la petite fille."));
        return true;
    }

    public boolean canCraftLittleGirlCape() {
        return WerewolfGame.get().isRunning() && sharedLeatherPieces >= 3;
    }

    public boolean depositLittleGirlCape(ServerPlayer player, ItemStack stack) {
        WerewolfGame game = WerewolfGame.get();

        if (!game.isRunning()) {
            player.sendSystemMessage(Component.literal("[LG] La partie n'est pas lancee."));
            return false;
        }

        if (game.isNight()) {
            player.sendSystemMessage(Component.literal("[LG] Cette quete se valide pendant le jour."));
            return false;
        }

        if (littleGirlQuestReadyNextNight) {
            player.sendSystemMessage(Component.literal("[LG] La quete petite fille est deja validee pour la prochaine nuit."));
            return false;
        }

        stack.shrink(1);
        littleGirlQuestReadyNextNight = true;
        player.sendSystemMessage(Component.literal("[LG] Cape deposee : la petite fille aura +1s sur ses QTE cette nuit."));
        return true;
    }

    public boolean depositTorchWolfPenalty(ServerPlayer player, ItemStack stack) {
        WerewolfGame game = WerewolfGame.get();

        if (!game.isRunning()) {
            player.sendSystemMessage(Component.literal("[LG] La partie n'est pas lancee."));
            return false;
        }

        if (game.isNight()) {
            player.sendSystemMessage(Component.literal("[LG] Cette quete se valide pendant le jour."));
            return false;
        }

        if (wolfTorchPenaltyReadyNextNight) {
            player.sendSystemMessage(Component.literal("[LG] La torche est deja activee pour la prochaine nuit."));
            return false;
        }

        stack.shrink(1);
        wolfTorchPenaltyReadyNextNight = true;
        player.sendSystemMessage(Component.literal("[LG] Torche deposee : le prochain tour des loups durera 55s."));
        return true;
    }

    public boolean canOpenWitchPotion(ServerPlayer player, String potionKind) {
        if (!witchTurnActive || WerewolfGame.get().getRole(player).orElse(null) != WerewolfRole.SORCIERE) {
            player.sendSystemMessage(Component.literal("[LG] La sorciere utilise ses potions pendant son tour."));
            return false;
        }

        if (!isWitchPotionKind(potionKind)) {
            return false;
        }

        if (witchPotionUsedThisTurn) {
            player.sendSystemMessage(Component.literal("[LG] Une potion a deja ete utilisee ce tour."));
            return false;
        }

        return true;
    }

    public void applyWitchPotion(ServerPlayer player, UUID targetUuid, String potionKind) {
        if (!canOpenWitchPotion(player, potionKind)) {
            return;
        }

        Item potionItem = getFinishedPotionItem(potionKind);

        ServerPlayer target = player.server.getPlayerList().getPlayer(targetUuid);

        if (target == null || !WerewolfGame.get().getRole(targetUuid).isPresent()) {
            player.sendSystemMessage(Component.literal("[LG] Cette cible n'est pas dans la partie."));
            return;
        }

        if (potionItem == null || !consumeWitchPotion(player, potionItem)) {
            player.sendSystemMessage(Component.literal("[LG] Tu n'as plus cette potion sur toi."));
            return;
        }

        witchPotionUsedThisTurn = true;

        if ("poison".equals(potionKind)) {
            player.sendSystemMessage(Component.literal("[LG] Sorciere : potion de poison utilisee sur "
                    + target.getGameProfile().getName() + ".").withStyle(ChatFormatting.DARK_GREEN));
        } else {
            player.sendSystemMessage(Component.literal("[LG] Sorciere : potion de protection utilisee sur "
                    + target.getGameProfile().getName() + ".").withStyle(ChatFormatting.RED));
        }
    }

    public void handleLittleGirlQteResult(ServerPlayer player, int qteId, boolean success) {
        if (!wolfTurnActive || activeLittleGirlQteId != qteId || littleGirlUuid == null
                || !littleGirlUuid.equals(player.getUUID())) {
            return;
        }

        activeLittleGirlQteId = -1;
        activeLittleGirlQteExpireTick = -1;
        activeLittleGirlSequence = "";

        if (success) {
            player.displayClientMessage(Component.literal("[LG] Petite fille : QTE reussi."), true);
            return;
        }

        failLittleGirlQte(player);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        int now = server.getTickCount();

        tickDayInfo(server, now);
        tickSimpleRoleTurn(server, now);
        tickWolfTurn(server, now);
    }

    public void showDayTitle(MinecraftServer server, int dayNumber) {
        stopWolfTurn(server);
        stopRoleTurns();
        nightSequencePhase = NightSequencePhase.NONE;
        showTitle(server, "Jour " + dayNumber, "Discussion, quetes et deplacements", ChatFormatting.GOLD);
    }

    public void showNightTitle(MinecraftServer server, int nightNumber) {
        showTitle(server, "Nuit " + nightNumber, nightNumber == 1 ? "Cupidon choisit les amoureux" : "Les roles se reveillent", ChatFormatting.DARK_PURPLE);
    }

    private void tickDayInfo(MinecraftServer server, int now) {
        if (dayActionEndTick < 0 || now < lastTimerMessageTick + SECOND) {
            return;
        }

        lastTimerMessageTick = now;

        if (now >= dayActionEndTick) {
            shufflePedestalItems(server);
            WerewolfGame.get().forEachParticipant(server, player ->
                    player.displayClientMessage(Component.literal("[LG] Jour : temps termine."), true));
            dayDiscussionEndTick = -1;
            dayActionEndTick = -1;
            return;
        }

        Component message;

        if (now < dayDiscussionEndTick) {
            message = Component.literal("[LG] Discussion : " + secondsLeft(dayDiscussionEndTick, now)
                    + "s | Action : " + secondsLeft(dayActionEndTick, dayDiscussionEndTick) + "s");
        } else {
            message = Component.literal("[LG] Action : " + secondsLeft(dayActionEndTick, now) + "s");
        }

        WerewolfGame.get().forEachParticipant(server, player -> player.displayClientMessage(message, true));
    }

    private void tickWolfTurn(MinecraftServer server, int now) {
        if (!wolfTurnActive) {
            return;
        }

        if (now >= currentTurnEndTick) {
            stopWolfTurn(server);
            shufflePedestalItems(server);
            WerewolfGame.get().forEachParticipant(server, player ->
                    player.displayClientMessage(Component.literal("[LG] Tour des loups termine."), true));
            pendingWerewolfVictim = pickWerewolfVictim();
            if (nightSequencePhase == NightSequencePhase.WOLVES) {
                startNextNightSequencePhase(server, NightSequencePhase.WITCH);
            }
            return;
        }

        if (now % SECOND == 0) {
            WerewolfGame.get().forEachParticipant(server, player ->
                    player.displayClientMessage(Component.literal("[LG] Tour des loups : "
                            + secondsLeft(currentTurnEndTick, now) + "s restantes."), true));
        }

        if (activeLittleGirlQteId >= 0 && now >= activeLittleGirlQteExpireTick) {
            ServerPlayer littleGirl = getLittleGirl(server);

            activeLittleGirlQteId = -1;
            activeLittleGirlQteExpireTick = -1;
            activeLittleGirlSequence = "";

            if (littleGirl != null) {
                failLittleGirlQte(littleGirl);
            }
        }

        if (nextLittleGirlQteIndex >= LITTLE_GIRL_QTE_OFFSETS.length || littleGirlUuid == null) {
            return;
        }

        int elapsed = now - currentTurnStartTick;

        if (elapsed >= LITTLE_GIRL_QTE_OFFSETS[nextLittleGirlQteIndex]) {
            startLittleGirlQte(server, now);
            nextLittleGirlQteIndex++;
        }
    }

    private void startLittleGirlQte(MinecraftServer server, int now) {
        ServerPlayer littleGirl = getLittleGirl(server);

        if (littleGirl == null) {
            return;
        }

        activeLittleGirlQteId = now + random.nextInt(1000);
        int qteTicks = LITTLE_GIRL_QTE_TICKS + littleGirlBonusQteTicks;
        activeLittleGirlQteExpireTick = now + qteTicks;
        activeLittleGirlSequence = randomLetterSequence();

        PacketDistributor.sendToPlayer(littleGirl, new ClientboundStartLittleGirlQtePayload(
                activeLittleGirlQteId,
                activeLittleGirlSequence,
                qteTicks
        ));
    }

    private ServerPlayer getLittleGirl(MinecraftServer server) {
        if (littleGirlUuid == null) {
            return null;
        }

        ServerPlayer player = server.getPlayerList().getPlayer(littleGirlUuid);

        if (player == null || !player.isAlive()) {
            return null;
        }

        return player;
    }

    private void failLittleGirlQte(ServerPlayer player) {
        player.serverLevel().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.VILLAGER_HURT,
                SoundSource.PLAYERS,
                2.0F,
                0.9F
        );
        player.sendSystemMessage(Component.literal("[LG] Petite fille : QTE rate, tu as fait du bruit.")
                .withStyle(ChatFormatting.RED));
    }

    private void stopWolfTurn(MinecraftServer server) {
        if (littleGirlUuid != null) {
            ServerPlayer player = server.getPlayerList().getPlayer(littleGirlUuid);

            if (player != null) {
                player.removeEffect(MobEffects.INVISIBILITY);
            }
        }

        wolfTurnActive = false;
        littleGirlUuid = null;
        activeLittleGirlQteId = -1;
        activeLittleGirlQteExpireTick = -1;
        activeLittleGirlSequence = "";
        littleGirlBonusQteTicks = 0;
    }

    private UUID pickWerewolfVictim() {
        if (werewolfVotes.isEmpty()) {
            return null;
        }

        java.util.List<UUID> selectedTargets = new java.util.ArrayList<>(werewolfVotes.values());
        return selectedTargets.get(random.nextInt(selectedTargets.size()));
    }

    private void stopRoleTurns() {
        seerTurnActive = false;
        witchTurnActive = false;
        witchPotionUsedThisTurn = false;
    }

    private void tickSimpleRoleTurn(MinecraftServer server, int now) {
        if (!seerTurnActive && !witchTurnActive) {
            return;
        }

        String label = seerTurnActive ? "Tour de la voyante" : "Tour de la sorciere";

        if (now >= currentTurnEndTick) {
            shufflePedestalItems(server);
            WerewolfGame.get().forEachParticipant(server, player ->
                    player.displayClientMessage(Component.literal("[LG] " + label + " termine."), true));
            stopRoleTurns();

            if (nightSequencePhase == NightSequencePhase.SEER) {
                startNextNightSequencePhase(server, NightSequencePhase.WOLVES);
            } else if (nightSequencePhase == NightSequencePhase.WITCH) {
                finishNightSequence(server);
            }
            return;
        }

        if (now % SECOND == 0) {
            WerewolfGame.get().forEachParticipant(server, player ->
                    player.displayClientMessage(Component.literal("[LG] " + label + " : "
                            + secondsLeft(currentTurnEndTick, now) + "s restantes."), true));
        }
    }

    private boolean isWitchPotion(Item item) {
        return item == ModItems.WEREWOLF_PROTECTION_POTION.get()
                || item == ModItems.WEREWOLF_DEATH_POTION.get();
    }

    private boolean isWitchPotionKind(String potionKind) {
        return "protection".equals(potionKind) || "poison".equals(potionKind);
    }

    private Item getFinishedPotionItem(String potionKind) {
        if ("protection".equals(potionKind)) {
            return ModItems.WEREWOLF_PROTECTION_POTION.get();
        }

        if ("poison".equals(potionKind)) {
            return ModItems.WEREWOLF_DEATH_POTION.get();
        }

        return null;
    }

    private boolean consumeWitchPotion(ServerPlayer player, Item potionItem) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(potionItem)) {
                stack.shrink(1);
                return true;
            }
        }

        return false;
    }

    private void startSeerTurnForSequence(MinecraftServer server) {
        stopWolfTurn(server);
        stopRoleTurns();
        nightSequencePhase = NightSequencePhase.SEER;
        seerTurnActive = true;
        currentTurnStartTick = server.getTickCount();
        currentTurnEndTick = currentTurnStartTick + TURN_TICKS;

        WerewolfGame.get().forEachParticipant(server, player ->
                player.sendSystemMessage(Component.literal("[LG] Tour de la voyante : 60s.").withStyle(ChatFormatting.AQUA)));
    }

    private void startWolfTurnForSequence(MinecraftServer server) {
        startWolfTurn(server);
        nightSequencePhase = NightSequencePhase.WOLVES;
    }

    private void startWitchTurnForSequence(MinecraftServer server) {
        startWitchTurn(server);
        nightSequencePhase = NightSequencePhase.WITCH;
    }

    private void startNextNightSequencePhase(MinecraftServer server, NightSequencePhase phase) {
        if (phase == NightSequencePhase.SEER) {
            if (hasLivingRole(server, WerewolfRole.VOYANTE)) {
                startSeerTurnForSequence(server);
                return;
            }

            startNextNightSequencePhase(server, NightSequencePhase.WOLVES);
            return;
        }

        if (phase == NightSequencePhase.WOLVES) {
            if (hasLivingRole(server, WerewolfRole.LOUP_GAROU)) {
                startWolfTurnForSequence(server);
                return;
            }

            startNextNightSequencePhase(server, NightSequencePhase.WITCH);
            return;
        }

        if (phase == NightSequencePhase.WITCH) {
            if (hasLivingRole(server, WerewolfRole.SORCIERE)) {
                startWitchTurnForSequence(server);
                return;
            }

            finishNightSequence(server);
        }
    }

    private boolean hasLivingRole(MinecraftServer server, WerewolfRole role) {
        return WerewolfGame.get().findPlayerByRole(server, role).isPresent();
    }

    private void finishNightSequence(MinecraftServer server) {
        nightSequencePhase = NightSequencePhase.NONE;
        WerewolfGame.get().forEachParticipant(server, player ->
                player.sendSystemMessage(Component.literal("[LG] Nuit terminee. Lance /lg day <discussion> <action>.").withStyle(ChatFormatting.YELLOW)));
    }

    private void showTitle(MinecraftServer server, String title, String subtitle, ChatFormatting color) {
        Component titleText = Component.literal(title).withStyle(color);
        Component subtitleText = Component.literal(subtitle).withStyle(ChatFormatting.GRAY);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(titleText));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitleText));
        }
    }

    private int secondsLeft(int endTick, int now) {
        return Math.max(0, (endTick - now + SECOND - 1) / SECOND);
    }

    private void shufflePedestalItems(MinecraftServer server) {
        WerewolfPedestalManager.get().shuffleRemainingItems(server);
    }

    private String randomLetterSequence() {
        char first = (char) ('A' + random.nextInt(26));
        char second = (char) ('A' + random.nextInt(26));

        return ("" + first + second).toUpperCase(Locale.ROOT);
    }
}
