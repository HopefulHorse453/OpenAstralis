package astralis.mixin.input;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientInput.class)
public abstract class MixinInput {
    @Shadow public Input keyPresses;
    @Shadow protected Vec2 moveVector;

    public boolean isPressingRight() {
        return this.keyPresses.right();
    }

    public boolean isPressingLeft() {
        return this.keyPresses.left();
    }

    public boolean isPressingBack() {
        return this.keyPresses.backward();
    }

    public boolean isPressingForward() {
        return this.keyPresses.forward();
    }

    public boolean isJumping() {
        return this.keyPresses.jump();
    }

    public boolean isSneaking() {
        return this.keyPresses.shift();
    }

    public boolean isSprinting() { return keyPresses.sprint(); }
}
