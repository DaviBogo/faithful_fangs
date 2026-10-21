package com.davibogo.faithfulfangs.wolf;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Safety rules for wolves: no walking into fire or lava, no jumping down cliffs, no fighting mobs
 * that stand in fire, and no teleporting next to a creeper.
 * "Hazard" below means fire, lava, magma blocks and lit campfires.
 */
public final class WolfSafety {
    /** Wolves keep at least this far from creepers. A creeper explosion hurts up to about 6 blocks away. */
    public static final float CREEPER_DISTANCE = 8.0F;
    /** How many path steps ahead are checked for a dangerous fall. */
    private static final int LOOK_AHEAD_NODES = 3;

    private WolfSafety() {
    }

    /** How far ahead (blocks) the wolf's next steps are checked for a hazard. */
    private static final double[] STEP_LOOK_AHEAD = {0.5, 0.9};

    /**
     * Called when a wolf joins a level: never path into fire, onto magma blocks or lit campfires,
     * and avoid walking right next to fire or lava when there is another way.
     */
    public static void applyPathfindingCosts(Mob wolf) {
        wolf.setPathfindingMalus(PathType.FIRE, -1.0F);
        wolf.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, 16.0F);
    }

    public static boolean isHazard(BlockState state) {
        return NodeEvaluator.isBurningBlock(state) || state.getFluidState().is(FluidTags.LAVA);
    }

    /** True if a hazard is within {@code radius} blocks sideways of {@code pos}, from {@code dyMin} to {@code dyMax} blocks up. */
    public static boolean isNearHazard(Level level, BlockPos pos, int radius, int dyMin, int dyMax) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-radius, dyMin, -radius), pos.offset(radius, dyMax, radius))) {
            if (isHazard(level.getBlockState(p))) {
                return true;
            }
        }
        return false;
    }

    /** A mob that is burning, or standing in or right next to fire or lava: fighting it would set the wolf on fire. */
    public static boolean isHazardousTarget(LivingEntity target) {
        return target.isOnFire() || target.isInLava() || isNearHazard(target.level(), target.blockPosition(), 1, -1, 0);
    }

    /** True if the wolf is standing in or right next to a hazard, in powder snow, or next to a cactus or berry bush. */
    public static boolean isInDanger(Mob mob) {
        if (mob.isInLava() || mob.isInPowderSnow || isNearHazard(mob.level(), mob.blockPosition(), 1, -1, 0)) {
            return true;
        }
        PathType type = WalkNodeEvaluator.getPathTypeStatic(mob, mob.blockPosition());
        return type == PathType.DAMAGING || type == PathType.DAMAGING_IN_NEIGHBOR;
    }

    /**
     * True if the wolf's next steps (where its path or its momentum takes it) end in fire or lava,
     * or above lava or fire it would fall into. Checked every tick; the last line of defense when
     * the path runs along a hazard on uneven ground, or the wolf gets pushed towards it.
     */
    public static boolean isHazardAhead(Mob mob) {
        Vec3 motion = mob.getDeltaMovement();
        if (isHazardAlong(mob, motion.x, motion.z)) {
            return true;
        }
        Path path = mob.getNavigation().getPath();
        if (path != null && !path.isDone()) {
            Vec3 next = path.getNextEntityPos(mob);
            return isHazardAlong(mob, next.x - mob.getX(), next.z - mob.getZ());
        }
        return false;
    }

    private static boolean isHazardAlong(Mob mob, double dx, double dz) {
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 1.0E-3) {
            return false;
        }
        dx /= length;
        dz /= length;
        double halfWidth = mob.getBbWidth() / 2.0;
        int feetY = Mth.floor(mob.getY() + 0.01);
        int maxDown = safeDrop(mob) + 1;
        for (double ahead : STEP_LOOK_AHEAD) {
            double x = mob.getX() + dx * ahead;
            double z = mob.getZ() + dz * ahead;
            for (int cx = -1; cx <= 1; cx += 2) {
                for (int cz = -1; cz <= 1; cz += 2) {
                    if (isHazardColumn(mob.level(), Mth.floor(x + cx * halfWidth), feetY, Mth.floor(z + cz * halfWidth), maxDown)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Looks down from the wolf's head height: a hazard before solid ground means danger. */
    private static boolean isHazardColumn(Level level, int x, int feetY, int z, int maxDown) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = feetY + 1; y >= feetY - maxDown; y--) {
            pos.set(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (isHazard(state)) {
                return true;
            }
            if (!state.getCollisionShape(level, pos).isEmpty()) {
                return false;
            }
        }
        return false;
    }

    /** Highest drop (in blocks) the wolf can take without getting hurt. */
    public static int safeDrop(Mob mob) {
        return Mth.floor(mob.getAttributeValue(Attributes.SAFE_FALL_DISTANCE));
    }

    /**
     * True if the next steps of the mob's current path include a fall it would get hurt by (landing
     * in water is fine). Without a target, vanilla never plans such falls; while fighting, it plans
     * falls of 20 blocks or more.
     */
    public static boolean hasDangerousDropAhead(Mob mob) {
        Path path = mob.getNavigation().getPath();
        if (path == null || path.isDone()) {
            return false;
        }
        int safeDrop = safeDrop(mob);
        int from = Math.max(0, path.getNextNodeIndex() - 1);
        int to = Math.min(path.getNodeCount() - 1, path.getNextNodeIndex() + LOOK_AHEAD_NODES);
        for (int i = from; i < to; i++) {
            Node node = path.getNode(i);
            Node next = path.getNode(i + 1);
            if (node.y - next.y > safeDrop && next.type != PathType.WATER) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNearCreeper(Level level, Vec3 pos, double radius) {
        AABB area = AABB.ofSize(pos, radius * 2, radius * 2, radius * 2);
        return !level.getEntitiesOfClass(Creeper.class, area,
                creeper -> creeper.isAlive() && creeper.distanceToSqr(pos) <= radius * radius).isEmpty();
    }

    /**
     * Teleports the wolf to a safe spot 2-3 blocks from {@code target}, like vanilla does when a wolf
     * is far from its owner, but never next to a creeper.
     *
     * @return true if the wolf was moved
     */
    public static boolean teleportNear(TamableAnimal wolf, BlockPos target) {
        RandomSource random = wolf.getRandom();
        for (int attempt = 0; attempt < 10; attempt++) {
            int dx = random.nextIntBetweenInclusive(-3, 3);
            int dz = random.nextIntBetweenInclusive(-3, 3);
            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                continue;
            }
            BlockPos pos = target.offset(dx, random.nextIntBetweenInclusive(-1, 1), dz);
            if (canTeleportTo(wolf, pos)) {
                wolf.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, wolf.getYRot(), wolf.getXRot());
                wolf.getNavigation().stop();
                return true;
            }
        }
        return false;
    }

    /** Vanilla's teleport checks (walkable, not next to fire or lava, not on leaves, room) plus creepers. */
    private static boolean canTeleportTo(TamableAnimal wolf, BlockPos pos) {
        Level level = wolf.level();
        if (WalkNodeEvaluator.getPathTypeStatic(wolf, pos) != PathType.WALKABLE) {
            return false;
        }
        // Vanilla's check can miss lava when water is next to the spot too.
        if (isNearHazard(level, pos, 1, -1, 1)) {
            return false;
        }
        if (level.getBlockState(pos.below()).getBlock() instanceof LeavesBlock) {
            return false;
        }
        if (!level.noCollision(wolf, wolf.getBoundingBox().move(pos.subtract(wolf.blockPosition())))) {
            return false;
        }
        return !isNearCreeper(level, Vec3.atBottomCenterOf(pos), CREEPER_DISTANCE);
    }
}
