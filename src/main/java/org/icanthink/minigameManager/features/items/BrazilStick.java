package org.icanthink.minigameManager.features.items;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.scheduler.BukkitRunnable;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;
import org.bukkit.event.EventPriority;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A custom item that sends players to Brazil (nearest jungle biome).
 */
public class BrazilStick extends CustomItem implements Listener {
    private static final String NAME = ChatColor.GREEN + "Brazil Stick";
    private static final Material MATERIAL = Material.STICK;
    private static final List<String> LORE = Arrays.asList(
        ChatColor.GRAY + "Hit a player to",
        ChatColor.GRAY + "send them to Brazil!"
    );
    private static final Random RANDOM = new Random();

    public BrazilStick(Minigame minigame) {
        super(minigame);
        MinigameManager.plugin.getServer().getPluginManager().registerEvents(this, MinigameManager.plugin);
        setMetaCustomizer(meta -> {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });
    }

    @Override
    protected String getName() {
        return NAME;
    }

    @Override
    protected Material getMaterial() {
        return MATERIAL;
    }

    @Override
    protected List<String> getLore() {
        return LORE;
    }

    public void onEntityDamage(EntityDamageByEntityEvent event) {

        Player sender = (Player) event.getDamager();
        Player target = (Player) event.getEntity();
        ItemStack item = sender.getInventory().getItemInMainHand();

        if (!isInstance(item)) return;
        if (!minigame.getPlayers().contains(sender) || !minigame.getPlayers().contains(target)) return;

        // Find nearest jungle biome in the overworld
        Location jungleLoc = findNearestJungle(target.getLocation());
        if (jungleLoc != null) {
            // Add some random offset to make it more interesting
            jungleLoc.add(RANDOM.nextInt(100) - 50, 0, RANDOM.nextInt(100) - 50);

            // Find a safe location
            Location safeLoc = findSafeLocation(jungleLoc);
            if (safeLoc != null) {
                // Make player temporarily invulnerable
                target.setInvulnerable(true);

                // Teleport with a small delay to prevent fall damage
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        target.teleport(safeLoc);
                        target.sendMessage(ChatColor.GREEN + "You've been sent to Brazil!");
                        sender.sendMessage(ChatColor.GREEN + "You sent " + target.getName() + " to Brazil!");

                        // Remove invulnerability after 3 seconds
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                target.setInvulnerable(false);
                            }
                        }.runTaskLater(MinigameManager.plugin, 60L); // 3 seconds = 60 ticks
                    }
                }.runTaskLater(MinigameManager.plugin, 5L); // Small delay before teleport
            } else {
                sender.sendMessage(ChatColor.RED + "Couldn't find a safe spot in Brazil!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Couldn't find any jungle biomes in the overworld!");
        }
    }

    private Location findNearestJungle(Location start) {
        // Always search in the overworld
        World overworld = start.getWorld().getEnvironment() == World.Environment.NORMAL
            ? start.getWorld()
            : MinigameManager.plugin.getServer().getWorlds().stream()
                .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
                .findFirst()
                .orElse(null);

        if (overworld == null) {
            return null;
        }

        // Convert coordinates to overworld if needed
        Location overworldLoc = start.getWorld().getEnvironment() == World.Environment.NORMAL
            ? start.clone()
            : new Location(overworld, start.getX() / 8, start.getY(), start.getZ() / 8);

        // Search in a spiral pattern from the start location
        int radius = 0;
        int maxRadius = 10000; // Maximum search radius

        while (radius < maxRadius) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) == radius || Math.abs(z) == radius) {
                        Location loc = overworldLoc.clone().add(x, 0, z);
                        if (overworld.getBiome(loc).name().contains("JUNGLE")) {
                            return loc;
                        }
                    }
                }
            }
            radius += 100; // Increase search radius
        }
        return null;
    }

    private Location findSafeLocation(Location loc) {
        World world = loc.getWorld();
        int maxAttempts = 10;
        int attempts = 0;

        while (attempts < maxAttempts) {
            // Get the highest solid block at this location
            int y = world.getHighestBlockYAt(loc);
            Location safeLoc = new Location(world, loc.getX(), y + 1, loc.getZ());

            // Check if the location is safe
            if (isSafeLocation(safeLoc)) {
                return safeLoc;
            }

            // Try a different location
            loc.add(RANDOM.nextInt(20) - 10, 0, RANDOM.nextInt(20) - 10);
            attempts++;
        }

        return null;
    }

    private boolean isSafeLocation(Location loc) {
        // Check if the block below is solid
        if (!loc.getBlock().getRelative(0, -1, 0).getType().isSolid()) {
            return false;
        }

        // Check if the block at the location and above are air
        if (!loc.getBlock().getType().isAir() || !loc.getBlock().getRelative(0, 1, 0).getType().isAir()) {
            return false;
        }

        // Check if there are no dangerous blocks nearby
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Material type = loc.getBlock().getRelative(x, y, z).getType();
                    if (type == Material.LAVA || type == Material.FIRE || type == Material.CAMPFIRE) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}