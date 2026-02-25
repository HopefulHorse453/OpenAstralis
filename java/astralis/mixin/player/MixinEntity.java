package astralis.mixin.player;

import cc.astralis.Astralis;
import cc.astralis.component.impl.player.RotationComponent;
import cc.astralis.event.events.impl.game.StrafeEvent;
import cc.astralis.event.events.impl.game.movementcorrection.YawCorrectionEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.interfaces.access.ILivingEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
@SuppressWarnings("UnreachableCode")
public abstract class MixinEntity {
    @Shadow
    public abstract Vec3 calculateViewVector(float pitch, float yaw);

    @Shadow
    public abstract float getXRot();

    @Shadow
    public abstract float getYRot();

    @Inject(method = "getViewVector", at = @At("HEAD"), cancellable = true)
    public void injectFakeRotation(float tickDelta, CallbackInfoReturnable<Vec3> cir) {
        // noinspection ConstantValue
        if ((Object) this == Minecraft.getInstance().player && RotationComponent.activate) {
            cir.setReturnValue(this.calculateViewVector(RotationComponent.fakePitch, RotationComponent.fakeYaw));
        }
    }

  /*  @Inject(
            method = "readCustomData(Lnet/minecraft/storage/ReadView;)V",
            at = @At("RETURN")
    )
    private void readCustomData(ReadView view, CallbackInfo ci) {
        if ((Object) this instanceof LivingEntity) {

        }
    }*/

    @Inject(method = "load", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;setYBodyRot(F)V"))
    public void hookUpdateHeadRotation(ValueInput view, CallbackInfo ci) {
        // noinspection ConstantValue
        if ((Object) this instanceof LivingEntity && RotationComponent.activate) {
            ILivingEntity accessor = (ILivingEntity) this;
            accessor.serenium_setHeadYaw(getYRot());
            accessor.serenium_setHeadPitch(getXRot());
        }
    }

    @Inject(method = "moveRelative", at = @At("HEAD"), cancellable = true)
    private void weeeooooUpdateVelo(float speed, Vec3 movementInput, CallbackInfo ci) {
        if ((Object)this instanceof Player) {
            StrafeEvent event = new StrafeEvent(movementInput, EventModes.PRE);
            Astralis.getInstance().getEventManager().call(event);

            if (event.isCancelled()) {
                ci.cancel();
                return;
            }
        }
    }

    @Inject(method = "moveRelative", at = @At("TAIL"), cancellable = true)
    private void updateVelo(float speed, Vec3 movementInput, CallbackInfo ci) {
        if ((Object)this instanceof Player) {
            StrafeEvent event = new StrafeEvent(movementInput, EventModes.POST);
            Astralis.getInstance().getEventManager().call(event);

            if (event.isCancelled()) {
                ci.cancel();
                return;
            }
        }
    }

    @Shadow
    public static Vec3 getInputVector(Vec3 movementInput, float speed, float yaw) {
        return null;
    }

    @Redirect(method = "collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;maxUpStep()F"))
    private float hookStepHeight(Entity instance) {
        return instance.maxUpStep();
    }

    @Redirect(method = "moveRelative", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getInputVector(Lnet/minecraft/world/phys/Vec3;FF)Lnet/minecraft/world/phys/Vec3;"))
    public Vec3 hookVelocity(Vec3 movementInput, float speed, float yaw) {
        if ((Object) this == Minecraft.getInstance().player) {
            YawCorrectionEvent yawCorrectionEvent = new YawCorrectionEvent(yaw);
            Astralis.getInstance().getEventManager().call(yawCorrectionEvent);
            yaw = yawCorrectionEvent.getYaw();
        }

        return getInputVector(movementInput, speed, yaw);
    }
}
