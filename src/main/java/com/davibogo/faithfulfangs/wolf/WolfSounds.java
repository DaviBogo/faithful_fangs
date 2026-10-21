package com.davibogo.faithfulfangs.wolf;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** The wolf's own sounds (they depend on its sound variant and on whether it is a puppy). */
public final class WolfSounds {
    private WolfSounds() {
    }

    /** The vanilla bark (the wolf's ambient sound), a bit louder than usual so the owner hears it. */
    public static void bark(Wolf wolf) {
        WolfSoundVariant.WolfSoundSet sounds = sounds(wolf);
        if (sounds != null) {
            wolf.playSound(sounds.ambientSound().value(), 1.5F, wolf.getVoicePitch());
        }
    }

    /** The whine, played at {@code pos} (the wolf may be about to vanish or just appeared). */
    public static void whine(ServerLevel level, Wolf wolf, Vec3 pos) {
        WolfSoundVariant.WolfSoundSet sounds = sounds(wolf);
        if (sounds != null) {
            level.playSound(null, pos.x, pos.y, pos.z, sounds.whineSound(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
    }

    private static WolfSoundVariant.@Nullable WolfSoundSet sounds(Wolf wolf) {
        Holder<WolfSoundVariant> variant = wolf.get(DataComponents.WOLF_SOUND_VARIANT);
        if (variant == null) {
            return null;
        }
        return wolf.isBaby() ? variant.value().babySounds() : variant.value().adultSounds();
    }
}
