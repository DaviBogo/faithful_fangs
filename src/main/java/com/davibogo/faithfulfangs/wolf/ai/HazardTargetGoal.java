package com.davibogo.faithfulfangs.wolf.ai;

import com.davibogo.faithfulfangs.wolf.WolfSafety;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;

/**
 * A tamed wolf does not fight a mob that is burning or standing in or next to fire or lava (it
 * would catch fire). It drops that target and, for 3 seconds, holds the TARGET flag at the highest
 * priority so no other target goal picks it again right away.
 */
public class HazardTargetGoal extends Goal {
    private static final int HOLD_TICKS = 60;

    private final Wolf wolf;
    private int holdUntil;

    public HazardTargetGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.wolf.getTarget();
        return this.wolf.isTame() && target != null && WolfSafety.isHazardousTarget(target);
    }

    @Override
    public boolean canContinueToUse() {
        return this.wolf.tickCount < this.holdUntil;
    }

    @Override
    public void start() {
        this.holdUntil = this.wolf.tickCount + HOLD_TICKS;
        PassiveNoTargetGoal.calmDown(this.wolf);
    }
}
