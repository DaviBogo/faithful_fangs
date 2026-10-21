package com.davibogo.faithfulfangs.wolf;

import java.util.Optional;
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
    private static final String SCENT_COMMON = "scent_common";
    private static final String SCENT_RARE = "scent_rare";
    private static final String SCENT_KILL_BONUS = "scent_kill_bonus";
    private static final String UPGRADES = "upgrades";
    private static final String SCENT_LAST_KILL = "scent_last_kill";

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

    /**
     * Scent: how far (in blocks) the owner still has to explore with the wolf before its next
     * common find ({@code rare} false) or special bone ({@code rare} true). Empty until first rolled.
     */
    public static Optional<Float> getScentDistance(Wolf wolf, boolean rare) {
        return read(wolf).getFloat(rare ? SCENT_RARE : SCENT_COMMON);
    }

    public static void setScentDistance(Wolf wolf, boolean rare, float distance) {
        CompoundTag root = read(wolf);
        root.putFloat(rare ? SCENT_RARE : SCENT_COMMON, distance);
        write(wolf, root);
    }

    /** Scent: bonus from monsters the wolf killed (0.25 = the next special bone comes 25% sooner). */
    public static float getKillBonus(Wolf wolf) {
        return read(wolf).getFloatOr(SCENT_KILL_BONUS, 0.0F);
    }

    /** Game time of the wolf's last monster kill that counted for the bonus. */
    public static long getLastKillTime(Wolf wolf) {
        return read(wolf).getLongOr(SCENT_LAST_KILL, 0L);
    }

    public static void setKillBonus(Wolf wolf, float bonus, long lastKillTime) {
        CompoundTag root = read(wolf);
        if (bonus <= 0.0F) {
            root.remove(SCENT_KILL_BONUS);
            root.remove(SCENT_LAST_KILL);
        } else {
            root.putFloat(SCENT_KILL_BONUS, bonus);
            root.putLong(SCENT_LAST_KILL, lastKillTime);
        }
        write(wolf, root);
    }

    /** Upgrade level (0 to 5) bought in the Wolf Panel. */
    public static int getUpgradeLevel(Wolf wolf, WolfUpgrade upgrade) {
        return read(wolf).getCompoundOrEmpty(UPGRADES).getIntOr(upgrade.id(), 0);
    }

    public static void setUpgradeLevel(Wolf wolf, WolfUpgrade upgrade, int level) {
        CompoundTag root = read(wolf);
        CompoundTag upgrades = root.getCompoundOrEmpty(UPGRADES);
        if (level <= 0) {
            upgrades.remove(upgrade.id());
        } else {
            upgrades.putInt(upgrade.id(), level);
        }
        if (upgrades.isEmpty()) {
            root.remove(UPGRADES);
        } else {
            root.put(UPGRADES, upgrades);
        }
        write(wolf, root);
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
