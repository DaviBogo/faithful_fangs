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
 * pack or modded variant), so the mod ships no copy of Mojang's textures.
 * <ul>
 * <li>Adult (64 px wide texture): each eye is an "L" of 2x2 pixels on rows 5-6 of the face. The
 * upper pixel gets the fur color right above it and the lower row becomes the eyelid line, in the
 * wolf's own pupil color.</li>
 * <li>Puppy (32 px wide texture): each eye is one pixel on row 19. The pixel next to it, towards
 * the edge of the face, takes the pupil color too, so the eye becomes a short eyelid line.</li>
 * </ul>
 * Higher resolution textures are handled by scaling those coordinates.
 * <p>
 * The generated textures are dropped on every resource reload and made again when needed.
 */
public final class ClosedEyesTextures implements ResourceManagerReloadListener {
    public static final ClosedEyesTextures INSTANCE = new ClosedEyesTextures();

    /** Vanilla adult texture width the adult eye coordinates refer to. */
    private static final int ADULT_BASE_WIDTH = 64;
    /** Adult, for each eye: first column of the eye and the column of its pupil. */
    private static final int[][] ADULT_EYES = {{4, 5}, {8, 8}};
    private static final int ADULT_UPPER_ROW = 5;
    private static final int ADULT_LOWER_ROW = 6;

    /** Vanilla puppy texture width the puppy eye coordinates refer to. */
    private static final int BABY_BASE_WIDTH = 32;
    /** Puppy, for each eye: column of the eye and of the pixel that joins it to make the eyelid. */
    private static final int[][] BABY_EYES = {{6, 5}, {9, 10}};
    private static final int BABY_ROW = 19;

    private record Key(Identifier source, boolean baby) {
    }

    private final Map<Key, Identifier> cache = new HashMap<>();

    private ClosedEyesTextures() {
    }

    /**
     * The closed-eyes version of {@code source}, or {@code source} itself if it cannot be made.
     *
     * @param baby true for a puppy texture (different face layout)
     */
    public Identifier get(Identifier source, boolean baby) {
        return this.cache.computeIfAbsent(new Key(source, baby), ClosedEyesTextures::create);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        Minecraft minecraft = Minecraft.getInstance();
        for (Map.Entry<Key, Identifier> entry : this.cache.entrySet()) {
            if (!entry.getValue().equals(entry.getKey().source())) {
                minecraft.getTextureManager().release(entry.getValue());
            }
        }
        this.cache.clear();
    }

    private static Identifier create(Key key) {
        Minecraft minecraft = Minecraft.getInstance();
        Identifier source = key.source();
        Identifier target = Identifier.fromNamespaceAndPath(FaithfulFangs.MODID,
                "closed_eyes/" + (key.baby() ? "baby/" : "adult/") + source.getNamespace() + "/" + source.getPath());
        try (InputStream in = minecraft.getResourceManager().open(source)) {
            NativeImage image = NativeImage.read(in);
            if (key.baby()) {
                closeBabyEyes(image);
            } else {
                closeAdultEyes(image);
            }
            minecraft.getTextureManager().register(target, new DynamicTexture(target::toString, image));
            return target;
        } catch (Exception e) {
            FaithfulFangs.LOGGER.warn("Could not make a closed-eyes texture from {}", source, e);
            return source;
        }
    }

    private static void closeAdultEyes(NativeImage image) {
        int scale = Math.max(1, image.getWidth() / ADULT_BASE_WIDTH);
        for (int[] eye : ADULT_EYES) {
            int lid = image.getPixel(eye[1] * scale, ADULT_LOWER_ROW * scale);
            for (int x = eye[0]; x <= eye[0] + 1; x++) {
                int furAbove = image.getPixel(x * scale, (ADULT_UPPER_ROW - 1) * scale);
                fill(image, x, ADULT_UPPER_ROW, scale, furAbove);
                fill(image, x, ADULT_LOWER_ROW, scale, lid);
            }
        }
    }

    private static void closeBabyEyes(NativeImage image) {
        int scale = Math.max(1, image.getWidth() / BABY_BASE_WIDTH);
        for (int[] eye : BABY_EYES) {
            int lid = image.getPixel(eye[0] * scale, BABY_ROW * scale);
            fill(image, eye[1], BABY_ROW, scale, lid);
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
