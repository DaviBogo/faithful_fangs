package com.davibogo.faithfulfangs.wolf.ai;

import com.davibogo.faithfulfangs.bond.Bond;
import com.davibogo.faithfulfangs.wolf.WolfHome;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Stay Home at night: the wolf goes to its bed and lies down on it (wolves have no sleeping
 * animation, so it uses the sitting pose). It gets up in the morning, when it has a target to
 * fight, or when something hurts it.
 */
public class SleepInBedGoal extends Goal {
    /** Close enough to the bed center to lie down (blocks). */
    private static final double LIE_DOWN_DISTANCE = 0.35;
    /** From this distance the wolf steps straight onto the bed instead of pathfinding. */
    private static final double FINAL_APPROACH_DISTANCE = 1.5;
    /** Gives up for a while if it cannot reach the bed in this time (ticks). */
    private static final int MAX_APPROACH_TICKS = 600;
    private static final int RETRY_COOLDOWN_TICKS = 1200;
    private static final int RECENT_HURT_TICKS = 100;

    private final Wolf wolf;
    private @Nullable Vec3 bed;
    private boolean lying;
    private int approachTicks;
    private int cooldown;

    public SleepInBedGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        if (!this.wantsToSleep()) {
            return false;
        }
        Bond home = WolfHome.homeBond(this.wolf);
        if (home == null || !WolfHome.isInHomeDimension(this.wolf, home) || !this.wolf.isWithinHome()) {
            return false;
        }
        this.bed = WolfHome.bedSurface(home.bed().pos());
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.bed != null && this.wantsToSleep() && WolfHome.homeBond(this.wolf) != null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.lying = false;
        this.approachTicks = 0;
        this.moveToBed();
    }

    @Override
    public void tick() {
        if (this.lying || this.bed == null) {
            return;
        }
        double distance = Math.sqrt(this.horizontalDistanceSqr(this.bed));
        if (distance <= LIE_DOWN_DISTANCE) {
            this.wolf.getNavigation().stop();
            this.wolf.setInSittingPose(true);
            this.lying = true;
            return;
        }
        if (++this.approachTicks > MAX_APPROACH_TICKS) {
            this.cooldown = RETRY_COOLDOWN_TICKS; // bed unreachable: try again later
            this.bed = null;
            return;
        }
        if (distance <= FINAL_APPROACH_DISTANCE) {
            this.wolf.getNavigation().stop();
            this.wolf.getMoveControl().setWantedPosition(this.bed.x, this.bed.y, this.bed.z, 0.8);
        } else if (this.wolf.getNavigation().isDone()) {
            this.moveToBed();
        }
    }

    @Override
    public void stop() {
        if (this.lying) {
            this.wolf.setInSittingPose(false);
        }
        this.lying = false;
        this.bed = null;
    }

    /** Night, Stay Home, not told to sit, nothing to fight, not just hurt. */
    private boolean wantsToSleep() {
        return WolfHome.isStayHome(this.wolf)
                && !this.wolf.isOrderedToSit()
                && this.wolf.level().isDarkOutside()
                && this.wolf.getTarget() == null
                && (this.wolf.getLastHurtByMob() == null
                        || this.wolf.tickCount - this.wolf.getLastHurtByMobTimestamp() > RECENT_HURT_TICKS);
    }

    private void moveToBed() {
        if (this.bed != null) {
            this.wolf.getNavigation().moveTo(this.bed.x, this.bed.y, this.bed.z, 1.0);
        }
    }

    private double horizontalDistanceSqr(Vec3 pos) {
        double dx = this.wolf.getX() - pos.x;
        double dz = this.wolf.getZ() - pos.z;
        return dx * dx + dz * dz;
    }
}
