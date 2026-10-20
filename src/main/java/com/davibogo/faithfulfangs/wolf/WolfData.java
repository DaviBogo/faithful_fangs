package com.davibogo.faithfulfangs.wolf;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.jspecify.annotations.Nullable;

/**
 * Faithful Fangs data stored on the wolf itself (inside Forge's persistent entity data,
 * under the "faithful_fangs" key). It is saved with the wolf and follows it across dimensions.
 */
public final class WolfData {
    private static final String ROOT = "faithful_fangs";
    private static final String BOND_NAME = "bond_name";
    private static final String STANDING_MODE = "standing_mode";
    private static final String STANCE = "stance";
    private static final String RETURNING = "returning";
    private static final String HOME_APPLIED = "home_applied";

    private WolfData() {
    }

    /**
     * The name this mod last gave the wolf through its bond, or null if the wolf has no bond
     * applied. Used to tell our name apart from one the player set later with a vanilla Name Tag.
     */
    public static @Nullable String getBondName(Wolf wolf) {
        return read(wolf).getString(BOND_NAME).orElse(null);
    }

    public static void setBondName(Wolf wolf, @Nullable String name) {
        CompoundTag root = read(wolf);
        if (name == null) {
            root.remove(BOND_NAME);
        } else {
            root.putString(BOND_NAME, name);
        }
        write(wolf, root);
    }

    /** Mode the wolf follows while it is not sitting: FOLLOW (default) or STAY_HOME. */
    public static WolfMode getStandingMode(Wolf wolf) {
        return WolfMode.STAY_HOME.id().equals(read(wolf).getString(STANDING_MODE).orElse(null))
                ? WolfMode.STAY_HOME
                : WolfMode.FOLLOW;
    }

    public static void setStandingMode(Wolf wolf, WolfMode mode) {
        CompoundTag root = read(wolf);
        if (mode == WolfMode.STAY_HOME) {
            root.putString(STANDING_MODE, mode.id());
        } else {
            root.remove(STANDING_MODE); // FOLLOW is the default
        }
        write(wolf, root);
    }

    public static WolfStance getStance(Wolf wolf) {
        return read(wolf).getString(STANCE).map(WolfStance::byId).orElse(WolfStance.DEFAULT);
    }

    public static void setStance(Wolf wolf, WolfStance stance) {
        CompoundTag root = read(wolf);
        if (stance == WolfStance.DEFAULT) {
            root.remove(STANCE);
        } else {
            root.putString(STANCE, stance.id());
        }
        write(wolf, root);
    }

    /** True while the wolf walks/teleports back to its bed (it is invulnerable meanwhile). */
    public static boolean isReturning(Wolf wolf) {
        return read(wolf).getBooleanOr(RETURNING, false);
    }

    public static void setReturning(Wolf wolf, boolean returning) {
        setFlag(wolf, RETURNING, returning);
    }

    /** True if the wolf's vanilla "home" restriction was set by this mod (Stay Home). */
    public static boolean isHomeApplied(Wolf wolf) {
        return read(wolf).getBooleanOr(HOME_APPLIED, false);
    }

    public static void setHomeApplied(Wolf wolf, boolean applied) {
        setFlag(wolf, HOME_APPLIED, applied);
    }

    private static void setFlag(Wolf wolf, String key, boolean value) {
        CompoundTag root = read(wolf);
        if (value) {
            root.putBoolean(key, true);
        } else {
            root.remove(key);
        }
        write(wolf, root);
    }

    private static CompoundTag read(Wolf wolf) {
        return wolf.getPersistentData().getCompoundOrEmpty(ROOT);
    }

    private static void write(Wolf wolf, CompoundTag root) {
        CompoundTag persistent = wolf.getPersistentData();
        if (root.isEmpty()) {
            persistent.remove(ROOT);
        } else {
            persistent.put(ROOT, root);
        }
    }
}
