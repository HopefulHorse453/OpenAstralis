package cc.astralis.module.impl.visual;

import cc.astralis.event.events.impl.render.ShaderEvent;
import cc.astralis.font.FontManager;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.util.io.ThreadUtil;
import cc.astralis.util.network.AccountUtil;
import cc.astralis.util.render.ColorUtil;
import cc.astralis.Astralis;
import cc.astralis.drag.Draggable;
import cc.astralis.ui.screens.client.HudEditorScreen;
import cc.astralis.interfaces.access.IDrag;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.module.impl.combat.KillauraModule;
import cc.astralis.ui.animations.Animation;
import cc.astralis.ui.animations.Easing;
import cc.astralis.skija.utils.SkijaUtil;
import cc.astralis.skija.io.ImageUtil;
import java.awt.*;
import java.text.DecimalFormat;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.world.entity.LivingEntity;

public class TargetHudModule extends Module {
    private final ModeProperty mode = new ModeProperty("Mode", "Modern", "Modern", "Legacy");

    private final Draggable draggable;
    private LivingEntity target;

    public final Animation healthAnimation = new Animation(Easing.EASE_IN_OUT_QUAD, 500);
    private double lastHealthValue = 0.0;

    private final Animation popAnimation = new Animation(Easing.EASE_IN_OUT_SINE, 300);

    private static final DecimalFormat FORMAT = new DecimalFormat("0.0");

    public TargetHudModule() {
        super(Category.VISUAL);
        draggable = new Draggable("Target Hud", 320, 300, 200, 100);
        IDrag.draggables.add(draggable);
        this.registerProperties(mode);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        this.setSuffix("Modern");

        LivingEntity newTarget = mc.screen instanceof HudEditorScreen ?
                mc.player : Astralis.getInstance().getModuleManager().getModule(KillauraModule.class).target;

        if (newTarget != null) {
            ImageUtil.clearImageCache();
            target = newTarget;
        }

        if (popAnimation.isFinished()) {
            popAnimation.setStartPoint(popAnimation.getValue());
        }

        popAnimation.run(newTarget != null ? 1 : 0);

        final float animValue = (float) popAnimation.getValue();
        if (animValue == 0) {
            return;
        }

   /*     if (popOutAnimation.getProgress() > 1) {
            healthAnimation.reset();
            lastHealthValue = -1;
            return;
        }*/

        int x = (int) draggable.getX(), y = (int) draggable.getY();
        double calculatedValue = target.getHealth() / target.getMaxHealth();

        if (target.getHealth() != lastHealthValue) {
            healthAnimation.setStartPoint(healthAnimation.getValue());
            healthAnimation.setEndPoint(calculatedValue);
            healthAnimation.reset();
            lastHealthValue = target.getHealth();
        }

        healthAnimation.run(calculatedValue);

        // let's hope we won't have more than lets say 3 target huds since using this approach makes it aids to scale to using more targethuds :brokean_heart:
        // collecting technical debt core :pray:.
        // - Kawase
        switch (mode.getProperty()) {
            case "Modern" -> drawModernTargetHud(x, y, animValue);
            case "Legacy" -> drawLegacyTargetHud(x, y, animValue);
        }
    }

    @EventTarget
    public void onShader(ShaderEvent event) {
        if (target == null) {
            return;
        }

        final float animValue = (float) popAnimation.getValue();
        if (animValue == 0) {
            return;
        }

        int x = (int) draggable.getX(), y = (int) draggable.getY();

        float width = "Modern".equals(mode.getProperty()) ? 180 : 150,
                height = "Modern".equals(mode.getProperty()) ? 55 : 46;

        float centerX = x + (width / 2f) * (1 - animValue);
        float centerY = y + (height / 2f) * (1 - animValue);

        width *= animValue;
        height *= animValue;

        switch (mode.getProperty()) {
            case "Modern" -> SkijaUtil.drawShaderRoundRectangle(centerX, centerY, width, height, 12f * animValue);
            case "Legacy" -> SkijaUtil.drawShaderRectangle(centerX, centerY, width, height);
        }
    }

    private void drawLegacyTargetHud(float x, float y, float animValue) {
        final int width = 150, height = 46, padding = 3, headWidth = height - padding * 2;

        SkijaUtil.push();

        float centerX = x + width / 2f;
        float centerY = y + height / 2f;

        SkijaUtil.translate(centerX, centerY);
        SkijaUtil.scale(animValue, animValue);
        SkijaUtil.translate(-centerX, -centerY);

        float startX = x + headWidth + padding * 2;

        SkijaUtil.drawRectPoint4(x, y, x + width, y + height, new Color(30, 30, 30, 180));

        final float hpWidth = width - ((startX - x) + padding * 2);
        final float animatedHealthWidth = (float) (hpWidth * healthAnimation.getValue());

        final int hpHeight = 12;

        final HudModule hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);

        final Color firstColor = switch (hud.colorMode.getProperty()) {
            case "Rainbow" -> HudModule.getRainbow(3000, 0, 0.7f, 1);
            default -> ColorUtil.getAccentColor(new UVPair(0, 6),
                    hud.firstColor.getProperty(), hud.secondColor.getProperty());
        };
        final Color secondColor = switch (hud.colorMode.getProperty()) {
            case "Rainbow" -> HudModule.getRainbow(4000, 0, 1f, 1);
            default -> ColorUtil.getAccentColor(new UVPair(0, 6),
                    hud.firstColor.getProperty(), hud.secondColor.getProperty());
        };

        SkijaUtil.drawRectPoint4(startX, y + height - (hpHeight + padding),
                startX + hpWidth, y + height - padding, new Color(25, 25, 25, 220));

        SkijaUtil.rectangleGradient(startX, y + height - (hpHeight + padding),
                animatedHealthWidth, hpHeight, firstColor, secondColor, false);

        SkijaUtil.renderImage(x + padding, y + padding, headWidth, headWidth,
                ImageUtil.loadSkin(target.getStringUUID()), null);

        if (target.getHealth() <= 0) {
            healthAnimation.reset();
            lastHealthValue = -1;
        }

        FontManager.getFont("Sf-Ui", 12).drawString(target.getName().getString(),
                startX, y + padding, Color.white);

        FontManager.getFont("Sf-Ui", 10).drawString(
                FORMAT.format(target.getHealth()) + " Health", startX, y + padding + 14, new Color(180, 180, 180));

        SkijaUtil.pop();
    }

    private void drawModernTargetHud(float x, float y, float animValue) {
        final int width = 180, height = 55;
        final float padding = 8f;
        final float cornerRadius = 12f;
        final float avatarSize = height - padding * 2;
        final float avatarRadius = 8f;

        SkijaUtil.push();
        float centerX = x + width / 2f;
        float centerY = y + height / 2f;

        SkijaUtil.translate(centerX, centerY);
        SkijaUtil.scale(animValue, animValue);
        SkijaUtil.translate(-centerX, -centerY);

        HudModule hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);
        SkijaUtil.roundedRectangle(x, y, width, height, cornerRadius,
                new Color(18, 18, 20, hud.backgroundAlpha.getProperty().intValue()));

      /*  SkijaUtil.roundedRectangleOutline(x, y, width, height, cornerRadius,
                new Color(0, 0, 0, 0), 1f, new Color(255, 255, 255, 15));*/

        float contentStartX = x + avatarSize + padding * 2;
        float contentWidth = width - avatarSize - padding * 3;

        String playerName = target.getName().getString();
        FontManager.getFont("Product Sans Bold", 14).drawString(playerName,
                contentStartX, y + padding - 1, Color.WHITE);

        String healthText = FORMAT.format(target.getHealth()) + " HP";
        FontManager.getFont("Product Sans Regular", 11).drawString(healthText,
                contentStartX, y + padding + 15, new Color(160, 160, 165));

        float healthBarY = y + height - padding - 8;
        final float healthBarHeight = 8;
        final float healthBarRadius = 5;

        SkijaUtil.roundedRectangle(contentStartX, healthBarY, contentWidth,
                healthBarHeight, healthBarRadius, new Color(35, 35, 40));

        final float animatedHealthWidth = (float) (contentWidth * healthAnimation.getValue());
        if (animatedHealthWidth > 0) {
            SkijaUtil.roundedRectangleGradient(
                    contentStartX - 1, healthBarY,
                    animatedHealthWidth + 1, healthBarHeight,
                    healthBarRadius,
                    Astralis.getInstance().getFirstColor(),
                    Astralis.getInstance().getSecondColor(),
                    false
            );
        }

        SkijaUtil.roundedRectangle(x + padding, y + padding, avatarSize, avatarSize,
                avatarRadius, new Color(45, 45, 50));

        SkijaUtil.renderRoundedImage(x + padding, y + padding, avatarSize, avatarSize, avatarRadius,
                ImageUtil.loadSkin(target.getStringUUID()), null);

        SkijaUtil.roundedRectangleGradientOutline(x + padding, y + padding, avatarSize, avatarSize, avatarRadius,
                Astralis.getInstance().getFirstColor(), Astralis.getInstance().getSecondColor(),true, 1.5f, new Color(0, 0, 0, 0));

      /*  SkijaUtil.roundedRectangleOutline(x + padding, y + padding, avatarSize, avatarSize,
                avatarRadius, new Color(0, 0, 0, 0), 2f, new Color(255, 255, 255, 35));*/
    /*    SkijaUtil.roundedRectangleGradient(x + 1, y + height * 0.2f, 3f, height * 0.6f, 1.5f,
                Astralis.getInstance().getFirstColor(), Astralis.getInstance().getSecondColor(), true);*/

        if (target.getHealth() <= 0) {
            healthAnimation.reset();
            lastHealthValue = -1;
        }

        SkijaUtil.pop();
    }
}
