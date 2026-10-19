package com.davibogo.faithfulfangs.wolf;

import java.util.Locale;

/**
 * Combat stance chosen in the Wolf Panel. Also applies in Stay Home mode.
 * <ul>
 *   <li>PASSIVE — never attacks; only runs away when targeted.</li>
 *   <li>NEUTRAL — vanilla: attacks what the owner attacks and defends the owner.</li>
 *   <li>AGGRESSIVE — attacks any monster nearby, even before the owner does.</li>
 * </ul>
 */
public enum WolfStance {
    PASSIVE,
    NEUTRAL,
    AGGRESSIVE;

    public static final WolfStance DEFAULT = NEUTRAL;

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "wolf_stance.faithful_fangs." + this.id();
    }

    public static WolfStance byId(String id) {
        for (WolfStance stance : values()) {
            if (stance.id().equals(id)) {
                return stance;
            }
        }
        return DEFAULT;
    }

    public static WolfStance byOrdinal(int ordinal) {
        WolfStance[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : DEFAULT;
    }
}
