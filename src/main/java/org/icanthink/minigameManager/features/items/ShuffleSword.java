package org.icanthink.minigameManager.features.items;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A custom item: Shuffle Sword. Hitting an entity with it shuffles your inventory.
 */
public class ShuffleSword extends CustomItem implements Listener {
    private static final String NAME = ChatColor.LIGHT_PURPLE + "Shuffle Sword";
    private static final Material MATERIAL = Material.NETHERITE_SWORD;
    private static final List<String> LORE = Arrays.asList(
            ChatColor.GRAY + "Hit something to shuffle your inventory!"
    );
    private static final Random RANDOM = new Random();

    public ShuffleSword(Minigame minigame) {
        super(minigame);
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

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isInstance(item)) return;
        // Shuffle the player's inventory
        List<ItemStack> contents = Arrays.asList(player.getInventory().getContents());
        Collections.shuffle(contents, RANDOM);
        player.getInventory().setContents(contents.toArray(new ItemStack[0]));
    }
}