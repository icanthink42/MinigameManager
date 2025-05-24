package org.icanthink.minigameManager.features.mobs;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.icanthink.minigameManager.Minigame;
import org.icanthink.minigameManager.MinigameManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Base class for custom mobs in minigames.
 * Provides common functionality for creating and managing custom mobs.
 */
public abstract class CustomMob {
    protected final Minigame minigame;
    protected final String name;
    public final EntityType entityType;
    protected final UUID mobId;
    protected boolean showBossBar;
    protected BossBar bossBar;
    protected final Set<Player> nearbyPlayers;
    private static final NamespacedKey MOB_ID_KEY = new NamespacedKey(MinigameManager.plugin, "custom_mob_id");
    private static final double BOSS_BAR_RANGE = 50.0; // Range in blocks to show boss bar

    /**
     * Create a new custom mob.
     *
     * @param minigame The minigame this mob belongs to
     */
    public CustomMob(Minigame minigame) {
        this.minigame = minigame;
        this.name = getName();
        this.entityType = getEntityType();
        this.mobId = UUID.randomUUID();
        this.showBossBar = shouldShowBossBar();
        this.nearbyPlayers = new HashSet<>();
        if (this.showBossBar) {
            this.bossBar = MinigameManager.plugin.getServer().createBossBar(
                name,
                getBossBarColor(),
                getBossBarStyle()
            );
        }
    }

    /**
     * Get the display name of this custom mob.
     *
     * @return The mob's display name
     */
    protected abstract String getName();

    /**
     * Get the entity type of this custom mob.
     *
     * @return The mob's entity type
     */
    protected abstract EntityType getEntityType();

    /**
     * Whether this mob should show a boss bar.
     * Override this method to enable boss bar for specific mobs.
     *
     * @return true if the mob should show a boss bar
     */
    protected boolean shouldShowBossBar() {
        return false;
    }

    /**
     * Get the color of the boss bar.
     * Override this method to customize the boss bar color.
     *
     * @return The boss bar color
     */
    protected BarColor getBossBarColor() {
        return BarColor.RED;
    }

    /**
     * Get the style of the boss bar.
     * Override this method to customize the boss bar style.
     *
     * @return The boss bar style
     */
    protected BarStyle getBossBarStyle() {
        return BarStyle.SOLID;
    }

    /**
     * Create and spawn a new instance of this custom mob.
     *
     * @param entity The entity to customize
     * @return The customized entity
     */
    public LivingEntity createMob(LivingEntity entity) {
        if (shouldShowNametag()) {
            entity.setCustomName(name);
            entity.setCustomNameVisible(true);
        } else {
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
        }
        entity.getPersistentDataContainer().set(MOB_ID_KEY, PersistentDataType.STRING, mobId.toString());
        customizeMob(entity);
        customizeAI(entity);
        return entity;
    }

    /**
     * Customize the mob's properties. Override this method to add custom behavior.
     *
     * @param entity The entity to customize
     */
    protected abstract void customizeMob(LivingEntity entity);

    /**
     * Customize the mob's AI behavior. Override this method to modify AI settings.
     *
     * @param entity The entity to customize AI for
     */
    protected void customizeAI(LivingEntity entity) {
        if (!(entity instanceof Mob mob)) return;

        // Set AI goals and targets
        setAIGoals(mob);

        // Only modify attributes if they are explicitly overridden
        double customSpeed = getMovementSpeed();
        if (customSpeed != -1) {
            AttributeInstance speed = entity.getAttribute(Attribute.valueOf("GENERIC_MOVEMENT_SPEED"));
            if (speed != null) {
                speed.setBaseValue(customSpeed);
            }
        }

        double customDamage = getAttackDamage();
        if (customDamage != -1) {
            AttributeInstance damage = entity.getAttribute(Attribute.valueOf("GENERIC_ATTACK_DAMAGE"));
            if (damage != null) {
                damage.setBaseValue(customDamage);
            }
        }

        double customFollowRange = getFollowRange();
        if (customFollowRange != -1) {
            AttributeInstance followRange = entity.getAttribute(Attribute.valueOf("GENERIC_FOLLOW_RANGE"));
            if (followRange != null) {
                followRange.setBaseValue(customFollowRange);
            }
        }

        double customKnockbackResistance = getKnockbackResistance();
        if (customKnockbackResistance != -1) {
            AttributeInstance knockbackResistance = entity.getAttribute(Attribute.valueOf("GENERIC_KNOCKBACK_RESISTANCE"));
            if (knockbackResistance != null) {
                knockbackResistance.setBaseValue(customKnockbackResistance);
            }
        }

        double customArmor = getArmor();
        if (customArmor != -1) {
            AttributeInstance armor = entity.getAttribute(Attribute.valueOf("GENERIC_ARMOR"));
            if (armor != null) {
                armor.setBaseValue(customArmor);
            }
        }

        double customArmorToughness = getArmorToughness();
        if (customArmorToughness != -1) {
            AttributeInstance armorToughness = entity.getAttribute(Attribute.valueOf("GENERIC_ARMOR_TOUGHNESS"));
            if (armorToughness != null) {
                armorToughness.setBaseValue(customArmorToughness);
            }
        }
    }

    /**
     * Set the AI goals for this mob. Override this method to customize AI behavior.
     *
     * @param mob The mob to set AI goals for
     */
    protected void setAIGoals(Mob mob) {
        // Override this method to set custom AI goals
    }

    /**
     * Get the base movement speed for this mob.
     * Override this method to customize movement speed.
     * Return -1 to keep the vanilla default.
     *
     * @return The base movement speed, or -1 to keep vanilla default
     */
    protected double getMovementSpeed() {
        return -1; // Keep vanilla default
    }

    /**
     * Get the base attack damage for this mob.
     * Override this method to customize attack damage.
     * Return -1 to keep the vanilla default.
     *
     * @return The base attack damage, or -1 to keep vanilla default
     */
    protected double getAttackDamage() {
        return -1; // Keep vanilla default
    }

    /**
     * Get the follow range for this mob.
     * Override this method to customize follow range.
     * Return -1 to keep the vanilla default.
     *
     * @return The follow range in blocks, or -1 to keep vanilla default
     */
    protected double getFollowRange() {
        return -1; // Keep vanilla default
    }

    /**
     * Get the knockback resistance for this mob.
     * Override this method to customize knockback resistance.
     * Return -1 to keep the vanilla default.
     *
     * @return The knockback resistance (0.0 to 1.0), or -1 to keep vanilla default
     */
    protected double getKnockbackResistance() {
        return -1; // Keep vanilla default
    }

    /**
     * Get the armor value for this mob.
     * Override this method to customize armor.
     * Return -1 to keep the vanilla default.
     *
     * @return The armor value, or -1 to keep vanilla default
     */
    protected double getArmor() {
        return -1; // Keep vanilla default
    }

    /**
     * Get the armor toughness for this mob.
     * Override this method to customize armor toughness.
     * Return -1 to keep the vanilla default.
     *
     * @return The armor toughness, or -1 to keep vanilla default
     */
    protected double getArmorToughness() {
        return -1; // Keep vanilla default
    }

    /**
     * Update the boss bar for nearby players.
     * Call this method periodically to update the boss bar visibility and progress.
     *
     * @param entity The entity to update the boss bar for
     */
    public void updateBossBar(LivingEntity entity) {
        if (!showBossBar || bossBar == null) return;

        // Update boss bar progress
        double healthPercent = entity.getHealth() / entity.getMaxHealth();
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, healthPercent)));

        // Update nearby players
        Set<Player> currentNearby = new HashSet<>();
        for (Player player : entity.getWorld().getPlayers()) {
            if (player.getLocation().distance(entity.getLocation()) <= BOSS_BAR_RANGE) {
                currentNearby.add(player);
                if (!nearbyPlayers.contains(player)) {
                    bossBar.addPlayer(player);
                }
            }
        }

        // Remove players that are no longer nearby
        for (Player player : nearbyPlayers) {
            if (!currentNearby.contains(player)) {
                bossBar.removePlayer(player);
            }
        }

        nearbyPlayers.clear();
        nearbyPlayers.addAll(currentNearby);
    }

    /**
     * Clean up the boss bar when the mob is removed.
     */
    public void cleanupBossBar() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        nearbyPlayers.clear();
    }

    /**
     * Get the unique ID of this custom mob.
     *
     * @return The mob's UUID
     */
    public UUID getMobId() {
        return mobId;
    }

    /**
     * Check if an entity is an instance of this custom mob.
     *
     * @param entity The entity to check
     * @return true if the entity is an instance of this custom mob
     */
    public boolean isInstance(Entity entity) {
        if (entity == null) return false;

        String storedId = entity.getPersistentDataContainer()
            .get(MOB_ID_KEY, PersistentDataType.STRING);

        return storedId != null && storedId.equals(mobId.toString());
    }

    /**
     * Called when this mob takes damage.
     * Override this method to implement custom damage behavior.
     *
     * @param event The damage event
     * @return true if the event should be cancelled
     */
    public boolean onDamage(EntityDamageEvent event) {
        return false;
    }

    /**
     * Called when this mob dies.
     * Override this method to implement custom death behavior.
     *
     * @param event The death event
     */
    public void onDeath(EntityDeathEvent event) {
        cleanupBossBar();
    }

    /**
     * Called when this mob targets another entity.
     * Override this method to implement custom targeting behavior.
     *
     * @param event The target event
     * @return true if the event should be cancelled
     */
    public boolean onTarget(EntityTargetEvent event) {
        return false;
    }

    /**
     * Whether this mob should show its nametag.
     * Override this method to control nametag visibility.
     *
     * @return true if the mob should show its nametag
     */
    protected boolean shouldShowNametag() {
        return true; // Show nametag by default
    }
}