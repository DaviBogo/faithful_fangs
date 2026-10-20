package com.davibogo.faithfulfangs.wolf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Follow mode: when the owner changes dimension (Nether, End, or any modded dimension), the wolves
 * following them come along and appear next to them.
 * <p>
 * Wolves are picked right before the owner leaves (standing, in Follow mode, not leashed or riding,
 * within {@link #RADIUS} blocks) and teleported once the owner has arrived. The first exit from
 * the End goes through the credits and a respawn instead of a normal dimension change; that case
 * is handled with the clone/respawn events.
 */
public final class FollowAcrossDimensions {
    private static final double RADIUS = 24.0;

    /** Wolves waiting to be brought to their owner (server thread only). */
    private static final Map<UUID, List<Wolf>> PENDING = new HashMap<>();

    private FollowAcrossDimensions() {
    }

    /** Before a player changes dimension through a portal or teleport. */
    public static void onTravelToDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getDimension() != player.level().dimension()) {
            capture(player, player);
        }
    }

    /** The player arrived in the new dimension. */
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            bringWolves(player);
        }
    }

    /** First End exit: the old player object still knows where it was in the End. */
    public static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() && event.getEntity() instanceof ServerPlayer player
                && event.getOriginal() instanceof ServerPlayer original
                && original.level().dimension() != player.level().dimension()) {
            capture(original, player);
        }
    }

    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.isEndConquered() && event.getEntity() instanceof ServerPlayer player) {
            bringWolves(player);
        }
    }

    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PENDING.remove(event.getEntity().getUUID());
    }

    /** Picks the wolves following {@code where} (the player as they leave) for {@code owner}. */
    private static void capture(ServerPlayer where, ServerPlayer owner) {
        List<Wolf> wolves = where.level().getEntitiesOfClass(Wolf.class,
                where.getBoundingBox().inflate(RADIUS), wolf -> isFollowing(wolf, owner));
        if (wolves.isEmpty()) {
            PENDING.remove(owner.getUUID());
        } else {
            PENDING.put(owner.getUUID(), wolves);
        }
    }

    private static void bringWolves(ServerPlayer player) {
        List<Wolf> wolves = PENDING.remove(player.getUUID());
        if (wolves == null) {
            return;
        }
        ServerLevel to = player.level();
        Vec3 position = player.position();
        for (Wolf wolf : wolves) {
            if (wolf.isRemoved() || wolf.level() == to || !isFollowing(wolf, player)) {
                continue;
            }
            wolf.teleport(new TeleportTransition(to, position, Vec3.ZERO, wolf.getYRot(), wolf.getXRot(),
                    TeleportTransition.DO_NOTHING));
        }
    }

    private static boolean isFollowing(Wolf wolf, Player owner) {
        return wolf.isAlive()
                && wolf.isTame()
                && wolf.isOwnedBy(owner)
                && !wolf.isOrderedToSit()
                && WolfData.getStandingMode(wolf) == WolfMode.FOLLOW
                && !wolf.isLeashed()
                && !wolf.isPassenger();
    }
}
