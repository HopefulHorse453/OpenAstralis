package cc.astralis.util.render;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import cc.astralis.Astralis;
import cc.astralis.module.impl.visual.ClickGuiModule;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ImguiUtil {
    private static Map<String, Integer> loadedTextures = new HashMap<>();
    
/*    public static int loadTexture(String path, String textureName) {
        if (loadedTextures.containsKey(textureName)) return loadedTextures.get(textureName);
        try {
            Identifier identifier = IdentifierAccessor.createIdentifier("astralis", path);
            Optional<Resource> resource = MinecraftClient.getInstance().getResourceManager().getResource(identifier);
            NativeImage nativeImage = NativeImage.read(resource.get().getInputStream());
            NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
            TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
            Identifier textureIdentifier = textureManager.registerDynamicTexture(textureName, texture);

            RenderSystem.setShaderTexture(0, textureIdentifier);
            int textureId = texture.getGlId();
            loadedTextures.put(textureName, textureId);

            return textureId;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load image: " + e.getMessage(), e);
        }
    }*/

    public static void setDarkMode(boolean invisibleButtonBg, float spacing) {
        setDarkMode(invisibleButtonBg, spacing, 240);
    }

    public static void setDarkMode(boolean invisibleButtonBg, float spacing, int alpha) {
        ImGuiStyle style = ImGui.getStyle();

        style.setWindowRounding(8);
        style.setFrameRounding(6);
        style.setGrabRounding(6);
        style.setPopupRounding(8);
        style.setScrollbarRounding(6);
        style.setTabRounding(6);

        style.setWindowPadding(12, 12);
        style.setFramePadding(8, 4);
        style.setItemSpacing(8, 6);
        style.setItemInnerSpacing(6, 6);
        style.setTouchExtraPadding(2, 2);

        //final int clampedAlpha = Math.min(255, Math.max(0, (int) alpha));
        final int darkBg = new Color(18, 18, 18, alpha).getRGB();
        final int darkerBg = new Color(12, 12, 12).getRGB();
        final int lightText = new Color(240, 240, 240).getRGB();
        final int mutedText = new Color(180, 180, 180).getRGB();

        int cherryColor = ColorUtil.getSwap(Astralis.getInstance().getModuleManager()
                .getModule(ClickGuiModule.class) == null ? Color.blue :Astralis.getInstance().getModuleManager()
                .getModule(ClickGuiModule.class).color.getProperty());

        style.setItemSpacing(6, spacing);

        // tab
        style.setColor(ImGuiCol.Tab, new Color(35, 35, 35).getRGB());
        style.setColor(ImGuiCol.TabHovered, cherryColor);
        style.setColor(ImGuiCol.TabActive, new Color(35, 35, 35).getRGB());

        // Modern color assignments
        style.setColor(ImGuiCol.Text, lightText);
        style.setColor(ImGuiCol.TextDisabled, mutedText);

        // Backgrounds
        style.setColor(ImGuiCol.WindowBg, darkBg);
        style.setColor(ImGuiCol.ChildBg, darkerBg);
        style.setColor(ImGuiCol.PopupBg, darkBg);

        // Borders
        style.setColor(ImGuiCol.Border, new Color(45, 45, 45).getRGB());
        style.setColor(ImGuiCol.BorderShadow, new Color(0, 0, 0, 0).getRGB());

        // Interactive elements
        style.setColor(ImGuiCol.FrameBg, new Color(30, 30, 30).getRGB());
        style.setColor(ImGuiCol.FrameBgHovered, new Color(45, 45, 45).getRGB());
        style.setColor(ImGuiCol.FrameBgActive, new Color(35, 35, 35).getRGB());

        // Accent colors
        style.setColor(ImGuiCol.CheckMark, cherryColor);
        style.setColor(ImGuiCol.SliderGrab, cherryColor);
        style.setColor(ImGuiCol.SliderGrabActive, cherryColor);

        // Buttons
        if (invisibleButtonBg) {
            style.setColor(ImGuiCol.Button, 0);
            style.setColor(ImGuiCol.ButtonHovered, 0);
            style.setColor(ImGuiCol.ButtonActive, 0);
        } else {
            style.setColor(ImGuiCol.Button, new Color(35, 35, 35).getRGB());
            style.setColor(ImGuiCol.ButtonHovered, cherryColor);
            style.setColor(ImGuiCol.ButtonActive, cherryColor);
        }

        // Headers/tabs
        style.setColor(ImGuiCol.Header, new Color(40, 40, 40).getRGB());
        style.setColor(ImGuiCol.HeaderHovered, cherryColor);
        style.setColor(ImGuiCol.HeaderActive, cherryColor);

        // Title bars
        style.setColor(ImGuiCol.TitleBg, new Color(20, 20, 20).getRGB());
        style.setColor(ImGuiCol.TitleBgActive, new Color(25, 25, 25).getRGB());
        style.setColor(ImGuiCol.TitleBgCollapsed, new Color(15, 15, 15).getRGB());

        // Scrollbars
        style.setColor(ImGuiCol.ScrollbarBg, new Color(15, 15, 15).getRGB());
        style.setColor(ImGuiCol.ScrollbarGrab, new Color(50, 50, 50).getRGB());
        style.setColor(ImGuiCol.ScrollbarGrabHovered, new Color(60, 60, 60).getRGB());
        style.setColor(ImGuiCol.ScrollbarGrabActive, new Color(70, 70, 70).getRGB());

        // Selection highlights
        style.setColor(ImGuiCol.TextSelectedBg, new Color(cherryColor & 0x00FFFFFF | 0x33000000).getRGB());

        // Navigation highlights
        style.setColor(ImGuiCol.NavHighlight, new Color(cherryColor & 0x00FFFFFF | 0x66000000).getRGB());

        // Modern transparent effects
        style.setColor(ImGuiCol.ModalWindowDimBg, new Color(10, 10, 10, 180).getRGB());
    }

    public static void drawModel() {

    }
}
