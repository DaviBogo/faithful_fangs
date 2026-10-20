package com.davibogo.faithfulfangs.wolf;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.wolf.Wolf;

/**
 * "Asleep in its Dog Bed" state. It is stored as the entity pose, which vanilla already syncs to
 * every client. Pose.SITTING is the pose camels use to lie down; vanilla wolves never use it and
 * it keeps the wolf's normal hitbox (Pose.SLEEPING would shrink it to 0.2 blocks).
 */
public final class WolfSleep {
    public static final Pose SLEEPING_POSE = Pose.SITTING;

    private WolfSleep() {
    }

    public static boolean isSleeping(Wolf wolf) {
        return wolf.getPose() == SLEEPING_POSE;
    }

    public static void setSleeping(Wolf wolf, boolean sleeping) {
        if (sleeping) {
            wolf.setPose(SLEEPING_POSE);
        } else if (isSleeping(wolf)) {
            wolf.setPose(Pose.STANDING);
        }
    }
}
