package astralis.mixin.player;

import cc.astralis.Astralis;
import cc.astralis.component.impl.player.RotationComponent;
import cc.astralis.interfaces.access.ILivingEntity;
import cc.astralis.event.events.impl.game.LoseSprintEvent;
import cc.astralis.module.impl.combat.ReachModule;
import cc.astralis.module.impl.movement.SafeWalkModule;
import cc.astralis.module.impl.movement.ScaffoldRecodeModule;
import cc.astralis.util.player.PlayerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static cc.astralis.interfaces.IAccess.mc;

@Mixin(Player.class)
public abstract class MixinPlayerEntity extends LivingEntity {
    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;yHeadRot:F"))
    public void updateHeadRotation(CallbackInfo ci) {
        float yaw = getYRot();
        float pitch = getXRot();
        //noinspection ConstantValue
        if ((Object) this == Minecraft.getInstance().player && RotationComponent.activate) {
            yaw = RotationComponent.fakeYaw;
            pitch = RotationComponent.fakePitch;
        }

        ((ILivingEntity) this).serenium_setHeadYaw(yaw);
        ((ILivingEntity) this).serenium_setHeadPitch(pitch);
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"), cancellable = true)
    private void onSetVelocity(Entity target, CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().player) {
            LoseSprintEvent sprintEvent = new LoseSprintEvent();

            Astralis.getInstance().getEventManager().call(sprintEvent);

            if (sprintEvent.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setSprinting(Z)V"), cancellable = true)
    private void onSetSprinting(Entity target, CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().player) {
            Player player = (Player) (Object) this;
            LoseSprintEvent sprintEvent = new LoseSprintEvent();

            Astralis.getInstance().getEventManager().call(sprintEvent);

            if (sprintEvent.isCancelled()) {
                player.setSprinting(true);
                ci.cancel();
            }
        }
    }

    @Inject(method = "isStayingOnGroundSurface", at = @At("HEAD"), cancellable = true)
    public void ClipatEdgeSafewalk(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this == mc.player) {
            SafeWalkModule safeWalkModule = Astralis.getInstance().getModuleManager().getModule(SafeWalkModule.class);
            ScaffoldRecodeModule scaffoldRecodeModule = Astralis.getInstance().getModuleManager().getModule(ScaffoldRecodeModule.class);
            if (safeWalkModule.isToggled()) {
                if (safeWalkModule.blocksOnly.getProperty() && !PlayerUtil.isHoldingBlocks()) {
                    return;
                }
                if (safeWalkModule.backwards.getProperty() && !mc.options.keyDown.isDown()) {
                    return;
                }
                if (safeWalkModule.pitchCheck.getProperty() && mc.player.getXRot() <= 60) {
                    return;
                }
                cir.setReturnValue(true);
            }
           /* if (scaffoldRecodeModule.isToggled() && scaffoldRecodeModule.safeWalk.getProperty()) {
                cir.setReturnValue(true);
            }*/
        }
    }

    @Inject(method = "blockInteractionRange", at = @At("HEAD"), cancellable = true)
    public void getBlockInteractionRangeHook(CallbackInfoReturnable<Double> cir) {
        ReachModule reach = Astralis.getInstance().getModuleManager().getModule(ReachModule.class);
        if (reach == null)
            return;

        if (reach.isToggled() && reach.block_reach.getProperty()) {
            cir.setReturnValue((double) reach.blockReach.getProperty().floatValue());
        }
    }

    @Inject(method = "entityInteractionRange", at = @At("HEAD"), cancellable = true)
    public void getEntityInteractionRangeHook(CallbackInfoReturnable<Double> cir) {
        ReachModule reach = Astralis.getInstance().getModuleManager().getModule(ReachModule.class);
        if (reach == null)
            return;

        if (reach.isToggled()) {
            cir.setReturnValue((double) reach.reach.getProperty().floatValue());
        }
    }
}
