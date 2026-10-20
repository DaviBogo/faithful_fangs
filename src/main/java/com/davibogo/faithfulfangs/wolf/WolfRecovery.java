package com.davibogo.faithfulfangs.wolf;

import com.davibogo.faithfulfangs.FaithfulFangs;
import com.davibogo.faithfulfangs.block.DogBedBlock;
import com.davibogo.faithfulfangs.bond.Bond;
import com.davibogo.faithfulfangs.bond.BondManager;
import com.davibogo.faithfulfangs.bond.BondRegistry;
import com.davibogo.faithfulfangs.bond.Recovery;
import com.davibogo.faithfulfangs.wolf.ai.PassiveNoTargetGoal;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.jspecify.annotations.Nullable;

/**
 * Respawn (Phase 2): a bonded wolf that dies does not die. It vanishes in a puff and comes back to
 * its bed right away, with half a heart, in Stay Home mode, and the owner is told.
 * <p>
 * If the bed area is not loaded, it is loaded for a moment (the same short ticket a portal uses);
 * meanwhile the whole wolf is kept in the bond registry, so it is never lost, even if the game
 * closes. Once the area's mobs are loaded the wolf is added there and later saved with that area
 * like any other mob.
 */
public final class WolfRecovery {
    /** Half a heart. */
    private static final float RETURN_HEALTH = 1.0F;
    /** Ask again for the bed area if it is still not loaded after this long. */
    private static final long LOADING_RETRY = 200L;

    private WolfRecovery() {
    }

    // ------------------------------------------------------------ death

    /** LivingDeathEvent listener. Returns true (cancels the death) for a bonded wolf. */
    public static boolean onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf) || !(wolf.level() instanceof ServerLevel level) || !wolf.isTame()) {
            return false;
        }
        MinecraftServer server = level.getServer();
        BondRegistry registry = BondRegistry.get(server);
        Bond bond = registry.getByWolf(wolf.getUUID());
        if (bond == null || registry.getRecovery(wolf.getUUID()) != null) {
            return false;
        }

        playWhine(level, wolf, wolf.position());
        level.broadcastEntityEvent(wolf, net.minecraft.world.entity.EntityEvent.POOF);

        prepareForRest(wolf);
        CompoundTag data = save(level, wolf);
        registry.putRecovery(new Recovery(wolf.getUUID(), bond.ownerId(), bond.wolfName(), bond.bed(), data));
        wolf.discard();
        return true;
    }

    /** Leaves the wolf as it should be when it comes back: calm, standing, Stay Home, half a heart. */
    private static void prepareForRest(Wolf wolf) {
        if (wolf.isLeashed()) {
            wolf.dropLeash();
        }
        wolf.ejectPassengers();
        wolf.stopRiding();
        wolf.removeAllEffects();
        wolf.clearFire();
        wolf.resetFallDistance();
        wolf.setDeltaMovement(Vec3.ZERO);
        wolf.setAirSupply(wolf.getMaxAirSupply());
        PassiveNoTargetGoal.calmDown(wolf);
        wolf.setOrderedToSit(false);
        wolf.setInSittingPose(false);
        WolfData.setStandingMode(wolf, WolfMode.STAY_HOME);
        WolfData.setReturning(wolf, false);
        wolf.setHealth(RETURN_HEALTH);
    }

    private static CompoundTag save(ServerLevel level, Wolf wolf) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        wolf.saveWithoutId(output);
        return output.buildResult();
    }

    // ------------------------------------------------------------ return

    /** Server tick listener: brings the recovering wolves back as soon as their bed area is loaded. */
    public static void onServerTick(TickEvent.ServerTickEvent.Post event) {
        MinecraftServer server = event.server();
        BondRegistry registry = BondRegistry.get(server);
        if (registry.recoveries().isEmpty()) {
            return;
        }
        long now = server.overworld().getGameTime();
        for (Recovery recovery : registry.recoveries()) {
            tryReturn(server, registry, recovery, now);
        }
    }

    private static void tryReturn(MinecraftServer server, BondRegistry registry, Recovery recovery, long now) {
        Bond bond = registry.getByWolf(recovery.wolfId());
        GlobalPos home = bond != null ? bond.bed() : recovery.lastBed();
        ServerLevel level = server.getLevel(home.dimension());
        if (level == null) {
            FaithfulFangs.LOGGER.warn("Can't bring back {}: dimension {} is missing", recovery.wolfName(), home.dimension());
            return;
        }

        // Load the bed area for a moment and wait until its mobs are loaded before adding the wolf.
        ChunkPos chunk = ChunkPos.containing(home.pos());
        Map<UUID, Long> loading = registry.bedLoadingSince();
        if (!level.areEntitiesLoaded(chunk.pack())) {
            Long since = loading.get(recovery.wolfId());
            if (since == null || now - since >= LOADING_RETRY || now < since) {
                level.getChunkSource().addTicketWithRadius(TicketType.PORTAL, chunk, 3);
                loading.put(recovery.wolfId(), now);
            }
            return;
        }

        Wolf wolf = load(level, recovery.data());
        if (wolf == null) {
            FaithfulFangs.LOGGER.error("Could not bring back {}: its saved data is unreadable", recovery.wolfName());
            registry.removeRecovery(recovery.wolfId());
            return;
        }
        boolean onBed = bond != null;
        Vec3 spot = onBed ? WolfHome.bedSurface(home.pos()) : Vec3.atBottomCenterOf(home.pos());
        wolf.snapTo(spot.x, spot.y, spot.z, bedYaw(level, home.pos(), wolf.getYRot()), 0.0F);
        wolf.setHealth(RETURN_HEALTH);
        wolf.setDeltaMovement(Vec3.ZERO);
        wolf.resetFallDistance();
        wolf.clearFire();
        if (!level.addFreshEntity(wolf)) {
            if (BondManager.findLoadedWolf(server, recovery.wolfId()) != null) {
                // It is already back (should not happen): keep that one.
                FaithfulFangs.LOGGER.warn("{} is already in the world; dropping its saved copy", recovery.wolfName());
                registry.removeRecovery(recovery.wolfId());
            }
            return; // otherwise try again next tick
        }
        registry.removeRecovery(recovery.wolfId());

        level.sendParticles(ParticleTypes.POOF, spot.x, spot.y + 0.4, spot.z, 12, 0.3, 0.3, 0.3, 0.02);
        playWhine(level, wolf, spot);
        String name = bond != null ? bond.wolfName() : recovery.wolfName();
        tellOwner(server, recovery.ownerId(), Component.translatable(onBed
                ? "message.faithful_fangs.wolf_back_home" : "message.faithful_fangs.wolf_back_no_bed", name));
    }

    private static @Nullable Wolf load(ServerLevel level, CompoundTag data) {
        return EntityType.create(net.minecraft.world.entity.EntityTypes.WOLF,
                        TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), data),
                        level, EntitySpawnReason.LOAD)
                .filter(Wolf.class::isInstance)
                .map(Wolf.class::cast)
                .orElse(null);
    }

    private static float bedYaw(ServerLevel level, BlockPos pos, float fallback) {
        BlockState state = level.getBlockState(pos);
        return state.hasProperty(DogBedBlock.FACING) ? state.getValue(DogBedBlock.FACING).toYRot() : fallback;
    }

    // ------------------------------------------------------------ helpers

    private static void playWhine(ServerLevel level, Wolf wolf, Vec3 pos) {
        Holder<WolfSoundVariant> variant = wolf.get(DataComponents.WOLF_SOUND_VARIANT);
        if (variant == null) {
            return;
        }
        WolfSoundVariant.WolfSoundSet sounds = wolf.isBaby() ? variant.value().babySounds() : variant.value().adultSounds();
        Holder<SoundEvent> whine = sounds.whineSound();
        level.playSound(null, pos.x, pos.y, pos.z, whine, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    private static void tellOwner(MinecraftServer server, UUID ownerId, Component message) {
        ServerPlayer owner = server.getPlayerList().getPlayer(ownerId);
        if (owner != null) {
            owner.sendSystemMessage(message);
        }
    }
}
