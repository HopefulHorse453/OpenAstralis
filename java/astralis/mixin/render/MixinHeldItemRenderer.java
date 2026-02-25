package astralis.mixin.render;

import cc.astralis.Astralis;
import cc.astralis.interfaces.IAccess;
import astralis.mixin.accessor.render.HeldItemRendererAccessor;
import cc.astralis.module.impl.combat.KillauraModule;
import cc.astralis.module.impl.movement.ScaffoldRecodeModule;
import cc.astralis.module.impl.visual.AnimationModule;
import cc.astralis.module.impl.visual.CameraModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinHeldItemRenderer implements IAccess {

    @Shadow
    @Final
    private static float ITEM_POS_Y;
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onUpdateHeldItems(CallbackInfo ci) {
        ScaffoldRecodeModule scaffold = Astralis.getInstance().getModuleManager().getModule(ScaffoldRecodeModule.class);

        if (scaffold.spoofedSlot != -1 && scaffold.spoofItem.getProperty()) {
            if (mc.player != null) {
                ItemStack spoofedMain = mc.player.getInventory().getItem(scaffold.spoofedSlot);
                ((HeldItemRendererAccessor) this).setMainHand(spoofedMain);
            }

            ci.cancel();
        }
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER))
    private void hookRenderFirstPersonItem(AbstractClientPlayer player, float tickProgress, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector orderedRenderCommandQueue, int light, CallbackInfo ci) {
        if (InteractionHand.MAIN_HAND == hand && Astralis.getInstance().getModuleManager().getModule(CameraModule.class).isToggled()) {
            matrices.translate(
                    CameraModule.itemX.getProperty().floatValue() / 10,
                    CameraModule.itemY.getProperty().floatValue() / 10,
                    CameraModule.itemZ.getProperty().floatValue() / 10
            );

            matrices.mulPose(Axis.XP.rotationDegrees(CameraModule.itemRotX.getProperty().floatValue()));
            matrices.mulPose(Axis.YP.rotationDegrees(CameraModule.itemRotY.getProperty().floatValue()));
            matrices.mulPose(Axis.ZP.rotationDegrees(CameraModule.itemRotZ.getProperty().floatValue()));
        }
    }

    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V",
                    ordinal = 2 // This is the one inside the BLOCK case
            ),
            cancellable = true
    )
    private void onBlockTransform(AbstractClientPlayer player, float tickProgress, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector orderedRenderCommandQueue, int light, CallbackInfo ci) {
        if (item.getUseAnimation() == ItemUseAnimation.BLOCK) {
            ci.cancel();
        }
    }

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void hideShield(AbstractClientPlayer player, float tickProgress, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector orderedRenderCommandQueue, int light, CallbackInfo ci) {
        final var module = Astralis.getInstance().getModuleManager().getModule(AnimationModule.class);

        if (
                !(item.getItem() instanceof ShieldItem) ||
                        !module.isToggled()
        ) {
            return;
        }

        ci.cancel();
    }

    @ModifyArg(method = "applyItemArmTransform", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"), index = 1)
    private float injectDisableEquipOffset(float y) {
        return AnimationModule.disableItemSwingOffset.getProperty() && Astralis.getInstance().getModuleManager().getModule(AnimationModule.class).isToggled()
                ? ITEM_POS_Y : y;
    }

    // Minecraft decided to apply item transformations to swords for some wtv reason, so unless they revert that we will keep this :pray:.
    @ModifyExpressionValue(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
    private Item preventConflictingCode(Item item) {
        if (mc.player.getInventory().getSelectedItem().is(ItemTags.SWORDS)) {
            return Items.SHIELD; // return true and therefore no item transformation.
        }

        return item;
    }

    @Redirect(method = "renderArmWithItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/ItemUseAnimation;",
            ordinal = 0
    ))
    private ItemUseAnimation hookUseAction(ItemStack instance) {
        var item = instance.getItem();
        if (mc.player.getInventory().getSelectedItem().is(ItemTags.SWORDS) && (Astralis.getInstance().getModuleManager().getModule(KillauraModule.class).isToggled() &&
                Astralis.getInstance().getModuleManager().getModule(KillauraModule.class).shouldRenderFakeAnim() ||
                Minecraft.getInstance().options.keyUse.isDown()) && Astralis.getInstance().getModuleManager().getModule(AnimationModule.class).isToggled()) {
            return ItemUseAnimation.BLOCK;
        }

        return instance.getUseAnimation();
    }

    @Redirect(method = "renderArmWithItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/AbstractClientPlayer;isUsingItem()Z",
            ordinal = 1
    ))
    private boolean hookIsUseItem(AbstractClientPlayer instance) {
        var item = instance.getMainHandItem().getItem();

        if (mc.player.getInventory().getSelectedItem().is(ItemTags.SWORDS) && (Astralis.getInstance().getModuleManager().getModule(KillauraModule.class).isToggled() &&
                Astralis.getInstance().getModuleManager().getModule(KillauraModule.class).shouldRenderFakeAnim() || Minecraft.getInstance().options.keyUse.isDown()) && Astralis.getInstance().getModuleManager().getModule(AnimationModule.class).isToggled()) {
            return true;
        }

        return instance.isUsingItem();
    }

    @Redirect(method = "renderArmWithItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUsedItemHand()Lnet/minecraft/world/InteractionHand;",
            ordinal = 1
    ))
    private InteractionHand hookActiveHand(AbstractClientPlayer instance) {
        var item = instance.getMainHandItem().getItem();

        if (mc.player.getInventory().getSelectedItem().is(ItemTags.SWORDS) && (Astralis.getInstance().getModuleManager().getModule(KillauraModule.class).isToggled() &&
                Astralis.getInstance().getModuleManager().getModule(KillauraModule.class).shouldRenderFakeAnim() || Minecraft.getInstance().options.keyUse.isDown()) && Astralis.getInstance().getModuleManager().getModule(AnimationModule.class).isToggled()) {
            return InteractionHand.MAIN_HAND;
        }

        return instance.getUsedItemHand();
    }

    @Redirect(method = "renderArmWithItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUseItemRemainingTicks()I",
            ordinal = 2
    ))
    private int hookItemUseItem(AbstractClientPlayer instance) {
        var item = instance.getMainHandItem().getItem();

        if (mc.player.getInventory().getSelectedItem().is(ItemTags.SWORDS) && (Astralis.getInstance().getModuleManager().getModule(KillauraModule.class).isToggled() &&
                Astralis.getInstance().getModuleManager().getModule(KillauraModule.class).shouldRenderFakeAnim() || Minecraft.getInstance().options.keyUse.isDown()) && Astralis.getInstance().getModuleManager().getModule(AnimationModule.class).isToggled()) {
            return 7200;
        }

        return instance.getUseItemRemainingTicks();
    }

    @ModifyArg(method = "renderArmWithItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V",
            ordinal = 4
    ), index = 2)
    private float applyEquipOffset(float equipProgress) {
        if (Astralis.getInstance().getModuleManager().getModule(AnimationModule.class).isToggled()) {
            return 0.0F;
        }
        return equipProgress;
    }

    @Inject(method = "renderArmWithItem",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/ItemUseAnimation;")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V", ordinal = 2, shift = At.Shift.AFTER))
    private void transformLegacyBlockAnimations(AbstractClientPlayer player, float tickProgress, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector orderedRenderCommandQueue, int light, CallbackInfo ci) {
        if (Astralis.getInstance().getModuleManager().getModule(AnimationModule.class).isToggled() && mc.player.getInventory().getSelectedItem().is(ItemTags.SWORDS)) {
            final HumanoidArm arm = (hand == InteractionHand.MAIN_HAND) ? player.getMainArm() : player.getMainArm().getOpposite();
            AnimationModule.runBlockHitAnimation(matrices, swingProgress, arm);
        }
    }
}
