package astralis.mixin.input;

import cc.astralis.Astralis;
import cc.astralis.event.events.impl.input.InputTickEvent;
import cc.astralis.event.events.impl.input.ModifyMovementEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Options;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = KeyboardInput.class, priority = 999)
public class MixinKeyboardInput extends ClientInput {
    @Shadow
    @Final
    private Options options;

    @Shadow
    private static float calculateImpulse(boolean bl, boolean bl2) {
        return 0.0F;
    }

    /**
     * @author nigga balls
     * @reason cuz he is gay
     */
    @WrapOperation(method = "tick", at = @At(value = "NEW", target = "(ZZZZZZZ)Lnet/minecraft/world/entity/player/Input;"))
    private Input tick(boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, boolean bl7, Operation<Input> original) {
        final InputTickEvent event = new InputTickEvent(bl, bl2, bl3, bl4, bl5, bl6, bl7);
        Astralis.getInstance().getEventManager().call(event);
        return original.call(event.up, event.down, event.left, event.right, event.jump, event.shift, event.sprint);
    }
}
