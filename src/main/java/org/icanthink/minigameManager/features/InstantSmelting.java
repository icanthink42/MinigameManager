package org.icanthink.minigameManager.features;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;
import org.bukkit.Material;

/**
 * Feature that makes furnaces smelt items instantly.
 */
public class InstantSmelting extends Feature implements Listener {

    public InstantSmelting(Minigame minigame) {
        super(minigame);
        // Register this feature as a listener
        Bukkit.getPluginManager().registerEvents(this, MinigameManager.plugin);
    }

    @EventHandler
    public void onFurnaceStartSmelt(FurnaceStartSmeltEvent event) {
        // Check if the minigame is running
        if (!minigame.isRunning()) {
            return;
        }


        event.setTotalCookTime(0);
    }

    private int getBurnTime(Material fuel) {
        switch (fuel) {
            case LAVA_BUCKET:
                return 20000; // 100 items
            case COAL_BLOCK:
                return 16000; // 80 items
            case BLAZE_ROD:
                return 2400;  // 12 items
            case COAL:
                return 1600;  // 8 items
            case CHARCOAL:
                return 1600;  // 8 items
            case OAK_LOG:
            case BIRCH_LOG:
            case SPRUCE_LOG:
            case JUNGLE_LOG:
            case ACACIA_LOG:
            case DARK_OAK_LOG:
            case MANGROVE_LOG:
            case CHERRY_LOG:
                return 300;   // 1.5 items
            case OAK_PLANKS:
            case BIRCH_PLANKS:
            case SPRUCE_PLANKS:
            case JUNGLE_PLANKS:
            case ACACIA_PLANKS:
            case DARK_OAK_PLANKS:
            case MANGROVE_PLANKS:
            case CHERRY_PLANKS:
                return 300;   // 1.5 items
            case OAK_FENCE:
            case BIRCH_FENCE:
            case SPRUCE_FENCE:
            case JUNGLE_FENCE:
            case ACACIA_FENCE:
            case DARK_OAK_FENCE:
            case MANGROVE_FENCE:
            case CHERRY_FENCE:
            case OAK_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case SPRUCE_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case ACACIA_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case MANGROVE_FENCE_GATE:
            case CHERRY_FENCE_GATE:
            case OAK_DOOR:
            case BIRCH_DOOR:
            case SPRUCE_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
            case MANGROVE_DOOR:
            case CHERRY_DOOR:
                return 300;   // 1.5 items
            case OAK_BOAT:
            case BIRCH_BOAT:
            case SPRUCE_BOAT:
            case JUNGLE_BOAT:
            case ACACIA_BOAT:
            case DARK_OAK_BOAT:
            case MANGROVE_BOAT:
            case CHERRY_BOAT:
                return 400;   // 2 items
            case BOWL:
            case OAK_SAPLING:
            case BIRCH_SAPLING:
            case SPRUCE_SAPLING:
            case JUNGLE_SAPLING:
            case ACACIA_SAPLING:
            case DARK_OAK_SAPLING:
            case MANGROVE_PROPAGULE:
            case CHERRY_SAPLING:
            case STICK:
                return 100;   // 0.5 items
            default:
                return 0;
        }
    }
}