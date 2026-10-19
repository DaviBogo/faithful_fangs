package com.davibogo.faithfulfangs.client;

import com.davibogo.faithfulfangs.FaithfulFangs;
import com.davibogo.faithfulfangs.menu.WolfPanelMenu;
import com.davibogo.faithfulfangs.wolf.WolfMode;
import com.davibogo.faithfulfangs.wolf.WolfStance;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

/**
 * Wolf Panel screen: wolf preview, health in hearts, bed location, and the mode and stance buttons.
 * Room is left for the upgrades (phase 4) and the armor slot (phase 5).
 */
public class WolfPanelScreen extends AbstractContainerScreen<WolfPanelMenu> {
    private static final Identifier BACKGROUND =
            Identifier.fromNamespaceAndPath(FaithfulFangs.MODID, "textures/gui/wolf_panel.png");
    private static final Identifier HEART_CONTAINER = Identifier.withDefaultNamespace("hud/heart/container");
    private static final Identifier HEART_FULL = Identifier.withDefaultNamespace("hud/heart/full");
    private static final Identifier HEART_HALF = Identifier.withDefaultNamespace("hud/heart/half");

    private static final int WIDTH = 206;
    private static final int HEIGHT = 150;
    /** Vanilla container label color. */
    private static final int LABEL_COLOR = 0xFF404040;
    private static final int RIGHT_COLUMN_X = 68;
    private static final int MODE_LABEL_Y = 78;
    private static final int MODE_BUTTONS_Y = 88;
    private static final int STANCE_LABEL_Y = 114;
    private static final int STANCE_BUTTONS_Y = 124;
    private static final int BUTTON_WIDTH = 62;
    private static final int BUTTON_GAP = 3;

    private final Map<WolfMode, Button> modeButtons = new EnumMap<>(WolfMode.class);
    private final Map<WolfStance, Button> stanceButtons = new EnumMap<>(WolfStance.class);
    private float xMouse;
    private float yMouse;

    public WolfPanelScreen(WolfPanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        this.modeButtons.clear();
        this.stanceButtons.clear();

        int x = this.leftPos + 7;
        for (WolfMode mode : WolfMode.values()) {
            int buttonId = WolfPanelMenu.MODE_BUTTONS + mode.ordinal();
            this.modeButtons.put(mode, this.addRenderableWidget(
                    Button.builder(Component.translatable(mode.translationKey()), button -> this.sendClick(buttonId))
                            .bounds(x, this.topPos + MODE_BUTTONS_Y, BUTTON_WIDTH, 20)
                            .build()));
            x += BUTTON_WIDTH + BUTTON_GAP;
        }

        x = this.leftPos + 7;
        for (WolfStance stance : WolfStance.values()) {
            int buttonId = WolfPanelMenu.STANCE_BUTTONS + stance.ordinal();
            this.stanceButtons.put(stance, this.addRenderableWidget(
                    Button.builder(Component.translatable(stance.translationKey()), button -> this.sendClick(buttonId))
                            .bounds(x, this.topPos + STANCE_BUTTONS_Y, BUTTON_WIDTH, 20)
                            .tooltip(Tooltip.create(Component.translatable(stance.translationKey() + ".tooltip")))
                            .build()));
            x += BUTTON_WIDTH + BUTTON_GAP;
        }
        this.updateButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.updateButtons();
    }

    /** Highlights the current mode and stance; Stay Home needs a bed. */
    private void updateButtons() {
        WolfMode currentMode = this.menu.getMode();
        for (Map.Entry<WolfMode, Button> entry : this.modeButtons.entrySet()) {
            WolfMode mode = entry.getKey();
            Button button = entry.getValue();
            button.setMessage(label(mode.translationKey(), mode == currentMode));
            boolean available = mode != WolfMode.STAY_HOME || this.menu.hasBed();
            button.active = available;
            button.setTooltip(Tooltip.create(Component.translatable(
                    available ? mode.translationKey() + ".tooltip" : mode.translationKey() + ".needs_bed")));
        }
        WolfStance currentStance = this.menu.getStance();
        for (Map.Entry<WolfStance, Button> entry : this.stanceButtons.entrySet()) {
            entry.getValue().setMessage(label(entry.getKey().translationKey(), entry.getKey() == currentStance));
        }
    }

    private static Component label(String key, boolean selected) {
        MutableComponent text = Component.translatable(key);
        return selected ? text.withStyle(ChatFormatting.YELLOW) : text;
    }

    private void sendClick(int buttonId) {
        if (this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.xMouse = mouseX;
        this.yMouse = mouseY;
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, 256, 256);
        LivingEntity wolf = this.findWolf();
        if (wolf != null) {
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                    this.leftPos + 8, this.topPos + 18, this.leftPos + 60, this.topPos + 70,
                    30, 0.0625F, this.xMouse, this.yMouse, wolf);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, LABEL_COLOR, false);
        this.extractHearts(graphics);
        this.extractBed(graphics);
        graphics.text(this.font, Component.translatable("screen.faithful_fangs.wolf_panel.mode"),
                8, MODE_LABEL_Y, LABEL_COLOR, false);
        graphics.text(this.font, Component.translatable("screen.faithful_fangs.wolf_panel.stance"),
                8, STANCE_LABEL_Y, LABEL_COLOR, false);
    }

    /** One heart per 2 health points, 10 per row, like the player's health bar. */
    private void extractHearts(GuiGraphicsExtractor graphics) {
        int health = this.menu.getHealth();
        int hearts = (this.menu.getMaxHealth() + 1) / 2;
        for (int i = 0; i < hearts; i++) {
            int x = RIGHT_COLUMN_X + (i % 10) * 8;
            int y = 19 + (i / 10) * 10;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_CONTAINER, x, y, 9, 9);
            int points = health - i * 2;
            if (points >= 2) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_FULL, x, y, 9, 9);
            } else if (points == 1) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_HALF, x, y, 9, 9);
            }
        }
    }

    private void extractBed(GuiGraphicsExtractor graphics) {
        WolfPanelMenu.BedInfo bed = this.menu.getBed();
        int y = 44;
        if (bed == null || !this.menu.hasBed()) {
            graphics.text(this.font, Component.translatable("screen.faithful_fangs.wolf_panel.no_bed"),
                    RIGHT_COLUMN_X, y, LABEL_COLOR, false);
            return;
        }
        graphics.text(this.font, Component.translatable("screen.faithful_fangs.wolf_panel.bed", dimensionName(bed.dimension())),
                RIGHT_COLUMN_X, y, LABEL_COLOR, false);
        BlockPos pos = bed.pos();
        graphics.text(this.font, Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ()),
                RIGHT_COLUMN_X, y + 10, LABEL_COLOR, false);
    }

    private static Component dimensionName(Identifier dimension) {
        if ("minecraft".equals(dimension.getNamespace())) {
            switch (dimension.getPath()) {
                case "overworld", "the_nether", "the_end" -> {
                    return Component.translatable("screen.faithful_fangs.wolf_panel.dimension." + dimension.getPath());
                }
                default -> {
                }
            }
        }
        return Component.literal(dimension.toString());
    }

    private @Nullable LivingEntity findWolf() {
        if (this.minecraft.level == null) {
            return null;
        }
        return this.minecraft.level.getEntity(this.menu.getWolfEntityId()) instanceof LivingEntity living ? living : null;
    }
}
