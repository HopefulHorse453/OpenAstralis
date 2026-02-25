package astralis.mixin.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundUseItemOnPacket.class)
public class MixinPlayerInteractBlockC2SPacket {

    @Inject(method = "write", at = @At("HEAD"))
    private void write(FriendlyByteBuf buf , CallbackInfo ci) {
        /* if (Serenium.getInstance().getModuleManager().getModule(DisablerModule.class).isToggled() &&
                Serenium.getInstance().getModuleManager().getModule(DisablerModule.class).mode.is("Verus Fly")) {
            ItemStack.OPTIONAL_PACKET_CODEC.encode((RegistryByteBuf) buf, new ItemStack(Items.WATER_BUCKET));
        } */
    }
}
