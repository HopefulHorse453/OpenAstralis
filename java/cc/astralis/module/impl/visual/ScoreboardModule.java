package cc.astralis.module.impl.visual;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.render.ShaderEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.ColorProperty;
import lombok.Setter;

import java.awt.*;

public class ScoreboardModule extends Module {
    public final BooleanProperty noScoreBoard = new BooleanProperty("No Score Board", false);
    public final BooleanProperty noScore = new BooleanProperty("No Score", true);
    public final BooleanProperty changeColor = new BooleanProperty("Change Color", false);
    public final ColorProperty headColor = new ColorProperty("Head Color", Color.white).setVisible(changeColor::getProperty);
    public final ColorProperty bodyColor = new ColorProperty("Body Color", Color.white).setVisible(changeColor::getProperty);
    public final ColorProperty scoreColor = new ColorProperty("Score Color", Color.red).setVisible(changeColor::getProperty);

    @Setter
    private Bounds bounds;

    public ScoreboardModule() {
        super(Category.VISUAL);
        registerProperties(noScoreBoard, noScore, changeColor, headColor, bodyColor, scoreColor);
    }

    @EventTarget
    public void onShader(ShaderEvent event) {
        if (bounds == null)
            return;

      /*  SkijaUtil.drawShaderRectangle(bounds.x, bounds.y, bounds.width, bounds.height);*/
    }

    public record Bounds(float x, float y, float width, float height) {
        /* w */
    }
}