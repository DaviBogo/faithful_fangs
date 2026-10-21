package com.davibogo.faithfulfangs.wolf.ai;

import com.davibogo.faithfulfangs.wolf.WolfData;
import com.davibogo.faithfulfangs.wolf.WolfMode;
import com.davibogo.faithfulfangs.wolf.WolfScent;
import com.davibogo.faithfulfangs.wolf.WolfSounds;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * After a scent find: the wolf stays by the item, tilts its head, looks at its owner and calls
 * with quick runs of 2 or 3 barks every couple of seconds, unlike its usual single random bark.
 * The first call (3 barks) always plays in full. Then it stops when the owner comes close or
 * someone takes the item, after 20 seconds, or when something more important happens (a fight,
 * danger, a new order).
 */
public class ScentAlertGoal extends Goal {
    private static final int DURATION = 400;
    private static final double OWNER_CLOSE = 3.0;
    /** The wolf walks back to the item if it is further than this (it may roll away). */
    private static final double STAY_DISTANCE = 2.0;
    /** Ticks between the barks of one call. */
    private static final int BARK_GAP = 7;
    /** Ticks between calls: 35 to 49. */
    private static final int CALL_GAP = 35;
    private static final int CALL_GAP_EXTRA = 15;
    private static final int FIRST_CALL_BARKS = 3;

    private final Wolf wolf;
    private final List<ItemEntity> items = new ArrayList<>();
    private boolean active;
    private int endTick;
    private int firstCallEndTick;
    private int nextBarkTick;
    private int barksInCall;
    private int callBarks;

    public ScentAlertGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /** Starts the alert for the items the wolf just dug up; the first call starts right away. */
    public void alert(List<ItemEntity> drops) {
        this.items.clear();
        this.items.addAll(drops);
        this.active = true;
        int now = this.wolf.tickCount;
        this.endTick = now + DURATION;
        this.firstCallEndTick = now + BARK_GAP * FIRST_CALL_BARKS;
        this.nextBarkTick = now;
        this.barksInCall = 0;
        this.callBarks = FIRST_CALL_BARKS;
    }

    public boolean isAlerting() {
        return this.active;
    }

    @Override
    public boolean canUse() {
        return this.stillAlerting();
    }

    @Override
    public boolean canContinueToUse() {
        return this.stillAlerting();
    }

    /** Checks whether the alert goes on; when it is over, forgets it. */
    private boolean stillAlerting() {
        if (!this.active) {
            return false;
        }
        this.items.removeIf(item -> !item.isAlive());
        LivingEntity owner = this.wolf.getOwner();
        boolean interrupted = owner == null || owner.level() != this.wolf.level()
                || owner.distanceToSqr(this.wolf) > WolfScent.OWNER_RANGE * WolfScent.OWNER_RANGE * 4
                || this.wolf.getTarget() != null || this.wolf.isOrderedToSit()
                || WolfData.getStandingMode(this.wolf) != WolfMode.FOLLOW;
        boolean answered = !interrupted && this.wolf.tickCount >= this.firstCallEndTick && (this.items.isEmpty()
                || isClose(owner, this.wolf) || isClose(owner, this.items.getFirst()));
        if (interrupted || answered || this.wolf.tickCount >= this.endTick) {
            this.active = false;
            this.items.clear();
        }
        return this.active;
    }

    private static boolean isClose(LivingEntity owner, Entity entity) {
        return owner.distanceToSqr(entity) < OWNER_CLOSE * OWNER_CLOSE;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.wolf.getNavigation().stop();
        this.wolf.setIsInterested(true);
    }

    @Override
    public void stop() {
        this.wolf.setIsInterested(false);
    }

    @Override
    public void tick() {
        if (!this.items.isEmpty()) {
            ItemEntity item = this.items.getFirst();
            if (this.wolf.distanceToSqr(item) > STAY_DISTANCE * STAY_DISTANCE) {
                if (this.wolf.getNavigation().isDone()) {
                    this.wolf.getNavigation().moveTo(item, 1.0);
                }
            } else {
                this.wolf.getNavigation().stop();
            }
        }
        LivingEntity owner = this.wolf.getOwner();
        if (owner != null) {
            this.wolf.getLookControl().setLookAt(owner, 10.0F, this.wolf.getMaxHeadXRot());
        }
        if (this.wolf.tickCount >= this.nextBarkTick) {
            WolfSounds.bark(this.wolf);
            if (++this.barksInCall < this.callBarks) {
                this.nextBarkTick = this.wolf.tickCount + BARK_GAP;
            } else {
                // Next call: 2 or 3 barks after a short pause.
                this.barksInCall = 0;
                this.callBarks = 2 + this.wolf.getRandom().nextInt(2);
                this.nextBarkTick = this.wolf.tickCount + CALL_GAP + this.wolf.getRandom().nextInt(CALL_GAP_EXTRA);
            }
        }
    }
}
