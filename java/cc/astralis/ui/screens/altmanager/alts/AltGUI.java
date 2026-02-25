package cc.astralis.ui.screens.altmanager.alts;

import cc.astralis.Astralis;
import cc.astralis.font.FontManager;
import cc.astralis.interfaces.Fonts;
import cc.astralis.interfaces.IAccess;
import cc.astralis.skija.utils.SkijaHelperUtil;
import cc.astralis.skija.utils.SkijaUtil;
import cc.astralis.skija.io.ImageUtil;
import cc.astralis.util.language.Lazy;
import cc.astralis.util.render.ColorUtil;
import io.github.humbleui.skija.*;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import java.awt.*;
import java.awt.Color;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AltGUI extends Screen implements IAccess, Fonts {
    private static final Lazy<Paint> shadowPaint = Lazy.of(() -> {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        return paint;
    });

    private static final Lazy<Paint> blurPaint = Lazy.of(() -> {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        return paint;
    });

    public AltGUI() {
        super(Component.empty());
    }

    public void drawAlt(Alt alt, float x, float y, boolean hovered) {
        final float shadowOffset = 6;
        final int width = 150, height = 50;
        final float cornerRadius = 8f;

        final int xInteger = (int) x, yInteger = (int) y;

        SkijaUtil.beginShaderFrame();
        SkijaUtil.drawShaderRoundRectangle(xInteger, yInteger, width, height, cornerRadius);
        SkijaUtil.drawShaders(true, 8, false, 10);
        SkijaUtil.drawLiquidGlass(xInteger, yInteger, width, height, 100);

        FontManager.getFont("Sf-Bold", 10).drawString(alt.getName(),
                xInteger + 60, yInteger + 15, Color.white);

        String accountType = alt.isPremium() ? "Premium" : "Offline";
        FontManager.getFont("Apple", 10).drawString(accountType,
                xInteger + 60, yInteger + 28, alt.isPremium() ? new Color(255, 196, 4) : new Color(75, 75, 75));

        if (hovered) {
            SkijaUtil.roundedRectangle(x, y, width, height, cornerRadius,
                    new Color(255, 255, 255, 15));
        }

        Image image = ImageUtil.loadSkin(alt.getUuid());
        if (image != null) {
            SkijaUtil.renderRoundedImage(xInteger + 10, yInteger + 5, 40, 40, 20, image,
                    null);
        }
    }
}