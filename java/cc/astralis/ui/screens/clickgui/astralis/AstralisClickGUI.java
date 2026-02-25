package cc.astralis.ui.screens.clickgui.astralis;

import cc.astralis.Astralis;
import cc.astralis.font.UIFont;
import cc.astralis.font.FontManager;
import cc.astralis.module.Category;
import cc.astralis.module.impl.visual.HudModule;
import cc.astralis.ui.cloud.CloudConfigComponent;
import cc.astralis.ui.screens.clickgui.astralis.impl.CategorySelector;
import cc.astralis.ui.screens.clickgui.astralis.impl.display.CategoryDisplay;
import cc.astralis.util.language.Lazy;
import cc.astralis.skija.SkijaManager;
import cc.astralis.skija.utils.SkijaHelperUtil;
import cc.astralis.skija.utils.SkijaUtil;
import io.github.humbleui.skija.FilterTileMode;
import io.github.humbleui.skija.ImageFilter;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.RRect;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class AstralisClickGUI extends Screen {
    private static final Color BACKGROUND = new Color(25, 25, 25);
    private static final Color FOREGROUND = new Color(30, 30, 30);
//    private static final Color OUTLINE = new Color(60, 60, 60);

    private static final int WIDTH = 360, HEIGHT = 218, CATEGORY_PADDING = 7, SIDE_WIDTH = 35, CATEGORY_SIZE = SIDE_WIDTH - (CATEGORY_PADDING * 2), VERTICAL_CATEGORY_PADDING = 5;
//    private static final float OUTLINE_SIZE = 1.5F;
    private final float categoriesHeight;
    private final List<CategorySelector> selectors = new ArrayList<>();
    private final Map<CategorySelector, CategoryDisplay> displays = new HashMap<>();
    private CategorySelector selector;

    private static final Lazy<Paint> shadowPaint = Lazy.of(() -> {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);

        return paint;
    });

    public AstralisClickGUI() {
        super(Component.nullToEmpty(""));

        float categoriesHeight = 0;
        for (final Category category : Category.values()) {
            final CategorySelector selector = new CategorySelector(category, CATEGORY_SIZE);
            selectors.add(selector);
            categoriesHeight += selector.getHeight() + VERTICAL_CATEGORY_PADDING;

            if (category == Category.COMBAT) {
                this.selector = selector;
            }

            final CategoryDisplay display = new CategoryDisplay(category, WIDTH - SIDE_WIDTH, HEIGHT);
            displays.put(selector, display);
        }

        this.categoriesHeight = categoriesHeight - VERTICAL_CATEGORY_PADDING;
    }

    @Override
    public void renderBackground(final GuiGraphics context, final int mouseX, final int mouseY, final float deltaTicks) {

    }

    @Override
    public void render(final GuiGraphics context, final int mouseX, final int mouseY, final float deltaTicks) {
        SkijaManager.addCallback(() -> {
            if (Astralis.getInstance().getModuleManager().getModule(HudModule.class).bloom.getProperty()) {
                final float x = (this.width - WIDTH) * 0.5F;
                final float y = (this.height - HEIGHT) * 0.5F;

                final float shadowOffset = 6;

                RRect shadowRect = RRect.makeXYWH(
                        SkijaHelperUtil.s(x),
                        SkijaHelperUtil.s(y),
                        SkijaHelperUtil.s(WIDTH + shadowOffset),
                        SkijaHelperUtil.s(HEIGHT + shadowOffset),
                        SkijaHelperUtil.s(8f)
                );

                shadowPaint.get()
                        .setColor(Color.black.getRGB())
                        .setImageFilter(ImageFilter.makeBlur(20, 20, FilterTileMode.DECAL))
                        .setAntiAlias(true);

                SkijaUtil.getCanvas().saveLayer(null, null);
                SkijaUtil.getCanvas().drawRRect(shadowRect, shadowPaint.get());
                SkijaUtil.getCanvas().restore();
            }

            render(mouseX, mouseY);
            CloudConfigComponent.getInstance().render(mouseX, mouseY);
        });
    }

    private void render(final float mouseX, final float mouseY) {
        final float x = (this.width - WIDTH) * 0.5F;
        final float y = (this.height - HEIGHT) * 0.5F;

        SkijaUtil.roundedRectangle(x, y, WIDTH, HEIGHT, 8, BACKGROUND);
        SkijaUtil.roundedRectangleVarying(x, y, SIDE_WIDTH, HEIGHT, 8, 0, 0, 8, FOREGROUND);

        final UIFont UIFont = FontManager.getFont("Sf-Bold", CATEGORY_SIZE);
        final HudModule hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);
        final float logoY = y + CATEGORY_PADDING;
        UIFont.drawStringWithShadow("A", x + CATEGORY_PADDING + 3.5F, logoY, hud.firstColor.getProperty());

        final float categoryX = x + CATEGORY_PADDING;
        float categoryY = logoY + CATEGORY_SIZE + 15;

        for (final CategorySelector selector : selectors) {
            selector.render(categoryX, categoryY, mouseX, mouseY);
            if (this.selector == selector) {
                final int padding = 6;
                SkijaUtil.roundedRectangleVarying(categoryX + padding, categoryY + selector.getHeight(), selector.getWidth() - (padding * 2), 1.5F, 0, 0, 1, 1, Color.white);
            }

            categoryY += selector.getHeight() + VERTICAL_CATEGORY_PADDING;
        }

        displays.get(selector).render(x + SIDE_WIDTH, y, mouseX, mouseY);
    }


    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (CloudConfigComponent.getInstance().mouseClicked(click, doubled))
            return true;

        if (!CloudConfigComponent.getInstance().isVisible()) {
            displays.get(selector).mouseClicked(click.x(), click.y(), click.button());
            for (final CategorySelector selector : selectors) {
                if (selector.mouseClicked(click.x(), click.y())) {
                    this.selector = selector;
                    break;
                }
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        displays.get(selector).mouseReleased(click.x(), click.y(), click.button());
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (CloudConfigComponent.getInstance().mouseDragged(click, offsetX, offsetY))
            return true;

        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount != 0) {
            displays.get(selector).mouseScrolled(-verticalAmount);
        }

        if (CloudConfigComponent.getInstance().mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
            return true;

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (CloudConfigComponent.getInstance().keyPressed(input))
            return true;

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (CloudConfigComponent.getInstance().charTyped(input))
            return true;

        return super.charTyped(input);
    }
}
