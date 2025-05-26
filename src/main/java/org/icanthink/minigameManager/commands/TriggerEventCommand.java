package org.icanthink.minigameManager.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;
import org.icanthink.minigameManager.games.grouphardcore.GroupHardcore;

import java.util.Map;

/**
 * Command to manually trigger a game event in an active minigame.
 * Usage: /triggerevent [game_id]
 */
public class TriggerEventCommand implements CommandExecutor {

    public TriggerEventCommand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Only players can use this command without a game ID
        if (args.length < 1 && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Console must specify a game ID: /triggerevent <game_id>");
            return false;
        }

        GroupHardcore game;
        String gameId;

        if (args.length < 1) {
            // No game ID provided, try to find the player's current game
            Player player = (Player) sender;
            Map<String, Minigame> activeGames = MinigameManager.plugin.getStartMinigameCommand().getActiveMinigames();

            GroupHardcore currentGame = null;
            String currentGameId = null;

            for (Map.Entry<String, Minigame> entry : activeGames.entrySet()) {
                Minigame minigame = entry.getValue();
                if (minigame instanceof GroupHardcore && minigame.getPlayers().contains(player)) {
                    currentGame = (GroupHardcore) minigame;
                    currentGameId = entry.getKey();
                    break;
                }
            }

            if (currentGame == null) {
                sender.sendMessage(ChatColor.RED + "You are not in any active GroupHardcore game. Please specify a game ID.");
                return true;
            }

            game = currentGame;
            gameId = currentGameId;
        } else {
            // Game ID provided, try to find that specific game
            gameId = args[0].toUpperCase();
            Minigame minigame = MinigameManager.plugin.getStartMinigameCommand().getMinigame(gameId);

            if (minigame == null) {
                sender.sendMessage(ChatColor.RED + "No active minigame found with ID: " + gameId);
                return true;
            }

            // Check if it's a GroupHardcore game
            if (!(minigame instanceof GroupHardcore)) {
                sender.sendMessage(ChatColor.RED + "This command only works with GroupHardcore games.");
                return true;
            }

            game = (GroupHardcore) minigame;
        }

        // Trigger a random event
        game.triggerRandomEvent();

        // Inform the sender
        sender.sendMessage(ChatColor.GREEN + "Successfully triggered a random event in game: " + gameId);

        return true;
    }
}