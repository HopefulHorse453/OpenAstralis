package cc.astralis.module.impl.visual;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.font.UIFont;
import cc.astralis.font.FontManager;
import cc.astralis.module.Category;
import cc.astralis.module.Module;

public class TestRenderModule extends Module {
    final UIFont UIFont = FontManager.getFont("Product Sans Regular", 18);

    public TestRenderModule() {
        super(Category.VISUAL);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        /*String vendor = GL11.glGetString(GL11.GL_VENDOR);
        Astralis.LOGGER.info("Vendor: {0}", vendor);

        UIFont.drawString(Formatting.BLUE + "Hello World", 3, 3, Color.WHITE);
        SkijaUtil.rectangle(30, 60, 100, 40, new Color(255, 50, 50, 255));

        SkijaUtil.rectangleGradient(150, 60, 100, 40, Color.BLUE, Color.CYAN, true);

        SkijaUtil.roundedRectangleOutline(30, 120, 100, 40, 10f,
                new Color(60, 60, 60, 255), 2f, Color.WHITE);

        SkijaUtil.roundedRectangleGradientVarying(150, 120, 100, 40,
                16f, 4f, 12f, 2f, Color.MAGENTA, Color.PINK, false);

        SkijaUtil.drawCircle(50, 200, 20f, Color.YELLOW);

        SkijaUtil.drawCircleOutline(100, 200, 20f, 2f, Color.ORANGE);

        SkijaUtil.drawTriangle(180, 190, 200, 220, 160, 220, new Color(0, 255, 0, 180));*/
    }
}
