package cc.astralis.util.client;

import cc.astralis.Astralis;
import cc.astralis.module.SubModule;
import cc.astralis.property.Property;
import cc.astralis.property.properties.*;
import cc.astralis.property.properties.body.BodyProperty;
import cc.astralis.util.render.ColorUtil;
import com.google.gson.JsonObject;
import imgui.type.ImFloat;

public class ConfigUtil {
    public static JsonObject getCurrentConfig() {
        JsonObject moduleNameObject = new JsonObject();

        Astralis.getInstance().getModuleManager().getModules().forEach(module -> {
            JsonObject moduleAttributesObject = new JsonObject();
            JsonObject propertiesObject = new JsonObject();

            moduleAttributesObject.addProperty("toggled", module.isToggled());
            moduleAttributesObject.addProperty("bind", module.getKeyCode());
            moduleAttributesObject.addProperty("hide", module.isHidden());

            module.getPropertyList().forEach(property -> {

                switch (property) {
                    case NumberProperty numberProperty -> propertiesObject.addProperty(numberProperty.getName(), numberProperty.getProperty().floatValue());
                    case ModeProperty modeProperty -> propertiesObject.addProperty(modeProperty.getName(), modeProperty.getProperty());
                    case ClassModeProperty classModeProperty -> propertiesObject.addProperty(classModeProperty.getName(), classModeProperty.getProperty().getFormatedName());
                    case BooleanProperty booleanProperty -> propertiesObject.addProperty(booleanProperty.getName(), booleanProperty.getProperty());
                    case InputProperty inputProperty -> propertiesObject.addProperty(inputProperty.getName(), inputProperty.getProperty());
                    case ColorProperty colorProperty -> propertiesObject.addProperty(colorProperty.getName(), colorProperty.getProperty().getRGB());
                    case BodyProperty bodyProperty -> propertiesObject.addProperty(bodyProperty.getName(), bodyProperty.getProperty().toString());
                    case TextProperty textProperty -> propertiesObject.addProperty(textProperty.getName(), textProperty.getProperty());
                    case FileProperty fileProperty -> propertiesObject.addProperty(fileProperty.getName(), fileProperty.getProperty());

                    default -> System.out.println("Unknown Property Type! " + property.getName());
                }
            });

            if (!propertiesObject.isEmpty()) {
                moduleAttributesObject.add("properties", propertiesObject);
            }

            moduleNameObject.add(module.getName(), moduleAttributesObject);
        });

        return moduleNameObject;
    }

    public static void loadConfig(JsonObject jsonObject) {
        Astralis.getInstance().getModuleManager().getModules().forEach(module -> {
            JsonObject moduleObject = jsonObject.getAsJsonObject(module.getName());

            if (moduleObject != null) {
                JsonObject properties = moduleObject.getAsJsonObject("properties");

                boolean toggleValueConfig = moduleObject.get("toggled").getAsBoolean();
                if (module.isToggled() != toggleValueConfig)
                    module.setToggled(toggleValueConfig);

                boolean hideValueConfig = moduleObject.get("hide").getAsBoolean();
                if (module.isHidden() != hideValueConfig)
                    module.setHidden(hideValueConfig);

                module.setKeyCode(moduleObject.get("bind").getAsInt());

                module.getPropertyList().forEach(property -> {
                    Property<?> gotenProperty = module.getPropertyByName(property.getName());

                    if (gotenProperty != null && properties != null && properties.has(gotenProperty.getName())) {
                        switch (gotenProperty) {
                            case BooleanProperty booleanProperty -> booleanProperty.setProperty(properties.get(booleanProperty.getName()).getAsBoolean());
                            case NumberProperty numberProperty -> numberProperty.setProperty(new ImFloat(properties.get(numberProperty.getName()).getAsFloat()));
                            case ModeProperty modeProperty -> modeProperty.setProperty(properties.get(modeProperty.getName()).getAsString());
                            case ClassModeProperty classModeProperty -> {
                                String savedName = properties.get(classModeProperty.getName()).getAsString();
                                SubModule mode = classModeProperty.getClassModes().get(savedName);
                                if (mode != null) {
                                    classModeProperty.setProperty(mode);
                                    classModeProperty.getProperty().setSelected(true);
                                }
                            }
                            case InputProperty inputProperty -> inputProperty.setProperty(properties.get(inputProperty.getName()).getAsString());
                            case ColorProperty colorProperty -> colorProperty.setProperty(ColorUtil.intToColor(properties.get(colorProperty.getName()).getAsInt()));
                            case FileProperty fileProperty -> fileProperty.setProperty(properties.get(fileProperty.getName()).getAsString());

                            default -> System.out.printf("Test %s%n", gotenProperty.getName());
                        }
                    }
                });
            }
        });
    }
}
