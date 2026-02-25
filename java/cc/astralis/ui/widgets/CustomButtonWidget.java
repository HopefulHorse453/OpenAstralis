package cc.astralis.ui.widgets;

import cc.astralis.font.FontManager;
import cc.astralis.skija.SkijaManager;
import cc.astralis.skija.utils.SkijaUtil;
import java.awt.*;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * @author Kawase
 * @since 20.10.2025
 */
public class CustomButtonWidget extends AbstractWidget {
    private final Consumer<CustomButtonWidget> onClick;

    public CustomButtonWidget(int x, int y, int width, int height, Component text, Consumer<CustomButtonWidget> onClick) {
        super(x, y, width, height, text);
        this.onClick = onClick;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        SkijaManager.addCallback(() -> {

            SkijaUtil.beginShaderFrame();
            SkijaUtil.drawShaderRoundRectangle(getX(), getY(), width, height, 9);
            SkijaUtil.drawShaders(true, 6, false, 10);
            SkijaUtil.drawLiquidGlass(getX(), getY(), width, height, 100);
            FontManager.getFont("Sf-Bold", 10).drawCenterStringInBox(getMessage().getString(), getX(), getY(), width, height, Color.WHITE);
        });
    }

    @Override
    public void onClick(MouseButtonEvent click, boolean doubled) {
        if (onClick != null) {
            onClick.accept(this);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        return;
    }
}