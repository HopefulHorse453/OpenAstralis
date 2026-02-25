package astralis.mixin.world;

import cc.astralis.util.player.StorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Kawase
 * @since 16.08.2025
 */
@Mixin(LevelChunk.class)
public class MixinWorldChunk {
    @Inject(method = "setBlockEntity", at = @At("TAIL"))
    private void astr$beAdded(BlockEntity be, CallbackInfo ci) {
        if (!StorageUtil.isCollectData()) return;
        if (be != null) StorageUtil.onBlockEntityAdded((LevelChunk)(Object)this, be);
    }

    @Inject(method = "removeBlockEntity", at = @At("TAIL"))
    private void astr$beRemoved(BlockPos pos, CallbackInfo ci) {
        if (!StorageUtil.isCollectData()) return;
        StorageUtil.onBlockEntityRemoved((LevelChunk)(Object)this, pos);
    }
}
