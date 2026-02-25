package cc.astralis.ui.screens.clickgui.astralis.impl;

import cc.astralis.Astralis;
import cc.astralis.font.UIFont;
import cc.astralis.font.FontManager;
import cc.astralis.module.Category;
import cc.astralis.module.impl.visual.HudModule;
import cc.astralis.ui.screens.clickgui.astralis.Component;
import cc.astralis.util.render.RenderUtil;
import cc.astralis.skija.utils.SkijaUtil;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CategorySelector extends Component {
    private final Category category;

    private static final Map<Category, CategoryInfo> CATEGORY_INFO = new HashMap<>();

    static {
        for (final Category category : Category.values()) {
            final CategoryInfo info = switch (category) {
                case COMBAT -> new CategoryInfo("S", 0, 0.5F, 13);
                case MOVEMENT -> new CategoryInfo("T", 0, 0.5F, 13);
                case WORLD -> new CategoryInfo("U", 0, 1, 13);
                case PLAYER -> new CategoryInfo("G", 0.4F, 0.6F, 16);
                case VISUAL -> new CategoryInfo("K", 0, 0.5F, 15);
                case EXPLOIT -> new CategoryInfo("A", 0.5F, 15);
            };

            CATEGORY_INFO.put(category, info);
        }
    }

    public CategorySelector(final Category category, final int size) {
        this.category = category;
        setWidth(size);
        setHeight(size);
    }

    @Override
    public void render(final float x, final float y, final float mouseX, final float mouseY) {
        setX(x);
        setY(y);

        final HudModule hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);
        SkijaUtil.roundedRectangleDiagonalGradient(x, y, getWidth(), getHeight(), 5, hud.firstColor.getProperty(), hud.secondColor.getProperty());

        final CategoryInfo info = CATEGORY_INFO.get(category);

        float xOff = 0;
        switch (category) {
            case PLAYER -> xOff = 2.0F;
            case EXPLOIT -> xOff = 1.5F;
            case VISUAL -> xOff = 1.0F;
        }

        final UIFont UIFont = FontManager.getFont("Icons-Regular", info.fontSize);
        UIFont.drawStringWithShadow(info.icon,
                x + getWidth() / 2 - UIFont.getStringHeight(info.icon) / 2 + xOff,
                y + getHeight() / 2 - UIFont.getStringHeight(info.icon) / 2,
                Color.white);
    }

    @Override
    public void shader() {
    }

    public boolean mouseClicked(final double mouseX, final double mouseY) {
        return RenderUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), getHeight());
    }

    private static final class CategoryInfo {
        private final String icon;
        private final float offset, verticalOffset;
        private final int fontSize;

        private CategoryInfo(final String icon, final float offset, final float verticalOffset, final int fontSize) {
            this.icon = icon;
            this.offset = offset;
            this.fontSize = fontSize;
            this.verticalOffset = verticalOffset;
        }

        private CategoryInfo(final String icon, final float offset, final int fontSize) {
            this(icon, offset, 0, fontSize);
        }
    }
}
