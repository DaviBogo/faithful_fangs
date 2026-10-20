package com.davibogo.faithfulfangs.wolf.ai;

import com.davibogo.faithfulfangs.wolf.WolfHome;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;

/**
 * Background goal with no control flags (never blocks other goals). Every second it keeps the
 * home restriction in sync with the mode, and in Stay Home drops targets that left the house area.
 */
public class HomeStateGoal extends Goal {
    private static final int INTERVAL = 20;
    /** A target this far beyond the home radius is given up. */
    private static final int CHASE_MARGIN = 2;

    private final Wolf wolf;
    private int cooldown;

    public HomeStateGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        return this.wolf.isTame();
    }

    @Override
    public boolean canContinueToUse() {
        return this.wolf.isTame();
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
    public void tick() {
        if (--this.cooldown > 0) {
            return;
        }
        this.cooldown = INTERVAL;
        WolfHome.updateHome(this.wolf);

        LivingEntity target = this.wolf.getTarget();
        if (target != null && WolfHome.isStayHome(this.wolf) && this.wolf.hasHome()) {
            double limit = WolfHome.HOME_RADIUS + CHASE_MARGIN;
            if (this.wolf.getHomePosition().distToCenterSqr(target.position()) > limit * limit) {
                this.wolf.setTarget(null);
            }
        }
    }
}
