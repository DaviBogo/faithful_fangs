package com.davibogo.faithfulfangs.wolf.ai;

import com.davibogo.faithfulfangs.bond.Bond;
import com.davibogo.faithfulfangs.wolf.WolfData;
import com.davibogo.faithfulfangs.wolf.WolfHome;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Stay Home, far from the bed (or in another dimension): the wolf walks for a few seconds (towards
 * the bed if it can, otherwise away from its owner) and then teleports onto its bed. It is
 * invulnerable the whole time. Closer than {@link #TELEPORT_DISTANCE} it just walks home
 * (vanilla "move towards restriction" goal).
 */
public class ReturnHomeGoal extends Goal {
    /** Beyond this distance from the bed (same dimension) the wolf walks a bit and teleports. */
    private static final double TELEPORT_DISTANCE = 24.0;
    /** How long it walks before teleporting (ticks). */
    private static final int WALK_TICKS = 100;
    private static final double WALK_SPEED = 1.1;
    /**
     * Uses the entity's temporary invulnerability, refreshed every tick while returning, so it
     * wears off by itself if anything interrupts the return.
     */
    private static final int INVULNERABLE_TICKS = 40;

    private final Wolf wolf;
    private @Nullable Bond bond;
    private int ticks;
    private boolean arrived;

    public ReturnHomeGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.wolf.isOrderedToSit() || this.wolf.isLeashed() || this.wolf.isPassenger()) {
            return false;
        }
        Bond home = WolfHome.homeBond(this.wolf);
        if (home == null || !this.isFarFromHome(home)) {
            return false;
        }
        this.bond = home;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.arrived || this.wolf.isOrderedToSit()) {
            return false;
        }
        Bond home = WolfHome.homeBond(this.wolf);
        return home != null && home.equals(this.bond) && this.isFarFromHome(home);
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
        this.ticks = 0;
        this.arrived = false;
        this.wolf.setTarget(null);
        this.wolf.setInvulnerableTime(INVULNERABLE_TICKS);
        WolfData.setReturning(this.wolf, true);
        this.walk();
    }

    @Override
    public void tick() {
        this.wolf.setInvulnerableTime(INVULNERABLE_TICKS);
        if (++this.ticks >= WALK_TICKS) {
            this.arrived = this.bond != null && WolfHome.teleportHome(this.wolf, this.bond);
            this.ticks = 0; // if the teleport failed, walk a bit more and try again
            return;
        }
        if (this.wolf.getNavigation().isDone()) {
            this.walk();
        }
    }

    @Override
    public void stop() {
        // After a teleport to another dimension this wolf instance is gone; the new one is
        // cleaned up in WolfHome.onJoin.
        this.wolf.setInvulnerableTime(0);
        WolfData.setReturning(this.wolf, false);
        this.bond = null;
    }

    private boolean isFarFromHome(Bond home) {
        if (!WolfHome.isInHomeDimension(this.wolf, home)) {
            return true;
        }
        return this.wolf.position().distanceToSqr(WolfHome.bedSurface(home.bed().pos())) > TELEPORT_DISTANCE * TELEPORT_DISTANCE;
    }

    /** Heads for the bed if a path exists, otherwise just walks away from the owner. */
    private void walk() {
        if (this.bond != null && WolfHome.isInHomeDimension(this.wolf, this.bond)) {
            Vec3 bed = WolfHome.bedSurface(this.bond.bed().pos());
            if (this.wolf.getNavigation().moveTo(bed.x, bed.y, bed.z, WALK_SPEED)) {
                return;
            }
        }
        LivingEntity owner = this.wolf.getOwner();
        Vec3 away = owner != null && owner.level() == this.wolf.level()
                ? DefaultRandomPos.getPosAway(this.wolf, 16, 7, owner.position())
                : DefaultRandomPos.getPos(this.wolf, 16, 7);
        if (away != null) {
            this.wolf.getNavigation().moveTo(away.x, away.y, away.z, WALK_SPEED);
        }
    }
}
