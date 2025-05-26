package org.icanthink.minigameManager.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;
import org.icanthink.minigameManager.games.grouphardcore.GroupHardcore;

import java.util.UUID;

/**
 * Command to start a minigame with all online players in the server.
 * Usage: /startservergame <type>
 */
public class StartServerMinigameCommand implements CommandExecutor {

    public StartServerMinigameCommand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /startservergame <type>");
            return false;
        }

        String minigameType = args[0].toLowerCase();

        // Get the world to use (either sender's world or the main world)
        World world;
        if (sender instanceof Player) {
            world = ((Player) sender).getWorld();
        } else {
            world = Bukkit.getWorlds().get(0); // Use the main world
        }

        // Create the appropriate minigame based on the type
        Minigame minigame;
        switch (minigameType) {
            case "grouphardcore":
                minigame = new GroupHardcore(world);
                break;
            // Add more minigame types here as they are developed
            default:
                sender.sendMessage(ChatColor.RED + "Unknown minigame type: " + minigameType);
                return false;
        }

        // Add all online players to the minigame
        int playerCount = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            minigame.playerJoin(player);
            playerCount++;

            // Inform the player they've been added to the game
            player.sendMessage(ChatColor.GREEN + "You've been added to a server-wide " +
                minigameType + " minigame!");
        }

        // Check if we have enough players
        if (playerCount < 1) {
            sender.sendMessage(ChatColor.RED + "No players online to start the minigame.");
            return true;
        }

        // Generate a unique ID and store the minigame
        String minigameId = UUID.randomUUID().toString().substring(0, 8);
        MinigameManager.plugin.getStartMinigameCommand().addActiveMinigame(minigameId, minigame);

        // Start the minigame
        minigame.minigameStart();

        // Broadcast game start
        Bukkit.broadcastMessage(ChatColor.GREEN + "A server-wide " + minigameType + " minigame has started!");
        Bukkit.broadcastMessage(ChatColor.GREEN + "Game ID: " + minigameId);

        return true;
    }
}