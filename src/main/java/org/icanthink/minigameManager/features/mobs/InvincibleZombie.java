package org.icanthink.minigameManager.features.mobs;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.icanthink.minigameManager.Minigame;

/**
 * A zombie that cannot take damage.
 */
public class InvincibleZombie extends CustomMob {
    public InvincibleZombie(Minigame minigame) {
        super(minigame);
    }

    @Override
    protected String getName() {
        return "§cInvincible Zombie";
    }

    @Override
    protected EntityType getEntityType() {
        return EntityType.ZOMBIE;
    }

    @Override
    protected void customizeMob(LivingEntity entity) {
        // No additional customization needed
    }

    @Override
    public boolean onDamage(EntityDamageEvent event) {
        return true;
    }

    @Override
    protected boolean shouldShowNametag() {
        return false;
    }
}