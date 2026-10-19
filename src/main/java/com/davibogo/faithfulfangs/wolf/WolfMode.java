package com.davibogo.faithfulfangs.wolf;

import java.util.Locale;
import net.minecraft.world.entity.animal.wolf.Wolf;

/**
 * Movement mode chosen in the Wolf Panel.
 * <p>
 * SIT is the vanilla "ordered to sit" state, so the normal right click still makes the wolf sit
 * or stand up. The mod stores separately the mode the wolf goes back to when it stands up
 * (FOLLOW or STAY_HOME); see {@link WolfData#getStandingMode}.
 */
public enum WolfMode {
    SIT,
    STAY_HOME,
    FOLLOW;

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "wolf_mode.faithful_fangs." + this.id();
    }

    public static WolfMode current(Wolf wolf) {
        return wolf.isOrderedToSit() ? SIT : WolfData.getStandingMode(wolf);
    }

    public static WolfMode byOrdinal(int ordinal) {
        WolfMode[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : FOLLOW;
    }
}
