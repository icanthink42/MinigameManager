package org.icanthink.minigameManager.features;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.inventory.ItemStack;
import org.icanthink.minigameManager.Minigame;

/**
 * Feature that makes copper ore drop iron ingots when broken and makes furnaces smelt items instantly.
 */
public class CopperDropIron extends Feature implements Listener {

    public CopperDropIron(Minigame minigame) {
        super(minigame);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Check if the player is in this minigame and the minigame is running
        if (!minigame.isRunning() || !minigame.getPlayers().contains(event.getPlayer())) {
            return;
        }

        // Check if the broken block is copper ore
        if (event.getBlock().getType() == Material.COPPER_ORE ||
            event.getBlock().getType() == Material.DEEPSLATE_COPPER_ORE) {
            // Cancel the default drops
            event.setDropItems(false);

            // Drop an iron ingot instead
            event.getBlock().getWorld().dropItemNaturally(
                event.getBlock().getLocation(),
                new ItemStack(Material.IRON_INGOT)
            );
        }
    }

    @EventHandler
    public void onFurnaceStartSmelt(FurnaceStartSmeltEvent event) {
        // Check if the minigame is running
        if (!minigame.isRunning()) {
            return;
        }

        // Make the smelting instant by setting the cooking time to 0
        event.setTotalCookTime(0);
    }
}