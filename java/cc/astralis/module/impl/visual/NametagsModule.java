package cc.astralis.module.impl.visual;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.event.events.impl.render.ShaderEvent;
import cc.astralis.font.UIFont;
import cc.astralis.font.FontManager;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.module.impl.combat.AntiBotModule;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.ColorProperty;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.render.ColorUtil;
import cc.astralis.util.render.W2SUtil;
import cc.astralis.skija.utils.SkijaUtil;
import org.joml.Vector4d;

import java.awt.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class NametagsModule extends Module {
    private final ModeProperty fontMode = new ModeProperty("Font", "Product Sans Regular",
            "Product Sans Regular", "Product Sans Bold",
            "JetBrains-Regular", "JetBrains-Bold",
            "Poppins-Regular", "Poppins-Bold",
            "Sf-Regular", "Sf-Bold", "Tahoma"
    );
    private final BooleanProperty texShadow = new BooleanProperty("Text Shadow", true);
    private final BooleanProperty background = new BooleanProperty("Background", true);
    private final BooleanProperty showHealth = new BooleanProperty("Show Health", true);
    private final ColorProperty textColor = new ColorProperty("Text Color", Color.WHITE);
    private final ColorProperty backgroundColor = new ColorProperty("Background Color", new Color(0, 0, 0, 150));
    private final NumberProperty cornerRadius = new NumberProperty("Corner Radius", 4f, 0f, 10f, 0.5f);
    private final NumberProperty padding = new NumberProperty("Padding", 3f, 0f, 10f, 0.5f);
    private final NumberProperty healthGap = new NumberProperty("Health Gap", 3f, 0f, 10f, 0.5f);
    private final BooleanProperty applyShaders = new BooleanProperty("Apply Shaders", true);

    public NametagsModule() {
        super(Category.VISUAL);
        this.registerProperties(fontMode, texShadow, background, showHealth,
                textColor, backgroundColor, cornerRadius, padding, healthGap, applyShaders
        );
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (mc.getEntityRenderDispatcher().camera == null || mc.level == null || mc.player == null) return;

        final HudModule hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);

        for (final Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof Player living)) continue;
            if (AntiBotModule.isBot(living)) continue;
            if (!frustum.isVisible(entity.getBoundingBox())) continue;

            final Vector4d screenPos = W2SUtil.calculateScreenPosition(entity);
            if (screenPos == null) continue;

            final float posX = (float) screenPos.x, posY = (float) screenPos.y, endPosX = (float) screenPos.z;
            final Color bgColor = ColorUtil.withAlpha(backgroundColor.getProperty(), hud.backgroundAlpha.getProperty().intValue());
            final UIFont font = FontManager.getFont(fontMode.getProperty(), 9);
            final float centerX = (posX + endPosX) / 2.0f, nameY = posY - 12.0f;

            final String nameText = entity.getName().getString();
            final float nameWidth = font.getStringWidth(nameText), textHeight = font.getStringHeight(nameText);
            final float namePadding = padding.getProperty().floatValue();
            final float nameBoxWidth = nameWidth + namePadding * 4, nameBoxHeight = textHeight + namePadding * 2;
            final float cornerRadiusValue = cornerRadius.getProperty().floatValue();

            SkijaUtil.beginShaderFrame();

            final boolean renderHealth = showHealth.getProperty();
            float startX = centerX - nameBoxWidth / 2f;

            final float health = living.getHealth();
            final String healthText = String.format("%.1f", health);
            final float healthWidth = font.getStringWidth(healthText);
            final float healthPadding = namePadding * 0.8f;
            final float healthBoxWidth = healthWidth + healthPadding * 3;
            final float healthGapValue = healthGap.getProperty().floatValue();
            final float totalWidth = nameBoxWidth + healthGapValue + healthBoxWidth;

            if (renderHealth) {
                startX = centerX - totalWidth / 2f;
            }

            float y = nameY - (textHeight + healthPadding * 1.5f) / 2 + textHeight / 4;

            if (background.getProperty()) {
                SkijaUtil.beginShaderFrame();

                if (renderHealth) {
                    SkijaUtil.drawShaderRoundRectangle(startX, nameY - nameBoxHeight / 2 + textHeight / 4, nameBoxWidth, nameBoxHeight, cornerRadiusValue);
                    SkijaUtil.drawShaderRoundRectangle(startX + nameBoxWidth + healthGapValue,
                            y,
                            healthBoxWidth, textHeight + healthPadding * 1.5f, cornerRadiusValue);
                } else {
                    SkijaUtil.drawShaderRoundRectangle(centerX - nameBoxWidth / 2, nameY - nameBoxHeight / 2 + textHeight / 4, nameBoxWidth, nameBoxHeight, cornerRadiusValue);
                }

                SkijaUtil.drawShaders(true, hud.blurRadius.getProperty().floatValue(), false, 69f);
            }

            if (background.getProperty()) {
                if (renderHealth) {
                    SkijaUtil.roundedRectangle(startX, nameY - nameBoxHeight / 2 + textHeight / 4, nameBoxWidth, nameBoxHeight, cornerRadiusValue, bgColor);
                    SkijaUtil.roundedRectangle(startX + nameBoxWidth + healthGapValue,
                            y,
                            healthBoxWidth, textHeight + healthPadding * 1.5f, cornerRadiusValue, bgColor);
                } else {
                    SkijaUtil.roundedRectangle(centerX - nameBoxWidth / 2, nameY - nameBoxHeight / 2 + textHeight / 4,
                            nameBoxWidth, nameBoxHeight, cornerRadiusValue, bgColor);
                }
            }

            final float nameX = renderHealth ? startX + namePadding * 2 : centerX - nameWidth / 2;
            font.drawString(nameText, nameX, nameY - textHeight / 3.2F, textColor.getProperty(), texShadow.getProperty());

            if (renderHealth) {
                font.drawString(healthText, startX + nameBoxWidth + healthGapValue + healthPadding * 1.5f,
                        nameY - textHeight / 3.2F, Color.white, texShadow.getProperty());
            }
        }
    }
}
