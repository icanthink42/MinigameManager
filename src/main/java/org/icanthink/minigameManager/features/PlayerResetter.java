package org.icanthink.minigameManager.features;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.icanthink.minigameManager.Minigame;

/**
 * Feature that provides player state management functionality for minigames.
 */
public class PlayerResetter extends Feature {

    public PlayerResetter(Minigame minigame) {
        super(minigame);
    }

    /**
     * Reset a player's state for the minigame.
     *
     * @param player The player to reset
     * @param scoreboard The scoreboard to apply
     */
    public void resetPlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setExp(0);
        player.setLevel(0);
        player.getInventory().clear();
    }

    /**
     * Reset a player's state when they join a running minigame.
     *
     * @param player The player to reset
     * @param scoreboard The scoreboard to apply
     */
    public void resetJoiningPlayer(Player player) {
        resetPlayer(player);
    }

    /**
     * Set a player to spectator mode.
     *
     * @param player The player to set as spectator
     * @param scoreboard The scoreboard to apply
     */
    public void setPlayerSpectator(Player player) {
        player.setGameMode(GameMode.SPECTATOR);
    }
}