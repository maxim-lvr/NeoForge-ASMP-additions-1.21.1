package net.maximlvr.asmpthingsadd.werewolf;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.maximlvr.asmpthingsadd.block.entity.WerewolfPedestalBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class WerewolfCommands {
    private static final int REROLL_CHUNK_RADIUS = 8;
    private static final SuggestionProvider<CommandSourceStack> ROLE_SUGGESTIONS = (context, builder) -> {
        for (WerewolfRole role : WerewolfRole.values()) {
            builder.suggest(role.name().toLowerCase(Locale.ROOT));
        }

        return builder.buildFuture();
    };

    private static final long DAY_TIME = 6000L;
    private static final long NIGHT_TIME = 18000L;
    private int dayNumber;
    private int nightNumber;

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("lg")
                        .then(Commands.literal("add")
                                .requires(this::canManageGame)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> addPlayer(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player")
                                        ))
                                )
                                .then(Commands.literal("admin")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> addAdmin(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "player")
                                                ))
                                        )
                                )
                        )

                        .then(Commands.literal("start")
                                .requires(this::canManageGame)
                                .executes(context -> startGame(context.getSource()))
                        )

                        .then(Commands.literal("stop")
                                .requires(this::canManageGame)
                                .executes(context -> stopGame(context.getSource()))
                        )

                        .then(Commands.literal("kill")
                                .requires(this::canManageGame)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> killPlayer(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player")
                                        ))
                                )
                        )

                        .then(Commands.literal("reveal")
                                .requires(this::canManageGame)
                                .executes(context -> revealWerewolfVictim(context.getSource()))
                        )

                        .then(Commands.literal("couple")
                                .then(Commands.argument("player1", EntityArgument.player())
                                        .then(Commands.argument("player2", EntityArgument.player())
                                                .executes(context -> setCouple(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "player1"),
                                                        EntityArgument.getPlayer(context, "player2")
                                                ))
                                        )
                                )
                        )

                        .then(Commands.literal("coupleDebug")
                                .requires(this::canManageGame)
                                .then(Commands.argument("player1", EntityArgument.player())
                                        .then(Commands.argument("player2", EntityArgument.player())
                                                .executes(context -> forceCouple(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "player1"),
                                                        EntityArgument.getPlayer(context, "player2")
                                                ))
                                        )
                                )
                        )

                        .then(Commands.literal("role")
                                .requires(this::canManageGame)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("role", StringArgumentType.word())
                                                .suggests(ROLE_SUGGESTIONS)
                                                .executes(context -> forceRole(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        StringArgumentType.getString(context, "role")
                                                ))
                                        )
                                )
                        )

                        .then(Commands.literal("debug")
                                .requires(this::canManageGame)
                                .executes(context -> showDebug(context.getSource()))
                        )

                        .then(Commands.literal("veronica")
                                .requires(this::canManageGame)
                                .executes(context -> spawnVeronica(context.getSource()))
                        )

                        .then(Commands.literal("reroll")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> rerollPedestals(context.getSource(), true))
                        )

                        .then(Commands.literal("init")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> initPedestals(context.getSource()))
                        )

                        .then(Commands.literal("day")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> setDay(context.getSource()))
                                .then(Commands.argument("discussionSeconds", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("actionSeconds", IntegerArgumentType.integer(0))
                                                .executes(context -> setDay(
                                                        context.getSource(),
                                                        IntegerArgumentType.getInteger(context, "discussionSeconds"),
                                                        IntegerArgumentType.getInteger(context, "actionSeconds")
                                                ))
                                        )
                                )
                        )

                        .then(Commands.literal("night")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> setNight(context.getSource()))
                        )

                        .then(Commands.literal("wolves")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> startWolfTurn(context.getSource()))
                        )

                        .then(Commands.literal("recipes")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> showRecipes(context.getSource()))
                        )

                        .then(Commands.literal("rerollRecipes")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> rerollRecipes(context.getSource()))
                        )
        );

        dispatcher.register(
                Commands.literal("lp")
                        .then(Commands.literal("day")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("discussionSeconds", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("actionSeconds", IntegerArgumentType.integer(0))
                                                .executes(context -> setDay(
                                                        context.getSource(),
                                                        IntegerArgumentType.getInteger(context, "discussionSeconds"),
                                                        IntegerArgumentType.getInteger(context, "actionSeconds")
                                                ))
                                        )
                                )
                        )
                        .then(Commands.literal("wolves")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> startWolfTurn(context.getSource()))
                        )
        );
    }

    private boolean canManageGame(CommandSourceStack source) {
        if (source.hasPermission(2)) {
            return true;
        }

        try {
            return WerewolfGame.get().isAdmin(source.getPlayerOrException());
        } catch (CommandSyntaxException exception) {
            return false;
        }
    }

    private int addPlayer(CommandSourceStack source, ServerPlayer target) {
        WerewolfGame game = WerewolfGame.get();

        if (game.isRunning()) {
            source.sendFailure(Component.literal("LG add impossible : une partie est deja lancee."));
            return 0;
        }

        boolean added = game.addPlayer(target);

        source.sendSuccess(
                () -> Component.literal("LG : " + target.getGameProfile().getName()
                        + (added ? " ajoute a la partie." : " est deja dans la partie.")),
                true
        );

        return added ? 1 : 0;
    }

    private int addAdmin(CommandSourceStack source, ServerPlayer target) {
        WerewolfGame game = WerewolfGame.get();

        if (game.isRunning()) {
            source.sendFailure(Component.literal("LG add admin impossible : une partie est deja lancee."));
            return 0;
        }

        boolean added = game.addAdmin(target);

        source.sendSuccess(
                () -> Component.literal("LG : " + target.getGameProfile().getName()
                        + (added ? " est admin de la partie." : " est deja admin de la partie.")),
                true
        );

        return added ? 1 : 0;
    }

    private int startGame(CommandSourceStack source) {
        WerewolfGame game = WerewolfGame.get();
        MinecraftServer server = source.getServer();

        if (game.playerCount() == 0) {
            source.sendFailure(Component.literal("LG start impossible : aucun joueur ajoute."));
            return 0;
        }

        if (!game.start(server)) {
            source.sendFailure(Component.literal("LG start impossible : une partie est deja lancee."));
            return 0;
        }

        dayNumber = 0;
        nightNumber = 0;

        source.sendSuccess(
                () -> Component.literal("LG : partie lancee avec " + game.playerCount() + " joueurs."),
                true
        );

        return 1;
    }

    private int stopGame(CommandSourceStack source) {
        WerewolfGame.get().stop(source.getServer());
        dayNumber = 0;
        nightNumber = 0;

        source.sendSuccess(
                () -> Component.literal("LG : partie arretee."),
                true
        );

        return 1;
    }

    private int killPlayer(CommandSourceStack source, ServerPlayer target) {
        int killed = WerewolfGame.get().kill(source.getServer(), target);

        if (killed == 0) {
            source.sendFailure(Component.literal("LG kill impossible : joueur absent de la partie ou partie inactive."));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("LG : " + killed + " joueur(s) tue(s)."),
                true
        );

        return killed;
    }

    private int revealWerewolfVictim(CommandSourceStack source) {
        boolean killed = WerewolfTurnHandler.get().revealWerewolfVictim(source.getServer());

        source.sendSuccess(
                () -> Component.literal(killed ? "LG reveal : victime revelee." : "LG reveal : aucune victime."),
                true
        );

        return killed ? 1 : 0;
    }

    private int setCouple(CommandSourceStack source, ServerPlayer first, ServerPlayer second) throws CommandSyntaxException {
        ServerPlayer cupidon = source.getPlayerOrException();
        boolean success = WerewolfGame.get().setCouple(cupidon, first, second);

        if (!success) {
            source.sendFailure(Component.literal("LG couple impossible : il faut etre Cupidon, choisir deux joueurs differents de la partie, et avoir une partie lancee."));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("LG : couple cree entre " + first.getGameProfile().getName()
                        + " et " + second.getGameProfile().getName() + "."),
                false
        );

        return 1;
    }

    private int forceCouple(CommandSourceStack source, ServerPlayer first, ServerPlayer second) {
        boolean success = WerewolfGame.get().forceCouple(first, second);

        if (!success) {
            source.sendFailure(Component.literal("LG coupleDebug impossible : partie inactive, joueurs absents, ou meme joueur."));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("LG debug : couple force entre " + first.getGameProfile().getName()
                        + " et " + second.getGameProfile().getName() + "."),
                true
        );

        return 1;
    }

    private int forceRole(CommandSourceStack source, ServerPlayer target, String roleName) {
        WerewolfRole role = parseRole(roleName);

        if (role == null) {
            source.sendFailure(Component.literal("LG role inconnu : " + roleName));
            return 0;
        }

        if (!WerewolfGame.get().setRole(target, role)) {
            source.sendFailure(Component.literal("LG role impossible : partie inactive ou joueur absent de la partie."));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("LG debug : " + target.getGameProfile().getName()
                        + " est maintenant " + role.displayName() + "."),
                true
        );

        return 1;
    }

    private int showDebug(CommandSourceStack source) {
        source.sendSuccess(
                () -> Component.literal(WerewolfGame.get().formatDebugState(source.getServer())),
                false
        );

        return 1;
    }

    private WerewolfRole parseRole(String roleName) {
        String normalized = roleName.toUpperCase(Locale.ROOT);

        for (WerewolfRole role : WerewolfRole.values()) {
            if (role.name().equals(normalized)) {
                return role;
            }
        }

        return null;
    }

    private int spawnVeronica(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        Villager villager = EntityType.VILLAGER.create(level);

        if (villager == null) {
            source.sendFailure(Component.literal("LG veronica impossible : le villager n'a pas pu etre cree."));
            return 0;
        }

        villager.setCustomName(Component.literal(WerewolfVeronicaHandler.VERONICA_NAME));
        villager.setCustomNameVisible(true);
        villager.setPersistenceRequired();
        villager.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
        level.addFreshEntity(villager);

        source.sendSuccess(
                () -> Component.literal("LG : Veronica est apparue."),
                true
        );

        return 1;
    }

    private int setDay(CommandSourceStack source) throws CommandSyntaxException {
        return setDay(source, -1, -1);
    }

    private int setDay(CommandSourceStack source, int discussionSeconds, int actionSeconds) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();

        freezeTime(level, source);
        level.setDayTime(DAY_TIME);
        WerewolfGame.get().startDayTurn(source.getServer());
        dayNumber++;
        WerewolfTurnHandler.get().showDayTitle(source.getServer(), dayNumber);

        if (discussionSeconds >= 0 && actionSeconds >= 0) {
            WerewolfTurnHandler.get().startDayInfo(source.getServer(), discussionSeconds, actionSeconds);
        }

        WerewolfBrewingRecipeManager.rerollAll(level.random);

        int movedItems = WerewolfPedestalManager.get().shuffleRemainingItems(source.getServer());

        source.sendSuccess(
                () -> Component.literal(
                        "LG day : midi fixe activé. Items restants deplaces : " + movedItems + "."
                ),
                true
        );

        return 1;
    }

    private int startWolfTurn(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();

        if (!WerewolfGame.get().isRunning()) {
            source.sendFailure(Component.literal("LG wolves impossible : la partie n'est pas lancee."));
            return 0;
        }

        freezeTime(level, source);
        level.setDayTime(NIGHT_TIME);
        WerewolfGame.get().startNightTurn(source.getServer());
        WerewolfTurnHandler.get().startWolfTurn(source.getServer());

        source.sendSuccess(
                () -> Component.literal("LG wolves : tour des loups lance pour 60s."),
                true
        );

        return 1;
    }

    private int setNight(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();

        freezeTime(level, source);
        level.setDayTime(NIGHT_TIME);
        WerewolfGame.get().startNightTurn(source.getServer());
        nightNumber++;
        WerewolfTurnHandler.get().showNightTitle(source.getServer(), nightNumber);
        WerewolfTurnHandler.get().startNightSequence(source.getServer(), nightNumber);

        source.sendSuccess(
                () -> Component.literal("LG night : nuit " + nightNumber + " lancée."),
                true
        );

        return 1;
    }

    private int showRecipes(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();

        String recipes = WerewolfBrewingRecipeManager.formatRecipes(level.random);

        source.sendSuccess(
                () -> Component.literal("Recettes LG :\n" + recipes),
                false
        );

        return 1;
    }

    private int rerollRecipes(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();

        WerewolfBrewingRecipeManager.rerollAll(level.random);

        String recipes = WerewolfBrewingRecipeManager.formatRecipes(level.random);

        source.sendSuccess(
                () -> Component.literal("Recettes LG reroll :\n" + recipes),
                true
        );

        return 1;
    }

    private void freezeTime(ServerLevel level, CommandSourceStack source) {
        level.getGameRules()
                .getRule(GameRules.RULE_DAYLIGHT)
                .set(false, source.getServer());
    }

    private int initPedestals(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        WerewolfPedestalManager.InitResult result = WerewolfPedestalManager.get().initAround(player);

        if (!result.success()) {
            source.sendFailure(Component.literal("LG init impossible : "
                    + result.pedestalCount() + " pedestals trouves, "
                    + result.requiredItems() + " requis."));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("LG init : " + result.placedItems()
                        + " items sorciere places sur " + result.pedestalCount() + " pedestals."),
                true
        );
        return result.placedItems();
    }

    private int rerollPedestals(CommandSourceStack source, boolean announce) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();

        List<WerewolfPedestalBlockEntity> pedestals = findPedestalsAroundPlayer(level, player);

        if (pedestals.isEmpty()) {
            source.sendFailure(Component.literal(
                    "LG reroll : aucun pedestal trouvé dans un rayon de "
                            + REROLL_CHUNK_RADIUS + " chunks."
            ));
            return 0;
        }

        List<ItemStack> randomizedLoot;

        try {
            randomizedLoot = WerewolfLootPool.createRandomizedLootForPedestals(
                    pedestals.size(),
                    level.random
            );
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal(
                    "LG reroll impossible : " + exception.getMessage()
            ));
            return 0;
        }

        int placedItems = 0;
        int emptyPedestals = 0;

        for (int i = 0; i < pedestals.size(); i++) {
            WerewolfPedestalBlockEntity pedestal = pedestals.get(i);
            ItemStack stack = randomizedLoot.get(i);

            pedestal.setDisplayedItem(stack);

            if (stack.isEmpty()) {
                emptyPedestals++;
            } else {
                placedItems++;
            }
        }

        if (announce) {
            final int finalPedestalCount = pedestals.size();
            final int finalPlacedItems = placedItems;
            final int finalEmptyPedestals = emptyPedestals;

            source.sendSuccess(
                    () -> Component.literal(
                            "LG reroll : "
                                    + finalPedestalCount + " pedestals trouvés dans "
                                    + REROLL_CHUNK_RADIUS + " chunks. "
                                    + finalPlacedItems + " items placés, "
                                    + finalEmptyPedestals + " pedestals vides."
                    ),
                    true
            );
        }

        return pedestals.size();
    }

    private List<WerewolfPedestalBlockEntity> findPedestalsAroundPlayer(ServerLevel level, ServerPlayer player) {
        List<WerewolfPedestalBlockEntity> pedestals = new ArrayList<>();

        int centerChunkX = player.chunkPosition().x;
        int centerChunkZ = player.chunkPosition().z;

        for (int chunkX = centerChunkX - REROLL_CHUNK_RADIUS; chunkX <= centerChunkX + REROLL_CHUNK_RADIUS; chunkX++) {
            for (int chunkZ = centerChunkZ - REROLL_CHUNK_RADIUS; chunkZ <= centerChunkZ + REROLL_CHUNK_RADIUS; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }

                LevelChunk chunk = level.getChunk(chunkX, chunkZ);

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockEntity blockEntity = entry.getValue();

                    if (blockEntity instanceof WerewolfPedestalBlockEntity pedestal) {
                        pedestals.add(pedestal);
                    }
                }
            }
        }

        return pedestals;
    }
}
