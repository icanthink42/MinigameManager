package org.icanthink.minigameManager.features;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.icanthink.minigameManager.Minigame;

import java.io.File;
import java.util.UUID;

/**
 * Feature that provides world management functionality for minigames.
 * Can create and delete worlds for minigames.
 */
public class WorldManager extends Feature {
    private static World lobbyWorld;
    private World gameWorld;

    public WorldManager(Minigame minigame) {
        super(minigame);
        // Set the lobby world if it hasn't been set yet
        if (lobbyWorld == null) {
            lobbyWorld = minigame.getWorld();
        }
    }

    /**
     * Get the lobby world.
     *
     * @return The lobby world
     */
    public static World getLobbyWorld() {
        return lobbyWorld;
    }

    /**
     * Set the lobby world.
     *
     * @param world The world to set as the lobby
     */
    public static void setLobbyWorld(World world) {
        lobbyWorld = world;
    }

    /**
     * Teleport a player to the lobby world's spawn location.
     *
     * @param player The player to teleport
     */
    public static void teleportToLobby(Player player) {
        if (lobbyWorld != null) {
            player.teleport(lobbyWorld.getSpawnLocation());
        }
    }

    /**
     * Create a new world for the minigame.
     *
     * @param worldName Optional name for the world. If null, a random name will be generated.
     * @param worldType The type of world to create (NORMAL, FLAT, etc.)
     * @return The created world
     */
    public World createWorld(String worldName, WorldType worldType) {
        // Generate a unique world name if none provided
        if (worldName == null || worldName.isEmpty()) {
            worldName = "minigame_" + UUID.randomUUID().toString().substring(0, 8);
        }

        // Create the world
        WorldCreator creator = new WorldCreator(worldName);
        creator.type(worldType);
        creator.generateStructures(true);

        gameWorld = creator.createWorld();

        if (gameWorld != null) {
            // Set the world as the minigame's world
            minigame.setWorld(gameWorld);
        }

        return gameWorld;
    }

    /**
     * Delete the minigame world.
     * This should be called when the minigame ends.
     */
    public void deleteWorld() {
        if (gameWorld != null) {
            // Teleport all players back to lobby before deleting
            for (Player player : minigame.getPlayers()) {
                teleportToLobby(player);
            }

            // Unload the world first
            if (Bukkit.unloadWorld(gameWorld, false)) {
                // Delete the world folder
                File worldFolder = gameWorld.getWorldFolder();
                if (worldFolder.exists()) {
                    deleteDirectory(worldFolder);
                }
            }
            gameWorld = null;
        }
    }

    /**
     * Get the current game world.
     *
     * @return The current game world, or null if none exists
     */
    public World getGameWorld() {
        return gameWorld;
    }

    /**
     * Helper method to recursively delete a directory.
     *
     * @param directory The directory to delete
     * @return true if deletion was successful
     */
    private boolean deleteDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directory.delete();
    }
}