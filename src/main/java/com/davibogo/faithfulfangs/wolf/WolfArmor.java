package com.davibogo.faithfulfangs.wolf;

import com.davibogo.faithfulfangs.item.WolfArmorTier;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * Behavior of the Faithful Fangs wolf armor tiers ({@link WolfArmorTier}).
 * <ul>
 *   <li>Absorption: exactly like the vanilla Wolf Armor, which the game only does for its own item:
 *       while worn, every hit (except the kinds that bypass wolf armor: drowning, starving, magic,
 *       wither, thorns, the void...) wears the armor down by its damage instead of hurting the
 *       wolf, with the same crack stages and sounds.</li>
 *   <li>Golden armor: piglins treat the owner as if they wore gold while this wolf is within
 *       {@link #GOLDEN_RANGE} blocks. Piglins that are angry at the owner still attack, and piglin
 *       brutes stay hostile, as with real gold armor.</li>
 * </ul>
 */
public final class WolfArmor {
    public static final double GOLDEN_RANGE = 16.0;

    private WolfArmor() {
    }

    /** LivingHurtEvent listener: returns true (no damage to the wolf) when its armor takes the hit. */
    public static boolean onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf) || !(wolf.level() instanceof ServerLevel level)) {
            return false;
        }
        ItemStack armor = wolf.getBodyArmorItem();
        WolfArmorTier tier = WolfArmorTier.of(armor);
        if (tier == null || event.getSource().is(DamageTypeTags.BYPASSES_WOLF_ARMOR)) {
            return false;
        }
        int damageBefore = armor.getDamageValue();
        int maxDamage = armor.getMaxDamage();
        armor.hurtAndBreak(Mth.ceil(event.getAmount()), wolf, EquipmentSlot.BODY);
        wolf.playSound(SoundEvents.WOLF_ARMOR_DAMAGE, 1.0F, 1.0F);
        if (Crackiness.WOLF_ARMOR.byDamage(damageBefore, maxDamage) != Crackiness.WOLF_ARMOR.byDamage(wolf.getBodyArmorItem())) {
            wolf.playSound(SoundEvents.WOLF_ARMOR_CRACK, 1.0F, 1.0F);
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, tier.crackParticle()),
                    wolf.getX(), wolf.getY() + 1.0, wolf.getZ(), 20, 0.2, 0.1, 0.2, 0.1);
        }
        return true;
    }

    /** LivingChangeTargetEvent listener: returns true (cancels) when a piglin should leave the owner alone. */
    public static boolean onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Piglin piglin) || !(event.getNewTarget() instanceof Player player)
                || !(piglin.level() instanceof ServerLevel)) {
            return false;
        }
        Optional<UUID> angryAt = piglin.getBrain().getMemory(MemoryModuleType.ANGRY_AT);
        if (angryAt.isPresent() && angryAt.get().equals(player.getUUID())) {
            return false;
        }
        return hasGoldenWolfNearby(player);
    }

    private static boolean hasGoldenWolfNearby(Player player) {
        return !player.level().getEntitiesOfClass(Wolf.class, player.getBoundingBox().inflate(GOLDEN_RANGE),
                wolf -> wolf.isAlive() && wolf.isTame() && wolf.isOwnedBy(player)
                        && WolfArmorTier.of(wolf.getBodyArmorItem()) == WolfArmorTier.GOLDEN).isEmpty();
    }
}
