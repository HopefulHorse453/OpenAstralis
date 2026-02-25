package astralis.mixin.player;

import cc.astralis.Astralis;
import cc.astralis.event.events.impl.game.movementcorrection.JumpCorrectionEvent;
import cc.astralis.interfaces.access.ILivingEntity;
import cc.astralis.component.impl.player.RotationComponent;
import cc.astralis.module.impl.player.NoPushModule;
import cc.astralis.module.impl.visual.AnimationModule;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cc.astralis.interfaces.IAccess.mc;

@Mixin(LivingEntity.class)
@SuppressWarnings("UnreachableCode")
public abstract class MixinLivingEntity extends Entity implements ILivingEntity {

    @Shadow
    public abstract float getViewYRot(float tickDelta);

    @Shadow
    protected double lerpYHeadRot;

    @Shadow
    protected int lerpHeadSteps;

    @Unique
    private float serenium_headPitch;
    @Unique
    private float serenium_prevHeadPitch;
    @Unique
    private float serenium_headYaw;
    @Unique
    private float serenium_prevHeadYaw;
    @Unique
    private boolean serenium_inInventory = false;

    public MixinLivingEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Redirect(method = "tick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"), slice = @Slice(from = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;aiStep()V"), to = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;tickHeadTurn(F)V")))
    public float replaceYaw_tick(LivingEntity instance) {
        final float fakeRotation = RotationComponent.getFakeYaw();

        //noinspection ConstantValue
        if ((Object) this == mc.player && RotationComponent.activate) {
            return fakeRotation;
        }

        return instance.getYRot();
    }


    @Redirect(method = "tickHeadTurn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"))
    public float replaceYaw_turnHead(LivingEntity instance) {
        //noinspection ConstantValue
        if ((Object) this == Minecraft.getInstance().player && RotationComponent.activate) {
            return RotationComponent.fakeYaw;
        }
        return instance.getYRot();
    }

    @Redirect(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F", ordinal = 0))
    private float redirectYRotForMoveFix(LivingEntity entity) {
        if ((Object) this == Minecraft.getInstance().player) {
            JumpCorrectionEvent jumpCorrectionEvent = new JumpCorrectionEvent(entity.getYRot());
            Astralis.getInstance().getEventManager().call(jumpCorrectionEvent);
            return jumpCorrectionEvent.getYaw();
        }

        return entity.getYRot();
    }

    @ModifyConstant(method = "getCurrentSwingDuration", constant = @Constant(intValue = 6))
    private int modifyHandSwingDuration(int constant) {
        final AnimationModule module = Astralis.getInstance().getModuleManager().getModule(AnimationModule.class);
        return module.isToggled() ? (int) ((double) constant / module.speed.getProperty().floatValue()) : constant;
    }

    @Inject(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;lerpHeadSteps:I", ordinal = 2))
    public void respectServerHeadYaw(CallbackInfo ci) {
        serenium_headYaw += (float) Mth.wrapDegrees(this.lerpYHeadRot - (double) this.serenium_headYaw) / (float) this.lerpHeadSteps;
    }

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;yHeadRot:F"))
    public void setHeadRotation(EntityType<?> entityType, Level world, CallbackInfo ci) {
        serenium_headYaw = getYRot();
        serenium_headPitch = getXRot();
    }

    @Inject(method = "recreateFromPacket", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;yHeadRot:F", ordinal = 1))
    public void setHeadRotation(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        serenium_headYaw = packet.getYHeadRot();
        serenium_prevHeadYaw = serenium_headYaw;

        serenium_headPitch = packet.getXRot();
        serenium_prevHeadPitch = serenium_headPitch;
    }

    @Inject(method = "baseTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;yBodyRotO:F"))
    public void updateHeadRotation(CallbackInfo ci) {
        serenium_prevHeadYaw = serenium_headYaw;
        serenium_prevHeadPitch = serenium_headPitch;
    }

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void onPushAwayFrom(Entity other, CallbackInfo ci) {
        final NoPushModule noPush = Astralis.getInstance().getModuleManager().getModule(NoPushModule.class);
        if (noPush.isToggled() && noPush.playerNoPush.getProperty()) {
            ci.cancel();
        }
    }

    @Override
    public float serenium_getHeadPitch() {
        return serenium_headPitch;
    }

    @Override
    public void serenium_setHeadPitch(float headPitch) {
        serenium_headPitch = headPitch;
    }

    @Override
    public float serenium_getPrevHeadPitch() {
        return serenium_prevHeadPitch;
    }

    @Override
    public float serenium_getHeadYaw() {
        return serenium_headYaw;
    }

    @Override
    public void serenium_setHeadYaw(float headYaw) {
        serenium_headYaw = headYaw;
    }

    @Override
    public float serenium_getPrevHeadYaw() {
        return serenium_prevHeadYaw;
    }

    @Override
    public boolean serenium_isInInventory() {
        return serenium_inInventory;
    }
}
