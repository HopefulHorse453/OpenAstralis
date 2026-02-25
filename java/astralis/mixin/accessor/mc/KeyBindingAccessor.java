package astralis.mixin.accessor.mc;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface KeyBindingAccessor {
    @Accessor("clickCount")
    int getTimesPressed();

    @Accessor("clickCount")
    void setTimesPressed(int timesPressed);
}