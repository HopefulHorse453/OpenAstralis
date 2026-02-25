package cc.astralis.module.impl.client.cheaterfinder.checks;

import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.impl.client.cheaterfinder.Check;
import net.minecraft.world.entity.player.Player;

public class NoslowCheck extends Check {

    public NoslowCheck() {
        super("No Slow");
    }

    @Override
    public void onMotion(MotionEvent event) {
        Player player = getPlayerData().player;
        if (player == null )  {
            return;
        }
        if (getPlayerData().noSlowTicks >= 11 && getPlayerData().speed >= 0.23) {
            warn();
        }
    }
}
