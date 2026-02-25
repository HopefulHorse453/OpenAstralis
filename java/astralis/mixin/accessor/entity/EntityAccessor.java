package astralis.mixin.accessor.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker
    Vec3 callCollide(Vec3 movement);

    @Invoker
    void callSetSharedFlag(int index, boolean value);
}
