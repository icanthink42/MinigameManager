package org.icanthink.minigameManager.features.mobs;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;
import org.icanthink.minigameManager.features.Feature;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Feature that manages custom mobs in minigames.
 * Handles registration and event handling for custom mobs.
 */
public class CustomMobManager extends Feature implements Listener {
    private final List<CustomMob> customMobs;
    private static final NamespacedKey MOB_ID_KEY = new NamespacedKey(MinigameManager.plugin, "custom_mob_id");

    public CustomMobManager(Minigame minigame) {
        super(minigame);
        this.customMobs = new ArrayList<>();
        MinigameManager.plugin.getServer().getPluginManager().registerEvents(this, MinigameManager.plugin);
    }

    /**
     * Spawn a custom mob at the given location.
     *
     * @param location The location to spawn the mob at
     * @param mobClass The class of the custom mob to spawn
     * @return The created and registered custom mob instance
     */
    public <T extends CustomMob> T spawnMob(Location location, Class<T> mobClass) {
        try {
            // Get the entity type from a temporary instance
            T tempMob = mobClass.getDeclaredConstructor(Minigame.class).newInstance(minigame);
            EntityType entityType = tempMob.getEntityType();

            // Spawn the entity and create the mob
            LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, entityType);
            T mob = mobClass.getDeclaredConstructor(Minigame.class).newInstance(minigame);
            customMobs.add(mob);
            mob.createMob(entity);
            return mob;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create custom mob instance", e);
        }
    }

    /**
     * Spawn a custom mob at the given player.
     *
     * @param player The player to spawn the mob at
     * @param mobClass The class of the custom mob to spawn
     * @return The created and registered custom mob instance
     */
    public <T extends CustomMob> T spawnMob(Player player, Class<T> mobClass) {
        return spawnMob(player.getLocation(), mobClass);
    }

    /**
     * Unregister a custom mob from this manager.
     *
     * @param mob The custom mob to unregister
     */
    public void unregisterMob(CustomMob mob) {
        customMobs.remove(mob);
    }

    /**
     * Get all registered custom mobs.
     *
     * @return List of registered custom mobs
     */
    public List<CustomMob> getCustomMobs() {
        return customMobs;
    }

    /**
     * Find a custom mob by its entity.
     *
     * @param entity The entity to find
     * @return The matching CustomMob, or null if not found
     */
    private CustomMob findCustomMob(Entity entity) {
        if (entity == null) return null;

        String storedId = entity.getPersistentDataContainer()
            .get(MOB_ID_KEY, PersistentDataType.STRING);
        if (storedId == null) return null;

        UUID mobId = UUID.fromString(storedId);
        for (CustomMob mob : customMobs) {
            if (mob.getMobId().equals(mobId)) {
                return mob;
            }
        }
        return null;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        CustomMob mob = findCustomMob(event.getEntity());
        if (mob != null) {
            if (mob.onDamage(event)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        CustomMob mob = findCustomMob(event.getEntity());
        if (mob != null) {
            mob.onDeath(event);
            unregisterMob(mob);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        CustomMob mob = findCustomMob(event.getEntity());
        if (mob != null) {
            if (mob.onTarget(event)) {
                event.setCancelled(true);
            }
        }
    }
}