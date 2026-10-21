package com.davibogo.faithfulfangs.wolf;

import com.davibogo.faithfulfangs.FaithfulFangs;
import com.davibogo.faithfulfangs.bond.BondRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.jspecify.annotations.Nullable;

/**
 * Scent (Phase 3): while its owner explores, a bonded wolf in Follow mode now and then digs up
 * something and drops it at its feet: a common find from {@code faithful_fangs:scent/common} or,
 * more rarely, a special bone from {@code faithful_fangs:scent/rare}. Both lists depend on where
 * the wolf is (data pack loot tables).
 * <p>
 * Finds are paid in distance, not time: every 5 seconds the owner's straight-line progress is
 * measured (small moves, like building or standing still, count as nothing; fast travel counts as
 * a run). Each wolf counts down its own distance to the next find; the countdowns are saved on the
 * wolf. Monsters the wolf kills speed up the countdown to the next special bone for a while.
 */
public final class WolfScent {
    public static final ResourceKey<LootTable> COMMON_FINDS = lootTable("scent/common");
    public static final ResourceKey<LootTable> RARE_FINDS = lootTable("scent/rare");

    /** How often (ticks) the owner's progress is measured. */
    public static final int SAMPLE_TICKS = 100;
    /** The owner must be at most this far from the wolf for it to sniff and to drop a find. */
    public static final double OWNER_RANGE = 24.0;
    /** Progress below this in one sample (5 s) counts as nothing: building, standing around, AFK. */
    private static final double MIN_PROGRESS = 2.0;
    /** Progress above this in one sample counts as this (6 blocks/s, a sprint): riding, flying or teleporting give no more. */
    private static final double MAX_PROGRESS = 30.0;
    /** Average exploring distance before a find: about 5 and 40 minutes at 3 blocks/s (walking with stops). */
    private static final float COMMON_DISTANCE = 900.0F;
    private static final float RARE_DISTANCE = 7200.0F;
    /** Each find comes after 50% to 150% of the average distance. */
    private static final float SPREAD = 0.5F;
    /** Each monster the wolf kills makes the countdown to the next special bone 25% faster... */
    private static final float KILL_BONUS = 0.25F;
    /** ...up to 3 times as fast... */
    private static final float MAX_KILL_BONUS = 2.0F;
    /** ...and the bonus is lost 10 minutes after the last kill (or when the special bone is found). */
    private static final long KILL_BONUS_TICKS = 12000L;

    private WolfScent() {
    }

    private static ResourceKey<LootTable> lootTable(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(FaithfulFangs.MODID, path));
    }

    // ------------------------------------------------------------ who sniffs

    /**
     * The owner, if the wolf can sniff right now: tamed, bonded, in Follow mode and not sitting,
     * with its owner alive, playing (not spectating) and within {@link #OWNER_RANGE} in the same level.
     */
    public static @Nullable ServerPlayer sniffingOwner(Wolf wolf) {
        if (!wolf.isTame() || !wolf.isAlive() || wolf.isOrderedToSit()
                || WolfData.getStandingMode(wolf) != WolfMode.FOLLOW
                || !(wolf.level() instanceof ServerLevel level)
                || BondRegistry.get(level.getServer()).getByWolf(wolf.getUUID()) == null) {
            return null;
        }
        if (!(wolf.getOwner() instanceof ServerPlayer owner) || owner.level() != level || !owner.isAlive()
                || owner.isSpectator() || owner.distanceToSqr(wolf) > OWNER_RANGE * OWNER_RANGE) {
            return null;
        }
        return owner;
    }

    // ------------------------------------------------------------ countdown

    /** Counts {@code progress} blocks of exploring towards both finds (more with the Scent upgrade). */
    public static void addProgress(Wolf wolf, double progress) {
        if (progress < MIN_PROGRESS) {
            return;
        }
        float blocks = (float) Math.min(progress, MAX_PROGRESS) * WolfUpgrades.scentMultiplier(wolf);
        float rareBlocks = blocks * (1.0F + killBonus(wolf));
        WolfData.setScentDistance(wolf, false, Math.max(0.0F, distanceLeft(wolf, false) - blocks));
        WolfData.setScentDistance(wolf, true, Math.max(0.0F, distanceLeft(wolf, true) - rareBlocks));
    }

    /** Blocks still to explore before the next find (0 = due); rolled the first time it is asked. */
    public static float distanceLeft(Wolf wolf, boolean rare) {
        return WolfData.getScentDistance(wolf, rare).orElseGet(() -> {
            float distance = rollDistance(wolf.getRandom(), rare);
            WolfData.setScentDistance(wolf, rare, distance);
            return distance;
        });
    }

    /** Makes a find due now (debug command). */
    public static void makeDue(Wolf wolf, boolean rare) {
        WolfData.setScentDistance(wolf, rare, 0.0F);
    }

    private static float rollDistance(RandomSource random, boolean rare) {
        float average = rare ? RARE_DISTANCE : COMMON_DISTANCE;
        return average * (1.0F - SPREAD + random.nextFloat() * 2.0F * SPREAD);
    }

    // ------------------------------------------------------------ kill bonus

    /** Monitor listener of LivingDeathEvent: a tamed wolf killed a monster. */
    public static void onLivingDeath(LivingDeathEvent event, boolean cancelled) {
        if (!cancelled && event.getEntity() instanceof Enemy
                && event.getSource().getEntity() instanceof Wolf wolf
                && wolf.isTame() && wolf.level() instanceof ServerLevel level) {
            float bonus = Math.min(MAX_KILL_BONUS, killBonus(wolf) + KILL_BONUS);
            WolfData.setKillBonus(wolf, bonus, level.getServer().overworld().getGameTime());
        }
    }

    /** Current kill bonus (0 if the last kill was too long ago). */
    public static float killBonus(Wolf wolf) {
        float bonus = WolfData.getKillBonus(wolf);
        if (bonus > 0.0F && wolf.level() instanceof ServerLevel level) {
            long sinceLastKill = level.getServer().overworld().getGameTime() - WolfData.getLastKillTime(wolf);
            if (sinceLastKill < 0 || sinceLastKill > KILL_BONUS_TICKS) {
                WolfData.setKillBonus(wolf, 0.0F, 0L);
                return 0.0F;
            }
        }
        return bonus;
    }

    // ------------------------------------------------------------ finding

    /** Whether the wolf may stop and dig right now: on the ground, calm and safe. */
    public static boolean canDig(Wolf wolf) {
        return wolf.onGround() && !wolf.isInWater() && !wolf.isInLava() && !wolf.isOnFire()
                && !wolf.isPassenger() && wolf.getTarget() == null && !WolfSafety.isInDanger(wolf);
    }

    /**
     * If a find is due (the special bone first) and the wolf can dig, drops it at the wolf's feet,
     * tells the owner ("Rex found something!") and starts the next countdown. Returns the dropped
     * items (empty if nothing was found).
     */
    public static List<ItemEntity> tryFind(Wolf wolf) {
        boolean rare = distanceLeft(wolf, true) <= 0.0F;
        if (!rare && distanceLeft(wolf, false) > 0.0F || !canDig(wolf) || !(wolf.level() instanceof ServerLevel level)) {
            return List.of();
        }
        List<ItemEntity> drops = dig(level, wolf, rare ? RARE_FINDS : COMMON_FINDS, rare);
        WolfData.setScentDistance(wolf, rare, rollDistance(wolf.getRandom(), rare));
        if (rare) {
            WolfData.setKillBonus(wolf, 0.0F, 0L);
        }
        if (!drops.isEmpty() && wolf.getOwner() instanceof ServerPlayer owner) {
            // Above the owner's hotbar, so a find is not missed even if the barks are.
            owner.sendOverlayMessage(Component.translatable(rare
                    ? "message.faithful_fangs.wolf_found_special" : "message.faithful_fangs.wolf_found_something",
                    wolf.getName()));
        }
        return drops;
    }

    private static List<ItemEntity> dig(ServerLevel level, Wolf wolf, ResourceKey<LootTable> table, boolean rare) {
        List<ItemEntity> drops = new ArrayList<>();
        RandomSource random = wolf.getRandom();
        wolf.dropFromGiftLootTable(level, table, (lootLevel, stack) -> {
            ItemEntity item = new ItemEntity(lootLevel, wolf.getX(), wolf.getY() + 0.1, wolf.getZ(), stack,
                    (random.nextDouble() - 0.5) * 0.1, 0.25, (random.nextDouble() - 0.5) * 0.1);
            item.setDefaultPickUpDelay();
            if (lootLevel.addFreshEntity(item)) {
                drops.add(item);
            }
        });
        if (drops.isEmpty()) {
            return drops;
        }
        // A little dirt (or sand, snow...) flies as the wolf digs the find out.
        BlockState ground = wolf.getBlockStateOn();
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                    wolf.getX(), wolf.getY() + 0.1, wolf.getZ(), 16, 0.25, 0.05, 0.25, 0.15);
        }
        if (rare) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, wolf.getX(), wolf.getY() + 0.5, wolf.getZ(),
                    12, 0.5, 0.4, 0.5, 0.0);
        }
        return drops;
    }
}
