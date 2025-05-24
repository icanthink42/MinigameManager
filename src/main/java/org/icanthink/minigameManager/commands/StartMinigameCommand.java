package org.icanthink.minigameManager.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;
import org.icanthink.minigameManager.games.GroupHardcore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Command to start minigames.
 * Usage: /startminigame <type> [players...]
 */
public class StartMinigameCommand implements CommandExecutor {

    private final Map<String, Minigame> activeMinigames;

    public StartMinigameCommand() {
        this.activeMinigames = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /startminigame <type> [players...]");
            return false;
        }

        String minigameType = args[0].toLowerCase();

        // Only players can start minigames
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can start minigames.");
            return true;
        }

        Player initiator = (Player) sender;
        World world = initiator.getWorld();

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

        // Add the command initiator to the minigame
        minigame.playerJoin(initiator);

        // Add specified players to the minigame
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                Player target = Bukkit.getPlayer(args[i]);
                if (target != null && target.isOnline()) {
                    minigame.playerJoin(target);
                    target.sendMessage(ChatColor.GREEN + "You've been added to a " +
                        minigameType + " minigame by " + initiator.getName() + "!");
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "Player " + args[i] + " not found or offline.");
                }
            }
        }

        // Store the minigame with a unique identifier
        String minigameId = UUID.randomUUID().toString().substring(0, 8);
        activeMinigames.put(minigameId, minigame);

        // Start the minigame
        minigame.minigameStart();

        // Send confirmation message
        sender.sendMessage(ChatColor.GREEN + minigameType + " minigame started with ID: " + minigameId);
        sender.sendMessage(ChatColor.GREEN + "Players: " + getPlayerNames(minigame));

        return true;
    }

    /**
     * Adds a minigame to the active minigames map.
     * Used when starting a hosted minigame.
     *
     * @param id The ID for the minigame
     * @param minigame The minigame to add
     */
    public void addActiveMinigame(String id, Minigame minigame) {
        activeMinigames.put(id, minigame);
    }

    /**
     * Gets a minigame by its ID.
     *
     * @param id The minigame ID
     * @return The minigame, or null if not found
     */
    public Minigame getMinigame(String id) {
        return activeMinigames.get(id);
    }

    /**
     * Removes a minigame from the active minigames list.
     *
     * @param id The ID of the minigame to remove
     */
    public void removeMinigame(String id) {
        activeMinigames.remove(id);
    }

    /**
     * Gets a comma-separated list of player names in a minigame.
     *
     * @param minigame The minigame to get player names from
     * @return A string with all player names
     */
    private String getPlayerNames(Minigame minigame) {
        StringBuilder names = new StringBuilder();
        boolean first = true;

        for (Player player : minigame.getPlayers()) {
            if (!first) {
                names.append(", ");
            }
            names.append(player.getName());
            first = false;
        }

        return names.toString();
    }

    /**
     * Gets all active minigames.
     *
     * @return Map of minigame IDs to minigames
     */
    public Map<String, Minigame> getActiveMinigames() {
        return activeMinigames;
    }
}