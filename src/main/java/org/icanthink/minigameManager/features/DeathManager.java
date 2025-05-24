package org.icanthink.minigameManager.features;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Feature that provides death management functionality for minigames.
 */
public class DeathManager extends Feature implements Listener {
    private final List<Player> deadPlayers = new ArrayList<>();
    private Consumer<Player> deathCallback;

    public DeathManager(Minigame minigame) {
        super(minigame);
        // Register this class as an event listener
        Bukkit.getPluginManager().registerEvents(this, MinigameManager.plugin);
    }

    /**
     * Set a callback that will be called when a player dies.
     *
     * @param callback The callback to be called when a player dies
     */
    public void setDeathCallback(Consumer<Player> callback) {
        this.deathCallback = callback;
    }

    /**
     * Get the list of dead players in the minigame.
     *
     * @return List of dead players
     */
    public List<Player> getDeadPlayers() {
        return deadPlayers;
    }
    /**
     * Check if the minigame is currently running.
     *
     * @return true if the minigame is running, false otherwise
     */
    public boolean isMinigameRunning() {
        return minigame.isRunning();
    }

    /**
     * Check if a player is dead in the minigame.
     *
     * @param player The player to check
     * @return true if the player is dead, false otherwise
     */
    public boolean isPlayerDead(Player player) {
        return deadPlayers.contains(player);
    }

    /**
     * Get all living players in the minigame.
     *
     * @return List of living players
     */
    public List<Player> getLivingPlayers() {
        List<Player> livingPlayers = new ArrayList<>(minigame.getPlayers());
        livingPlayers.removeAll(deadPlayers);
        return livingPlayers;
    }

    /**
     * Count the number of living players in the minigame.
     *
     * @return The count of living players
     */
    public int getLivingPlayerCount() {
        return minigame.getPlayers().size() - deadPlayers.size();
    }

    /**
     * Revive a player in the minigame.
     *
     * @param player The player to revive
     * @return true if the player was revived, false if they weren't dead
     */
    public boolean revivePlayer(Player player) {
        if (isPlayerDead(player)) {
            deadPlayers.remove(player);

            // If using PlayerResetter, reset the player
            PlayerResetter resetter = minigame.getFeature(PlayerResetter.class);
            if (resetter != null) {
                resetter.resetPlayer(player);
            }

            return true;
        }
        return false;
    }

    /**
     * Set a player as dead.
     *
     * @param player The player who died
     */
    public void setPlayerAsDead(Player player) {
        if (!isPlayerDead(player)) {
            deadPlayers.add(player);

            // If using PlayerResetter, set player to spectator
            PlayerResetter resetter = minigame.getFeature(PlayerResetter.class);
            if (resetter != null) {
                resetter.setPlayerSpectator(player);
            }

            // Call the death callback if one is set
            if (deathCallback != null) {
                deathCallback.accept(player);
            }
        }
    }

    /**
     * Handle player damage in minigames to prevent deaths and set to spectator.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent event) {
        // If the event is already cancelled, don't proceed
        if (event.isCancelled()) return;

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Check if the player is in this minigame and the minigame is running
        if (!minigame.isRunning() || !minigame.getPlayers().contains(player)) {
            return;
        }

        // Check if this damage would be fatal
        if (player.getHealth() - event.getFinalDamage() > 0) {
            return;
        }

        // Cancel the damage event to prevent death
        event.setCancelled(true);

        // Set the player as dead
        setPlayerAsDead(player);

    }
}