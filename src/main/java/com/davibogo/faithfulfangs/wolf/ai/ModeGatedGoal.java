package com.davibogo.faithfulfangs.wolf.ai;

import java.util.function.BooleanSupplier;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Wraps a vanilla goal so it only runs while a condition holds (for example, "follow the owner"
 * only in Follow mode). Everything else is delegated to the wrapped goal.
 */
public class ModeGatedGoal extends Goal {
    private final Goal goal;
    private final BooleanSupplier condition;

    public ModeGatedGoal(Goal goal, BooleanSupplier condition) {
        this.goal = goal;
        this.condition = condition;
        this.setFlags(goal.getFlags());
    }

    public Goal getWrapped() {
        return this.goal;
    }

    @Override
    public boolean canUse() {
        return this.condition.getAsBoolean() && this.goal.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.condition.getAsBoolean() && this.goal.canContinueToUse();
    }

    @Override
    public boolean isInterruptable() {
        return this.goal.isInterruptable();
    }

    @Override
    public void start() {
        this.goal.start();
    }

    @Override
    public void stop() {
        this.goal.stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return this.goal.requiresUpdateEveryTick();
    }

    @Override
    public void tick() {
        this.goal.tick();
    }

    @Override
    public String toString() {
        return "Gated[" + this.goal + "]";
    }
}
