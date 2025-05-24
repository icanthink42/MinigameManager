package org.icanthink.minigameManager.tabcompleters;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.icanthink.minigameManager.MinigameManager;
import org.icanthink.minigameManager.commands.GiveCustomItemCommand;
import org.icanthink.minigameManager.commands.SummonCustomMobCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides tab completion for all minigame commands.
 */
public class MinigameTabCompleter implements TabCompleter {

    private final List<String> minigameTypes;
    private final List<String> customItems;
    private final List<String> customMobs;

    public MinigameTabCompleter() {
        // Add all available minigame types here
        this.minigameTypes = Arrays.asList("grouphardcore");
        // Dynamically fetch all available custom items
        this.customItems = GiveCustomItemCommand.getAvailableItemNames();
        // Dynamically fetch all available custom mobs
        this.customMobs = SummonCustomMobCommand.getAvailableMobNames();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("hostgame")) {
            if (args.length == 1) {
                // First argument: minigame type
                completions.addAll(getMinigameTypeCompletions(args[0]));
            } else if (args.length > 1) {
                // Additional arguments: player names
                completions.addAll(getPlayerCompletions(args[args.length - 1]));
            }
        } else if (command.getName().equalsIgnoreCase("startservergame")) {
            if (args.length == 1) {
                // First argument: minigame type
                completions.addAll(getMinigameTypeCompletions(args[0]));
            }
        } else if (command.getName().equalsIgnoreCase("joinminigame")) {
            if (args.length == 1) {
                // First argument: join code
                completions.addAll(getPendingGameCodes(args[0]));
            }
        } else if (command.getName().equalsIgnoreCase("startgame")) {
            if (args.length == 1) {
                // First argument: join code
                completions.addAll(getPendingGameCodes(args[0]));
            }
        } else if (command.getName().equalsIgnoreCase("stopgame")) {
            if (args.length == 1) {
                // First argument: game ID
                completions.addAll(getActiveGameIds(args[0]));
            }
        } else if (command.getName().equalsIgnoreCase("givecustomitem")) {
            if (args.length == 1) {
                // First argument: item name
                completions.addAll(getCustomItemCompletions(args[0]));
            } else if (args.length == 2) {
                // Second argument: player name
                completions.addAll(getPlayerCompletions(args[1]));
            }
        } else if (command.getName().equalsIgnoreCase("summoncustommob")) {
            if (args.length == 1) {
                // First argument: mob name
                completions.addAll(getCustomMobCompletions(args[0]));
            } else if (args.length >= 2 && args.length <= 4) {
                // Second through fourth arguments: coordinates
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Location loc = player.getLocation();
                    if (args.length == 2) {
                        completions.add(String.format("%.1f", loc.getX()));
                    } else if (args.length == 3) {
                        completions.add(String.format("%.1f", loc.getY()));
                    } else if (args.length == 4) {
                        completions.add(String.format("%.1f", loc.getZ()));
                    }
                }
            }
        }

        return completions;
    }

    /**
     * Gets minigame type completions that match the current input.
     *
     * @param current The current input
     * @return List of matching minigame types
     */
    private List<String> getMinigameTypeCompletions(String current) {
        if (current.isEmpty()) {
            return minigameTypes;
        }

        return minigameTypes.stream()
                .filter(type -> type.toLowerCase().startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Gets custom item completions that match the current input.
     *
     * @param current The current input
     * @return List of matching custom items
     */
    private List<String> getCustomItemCompletions(String current) {
        if (current.isEmpty()) {
            return customItems;
        }

        return customItems.stream()
                .filter(item -> item.toLowerCase().startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Gets custom mob completions that match the current input.
     *
     * @param current The current input
     * @return List of matching custom mobs
     */
    private List<String> getCustomMobCompletions(String current) {
        if (current.isEmpty()) {
            return customMobs;
        }

        return customMobs.stream()
                .filter(mob -> mob.toLowerCase().startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Gets online player name completions that match the current input.
     *
     * @param current The current input
     * @return List of matching player names
     */
    private List<String> getPlayerCompletions(String current) {
        List<String> playerNames = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            String name = player.getName();
            if (current.isEmpty() || name.toLowerCase().startsWith(current.toLowerCase())) {
                playerNames.add(name);
            }
        }

        return playerNames;
    }

    /**
     * Gets pending game codes that match the current input.
     *
     * @param current The current input
     * @return List of matching game codes
     */
    private List<String> getPendingGameCodes(String current) {
        if (MinigameManager.plugin.getHostMinigameCommand() == null) {
            return Collections.emptyList();
        }

        List<String> codes = MinigameManager.plugin.getHostMinigameCommand().getAllPendingCodes();

        if (current.isEmpty()) {
            return codes;
        }

        return codes.stream()
                .filter(code -> code.toUpperCase().startsWith(current.toUpperCase()))
                .collect(Collectors.toList());
    }

    /**
     * Gets active game IDs that match the current input.
     *
     * @param current The current input
     * @return List of matching game IDs
     */
    private List<String> getActiveGameIds(String current) {
        List<String> ids = new ArrayList<>(MinigameManager.plugin.getStartMinigameCommand().getActiveMinigames().keySet());
        if (current.isEmpty()) {
            return ids;
        }

        return ids.stream()
                .filter(id -> id.toUpperCase().startsWith(current.toUpperCase()))
                .collect(Collectors.toList());
    }
}