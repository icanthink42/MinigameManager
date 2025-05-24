package org.icanthink.minigameManager.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;

/**
 * Command to join a hosted minigame using a join code.
 * Usage: /joinminigame <code>
 */
public class JoinMinigameCommand implements CommandExecutor {

    public JoinMinigameCommand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /joinminigame <code>");
            return false;
        }

        // Only players can join minigames
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can join minigames.");
            return true;
        }

        Player player = (Player) sender;
        String joinCode = args[0].toUpperCase();

        // Check if the join code is valid
        if (!MinigameManager.plugin.getHostMinigameCommand().isValidJoinCode(joinCode)) {
            sender.sendMessage(ChatColor.RED + "Invalid join code. Please check the code and try again.");
            return true;
        }

        // Get the minigame and add the player
        Minigame minigame = MinigameManager.plugin.getHostMinigameCommand().getPendingMinigame(joinCode);
        minigame.playerJoin(player);

        // Inform the player and other participants
        player.sendMessage(ChatColor.GREEN + "You have joined the minigame!");

        // Inform other players in the minigame that someone joined
        for (Player otherPlayer : minigame.getPlayers()) {
            if (otherPlayer != player) {
                otherPlayer.sendMessage(ChatColor.GREEN + player.getName() + " has joined the minigame!");
            }
        }

        return true;
    }
}