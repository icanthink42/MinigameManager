package org.icanthink.minigameManager.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;

import java.util.Map;

/**
 * Command to list all active minigames.
 * Usage: /listgames
 */
public class ListGamesCommand implements CommandExecutor {

    public ListGamesCommand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Map<String, Minigame> activeGames = MinigameManager.plugin.getStartMinigameCommand().getActiveMinigames();
        Map<String, Minigame> pendingGames = MinigameManager.plugin.getHostMinigameCommand().getPendingMinigames();

        if (activeGames.isEmpty() && pendingGames.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "There are no active or pending minigames.");
            return true;
        }

        // List active games
        if (!activeGames.isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "Active Minigames:");
            for (Map.Entry<String, Minigame> entry : activeGames.entrySet()) {
                Minigame game = entry.getValue();
                String gameId = entry.getKey();
                String playerList = String.join(", ", game.getPlayers().stream()
                        .map(Player::getName)
                        .toArray(String[]::new));

                sender.sendMessage(ChatColor.GOLD + "ID: " + gameId);
                sender.sendMessage(ChatColor.YELLOW + "- Type: " + game.getClass().getSimpleName());
                sender.sendMessage(ChatColor.YELLOW + "- Players: " + playerList);
                sender.sendMessage(ChatColor.YELLOW + "- World: " + game.getWorld().getName());
            }
        }

        // List pending games
        if (!pendingGames.isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "\nPending Minigames:");
            for (Map.Entry<String, Minigame> entry : pendingGames.entrySet()) {
                Minigame game = entry.getValue();
                String joinCode = entry.getKey();
                String playerList = String.join(", ", game.getPlayers().stream()
                        .map(Player::getName)
                        .toArray(String[]::new));

                sender.sendMessage(ChatColor.GOLD + "Join Code: " + joinCode);
                sender.sendMessage(ChatColor.YELLOW + "- Type: " + game.getClass().getSimpleName());
                sender.sendMessage(ChatColor.YELLOW + "- Players: " + playerList);
                sender.sendMessage(ChatColor.YELLOW + "- World: " + game.getWorld().getName());
            }
        }

        return true;
    }
}