package cc.astralis.ui.screens.clickgui.dropdown.impl;

import cc.astralis.Astralis;
import cc.astralis.font.FontManager;
import cc.astralis.font.UIFont;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.ui.animations.Animation;
import cc.astralis.ui.animations.Easing;
import cc.astralis.ui.screens.clickgui.dropdown.Component;
import cc.astralis.util.render.ColorUtil;
import cc.astralis.skija.utils.SkijaUtil;
import lombok.Getter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
public class TabComponent extends Component {
    private final List<ModuleComponent> moduleList = new ArrayList<>();
    private final Category category;
    private final Animation scrollAnimation = new Animation(Easing.EASE_OUT_BACK, 300);

    private float scrollOffset, targetScrollOffset;
    private static final float FIXED_PANEL_HEIGHT = 400, SCROLL_SPEED = 20;

    public TabComponent(Category category, float x, float y, float width, float height) {
        this.category = category;
        this.setWidth(width);
        this.setHeight(height);
        this.setX(x);
        this.setY(y);

        for (Module module : Astralis.getInstance().getModuleManager().getModulesFromCategory(category)) {
            moduleList.add(new ModuleComponent(module, moduleList));
        }
    }

    private float getContentHeight() {
        float contentHeight = 0;
        for (ModuleComponent module : moduleList) {
            contentHeight += module.getHeight();
            if (module.extended || module.isAnimating) {
                contentHeight += (float) module.dropDownAnimation.getValue();
            }
        }
        return contentHeight;
    }

    private float getMaxScroll() {
        return Math.max(0, getContentHeight() - (FIXED_PANEL_HEIGHT - getHeight()));
    }

    public void scroll(double amount) {
        targetScrollOffset -= (float) (amount * SCROLL_SPEED);
        targetScrollOffset = Math.max(0, Math.min(targetScrollOffset, getMaxScroll()));

        scrollAnimation.setStartPoint(scrollOffset);
        scrollAnimation.setEndPoint(targetScrollOffset);
        scrollAnimation.reset();
    }

    @Override
    public void render(float mouseX, float mouseY) {
        final float actualContentHeight = Math.min(FIXED_PANEL_HEIGHT - getHeight(), getContentHeight());

        SkijaUtil.beginShaderFrame();
        SkijaUtil.drawShaderRectangleVarying(getX() + 1, getY(), getWidth() - 1, getHeight() + actualContentHeight, 5, 5, 5, 5);
        SkijaUtil.drawShaders();

        final UIFont font12 = FontManager.getFont("tenacity-bold", 12);
        final int baseAlpha = 187;
        final Color baseColor = ColorUtil.intToColor(0xBB000000);

        scrollAnimation.run(targetScrollOffset);
        scrollOffset = (float) scrollAnimation.getValue();

        SkijaUtil.roundedRectangleVarying(getX() + 1, getY(), getWidth() - 1, getHeight(), 5, 5, 0, 0, baseColor);
        font12.drawCenterStringInBox(category.getName(), getX(), getY(), getWidth(), getHeight(), Color.white);

        final float contentStartY = getY() + getHeight();
        final float visibleHeight = FIXED_PANEL_HEIGHT - getHeight();

        SkijaUtil.scissored(getX(), contentStartY, getWidth(), visibleHeight, () -> {
            /*SkijaUtil.rectangle(getX() + 1, contentStartY, getWidth() - 1, visibleHeight, baseColor);*/

            float currentY = contentStartY - scrollOffset;
            for (ModuleComponent module : moduleList) {
                module.setX(getX() + 1);
                module.setY(currentY);
                module.setWidth(getWidth() - 1);
                module.setHeight(23);
                module.render(mouseX, mouseY);

                currentY += module.getHeight();
                if (module.extended || module.isAnimating) {
                    currentY += (float) module.dropDownAnimation.getValue();
                }
            }
        });

      /*  final float maxScroll = getMaxScroll();
        if (maxScroll > 0) {
            final float scrollbarHeight = Math.max(20, (visibleHeight / getContentHeight()) * visibleHeight);
            SkijaUtil.roundedRectangle(
                    getX() + getWidth() - 4,
                    contentStartY + (scrollOffset / maxScroll) * (visibleHeight - scrollbarHeight),
                    2,
                    scrollbarHeight,
                    1,
                    new Color(255, 255, 255, 100)
            );
        }*/
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        final float contentStartY = getY() + getHeight();
        final float visibleHeight = FIXED_PANEL_HEIGHT - getHeight();

        if (mouseX >= getX() && mouseX <= getX() + getWidth() &&
                mouseY >= contentStartY && mouseY <= contentStartY + visibleHeight) {

            final float virtualY = (float) mouseY - contentStartY + scrollOffset;
            float currentY = 0;

            for (ModuleComponent module : moduleList) {
                float moduleHeight = module.getHeight();
                if (module.extended || module.isAnimating) {
                    moduleHeight += (float) module.dropDownAnimation.getValue();
                }

                if (virtualY >= currentY && virtualY <= currentY + moduleHeight) {
                    module.click(mouseX, mouseY, button);
                    break;
                }

                currentY += moduleHeight;
            }
        }
    }

    @Override
    public void release(double mouseX, double mouseY, int button) {
        final float contentStartY = getY() + getHeight();
        final float visibleHeight = FIXED_PANEL_HEIGHT - getHeight();

        if (mouseX >= getX() && mouseX <= getX() + getWidth() &&
                mouseY >= contentStartY && mouseY <= contentStartY + visibleHeight) {

            final float virtualY = (float) mouseY - contentStartY + scrollOffset;
            float currentY = 0;

            for (ModuleComponent module : moduleList) {
                float moduleHeight = module.getHeight();
                if (module.extended || module.isAnimating) {
                    moduleHeight += (float) module.dropDownAnimation.getValue();
                }

                if (virtualY >= currentY && virtualY <= currentY + moduleHeight) {
                    module.release(mouseX, mouseY, button);
                    break;
                }

                currentY += moduleHeight;
            }
        }
    }

    public boolean isHovered(double mouseX, double mouseY) {
        final float contentStartY = getY() + getHeight();
        final float visibleHeight = FIXED_PANEL_HEIGHT - getHeight();
        return mouseX >= getX() && mouseX <= getX() + getWidth() &&
                mouseY >= getY() && mouseY <= contentStartY + visibleHeight;
    }
}