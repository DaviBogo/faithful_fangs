package com.davibogo.faithfulfangs.wolf.ai;

import com.davibogo.faithfulfangs.wolf.WolfData;
import com.davibogo.faithfulfangs.wolf.WolfStance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.warden.Warden;

/**
 * Adds the Faithful Fangs AI goals to every wolf (they only act on tamed wolves, by stance).
 * Neutral stance keeps the vanilla goals as they are.
 */
public final class WolfGoals {
    /** Target priority of the Aggressive goal: after the vanilla "defend owner" and "revenge" goals. */
    private static final int AGGRESSIVE_TARGET_PRIORITY = 5;

    private WolfGoals() {
    }

    /** Called when a wolf joins a level (server side). Safe to call more than once. */
    public static void install(Wolf wolf) {
        for (WrappedGoal goal : wolf.targetSelector.getAvailableGoals()) {
            if (goal.getGoal() instanceof PassiveNoTargetGoal) {
                return; // already installed on this wolf
            }
        }
        wolf.targetSelector.addGoal(0, new PassiveNoTargetGoal(wolf));
        // mustSee + mustReach: ignores monsters it cannot get to (behind walls, flying, across water).
        wolf.targetSelector.addGoal(AGGRESSIVE_TARGET_PRIORITY, new NearestAttackableTargetGoal<>(
                wolf, Mob.class, 10, true, true, (target, level) -> isAggressiveTarget(wolf, target)));
        wolf.goalSelector.addGoal(1, new PassiveFleeGoal(wolf));
    }

    /**
     * Aggressive stance: any hostile mob, before the owner attacks it. Skips creepers (wolves keep
     * away from them), calm neutral mobs (endermen, zombified piglins), piglins that are not
     * hostile right now, and mobs a wolf should never pick a fight with (warden, bosses).
     */
    static boolean isAggressiveTarget(Wolf wolf, LivingEntity target) {
        if (!wolf.isTame() || wolf.isOrderedToSit() || WolfData.getStance(wolf) != WolfStance.AGGRESSIVE) {
            return false;
        }
        LivingEntity owner = wolf.getOwner();
        if (owner == null || !(target instanceof Enemy)) {
            return false;
        }
        if (target instanceof Creeper || target instanceof Warden
                || target instanceof WitherBoss || target instanceof EnderDragon) {
            return false;
        }
        if (target instanceof NeutralMob neutral && !neutral.isAngry()) {
            return false;
        }
        if (target instanceof AbstractPiglin piglin && piglin.getTarget() == null) {
            return false;
        }
        return wolf.wantsToAttack(target, owner);
    }
}
