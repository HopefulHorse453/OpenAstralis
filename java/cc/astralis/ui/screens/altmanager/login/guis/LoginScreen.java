package cc.astralis.ui.screens.altmanager.login.guis;

import cc.astralis.Astralis;
import cc.astralis.interfaces.IAccess;
import astralis.mixin.accessor.mc.MinecraftClientSessionAccessor;
import cc.astralis.ui.imgui.ImGuiImpl;
import cc.astralis.ui.imgui.windows.FileExplorerWindow;
import cc.astralis.ui.screens.altmanager.AltManagerScreen;
import cc.astralis.ui.screens.altmanager.alts.Alt;
import cc.astralis.ui.screens.altmanager.microsoft.MicrosoftClient;
import cc.astralis.ui.screens.altmanager.microsoft.MicrosoftClientIDs;
import cc.astralis.ui.screens.altmanager.microsoft.MicrosoftLogin;
import cc.astralis.ui.widgets.CustomButtonWidget;
import cc.astralis.util.network.AccountUtil;
import cc.astralis.util.network.BrowserUtil;
import cc.astralis.util.render.ImguiUtil;
import imgui.ImGui;
import java.awt.*;
import java.io.File;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.User;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LoginScreen extends Screen implements IAccess, MicrosoftClientIDs {
    private EditBox nameField;
    private final boolean addAlt;
    private FileExplorerWindow fileExplorerWindow;
    private boolean pendingCookieLogin = false;

    private MicrosoftClient selectedClient;

    public LoginScreen(boolean addAlt) {
        super(Component.nullToEmpty(addAlt ? "Add Alt Gui" : "Direct Login"));
        this.addAlt = addAlt;
        this.selectedClient = MicrosoftClientIDs.IAS;
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

        CustomButtonWidget clientCycleButton = new CustomButtonWidget(
                (this.width - buttonWidth) / 2,
                (this.height - buttonHeight) / 2 - 60,
                buttonWidth, buttonHeight,
                getClientButtonText(),
                (button) -> {
                    int index = MicrosoftClient.getClients().indexOf(selectedClient);
                    int nextIndex = (index + 1) % MicrosoftClient.getClients().size();
                    selectedClient = MicrosoftClient.getClients().get(nextIndex);
                    button.setMessage(getClientButtonText());
                }
        );

        CustomButtonWidget browserLogin = new CustomButtonWidget(
                (this.width - buttonWidth) / 2,
                (this.height - buttonHeight) / 2 + 25,
                buttonWidth, buttonHeight,
                Component.literal("Browser Login"),
                (button) -> {
                    MicrosoftLogin.getRefreshToken(refreshToken -> {
                        if (refreshToken != null) {
                            new Thread(() -> {
                                MicrosoftLogin.LoginData loginData = BrowserUtil.loginWithRefreshToken(refreshToken, selectedClient.getId());
                                if (addAlt)
                                    AltManagerScreen.getAlts().add(new Alt(
                                            loginData.username,
                                            loginData.mcToken,
                                            loginData.uuid,
                                            true)
                                    );
                            }).start();
                        }
                    }, selectedClient.getId());
                }
        );

        CustomButtonWidget crackedLogin = new CustomButtonWidget(
                (this.width - buttonWidth) / 2,
                (this.height - buttonHeight) / 2,
                buttonWidth, buttonHeight,
                Component.literal("Cracked Login"),
                (button) -> {
                    AccountUtil.loginWithAlt(new Alt(nameField.getValue(), "", ""));
                    if (addAlt)
                        AltManagerScreen.getAlts().add(new Alt(mc.getUser().getName(), "", "", false));
                }
        );

        CustomButtonWidget tokenLoginButton = new CustomButtonWidget(
                (this.width - buttonWidth) / 2,
                (this.height - buttonHeight) / 2 + 50,
                buttonWidth, buttonHeight,
                Component.literal("Token Login"),
                (button) -> {
                    MinecraftClientSessionAccessor minecraftClientSessionAccessor = (MinecraftClientSessionAccessor) mc;
                    try {
                        String clipboardText = mc.keyboardHandler.getClipboard();

                        if (clipboardText == null || clipboardText.isEmpty()) {
                            Astralis.LOGGER.error("Clipboard is empty");
                            return;
                        }

                        String token = clipboardText.replaceAll("[^A-Za-z0-9\\-_.]", "");

                        String[] accountAttributes = AccountUtil.getProfileInfo(token);

                        UUID uuid = AccountUtil.formatUUID(accountAttributes[1]);
                        User session = new User(
                                accountAttributes[0],
                                uuid,
                                token,
                                Optional.empty(), Optional.empty()
                        );

                        if (addAlt)
                            AltManagerScreen.getAlts().add(new Alt(accountAttributes[0], token, accountAttributes[1], true));

                        minecraftClientSessionAccessor.setUser(session);
                    } catch (Exception e) {
                        Astralis.LOGGER.error("fail");
                    }
                }
        );

        CustomButtonWidget cookieLoginButton = new CustomButtonWidget(
                (this.width - buttonWidth) / 2,
                (this.height - buttonHeight) / 2 + 75,
                buttonWidth, buttonHeight,
                Component.literal("Cookie Login"),
                (button) -> {
                    fileExplorerWindow = new FileExplorerWindow(System.getProperty("user.home"), "txt");
                    pendingCookieLogin = true;
                }
        );

        this.addWidget(nameField);
        this.addRenderableWidget(clientCycleButton);
        this.addRenderableWidget(crackedLogin);
        this.addRenderableWidget(browserLogin);
        this.addRenderableWidget(tokenLoginButton);
        this.addRenderableWidget(cookieLoginButton);

    }

    private Component getClientButtonText() {
        return Component.literal(selectedClient.getColor() + selectedClient.getName());
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderPanorama(context, delta);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        /* RenderGateway.render(RenderUtil::drawCurrentUserWallpaper);*/

        if (fileExplorerWindow != null && pendingCookieLogin) {
            ImGuiImpl.draw(io -> {
                if (ImGui.begin("File Explorer")) {
                    ImguiUtil.setDarkMode(false, 3);
                    fileExplorerWindow.showInline();
                    ImGui.end();
                }
            });

            if (fileExplorerWindow.getSelectedFile() != null && fileExplorerWindow.isShouldClose()) {
                pendingCookieLogin = false;

                new Thread(() -> {
                    try {
                        File file = new File(fileExplorerWindow.getSelectedFile());
                        AccountUtil.loginWithCooke(file, addAlt);

                        fileExplorerWindow.reset();
                    } catch (Exception e) {
                        Astralis.LOGGER.error("Cookie login failed", e);
                    }
                }, "Cookie Login Thread").start();
            }
        }

        context.drawCenteredString(mc.font, getTitle(), width / 2, 10, -1);
        context.drawString(mc.font, "Logged In With " + mc.getUser().getName(), 3, 3, Color.white.getRGB(), true);
        nameField.render(context, mouseX, mouseY, delta);
    }
}
