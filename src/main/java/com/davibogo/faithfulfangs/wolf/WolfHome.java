package com.davibogo.faithfulfangs.wolf;

import com.davibogo.faithfulfangs.bond.Bond;
import com.davibogo.faithfulfangs.bond.BondRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Stay Home mode: the wolf lives within {@link #HOME_RADIUS} blocks of its Dog Bed.
 * <p>
 * Uses the vanilla mob "home" restriction centered on the bed. With it, vanilla random strolling
 * stays inside the area, and the wolf only picks and fights targets inside it (this is how it
 * defends the house without running off after monsters).
 */
public final class WolfHome {
    /** Radius around the bed where a Stay Home wolf wanders and defends. */
    public static final int HOME_RADIUS = 10;
    /** Height of the bed cushion: where the wolf lands when it teleports home. */
    private static final double BED_SURFACE = 3.0 / 16.0;

    private WolfHome() {
    }

    public static boolean isStayHome(Wolf wolf) {
        return wolf.isTame() && WolfData.getStandingMode(wolf) == WolfMode.STAY_HOME;
    }

    /** The wolf's bond if it is in Stay Home mode and has a bed, otherwise null. */
    public static @Nullable Bond homeBond(Wolf wolf) {
        if (!isStayHome(wolf) || !(wolf.level() instanceof ServerLevel level)) {
            return null;
        }
        return BondRegistry.get(level.getServer()).getByWolf(wolf.getUUID());
    }

    public static boolean isInHomeDimension(Wolf wolf, Bond bond) {
        return bond.bed().dimension() == wolf.level().dimension();
    }

    public static Vec3 bedSurface(BlockPos bed) {
        return new Vec3(bed.getX() + 0.5, bed.getY() + BED_SURFACE, bed.getZ() + 0.5);
    }

    /**
     * Sets or clears the home restriction to match the current mode. Leashed wolves are left alone
     * (vanilla uses the same restriction for leashes).
     */
    public static void updateHome(Wolf wolf) {
        if (wolf.isLeashed()) {
            return;
        }
        Bond bond = homeBond(wolf);
        if (bond != null && isInHomeDimension(wolf, bond)) {
            BlockPos bed = bond.bed().pos();
            if (!wolf.hasHome() || !wolf.getHomePosition().equals(bed) || wolf.getHomeRadius() != HOME_RADIUS) {
                wolf.setHomeTo(bed, HOME_RADIUS);
            }
            if (!WolfData.isHomeApplied(wolf)) {
                WolfData.setHomeApplied(wolf, true);
            }
        } else if (WolfData.isHomeApplied(wolf)) {
            wolf.clearHome();
            WolfData.setHomeApplied(wolf, false);
        }
    }

    /**
     * Called when a wolf joins a level. A wolf that was returning home when it was saved, or that
     * just arrived through another dimension, stops being invulnerable here.
     */
    public static void onJoin(Wolf wolf) {
        if (WolfData.isReturning(wolf)) {
            wolf.setInvulnerableTime(0);
            WolfData.setReturning(wolf, false);
        }
        updateHome(wolf);
    }

    /**
     * Teleports the wolf onto its bed, in any dimension. The landing area stays loaded for a few
     * seconds (vanilla portal ticket), then unloads normally with the wolf saved in it.
     *
     * @return true if the wolf was sent home
     */
    public static boolean teleportHome(Wolf wolf, Bond bond) {
        if (!(wolf.level() instanceof ServerLevel from)) {
            return false;
        }
        MinecraftServer server = from.getServer();
        ServerLevel to = server.getLevel(bond.bed().dimension());
        if (to == null) {
            return false;
        }
        poof(from, wolf.position());
        Vec3 target = bedSurface(bond.bed().pos());
        wolf.getNavigation().stop();
        wolf.setTarget(null);
        Entity arrived = wolf.teleport(new TeleportTransition(to, target, Vec3.ZERO, wolf.getYRot(), 0.0F,
                TeleportTransition.PLACE_PORTAL_TICKET));
        if (arrived == null) {
            return false;
        }
        poof(to, target);
        return true;
    }

    private static void poof(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.POOF, pos.x, pos.y + 0.4, pos.z, 12, 0.3, 0.3, 0.3, 0.02);
    }
}
