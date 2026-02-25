package cc.astralis.module.impl.client.cheaterfinder.checks;

import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.impl.client.cheaterfinder.Check;
import cc.astralis.module.impl.client.cheaterfinder.PlayerData;
import net.minecraft.world.entity.player.Player;

public class AutoblockCheck extends Check {

    public AutoblockCheck() {
        super("Auto Block");
    }

    @Override
    public void onMotion(MotionEvent event) {
        Player player = getPlayerData().player;
        if (player == null )  {
            return;
        }

        if(getPlayerData().autoBlockTicks >= 8) {
            warn();
        }
    }
}
