package cc.astralis.module.impl.visual;

import cc.astralis.Astralis;
import cc.astralis.drag.Draggable;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.event.events.impl.render.ShaderEvent;
import cc.astralis.font.FontManager;
import cc.astralis.font.UIFont;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.skija.utils.SkijaUtil;
import cc.astralis.util.math.TimeUtil;
import cc.astralis.util.render.ColorUtil;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.PaintMode;
import io.github.humbleui.skija.PaintStrokeCap;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SessionInformationModule extends Module {
    private final Draggable draggable;

    private final Paint paint = new Paint();

    // js reset this when the player joins a server.
    // for now it isnt hooked anywhere 3;.
    private final TimeUtil joinTime = new TimeUtil();

    public SessionInformationModule() {
        super(Category.VISUAL);
        this.draggable = new Draggable("Session Information", 3, 100, 150, 70);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        final HudModule hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);

        final float x = draggable.getX(), y = draggable.getY();
        final float width = draggable.getWidth(), height = draggable.getHeight();

        final long msInHour = 60 * 60 * 1000;
        long msThisHour = joinTime.getElapsedTime() % msInHour;

        float angle = (float) ((msThisHour / (float) msInHour) * 360.0);

        String formatted = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        final float cx = x + 70, cy = y + 171;
        final float radius = 50;

        final Color bg = new Color(18, 18, 20);

        SkijaUtil.roundedRectangle(x, y, width, height, 5, ColorUtil.withAlpha(bg, hud.backgroundAlpha.getProperty().intValue()));

        paint.reset()
                .setColor(new Color(35, 35, 40).getRGB())
                .setAntiAlias(true)
                .setMode(PaintMode.STROKE)
                .setStrokeWidth(4)
                .setStrokeCap(PaintStrokeCap.ROUND);

        SkijaUtil.getCanvas().drawArc(cx - radius, cy - radius, cx + radius, cy + radius, -90, 360, false, paint);

        paint.reset()
                .setColor(Astralis.getInstance().getFirstColor().getRGB())
                .setAntiAlias(true)
                .setMode(PaintMode.STROKE)
                .setStrokeWidth(4)
                .setStrokeCap(PaintStrokeCap.ROUND);

        SkijaUtil.getCanvas().drawArc(cx - radius, cy - radius, cx + radius, cy + radius, -90, angle, false, paint);

        final UIFont font = product_bold_9;

        final float centeredY = y + (height - font.getStringHeight(formatted)) / (2f);
        font.drawString(formatted, x + 21, centeredY, Color.white);
    }

    @EventTarget
    public void onShader(ShaderEvent event) {
        SkijaUtil.drawShaderRoundRectangle(draggable.getX(), draggable.getY(), draggable.getWidth(), draggable.getHeight(), 5);
    }
}
