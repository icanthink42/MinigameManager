package org.icanthink.minigameManager.features.items;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A custom item that teleports players in the direction they're looking.
 */
public class TeleportRod extends CustomItem implements Listener {
    private static final String NAME = "§bTeleport Rod";
    private static final Material MATERIAL = Material.BLAZE_ROD;
    private static final List<String> LORE = Arrays.asList(
        "§7Right-click to teleport",
        "§7in the direction you're looking",
        "§7Cooldown: 5 seconds"
    );
    private static final long COOLDOWN_MS = 5000; // 5 seconds in milliseconds
    private final Map<UUID, Long> lastUsage = new HashMap<>();

    public TeleportRod(Minigame minigame) {
        super(minigame);
        MinigameManager.plugin.getServer().getPluginManager().registerEvents(this, MinigameManager.plugin);
        // Make the rod look enchanted
        setMetaCustomizer(meta -> {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });
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
    public boolean onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Check if player is on cooldown
        if (lastUsage.containsKey(playerId)) {
            long timeLeft = COOLDOWN_MS - (currentTime - lastUsage.get(playerId));
            if (timeLeft > 0) {
                player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(
                        ChatColor.RED + "You must wait " + String.format("%.1f", timeLeft / 1000.0) + " seconds before using the Teleport Rod again!"
                    )
                );
                return false;
            }
        }

        // Get the direction the player is looking
        Vector direction = player.getLocation().getDirection();

        // Teleport the player 10 blocks in that direction
        player.teleport(player.getLocation().add(direction.multiply(10)));

        // Play a sound effect
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        // Update last usage time
        lastUsage.put(playerId, currentTime);

        return true;
    }
}