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

    private WolfData() {
    }

    /**
     * The name this mod last gave the wolf through its bond, or null if the wolf has no bond
     * applied. Used to tell our name apart from one the player set later with a vanilla Name Tag.
     */
    public static @Nullable String getBondName(Wolf wolf) {
        CompoundTag root = wolf.getPersistentData().getCompoundOrEmpty(ROOT);
        return root.getString(BOND_NAME).orElse(null);
    }

    public static void setBondName(Wolf wolf, @Nullable String name) {
        CompoundTag persistent = wolf.getPersistentData();
        CompoundTag root = persistent.getCompoundOrEmpty(ROOT);
        if (name == null) {
            root.remove(BOND_NAME);
        } else {
            root.putString(BOND_NAME, name);
        }
        if (root.isEmpty()) {
            persistent.remove(ROOT);
        } else {
            persistent.put(ROOT, root);
        }
    }
}
