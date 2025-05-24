package org.icanthink.minigameManager.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;

/**
 * Command to stop an active minigame.
 * Usage: /stopgame <id>
 */
public class StopGameCommand implements CommandExecutor {

    public StopGameCommand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /stopgame <id>");
            return false;
        }

        String gameId = args[0].toUpperCase();
        Minigame minigame = MinigameManager.plugin.getStartMinigameCommand().getMinigame(gameId);

        if (minigame == null) {
            sender.sendMessage(ChatColor.RED + "No active minigame found with ID: " + gameId);
            return true;
        }

        // End the minigame
        minigame.minigameEnd();
        MinigameManager.plugin.getStartMinigameCommand().removeMinigame(gameId);

        // Inform players
        sender.sendMessage(ChatColor.GREEN + "Successfully stopped minigame with ID: " + gameId);

        return true;
    }
}