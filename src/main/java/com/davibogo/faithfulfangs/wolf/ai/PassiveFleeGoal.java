package com.davibogo.faithfulfangs.wolf.ai;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Passive stance: when the wolf was just hurt by something, or a mob is targeting it, it runs away
 * until it is far enough. It never fights back.
 */
public class PassiveFleeGoal extends Goal {
    /** How long a hit is remembered as a reason to flee (ticks). */
    private static final int HURT_MEMORY_TICKS = 100;
    private static final double HUNTER_SCAN_RADIUS = 10.0;
    private static final double SAFE_DISTANCE = 16.0;
    private static final double SPEED = 1.5;
    /** Scan for mobs targeting the wolf only every few ticks. */
    private static final int SCAN_INTERVAL = 10;

    private final Wolf wolf;
    private @Nullable LivingEntity threat;
    private @Nullable Vec3 fleeTo;
    private int scanCooldown;

    public PassiveFleeGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!PassiveNoTargetGoal.isPassive(this.wolf) || this.wolf.isOrderedToSit()) {
            return false;
        }
        this.threat = this.findThreat();
        return this.threat != null && this.pickFleePosition();
    }

    @Override
    public boolean canContinueToUse() {
        return PassiveNoTargetGoal.isPassive(this.wolf)
                && !this.wolf.isOrderedToSit()
                && this.threat != null
                && this.threat.isAlive()
                && this.wolf.distanceToSqr(this.threat) < SAFE_DISTANCE * SAFE_DISTANCE;
    }

    @Override
    public void start() {
        this.moveToFleePosition();
    }

    @Override
    public void tick() {
        if (this.wolf.getNavigation().isDone() && this.pickFleePosition()) {
            this.moveToFleePosition();
        }
    }

    @Override
    public void stop() {
        this.threat = null;
        this.fleeTo = null;
        this.wolf.getNavigation().stop();
    }

    private @Nullable LivingEntity findThreat() {
        LivingEntity attacker = this.wolf.getLastHurtByMob();
        if (attacker != null && attacker.isAlive() && !this.wolf.isOwnedBy(attacker)
                && this.wolf.tickCount - this.wolf.getLastHurtByMobTimestamp() < HURT_MEMORY_TICKS) {
            return attacker;
        }
        if (--this.scanCooldown > 0) {
            return null;
        }
        this.scanCooldown = SCAN_INTERVAL;
        List<Mob> hunters = this.wolf.level().getEntitiesOfClass(Mob.class,
                this.wolf.getBoundingBox().inflate(HUNTER_SCAN_RADIUS),
                mob -> mob.isAlive() && mob.getTarget() == this.wolf);
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Mob hunter : hunters) {
            double distance = this.wolf.distanceToSqr(hunter);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = hunter;
            }
        }
        return nearest;
    }

    private boolean pickFleePosition() {
        if (this.threat == null) {
            return false;
        }
        this.fleeTo = DefaultRandomPos.getPosAway(this.wolf, 16, 7, this.threat.position());
        return this.fleeTo != null;
    }

    private void moveToFleePosition() {
        if (this.fleeTo != null) {
            this.wolf.getNavigation().moveTo(this.fleeTo.x, this.fleeTo.y, this.fleeTo.z, SPEED);
        }
    }
}
