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
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlyingShield extends CustomItem implements Listener {
    private static final Map<UUID, BukkitRunnable> levitationTasks = new HashMap<>();
    private static final Map<UUID, Boolean> wasBlocking = new HashMap<>();
    private static final Map<UUID, Long> usedLevitationTime = new HashMap<>();
    private static final long MAX_LEVITATION_TIME = 10000; // 10 seconds in milliseconds

    public FlyingShield(Minigame minigame) {
        super(minigame);
        MinigameManager.plugin.getServer().getPluginManager().registerEvents(this, MinigameManager.plugin);
        setMetaCustomizer(meta -> {
            meta.setUnbreakable(true);
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });
    }

    @Override
    protected String getName() {
        return "§bFlying Shield";
    }

    @Override
    protected Material getMaterial() {
        return Material.SHIELD;
    }

    @Override
    protected List<String> getLore() {
        return Arrays.asList(
            "§7A shield made of helium",
            "§7Somehow still blocks swords and arrows"
        );
    }

    private void startLevitationTask(Player player) {
        // Cancel any existing task for this player
        stopLevitationTask(player);

        // Initialize used time if not set
        if (!usedLevitationTime.containsKey(player.getUniqueId())) {
            usedLevitationTime.put(player.getUniqueId(), 0L);
        }

        // Create new task
        BukkitRunnable task = new BukkitRunnable() {
            private long lastTickTime = System.currentTimeMillis();

            @Override
            public void run() {
                // Check if player still has shield
                ItemStack shield = player.getInventory().getItemInMainHand();
                ItemStack offhand = player.getInventory().getItemInOffHand();
                if (!isInstance(shield) && !isInstance(offhand)) {
                    stopLevitationTask(player);
                    return;
                }

                boolean isBlocking = player.isBlocking();
                boolean wasBlockingBefore = wasBlocking.getOrDefault(player.getUniqueId(), false);

                // Handle blocking state changes
                if (!isBlocking && wasBlockingBefore) {
                    // Stopped blocking, remove effects
                    player.removePotionEffect(PotionEffectType.LEVITATION);
                    player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                }

                // Update time tracking
                long currentTime = System.currentTimeMillis();
                if (isBlocking) {
                    // Add time since last tick if blocking
                    long deltaTime = currentTime - lastTickTime;
                    long totalUsed = usedLevitationTime.get(player.getUniqueId()) + deltaTime;
                    usedLevitationTime.put(player.getUniqueId(), totalUsed);

                    // Apply effects based on time used
                    if (totalUsed < MAX_LEVITATION_TIME) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 5, 2, false, false));

                        // Show remaining time
                        long timeLeft = MAX_LEVITATION_TIME - totalUsed;
                        double secondsLeft = Math.ceil(timeLeft / 1000.0);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(String.format("§eLevitation: %.0f seconds", secondsLeft)));
                    } else {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 5, 0, false, false));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent("§cLevitation expired!"));
                    }
                }

                // Update states for next tick
                lastTickTime = currentTime;
                wasBlocking.put(player.getUniqueId(), isBlocking);
            }
        };

        // Run task every tick to maintain smooth flight
        task.runTaskTimer(MinigameManager.plugin, 0L, 1L);
        levitationTasks.put(player.getUniqueId(), task);
        wasBlocking.put(player.getUniqueId(), false);
    }

    private void stopLevitationTask(Player player) {
        BukkitRunnable task = levitationTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
            player.removePotionEffect(PotionEffectType.LEVITATION);
            player.removePotionEffect(PotionEffectType.SLOW_FALLING);
        }
        wasBlocking.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // Check if player has landed (block below feet is solid)
        if (event.getTo().getBlock().getRelative(0, -1, 0).getType().isSolid()) {
            // Reset levitation time when landing
            usedLevitationTime.put(player.getUniqueId(), 0L);
        }
    }

    @EventHandler
    public void onEquip(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!isInstance(event.getCurrentItem())) return;

        Player player = (Player) event.getWhoClicked();
        startLevitationTask(player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        if (!isInstance(event.getItem())) return;

        Player player = event.getPlayer();
        startLevitationTask(player);
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        ItemStack offhandItem = player.getInventory().getItemInOffHand();

        // Start task if either hand has the shield
        if ((newItem != null && isInstance(newItem)) || isInstance(offhandItem)) {
            startLevitationTask(player);
        } else {
            // Only stop if neither hand has the shield
            ItemStack currentItem = player.getInventory().getItem(event.getPreviousSlot());
            if (!isInstance(offhandItem) && (currentItem == null || !isInstance(currentItem))) {
                stopLevitationTask(player);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stopLevitationTask(event.getPlayer());
        usedLevitationTime.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent event) {
        if (isInstance(event.getBrokenItem())) {
            stopLevitationTask(event.getPlayer());
        }
    }
}