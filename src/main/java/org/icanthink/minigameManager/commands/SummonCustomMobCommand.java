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
        if (args.length >= 4) {
            try {
                double x = Double.parseDouble(args[1]);
                double y = Double.parseDouble(args[2]);
                double z = Double.parseDouble(args[3]);

                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be a player to use coordinates.");
                    return false;
                }
                Player player = (Player) sender;
                spawnLocation = new Location(player.getWorld(), x, y, z);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid coordinates. Please provide valid numbers.");
                return false;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to use this command without coordinates.");
                return false;
            }
            spawnLocation = ((Player) sender).getLocation();
        }

        // Find an active minigame that the sender is in
        Minigame targetMinigame = null;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            for (Minigame minigame : MinigameManager.plugin.getStartMinigameCommand().getActiveMinigames().values()) {
                if (minigame.getPlayers().contains(player)) {
                    targetMinigame = minigame;
                    break;
                }
            }
        }

        if (targetMinigame == null) {
            sender.sendMessage(ChatColor.RED + "You must be in an active minigame to summon custom mobs.");
            return false;
        }

        try {
            // Create and spawn the custom mob using the CustomMobManager
            LivingEntity entity = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, mobClass.getDeclaredConstructor(Minigame.class).newInstance(targetMinigame).entityType);
            CustomMobManager mobManager = targetMinigame.getMobManager();
            mobManager.spawnMob(entity, mobClass);

            sender.sendMessage(ChatColor.GREEN + "Summoned " + mobName + " at " +
                String.format("%.1f, %.1f, %.1f", spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ()));
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to summon mob: " + e.getMessage());
        }

        return true;
    }
}