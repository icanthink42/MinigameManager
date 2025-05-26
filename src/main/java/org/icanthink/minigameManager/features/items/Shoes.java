package org.icanthink.minigameManager.features.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.block.Action;
import org.bukkit.attribute.Attribute;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Shoes extends CustomItem implements Listener {
    private static final Map<UUID, BukkitRunnable> speedTasks = new HashMap<>();
    private static final Map<UUID, Boolean> isBlocking = new HashMap<>();

    public Shoes(Minigame minigame) {
        super(minigame);
        MinigameManager.plugin.getServer().getPluginManager().registerEvents(this, MinigameManager.plugin);
        setMetaCustomizer(meta -> {
            meta.setUnbreakable(true);
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        });
    }

    @Override
    protected String getName() {
        return "§bPretty Good Quality Shoes";
    }

    @Override
    protected Material getMaterial() {
        return Material.LEATHER_BOOTS;
    }

    @Override
    protected List<String> getLore() {
        return Arrays.asList(
            "§7Made in China"
        );
    }

    private void startSpeedTask(Player player) {
        // Cancel any existing task for this player
        stopSpeedTask(player);

        // Create new task
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                // Check if player still has shoes equipped
                ItemStack boots = player.getInventory().getBoots();
                if (boots == null || !isInstance(boots)) {
                    stopSpeedTask(player);
                    return;
                }
                // Reapply speed effect and step height
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 39, false, false));
                player.getAttribute(Attribute.STEP_HEIGHT).setBaseValue(2.0);

                // Check if player is blocking with a shield
                isBlocking.put(player.getUniqueId(), player.isBlocking());

                // If sneaking or blocking, give additional speed to counteract slowdown
                if (player.isSneaking() || isBlocking.getOrDefault(player.getUniqueId(), false)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 99, false, false));
                }
            }
        };

        // Run task more frequently to catch shield changes
        task.runTaskTimer(MinigameManager.plugin, 0L, 1L);
        speedTasks.put(player.getUniqueId(), task);
    }

    private void stopSpeedTask(Player player) {
        BukkitRunnable task = speedTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
            player.getAttribute(Attribute.STEP_HEIGHT).setBaseValue(0.6);
        }
        isBlocking.remove(player.getUniqueId());
    }

    @EventHandler
    public void onEquip(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!isInstance(event.getCurrentItem())) return;

        Player player = (Player) event.getWhoClicked();
        startSpeedTask(player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        if (!isInstance(event.getItem())) return;

        // Check after a tick to ensure boots are equipped
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = event.getPlayer();
                ItemStack boots = player.getInventory().getBoots();
                if (boots != null && isInstance(boots)) {
                    startSpeedTask(player);
                }
            }
        }.runTaskLater(MinigameManager.plugin, 1L);
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack boots = player.getInventory().getBoots();
        if (boots != null && isInstance(boots)) {
            startSpeedTask(player); // Update speed when switching items
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stopSpeedTask(event.getPlayer());
    }

    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent event) {
        if (isInstance(event.getBrokenItem())) {
            stopSpeedTask(event.getPlayer());
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        ItemStack boots = player.getInventory().getBoots();
        if (boots != null && isInstance(boots)) {
            startSpeedTask(player); // This will update the speed effect based on sneak state
        }
    }
}