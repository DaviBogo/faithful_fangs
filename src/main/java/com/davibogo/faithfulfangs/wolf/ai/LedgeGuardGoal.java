package com.davibogo.faithfulfangs.wolf.ai;

import com.davibogo.faithfulfangs.wolf.WolfSafety;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.jspecify.annotations.Nullable;

/**
 * Keeps a tamed wolf from jumping down a cliff. While fighting, vanilla lets a healthy wolf plan
 * falls of 20 blocks or more to reach its target; when the next steps of the current path include a
 * fall that would hurt, this goal (highest priority) stops the wolf at the edge for a moment. If the
 * same target stays out of reach for 5 seconds, the wolf gives it up.
 */
public class LedgeGuardGoal extends Goal {
    private static final int HOLD_TICKS = 20;
    private static final int GIVE_UP_TICKS = 100;
    /** Blocked again after this long counts as a new attempt. */
    private static final int FORGET_TICKS = 100;

    private final Wolf wolf;
    private int holdUntil;
    private @Nullable LivingEntity blockedTarget;
    private int blockedSince;
    private int lastBlocked;

    public LedgeGuardGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.wolf.isTame() && !this.wolf.isOrderedToSit() && WolfSafety.hasDangerousDropAhead(this.wolf);
    }

    @Override
    public boolean canContinueToUse() {
        return this.wolf.tickCount < this.holdUntil;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.wolf.getNavigation().stop();
        int now = this.wolf.tickCount;
        this.holdUntil = now + HOLD_TICKS;
        LivingEntity target = this.wolf.getTarget();
        if (target != this.blockedTarget || now - this.lastBlocked > FORGET_TICKS) {
            this.blockedTarget = target;
            this.blockedSince = now;
        }
        this.lastBlocked = now;
    }

    @Override
    public void tick() {
        int now = this.wolf.tickCount;
        this.lastBlocked = now;
        LivingEntity target = this.wolf.getTarget();
        if (target == null) {
            return;
        }
        this.wolf.getLookControl().setLookAt(target, 10.0F, this.wolf.getMaxHeadXRot());
        if (target == this.blockedTarget && now - this.blockedSince >= GIVE_UP_TICKS) {
            this.wolf.setTarget(null);
            this.blockedTarget = null;
        }
    }
}
