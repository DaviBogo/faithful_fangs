package com.davibogo.faithfulfangs.wolf;

import com.davibogo.faithfulfangs.bond.BondRegistry;
import com.davibogo.faithfulfangs.wolf.ai.PassiveNoTargetGoal;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.animal.wolf.Wolf;

/** Server-side changes of mode and stance, used by the Wolf Panel buttons. */
public final class WolfCommands {
    private WolfCommands() {
    }

    /** Applies a movement mode. Returns false if it is not allowed (Stay Home without a bed). */
    public static boolean setMode(MinecraftServer server, Wolf wolf, WolfMode mode) {
        switch (mode) {
            case SIT -> setSitting(wolf, true);
            case FOLLOW -> {
                WolfData.setStandingMode(wolf, WolfMode.FOLLOW);
                setSitting(wolf, false);
            }
            case STAY_HOME -> {
                if (!hasBed(server, wolf)) {
                    return false;
                }
                WolfData.setStandingMode(wolf, WolfMode.STAY_HOME);
                setSitting(wolf, false);
            }
        }
        return true;
    }

    public static void setStance(Wolf wolf, WolfStance stance) {
        WolfData.setStance(wolf, stance);
        if (stance == WolfStance.PASSIVE) {
            PassiveNoTargetGoal.calmDown(wolf); // stop a fight right away
        }
    }

    public static boolean hasBed(MinecraftServer server, Wolf wolf) {
        return BondRegistry.get(server).getByWolf(wolf.getUUID()) != null;
    }

    /** Same as the vanilla right click on a tamed wolf. */
    private static void setSitting(Wolf wolf, boolean sitting) {
        wolf.setOrderedToSit(sitting);
        wolf.setJumping(false);
        wolf.getNavigation().stop();
        wolf.setTarget(null);
    }
}
