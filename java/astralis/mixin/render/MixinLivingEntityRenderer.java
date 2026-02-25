
package astralis.mixin.render;

import cc.astralis.Astralis;
import cc.astralis.interfaces.access.ILivingEntity;
import cc.astralis.component.impl.player.RotationComponent;
import cc.astralis.interfaces.IAccess;
import cc.astralis.module.impl.visual.NametagsModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> implements IAccess, RenderLayerParent<S, M> {
    protected MixinLivingEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Inject(method = "shouldShowName*", at = @At("HEAD"), cancellable = true)
    private void hideNametags(T entity, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (entity == mc.player) {
            cir.setReturnValue(true);
            return;
        }

        cir.setReturnValue(
                !Astralis.getInstance().getModuleManager().getModule(NametagsModule.class).isToggled() &&
                        Astralis.getInstance().getModuleManager().getModule(NametagsModule.class) != null
        );
    }

    @ModifyExpressionValue(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;rotLerp(FFF)F"))
    private float hookHeadYaw(float original, LivingEntity entity, S state, float tickDelta) {
        if (entity == mc.player && RotationComponent.activate) {
            ILivingEntity livingEntityNoAccessor = (ILivingEntity) entity;
            if (!livingEntityNoAccessor.serenium_isInInventory()) {
                return Mth.rotLerp(tickDelta, livingEntityNoAccessor.serenium_getPrevHeadYaw(), livingEntityNoAccessor.serenium_getHeadYaw());
            }
        }

        return original;
    }

    @ModifyExpressionValue(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getXRot(F)F"))
    private float hookPitch(float original, LivingEntity entity, S state, float tickDelta) {
        if (entity == mc.player && RotationComponent.activate) {
            ILivingEntity livingEntityNoAccessor = (ILivingEntity) entity;
            if (!livingEntityNoAccessor.serenium_isInInventory()) {
                return Mth.rotLerp(tickDelta, livingEntityNoAccessor.serenium_getPrevHeadPitch(), livingEntityNoAccessor.serenium_getHeadPitch());
            }
        }

        return original;
    }

    /*@Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void storeEntity(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        serenium_entity = livingEntity;
    }*/
    /*   @Redirect(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F", ordinal = 1)
    )
    public float serenium$modifyHeadYaw(float delta, float prevYaw, float yaw) {
        if (serenium_entity == MinecraftClient.getInstance().player && RotationComponent.activate) {
            ILivingEntity accessor = (ILivingEntity) serenium_entity;
            if (!accessor.serenium_isInInventory()) {
                return MathHelper.lerpAngleDegrees(delta, accessor.serenium_getPrevHeadYaw(), accessor.serenium_getHeadYaw());
            }
        }

        return MathHelper.lerpAngleDegrees(delta, prevYaw, yaw);
    }

    @Redirect(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F", ordinal = 0))
    public float modifyPitch(float delta, float start, float end) {
        if (serenium_entity == MinecraftClient.getInstance().player && RotationComponent.activate) {
            ILivingEntity accessor = (ILivingEntity) serenium_entity;
            if(!accessor.serenium_isInInventory())
                return MathHelper.lerp(delta, accessor.serenium_getPrevHeadPitch(), accessor.serenium_getHeadPitch());
        }

        return MathHelper.lerp(delta, start, end);
    }

   /* @Redirect(
            method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F", ordinal = 0)
    )
    private float serenium$customPitchLerp(float delta, float start, float end) {
        if (serenium_entity == MinecraftClient.getInstance().player && RotationComponent.activate) {
            ILivingEntity accessor = (ILivingEntity) serenium_entity;
            if (!accessor.serenium_isInInventory()) {
                return MathHelper.lerp(delta, accessor.serenium_getPrevHeadPitch(), accessor.serenium_getHeadPitch());
            }
        }

        return MathHelper.lerp(delta, start, end);
    }*/

/*

    @Redirect(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getLerpedPitch(F)F"))
    public float serenium$lerpedPitch(LivingEntity instance, float original) {
        if(instance != MinecraftClient.getInstance().player) {
            return instance.getLerpedPitch(original);
        }

        ILivingEntity instance1 = (ILivingEntity) instance;

       // return instance1.getPrevHeadPitch() + (instance1.getHeadPitch() - instance1.getPrevHeadPitch()) * original;
        return instance1.serenium_getPrevHeadPitch() + (instance1.serenium_getHeadPitch() - instance1.serenium_getPrevHeadPitch()) * original;
    }
*/

    // todo: ihassedich fix this

  /*  @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void renderHead(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        ChamsModule chamsModule = Astralis.getInstance().getModuleManager().getModule(ChamsModule.class);
        if (chamsModule.isToggled() && serenium_entity.isPlayer() && serenium_entity != mc.player && !AntiBotModule.isBot((PlayerEntity) serenium_entity)) {
            glEnable(GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(1.0f, -1100000.0f);
        }
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    private void renderTail(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        ChamsModule chamsModule = Astralis.getInstance().getModuleManager().getModule(ChamsModule.class);
        if (chamsModule.isToggled() && serenium_entity.isPlayer() && serenium_entity != mc.player && !AntiBotModule.isBot((PlayerEntity) serenium_entity)) {
            glPolygonOffset(1.0f, 1100000.0f);
            glDisable(GL_POLYGON_OFFSET_FILL);
        }
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;"
            )
    )
    private VertexConsumer modifyVertexColor(VertexConsumerProvider provider, RenderLayer layer, T entity, float f, float g, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        ChamsModule chams = Astralis.getInstance().getModuleManager().getModule(ChamsModule.class);

        if (chams.isToggled() && entity instanceof PlayerEntity && entity != MinecraftClient.getInstance().player && !AntiBotModule.isBot((PlayerEntity) entity)) {
            return provider.getBuffer(RenderLayer.getEntityTranslucent(getTexture(entity))).color(255, 0, 0, 255);
        }

        return provider.getBuffer(layer);
    }*/

}

