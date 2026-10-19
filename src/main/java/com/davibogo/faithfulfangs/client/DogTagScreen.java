package com.davibogo.faithfulfangs.client;

import com.davibogo.faithfulfangs.item.EngravedTag;
import com.davibogo.faithfulfangs.network.EngraveTagPacket;
import com.davibogo.faithfulfangs.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Screen to type a wolf's name. For a wolf without a bed it engraves a blank Dog Tag;
 * for a wolf that already has a bed it renames the wolf right away.
 */
public class DogTagScreen extends Screen {
    private static final Component NAME_LABEL = Component.translatable("screen.faithful_fangs.dog_tag.name");
    private static final Component HINT = Component.translatable("screen.faithful_fangs.dog_tag.hint")
            .withStyle(ChatFormatting.GRAY);

    private final int wolfEntityId;
    private final String initialName;
    private final boolean rename;
    private EditBox nameBox;
    private Button confirmButton;

    public DogTagScreen(int wolfEntityId, String initialName, boolean rename) {
        super(rename
                ? Component.translatable("screen.faithful_fangs.dog_tag.rename_title", initialName)
                : Component.translatable("screen.faithful_fangs.dog_tag.engrave_title"));
        this.wolfEntityId = wolfEntityId;
        this.initialName = initialName;
        this.rename = rename;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 100;
        this.nameBox = new EditBox(this.font, left, 90, 200, 20, NAME_LABEL);
        this.nameBox.setMaxLength(EngravedTag.MAX_NAME_LENGTH);
        this.nameBox.setValue(this.initialName);
        this.nameBox.setResponder(value -> this.updateConfirmButton());
        this.addWidget(this.nameBox);

        Component confirmText = Component.translatable(this.rename
                ? "screen.faithful_fangs.dog_tag.rename"
                : "screen.faithful_fangs.dog_tag.engrave");
        this.confirmButton = this.addRenderableWidget(Button.builder(confirmText, button -> this.confirm())
                .bounds(left, 140, 98, 20)
                .build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose())
                .bounds(left + 102, 140, 98, 20)
                .build());
        this.updateConfirmButton();
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.nameBox);
    }

    @Override
    public void resize(int width, int height) {
        String typed = this.nameBox.getValue();
        this.init(width, height);
        this.nameBox.setValue(typed);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.confirmButton.active && this.getFocused() == this.nameBox && event.isConfirmation()) {
            this.confirm();
            return true;
        }
        return super.keyPressed(event);
    }

    private void updateConfirmButton() {
        this.confirmButton.active = EngravedTag.sanitizeName(this.nameBox.getValue()) != null;
    }

    private void confirm() {
        String name = EngravedTag.sanitizeName(this.nameBox.getValue());
        if (name != null) {
            ModNetwork.sendToServer(new EngraveTagPacket(this.wolfEntityId, name));
            this.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, 50, -1);
        graphics.text(this.font, NAME_LABEL, this.width / 2 - 100 + 1, 78, -6250336);
        this.nameBox.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, HINT, this.width / 2, 118, -1);
    }
}
