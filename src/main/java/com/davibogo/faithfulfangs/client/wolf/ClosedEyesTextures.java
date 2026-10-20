package com.davibogo.faithfulfangs.client.wolf;

import com.davibogo.faithfulfangs.FaithfulFangs;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * Closed-eyes versions of wolf textures, used while a wolf sleeps in its Dog Bed.
 * <p>
 * They are made at runtime from whatever texture the wolf has (any vanilla variant, resource
 * pack or modded variant), so the mod ships no copy of Mojang's textures. On the vanilla wolf head
 * each eye is an "L" of 2x2 pixels on rows 5-6 of the face: the upper pixel gets the fur color
 * right above it and the lower row becomes the eyelid line, in the wolf's own pupil color.
 * Higher resolution textures are handled by scaling those coordinates.
 * <p>
 * The generated textures are dropped on every resource reload and made again when needed.
 */
public final class ClosedEyesTextures implements ResourceManagerReloadListener {
    public static final ClosedEyesTextures INSTANCE = new ClosedEyesTextures();

    /** Vanilla wolf texture width the eye coordinates refer to. */
    private static final int BASE_WIDTH = 64;
    /** For each eye: first column of the eye and the column of its pupil. */
    private static final int[][] EYES = {{4, 5}, {8, 8}};
    private static final int UPPER_ROW = 5;
    private static final int LOWER_ROW = 6;

    private final Map<Identifier, Identifier> cache = new HashMap<>();

    private ClosedEyesTextures() {
    }

    /** The closed-eyes version of {@code source}, or {@code source} itself if it cannot be made. */
    public Identifier get(Identifier source) {
        return this.cache.computeIfAbsent(source, ClosedEyesTextures::create);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        Minecraft minecraft = Minecraft.getInstance();
        for (Map.Entry<Identifier, Identifier> entry : this.cache.entrySet()) {
            if (!entry.getValue().equals(entry.getKey())) {
                minecraft.getTextureManager().release(entry.getValue());
            }
        }
        this.cache.clear();
    }

    private static Identifier create(Identifier source) {
        Minecraft minecraft = Minecraft.getInstance();
        Identifier target = Identifier.fromNamespaceAndPath(FaithfulFangs.MODID,
                "closed_eyes/" + source.getNamespace() + "/" + source.getPath());
        try (InputStream in = minecraft.getResourceManager().open(source)) {
            NativeImage image = NativeImage.read(in);
            closeEyes(image);
            minecraft.getTextureManager().register(target, new DynamicTexture(target::toString, image));
            return target;
        } catch (Exception e) {
            FaithfulFangs.LOGGER.warn("Could not make a closed-eyes texture from {}", source, e);
            return source;
        }
    }

    private static void closeEyes(NativeImage image) {
        int scale = Math.max(1, image.getWidth() / BASE_WIDTH);
        for (int[] eye : EYES) {
            int lid = image.getPixel(eye[1] * scale, LOWER_ROW * scale);
            for (int x = eye[0]; x <= eye[0] + 1; x++) {
                int furAbove = image.getPixel(x * scale, (UPPER_ROW - 1) * scale);
                fill(image, x, UPPER_ROW, scale, furAbove);
                fill(image, x, LOWER_ROW, scale, lid);
            }
        }
    }

    private static void fill(NativeImage image, int x, int y, int scale, int color) {
        for (int dx = 0; dx < scale; dx++) {
            for (int dy = 0; dy < scale; dy++) {
                image.setPixel(x * scale + dx, y * scale + dy, color);
            }
        }
    }
}
