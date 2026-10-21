package com.davibogo.faithfulfangs.wolf;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.wolf.Wolf;

/**
 * Attribute upgrades (Phase 4). The levels are saved on the wolf ({@link WolfData}), so they stay
 * through respawn and when the bed is broken; the attribute modifiers follow them and are saved
 * with the wolf too (so its extra health is there as soon as it loads).
 */
public final class WolfUpgrades {
    public enum Result {
        UPGRADED, MAX_LEVEL, MISSING_BONES, NOT_ALLOWED
    }

    private WolfUpgrades() {
    }

    public static int level(Wolf wolf, WolfUpgrade upgrade) {
        return Math.clamp(WolfData.getUpgradeLevel(wolf, upgrade), 0, WolfUpgrade.MAX_LEVEL);
    }

    /** Puts the attribute modifiers in line with the saved levels (on load and after each upgrade). */
    public static void applyModifiers(Wolf wolf) {
        for (WolfUpgrade upgrade : WolfUpgrade.values()) {
            if (upgrade.attribute() == null) {
                continue;
            }
            AttributeInstance instance = wolf.getAttribute(upgrade.attribute());
            if (instance == null) {
                continue;
            }
            int level = level(wolf, upgrade);
            if (level > 0) {
                instance.addOrReplacePermanentModifier(upgrade.modifier(level));
            } else {
                instance.removeModifier(upgrade.modifierId());
            }
        }
    }

    /**
     * Buys the next level of {@code upgrade} for the player's wolf, paying with special bones.
     * The new hearts of a Health level come filled.
     */
    public static Result upgrade(ServerPlayer player, Wolf wolf, WolfUpgrade upgrade) {
        if (!wolf.isAlive() || !wolf.isTame() || !wolf.isOwnedBy(player) || !(wolf.level() instanceof ServerLevel level)) {
            return Result.NOT_ALLOWED;
        }
        int current = level(wolf, upgrade);
        if (current >= WolfUpgrade.MAX_LEVEL) {
            return Result.MAX_LEVEL;
        }
        WolfUpgrade.Cost cost = WolfUpgrade.cost(current + 1);
        if (!cost.canPay(player)) {
            return Result.MISSING_BONES;
        }
        cost.pay(player);
        WolfData.setUpgradeLevel(wolf, upgrade, current + 1);
        applyModifiers(wolf);
        if (upgrade == WolfUpgrade.HEALTH) {
            wolf.heal((float) upgrade.perLevel());
        }
        level.playSound(null, wolf.getX(), wolf.getY(), wolf.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.75F, 1.0F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, wolf.getX(), wolf.getY() + 0.6, wolf.getZ(), 15, 0.4, 0.4, 0.4, 0.0);
        return Result.UPGRADED;
    }

    /** Clears every upgrade (debug command only: upgrades cannot be undone in the game). */
    public static void reset(Wolf wolf) {
        for (WolfUpgrade upgrade : WolfUpgrade.values()) {
            WolfData.setUpgradeLevel(wolf, upgrade, 0);
        }
        applyModifiers(wolf);
    }

    /** Scent upgrade: how much more the owner's exploring counts towards finds (1.0 to 1.75). */
    public static float scentMultiplier(Wolf wolf) {
        return 1.0F + (float) (WolfUpgrade.SCENT.perLevel() * level(wolf, WolfUpgrade.SCENT));
    }
}
