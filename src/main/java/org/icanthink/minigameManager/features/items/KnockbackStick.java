package org.icanthink.minigameManager.features.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;

import java.util.Arrays;
import java.util.List;

public class KnockbackStick extends CustomItem implements Listener {
    public KnockbackStick(Minigame minigame) {
        super(minigame);
        MinigameManager.plugin.getServer().getPluginManager().registerEvents(this, MinigameManager.plugin);
        setMetaCustomizer(meta -> meta.addEnchant(Enchantment.KNOCKBACK, 10, true));
    }

    @Override
    protected String getName() {
        return "§6Knockback Stick";
    }

    @Override
    protected Material getMaterial() {
        return Material.STICK;
    }

    @Override
    protected List<String> getLore() {
        return Arrays.asList(
            "§7A stick with Knockback X",
            "§7Only works on players!"
        );
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player)) return;
        Player damager = (Player) event.getDamager();

        if (!isInstance(damager.getInventory().getItemInMainHand())) return;
        event.setDamage(0);

        if (!(event.getEntity() instanceof Player)) {
            event.setCancelled(true);
        }

    }
}