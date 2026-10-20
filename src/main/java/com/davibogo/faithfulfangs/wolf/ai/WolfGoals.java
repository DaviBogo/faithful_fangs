package com.davibogo.faithfulfangs.wolf.ai;

import com.davibogo.faithfulfangs.wolf.WolfData;
import com.davibogo.faithfulfangs.wolf.WolfMode;
import com.davibogo.faithfulfangs.wolf.WolfStance;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
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
 * Adds the Faithful Fangs AI goals to every wolf. They only act on tamed wolves: stances
 * (Neutral keeps the vanilla goals as they are) and modes (Follow keeps vanilla, Stay Home adds the
 * home goals and turns off the vanilla goals that pull the wolf to its owner).
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

        // Stances.
        wolf.targetSelector.addGoal(0, new PassiveNoTargetGoal(wolf));
        // mustSee + mustReach: ignores monsters it cannot get to (behind walls, flying, across water).
        wolf.targetSelector.addGoal(AGGRESSIVE_TARGET_PRIORITY, new NearestAttackableTargetGoal<>(
                wolf, Mob.class, 10, true, true, (target, level) -> isAggressiveTarget(wolf, target)));
        wolf.goalSelector.addGoal(1, new PassiveFleeGoal(wolf));

        // Modes.
        gateVanillaGoals(wolf);
        wolf.goalSelector.addGoal(0, new HomeStateGoal(wolf));
        wolf.goalSelector.addGoal(2, new ReturnHomeGoal(wolf));
        wolf.goalSelector.addGoal(6, new SleepInBedGoal(wolf));
        // Only acts while the wolf is outside its home area (Stay Home): walks back in.
        wolf.goalSelector.addGoal(7, new MoveTowardsRestrictionGoal(wolf, 1.0));
    }

    /**
     * Vanilla goals that must not run in Stay Home mode:
     * <ul>
     *   <li>follow owner (also teleports to the owner) — Follow mode only;</li>
     *   <li>panic from fire/lava (also teleports to the owner) — replaced by a plain panic in Stay Home;</li>
     *   <li>"sit when ordered", which also makes tamed wolves sit whenever the owner is offline or in
     *       another dimension — in Stay Home only when really ordered to sit.</li>
     * </ul>
     */
    private static void gateVanillaGoals(Wolf wolf) {
        BooleanSupplier following = () -> WolfData.getStandingMode(wolf) == WolfMode.FOLLOW;
        List<WrappedGoal> vanillaGoals = new ArrayList<>(wolf.goalSelector.getAvailableGoals());
        for (WrappedGoal wrapped : vanillaGoals) {
            Goal goal = wrapped.getGoal();
            int priority = wrapped.getPriority();
            if (goal instanceof FollowOwnerGoal) {
                replace(wolf, priority, goal, new ModeGatedGoal(goal, following));
            } else if (goal instanceof TamableAnimal.TamableAnimalPanicGoal) {
                replace(wolf, priority, goal, new ModeGatedGoal(goal, following));
                wolf.goalSelector.addGoal(priority, new ModeGatedGoal(
                        new PanicGoal(wolf, 1.5, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES),
                        () -> !following.getAsBoolean()));
            } else if (goal instanceof SitWhenOrderedToGoal) {
                replace(wolf, priority, goal, new ModeGatedGoal(goal,
                        () -> following.getAsBoolean() || wolf.isOrderedToSit()));
            }
        }
    }

    private static void replace(Wolf wolf, int priority, Goal original, Goal replacement) {
        wolf.goalSelector.removeGoal(original);
        wolf.goalSelector.addGoal(priority, replacement);
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
