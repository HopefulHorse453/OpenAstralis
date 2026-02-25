package cc.astralis.module.impl.visual;

import cc.astralis.module.Category;
import cc.astralis.module.Module;

public class FullbrightModule extends Module {
   // private final ModeProperty mode = new ModeProperty("Mode", "Bright", "Bright", "Color");
   // private final ColorProperty color = new ColorProperty("Color", Color.BLUE).setVisible(() -> mode.is("Color"));

    public FullbrightModule() {
        super(Category.VISUAL);
      //  registerProperties(mode, color);
    }
/*
    @EventTarget
    public void onGamma(GammaEvent e) {
        int red = (e.getColor() >> 16) & 255;
        int green = (e.getColor() >> 8) & 255;
        int blue = e.getColor() & 255;

        if (mode.is("Bright") || (e.getX() == 15 && e.getY() == 15)) { //Dont change it or break game mc is ass
            red = 255;
            green = 255;
            blue = 255;
        } else if (mode.is("Color")) {
            Color c = color.getProperty();

            red = (int) ((red * (c.getRed() / 255.0)));
            green = (int) ((green * (c.getGreen() / 255.0)));
            blue = (int) ((blue * (c.getBlue() / 255.0)));
        }

        e.setColor((red & 255) | ((green & 255) << 8) | ((blue & 255) << 16) | 0xFF000000);
    }

 */
}
