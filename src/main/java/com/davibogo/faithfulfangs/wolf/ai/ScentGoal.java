package com.davibogo.faithfulfangs.wolf.ai;

import com.davibogo.faithfulfangs.wolf.WolfScent;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Scent bookkeeping ({@link WolfScent}): every 5 seconds measures how far the owner got (straight
 * line, ignoring height) while the wolf can sniff, counts it towards the next finds and, when one
 * is due, has the wolf dig it up and hands it to {@link ScentAlertGoal}. Holds no goal flags, so
 * it runs alongside everything else.
 */
public class ScentGoal extends Goal {
    private final Wolf wolf;
    private final ScentAlertGoal alert;
    /** Where the owner was at the last sample; null when the last sample did not count. */
    private @Nullable Vec3 lastOwnerPos;
    private int nextSampleTick;

    public ScentGoal(Wolf wolf, ScentAlertGoal alert) {
        this.wolf = wolf;
        this.alert = alert;
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
    public void tick() {
        if (this.wolf.tickCount < this.nextSampleTick) {
            return;
        }
        this.nextSampleTick = this.wolf.tickCount + WolfScent.SAMPLE_TICKS;

        ServerPlayer owner = WolfScent.sniffingOwner(this.wolf);
        if (owner == null || this.alert.isAlerting()) {
            this.lastOwnerPos = null;
            return;
        }
        Vec3 ownerPos = owner.position();
        if (this.lastOwnerPos != null) {
            WolfScent.addProgress(this.wolf, ownerPos.subtract(this.lastOwnerPos).horizontalDistance());
        }
        this.lastOwnerPos = ownerPos;

        List<ItemEntity> drops = WolfScent.tryFind(this.wolf);
        if (!drops.isEmpty()) {
            this.alert.alert(drops);
        }
    }
}
