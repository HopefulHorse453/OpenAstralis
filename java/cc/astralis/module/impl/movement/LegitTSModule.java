package cc.astralis.module.impl.movement;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.input.ModifyMovementEvent;
import cc.astralis.event.events.impl.render.Render3DEvent;
import cc.astralis.event.types.Priority;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.module.impl.combat.KillauraModule;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.render.Render3DUtil;
import net.minecraft.world.entity.LivingEntity;

public class LegitTSModule extends Module {
    private final NumberProperty radius = new NumberProperty("Range", 0.6f, 0, 5, 0.1f);

    private final BooleanProperty onlyWithSpace = new BooleanProperty("Only with Space", false);

    private double angle;

    public LegitTSModule() {
        super(Category.MOVEMENT);
        this.registerProperties(radius, onlyWithSpace);
    }

    @EventTarget(Priority.LOWEST)
    public void onMovementInput(ModifyMovementEvent event) {
        this.setSuffix(String.valueOf(radius.getProperty().floatValue()));
        LivingEntity target = Astralis.getInstance().getModuleManager().getModule(KillauraModule.class).target;

        if (target == null ||
                !Astralis.getInstance().getModuleManager().getModule(SpeedModule.class).isToggled() &&
                        !Astralis.getInstance().getModuleManager().getModule(ElytraFlightModule.class).isToggled() &&
                        !Astralis.getInstance().getModuleManager().getModule(FlightModule.class).isToggled()|| !mc.player.hasLineOfSight(target)
        ) {
            return;
        }

        if (onlyWithSpace.getProperty() && !mc.options.keyJump.isDown())
            return;

        LivingEntity targetPosition = Astralis.getInstance().getModuleManager().getModule(KillauraModule.class).target;
        angle += 1;

        double offsetX = radius.getProperty().floatValue() * Math.cos(angle);
        double offsetZ = radius.getProperty().floatValue() * Math.sin(angle);

        double directionX = targetPosition.position().x() + offsetX - mc.player.getX();
        double directionZ = targetPosition.position().z() + offsetZ - mc.player.getZ();

        double magnitude = Math.sqrt(directionX * directionX + directionZ * directionZ);

        if (magnitude > 0.01) {
            directionX /= magnitude;
            directionZ /= magnitude;

            double yawRadians = Math.toRadians(-mc.player.getYRot());

            double rotatedX = directionX * Math.cos(yawRadians) - directionZ * Math.sin(yawRadians);
            double rotatedZ = directionX * Math.sin(yawRadians) + directionZ * Math.cos(yawRadians);

            event.setMovementSideways((float) rotatedX);
            event.setMovementForward((float) rotatedZ);
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        LivingEntity target = Astralis.getInstance().getModuleManager().getModule(KillauraModule.class).target;
        if (target == null) return;

        Render3DUtil.drawCircle(event.getMatricies(), target, radius.getProperty().floatValue());

/*        LivingEntity target = Astralis.getInstance().getModuleManager().getModule(KillauraModule.class).target;
        if (target == null) return;

        final MatrixStack stack = event.getMatricies();

        final double x = target.prevX + (target.getX() - target.prevX) * Rendering3DUtil.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        final double y = target.prevY + (target.getY() - target.prevY) * Rendering3DUtil.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY();
        final double z = target.prevZ + (target.getZ() - target.prevZ) * Rendering3DUtil.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();

        stack.push();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        final int segments = 100;
        for (int i = 0; i <= segments; i++) {
            // 6 is wild tho :sob:
            double angle = 6 * Math.PI * i / segments;
            float xOffset = (float) (Math.cos(angle) * radius.getProperty().floatValue());
            float zOffset = (float) (Math.sin(angle) * radius.getProperty().floatValue());

            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) (x + xOffset), (float) y, (float) (z + zOffset))
                    .color(Astralis.getInstance().getFirstColor().getRGB());
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        stack.pop();*/
    }
}
