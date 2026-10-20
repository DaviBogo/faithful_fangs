package com.davibogo.faithfulfangs.command;

import com.davibogo.faithfulfangs.block.DogBedBlockEntity;
import com.davibogo.faithfulfangs.bond.Bond;
import com.davibogo.faithfulfangs.bond.BondManager;
import com.davibogo.faithfulfangs.bond.BondRegistry;
import com.davibogo.faithfulfangs.bond.Recovery;
import com.davibogo.faithfulfangs.item.EngravedTag;
import com.davibogo.faithfulfangs.wolf.WolfData;
import com.davibogo.faithfulfangs.wolf.WolfRecovery;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * {@code /faithfulfangs debug} — operator-only tools to inspect and test the bond data while the
 * mod is being developed. Output is plain English text (not translated) on purpose.
 * <ul>
 *   <li>{@code /faithfulfangs debug} — list all bonds and describe the wolf or Dog Bed you are looking at</li>
 *   <li>{@code /faithfulfangs debug bind <name>} — bind the wolf you are looking at to the nearest free Dog Bed</li>
 *   <li>{@code /faithfulfangs debug unbind} — remove the bond of the wolf or Dog Bed you are looking at</li>
 *   <li>{@code /faithfulfangs debug return} — every recovering wolf comes back to its bed now</li>
 * </ul>
 */
public final class FaithfulFangsCommand {
    private static final double REACH = 8.0;
    private static final int BED_SEARCH_RADIUS = 16;

    private FaithfulFangsCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("faithfulfangs")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("debug")
                        .executes(context -> info(context.getSource()))
                        .then(Commands.literal("bind")
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(context -> bind(context.getSource(),
                                                StringArgumentType.getString(context, "name")))))
                        .then(Commands.literal("unbind")
                                .executes(context -> unbind(context.getSource())))
                        .then(Commands.literal("return")
                                .executes(context -> returnAll(context.getSource())))));
    }

    private static int info(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        MinecraftServer server = source.getServer();
        BondRegistry registry = BondRegistry.get(server);

        send(source, header("Bonds in this world: " + registry.all().size()));
        for (Bond bond : registry.all()) {
            boolean loaded = BondManager.findLoadedWolf(server, bond.wolfId()) != null;
            send(source, Component.literal(" - " + bond.wolfName() + ": bed " + describe(bond.bed())
                    + ", owner " + ownerName(server, bond.ownerId())
                    + ", wolf " + (loaded ? "loaded" : "not loaded") + " [" + shortId(bond.wolfId()) + "]"));
        }

        long now = server.overworld().getGameTime();
        if (!registry.recoveries().isEmpty()) {
            send(source, header("Recovering: " + registry.recoveries().size()));
            for (Recovery recovery : registry.recoveries()) {
                long seconds = Math.max(0L, recovery.returnTime() - now) / 20L;
                send(source, Component.literal(" - " + recovery.wolfName() + ": back in " + seconds + " s"
                        + ", owner " + ownerName(server, recovery.ownerId())
                        + (registry.getByWolf(recovery.wolfId()) == null ? ", bed gone" : "")
                        + " [" + shortId(recovery.wolfId()) + "]"));
            }
        }

        Wolf wolf = lookedAtWolf(player);
        if (wolf != null) {
            Bond bond = registry.getByWolf(wolf.getUUID());
            send(source, header("Wolf [" + shortId(wolf.getUUID()) + "]"));
            send(source, Component.literal(" tamed: " + wolf.isTame()
                    + (wolf.getOwnerReference() != null ? ", owner " + ownerName(server, wolf.getOwnerReference().getUUID()) : "")));
            send(source, Component.literal(" bond: " + (bond == null ? "none" : bond.wolfName() + " at " + describe(bond.bed()))));
            send(source, Component.literal(" name on wolf: " + (wolf.getCustomName() == null ? "none" : wolf.getCustomName().getString())
                    + ", name applied by bond: " + nullToNone(WolfData.getBondName(wolf))));
            return 1;
        }

        BlockPos bedPos = lookedAtBlock(player);
        if (bedPos != null && source.getLevel().getBlockEntity(bedPos) instanceof DogBedBlockEntity bed) {
            Bond bond = registry.getByBed(GlobalPos.of(source.getLevel().dimension(), bedPos));
            send(source, header("Dog Bed at " + bedPos.toShortString()));
            send(source, Component.literal(" bed copy: " + (bed.isBound() ? bed.getWolfName() + " [" + shortId(bed.getWolfId()) + "]" : "free")));
            send(source, Component.literal(" registry: " + (bond == null ? "free" : bond.wolfName() + " [" + shortId(bond.wolfId()) + "]")));
        }
        return 1;
    }

    private static int returnAll(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        int count = 0;
        for (Recovery recovery : BondRegistry.get(server).recoveries()) {
            if (WolfRecovery.returnNow(server, recovery.wolfId())) {
                count++;
            }
        }
        int returning = count;
        source.sendSuccess(() -> Component.literal(returning == 0
                ? "No wolf is recovering." : returning + " wolf(s) coming back within a second."), false);
        return count;
    }

    private static int bind(CommandSourceStack source, String rawName) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Wolf wolf = lookedAtWolf(player);
        if (wolf == null) {
            return fail(source, "Look at a wolf (within " + (int) REACH + " blocks).");
        }
        if (!wolf.isTame() || !wolf.isOwnedBy(player)) {
            return fail(source, "That wolf is not yours.");
        }
        String name = rawName.trim();
        if (name.isEmpty() || name.length() > EngravedTag.MAX_NAME_LENGTH) {
            return fail(source, "The name must have 1 to " + EngravedTag.MAX_NAME_LENGTH + " characters.");
        }

        ServerLevel level = source.getLevel();
        BlockPos bedPos = findNearestFreeBed(level, player.blockPosition(), wolf.getUUID());
        if (bedPos == null) {
            return fail(source, "No free Dog Bed within " + BED_SEARCH_RADIUS + " blocks.");
        }

        BondManager.BindResult result = BondManager.bind(level, bedPos, wolf.getUUID(), player.getUUID(), name);
        if (!result.success()) {
            return fail(source, "That bed already belongs to " + result.takenBy().wolfName() + ".");
        }
        String moved = result.previous() != null && !result.previous().bed().pos().equals(bedPos)
                ? " The old bed at " + describe(result.previous().bed()) + " is free now."
                : "";
        send(source, Component.literal("Bound " + name + " to the Dog Bed at " + bedPos.toShortString() + "." + moved)
                .withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static int unbind(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        MinecraftServer server = source.getServer();

        UUID wolfId = null;
        Wolf wolf = lookedAtWolf(player);
        if (wolf != null) {
            wolfId = wolf.getUUID();
        } else {
            BlockPos bedPos = lookedAtBlock(player);
            if (bedPos != null) {
                Bond bond = BondRegistry.get(server).getByBed(GlobalPos.of(source.getLevel().dimension(), bedPos));
                if (bond != null) {
                    wolfId = bond.wolfId();
                }
            }
        }
        if (wolfId == null) {
            return fail(source, "Look at a bound wolf or a bound Dog Bed.");
        }

        Bond removed = BondManager.unbind(server, wolfId);
        if (removed == null) {
            return fail(source, "That wolf has no bed.");
        }
        send(source, Component.literal("Removed the bond of " + removed.wolfName() + " (bed at "
                + describe(removed.bed()) + ").").withStyle(ChatFormatting.YELLOW));
        return 1;
    }

    private static @Nullable Wolf lookedAtWolf(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 reach = player.getViewVector(1.0F).scale(REACH);
        AABB area = player.getBoundingBox().expandTowards(reach).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, eye.add(reach), area,
                entity -> entity instanceof Wolf, REACH * REACH);
        return hit != null && hit.getEntity() instanceof Wolf wolf ? wolf : null;
    }

    private static @Nullable BlockPos lookedAtBlock(ServerPlayer player) {
        HitResult hit = player.pick(REACH, 1.0F, false);
        return hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit ? blockHit.getBlockPos() : null;
    }

    /** Nearest Dog Bed around {@code center} that is free or already belongs to {@code wolfId}. */
    private static @Nullable BlockPos findNearestFreeBed(ServerLevel level, BlockPos center, UUID wolfId) {
        BondRegistry registry = BondRegistry.get(level.getServer());
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-BED_SEARCH_RADIUS, -BED_SEARCH_RADIUS, -BED_SEARCH_RADIUS),
                center.offset(BED_SEARCH_RADIUS, BED_SEARCH_RADIUS, BED_SEARCH_RADIUS))) {
            if (!(level.getBlockEntity(pos) instanceof DogBedBlockEntity)) {
                continue;
            }
            Bond occupant = registry.getByBed(GlobalPos.of(level.dimension(), pos));
            if (occupant != null && !occupant.wolfId().equals(wolfId)) {
                continue;
            }
            double distance = pos.distSqr(center);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = pos.immutable();
            }
        }
        return best;
    }

    private static String describe(GlobalPos pos) {
        return pos.pos().toShortString() + " (" + pos.dimension().identifier() + ")";
    }

    private static String ownerName(MinecraftServer server, UUID ownerId) {
        ServerPlayer owner = server.getPlayerList().getPlayer(ownerId);
        return owner != null ? owner.getGameProfile().name() : shortId(ownerId);
    }

    private static String shortId(@Nullable UUID id) {
        return id == null ? "none" : id.toString().substring(0, 8);
    }

    private static String nullToNone(@Nullable String value) {
        return value == null ? "none" : value;
    }

    private static MutableComponent header(String text) {
        return Component.literal("[Faithful Fangs] " + text).withStyle(ChatFormatting.GOLD);
    }

    private static void send(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> message, false);
    }

    private static int fail(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal(message));
        return 0;
    }
}
