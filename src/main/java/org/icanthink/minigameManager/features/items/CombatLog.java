package org.icanthink.minigameManager.features.items;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A custom item that saves players from death by growing a tree and teleporting them to safety.
 */
public class CombatLog extends CustomItem implements Listener {
    private static final String NAME = ChatColor.DARK_GREEN + "Combat Log";
    private static final Material MATERIAL = Material.OAK_LOG;
    private static final List<String> LORE = Arrays.asList(
        ChatColor.GRAY + "When you're about to die,",
        ChatColor.GRAY + "grows a tree and saves you!"
    );
    private static final Random RANDOM = new Random();

    public CombatLog(Minigame minigame) {
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

    @EventHandler
    public boolean onRightClick(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && isInstance(item)) {
            event.setCancelled(true);
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // Check if player is in the minigame
        if (!minigame.getPlayers().contains(player)) return;

        // Check if player has the combat log in either hand
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (!isInstance(mainHand) && !isInstance(offHand)) return;

        // Check if the damage would kill the player
        if (event.getFinalDamage() < player.getHealth()) return;

        // Cancel the damage event immediately
        event.setCancelled(true);

        // Get the player's location before teleporting
        Location originalLoc = player.getLocation().clone();

        // Teleport player to y=255
        Location safeLoc = player.getLocation().clone();
        safeLoc.setY(255);
        player.teleport(safeLoc);

        // Add slow falling effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 1200, 0)); // 60 seconds

        // Grow a tree at the original location
        new BukkitRunnable() {
            @Override
            public void run() {
                // Ensure the block below is solid for tree growth
                Location treeLoc = originalLoc.clone();
                if (!treeLoc.getBlock().getRelative(0, -1, 0).getType().isSolid()) {
                    treeLoc.getBlock().getRelative(0, -1, 0).setType(Material.DIRT);
                }

                // Try to grow a tree
                treeLoc.getWorld().generateTree(treeLoc, RANDOM.nextBoolean() ?
                    org.bukkit.TreeType.TREE : org.bukkit.TreeType.BIG_TREE);
            }
        }.runTaskLater(MinigameManager.plugin, 1L);

        // Send messages
        player.sendMessage(ChatColor.GREEN + "Your combat log saved you!");
        player.sendMessage(ChatColor.YELLOW + "A tree grew where you were!");

        // Remove the combat log from the player's inventory
        if (isInstance(mainHand)) {
            player.getInventory().setItemInMainHand(null);
        } else {
            player.getInventory().setItemInOffHand(null);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;

        ItemStack item = event.getItem();
        if (item != null && isInstance(item)) {
            event.setCancelled(true);
        }
    }
}