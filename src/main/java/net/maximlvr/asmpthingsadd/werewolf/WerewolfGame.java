package net.maximlvr.asmpthingsadd.werewolf;

import net.maximlvr.asmpthingsadd.component.ModDataComponents;
import net.maximlvr.asmpthingsadd.item.ModItems;
import net.maximlvr.asmpthingsadd.network.payload.WerewolfGameStatePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class WerewolfGame {
    private static final WerewolfGame INSTANCE = new WerewolfGame();

    private final Set<UUID> players = new HashSet<>();
    private final Set<UUID> admins = new HashSet<>();
    private final Map<UUID, WerewolfRole> roles = new HashMap<>();
    private final Map<UUID, UUID> couples = new HashMap<>();
    private final Map<UUID, GameType> eliminatedPreviousGameModes = new HashMap<>();
    private final Map<UUID, PendingElimination> pendingEliminations = new HashMap<>();
    private final Map<UUID, EnumMap<WerewolfGatheringResource, Integer>> gatheringProgress = new HashMap<>();
    private final Map<UUID, EnumSet<WerewolfGatheringResource>> gatheredThisTurn = new HashMap<>();
    private boolean running;
    private boolean night;
    private boolean bulletCrafted;
    private boolean cupidonCoupleChosen;

    private WerewolfGame() {
    }

    public static WerewolfGame get() {
        return INSTANCE;
    }

    public boolean addPlayer(ServerPlayer player) {
        if (running) {
            return false;
        }

        return players.add(player.getUUID());
    }

    public boolean addAdmin(ServerPlayer player) {
        if (running) {
            return false;
        }

        return admins.add(player.getUUID());
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isNight() {
        return night;
    }

    public boolean isAdmin(ServerPlayer player) {
        return admins.contains(player.getUUID());
    }

    public boolean isPlayer(ServerPlayer player) {
        return players.contains(player.getUUID());
    }

    public Optional<WerewolfRole> getRole(ServerPlayer player) {
        return Optional.ofNullable(roles.get(player.getUUID()));
    }

    public int playerCount() {
        return players.size();
    }

    public String encodeLivingPlayers(MinecraftServer server) {
        StringBuilder builder = new StringBuilder();

        for (UUID playerUuid : players) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);

            if (player != null && player.isAlive() && roles.containsKey(playerUuid)) {
                if (!builder.isEmpty()) {
                    builder.append('\n');
                }

                builder.append(playerUuid).append('|').append(player.getGameProfile().getName());
            }
        }

        return builder.toString();
    }

    public Optional<WerewolfRole> getRole(UUID playerUuid) {
        return Optional.ofNullable(roles.get(playerUuid));
    }

    public Optional<ServerPlayer> findPlayerByRole(MinecraftServer server, WerewolfRole searchedRole) {
        for (UUID playerUuid : players) {
            if (roles.get(playerUuid) != searchedRole) {
                continue;
            }

            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);

            if (player != null && player.isAlive()) {
                return Optional.of(player);
            }
        }

        return Optional.empty();
    }

    public void forEachParticipant(MinecraftServer server, Consumer<ServerPlayer> consumer) {
        Set<UUID> recipients = new HashSet<>(players);
        recipients.addAll(admins);

        for (UUID playerUuid : recipients) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);

            if (player != null) {
                consumer.accept(player);
            }
        }
    }

    public boolean setRole(ServerPlayer player, WerewolfRole role) {
        if (!running || !players.contains(player.getUUID())) {
            return false;
        }

        roles.put(player.getUUID(), role);
        giveRoleCard(player, role);
        player.sendSystemMessage(Component.literal("[LG] Role force : " + role.displayName())
                .withStyle(ChatFormatting.GOLD));
        return true;
    }

    public boolean forceCouple(ServerPlayer first, ServerPlayer second) {
        if (!running || first.getUUID().equals(second.getUUID())) {
            return false;
        }

        if (!players.contains(first.getUUID()) || !players.contains(second.getUUID())) {
            return false;
        }

        couples.clear();
        couples.put(first.getUUID(), second.getUUID());
        couples.put(second.getUUID(), first.getUUID());

        first.sendSystemMessage(Component.literal("[LG] Tu es en couple avec " + second.getGameProfile().getName() + ".")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        second.sendSystemMessage(Component.literal("[LG] Tu es en couple avec " + first.getGameProfile().getName() + ".")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        return true;
    }

    public String formatDebugState(MinecraftServer server) {
        StringBuilder builder = new StringBuilder();
        builder.append("LG debug : ")
                .append(running ? "partie lancee" : "partie arretee")
                .append(", joueurs=")
                .append(players.size())
                .append(", admins=")
                .append(admins.size());

        for (UUID playerUuid : players) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
            String playerName = player == null ? playerUuid.toString() : player.getGameProfile().getName();
            WerewolfRole role = roles.get(playerUuid);
            UUID partnerUuid = couples.get(playerUuid);
            ServerPlayer partner = partnerUuid == null ? null : server.getPlayerList().getPlayer(partnerUuid);

            builder.append("\n- ")
                    .append(playerName)
                    .append(" : ")
                    .append(role == null ? "aucun role" : role.displayName());

            if (partnerUuid != null) {
                builder.append(", couple=")
                        .append(partner == null ? partnerUuid : partner.getGameProfile().getName());
            }
        }

        return builder.toString();
    }

    public Optional<String> getPartnerName(MinecraftServer server, ServerPlayer player) {
        UUID partnerUuid = couples.get(player.getUUID());

        if (partnerUuid == null) {
            return Optional.empty();
        }

        ServerPlayer partner = server.getPlayerList().getPlayer(partnerUuid);

        if (partner == null) {
            return Optional.empty();
        }

        return Optional.of(partner.getGameProfile().getName());
    }

    public boolean start(MinecraftServer server) {
        if (running || players.isEmpty()) {
            return false;
        }

        running = true;
        night = false;
        bulletCrafted = false;
        cupidonCoupleChosen = false;
        resetGatheringTurn();
        roles.clear();
        couples.clear();
        eliminatedPreviousGameModes.clear();
        assignRoles(server);
        syncGameState(server);
        return true;
    }

    public void stop(MinecraftServer server) {
        boolean wasRunning = running;

        running = false;
        night = false;
        restoreEliminatedPlayers(server);
        roles.clear();
        couples.clear();
        eliminatedPreviousGameModes.clear();
        pendingEliminations.clear();
        gatheringProgress.clear();
        gatheredThisTurn.clear();
        bulletCrafted = false;
        cupidonCoupleChosen = false;
        syncGameState(server);

        if (wasRunning) {
            broadcastToGame(server, Component.literal("[LG] Partie terminee. Le chat est de nouveau visible."));
        }

        players.clear();
        admins.clear();
    }

    public boolean setCouple(ServerPlayer cupidon, ServerPlayer first, ServerPlayer second) {
        if (!running || getRole(cupidon).orElse(null) != WerewolfRole.CUPIDON || first.getUUID().equals(second.getUUID())) {
            return false;
        }

        if (cupidonCoupleChosen) {
            return false;
        }

        if (!players.contains(first.getUUID()) || !players.contains(second.getUUID())) {
            return false;
        }

        couples.clear();
        couples.put(first.getUUID(), second.getUUID());
        couples.put(second.getUUID(), first.getUUID());

        first.sendSystemMessage(Component.literal("[LG] Tu es en couple avec " + second.getGameProfile().getName() + ".")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        second.sendSystemMessage(Component.literal("[LG] Tu es en couple avec " + first.getGameProfile().getName() + ".")
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        cupidonCoupleChosen = true;
        return true;
    }

    public int kill(MinecraftServer server, ServerPlayer target) {
        if (!running || !players.remove(target.getUUID())) {
            return 0;
        }

        UUID targetUuid = target.getUUID();
        UUID partnerUuid = couples.remove(targetUuid);
        WerewolfRole targetRole = roles.get(targetUuid);

        announceDeath(server, target, targetRole);
        scheduleElimination(server, target);
        roles.remove(targetUuid);
        admins.remove(targetUuid);

        int killed = 1;

        if (partnerUuid != null) {
            couples.remove(partnerUuid);
            WerewolfRole partnerRole = roles.remove(partnerUuid);
            players.remove(partnerUuid);
            admins.remove(partnerUuid);

            ServerPlayer partner = server.getPlayerList().getPlayer(partnerUuid);
            if (partner != null) {
                announceDeath(server, partner, partnerRole);
                scheduleElimination(server, partner);
                partner.sendSystemMessage(Component.literal("[LG] Ton couple est mort, tu meurs aussi.")
                        .withStyle(ChatFormatting.RED));
            }

            killed++;
        }

        return killed;
    }

    public void handlePlayerDeath(MinecraftServer server, ServerPlayer target) {
        if (!running || !players.remove(target.getUUID())) {
            return;
        }

        UUID targetUuid = target.getUUID();
        UUID partnerUuid = couples.remove(targetUuid);
        WerewolfRole targetRole = roles.remove(targetUuid);
        admins.remove(targetUuid);
        announceDeath(server, target, targetRole);

        if (partnerUuid == null) {
            return;
        }

        couples.remove(partnerUuid);
        players.remove(partnerUuid);
        WerewolfRole partnerRole = roles.remove(partnerUuid);
        admins.remove(partnerUuid);

        ServerPlayer partner = server.getPlayerList().getPlayer(partnerUuid);
        if (partner != null) {
            announceDeath(server, partner, partnerRole);
            scheduleElimination(server, partner);
            partner.sendSystemMessage(Component.literal("[LG] Ton couple est mort, tu meurs aussi.")
                    .withStyle(ChatFormatting.RED));
        }
    }

    public GatheringResult hitGatheringBlock(ServerPlayer player, WerewolfGatheringResource resource) {
        if (!running) {
            return new GatheringResult(GatheringStatus.INACTIVE, 0);
        }

        UUID playerUuid = player.getUUID();
        EnumSet<WerewolfGatheringResource> gatheredResources = gatheredThisTurn.get(playerUuid);

        if (gatheredResources != null && gatheredResources.contains(resource)) {
            return new GatheringResult(GatheringStatus.ALREADY_GATHERED, 0);
        }

        EnumMap<WerewolfGatheringResource, Integer> playerProgress = gatheringProgress.computeIfAbsent(
                playerUuid,
                ignored -> new EnumMap<>(WerewolfGatheringResource.class)
        );
        int hits = playerProgress.getOrDefault(resource, 0) + 1;

        if (hits < 10) {
            playerProgress.put(resource, hits);
            return new GatheringResult(GatheringStatus.PROGRESS, hits);
        }

        playerProgress.remove(resource);

        if (playerProgress.isEmpty()) {
            gatheringProgress.remove(playerUuid);
        }

        gatheredThisTurn.computeIfAbsent(playerUuid, ignored -> EnumSet.noneOf(WerewolfGatheringResource.class))
                .add(resource);
        player.getInventory().add(new ItemStack(resource.reward()));
        return new GatheringResult(GatheringStatus.GATHERED, 10);
    }

    public void startDayTurn() {
        night = false;
        resetGatheringTurn();
    }

    public void startNightTurn(MinecraftServer server) {
        night = true;
        resetGatheringTurn();

        for (UUID playerUuid : players) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);

            if (player == null || roles.get(playerUuid) == WerewolfRole.SORCIERE) {
                continue;
            }

            removeTemporaryNightItems(player);
        }

        syncGameState(server);
    }

    public void startDayTurn(MinecraftServer server) {
        night = false;
        resetGatheringTurn();
        syncGameState(server);
    }

    public void syncPlayerState(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new WerewolfGameStatePayload(running, night));
    }

    public void tryCraftBullet(ServerPlayer player) {
        if (!running || getRole(player).orElse(null) != WerewolfRole.CHASSEUR || bulletCrafted) {
            player.sendSystemMessage(Component.literal("[LG] Tu termines la tache de forge."));
            return;
        }

        bulletCrafted = true;
        player.getInventory().add(new ItemStack(ModItems.WEREWOLF_BULLET.get()));
        player.sendSystemMessage(Component.literal("[LG] Tu fabriques une balle. Garde-la precieusement.")
                .withStyle(ChatFormatting.GOLD));
    }

    private void assignRoles(MinecraftServer server) {
        List<UUID> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);

        List<WerewolfRole> rolePool = createRolePool(shuffledPlayers.size());

        for (int i = 0; i < shuffledPlayers.size(); i++) {
            UUID playerUuid = shuffledPlayers.get(i);
            WerewolfRole role = rolePool.get(i);

            roles.put(playerUuid, role);

            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
            if (player != null) {
                giveRoleCard(player, role);
                player.sendSystemMessage(Component.literal("[LG] Ton role : " + role.displayName())
                        .withStyle(ChatFormatting.GOLD));
            }
        }
    }

    private List<WerewolfRole> createRolePool(int size) {
        List<WerewolfRole> pool = new ArrayList<>();
        EnumMap<WerewolfRole, Integer> caps = new EnumMap<>(WerewolfRole.class);
        caps.put(WerewolfRole.LOUP_GAROU, Math.max(1, size / 4));
        caps.put(WerewolfRole.CUPIDON, 1);
        caps.put(WerewolfRole.CHASSEUR, 1);
        caps.put(WerewolfRole.SORCIERE, 1);
        caps.put(WerewolfRole.PETITE_FILLE, 1);
        caps.put(WerewolfRole.VOYANTE, 1);

        for (WerewolfRole role : WerewolfRole.values()) {
            if (role == WerewolfRole.VILLAGEOIS) {
                continue;
            }

            int count = caps.getOrDefault(role, 0);
            for (int i = 0; i < count && pool.size() < size; i++) {
                pool.add(role);
            }
        }

        while (pool.size() < size) {
            pool.add(WerewolfRole.VILLAGEOIS);
        }

        Collections.shuffle(pool);
        return pool;
    }

    private void giveRoleCard(ServerPlayer player, WerewolfRole role) {
        ItemStack stack = new ItemStack(ModItems.ROLE_CARD.get());
        stack.set(ModDataComponents.ROLE_CARD_TYPE.get(), role.cardType());
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Carte - " + role.displayName()));
        player.getInventory().add(stack);
    }

    private void resetGatheringTurn() {
        gatheringProgress.clear();
        gatheredThisTurn.clear();
    }

    private void removeTemporaryNightItems(ServerPlayer player) {
        removeItem(player, Items.STICK);
        removeItem(player, Items.COAL);
        removeItem(player, Items.IRON_INGOT);
        removeItem(player, Items.TORCH);
    }

    private void removeItem(ServerPlayer player, Item item) {
        int count = player.getInventory().countItem(item);

        if (count > 0) {
            player.getInventory().clearOrCountMatchingItems(stack -> stack.is(item), count, player.inventoryMenu.getCraftSlots());
        }
    }

    private void broadcastToGame(MinecraftServer server, Component message) {
        Set<UUID> recipients = new HashSet<>(players);
        recipients.addAll(admins);

        for (UUID playerUuid : recipients) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
            if (player != null) {
                player.sendSystemMessage(message);
            }
        }
    }

    private void announceDeath(MinecraftServer server, ServerPlayer player, WerewolfRole role) {
        Component message = Component.literal("[LG] " + player.getGameProfile().getName()
                        + " est mort. Son role etait "
                        + (role == null ? "inconnu" : role.displayName()) + ".")
                .withStyle(ChatFormatting.RED);

        broadcastToGame(server, message);
    }

    public void tickPendingEliminations(MinecraftServer server) {
        int now = server.getTickCount();

        pendingEliminations.entrySet().removeIf(entry -> {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            PendingElimination pending = entry.getValue();

            if (player == null) {
                return true;
            }

            if (!pending.levitationApplied() && now >= pending.levitationTick()) {
                player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 4 * 20, 200, false, true, true));
                pending.setLevitationApplied(true);
            }

            if (now < pending.spectatorTick()) {
                return false;
            }

            finishElimination(server, player);
            return true;
        });
    }

    private void scheduleElimination(MinecraftServer server, ServerPlayer player) {
        eliminatedPreviousGameModes.putIfAbsent(player.getUUID(), player.gameMode.getGameModeForPlayer());
        int now = server.getTickCount();
        pendingEliminations.put(player.getUUID(), new PendingElimination(now + 3 * 20, now + 7 * 20));
    }

    private void finishElimination(MinecraftServer server, ServerPlayer player) {
        eliminatedPreviousGameModes.putIfAbsent(player.getUUID(), player.gameMode.getGameModeForPlayer());
        player.setGameMode(GameType.SPECTATOR);
        findOnlineAdmin(server).ifPresent(admin ->
                player.teleportTo(admin.serverLevel(), admin.getX(), admin.getY(), admin.getZ(), admin.getYRot(), admin.getXRot()));
    }

    private void restoreEliminatedPlayers(MinecraftServer server) {
        for (Map.Entry<UUID, GameType> entry : eliminatedPreviousGameModes.entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

            if (player != null) {
                player.setGameMode(entry.getValue());
            }
        }
    }

    private Optional<ServerPlayer> findOnlineAdmin(MinecraftServer server) {
        for (UUID adminUuid : admins) {
            ServerPlayer admin = server.getPlayerList().getPlayer(adminUuid);

            if (admin != null) {
                return Optional.of(admin);
            }
        }

        return Optional.empty();
    }

    private static final class PendingElimination {
        private final int levitationTick;
        private final int spectatorTick;
        private boolean levitationApplied;

        private PendingElimination(int levitationTick, int spectatorTick) {
            this.levitationTick = levitationTick;
            this.spectatorTick = spectatorTick;
        }

        private int levitationTick() {
            return levitationTick;
        }

        private int spectatorTick() {
            return spectatorTick;
        }

        private boolean levitationApplied() {
            return levitationApplied;
        }

        private void setLevitationApplied(boolean levitationApplied) {
            this.levitationApplied = levitationApplied;
        }
    }

    private void syncGameState(MinecraftServer server) {
        WerewolfGameStatePayload payload = new WerewolfGameStatePayload(running, night);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    public record GatheringResult(GatheringStatus status, int hits) {
    }

    public enum GatheringStatus {
        PROGRESS,
        GATHERED,
        ALREADY_GATHERED,
        INACTIVE
    }
}
