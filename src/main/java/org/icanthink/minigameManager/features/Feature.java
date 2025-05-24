package org.icanthink.minigameManager.features;

import org.icanthink.minigameManager.Minigame;

/**
 * Base class for all minigame features.
 * Features provide modular functionality that can be added to minigames.
 */
public abstract class Feature {
    protected final Minigame minigame;

    public Feature(Minigame minigame) {
        this.minigame = minigame;
    }

    /**
     * Get the minigame this feature is attached to.
     *
     * @return The minigame instance
     */
    public Minigame getMinigame() {
        return minigame;
    }
}