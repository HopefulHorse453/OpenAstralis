package cc.astralis.module.impl.visual;

import cc.astralis.Astralis;
import cc.astralis.drag.Draggable;
import cc.astralis.interfaces.access.IDrag;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.util.player.EffectUtil;
import cc.astralis.skija.utils.SkijaUtil;
import java.awt.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;

public class PotionIndicatorModule extends Module {
    private final Draggable draggable;

    public PotionIndicatorModule() {
        super(Category.VISUAL);
        draggable = new Draggable("Potion Indicator", 4, 50, 100, 100);
        IDrag.draggables.add(draggable);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        final float x = draggable.getX(), y = draggable.getY();
        final float width = 100, height = 15;
        int statusY = (int) (y + 15), index = 0;
        final int totalEffects = EffectUtil.getEffects().size();
        final Color bgColor = new Color(30, 30, 30);
        final float radius = !mc.player.getActiveEffects().isEmpty() ? 0 : 3;

        SkijaUtil.roundedRectangleVarying(x, y, width, height, 3, 3, radius, radius, Astralis.getInstance().getFirstColor());

       /* product_regular_8.drawString("Active Potion Effects",
                x + (width - product_regular_8.getStringWidth("Active Potion Effects")) / 2,
                y + (height - product_regular_8.getStringHeight("Active Potion Effects")) / 2, Color.white);*/

        product_regular_8.drawCenterStringInBox(
                "Active Potion Effects",
                x, y, width, height,
                Color.white
        );

        for (MobEffectInstance potionEffect : mc.player.getActiveEffects()) {
            if (index != totalEffects - 1)
                SkijaUtil.rectangle(x, statusY, width, 10, bgColor);
            else
                SkijaUtil.roundedRectangleVarying(x, statusY, width, 12, 0, 0, 3, 3, bgColor);

            String effect = EffectUtil.getStatusEffectDescription(potionEffect).getString();
            String duration = MobEffectUtil.formatDuration(potionEffect, 1.0F, mc.level.tickRateManager().tickrate()).getString();

            product_regular_8.drawCenterStringInBox(effect + " " + duration,
                    x, statusY, width, 10, Color.white
            );

            statusY += 10;
            index++;
        }
    }
}
