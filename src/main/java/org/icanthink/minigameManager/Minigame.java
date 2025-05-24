package org.icanthink.minigameManager;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.icanthink.minigameManager.features.Feature;
import org.icanthink.minigameManager.features.items.CustomItemManager;
import org.icanthink.minigameManager.features.mobs.CustomMobManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class representing a minigame.
 * Serves as a superclass for all specific minigame implementations.
 */
public abstract class Minigame {

    private List<Player> players;
    private World world;
    private boolean isRunning;
    private final Map<Class<? extends Feature>, Feature> features;

    /**
     * Creates a new Minigame instance.
     *
     * @param world The world where the minigame takes place
     */
    public Minigame(World world) {
        this.players = new ArrayList<>();
        this.world = world;
        this.isRunning = false;
        this.features = new HashMap<>();
    }

    /**
     * Add a feature to the minigame.
     *
     * @param feature The feature to add
     */
    protected void addFeature(Feature feature) {
        features.put(feature.getClass(), feature);
    }

    /**
     * Get a feature by its class.
     *
     * @param featureClass The class of the feature to get
     * @return The feature instance, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T extends Feature> T getFeature(Class<T> featureClass) {
        return (T) features.get(featureClass);
    }

    /**
     * Get the CustomItemManager for this minigame.
     *
     * @return The CustomItemManager instance, or null if not found
     */
    public CustomItemManager getItemManager() {
        return getFeature(CustomItemManager.class);
    }

    /**
     * Get the CustomMobManager for this minigame.
     *
     * @return The CustomMobManager instance, or null if not found
     */
    public CustomMobManager getMobManager() {
        return getFeature(CustomMobManager.class);
    }

    /**
     * Called when the minigame starts.
     */
    public abstract void minigameStart();

    /**
     * Called when the minigame ends.
     */
    public abstract void minigameEnd();

    /**
     * Called when a player rejoins the minigame.
     *
     * @param player The player who rejoined
     */
    public abstract void playerRejoin(Player player);

    /**
     * Called when a player joins the minigame.
     *
     * @param player The player who joined
     */
    public abstract void playerJoin(Player player);

    /**
     * Called when a player leaves the minigame.
     *
     * @param player The player who left
     */
    public abstract void playerLeave(Player player);

    /**
     * Get the list of players in the minigame.
     *
     * @return List of players
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Get the world where the minigame takes place.
     *
     * @return The minigame world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Set the world where the minigame takes place.
     *
     * @param world The minigame world
     */
    public void setWorld(World world) {
        this.world = world;
    }

    /**
     * Add a player to the minigame.
     *
     * @param player The player to add
     */
    protected void addPlayer(Player player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    /**
     * Remove a player from the minigame.
     *
     * @param player The player to remove
     */
    protected void removePlayer(Player player) {
        players.remove(player);
    }

    /**
     * Check if the minigame is currently running.
     *
     * @return true if the minigame is running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Set whether the minigame is running.
     *
     * @param running true to set the minigame as running, false otherwise
     */
    protected void setRunning(boolean running) {
        this.isRunning = running;
    }

    /**
     * End the minigame.
     */
    public void end() {
        minigameEnd();
    }
}