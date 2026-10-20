package com.davibogo.faithfulfangs.wolf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Safety rules for tamed wolves: no walking into fire, no jumping down cliffs, no leaping into
 * lava or over edges, and no teleporting next to a creeper.
 */
public final class WolfSafety {
    /** Wolves keep at least this far from creepers. A creeper explosion hurts up to about 6 blocks away. */
    public static final float CREEPER_DISTANCE = 8.0F;
    /** How many path steps ahead are checked for a dangerous fall. */
    private static final int LOOK_AHEAD_NODES = 3;
    /** Leaps cover about 3 blocks; the ground is checked at these distances. */
    private static final int LEAP_LENGTH = 3;

    private WolfSafety() {
    }

    /** Called when a wolf joins a level: never path into fire, onto magma blocks or lit campfires. */
    public static void applyPathfindingCosts(Mob wolf) {
        wolf.setPathfindingMalus(PathType.FIRE, -1.0F);
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

    /** True if leaping at the target lands on safe ground: no drop, no lava, fire or powder snow. */
    public static boolean isSafeLeap(Mob mob, LivingEntity target) {
        if (target.getY() < mob.getY() - 1.5) {
            return false;
        }
        Vec3 direction = new Vec3(target.getX() - mob.getX(), 0.0, target.getZ() - mob.getZ());
        if (direction.lengthSqr() < 1.0E-7) {
            return true;
        }
        direction = direction.normalize();
        for (int step = 1; step <= LEAP_LENGTH; step++) {
            BlockPos feet = BlockPos.containing(mob.getX() + direction.x * step, mob.getY() + 0.1,
                    mob.getZ() + direction.z * step);
            if (!hasSafeGround(mob, feet)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasSafeGround(Mob mob, BlockPos feet) {
        BlockPos.MutableBlockPos pos = feet.mutable();
        int safeDrop = safeDrop(mob);
        for (int down = 0; down <= safeDrop; down++) {
            PathType type = WalkNodeEvaluator.getPathTypeStatic(mob, pos);
            switch (type) {
                case OPEN -> pos.move(Direction.DOWN);
                case LAVA, FIRE, FIRE_IN_NEIGHBOR, DAMAGING, POWDER_SNOW, ON_TOP_OF_POWDER_SNOW -> {
                    return false;
                }
                default -> {
                    return true; // ground, water, or a wall the leap will stop against
                }
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
        if (level.getBlockState(pos.below()).getBlock() instanceof LeavesBlock) {
            return false;
        }
        if (!level.noCollision(wolf, wolf.getBoundingBox().move(pos.subtract(wolf.blockPosition())))) {
            return false;
        }
        return !isNearCreeper(level, Vec3.atBottomCenterOf(pos), CREEPER_DISTANCE);
    }
}
