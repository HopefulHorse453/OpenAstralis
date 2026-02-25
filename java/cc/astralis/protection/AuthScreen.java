package cc.astralis.protection;

import cc.astralis.Astralis;
import cc.astralis.interfaces.IAccess;
import cc.astralis.util.io.ProtectionUtil;
import club.serenityutils.packets.impl.AuthPacket;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import static cc.astralis.util.io.HwidUtil.getHardwareID;

public class AuthScreen extends Screen implements IAccess {
    private EditBox uidField;

    private boolean wasPressed;
    public int lastTriedUid = 1237867;

    private String authStatus;

    public AuthScreen() {
        super(Component.literal("Authentication"));
    }

    @Override
    protected void init() {
        Flags.authGuiShown = true;

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        uidField = new EditBox(this.font, centerX - 100, centerY - 10, 200, 20, Component.literal("UID"));
        uidField.setMaxLength(64);
        uidField.setHint(Component.literal("Enter UID"));
        this.addRenderableWidget(uidField);
      //  this.setInitialFocus(uidField);

        Button authButton = this.addRenderableWidget(
                Button.builder(Component.literal("Authenticate"), b -> handleAuthentication())
                        .bounds(centerX - 100, centerY + 25, 200, 20)
                        .build()
        );

        Button copyHwidButton = this.addRenderableWidget(
                Button.builder(Component.literal("Copy HWID"), b -> {
                    String hwid = getHardwareID();
                    if (this.minecraft != null && this.minecraft.keyboardHandler != null) {
                        this.minecraft.keyboardHandler.setClipboard(hwid);
                    }
                }).bounds(centerX - 100, centerY + 50, 200, 20).build()
        );
    }

    private void handleAuthentication() {
        if (!this.uidField.getValue().isEmpty()) {
            try {
                wasPressed = true;
                new Thread(() -> {
                    authStatus = "Authenticating...";
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (Flags.isNotAuthenticated) {
                        authStatus = "Failed to authenticate";
                    }
                }).start();

                int uid = Integer.parseInt(this.uidField.getValue());
                lastTriedUid = uid;
                new AuthPacket(uid).sendPacket(Astralis.getInstance().getClient());
                Flags.authPacketSent = true;
            } catch (Exception e) {
                ProtectionUtil.crash();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!Flags.isNotAuthenticated && Objects.equals(Flags.authStatus, "gud boy") && Flags.authGuiShown) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new TitleScreen());
            }
        }
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.input() == GLFW.GLFW_KEY_ENTER || input.input() == GLFW.GLFW_KEY_KP_ENTER) {
            handleAuthentication();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        context.drawCenteredString(this.font, "Authentication Required", centerX, centerY - 40, 0xFFFFFF);
        context.drawCenteredString(this.font, "Please enter your UID to continue", centerX, centerY - 26, 0xAAAAAA);

        if (wasPressed && Flags.isNotAuthenticated) {
            context.drawCenteredString(this.font,
                    "Invalid UID / Invalid HWID. Please try again or create ticket.", centerX, centerY + 80, 0xFF4545);
        } else if (authStatus != null && authStatus.equals("Authenticating...")) {
            context.drawCenteredString(this.font, authStatus, centerX, centerY + 80, 0xFFFFFF);
        }

        super.render(context, mouseX, mouseY, delta);
    }
}
