package cc.astralis.module.impl.combat;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.player.PlayerUtil;
import cc.astralis.util.player.TeamUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public class TriggerbotModule extends Module {
    private final ModeProperty critMode = new ModeProperty("Wait for Crit", "Off", "Off", "Normal", "Force");
    private final NumberProperty percentage = new NumberProperty("Percentage", 0.95f, 0.1f, 1f, 0.01f);
    private final NumberProperty reactionTime = new NumberProperty("Reaction Time", 0, 0, 500, 10);
    private final NumberProperty missChance = new NumberProperty("Miss Chance", 0f, 0f, 1f, 0.01f);
    private final BooleanProperty weaponOnly = new BooleanProperty("Weapon Only", true);
    private final BooleanProperty ignoreTeammates = new BooleanProperty("Ignore Teammates", false);

    public TriggerbotModule() {
        super(Category.COMBAT);
        registerProperties(critMode, percentage, reactionTime, missChance, weaponOnly, ignoreTeammates);
    }
    private long reactStart = -1;

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setSuffix("" + percentage.getProperty());
        if (mc.hitResult instanceof EntityHitResult targetHit && canAttack(targetHit)) {
            long now = System.currentTimeMillis();
            if (reactStart == -1) reactStart = now;
            if (now - reactStart < reactionTime.getProperty().intValue()) return;

            if (mc.player.getAttackStrengthScale(0.0f) >= percentage.getProperty().floatValue()) {
                if (!mc.options.keyUse.isDown()) {
                    if (Math.random() < missChance.getProperty().floatValue()) {
                        mc.player.swing(InteractionHand.MAIN_HAND);
                        reactStart = -1;
                        return;
                    }
                    if (shouldCrit()) return;

                    mc.gameMode.stopDestroyBlock();
                    mc.gameMode.attack(mc.player, targetHit.getEntity());
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    reactStart = -1;
                }
            }
        } else {
            reactStart = -1;
        }
    }

    private boolean canAttack(EntityHitResult hit) {
        if (!(hit.getEntity() instanceof Player target) || !target.isAlive()) return false;
        if (weaponOnly.getProperty() && !PlayerUtil.isHoldingWeapon()) return false;
        if (ignoreTeammates.getProperty() && TeamUtil.isSameTeam(target)) return false;
        if (mc.screen != null) return false;
        return true;
    }

    private boolean shouldCrit() {
        return switch (critMode.getProperty()) {
            case "Off" -> false;
            case "Normal" -> !mc.player.onGround() && !canCrit();
            case "Force" -> !canCrit();
            default -> false;
        };
    }

    private boolean canCrit() {
        return mc.player.fallDistance > 0.1f &&
                !mc.player.onGround() &&
                !mc.player.onClimbable() &&
                !mc.player.isInWaterOrRain() &&
                !mc.player.hasEffect(MobEffects.BLINDNESS) &&
                !mc.player.hasEffect(MobEffects.SLOWNESS) &&
                !mc.player.isPassenger();
    }

}