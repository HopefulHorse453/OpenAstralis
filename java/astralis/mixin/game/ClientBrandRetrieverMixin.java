package astralis.mixin.game;

import cc.astralis.Astralis;
import cc.astralis.module.impl.exploit.ClientBrandSpoofer;
import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientBrandRetriever.class)
public class ClientBrandRetrieverMixin {
    @Shadow @Final public static String VANILLA_NAME;

    @Inject(method = "getClientModName", at = @At("HEAD"), remap = false, cancellable = true)
    private static void getClientModName(CallbackInfoReturnable<String> cir) {
        if (Astralis.getInstance().getModuleManager() != null &&
                Astralis.getInstance().getModuleManager().getModule(ClientBrandSpoofer.class) != null &&
                Astralis.getInstance().getModuleManager().getModule(ClientBrandSpoofer.class).isToggled()
        ) {
            cir.setReturnValue(VANILLA_NAME);
        }
    }
}