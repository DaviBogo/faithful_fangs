package com.davibogo.faithfulfangs.wolf.ai;

import com.davibogo.faithfulfangs.wolf.WolfData;
import com.davibogo.faithfulfangs.wolf.WolfStance;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;

/**
 * Passive stance: runs at the highest target priority and holds the TARGET flag, so none of the
 * vanilla target goals (defend owner, revenge, skeletons...) can start. Also drops any target or anger.
 */
public class PassiveNoTargetGoal extends Goal {
    private final Wolf wolf;

    public PassiveNoTargetGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public static boolean isPassive(Wolf wolf) {
        return wolf.isTame() && WolfData.getStance(wolf) == WolfStance.PASSIVE;
    }

    @Override
    public boolean canUse() {
        return isPassive(this.wolf);
    }

    @Override
    public boolean canContinueToUse() {
        return isPassive(this.wolf);
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        calmDown(this.wolf);
    }

    @Override
    public void tick() {
        calmDown(this.wolf);
    }

    /** Drops the current target and anger, but keeps "last hurt by" so the wolf still knows whom to flee. */
    public static void calmDown(Wolf wolf) {
        if (wolf.getTarget() != null) {
            wolf.setTarget(null);
        }
        if (wolf.isAngry()) {
            wolf.setPersistentAngerTarget(null);
            wolf.setPersistentAngerEndTime(-1L);
        }
    }
}
