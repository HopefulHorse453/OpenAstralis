package astralis.mixin.accessor.entity;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor
    int getNoJumpDelay();

    @Accessor
    void setNoJumpDelay(int jumpingCooldown);

    @Invoker
    float callGetJumpPower();
}