package astralis.mixin.accessor.network;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundSetEntityMotionPacket.class)
public interface EntityVelocityUpdateS2CPacketAccessor {
    @Accessor
    Vec3 getMovement();

    @Mutable
    @Accessor
    void setMovement(Vec3 velocity);

    @Accessor
    int getId();

    @Mutable
    @Accessor
    void setId(int entityId);
}
