package com.davibogo.faithfulfangs.wolf.ai;

import com.davibogo.faithfulfangs.wolf.WolfData;
import com.davibogo.faithfulfangs.wolf.WolfSafety;
import com.davibogo.faithfulfangs.wolf.WolfStance;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.jspecify.annotations.Nullable;

/**
 * Makes a tamed wolf drop a target when:
 * <ul>
 *   <li>(Neutral) the target is burning or standing in or next to fire or lava: fighting it would
 *       set the wolf on fire. An Aggressive wolf takes that risk; it still never steps into fire or
 *       lava ({@link StepGuardGoal});</li>
 *   <li>(any stance) the wolf has not got any closer to the target for 5 seconds, for example
 *       because reaching it would mean stepping into fire or lava: it gives up instead of waiting
 *       at the edge forever.</li>
 * </ul>
 * After dropping a target it holds the TARGET flag at the highest priority for 3 seconds, so no
 * other target goal picks it again right away.
 */
public class HazardTargetGoal extends Goal {
    private static final int HOLD_TICKS = 60;
    private static final int GIVE_UP_TICKS = 100;

    private final Wolf wolf;
    private int holdUntil;
    private @Nullable LivingEntity trackedTarget;
    private double bestDistance;
    private int bestDistanceTick;

    public HazardTargetGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.wolf.getTarget();
        if (!this.wolf.isTame() || target == null) {
            this.trackedTarget = null;
            return false;
        }
        if (WolfData.getStance(this.wolf) != WolfStance.AGGRESSIVE && WolfSafety.isHazardousTarget(target)) {
            return true;
        }
        return this.gotNoCloser(target);
    }

    /** Tracks the distance to the target; true once it has not shrunk for {@link #GIVE_UP_TICKS}. */
    private boolean gotNoCloser(LivingEntity target) {
        int now = this.wolf.tickCount;
        double distance = this.wolf.distanceTo(target);
        if (target != this.trackedTarget) {
            this.trackedTarget = target;
            this.bestDistance = distance;
            this.bestDistanceTick = now;
            return false;
        }
        if (distance < this.bestDistance - 1.0 || this.wolf.isWithinMeleeAttackRange(target)) {
            this.bestDistance = Math.min(this.bestDistance, distance);
            this.bestDistanceTick = now;
            return false;
        }
        return now - this.bestDistanceTick >= GIVE_UP_TICKS;
    }

    @Override
    public boolean canContinueToUse() {
        return this.wolf.tickCount < this.holdUntil;
    }

    @Override
    public void start() {
        this.holdUntil = this.wolf.tickCount + HOLD_TICKS;
        this.trackedTarget = null;
        PassiveNoTargetGoal.calmDown(this.wolf);
    }
}
