package org.icanthink.minigameManager.features.mobs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;

/**
 * A special dog that bonds with the nearest player.
 * If the dog dies, its owner dies too.
 */
public class SpecialDog extends CustomMob implements Listener {
    private Player owner;
    private String customName;
    private boolean awaitingName = false;

    public SpecialDog(Minigame minigame) {
        super(minigame);
        MinigameManager.plugin.getServer().getPluginManager().registerEvents(this, MinigameManager.plugin);
    }

    @Override
    protected String getName() {
        return customName != null ? customName : "Special Dog";
    }

    @Override
    protected EntityType getEntityType() {
        return EntityType.WOLF;
    }

    @Override
    protected void customizeMob(LivingEntity entity) {
        if (!(entity instanceof Wolf wolf)) return;

        // Find the nearest player
        Player nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player player : entity.getWorld().getPlayers()) {
            if (!minigame.getPlayers().contains(player)) continue;

            double distance = player.getLocation().distance(entity.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestPlayer = player;
            }
        }

        if (nearestPlayer != null) {
            owner = nearestPlayer;
            wolf.setTamed(true);
            wolf.setOwner(owner);
            wolf.setSitting(false); // Prevent initial sitting

            // Remove the sit goal from the wolf's AI
            ((Wolf) entity).setAware(true);

            // Prompt the player to name their dog
            owner.sendMessage(ChatColor.GREEN + "You've been chosen by a special dog! Type a name for your new companion:");
            awaitingName = true;

            // Schedule a task to check for player chat
            Bukkit.getScheduler().runTaskLater(MinigameManager.plugin, () -> {
                if (customName == null) {
                    // If no name was set after 30 seconds, use a default name
                    setCustomName(owner.getName() + "'s Dog");
                    awaitingName = false;
                }
            }, 600L); // 30 seconds
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (awaitingName && event.getPlayer() == owner) {
            event.setCancelled(true); // Don't broadcast the name message
            String name = event.getMessage().trim();

            // Schedule the name change on the main thread
            Bukkit.getScheduler().runTask(MinigameManager.plugin, () -> {
                setCustomName(name);
                awaitingName = false;
            });
        }
    }

    public void setCustomName(String name) {
        this.customName = ChatColor.GOLD + name;
        if (owner != null) {
            owner.sendMessage(ChatColor.GREEN + "Your dog is now named: " + customName);
            // Find the wolf entity and update its name
            for (LivingEntity entity : owner.getWorld().getLivingEntities()) {
                if (entity instanceof Wolf && isInstance(entity)) {
                    entity.setCustomName(customName);
                    entity.setCustomNameVisible(true);
                    break;
                }
            }
        }
    }

    @Override
    public void onDeath(EntityDeathEvent event) {
        super.onDeath(event);

        if (owner != null) {
            owner.sendMessage(ChatColor.RED + "Your special dog has died!");
            // Deal massive damage to the owner
            owner.damage(100.0); // 50 hearts of damage
        }
    }

    @Override
    protected boolean shouldShowBossBar() {
        return false;
    }

    @Override
    protected boolean shouldShowNametag() {
        return true;
    }

    public Player getOwner() {
        return owner;
    }

    @EventHandler
    public void onWolfSit(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Wolf wolf && isInstance(wolf)) {
            event.setCancelled(true);
            wolf.setSitting(false);
        }
    }
}