package org.icanthink.minigameManager.features.items;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.icanthink.minigameManager.features.Feature;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Feature that manages custom items in minigames.
 * Handles registration and event handling for custom items.
 */
public class CustomItemManager extends Feature implements Listener {
    protected final List<CustomItem> customItems;

    public CustomItemManager(Minigame minigame) {
        super(minigame);
        this.customItems = new ArrayList<>();
        MinigameManager.plugin.getServer().getPluginManager().registerEvents(this, MinigameManager.plugin);
    }

    /**
     * Register a custom item with this manager.
     *
     * @param item The custom item to register
     */
    public void registerItem(CustomItem item) {
        customItems.add(item);
    }

    /**
     * Get all registered custom items.
     *
     * @return List of registered custom items
     */
    public List<CustomItem> getItems() {
        return customItems;
    }

    /**
     * Find a custom item instance that matches the given ItemStack.
     *
     * @param itemStack The ItemStack to check
     * @return The matching CustomItem, or null if none found
     */
    public CustomItem getCustomItem(ItemStack itemStack) {
        if (itemStack == null) return null;

        for (CustomItem item : customItems) {
            if (item.isInstance(itemStack)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Create an ItemStack for a custom item by its class.
     *
     * @param itemClass The class of the custom item
     * @return The created ItemStack, or null if the item class isn't registered
     */
    public ItemStack createItem(Class<? extends CustomItem> itemClass) {
        for (CustomItem item : customItems) {
            if (item.getClass() == itemClass) {
                return item.createItem();
            }
        }
        return null;
    }

    /**
     * Give a custom item to a player.
     * The item will be added to the player's inventory if there is space.
     *
     * @param player The player to give the item to
     * @param itemClass The class of the custom item to give
     * @return true if the item was given successfully, false if the player's inventory is full
     */
    public <T extends CustomItem> boolean giveItem(Player player, Class<T> itemClass) {
        if (!minigame.getPlayers().contains(player)) {
            return false;
        }

        try {
            T item = itemClass.getDeclaredConstructor(Minigame.class).newInstance(minigame);
            customItems.add(item);
            ItemStack itemStack = item.createItem();
            return player.getInventory().addItem(itemStack).isEmpty();
        } catch (Exception e) { throw new RuntimeException("Failed to create custom item instance", e); }
    }

    /**
     * Give a custom item to all players in the minigame.
     *
     * @param itemClass The class of the custom item to give
     */
    public <T extends CustomItem> void giveItemToAll(Class<T> itemClass) {
        for (Player player : minigame.getPlayers()) {
            giveItem(player, itemClass);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Skip if player not in minigame
        if (!minigame.getPlayers().contains(event.getPlayer())) return;

        // Each item now handles its own events through their own Listener implementations
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Skip if player not in minigame
        if (!minigame.getPlayers().contains(event.getPlayer())) return;

        // Each item now handles its own events through their own Listener implementations
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Skip if not a player or player not in minigame
        if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player)) return;
        if (!minigame.getPlayers().contains((org.bukkit.entity.Player) event.getWhoClicked())) return;

        // Each item now handles its own events through their own Listener implementations
    }
}