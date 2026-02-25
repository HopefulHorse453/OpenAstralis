package cc.astralis.module.impl.visual;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.client.SuffixChangeEvent;
import cc.astralis.event.events.impl.client.ToggleModuleEvent;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.event.events.impl.render.ShaderEvent;
import cc.astralis.event.types.Priority;
import cc.astralis.font.FontManager;
import cc.astralis.font.UIFont;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.ui.animations.Animation;
import cc.astralis.util.render.ColorUtil;
import com.mojang.blaze3d.platform.Window;
import cc.astralis.skija.utils.SkijaUtil;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ArraylistModule extends Module {
    private final NumberProperty fontSize = new NumberProperty("Font Size", 10, 0, 50, 1);
    private final ModeProperty hiddenCategories = new ModeProperty("Hidden Categories",
            "None", "Combat", "Exploit", "Movement", "Player", "Visual", "World", "None");

    private final NumberProperty animationSpeed = new NumberProperty("Animation Speed", 300, 1, 2000, 1);
    private final NumberProperty colorSpeed = new NumberProperty("Color Speed", 10, 1, 50, 1);

    private final BooleanProperty backGround = new BooleanProperty("Back Ground", true);
    private final BooleanProperty sideLine = new BooleanProperty("Side Line", true);
    private final BooleanProperty fullOutline = new BooleanProperty("Full Outline", false);

    private List<String> hiddenModules = new ArrayList<>();

    private record ModuleEntry(Module module, String displayName, int width) {}
    private List<ModuleEntry> moduleEntries = new ArrayList<>();

    public ArraylistModule() {
        super(Category.VISUAL);
        registerProperties(fontSize, animationSpeed, colorSpeed, backGround, sideLine, fullOutline, hiddenCategories);
    }

    private void rebuildModules() {
        var hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);
        UIFont uiFont = FontManager.getFont(hud.fontMode.getProperty(), fontSize.getProperty().intValue());

        hiddenModules = Arrays.stream(hiddenCategories.getModes())
                .filter(hiddenCategories::is)
                .map(String::toUpperCase)
                .toList();

        moduleEntries = Astralis.getInstance().getModuleManager().getModules().stream()
                .filter(mod -> !hiddenModules.contains(mod.getCategory().name()))
                .map(mod -> new ModuleEntry(mod, mod.getDisplayName(true),
                        (int) uiFont.getFontRenderer().getStringWidth(mod.getDisplayName(true))))
                .sorted(Comparator.comparingInt(e -> -e.width))
                .toList();
    }

    @EventTarget(value = Priority.LOWEST)
    public void onRender2D(Render2DEvent event) {
        var hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);
        UIFont uiFont = FontManager.getFont(hud.fontMode.getProperty(), fontSize.getProperty().intValue());
        Window window = mc.getWindow();

        float y = mc.player.getActiveEffects().isEmpty() ? 4 : 25;
        int visibleIndex = 0;

        float lineHeight = fontSize.getProperty().intValue() + 4;

        ModuleEntry firstVisible = null, lastVisible = null;
        for (ModuleEntry entry : moduleEntries) {
            if (entry.module().isToggled() && !entry.module().isHidden()) {
                if (firstVisible == null) firstVisible = entry;
                lastVisible = entry;
            }
        }

        for (ModuleEntry entry : moduleEntries) {
            Module module = entry.module();

            Animation animation = Astralis.getInstance().getAnimationManager().getAnimation(module);
            animation.run(module.isToggled() && !module.isHidden() ? 1f : 0f);
            animation.setDuration(animationSpeed.getProperty().longValue());

            float progress = (float) animation.getValue();
            if (progress <= 0) continue;

            float baseWidth = 5 + entry.width;
            float width = baseWidth * progress;
            float height = lineHeight * progress;

            float elementX = window.getGuiScaledWidth() - 4;
            float elementY = y + height;
            float offset = 2;

            Color fadeColor = switch (hud.colorMode.getProperty()) {
                case "Rainbow" -> HudModule.getRainbow(3200, visibleIndex * 160, 0.8F, 1.0F);
                default -> ColorUtil.interpolateColorsBackAndForth(
                        colorSpeed.getProperty().intValue(), 3 + (visibleIndex * 20),
                        Astralis.getInstance().getFirstColor(),
                        Astralis.getInstance().getSecondColor(), false
                );
            };

            if (backGround.getProperty()) {
                SkijaUtil.drawRectPoint4(window.getGuiScaledWidth() - width - offset, y,
                        elementX + (sideLine.getProperty() ? 2 : 1), elementY,
                        new Color(15, 15, 18, hud.backgroundAlpha.getProperty().intValue()));
            }

            if (sideLine.getProperty()) {
                SkijaUtil.drawRectPoint4(window.getGuiScaledWidth() - 2, y,
                        elementX + 1, elementY, fadeColor);
            }

            if (fullOutline.getProperty()) {
                float outlineX1 = window.getGuiScaledWidth() - width - offset;
                float outlineX2 = window.getGuiScaledWidth() - 3;

                SkijaUtil.drawRectPoint4(outlineX1 - 1, y, outlineX1, elementY, fadeColor);

                if (entry == firstVisible)
                    SkijaUtil.drawRectPoint4(outlineX1, y, outlineX2, y + 1, fadeColor);

                if (entry == lastVisible)
                    SkijaUtil.drawRectPoint4(outlineX1 - 1, elementY, outlineX2 + 1, elementY + 1, fadeColor);

                int currentIndex = moduleEntries.indexOf(entry);
                for (int i = currentIndex + 1; i < moduleEntries.size(); i++) {
                    ModuleEntry nextEntry = moduleEntries.get(i);
                    if (nextEntry.module().isToggled() && !nextEntry.module().isHidden()) {
                        Animation nextAnim = Astralis.getInstance().getAnimationManager().getAnimation(nextEntry.module());
                        float nextWidth = 5 + nextEntry.width * (float) nextAnim.getValue();
                        float connectorEnd = window.getGuiScaledWidth() - nextWidth - offset;
                        if (Math.abs(outlineX1 - connectorEnd) >= 1) {
                            SkijaUtil.drawRectPoint4(outlineX1 - 1, elementY, connectorEnd, elementY + 1, fadeColor);
                        }

                        break;
                    }
                }
            }

            SkijaUtil.push();
            SkijaUtil.translate(window.getGuiScaledWidth() - (offset + ((baseWidth - 1.5F) * progress)), y + 1);
            SkijaUtil.scale(progress, progress);
            uiFont.getFontRenderer().drawStringWithShadow(entry.displayName, 0, 0, fadeColor);
            SkijaUtil.pop();

            y += height;
            visibleIndex++;
        }
    }

    @EventTarget
    public void onShader(ShaderEvent event) {
        var hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);
        UIFont uiFont = FontManager.getFont(hud.fontMode.getProperty(), fontSize.getProperty().intValue());
        Window window = mc.getWindow();

        float y = mc.player.getActiveEffects().isEmpty() ? 4 : 25;
        float lineHeight = fontSize.getProperty().intValue() + 4;

        for (ModuleEntry entry : moduleEntries) {
            Module module = entry.module();
            Animation animation = Astralis.getInstance().getAnimationManager().getAnimation(module);

            animation.run(module.isToggled() && !module.isHidden() ? 1f : 0f);
            animation.setDuration(animationSpeed.getProperty().longValue());

            float progress = (float) animation.getValue();
            if (progress <= 0) continue;

            float baseWidth = 5 + entry.width;
            float width = baseWidth * progress;
            float height = lineHeight * progress;

            float elementX = window.getGuiScaledWidth() - 4;
            float elementY = y + height;
            float offset = 2;

            if (backGround.getProperty()) {
                SkijaUtil.drawShaderPoint4Rectangle(window.getGuiScaledWidth() - width - offset, y ,
                        elementX + (sideLine.getProperty() ? 2 : 1), elementY);
            }

            y += height;
        }
    }

    @EventTarget
    public void onSuffixChange(SuffixChangeEvent event) {
        rebuildModules();
    }

    @EventTarget
    public void onToggleModule(ToggleModuleEvent event) {
        rebuildModules();
    }
}