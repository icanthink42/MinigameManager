package org.icanthink.minigameManager.games;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;
import org.icanthink.minigameManager.features.InstantSmelting;
import org.icanthink.minigameManager.features.PlayerResetter;
import org.icanthink.minigameManager.features.WorldManager;
import org.icanthink.minigameManager.features.DeathManager;
import org.icanthink.minigameManager.features.items.CustomItemManager;
import org.icanthink.minigameManager.features.items.PlayerTracker;
import org.icanthink.minigameManager.features.mobs.CustomMobManager;

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
    }

    @Override
    public void minigameEnd() {
        setRunning(false);

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

    @Override
    public void playerJoin(Player player) {
        // Add player to the game
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
}