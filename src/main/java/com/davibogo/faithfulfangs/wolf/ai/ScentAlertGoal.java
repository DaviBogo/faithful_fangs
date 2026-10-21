package com.davibogo.faithfulfangs.wolf.ai;

import com.davibogo.faithfulfangs.wolf.WolfData;
import com.davibogo.faithfulfangs.wolf.WolfMode;
import com.davibogo.faithfulfangs.wolf.WolfScent;
import com.davibogo.faithfulfangs.wolf.WolfSounds;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * After a scent find: the wolf stays by the item, tilts its head, looks at its owner and barks
 * every couple of seconds. It stops when the owner comes close or takes the item, after 20
 * seconds, or when something more important happens (a fight, danger, a new order).
 */
public class ScentAlertGoal extends Goal {
    private static final int DURATION = 400;
    private static final double OWNER_CLOSE = 3.0;
    /** The wolf walks back to the item if it is further than this (it may roll away). */
    private static final double STAY_DISTANCE = 2.0;
    private static final int BARK_MIN_TICKS = 30;
    private static final int BARK_EXTRA_TICKS = 20;

    private final Wolf wolf;
    private final List<ItemEntity> items = new ArrayList<>();
    private int endTick;
    private int nextBarkTick;

    public ScentAlertGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /** Starts the alert for the items the wolf just dug up, with a first bark right away. */
    public void alert(List<ItemEntity> drops) {
        this.items.clear();
        this.items.addAll(drops);
        this.endTick = this.wolf.tickCount + DURATION;
        this.nextBarkTick = this.wolf.tickCount + BARK_MIN_TICKS;
        WolfSounds.bark(this.wolf);
    }

    public boolean isAlerting() {
        return !this.items.isEmpty();
    }

    @Override
    public boolean canUse() {
        return this.stillAlerting();
    }

    @Override
    public boolean canContinueToUse() {
        return this.stillAlerting();
    }

    /** Checks whether the alert goes on; when it is over, forgets the items. */
    private boolean stillAlerting() {
        this.items.removeIf(item -> !item.isAlive());
        if (this.items.isEmpty()) {
            return false;
        }
        LivingEntity owner = this.wolf.getOwner();
        boolean over = this.wolf.tickCount >= this.endTick
                || owner == null || owner.level() != this.wolf.level()
                || owner.distanceToSqr(this.wolf) > WolfScent.OWNER_RANGE * WolfScent.OWNER_RANGE * 4
                || owner.distanceToSqr(this.wolf) < OWNER_CLOSE * OWNER_CLOSE
                || owner.distanceToSqr(this.items.getFirst()) < OWNER_CLOSE * OWNER_CLOSE
                || this.wolf.getTarget() != null || this.wolf.isOrderedToSit()
                || WolfData.getStandingMode(this.wolf) != WolfMode.FOLLOW;
        if (over) {
            this.items.clear();
        }
        return !over;
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
        ItemEntity item = this.items.getFirst();
        if (this.wolf.distanceToSqr(item) > STAY_DISTANCE * STAY_DISTANCE) {
            if (this.wolf.getNavigation().isDone()) {
                this.wolf.getNavigation().moveTo(item, 1.0);
            }
        } else {
            this.wolf.getNavigation().stop();
        }
        LivingEntity owner = this.wolf.getOwner();
        if (owner != null) {
            this.wolf.getLookControl().setLookAt(owner, 10.0F, this.wolf.getMaxHeadXRot());
        }
        if (this.wolf.tickCount >= this.nextBarkTick) {
            WolfSounds.bark(this.wolf);
            this.nextBarkTick = this.wolf.tickCount + BARK_MIN_TICKS + this.wolf.getRandom().nextInt(BARK_EXTRA_TICKS);
        }
    }
}
