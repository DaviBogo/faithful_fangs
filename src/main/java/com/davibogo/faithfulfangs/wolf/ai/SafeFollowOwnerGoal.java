package com.davibogo.faithfulfangs.wolf.ai;

import com.davibogo.faithfulfangs.wolf.WolfSafety;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Replaces the vanilla "follow owner" goal (Follow mode). Same distances and speed as vanilla, plus:
 * <ul>
 *   <li>teleports only to safe spots (never next to a creeper);</li>
 *   <li>a wolf that is stuck (fence, stairs, water bank...) while its owner is more than 5 blocks
 *       away teleports to the owner, instead of waiting until the owner is 12 blocks away like
 *       vanilla. Stuck means it barely moved for 3 seconds, or it has no path (or is swimming)
 *       and has not got any closer for 5 seconds;</li>
 *   <li>if no safe teleport spot is found, it keeps walking towards the owner.</li>
 * </ul>
 */
public class SafeFollowOwnerGoal extends Goal {
    private static final float START_DISTANCE = 10.0F;
    private static final float STOP_DISTANCE = 2.0F;
    /** Vanilla: beyond 12 blocks the wolf teleports to its owner. */
    private static final double TELEPORT_DISTANCE_SQR = 12.0 * 12.0;
    /** While farther than this from the owner the goal keeps trying, even without a path. */
    private static final double KEEP_TRYING_DISTANCE_SQR = 5.0 * 5.0;
    /** A wolf that moved less than this... */
    private static final double STUCK_MOVE_SQR = 1.5 * 1.5;
    /** ...in this many ticks is stuck. */
    private static final int STUCK_TICKS = 60;
    /** A wolf that got no closer to its owner in this many ticks is stuck too. */
    private static final int NO_PROGRESS_TICKS = 100;
    private static final double SPEED = 1.0;

    private final TamableAnimal wolf;
    private final PathNavigation navigation;
    private @Nullable LivingEntity owner;
    private int timeToRecalcPath;
    private float oldWaterCost;
    private Vec3 anchor = Vec3.ZERO;
    private int anchorTick;
    private double bestDistance;
    private int bestDistanceTick;

    public SafeFollowOwnerGoal(TamableAnimal wolf) {
        this.wolf = wolf;
        this.navigation = wolf.getNavigation();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = this.wolf.getOwner();
        if (owner == null || this.wolf.unableToMoveToOwner()
                || this.wolf.distanceToSqr(owner) < START_DISTANCE * START_DISTANCE) {
            return false;
        }
        this.owner = owner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.owner == null || this.wolf.unableToMoveToOwner()) {
            return false;
        }
        double distanceSqr = this.wolf.distanceToSqr(this.owner);
        if (distanceSqr <= STOP_DISTANCE * STOP_DISTANCE) {
            return false;
        }
        // Vanilla gives up as soon as the path ends; this keeps trying while still far away.
        return !this.navigation.isDone() || distanceSqr >= KEEP_TRYING_DISTANCE_SQR;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.wolf.getPathfindingMalus(PathType.WATER);
        this.wolf.setPathfindingMalus(PathType.WATER, 0.0F);
        this.resetStuckTimer();
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.wolf.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        LivingEntity owner = this.owner;
        if (owner == null) {
            return;
        }
        double distanceSqr = this.wolf.distanceToSqr(owner);
        boolean far = distanceSqr >= TELEPORT_DISTANCE_SQR;
        if (!far) {
            this.wolf.getLookControl().setLookAt(owner, 10.0F, this.wolf.getMaxHeadXRot());
        }

        int now = this.wolf.tickCount;
        if (this.wolf.position().distanceToSqr(this.anchor) > STUCK_MOVE_SQR) {
            this.anchor = this.wolf.position();
            this.anchorTick = now;
        }
        double distance = Math.sqrt(distanceSqr);
        boolean walkingAPath = !this.navigation.isDone() && !this.wolf.isInWater();
        if (distance < this.bestDistance - 1.0 || walkingAPath) {
            // A wolf walking a path normally is never "stuck" here, even if a running owner keeps
            // the distance from shrinking; the "barely moved" check covers wolves pushing on a fence.
            this.bestDistance = Math.min(this.bestDistance, distance);
            this.bestDistanceTick = now;
        }
        boolean stuck = distanceSqr >= KEEP_TRYING_DISTANCE_SQR
                && (now - this.anchorTick >= STUCK_TICKS || now - this.bestDistanceTick >= NO_PROGRESS_TICKS);

        if (--this.timeToRecalcPath > 0) {
            return;
        }
        this.timeToRecalcPath = this.adjustedTickDelay(10);
        if ((far || stuck) && WolfSafety.teleportNear(this.wolf, owner.blockPosition())) {
            this.resetStuckTimer();
            return;
        }
        this.navigation.moveTo(owner, SPEED);
    }

    private void resetStuckTimer() {
        this.anchor = this.wolf.position();
        this.anchorTick = this.wolf.tickCount;
        this.bestDistance = this.owner == null ? Double.MAX_VALUE : this.wolf.distanceTo(this.owner);
        this.bestDistanceTick = this.wolf.tickCount;
    }
}
