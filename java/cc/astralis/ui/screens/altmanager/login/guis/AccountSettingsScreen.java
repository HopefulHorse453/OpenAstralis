package cc.astralis.ui.screens.altmanager.login.guis;

import cc.astralis.interfaces.IAccess;
import cc.astralis.ui.screens.altmanager.alts.Alt;
import java.awt.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AccountSettingsScreen extends Screen implements IAccess {
    private EditBox nameField;
    private Alt alt;

    public AccountSettingsScreen(Alt alt) {
        super(Component.nullToEmpty("Account Settings"));
        this.alt = alt;
    }

    @Override
    public void init() {
        final int fieldWidth = 200, fieldHeight = 20;
        nameField = new EditBox(
                this.font,
                (this.width - fieldWidth) / 2,
                (this.height - fieldHeight) / 2 - 25,
                fieldWidth, fieldHeight, Component.literal("Name")
        );

        final int buttonWidth = 200, buttonHeight = 20;

        Button.Builder changeSkinButton = Button.builder(Component.literal("Change Skin"), (buttonWidget) -> {

        }).size(buttonWidth, buttonHeight);

        Button.Builder changeNameButton = Button.builder(Component.literal("Change Name"), (buttonWidget) -> {
        }).size(buttonWidth, buttonHeight);

        this.addWidget(nameField);
        this.addRenderableWidget(changeNameButton.pos((this.width - buttonWidth) / 2, (this.height - buttonHeight) / 2).build());
        this.addRenderableWidget(changeSkinButton.pos((this.width - buttonWidth) / 2, (this.height - buttonHeight) / 2 + buttonHeight + 5).build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(mc.font, getTitle(), width / 2, 10, -1);
        context.drawString(mc.font, "Logged In With " + mc.getUser().getName(), 3, 3, Color.white.getRGB(), true);
        nameField.render(context, mouseX, mouseY, delta);
    }
}
