package org.icanthink.minigameManager.features.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;

import java.util.Arrays;
import java.util.List;

public class CursedPumpkinItem extends CustomItem implements Listener {
    public CursedPumpkinItem(Minigame minigame) {
        super(minigame);
        MinigameManager.plugin.getServer().getPluginManager().registerEvents(this, MinigameManager.plugin);
    }

    @Override
    protected String getName() {
        return "§6Cursed Pumpkin";
    }

    @Override
    protected Material getMaterial() {
        return Material.CARVED_PUMPKIN;
    }

    @Override
    protected List<String> getLore() {
        return Arrays.asList(
            "§7Right-click on a player",
            "§7to curse them with a",
            "§7binding pumpkin head!"
        );
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;

        ItemStack item = event.getItem();
        if (item == null || !isInstance(item)) return;

        // Cancel the event to prevent pumpkin placement
        event.setCancelled(true);

        Player player = event.getPlayer();
        player.sendMessage("§6Looking for a target...");

        // Get the player the user is looking at within 5 blocks
        Player targetPlayer = null;
        double closestDistance = 5.0;

        for (Player p : player.getWorld().getPlayers()) {
            if (p != player) {
                if (player.hasLineOfSight(p)) {
                    double distance = player.getLocation().distance(p.getLocation());
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        targetPlayer = p;
                    }
                }
            }
        }

        if (targetPlayer != null) {
            // Create cursed pumpkin
            ItemStack cursedPumpkin = new ItemStack(Material.CARVED_PUMPKIN);
            ItemMeta meta = cursedPumpkin.getItemMeta();
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            cursedPumpkin.setItemMeta(meta);

            // Set it on the target's head
            targetPlayer.getInventory().setHelmet(cursedPumpkin);

            // Send messages
            player.sendMessage("§6You cursed " + targetPlayer.getName() + " with a binding pumpkin!");
            targetPlayer.sendMessage("§cYou've been cursed with a binding pumpkin!");

            // Remove one pumpkin from the player's hand
            ItemStack handItem = player.getInventory().getItemInMainHand();
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            player.sendMessage("§cNo target found within range!");
        }
    }
}