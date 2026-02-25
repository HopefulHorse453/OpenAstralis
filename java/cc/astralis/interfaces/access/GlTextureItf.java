package cc.astralis.interfaces.access;

import com.mojang.blaze3d.pipeline.RenderTarget;
import io.github.humbleui.skija.DirectContext;
import io.github.humbleui.skija.Image;

public interface GlTextureItf {
    Image astralis$getOrCreateSkikoImage(DirectContext context, RenderTarget framebuffer, boolean hasAlpha);
}
