package org.icanthink.minigameManager.features.items;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * A custom item that tracks players in the minigame.
 * Right-click to cycle through players.
 */
public class PlayerTracker extends CustomItem implements Listener {
    private static final String NAME = "§ePlayer Tracker";
    private static final Material MATERIAL = Material.COMPASS;
    private static final List<String> LORE = Arrays.asList(
        "§7Right-click to cycle through",
        "§7players in the minigame"
    );

    private int currentPlayerIndex = 0;
    private int taskId;
    private Location lastTargetLocation;
    private Player currentTarget;

    public PlayerTracker(Minigame minigame) {
        super(minigame);
        // Make the compass look enchanted
        setMetaCustomizer(meta -> {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });
        // Start updating compass target every tick
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MinigameManager.plugin, this::updateAllCompasses, 0L, 1L);
        // Register event listener
        MinigameManager.plugin.getServer().getPluginManager().registerEvents(this, MinigameManager.plugin);
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

    @Override
    public ItemStack createItem() {
        ItemStack item = super.createItem();
        updateCompassTarget(item);
        return item;
    }

    private void updateCompassTarget(ItemStack item) {
        if (currentTarget == null || !currentTarget.isOnline() || !minigame.getPlayers().contains(currentTarget)) {
            return;
        }

        CompassMeta meta = (CompassMeta) item.getItemMeta();
        if (meta != null) {
            // Create a location at the target's position but at world spawn height
            Location targetLoc = currentTarget.getLocation();
            Location compassLoc = new Location(targetLoc.getWorld(), targetLoc.getX(), 0, targetLoc.getZ());
            meta.setLodestone(compassLoc);
            meta.setLodestoneTracked(false);
            item.setItemMeta(meta);
        }
    }

    private void updateAllCompasses() {
        if (!minigame.isRunning()) return;
        if (currentTarget == null || !currentTarget.isOnline() || !minigame.getPlayers().contains(currentTarget)) {
            return;
        }

        Location currentLocation = currentTarget.getLocation();

        // Only update if target has moved more than 5 blocks
        if (lastTargetLocation == null ||
            currentLocation.distanceSquared(lastTargetLocation) > 25) {
            lastTargetLocation = currentLocation;
            for (Player player : minigame.getPlayers()) {
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && isInstance(item)) {
                        updateCompassTarget(item);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        if (!isInstance(event.getItem())) return;

        Player player = event.getPlayer();
        List<Player> players = new ArrayList<>(minigame.getPlayers());
        players.remove(player); // Don't track yourself

        if (players.isEmpty()) {
            player.sendMessage("§cNo other players to track!");
            return;
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        currentTarget = players.get(currentPlayerIndex);
        player.sendMessage("§eNow tracking: §f" + currentTarget.getName());
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);

        // Update compass immediately
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isInstance(item)) {
                updateCompassTarget(item);
            }
        }

        event.setCancelled(true);
    }
}