package cc.astralis.ui.screens.client;

import cc.astralis.drag.Draggable;
import cc.astralis.interfaces.Fonts;
import cc.astralis.interfaces.IAccess;
import cc.astralis.interfaces.access.IDrag;
import cc.astralis.util.render.RenderUtil;
import cc.astralis.skija.utils.SkijaUtil;
import java.awt.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class HudEditorScreen extends Screen implements Fonts, IAccess, IDrag {
    private Draggable currentDraggable;
    private float grabX, grabY;

    public HudEditorScreen() {
        super(Component.nullToEmpty("Hud Editor"));
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) { /* w */ }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        SkijaUtil.drawRectPoint4(0, 0, width, height, new Color(0, 0, 0, 200));

        SkijaUtil.line(width / 2, 0, width / 2, height, 1, new Color(255, 255, 255, 30));
        SkijaUtil.line(0, height / 2, width, height / 2, 1, new Color(255, 255, 255, 30));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();

        draggables.forEach(draggable -> {
            if (RenderUtil.isHovered(mouseX, mouseY, draggable.getX(), draggable.getY(), draggable.getWidth(), draggable.getHeight())) {
                currentDraggable = draggable;

                grabX = (float) mouseX - draggable.getX();
                grabY = (float) mouseY - draggable.getY();
            }
        });

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        double mouseX = click.x();
        double mouseY = click.y();

        if (currentDraggable != null) {
            float x = (float) mouseX - grabX;
            float y = (float) mouseY - grabY;

            int SNAP_DISTANCE = 4;
            if (Math.abs(x - width / 2f) < SNAP_DISTANCE) x = width / 2f;
            if (Math.abs(y - height / 2f) < SNAP_DISTANCE) y = height / 2f;

            currentDraggable.drag(x, y);
        }

        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        currentDraggable = null;
        return super.mouseReleased(click);
    }

    // linear interpolation
    private float lerp(float start, float end) {
        final float amt = 0.05f;
        return (1 - amt) * start + amt * end;
    }
}
