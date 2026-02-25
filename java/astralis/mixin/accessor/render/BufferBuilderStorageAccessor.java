package astralis.mixin.accessor.render;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderBuffers.class)
public interface BufferBuilderStorageAccessor {
    @Accessor("bufferSource")
    MultiBufferSource.BufferSource getEntityVertexConsumers();
}
