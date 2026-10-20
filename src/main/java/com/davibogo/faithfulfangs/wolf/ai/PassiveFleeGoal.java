package com.davibogo.faithfulfangs.wolf.ai;

import com.davibogo.faithfulfangs.wolf.WolfData;
import com.davibogo.faithfulfangs.wolf.WolfMode;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Passive stance: the wolf never fights back; it runs away for as long as it is being hunted.
 * <p>
 * A threat is something that hurt the wolf in the last few seconds or a mob whose target is the
 * wolf. The wolf keeps fleeing while the threat is still after it (up to {@link #DANGER_DISTANCE}),
 * and at least until it is {@link #SAFE_DISTANCE} away. In Follow mode it also teleports to its
 * owner once it is far from them, like the vanilla wolf panic does, so fast or teleporting
 * hunters (endermen) cannot corner it.
 */
public class PassiveFleeGoal extends Goal {
    /** How long a hit is remembered as a reason to flee (ticks). */
    private static final int HURT_MEMORY_TICKS = 100;
    /** Mobs targeting the wolf are noticed within this radius. */
    private static final double HUNTER_SCAN_RADIUS = 24.0;
    /** While the threat is still hunting the wolf, keep fleeing up to this distance. */
    private static final double DANGER_DISTANCE = 32.0;
    /** Even if the threat gives up, keep running until at least this far. */
    private static final double SAFE_DISTANCE = 16.0;
    private static final double SPEED = 1.5;
    private static final int SCAN_INTERVAL = 10;
    /** Pick a new escape point this often even if the current one was not reached (ticks). */
    private static final int REPATH_INTERVAL = 20;

    private final Wolf wolf;
    private @Nullable LivingEntity threat;
    private @Nullable Vec3 fleeTo;
    private int scanCooldown;
    private int repathCooldown;

    public PassiveFleeGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!PassiveNoTargetGoal.isPassive(this.wolf) || this.wolf.isOrderedToSit()) {
            return false;
        }
        this.threat = this.findThreat(false);
        return this.threat != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!PassiveNoTargetGoal.isPassive(this.wolf) || this.wolf.isOrderedToSit() || !this.isPresent(this.threat)) {
            return false;
        }
        double distanceSqr = this.wolf.distanceToSqr(this.threat);
        if (this.isHunting(this.threat)) {
            return distanceSqr < DANGER_DISTANCE * DANGER_DISTANCE;
        }
        return distanceSqr < SAFE_DISTANCE * SAFE_DISTANCE;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.repathCooldown = 0;
        this.flee();
    }

    @Override
    public void tick() {
        // Someone else may have started hunting the wolf, or hit it again.
        LivingEntity newest = this.findThreat(true);
        if (newest != null) {
            this.threat = newest;
        }

        // Follow mode: run to the owner when far from them (vanilla panic does the same).
        if (WolfData.getStandingMode(this.wolf) == WolfMode.FOLLOW
                && !this.wolf.unableToMoveToOwner() && this.wolf.shouldTryTeleportToOwner()) {
            this.wolf.tryToTeleportToOwner();
            this.repathCooldown = 0;
        }

        if (--this.repathCooldown <= 0 || this.wolf.getNavigation().isDone()) {
            this.flee();
        }
    }

    @Override
    public void stop() {
        this.threat = null;
        this.fleeTo = null;
        this.wolf.getNavigation().stop();
    }

    private void flee() {
        this.repathCooldown = REPATH_INTERVAL;
        if (this.threat == null) {
            return;
        }
        this.fleeTo = DefaultRandomPos.getPosAway(this.wolf, 16, 7, this.threat.position());
        if (this.fleeTo == null) {
            // Cornered: any reachable spot is better than standing still.
            this.fleeTo = DefaultRandomPos.getPos(this.wolf, 10, 7);
        }
        if (this.fleeTo != null) {
            this.wolf.getNavigation().moveTo(this.fleeTo.x, this.fleeTo.y, this.fleeTo.z, SPEED);
        }
    }

    /**
     * The last attacker if it hit the wolf recently, otherwise the nearest mob targeting the wolf.
     * The area scan only runs every {@link #SCAN_INTERVAL} ticks.
     */
    private @Nullable LivingEntity findThreat(boolean onlyNewHit) {
        LivingEntity attacker = this.wolf.getLastHurtByMob();
        if (this.isPresent(attacker) && this.recentlyHurtBy(attacker)) {
            return attacker;
        }
        if (onlyNewHit || --this.scanCooldown > 0) {
            return null;
        }
        this.scanCooldown = SCAN_INTERVAL;
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Mob hunter : this.wolf.level().getEntitiesOfClass(Mob.class,
                this.wolf.getBoundingBox().inflate(HUNTER_SCAN_RADIUS), this::isHunting)) {
            double distance = this.wolf.distanceToSqr(hunter);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = hunter;
            }
        }
        return nearest;
    }

    /** Still after the wolf: hit it recently, or has it as target. */
    private boolean isHunting(@Nullable LivingEntity entity) {
        return this.isPresent(entity)
                && (this.recentlyHurtBy(entity) || entity instanceof Mob mob && mob.getTarget() == this.wolf);
    }

    private boolean recentlyHurtBy(LivingEntity entity) {
        return entity == this.wolf.getLastHurtByMob()
                && this.wolf.tickCount - this.wolf.getLastHurtByMobTimestamp() < HURT_MEMORY_TICKS;
    }

    /** Alive, in the same level, and not the owner (an accidental hit from the owner is no threat). */
    private boolean isPresent(@Nullable LivingEntity entity) {
        return entity != null && entity.isAlive() && entity.level() == this.wolf.level() && !this.wolf.isOwnedBy(entity);
    }
}
