package cc.astralis.ui.animations;

import cc.astralis.Astralis;
import cc.astralis.module.Module;

import java.util.HashMap;

/**
 * @author plusbox (thx pluh)
 * @since 3/23/2024
 */
public final class AnimationManager {
    private final HashMap<Module, Animation> moduleAnimationMap = new HashMap<>();

    public void start() {
        for (final Module module : Astralis.getInstance().getModuleManager().getModules()) {
            moduleAnimationMap.put(module, new Animation(Easing.EASE_IN_OUT_SINE, 250));
        }
    }

    public Animation getAnimation(final Module module) {
        return moduleAnimationMap.get(module);
    }
}

