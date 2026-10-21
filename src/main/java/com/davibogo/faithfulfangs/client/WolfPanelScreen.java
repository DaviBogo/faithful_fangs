package com.davibogo.faithfulfangs.client;

import com.davibogo.faithfulfangs.FaithfulFangs;
import com.davibogo.faithfulfangs.menu.WolfPanelMenu;
import com.davibogo.faithfulfangs.wolf.WolfMode;
import com.davibogo.faithfulfangs.wolf.WolfStance;
import com.davibogo.faithfulfangs.wolf.WolfUpgrade;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Wolf Panel screen (262x170, about the size of the horse screen).
 * <ul>
 *   <li>Left: the armor slot beside the wolf preview, health in hearts and the bed location; the
 *       player's inventory right below.</li>
 *   <li>Right column: big Mode and Stance buttons, for quick use. The "Upgrades..." button swaps
 *       them for the upgrades (Health, Strength, Agility, Scent: level squares colored by the bone
 *       each level took, and a button with the bones the next level costs); "Back" swaps back.</li>
 * </ul>
 */
public class WolfPanelScreen extends AbstractContainerScreen<WolfPanelMenu> {
    private static final Identifier BACKGROUND =
            Identifier.fromNamespaceAndPath(FaithfulFangs.MODID, "textures/gui/wolf_panel.png");
    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 256;
    private static final Identifier HEART_CONTAINER = Identifier.withDefaultNamespace("hud/heart/container");
    private static final Identifier HEART_FULL = Identifier.withDefaultNamespace("hud/heart/full");
    private static final Identifier HEART_HALF = Identifier.withDefaultNamespace("hud/heart/half");

    private static final int WIDTH = 262;
    private static final int HEIGHT = 170;
    /** Width of the left part (wolf and inventory), like a vanilla inventory. */
    private static final int LEFT_WIDTH = 176;
    /** Vanilla container label color. */
    private static final int LABEL_COLOR = 0xFF404040;
    private static final int SLEEPING_COLOR = 0xFF3F5FA0;
    private static final int MAX_COLOR = 0xFFFFAA00;
    /** Hearts and bed, right of the wolf preview. */
    private static final int INFO_X = 88;
    /** Inside of the wolf preview box. */
    private static final int PREVIEW_X0 = 28;
    private static final int PREVIEW_X1 = 80;
    private static final int INVENTORY_LABEL_Y = 74;

    // Right column.
    private static final int COLUMN_X = 181;
    private static final int COLUMN_WIDTH = 74;
    private static final int BUTTON_HEIGHT = 18;
    private static final int BUTTON_STEP = 19;
    private static final int MODE_LABEL_Y = 6;
    private static final int MODE_BUTTONS_Y = 16;
    private static final int STANCE_LABEL_Y = 76;
    private static final int STANCE_BUTTONS_Y = 86;
    private static final int TOGGLE_Y = 146;
    // Upgrades view of the right column: per upgrade, icon and name, then level squares and the button.
    private static final int UPGRADE_ROWS_Y = 16;
    private static final int UPGRADE_ROW_STEP = 31;
    private static final int UPGRADE_BUTTON_WIDTH = 24;
    private static final int UPGRADE_BUTTON_X = COLUMN_X + COLUMN_WIDTH - UPGRADE_BUTTON_WIDTH;

    /** Sprites in the panel texture, right of the panel. */
    private static final int SPRITES_U = 268;
    private static final int ICONS_V = 0;
    private static final int ICON_SIZE = 9;
    private static final int PIPS_V = 12;
    private static final int PIP_SIZE = 6;

    private final Map<WolfMode, Button> modeButtons = new EnumMap<>(WolfMode.class);
    private final Map<WolfStance, Button> stanceButtons = new EnumMap<>(WolfStance.class);
    private final Map<WolfUpgrade, UpgradeButton> upgradeButtons = new EnumMap<>(WolfUpgrade.class);
    private @Nullable Button toggleButton;
    /** Right column shows the upgrades instead of Mode and Stance. */
    private boolean showUpgrades;
    private float xMouse;
    private float yMouse;

    public WolfPanelScreen(WolfPanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
        this.inventoryLabelY = INVENTORY_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
        this.modeButtons.clear();
        this.stanceButtons.clear();
        this.upgradeButtons.clear();
        int x = this.leftPos + COLUMN_X;

        int y = this.topPos + MODE_BUTTONS_Y;
        for (WolfMode mode : WolfMode.values()) {
            int buttonId = WolfPanelMenu.MODE_BUTTONS + mode.ordinal();
            this.modeButtons.put(mode, this.addRenderableWidget(
                    Button.builder(Component.translatable(mode.translationKey()), button -> this.sendClick(buttonId))
                            .bounds(x, y, COLUMN_WIDTH, BUTTON_HEIGHT)
                            .build()));
            y += BUTTON_STEP;
        }

        y = this.topPos + STANCE_BUTTONS_Y;
        for (WolfStance stance : WolfStance.values()) {
            int buttonId = WolfPanelMenu.STANCE_BUTTONS + stance.ordinal();
            this.stanceButtons.put(stance, this.addRenderableWidget(
                    Button.builder(Component.translatable(stance.translationKey()), button -> this.sendClick(buttonId))
                            .bounds(x, y, COLUMN_WIDTH, BUTTON_HEIGHT)
                            .tooltip(Tooltip.create(Component.translatable(stance.translationKey() + ".tooltip")))
                            .build()));
            y += BUTTON_STEP;
        }

        for (WolfUpgrade upgrade : WolfUpgrade.values()) {
            int buttonId = WolfPanelMenu.UPGRADE_BUTTONS + upgrade.ordinal();
            this.upgradeButtons.put(upgrade, this.addRenderableWidget(new UpgradeButton(
                    this.leftPos + UPGRADE_BUTTON_X, this.topPos + rowY(upgrade) + 10,
                    Component.translatable(upgrade.translationKey()), button -> this.sendClick(buttonId))));
        }

        this.toggleButton = this.addRenderableWidget(Button.builder(Component.empty(), button -> {
                    this.showUpgrades = !this.showUpgrades;
                    this.updateButtons();
                })
                .bounds(x, this.topPos + TOGGLE_Y, COLUMN_WIDTH, BUTTON_HEIGHT)
                .build());
        this.updateButtons();
    }

    private static int rowY(WolfUpgrade upgrade) {
        return UPGRADE_ROWS_Y + upgrade.ordinal() * UPGRADE_ROW_STEP;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.updateButtons();
    }

    /**
     * Shows Mode and Stance or the upgrades; highlights the current mode and stance; Stay Home
     * needs a bed; upgrade buttons show the next price.
     */
    private void updateButtons() {
        if (this.toggleButton != null) {
            this.toggleButton.setMessage(Component.translatable(this.showUpgrades
                    ? "screen.faithful_fangs.wolf_panel.back" : "screen.faithful_fangs.wolf_panel.upgrades_button"));
        }
        WolfMode currentMode = this.menu.getMode();
        for (Map.Entry<WolfMode, Button> entry : this.modeButtons.entrySet()) {
            WolfMode mode = entry.getKey();
            Button button = entry.getValue();
            button.visible = !this.showUpgrades;
            button.setMessage(label(mode.translationKey(), mode == currentMode));
            boolean available = mode != WolfMode.STAY_HOME || this.menu.hasBed();
            button.active = available;
            button.setTooltip(Tooltip.create(Component.translatable(
                    available ? mode.translationKey() + ".tooltip" : mode.translationKey() + ".needs_bed")));
        }
        WolfStance currentStance = this.menu.getStance();
        for (Map.Entry<WolfStance, Button> entry : this.stanceButtons.entrySet()) {
            entry.getValue().visible = !this.showUpgrades;
            entry.getValue().setMessage(label(entry.getKey().translationKey(), entry.getKey() == currentStance));
        }

        Player player = this.minecraft.player;
        for (Map.Entry<WolfUpgrade, UpgradeButton> entry : this.upgradeButtons.entrySet()) {
            WolfUpgrade upgrade = entry.getKey();
            UpgradeButton button = entry.getValue();
            int level = this.menu.getUpgradeLevel(upgrade);
            button.visible = this.showUpgrades && level < WolfUpgrade.MAX_LEVEL;
            if (!button.visible || player == null) {
                continue;
            }
            WolfUpgrade.Cost cost = WolfUpgrade.cost(level + 1);
            button.setCost(cost.item(), cost.count());
            button.active = cost.canPay(player);
            button.setTooltip(Tooltip.create(upgradeTooltip(upgrade, level, cost, player)));
        }
    }

    /** "Upgrade Strength to level 2" / "+1 attack damage, total +2" / "Costs 2 Ancient Bones". */
    private static Component upgradeTooltip(WolfUpgrade upgrade, int level, WolfUpgrade.Cost cost, Player player) {
        Component name = Component.translatable(upgrade.translationKey());
        MutableComponent text = Component.translatable("screen.faithful_fangs.wolf_panel.upgrade.next", name, level + 1);
        text.append("\n").append(Component.translatable(upgrade.translationKey() + ".gain", upgrade.displayTotal(level + 1))
                .withStyle(ChatFormatting.GRAY));
        Component bones = boneName(cost.item(), cost.count());
        if (cost.canPay(player)) {
            text.append("\n").append(Component.translatable("screen.faithful_fangs.wolf_panel.upgrade.cost", cost.count(), bones)
                    .withStyle(ChatFormatting.GRAY));
        } else {
            text.append("\n").append(Component.translatable("screen.faithful_fangs.wolf_panel.upgrade.missing",
                    cost.count(), bones, WolfUpgrade.countBones(player, cost.item())).withStyle(ChatFormatting.RED));
        }
        return text;
    }

    private static Component boneName(Item bone, int count) {
        String key = bone.getDescriptionId();
        return Component.translatable(count == 1 ? key : key + ".plural");
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
                this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        LivingEntity wolf = this.findWolf();
        if (wolf != null) {
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                    this.leftPos + PREVIEW_X0, this.topPos + 18, this.leftPos + PREVIEW_X1, this.topPos + 70,
                    30, 0.0625F, this.xMouse, this.yMouse, wolf);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, LABEL_COLOR, false);
        if (this.menu.isSleeping()) {
            Component sleeping = Component.translatable("screen.faithful_fangs.wolf_panel.sleeping");
            graphics.text(this.font, sleeping, LEFT_WIDTH - 8 - this.font.width(sleeping), this.titleLabelY, SLEEPING_COLOR, false);
        }
        int heartRows = this.extractHearts(graphics);
        this.extractBed(graphics, Math.max(44, 19 + heartRows * 10 + 3));
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, LABEL_COLOR, false);
        if (this.showUpgrades) {
            this.extractUpgrades(graphics);
        } else {
            graphics.text(this.font, Component.translatable("screen.faithful_fangs.wolf_panel.mode"),
                    COLUMN_X + 1, MODE_LABEL_Y, LABEL_COLOR, false);
            graphics.text(this.font, Component.translatable("screen.faithful_fangs.wolf_panel.stance"),
                    COLUMN_X + 1, STANCE_LABEL_Y, LABEL_COLOR, false);
        }
    }

    /** One heart per 2 health points, 10 per row, like the player's health bar. Returns the rows used. */
    private int extractHearts(GuiGraphicsExtractor graphics) {
        int health = this.menu.getHealth();
        int hearts = (this.menu.getMaxHealth() + 1) / 2;
        for (int i = 0; i < hearts; i++) {
            int x = INFO_X + (i % 10) * 8;
            int y = 19 + (i / 10) * 10;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_CONTAINER, x, y, 9, 9);
            int points = health - i * 2;
            if (points >= 2) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_FULL, x, y, 9, 9);
            } else if (points == 1) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_HALF, x, y, 9, 9);
            }
        }
        return (hearts + 9) / 10;
    }

    private void extractBed(GuiGraphicsExtractor graphics, int y) {
        WolfPanelMenu.BedInfo bed = this.menu.getBed();
        if (bed == null || !this.menu.hasBed()) {
            graphics.text(this.font, Component.translatable("screen.faithful_fangs.wolf_panel.no_bed"),
                    INFO_X, y, LABEL_COLOR, false);
            return;
        }
        graphics.text(this.font, Component.translatable("screen.faithful_fangs.wolf_panel.bed", dimensionName(bed.dimension())),
                INFO_X, y, LABEL_COLOR, false);
        BlockPos pos = bed.pos();
        graphics.text(this.font, Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ()),
                INFO_X, y + 10, LABEL_COLOR, false);
    }

    /** Upgrades view of the right column: icon and name, then the level squares and the price button. */
    private void extractUpgrades(GuiGraphicsExtractor graphics) {
        graphics.text(this.font, Component.translatable("screen.faithful_fangs.wolf_panel.upgrades"),
                COLUMN_X + 1, MODE_LABEL_Y, LABEL_COLOR, false);
        for (WolfUpgrade upgrade : WolfUpgrade.values()) {
            int y = rowY(upgrade);
            if (upgrade == WolfUpgrade.HEALTH) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_FULL, COLUMN_X + 1, y, ICON_SIZE, ICON_SIZE);
            } else {
                graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, COLUMN_X + 1, y,
                        SPRITES_U + (upgrade.ordinal() - 1) * ICON_SIZE, ICONS_V, ICON_SIZE, ICON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            }
            graphics.text(this.font, Component.translatable(upgrade.translationKey()), COLUMN_X + 13, y + 1, LABEL_COLOR, false);

            int level = this.menu.getUpgradeLevel(upgrade);
            for (int i = 0; i < WolfUpgrade.MAX_LEVEL; i++) {
                // Empty square, or the color of the bone that level took.
                int u = SPRITES_U + (i < level ? pipSprite(i + 1) * PIP_SIZE : 0);
                graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, COLUMN_X + 1 + i * 7, y + 16,
                        u, PIPS_V, PIP_SIZE, PIP_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            }
            if (level >= WolfUpgrade.MAX_LEVEL) {
                Component max = Component.translatable("screen.faithful_fangs.wolf_panel.max");
                graphics.text(this.font, max, UPGRADE_BUTTON_X + (UPGRADE_BUTTON_WIDTH - this.font.width(max)) / 2, y + 15,
                        MAX_COLOR, false);
            }
        }
    }

    /** Sprite index of a filled level square: 1 Ancient (levels 1-2), 2 Blazing (3-4), 3 Ethereal (5). */
    private static int pipSprite(int level) {
        return level <= 2 ? 1 : level <= 4 ? 2 : 3;
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

    /** A small button showing the bone the next level costs, with the amount in the corner like a stack. */
    private static final class UpgradeButton extends Button {
        private ItemStack bone = ItemStack.EMPTY;
        private int count;

        UpgradeButton(int x, int y, Component narration, Button.OnPress onPress) {
            super(x, y, UPGRADE_BUTTON_WIDTH, 20, narration, onPress, DEFAULT_NARRATION);
        }

        void setCost(Item item, int amount) {
            if (!this.bone.is(item)) {
                this.bone = new ItemStack(item);
            }
            this.count = amount;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            this.extractDefaultSprite(graphics);
            if (!this.bone.isEmpty()) {
                int x = this.getX() + (this.getWidth() - 16) / 2;
                int y = this.getY() + 2;
                graphics.item(this.bone, x, y);
                graphics.itemDecorations(Minecraft.getInstance().font, this.bone, x, y, String.valueOf(this.count));
            }
        }
    }
}
