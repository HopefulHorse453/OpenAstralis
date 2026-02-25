package astralis.mixin.render;

import cc.astralis.Astralis;
import cc.astralis.module.impl.visual.AmbienceModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.awt.Color;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
public abstract class MixinScreen {

    @ModifyArg(
            method = "renderTransparentBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;fillGradient(IIIIII)V"
            ),
            index = 5
    )
    private int modifyGradientEndColor(int originalColor) {
        AmbienceModule ambience = Astralis.getInstance().getModuleManager().getModule(AmbienceModule.class);
        if (ambience != null && ambience.isToggled() && ambience.customGradient.getProperty()) {
            return applyAlpha(
                    ambience.gradientBottom.getProperty(),
                    ambience.gradientBottomAlpha.getProperty().floatValue()
            );
        }
        return originalColor;
    }

    @ModifyArg(
            method = "renderTransparentBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;fillGradient(IIIIII)V"
            ),
            index = 4
    )
    private int modifyGradientStartColor(int originalColor) {
        AmbienceModule ambience = Astralis.getInstance().getModuleManager().getModule(AmbienceModule.class);
        if (ambience != null && ambience.isToggled() && ambience.customGradient.getProperty()) {
            return applyAlpha(
                    ambience.gradientTop.getProperty(),
                    ambience.gradientTopAlpha.getProperty().floatValue()
            );
        }
        return originalColor;
    }

    @Unique
    private int applyAlpha(Color color, float alpha) {
        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                (int)(alpha * 255)
        ).getRGB();
    }
}