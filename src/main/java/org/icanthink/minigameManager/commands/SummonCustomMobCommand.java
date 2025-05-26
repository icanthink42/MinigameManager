package org.icanthink.minigameManager.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;
import org.icanthink.minigameManager.features.mobs.CustomMob;
import org.icanthink.minigameManager.features.mobs.CustomMobManager;
import org.icanthink.minigameManager.features.mobs.InvincibleZombie;
import org.icanthink.minigameManager.features.mobs.SpecialDog;
import org.icanthink.minigameManager.features.mobs.BusinessVillager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command to summon custom mobs.
 * Usage: /summoncustommob <mob> [x y z]
 */
public class SummonCustomMobCommand implements CommandExecutor {
    public static final Map<String, Class<? extends CustomMob>> availableMobs = new HashMap<>();

    public SummonCustomMobCommand() {
        // Register available custom mobs (if not already registered)
        if (availableMobs.isEmpty()) {
            availableMobs.put("invinciblezombie", InvincibleZombie.class);
            availableMobs.put("specialdog", SpecialDog.class);
            availableMobs.put("mrbusiness", BusinessVillager.class);
        }
    }

    public static List<String> getAvailableMobNames() {
        return new ArrayList<>(availableMobs.keySet());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /summoncustommob <mob> [x y z]");
            sender.sendMessage(ChatColor.YELLOW + "Available mobs: " + String.join(", ", getAvailableMobNames()));
            return false;
        }

        String mobName = args[0].toLowerCase();
        Class<? extends CustomMob> mobClass = availableMobs.get(mobName);

        if (mobClass == null) {
            sender.sendMessage(ChatColor.RED + "Unknown mob: " + mobName);
            sender.sendMessage(ChatColor.YELLOW + "Available mobs: " + String.join(", ", getAvailableMobNames()));
            return false;
        }

        // Get spawn location
        Location spawnLocation;
        Player targetPlayer;
        Minigame targetMinigame = null;

        if (!(sender instanceof Player)) {
            if (args.length < 4) {
                sender.sendMessage(ChatColor.RED + "Console must specify coordinates: /summoncustommob <mob> <x> <y> <z>");
                return false;
            }
            try {
                double x = Double.parseDouble(args[1]);
                double y = Double.parseDouble(args[2]);
                double z = Double.parseDouble(args[3]);
                spawnLocation = new Location(Bukkit.getWorlds().get(0), x, y, z);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid coordinates!");
                return false;
            }
            targetPlayer = null;
        } else {
            targetPlayer = (Player) sender;
            if (args.length >= 4) {
                try {
                    double x = Double.parseDouble(args[1]);
                    double y = Double.parseDouble(args[2]);
                    double z = Double.parseDouble(args[3]);
                    spawnLocation = new Location(targetPlayer.getWorld(), x, y, z);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid coordinates!");
                    return false;
                }
            } else {
                spawnLocation = targetPlayer.getLocation();
            }
        }

        // Find the minigame the player is in
        if (targetPlayer != null) {
            for (Minigame minigame : MinigameManager.plugin.getStartMinigameCommand().getActiveMinigames().values()) {
                if (minigame.getPlayers().contains(targetPlayer)) {
                    targetMinigame = minigame;
                    break;
                }
            }
        }

        if (targetMinigame == null) {
            sender.sendMessage(ChatColor.RED + "No active minigame found! The mob will be spawned without minigame features.");
            return false;
        }

        try {
            // Spawn the mob using the CustomMobManager
            targetMinigame.getMobManager().spawnMob(spawnLocation, mobClass);

            sender.sendMessage(ChatColor.GREEN + "Summoned " + mobName + " at " +
                String.format("%.1f, %.1f, %.1f", spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ()));
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to summon mob: " + e.getMessage());
        }

        return true;
    }
}