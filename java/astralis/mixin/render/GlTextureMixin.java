package astralis.mixin.render;

import cc.astralis.interfaces.access.GlTextureItf;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import io.github.humbleui.skija.ColorType;
import io.github.humbleui.skija.DirectContext;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.SurfaceOrigin;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlTexture.class)
public class GlTextureMixin implements GlTextureItf {

    @Shadow
    @Final
    protected int id;
    @Shadow
    @Final
    private Int2IntMap fboCache;
    @Unique
    private Image image;

    @Override
    public Image astralis$getOrCreateSkikoImage(DirectContext context, RenderTarget framebuffer, boolean hasAlpha) {
        if (image != null) {
            return image;
        }

        final GlTexture thiz = (GlTexture) (Object) this;

        final int width = (thiz.getWidth(thiz.getMipLevels()));
        final int height = (thiz.getHeight(thiz.getMipLevels()));

        image = Image.adoptGLTextureFrom(
                context,
                id,
                GL33C.GL_TEXTURE_2D,
                width, height,
                GL11.GL_RGBA8,
                SurfaceOrigin.BOTTOM_LEFT,
                hasAlpha ? ColorType.RGBA_8888 : ColorType.RGB_888X
        );
        return image;
    }

    @Inject(method = "destroyImmediately", at = @At("HEAD"), cancellable = true)
    private void astralis$free(CallbackInfo ci) {
        if (image != null) {
            image.close();
            image = null;
            IntIterator var1 = this.fboCache.values().iterator();

            while (var1.hasNext()) {
                int i = var1.next();
                GlStateManager._glDeleteFramebuffers(i);
            }

            ci.cancel();
        }
    }
}