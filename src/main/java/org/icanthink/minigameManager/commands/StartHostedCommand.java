package org.icanthink.minigameManager.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;

/**
 * Command to start a previously hosted minigame.
 * Usage: /starthosted <code>
 */
public class StartHostedCommand implements CommandExecutor {

    public StartHostedCommand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /starthosted <code>");
            return false;
        }

        // Only players can start minigames
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can start minigames.");
            return true;
        }

        String joinCode = args[0].toUpperCase();

        // Check if the join code is valid
        if (!MinigameManager.plugin.getHostMinigameCommand().isValidJoinCode(joinCode)) {
            sender.sendMessage(ChatColor.RED + "Invalid join code. Please check the code and try again.");
            return true;
        }

        // Get the minigame
        Minigame minigame = MinigameManager.plugin.getHostMinigameCommand().getPendingMinigame(joinCode);

        // Start the minigame
        minigame.minigameStart();

        // Move to active minigames and remove from pending
        String activeId = joinCode;
        MinigameManager.plugin.getStartMinigameCommand().addActiveMinigame(activeId, minigame);
        MinigameManager.plugin.getHostMinigameCommand().removePendingMinigame(joinCode);

        // Inform all players that the game has started
        for (Player player : minigame.getPlayers()) {
            player.sendMessage(ChatColor.GREEN + "The minigame has started!");
        }

        return true;
    }
}