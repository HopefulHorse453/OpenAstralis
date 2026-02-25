package cc.astralis.ui.screens.altmanager;

import cc.astralis.Astralis;
import cc.astralis.interfaces.IAccess;
import cc.astralis.ui.screens.altmanager.alts.Alt;
import cc.astralis.ui.screens.altmanager.login.guis.LoginScreen;
import cc.astralis.ui.widgets.CustomButtonWidget;
import cc.astralis.util.network.AccountUtil;
import cc.astralis.util.network.BrowserUtil;
import cc.astralis.util.render.RenderUtil;
import cc.astralis.skija.SkijaManager;
import cc.astralis.skija.io.ImageUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class AltManagerScreen extends Screen implements IAccess {
    private static final ArrayList<Alt> alts = new ArrayList<>();
    private GLFWDropCallback dropCallback;

    @Getter
    @Setter
    private static boolean encryption = false;

    public AltManagerScreen() {
        super(Component.nullToEmpty("Alt Manager"));
        Astralis.getInstance().getEventManager().register(this);
    }

    @Override
    public void init() {
        super.init();

        long handle = mc.getWindow().handle();

        // from: https://stackoverflow.com/questions/34114847/selecting-dragging-and-dropping-a-file-into-glfw-application-from-windows
        dropCallback = GLFWDropCallback.create((window, count, names) -> {
            for (int i = 0; i < count; i++) {
                String path = GLFWDropCallback.getName(names, i);

                if (path.endsWith(".txt")) {
                    AccountUtil.loginWithCooke(new File(path), true);
                }
            }
        });

        GLFW.glfwSetDropCallback(handle, dropCallback);

        CustomButtonWidget addLoginButton = new CustomButtonWidget(
                10, 40, 100, 20,
                Component.literal("Add Alt"),
                (btn) -> mc.setScreen(new LoginScreen(true))
        );

        CustomButtonWidget directLoginButton = new CustomButtonWidget(
                10, 65, 100, 20,
                Component.literal("Direct Login"),
                (btn) -> mc.setScreen(new LoginScreen(false))
        );

        CustomButtonWidget encryptionToggleButton = new CustomButtonWidget(
                10, 90, 100, 20,
                Component.literal("Encryption " + (isEncryption() ? ChatFormatting.GREEN : ChatFormatting.RED) + isEncryption()),
                (btn) -> encryption = !encryption
        );

        CustomButtonWidget websiteButton = new CustomButtonWidget(
                10, 115, 100, 20,
                Component.literal("YYY Alts"),
                (btn) -> {
                    try {
                        BrowserUtil.openUrl("https://discord.gg/CxXjUVfqbj");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );

        this.addRenderableWidget(addLoginButton);
        this.addRenderableWidget(directLoginButton);
        this.addRenderableWidget(encryptionToggleButton);
        this.addRenderableWidget(websiteButton);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

        SkijaManager.addCallback(() -> {
         /*   RenderUtil.drawCurrentUserWallpaper();*/
            float areaX = 120, areaY = 20;
            float areaWidth = this.width - 140, areaHeight = this.height - 40;

            final float baseX = 135, baseY = 30;
            final float width = 150, height = 50;

            for (int i = 0; i < alts.size(); i++) {
                Alt alt = alts.get(i);

                int cols = (int) ((areaWidth - 20) / (width + 10));
                int col = i % cols;
                int row = i / cols;

                float x = baseX + col * (width + 10);
                float y = baseY + row * (height + 10);

                if (y + height > areaX + areaHeight - 10) {
                    continue;
                }

                alt.getAltGUI().drawAlt(alt, x, y,
                        RenderUtil.isHovered(mouseX, mouseY, x, y, width, height));
            }
        });

        super.render(context, mouseX, mouseY, delta);
        context.drawString(mc.font, "Logged In With " + mc.getUser().getName(), 3, 3, Color.white.getRGB(), true);
    }

    @Override
    public void onClose() {
        if (dropCallback != null) {
            dropCallback.free();
            dropCallback = null;
        }

        Astralis.getInstance().getEventManager().unregister(this);
        ImageUtil.clearImageCache();
        
        super.onClose();
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderPanorama(context, delta);
        // im too incompetent to draw an image.
     /*   Identifier wallpaper = Identifier.of("astralis", "wallpaper");

        context.drawGuiTexture(
                RenderPipelines.GUI_TEXTURED,
                wallpaper,
                0, 0,
                width, height
        );*/
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        float areaX = 120;
        float areaWidth = this.width - 140, areaHeight = this.height - 40;

        final float baseX = 135, baseY = 30;
        final float width = 150, height = 50;

        for (int i = 0; i < alts.size(); i++) {
            Alt alt = alts.get(i);

            int cols = (int) ((areaWidth - 20) / (width + 10));
            int col = i % cols;
            int row = i / cols;

            float x = baseX + col * (width + 10);
            float y = baseY + row * (height + 10);

            if (y + height > areaX + areaHeight - 10) {
                continue;
            }

            if (RenderUtil.isHovered(click.x(), click.y(), x, y, width, height)) {
                AccountUtil.loginWithAlt(alt);
            }
        }

        return super.mouseClicked(click, doubled);
    }

    public static ArrayList<Alt> getAlts() {
        return alts;
    }
}