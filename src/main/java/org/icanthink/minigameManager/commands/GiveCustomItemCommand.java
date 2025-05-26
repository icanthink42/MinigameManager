package org.icanthink.minigameManager.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;
import org.icanthink.minigameManager.features.items.CustomItem;
import org.icanthink.minigameManager.features.items.TeleportRod;
import org.icanthink.minigameManager.features.items.PlayerTracker;
import org.icanthink.minigameManager.features.items.ShuffleSword;
import org.icanthink.minigameManager.features.items.BrazilStick;
import org.icanthink.minigameManager.features.items.CombatLog;
import org.icanthink.minigameManager.features.items.WishItem;
import org.icanthink.minigameManager.features.items.CursedPumpkinItem;
import org.icanthink.minigameManager.features.items.KnockbackStick;
import org.icanthink.minigameManager.features.items.Shoes;
import org.icanthink.minigameManager.features.items.FlyingShield;
import org.icanthink.minigameManager.utils.ChatGPTClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command to give custom items to players.
 * Usage: /givecustomitem <item> [player]
 */
public class GiveCustomItemCommand implements CommandExecutor {
    public static final Map<String, Class<? extends CustomItem>> availableItems = new HashMap<>();
    private final ChatGPTClient chatGPTClient;

    public GiveCustomItemCommand() {
        // Initialize ChatGPT client
        this.chatGPTClient = new ChatGPTClient(MinigameManager.plugin.getConfig());

        // Register available custom items (if not already registered)
        if (availableItems.isEmpty()) {
            availableItems.put("teleportrod", TeleportRod.class);
            availableItems.put("playertracker", PlayerTracker.class);
            availableItems.put("shufflesword", ShuffleSword.class);
            availableItems.put("brazilstick", BrazilStick.class);
            availableItems.put("combatlog", CombatLog.class);
            availableItems.put("wish", WishItem.class);
            availableItems.put("cursedpumpkin", CursedPumpkinItem.class);
            availableItems.put("knockbackstick", KnockbackStick.class);
            availableItems.put("shoes", Shoes.class);
            availableItems.put("flyingshield", FlyingShield.class);
        }
    }

    public static List<String> getAvailableItemNames() {
        return new ArrayList<>(availableItems.keySet());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /givecustomitem <item> [player]");
            sender.sendMessage(ChatColor.YELLOW + "Available items: " + String.join(", ", getAvailableItemNames()));
            return false;
        }

        String itemName = args[0].toLowerCase();
        Class<? extends CustomItem> itemClass = availableItems.get(itemName);

        if (itemClass == null) {
            sender.sendMessage(ChatColor.RED + "Unknown item: " + itemName);
            sender.sendMessage(ChatColor.YELLOW + "Available items: " + String.join(", ", getAvailableItemNames()));
            return false;
        }

        Player target;
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                return false;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must specify a player when using this command from console.");
                return false;
            }
            target = (Player) sender;
        }

        // Find an active minigame that the target player is in
        Minigame targetMinigame = null;
        for (Minigame minigame : MinigameManager.plugin.getStartMinigameCommand().getActiveMinigames().values()) {
            if (minigame.getPlayers().contains(target)) {
                targetMinigame = minigame;
                break;
            }
        }

        if (targetMinigame == null) {
            sender.sendMessage(ChatColor.RED + target.getName() + " is not in any active minigame.");
            return false;
        }

        // Give the item to the player
        boolean success = targetMinigame.getItemManager().giveItem(target, itemClass);

        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Gave " + itemName + " to " + target.getName());
            if (sender != target) {
                target.sendMessage(ChatColor.GREEN + "You received a " + itemName);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to give item to " + target.getName() + " (inventory full?)");
        }

        return true;
    }
}