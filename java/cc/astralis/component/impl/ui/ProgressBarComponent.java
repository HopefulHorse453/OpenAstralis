package cc.astralis.component.impl.ui;

import cc.astralis.Astralis;
import cc.astralis.component.Component;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.interfaces.IAccess;
import cc.astralis.ui.progress.ProgressBar;
import com.mojang.blaze3d.platform.Window;
import cc.astralis.skija.utils.SkijaUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProgressBarComponent extends Component implements IAccess {
    private static final Map<String, ProgressBar> BARS = new LinkedHashMap<>();

    @EventTarget
    public void onRender2D(Render2DEvent e) {
        if (BARS.isEmpty()) return;

        final Window window = mc.getWindow();
        final int width = window.getGuiScaledWidth();
        final int height = window.getGuiScaledHeight();

        final float centerX = width / 2f;
        float centerY = height / 2f + 30f;

        final List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, ProgressBar> entry : BARS.entrySet()) {
            final String id = entry.getKey();
            final ProgressBar bar = entry.getValue();

            bar.setDisplayed(bar.getDisplayed() + (bar.getProgress() - bar.getDisplayed()) * 0.2f);

            if (!bar.isCompleted() && bar.getProgress() >= 0.999f && bar.getDisplayed() >= 0.999f) {
                bar.setCompleted(true);
                bar.setRemoving(true);
            }

            final boolean isVisible = bar.getProgress() > 0f || bar.getDisplayed() > 0.001f;
            final boolean targetVisible = !bar.isRemoving() && isVisible;

            bar.getPopAnimation().run(targetVisible ? 1f : 0f);
            final float anim = (float) bar.getPopAnimation().getValue();

            if (bar.isRemoving() && anim <= 0f) {
                toRemove.add(id);
                continue;
            }

            if (anim <= 0f) continue;

            final float barWidth = 200f;
            final float barHeight = 8f;
            final float x = centerX - barWidth / 2f;
            final float y = centerY;

            SkijaUtil.push();
            {
                final float cx = x + barWidth / 2f;
                final float cy = y + barHeight / 2f;

                SkijaUtil.translate(cx, cy);
                SkijaUtil.scale(anim, anim);
                SkijaUtil.translate(-cx, -cy);

                SkijaUtil.roundedRectangle(x, y, barWidth, barHeight, 4, new Color(25, 25, 25));

                final float fillWidth = bar.getDisplayed() * barWidth;
                if (fillWidth > 0.1f) {
                    SkijaUtil.scissored(x, y, fillWidth, barHeight, () -> {
                        SkijaUtil.roundedRectangleGradient(
                                x, y, barWidth, barHeight, 4,
                                Astralis.getInstance().getFirstColor(),
                                Astralis.getInstance().getSecondColor(),
                                false
                        );
                    });
                }
            }
            SkijaUtil.pop();

            centerY += barHeight + 8f;
        }

        toRemove.forEach(BARS::remove);
    }

    public static void createBar(String id) {
        BARS.computeIfAbsent(id, ProgressBar::new);
    }

    public static void updateBar(String id, float progress) {
        ProgressBar bar = BARS.get(id);
        if (bar == null) return;

        if (bar.isRemoving() || bar.isCompleted()) {
            bar.setCompleted(false);
            bar.setRemoving(false);
            bar.getPopAnimation().reset();
        }

        bar.setProgress(progress);
    }

    public static void removeBar(String id) {
        ProgressBar bar = BARS.get(id);
        if (bar != null) {
            bar.setCompleted(true);
            bar.setRemoving(true);
        }
    }

    public static void clear() {
        BARS.clear();
    }
}