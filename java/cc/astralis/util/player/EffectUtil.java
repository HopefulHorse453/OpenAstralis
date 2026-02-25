package cc.astralis.util.player;

import cc.astralis.interfaces.IAccess;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;

public class EffectUtil implements IAccess {

    public static boolean hasGoodEffects() {
        if (mc.player == null) {
            return false;
        }

        for (MobEffectInstance statusEffect : mc.player.getActiveEffects().stream().toList()) {
            if (statusEffect.getEffect().value().isBeneficial()) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasBadEffects() {
        if (mc.player == null) {
            return false;
        }

        for (MobEffectInstance statusEffect : mc.player.getActiveEffects().stream().toList()) {
            if (!statusEffect.getEffect().value().isBeneficial()) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasEffects() {
        return mc.player != null && !mc.player.getActiveEffects().isEmpty();
    }

    public static List<MobEffectInstance> getEffects() {
        if (mc.player == null) {
            return List.of();
        }
        return mc.player.getActiveEffects().stream().toList();
    }

    public static Component getStatusEffectDescription(@NotNull MobEffectInstance statusEffect) {
        MutableComponent mutableText = statusEffect.getEffect().value().getDisplayName().copy();
        if (statusEffect.getAmplifier() >= 1 && statusEffect.getAmplifier() <= 9) {
            MutableComponent var10000 = mutableText.append(CommonComponents.SPACE);
            int var10001 = statusEffect.getAmplifier();
            var10000.append(Component.translatable("enchantment.level." + (var10001 + 1)));
        }

        return mutableText;
    }
}
