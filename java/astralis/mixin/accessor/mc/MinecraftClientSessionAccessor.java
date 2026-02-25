package astralis.mixin.accessor.mc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftClientSessionAccessor {
    @Accessor
    User getUser();

    @Mutable
    @Accessor
    void setUser(User session);

    @Accessor("rightClickDelay")
    int getItemUseCooldown();

    @Accessor("rightClickDelay")
    void setItemUseCooldown(int cooldown);

}
