package astralis.mixin.render;


import cc.astralis.Astralis;
import cc.astralis.module.impl.visual.AmbienceModule;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Options.class)
public class MixinGameOptions {
    @Shadow
    @Final
    private OptionInstance<Integer> menuBackgroundBlurriness;

    @Redirect(method = "menuBackgroundBlurriness", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;menuBackgroundBlurriness:Lnet/minecraft/client/OptionInstance;", opcode = 180)) // GETFIELD
    private OptionInstance<Integer> blur$overrideBlurriness(Options options) {
        AmbienceModule ambience = Astralis.getInstance().getModuleManager().getModule(AmbienceModule.class);
        if (ambience != null && ambience.isToggled() && ambience.customBlur.getProperty()) {
            return new OptionInstance<>(
                    "options.menu_background_blurriness",
                    OptionInstance.cachedConstantTooltip(Component.translatable("options.menu_background_blurriness.tooltip")),
                    (optionText, value) -> Component.translatable("options.generic_value", optionText, value),
                    new OptionInstance.IntRange(0, 20),
                    ambience.blurStrength.getProperty().intValue(),
                    value -> {} // No-op callback
            );
        }

        return this.menuBackgroundBlurriness;
    }
}
