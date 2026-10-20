package com.davibogo.faithfulfangs.wolf.ai;

import com.davibogo.faithfulfangs.wolf.WolfData;
import com.davibogo.faithfulfangs.wolf.WolfMode;
import com.davibogo.faithfulfangs.wolf.WolfSafety;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jspecify.annotations.Nullable;

/**
 * Replaces the vanilla panic from fire, lava, cactus, freezing... (all wolves). Vanilla runs to
 * random spots, sometimes back into the fire, over and over. Instead the wolf:
 * <ul>
 *   <li>(Follow mode, owner 12+ blocks away) teleports to a safe spot by its owner, like vanilla;</li>
 *   <li>if it is burning and there is water within 8 blocks, goes into the water;</li>
 *   <li>if it stands in or right next to the danger, walks to the nearest spot at least 2 blocks
 *       away from any fire or lava, and stays there until the fire on it goes out.</li>
 * </ul>
 */
public class FireEscapeGoal extends Goal {
    private static final double SPEED = 1.5;
    private static final int WATER_SEARCH = 8;
    private static final int SAFE_SEARCH = 8;
    /** Paths checked at most per search (pathfinding is costly). */
    private static final int MAX_PATH_TRIES = 12;
    private static final double TELEPORT_DISTANCE_SQR = 12.0 * 12.0;
    /** After a search that found nothing, wait this long before searching again. */
    private static final int SEARCH_COOLDOWN = 20;

    private final Wolf wolf;
    private @Nullable Path path;
    private int nextSearchTick;

    public FireEscapeGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.isHurtByEnvironment() && !this.wolf.isOnFire() && !WolfSafety.isInDanger(this.wolf)) {
            return false;
        }
        if (this.tryTeleportToOwner() || this.wolf.tickCount < this.nextSearchTick) {
            return false;
        }
        this.path = null;
        if (this.wolf.isOnFire() && !this.wolf.isInLava() && !this.wolf.isInWater()) {
            this.path = this.findPath(this.wolf.level(), WATER_SEARCH, true);
        }
        if (this.path == null && WolfSafety.isInDanger(this.wolf)) {
            this.path = this.findPath(this.wolf.level(), SAFE_SEARCH, false);
        }
        if (this.path == null) {
            this.nextSearchTick = this.wolf.tickCount + SEARCH_COOLDOWN;
        }
        return this.path != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.wolf.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.wolf.getNavigation().moveTo(this.path, SPEED);
    }

    @Override
    public void stop() {
        this.path = null;
    }

    private boolean isHurtByEnvironment() {
        DamageSource source = this.wolf.getLastDamageSource();
        return source != null && source.is(DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES);
    }

    private boolean tryTeleportToOwner() {
        if (!this.wolf.isTame() || WolfData.getStandingMode(this.wolf) != WolfMode.FOLLOW
                || this.wolf.unableToMoveToOwner()) {
            return false;
        }
        LivingEntity owner = this.wolf.getOwner();
        return owner != null && this.wolf.distanceToSqr(owner) >= TELEPORT_DISTANCE_SQR
                && WolfSafety.teleportNear(this.wolf, owner.blockPosition());
    }

    /** Nearest reachable water (to put the fire out) or nearest reachable safe spot. */
    private @Nullable Path findPath(Level level, int range, boolean water) {
        BlockPos from = this.wolf.blockPosition();
        int tries = 0;
        for (BlockPos pos : BlockPos.withinBoxByManhattanDistance(from, range, 2, range)) {
            boolean suitable = water ? level.getFluidState(pos).is(FluidTags.WATER) : this.isSafeSpot(level, pos);
            if (!suitable) {
                continue;
            }
            Path candidate = this.wolf.getNavigation().createPath(pos.immutable(), 0);
            if (candidate != null && candidate.canReach()) {
                return candidate;
            }
            if (++tries >= MAX_PATH_TRIES) {
                return null;
            }
        }
        return null;
    }

    private boolean isSafeSpot(Level level, BlockPos pos) {
        return WalkNodeEvaluator.getPathTypeStatic(this.wolf, pos) == PathType.WALKABLE
                && !WolfSafety.isNearHazard(level, pos, 2, -1, 1);
    }
}
