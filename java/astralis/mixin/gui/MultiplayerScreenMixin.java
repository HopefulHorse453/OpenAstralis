package astralis.mixin.gui;

import astralis.mixin.accessor.render.ScreenAccessor;
import cc.astralis.Astralis;
import cc.astralis.protection.Flags;
import cc.astralis.ui.screens.altmanager.AltManagerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;

@Mixin(JoinMultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    protected MultiplayerScreenMixin(Component title) {
        super(title);
    }


    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof JoinMultiplayerScreen screen) {
            Button.Builder builder = Button.builder(
                    Component.literal("Alt Manager"),
                    button -> Minecraft.getInstance().setScreen(new AltManagerScreen())
            ).size(100, 20);

            ((ScreenAccessor) screen).invokeAddDrawableChild(
                    builder.pos(screen.width - 105, screen.height - 35).build()
            );
        }
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void earlyAuthCheck(CallbackInfo ci) {
        if ((Flags.isNotAuthenticated || !Objects.equals(Flags.authStatus, "gud boy") || !Flags.authGuiShown)) {
            Minecraft.getInstance().setScreen(Astralis.getInstance().getAuthScreen());
            ci.cancel();
        }
    }
}
