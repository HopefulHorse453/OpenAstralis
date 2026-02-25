package astralis.mixin.render;

import cc.astralis.util.render.ShaderUtil;
import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.renderer.ShaderManager$CompilationCache")
public class MixinShaderLoaderCache {

    @Inject(method = "getShaderSource", at = @At("HEAD"), cancellable = true)
    private void overrideShaderSource(ResourceLocation id, ShaderType type, CallbackInfoReturnable<String> cir) {
//        ShaderProgram glowShader = null;
//        System.out.println(id.toString());
        if (id.toString().equals("minecraft:post/entity_outline_box_blur")/* && type == ShaderType.FRAGMENT*/) {
            cir.setReturnValue(ShaderUtil.getCustomShader());
        }
    }
}
