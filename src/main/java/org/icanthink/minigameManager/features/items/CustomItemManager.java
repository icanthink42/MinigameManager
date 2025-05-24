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
    private final List<CustomItem> customItems;

    public CustomItemManager(Minigame minigame) {
        super(minigame);
        this.customItems = new ArrayList<>();
        MinigameManager.plugin.getServer().getPluginManager().registerEvents(this, MinigameManager.plugin);
    }

    /**
     * Register a custom item class with this manager.
     * Creates a new instance of the item and registers it.
     *
     * @param itemClass The class of the custom item to register
     * @return The created and registered custom item instance
     */
    public <T extends CustomItem> T registerItem(Class<T> itemClass) {
        try {
            T item = itemClass.getDeclaredConstructor(Minigame.class).newInstance(minigame);
            customItems.add(item);
            return item;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create custom item instance", e);
        }
    }

    /**
     * Unregister a custom item from this manager.
     *
     * @param item The custom item to unregister
     */
    public void unregisterItem(CustomItem item) {
        customItems.remove(item);
    }

    /**
     * Get all registered custom items.
     *
     * @return List of registered custom items
     */
    public List<CustomItem> getCustomItems() {
        return customItems;
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
        } catch (Exception e) {
            throw new RuntimeException("Failed to create custom item instance", e);
        }
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

    /**
     * Find a custom item by its ItemStack.
     *
     * @param itemStack The ItemStack to find
     * @return The matching CustomItem, or null if not found
     */
    private CustomItem findCustomItem(ItemStack itemStack) {
        if (itemStack == null) return null;

        for (CustomItem item : customItems) {
            if (item.isInstance(itemStack)) {
                return item;
            }
        }
        return null;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!minigame.isRunning()) return;
        if (!minigame.getPlayers().contains(event.getPlayer())) return;

        CustomItem customItem = findCustomItem(event.getItem());
        if (customItem != null) {
            boolean shouldCancel = false;

            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                shouldCancel = customItem.onLeftClick(event);
            } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                shouldCancel = customItem.onRightClick(event);
            }

            if (shouldCancel) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!minigame.isRunning()) return;
        if (!minigame.getPlayers().contains(event.getPlayer())) return;

        CustomItem customItem = findCustomItem(event.getItemInHand());
        if (customItem != null) {
            boolean shouldCancel = customItem.onPlace(event);
            if (shouldCancel) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!minigame.isRunning()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!minigame.getPlayers().contains(event.getWhoClicked())) return;

        // Check if the clicked item is a custom item
        CustomItem clickedItem = findCustomItem(event.getCurrentItem());
        if (clickedItem != null) {
            boolean shouldCancel = clickedItem.onLeaveInventory(event);
            if (shouldCancel) {
                event.setCancelled(true);
            }
        }

        // Check if the cursor item is a custom item
        CustomItem cursorItem = findCustomItem(event.getCursor());
        if (cursorItem != null) {
            boolean shouldCancel = cursorItem.onEnterInventory(event);
            if (shouldCancel) {
                event.setCancelled(true);
            }
        }
    }
}