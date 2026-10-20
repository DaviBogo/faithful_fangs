package com.davibogo.faithfulfangs.wolf.ai;

import com.davibogo.faithfulfangs.wolf.WolfSafety;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.phys.Vec3;

/**
 * Last line of defense for tamed wolves, checked every tick: when the wolf's next steps (its path
 * or its momentum, including a push or a jump) would end in fire or lava, it stops right there.
 * Paths already avoid fire and lava; this catches uneven ground and lava that spreads. It holds no
 * goal flags, so it never interrupts other goals. A wolf that is already in danger is left alone,
 * so it can get out.
 */
public class StepGuardGoal extends Goal {
    private final Wolf wolf;

    public StepGuardGoal(Wolf wolf) {
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
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.wolf.isOrderedToSit() || WolfSafety.isInDanger(this.wolf) || !WolfSafety.isHazardAhead(this.wolf)) {
            return;
        }
        this.wolf.getNavigation().stop();
        Vec3 motion = this.wolf.getDeltaMovement();
        this.wolf.setDeltaMovement(0.0, Math.min(motion.y, 0.0), 0.0);
    }
}
