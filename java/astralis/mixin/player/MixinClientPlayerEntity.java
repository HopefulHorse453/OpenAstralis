package astralis.mixin.player;

import astralis.mixin.accessor.player.ClientPlayerEntityAccessor;
import cc.astralis.event.events.impl.game.*;
import cc.astralis.event.events.impl.input.SprintingTickEndEvent;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import cc.astralis.Astralis;
import cc.astralis.component.impl.player.RotationComponent;
import cc.astralis.interfaces.IAccess;
import cc.astralis.util.Data;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LocalPlayer.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayer implements IAccess {

    @Shadow
    @Final
    public ClientPacketListener connection;

    @Shadow
    private boolean lastOnGround;

    @Shadow
    protected abstract boolean isControlledCamera();

    @Shadow
    private int positionReminder;

    @Shadow
    private boolean autoJumpEnabled;

    @Shadow
    @Final
    protected Minecraft minecraft;

    @Shadow
    protected abstract void sendIsSprintingIfNeeded();

    @Unique
    private Item lastItem;
    @Unique
    private boolean didSaveItem = false;

    @Shadow public abstract void move(MoverType movementType, Vec3 movement);

    public MixinClientPlayerEntity(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo info) {
        UpdateEvent updateEvent = new UpdateEvent();

        Astralis.getInstance().getEventManager().call(updateEvent);
        if (updateEvent.isCancelled())
            info.cancel();
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), name = "arg2", ordinal = 0, index = 2, argsOnly = true)
    private Vec3 hookMove(Vec3 movement, MoverType type) {
        MoveEvent moveEvent = new MoveEvent(type, movement.x, movement.y, movement.z);
        Astralis.getInstance().getEventManager().call(moveEvent);

        return moveEvent.isCancelled() ? Vec3.ZERO : new Vec3(moveEvent.getX(), moveEvent.getY(), moveEvent.getZ());
    }

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean onItemSlowdown(LocalPlayer player) {
        SlowDownEvent slowDownEvent = new SlowDownEvent();
        Astralis.getInstance().getEventManager().call(slowDownEvent);
        return !slowDownEvent.isCancelled() && player.isUsingItem();
    }

    @Redirect(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    public boolean canStartSprinting(LocalPlayer clientPlayerEntity) {
        SlowDownEvent slowDownEvent = new SlowDownEvent();
        Astralis.getInstance().getEventManager().call(slowDownEvent);
        return slowDownEvent.isCancelled() ?
                false : clientPlayerEntity.isUsingItem();
    }

    @ModifyConstant(method = "modifyInput", constant = @Constant(floatValue = 0.2F))
    private float modifyItemUseSlowdown(float original) {
        SlowDownEvent slowDownEvent = new SlowDownEvent();
        Astralis.getInstance().getEventManager().call(slowDownEvent);
        return slowDownEvent.isCancelled()
                ? slowDownEvent.getSlowDown()
                : original;
    }

    @Inject(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;mayfly:Z", opcode = Opcodes.GETFIELD, ordinal = 0))
    private void hookSprintingTickEndEvent(CallbackInfo ci) {
        Astralis.getInstance().getEventManager().call(new SprintingTickEndEvent());
    }

    @Redirect(method = "sendIsSprintingIfNeeded", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSprinting()Z"))
    private boolean hookSprintingSendEvent(LocalPlayer player) {
        NiggerEvent event = new NiggerEvent(player.isSprinting());
        Astralis.getInstance().getEventManager().call(event);

        return event.isCancelled() ? ((ClientPlayerEntityAccessor) mc.player).getWasSprinting() : event.isSprint();
    }

    private MotionEvent positionSendEvent;

    @Inject(method = "sendPosition", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("all")
    private void hookPositionSendEvent(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        boolean b = RotationComponent.activate;
        Data.offGroundMotionTicks = mc.player.onGround() ? 0 : ++Data.offGroundMotionTicks;
        Data.onGroundMotionTicks = !mc.player.onGround() ? 0 : ++Data.onGroundMotionTicks;
        positionSendEvent = new MotionEvent(player.getX(), player.getY(), player.getZ(), b ? RotationComponent.getYaw() : player.getYRot(), b ? RotationComponent.getPitch() : player.getXRot(), player.onGround());
        Astralis.getInstance().getEventManager().call(positionSendEvent);
        if (positionSendEvent.isCancelled()) ci.cancel();
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getX()D"))
    private double redirectXPositionSendEvent(LocalPlayer player) {
        return positionSendEvent.getX();
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getY()D"))
    private double redirectYPositionSendEvent(LocalPlayer player) {
        return positionSendEvent.getY();
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getZ()D"))
    private double redirectZPositionSendEvent(LocalPlayer player) {
        return positionSendEvent.getZ();
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getYRot()F"))
    private float redirectYRotPositionSendEvent(LocalPlayer player) {
        return positionSendEvent.getYaw();
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot()F"))
    private float redirectXRotPositionSendEvent(LocalPlayer player) {
        return positionSendEvent.getPitch();
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;onGround()Z"))
    private boolean redirectOnGroundPositionSendEvent(LocalPlayer player) {
        return positionSendEvent.isOnGround();
    }

    @Inject(method = "sendPosition", at = @At("TAIL"))
    private void hookPositionSendEndEvent(CallbackInfo ci) {
        Astralis.getInstance().getEventManager().call(new PostMotionEvent());
    }
}
