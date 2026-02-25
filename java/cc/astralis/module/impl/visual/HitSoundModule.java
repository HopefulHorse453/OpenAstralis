package cc.astralis.module.impl.visual;

import astralis.mixin.accessor.mc.IdentifierAccessor;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.EntityInteractEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class HitSoundModule extends Module {

    private final ModeProperty mode = new ModeProperty("Hit Sound", "Skeet",
            "Skeet", "Never Lose", "Dark loves them");

    public HitSoundModule() {
        super(Category.VISUAL);
        this.registerProperty(mode);
    }

    @EventTarget
    public void onInteractEvent(EntityInteractEvent event) {
        SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getValue(getSoundIdentifier());
        if (soundEvent == null) {
            System.out.println("Sound Event is null.");
            return;
        }

        SoundInstance soundInstance = SimpleSoundInstance.forUI(soundEvent/*, SoundCategory.PLAYERS*/, 1.0f, 1.0f);
        Minecraft.getInstance().getSoundManager().play(soundInstance);
    }

    public ResourceLocation getSoundIdentifier() {
        return switch (mode.getProperty()) {
            case "Skeet" -> IdentifierAccessor.createIdentifier("astralis", "effects.skeet");
            case "Never Lose" -> IdentifierAccessor.createIdentifier("astralis", "effects.neverlose");
            case "Dark loves them" -> IdentifierAccessor.createIdentifier("astralis", "effects.dark");
            default ->
                throw new IllegalStateException("Unexpected value: " + mode.getProperty());
        };
    }
}
