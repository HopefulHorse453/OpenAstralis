package astralis.mixin.input;

import cc.astralis.Astralis;
import cc.astralis.event.events.impl.game.EntityInteractEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.impl.combat.VelocityModule;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MixinClientPlayerInteractionManager {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void hookPreAttack(Player player, Entity target, CallbackInfo ci) {
        EntityInteractEvent pre = new EntityInteractEvent(target, EventModes.PRE);
        Astralis.getInstance().getEventManager().call(pre);

        if (pre.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "attack", at = @At("TAIL"))
    private void hookPostAttack(Player player, Entity target, CallbackInfo ci) {
        Astralis.getInstance().getEventManager().call(new EntityInteractEvent(target, EventModes.POST));
    }

    @Shadow
    private int destroyDelay;

    /*@Redirect(method = "updateBlockBreakingProgress",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", opcode = Opcodes.GETFIELD, ordinal = 0))
    public int updateBlockBreakingProgress(ClientPlayerInteractionManager clientPlayerInteractionManager) {
        int cooldown = this.blockBreakingCooldown;
        return Astralis.getInstance().getModuleManager().getModule(NoMinningCooldownModule.class).isToggled() ? 0 : cooldown;
    }*/
}
