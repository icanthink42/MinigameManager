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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Command to host a minigame without starting it immediately.
 * Usage: /hostminigame <type> [players...]
 */
public class HostMinigameCommand implements CommandExecutor {

    private final Map<String, Minigame> pendingMinigames;

    public HostMinigameCommand() {
        this.pendingMinigames = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /hostminigame <type> [players...]");
            return false;
        }

        String minigameType = args[0].toLowerCase();

        // Only players can host minigames
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can host minigames.");
            return true;
        }

        Player host = (Player) sender;
        World world = host.getWorld();

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

        // Add the host to the minigame
        minigame.playerJoin(host);

        // Add specified players to the minigame
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                Player target = Bukkit.getPlayer(args[i]);
                if (target != null && target.isOnline()) {
                    minigame.playerJoin(target);
                    target.sendMessage(ChatColor.GREEN + "You've been added to a " +
                        minigameType + " minigame hosted by " + host.getName() + "!");
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "Player " + args[i] + " not found or offline.");
                }
            }
        }

        // Generate a short code for easier joining
        String minigameId = generateJoinCode();
        pendingMinigames.put(minigameId, minigame);

        // Send confirmation messages
        sender.sendMessage(ChatColor.GREEN + "Minigame hosted successfully!");
        sender.sendMessage(ChatColor.GREEN + "Join code: " + minigameId);
        sender.sendMessage(ChatColor.GREEN + "Players: " + getPlayerNames(minigame));
        sender.sendMessage(ChatColor.YELLOW + "Use /startgame " + minigameId + " to start the game when ready.");

        return true;
    }

    /**
     * Generates a random join code for the minigame.
     *
     * @return A random join code
     */
    private String generateJoinCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Gets a pending minigame by its join code.
     *
     * @param code The join code
     * @return The minigame, or null if not found
     */
    public Minigame getPendingMinigame(String code) {
        return pendingMinigames.get(code.toUpperCase());
    }

    /**
     * Gets all pending minigame codes.
     *
     * @return List of all pending minigame codes
     */
    public List<String> getAllPendingCodes() {
        return new ArrayList<>(pendingMinigames.keySet());
    }

    /**
     * Removes a pending minigame.
     *
     * @param code The join code of the minigame to remove
     */
    public void removePendingMinigame(String code) {
        pendingMinigames.remove(code.toUpperCase());
    }

    /**
     * Checks if a join code is valid.
     *
     * @param code The join code to check
     * @return true if the code exists, false otherwise
     */
    public boolean isValidJoinCode(String code) {
        return pendingMinigames.containsKey(code.toUpperCase());
    }

    /**
     * Gets all pending minigames.
     *
     * @return Map of join codes to minigames
     */
    public Map<String, Minigame> getPendingMinigames() {
        return pendingMinigames;
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
}