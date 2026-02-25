package cc.astralis.module.impl.visual;

import cc.astralis.event.types.Priority;
import cc.astralis.module.impl.combat.AntiBotModule;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.ColorProperty;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.util.render.W2SUtil;
import cc.astralis.skija.utils.SkijaUtil;
import org.joml.Vector4d;

import java.awt.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class ProjectionESPModule extends Module {
    private final BooleanProperty mainBox = new BooleanProperty("Main Box", true),
            mainBoxShadow = new BooleanProperty("Main box Shadow", true);

    private final ModeProperty colorMode = new ModeProperty("Color Mode", "Client", "Client", "Custom");

    private final ColorProperty leftBoxTopColor = new ColorProperty("Left Box Top Color", Color.white),
            leftBoxBottomColor = new ColorProperty("Left Box Bottom Color", Color.white);
    private final ColorProperty rightBoxTopColor = new ColorProperty("Right Box Top Color", Color.white),
            rightBoxBottomColor = new ColorProperty("Right Box Bottom Color", Color.white);
    private final ColorProperty topBoxLeftColor = new ColorProperty("Top Box Left Color", Color.white),
            topBoxRightColor = new ColorProperty("Top Box Right Color", Color.white);
    private final ColorProperty bottomBoxLeftColor = new ColorProperty("Bottom Box Left Color", Color.white),
            bottomBoxRightColor = new ColorProperty("Bottom Box Right Color", Color.white);

    private final BooleanProperty fill = new BooleanProperty("Fill", true);
    private final ColorProperty fillInTopColor = new ColorProperty("Fill In Color", new Color(0, 0, 0, 100)),
            fillInBottomColor = new ColorProperty("Fill In Bottom Color", new Color(0, 0, 0, 100));
    private final BooleanProperty healthBar = new BooleanProperty("Health Bar", true),
            healthBarShadow = new BooleanProperty("Health Bar Shadow", false);
    private final ColorProperty topHealthBarColor = new ColorProperty("Top Health Bar Color", Color.red),
            bottomHealthBarColor = new ColorProperty("Bottom Health Bar Color", Color.white);

    public ProjectionESPModule() {
        super(Category.VISUAL);
        registerProperties(mainBox,
                mainBoxShadow.setVisible(mainBox::getProperty),
                leftBoxTopColor.setVisible(mainBox::getProperty),
                leftBoxBottomColor.setVisible(mainBox::getProperty),
                rightBoxTopColor.setVisible(mainBox::getProperty),
                rightBoxBottomColor.setVisible(mainBox::getProperty),
                topBoxLeftColor.setVisible(mainBox::getProperty),
                topBoxRightColor.setVisible(mainBox::getProperty),
                bottomBoxLeftColor.setVisible(mainBox::getProperty),
                bottomBoxRightColor.setVisible(mainBox::getProperty),
                fill, fillInTopColor.setVisible(fill::getProperty),
                fillInBottomColor.setVisible(fill::getProperty),
                healthBar, healthBarShadow.setVisible(healthBar::getProperty),
                topHealthBarColor.setVisible(healthBar::getProperty),
                bottomHealthBarColor.setVisible(healthBar::getProperty)
        );
    }

    @EventTarget(value = Priority.HIGHEST)
    private void onRender2D(Render2DEvent event) {
        if (mc.getEntityRenderDispatcher().camera == null || mc.level == null || mc.player == null) {
            return;
        }

        for (final Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity)) {
                continue;
            }

            if (!(entity instanceof Player) || entity == mc.player && mc.options.getCameraType().isFirstPerson()) {
                continue;
            }

            if (entity instanceof Player playerEntity && playerEntity.getId() == mc.player.getId()) {
                continue;
            }

            if (AntiBotModule.isBot((Player) entity))
                return;

            final AABB box = entity.getBoundingBox();

            if (!frustum.isVisible(box)) {
                continue;
            }

            final Vector4d position = W2SUtil.calculateScreenPosition(entity);

            if (position != null) {
                final float posX = (float) position.x, posY = (float) position.y;
                final float endPosX = (float) position.z, endPosY = (float) position.w;

                if (fill.getProperty()) {
                    SkijaUtil.drawRectPoint4VerticalGradient(posX, posY, endPosX, endPosY,
                            fillInTopColor.getProperty(), fillInBottomColor.getProperty());
                }

                // Box shadow
                if (mainBox.getProperty()) {
                    if (mainBoxShadow.getProperty()) {
                        SkijaUtil.drawRectPoint4(posX - 1.0f, posY, posX + 0.5f, endPosY + 0.5f, Color.BLACK);
                        SkijaUtil.drawRectPoint4(posX - 1.0f, posY - 0.5f, endPosX + 0.5f, posY + 1.0f, Color.BLACK);
                        SkijaUtil.drawRectPoint4(endPosX - 1.0f, posY, endPosX + 0.5f, endPosY + 0.5f, Color.BLACK);
                        SkijaUtil.drawRectPoint4(posX - 1.0f, endPosY - 1.0f, endPosX + 0.5f, endPosY + 0.5f, Color.BLACK);
                    }

                    // Actual box
                    SkijaUtil.drawRectPoint4VerticalGradient(posX - 0.5f, posY, posX, endPosY,
                            leftBoxTopColor.getProperty(), leftBoxBottomColor.getProperty());
                    SkijaUtil.drawRectPoint4HorizontalGradient(posX, endPosY - 0.5f, endPosX, endPosY,
                            bottomBoxLeftColor.getProperty(), bottomBoxRightColor.getProperty());
                    SkijaUtil.drawRectPoint4HorizontalGradient(posX - 0.5f, posY, endPosX, posY + 0.5f,
                            topBoxLeftColor.getProperty(), topBoxRightColor.getProperty());
                    SkijaUtil.drawRectPoint4VerticalGradient(endPosX - 0.5f, posY, endPosX, endPosY,
                            rightBoxTopColor.getProperty(), rightBoxBottomColor.getProperty());
                }

                float healthRatio = ((Player) entity).getHealth() / ((Player) entity).getMaxHealth();
                float healthBarTop = endPosY + (posY - endPosY) * healthRatio;

                if (healthBar.getProperty()) {
                    // Health bar shadow
                    if (healthBarShadow.getProperty()) {
                        float yOffset = 0.3f;
                        SkijaUtil.drawRectPoint4(posX - 5.5f, posY, posX - 2.5f, endPosY, Color.BLACK);
                        SkijaUtil.drawRectPoint4(posX - 5.0f, healthBarTop - yOffset, posX - 3f, healthBarTop, Color.BLACK);
                        SkijaUtil.drawRectPoint4(posX - 5.0f, endPosY, posX - 3f, endPosY + yOffset, Color.BLACK);
                    }

                    // Actual health bar
                    SkijaUtil.drawRectPoint4VerticalGradient(posX - 5.0f, healthBarTop, posX - 3.0f, endPosY,
                            topHealthBarColor.getProperty(), bottomHealthBarColor.getProperty());
                }
            }
        }
    }
}