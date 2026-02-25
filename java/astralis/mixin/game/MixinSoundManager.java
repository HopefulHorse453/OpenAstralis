package astralis.mixin.game;

import cc.astralis.Astralis;
import cc.astralis.module.impl.visual.HitSoundModule;
import cc.astralis.module.impl.visual.HudModule;
import cc.astralis.module.impl.world.LegacySoundsModule;
import cc.astralis.util.render.ChatUtil;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(SoundManager.class)
public class MixinSoundManager {
    @Unique
    private static final List<ResourceLocation> ignoredSounds = List.of(
            SoundEvents.PLAYER_ATTACK_KNOCKBACK.location(),
            SoundEvents.PLAYER_ATTACK_SWEEP.location(),
            SoundEvents.PLAYER_ATTACK_CRIT.location(),
            SoundEvents.PLAYER_ATTACK_STRONG.location(),
            SoundEvents.PLAYER_ATTACK_WEAK.location(),
            SoundEvents.PLAYER_ATTACK_NODAMAGE.location()
    );

    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;",
            at = @At("HEAD"), cancellable = true)
    private void onPlay(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        LegacySoundsModule legacySoundsModule = Astralis.getInstance().getModuleManager().getModule(LegacySoundsModule.class);
        if (Astralis.getInstance().getModuleManager().getModule(HudModule.class) == null)
            return;

        HitSoundModule hitSoundModule = Astralis.getInstance().getModuleManager().getModule(HitSoundModule.class);

       // ChatUtil.printDebug(sound.getId().getPath());
        if (ignoredSounds.contains(sound.getLocation()) && legacySoundsModule != null && legacySoundsModule.isToggled()) {
            cir.cancel();
        }

        if (hitSoundModule.isToggled() && sound.getLocation().equals(SoundEvents.PLAYER_HURT.location()))
            cir.cancel();

        /*if (hitSoundModule != null && hitSoundModule.isToggled()) {
            SoundEvent soundEvent = Registries.SOUND_EVENT.get(hitSoundModule.getSoundIdentifier());

            if (soundEvent != null) {
                SoundInstance soundInstance = PositionedSoundInstance.master(soundEvent*//*, SoundCategory.PLAYERS*//*, 1.0f, 1.0f);
                MinecraftClient.getInstance().getSoundManager().play(soundInstance);
            }
        }*/
    }
}
