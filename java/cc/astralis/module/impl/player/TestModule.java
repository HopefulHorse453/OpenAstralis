package cc.astralis.module.impl.player;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;

public class TestModule extends Module {
    private boolean gotPacket;

    public TestModule() {
        super(Category.PLAYER);
    }

    @Override
    public void onEnable() {
       /* JsonObject moduleNameObject = new JsonObject();

        Astralis.getInstance().getModuleManager().getModules().forEach(module -> {
            JsonObject moduleAttributesObject = new JsonObject();
            JsonObject propertiesObject = new JsonObject();

            moduleAttributesObject.addProperty("toggled", module.isToggled());
            moduleAttributesObject.addProperty("bind", module.getKeyCode());

            module.getPropertyList().forEach(property -> {

                switch (property) {
                    case NumberProperty numberProperty -> propertiesObject.addProperty(numberProperty.getName(), numberProperty.getProperty().floatValue());
                    case ModeProperty modeProperty -> propertiesObject.addProperty(modeProperty.getName(), modeProperty.getProperty());
                    case ClassModeProperty classModeProperty -> propertiesObject.addProperty(classModeProperty.getName(), classModeProperty.getProperty().getFormatedName());
                    case BooleanProperty booleanProperty -> propertiesObject.addProperty(booleanProperty.getName(), booleanProperty.getProperty());
                    case InputProperty inputProperty -> propertiesObject.addProperty(inputProperty.getName(), inputProperty.getProperty());
                    case ColorProperty colorProperty -> propertiesObject.addProperty(colorProperty.getName(), colorProperty.getProperty().getRGB());
                    case BodyProperty bodyProperty -> propertiesObject.addProperty(bodyProperty.getName(), bodyProperty.getProperty().toString());
                    case InfoProperty infoProperty -> propertiesObject.addProperty(infoProperty.getName(), infoProperty.getProperty());
                    case FileProperty fileProperty -> propertiesObject.addProperty(fileProperty.getName(), fileProperty.getProperty());

                    default -> throw new IllegalStateException("Unknown Property Type! " + property.getClass().getName());
                }
            });

            if (!propertiesObject.isEmpty()) {
                moduleAttributesObject.add("properties", propertiesObject);
            }

            moduleNameObject.add(module.getName(), moduleAttributesObject);
        });

     *//*   ChatUtil.printDebug("sent");
        new AddCloudConfigPacket(new CloudConfig("Test", "fr", 69, System.currentTimeMillis(), moduleNameObject, 1))
                .sendPacket(Astralis.getInstance().getClient());*//*
        new FetchCloudConfigsPacket().sendPacket(Astralis.getInstance().getClient());*/

      /*  FontManager.reinitializeAllFonts();*/
      /*  ProgressBarComponent.createBar("tuff");
        ProgressBarComponent.createBar("tuff2");
        testProgress = 0;*/
        super.onEnable();
    }

 /*   private boolean increasing;
    private float testProgress;
*/
    @EventTarget
    public void onMotion(MotionEvent event) {
     /*       testProgress += 0.01f;

        ProgressBarComponent.updateBar("tuff", testProgress);*/
    }

  /*  @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof EntityAttributesS2CPacket balls && balls.getEntityId() == mc.player.getId()) {
            for (EntityAttributesS2CPacket.Entry entry : balls.getEntries()) {
                if (entry.attribute().equals(EntityAttributes.MOVEMENT_SPEED)) {
                    boolean s = false;
                    for (EntityAttributeModifier attributeModifier : entry.modifiers()) {
                        if (attributeModifier.id().equals(Identifier.ofVanilla("sprinting"))) {
                            s = true;
                            if (mc.player.isSprinting() && ((ClientPlayerEntityAccessor) mc.player).getLastSprinting()) {
                                gotPacket = true;
                                mc.player.setSprinting(false);
                            }
                        }
                    }
                    // ChatUtil.printDebug("movement speed attribute update | sprinting: " + s + " | player is sprinting: " + mc.player.isSprinting() + " | player was sprinting: " + ((ClientPlayerEntityAccessor) mc.player).getLastSprinting() + " | " + mc.player.age);
                }
            }
        }

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            gotPacket = false;
        }
    }

    @EventTarget
    public void onSprintingTickEndEvent(SprintingTickEndEvent event) {
        if (!gotPacket)
            return;

        float y = mc.player.getYaw() % 90F;
        if (y >= 45) y -= 90;
        if (y < -45) y += 90;
        if (Math.abs(y) > 22.5 && mc.player.age % 4 == 0 && Astralis.getInstance().getModuleManager().getModule(ScaffoldWalkModule.class).isToggled()) {
            mc.player.setSprinting(false);
        } else {
            mc.player.setSprinting(true);
            ((EntityAccessor) mc.player).callSetFlag(3, false);
        }
    }

    // nigger.
    @EventTarget
    public void onNigger(NiggerEvent event) {
        if (gotPacket)
            event.setSprint(false);
    }*/
}
