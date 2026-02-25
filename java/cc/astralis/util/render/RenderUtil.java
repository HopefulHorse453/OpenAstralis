package cc.astralis.util.render;

import cc.astralis.Astralis;
import cc.astralis.font.UIFont;
import cc.astralis.module.impl.visual.HudModule;
import cc.astralis.interfaces.IAccess;
import cc.astralis.util.Data;
import cc.astralis.skija.utils.SkijaUtil;
import com.sun.jna.platform.win32.*;
import java.awt.*;
import net.minecraft.client.model.geom.builders.UVPair;

public class RenderUtil extends Data implements IAccess {
    public static void drawGradientString(UIFont uifontrenderer, String text, float x, float y, Color firstColor, Color secondColor, boolean shadow) {
        drawGradientString(uifontrenderer, text, x, y, 0, firstColor, secondColor, shadow);
    }

    public static void drawGradientString(UIFont uifontrenderer, String text, float x, float y, float spaceBetweenLetters, Color firstColor, Color secondColor, boolean shadow) {
        final HudModule hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);
        final Color color = switch (hud.colorMode.getProperty()) {
            case "Rainbow" -> HudModule.getRainbow(3000, (int) (x * 32), 0.7F, 1);
//                case "Astolfo" -> HudModule.getAstolfo((int) (charX * 16));
            default -> ColorUtil.getAccentColor(new UVPair(x * 32, 6), firstColor, secondColor);
        };

        if (shadow) {
            uifontrenderer.drawStringWithShadow(
                    text,
                    x, y,
                    color
            );
        } else {
            uifontrenderer.getFontRenderer().drawString(
                    text,
                    x, y, true,
                    color
            );
        }
    }

    public static boolean isHovered(final double mouseX, final double mouseY, final float x, final float y, final float width, final float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static final String REGISTRY_PATH = "Control Panel\\Desktop";
    public static final String REGISTRY_Key = "WallPaper";

    public static void drawCurrentUserWallpaper() {
  /*      if (HwidUtil.PlatformInfo.detect() != HwidUtil.PlatformInfo.OS.WIN)
            return;

        try {
            var hKey = new WinReg.HKEYByReference();
            var registryAccessResult = openRegistryKey(hKey);
            validate(registryAccessResult);
            if (backgroundImage == null) {
                backgroundImage = NVGImage.ofFilePath(getWallpaperPath());
            } else {
                SkijaUtil.renderImage(0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(), backgroundImage, Color.white);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        SkijaUtil.rectangleGradient(0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(),
                new Color(20, 20, 20), new Color(50, 50, 50), false);

    }

    public static int openRegistryKey(WinReg.HKEYByReference hKey) {
        return Advapi32.INSTANCE.RegOpenKeyEx(
                WinReg.HKEY_CURRENT_USER,
                REGISTRY_PATH,
                0,
                WinNT.KEY_READ,
                hKey
        );
    }

    public static void validate(int registryAccessResult) {
        if (registryAccessResult != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(registryAccessResult);
        }
    }

    public static String getWallpaperPath() {
        return Advapi32Util.registryGetStringValue(
                WinReg.HKEY_CURRENT_USER,
                REGISTRY_PATH,
                REGISTRY_Key
        );
    }

    // ints cuz mc scissor todo: maybe do your own.
  /*  public static void drawBlur(float delta, int x, int y, int width, int height, float radius) {
        ClickGuiModule clickGuiModule = Astralis.getInstance().getModuleManager().getModule(ClickGuiModule.class);
        GameRendererAccessor gameRenderer = (GameRendererAccessor) mc.gameRenderer;

        if (gameRenderer.getBlurPostProcessor() != null && radius >= 1.0F) {
            int scissorY = mc.getWindow().getHeight() - y - height;
            RenderSystem.enableScissor(x, scissorY, width, height);

            gameRenderer.getBlurPostProcessor().setUniforms("Radius", radius);
            gameRenderer.getBlurPostProcessor().render(delta);

            RenderSystem.disableScissor();
        }
    }*/

}
