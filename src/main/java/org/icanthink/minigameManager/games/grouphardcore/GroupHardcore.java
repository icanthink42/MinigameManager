package org.icanthink.minigameManager.games.grouphardcore;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;
import org.icanthink.minigameManager.features.InstantSmelting;
import org.icanthink.minigameManager.features.PlayerResetter;
import org.icanthink.minigameManager.features.WorldManager;
import org.icanthink.minigameManager.features.DeathManager;
import org.icanthink.minigameManager.features.items.CustomItemManager;
import org.icanthink.minigameManager.features.items.PlayerTracker;
import org.icanthink.minigameManager.features.mobs.CustomMobManager;

import java.util.Random;

/**
 * A hardcore minigame where if any player dies, the game ends.
 * Uses InstantSmelting feature to make furnaces smelt instantly.
 */
public class GroupHardcore extends Minigame implements Listener {
    private PlayerResetter playerResetter;
    private InstantSmelting instantSmelting;
    private WorldManager worldManager;
    private DeathManager deathManager;
    private CustomItemManager itemManager;
    private CustomMobManager mobManager;
    private BukkitRunnable eventScheduler;
    private final Random random = new Random();

    private static final long MIN_EVENT_DELAY = 3600; // 3 minutes
    private static final long MAX_EVENT_DELAY = 6000; // 5 minutes

    /**
     * Creates a new GroupHardcore minigame.
     *
     * @param world The world where the minigame takes place
     */
    public GroupHardcore(World world) {
        super(world);

        this.playerResetter = new PlayerResetter(this);
        this.instantSmelting = new InstantSmelting(this);
        this.worldManager = new WorldManager(this);
        this.deathManager = new DeathManager(this);
        this.itemManager = new CustomItemManager(this);
        this.mobManager = new CustomMobManager(this);

        // Add features
        addFeature(playerResetter);
        addFeature(instantSmelting);
        addFeature(worldManager);
        addFeature(deathManager);
        addFeature(itemManager);
        addFeature(mobManager);

        // Set up death callback
        deathManager.setDeathCallback(player -> {
            for (Player p : getPlayers()) {
                p.sendTitle("§c" + player.getName() + " died!", "§4Game over", 10, 70, 20);
                playerResetter.setPlayerSpectator(p);
            }

            // End the game after 10 seconds
            Bukkit.getScheduler().runTaskLater(MinigameManager.plugin, () -> {
                if (isRunning()) {
                    minigameEnd();
                }
            }, 200L); // 200 ticks = 10 seconds
        });
    }

    @Override
    public void minigameStart() {
        // Create a new world for the game
        World gameWorld = worldManager.createWorld(null, WorldType.NORMAL);
        if (gameWorld == null) {
            Bukkit.broadcastMessage("Failed to create game world!");
            return;
        }

        // Reset the game state
        setRunning(true);

        // Register this as a listener
        Bukkit.getPluginManager().registerEvents(this, MinigameManager.plugin);

        // Teleport all players to the new world
        for (Player player : getPlayers()) {
            player.teleport(gameWorld.getSpawnLocation());
            playerResetter.resetPlayer(player);
            itemManager.giveItem(player, PlayerTracker.class);
        }

        // Announce game start
        for (Player player : getPlayers()) {
            player.sendTitle("Group Hardcore", "If any player dies, the game ends!", 10, 70, 20);
        }

        // Start random events
        startEventScheduler();
    }

    @Override
    public void minigameEnd() {
        setRunning(false);

        // Stop random events
        if (eventScheduler != null) {
            eventScheduler.cancel();
            eventScheduler = null;
        }

        // Wait 10 seconds before ending
        Bukkit.getScheduler().runTaskLater(MinigameManager.plugin, () -> {
            // Teleport all players back to lobby
            for (Player player : getPlayers()) {
                WorldManager.teleportToLobby(player);
                playerResetter.resetPlayer(player);
            }

            // Delete the game world
            worldManager.deleteWorld();
        }, 200L); // 200 ticks = 10 seconds
    }

    private void startEventScheduler() {
        if (eventScheduler != null) {
            eventScheduler.cancel();
        }

        eventScheduler = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isRunning()) {
                    this.cancel();
                    return;
                }

                // Trigger a random event
                triggerRandomEvent();

                // Schedule next event with random delay
                long nextDelay = MIN_EVENT_DELAY + random.nextInt((int) (MAX_EVENT_DELAY - MIN_EVENT_DELAY));
                this.runTaskLater(MinigameManager.plugin, nextDelay);
            }
        };

        // Start first event after a random delay
        long initialDelay = MIN_EVENT_DELAY + random.nextInt((int) (MAX_EVENT_DELAY - MIN_EVENT_DELAY));
        eventScheduler.runTaskLater(MinigameManager.plugin, initialDelay);
    }

    /**
     * Triggers a random event from the available events.
     */
    public void triggerRandomEvent() {
        GameEvents.EVENTS.get(random.nextInt(GameEvents.EVENTS.size())).accept(this);
    }

    @Override
    public void playerJoin(Player player) {
        addPlayer(player);
    }

    @Override
    public void playerLeave(Player player) {
        removePlayer(player);

        // Check if we should end the game
        if (isRunning() && getPlayers().isEmpty()) {
            minigameEnd();
        }
    }

    @Override
    public void playerRejoin(Player player) {}

    /**
     * Gets the item manager for this game.
     * @return The CustomItemManager instance
     */
    public CustomItemManager getItemManager() {
        return itemManager;
    }
}