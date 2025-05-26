package org.icanthink.minigameManager.features.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Base class for custom items in minigames.
 * Provides common functionality for creating and managing custom items.
 */
public abstract class CustomItem {
    protected final Minigame minigame;
    protected final String name;
    protected final Material material;
    protected final List<String> lore;
    private Consumer<ItemMeta> metaCustomizer;
    private static final NamespacedKey ITEM_ID_KEY = new NamespacedKey(MinigameManager.plugin, "custom_item_id");

    /**
     * Create a new custom item.
     *
     * @param minigame The minigame this item belongs to
     */
    public CustomItem(Minigame minigame) {
        this.minigame = minigame;
        this.name = getName();
        this.material = getMaterial();
        this.lore = getLore();
    }

    /**
     * Get the display name of this custom item.
     *
     * @return The item's display name
     */
    protected abstract String getName();

    /**
     * Get the material type of this custom item.
     *
     * @return The item's material
     */
    protected abstract Material getMaterial();

    /**
     * Get the lore of this custom item.
     *
     * @return The item's lore
     */
    protected abstract List<String> getLore();

    /**
     * Create an ItemStack for this custom item.
     *
     * @return The created ItemStack
     */
    public ItemStack createItem() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(ITEM_ID_KEY, PersistentDataType.STRING, getItemId().toString());
            if (metaCustomizer != null) {
                metaCustomizer.accept(meta);
            }
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Get the unique ID of this custom item.
     * This ID is deterministically generated from the class name,
     * ensuring all instances of the same item type share the same ID.
     *
     * @return The item's UUID
     */
    public UUID getItemId() {
        // Use the class name as a namespace to generate a deterministic UUID
        // This ensures all instances of the same item type have the same ID
        return UUID.nameUUIDFromBytes(getClass().getName().getBytes());
    }

    /**
     * Check if an ItemStack is an instance of this custom item.
     *
     * @param itemStack The ItemStack to check
     * @return true if the ItemStack is an instance of this custom item
     */
    public boolean isInstance(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return false;

        String storedId = itemStack.getItemMeta().getPersistentDataContainer()
            .get(ITEM_ID_KEY, PersistentDataType.STRING);

        return storedId != null && storedId.equals(getItemId().toString());
    }

    /**
     * Set a customizer for the ItemMeta, allowing additional options to be set.
     * @param customizer The lambda to customize the ItemMeta
     */
    public void setMetaCustomizer(Consumer<ItemMeta> customizer) {
        this.metaCustomizer = customizer;
    }
}