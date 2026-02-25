package cc.astralis.ui.imgui;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import imgui.*;
import imgui.extension.implot.ImPlot;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;

import java.io.IOException;
import java.io.InputStream;
import net.minecraft.client.Minecraft;

public class ImGuiImpl {
    private final static ImGuiImplGlfw imGuiImplGlfw = new ImGuiImplGlfw();
    private final static ImGuiImplGl3 imGuiImplGl3 = new ImGuiImplGl3();

    private static ImFont fontAwesomeFont;

    public static final char ICON_FOLDER = '\uf07b';
    public static final char ICON_FILE = '\uf15b';
    public static final char ICON_IMAGE = '\uf1c5';
    public static final char ICON_DOCUMENT = '\uf1c9';
    public static final char ICON_AUDIO = '\uf1c7';
    public static final char ICON_VIDEO = '\uf1c8';
    public static final char ICON_ARCHIVE = '\uf1c6';

    public static void create(final long handle) {
        ImGui.createContext();
        ImPlot.createContext();

        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename("astralis.ini");
        io.setConfigFlags(ImGuiConfigFlags.DockingEnable | ImGuiConfigFlags.ViewportsEnable);
        io.setFontGlobalScale(1F);

        loadMainFont(io);
        loadFontAwesome(io);
        io.getFonts().build();

        imGuiImplGlfw.init(handle, true);
        imGuiImplGl3.init();
    }

    private static void loadMainFont(ImGuiIO io) {
        try (InputStream is = ImGuiImpl.class.getResourceAsStream("/assets/astralis/fonts/Product Sans Regular.ttf")) {
            if (is != null) {
                byte[] fontData = is.readAllBytes();
                io.getFonts().addFontFromMemoryTTF(fontData, 16.5f);
            } else {
                System.err.println("Product Sans font not found.");
            }
        } catch (IOException e) {
            System.err.println("Failed to load main font");
        }
    }

    private static void loadFontAwesome(ImGuiIO io) {
        try (InputStream is = ImGuiImpl.class.getResourceAsStream("/assets/astralis/fonts/fontawesome-webfont.ttf")) {
            if (is != null) {
                byte[] fontData = is.readAllBytes();

                ImFontConfig fontConfig = new ImFontConfig();
                fontConfig.setMergeMode(true);
                fontConfig.setGlyphMinAdvanceX(14.0f);
                fontConfig.setPixelSnapH(true);

                short[] iconRanges = {
                        (short) ICON_FOLDER, (short) ICON_FOLDER,
                        (short) ICON_FILE, (short) ICON_FILE,
                        (short) ICON_IMAGE, (short) ICON_IMAGE,
                        (short) ICON_DOCUMENT, (short) ICON_DOCUMENT,
                        (short) ICON_AUDIO, (short) ICON_AUDIO,
                        (short) ICON_VIDEO, (short) ICON_VIDEO,
                        (short) ICON_ARCHIVE, (short) ICON_ARCHIVE,
                        0
                };

                fontAwesomeFont = io.getFonts().addFontFromMemoryTTF(fontData, 14.0f, fontConfig, iconRanges);
            } else {
                System.err.println("FontAwesome font not found.");
            }
        } catch (IOException e) {
            System.err.println("Failed to load Font Awesome");
        }
    }

    public static void pushFontAwesome() {
        if (fontAwesomeFont != null) {
            ImGui.pushFont(fontAwesomeFont);
        }
    }

    public static void popFontAwesome() {
        if (fontAwesomeFont != null) {
            ImGui.popFont();
        }
    }

    public static void draw(final ImGuiRenderer runnable) {
        final RenderTarget framebuffer = Minecraft.getInstance().getMainRenderTarget();
        GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, ((GlTexture) framebuffer.getColorTexture()).getFbo(((GlDevice) RenderSystem.getDevice()).directStateAccess(), null));
        GL11C.glViewport(0, 0, framebuffer.width, framebuffer.height);

        imGuiImplGl3.newFrame();
        imGuiImplGlfw.newFrame();
        ImGui.newFrame();

        runnable.render(ImGui.getIO());

        ImGui.render();
        imGuiImplGl3.renderDrawData(ImGui.getDrawData());

        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long pointer = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();

            GLFW.glfwMakeContextCurrent(pointer);
        }
    }

    public static void dispose() {
  /*      imGuiImplGl3.shutdown();
        imGuiImplGlfw.shutdown();
        ImPlot.destroyContext();
        ImGui.destroyContext();*/
    }
}
