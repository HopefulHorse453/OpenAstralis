package astralis.mixin.input;

import cc.astralis.Astralis;
import cc.astralis.event.events.impl.input.KeyboardEvent;
import cc.astralis.interfaces.IAccess;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class MixinKeyBoard implements IAccess {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void onPress(long window, int action, KeyEvent input, CallbackInfo ci) {
        if(action == GLFW.GLFW_PRESS && mc.screen == null) {
            Astralis.getInstance().getEventManager().call(new KeyboardEvent(input.key()));
        }
    }
}
