package cc.astralis.ui.screens.clickgui.astralis.impl.display;

import cc.astralis.Astralis;
import cc.astralis.font.FontManager;
import cc.astralis.module.Module;
import cc.astralis.module.impl.visual.HudModule;
import cc.astralis.property.Property;
import cc.astralis.property.properties.*;
import cc.astralis.ui.animations.Animation;
import cc.astralis.ui.animations.Easing;
import cc.astralis.ui.screens.clickgui.astralis.Component;
import cc.astralis.ui.screens.clickgui.astralis.impl.display.property.*;
import cc.astralis.util.render.RenderUtil;
import cc.astralis.skija.utils.SkijaUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleDisplay extends Component {
    private final Module module;
    private boolean extended;

    private static final Color START = new Color(30, 30, 30);
    private static final Color FOREGROUND = new Color(33, 33, 33);
    public static final Color DESCRIPTION = new Color(180, 180, 180).darker();
    public static final int PROPERTY_PADDING = 4, PADDING = 6, MAX_LENGTH = 60;
    private static final String POSTFIX = "...";

    private final List<PropertyEntry> displays = new ArrayList<>();
    private final Animation extendedInOut = new Animation(Easing.EASE_IN_OUT_SINE, 300);
    private final Animation enabledInOut = new Animation(Easing.EASE_IN_OUT_SINE, 300);

    public ModuleDisplay(final Module module, final float width, final float height) {
        this.module = module;
        setWidth(width);
        setHeight(height);

        final int propertyHeight = 16;
        for (final Property<?> property : module.getPropertyList()) {
            switch (property) {
                case BooleanProperty p -> displays.add(new PropertyEntry(property, new BooleanDisplay(p, getWidth(), propertyHeight)));
                case ModeProperty p -> displays.add(new PropertyEntry(property, new ModeDisplay(p, getWidth(), propertyHeight)));
                case ClassModeProperty p -> displays.add(new PropertyEntry(property, new ClassModeDisplay(p, getWidth(), propertyHeight)));
                case NumberProperty p -> displays.add(new PropertyEntry(property, new NumberDisplay(p, getWidth(), propertyHeight)));
                case ColorProperty p -> displays.add(new PropertyEntry(property, new ColorDisplay(p, getWidth(), propertyHeight)));
                default -> { /* w */ }
            }
        }
    }


    @Override
    public void render(final float x, final float y, final float mouseX, final float mouseY) {
        setX(x);
        setY(y);

        final float textY = y + PADDING - 2;
        extendedInOut.run(extended ? 1 : 0);

        SkijaUtil.roundedRectangle(x, y, getWidth(), getHeight(), 5, START);
        SkijaUtil.roundedRectangle(x, y, getWidth(), super.getHeight(), 5, FOREGROUND);

        FontManager.getFont("Sf-Ui", 11).drawStringWithShadow(module.getName(), x + PADDING, textY, Color.white.darker());

        String desc = module.getDesc();
        if (desc.length() > MAX_LENGTH) {
            desc = desc.substring(0, MAX_LENGTH - POSTFIX.length()) + POSTFIX;
        }

        FontManager.getFont("Sf-Ui", 10).drawStringWithShadow(desc, x + PADDING, textY + 11, DESCRIPTION);

        final float enabledSize = super.getHeight() - (PADDING * 2);
        SkijaUtil.roundedRectangle(x + getWidth() - (enabledSize + PADDING), y + PADDING, enabledSize, enabledSize, 4, new Color(28, 28, 28));

        if (module.isToggled()) {
            final HudModule hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);
            SkijaUtil.roundedRectangleDiagonalGradient(x + getWidth() - (enabledSize + PADDING), y + PADDING, enabledSize, enabledSize, 4, hud.firstColor.getProperty(), hud.secondColor.getProperty());
        }

        if (extendedInOut.getValue() > 0) {
            SkijaUtil.scissored(x, y, getWidth(), getHeight(), () -> {
                float propertyY = y + super.getHeight() + 3;
                for (final PropertyEntry propertyEntry : displays) {
                    if (propertyEntry.property.getVisible().get()) {
                        propertyEntry.component().render(x, propertyY, mouseX, mouseY);
                        propertyY += propertyEntry.component.getHeight() + PROPERTY_PADDING;
                    }
                }
            });
        }
    }

    @Override
    public void mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (RenderUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), super.getHeight())) {
            if (button == 0) {
                module.toggle();
            } else if (!displays.isEmpty()) {
                extended = !extended;
                extendedInOut.setStartPoint(extendedInOut.getValue());
            }
        } else if (RenderUtil.isHovered(mouseX, mouseY, getX(), getY() + super.getHeight(), getWidth(), getHeight())) {
            for (final PropertyEntry propertyEntry : displays) {
                if (propertyEntry.property.getVisible().get()) {

                    propertyEntry.component.mouseClicked(mouseX, mouseY, button);
                }
            }
        }
    }

    @Override
    public void mouseReleased(final double mouseX, final double mouseY, final int button) {
        for (final PropertyEntry propertyEntry : displays) {
            if (propertyEntry.property.getVisible().get()) {
                propertyEntry.component().mouseReleased(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public float getHeight() {
        float height = 0;
        if (extendedInOut.getValue() > 0 && !displays.isEmpty()) {
            for (final PropertyEntry propertyEntry : displays) {
                if (propertyEntry.property.getVisible().get()) {
                    height += propertyEntry.component.getHeight() + PROPERTY_PADDING;
                }
            }

            height += 6;
            height -= PROPERTY_PADDING;
        }

        return (float) (super.getHeight() + height * extendedInOut.getValue());
    }

    private record PropertyEntry(Property<?> property, Component component) {
        /* w */
    }
}
