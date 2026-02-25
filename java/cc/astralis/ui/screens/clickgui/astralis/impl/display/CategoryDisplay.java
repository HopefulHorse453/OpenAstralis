package cc.astralis.ui.screens.clickgui.astralis.impl.display;

import cc.astralis.Astralis;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.ui.screens.clickgui.astralis.Component;
import cc.astralis.util.render.RenderUtil;
import cc.astralis.skija.utils.SkijaUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CategoryDisplay extends Component {
    private final Category category;
    private final List<ModuleDisplay> displays = new ArrayList<>();

    private static final int MODULE_PADDING = 7;
    private static final int MODULE_HEIGHT = 31;

    private float mouseX, mouseY, offset;

    public CategoryDisplay(final Category category, final float width, final float height) {
        this.category = category;
        setWidth(width);
        setHeight(height);

        for (final Module module : Astralis.getInstance().getModuleManager().getModulesFromCategory(category)
                .stream()
                .sorted(Comparator.comparing(Module::getName))
                .toList()) {
            final ModuleDisplay display = new ModuleDisplay(module, (width - (MODULE_PADDING) * 2), MODULE_HEIGHT);
            displays.add(display);
        }
    }

    @Override
    public void render(final float x, final float y, final float mouseX, final float mouseY) {
        setX(x);
        setY(y);
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        float displayHeight = MODULE_PADDING;
        for (final ModuleDisplay display : displays) {
            displayHeight += display.getHeight() + MODULE_PADDING;
        }

        final float height = getHeight();
        offset = Math.clamp(offset, 0, Math.max(displayHeight, height) - height);

        final float moduleX = x + MODULE_PADDING;
        final float[] moduleY = {y + MODULE_PADDING - offset};

        SkijaUtil.scissored(x, y, getWidth(), getHeight(), () -> {
            for (final ModuleDisplay display : displays) {
                display.render(moduleX, moduleY[0], mouseX, mouseY);
                moduleY[0] += display.getHeight() + MODULE_PADDING;
            }
        });
    }

    @Override
    public void mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (RenderUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            for (final ModuleDisplay display : displays) {
                display.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseReleased(final double mouseX, final double mouseY, final int button) {
        for (final ModuleDisplay display : displays) {
            display.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseScrolled(final double amount) {
        if (RenderUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            offset += (float) (amount * 20);
        }
    }
}
