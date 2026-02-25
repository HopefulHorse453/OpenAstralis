package cc.astralis.module.impl.client.cheaterfinder.checks;

import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.impl.client.cheaterfinder.Check;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.AirBlock;

import static cc.astralis.interfaces.IAccess.mc;

public class ScaffoldCheck extends Check {

    public ScaffoldCheck() {
        super("Scaffold");
    }

    @Override
    public void onMotion(MotionEvent event) {
        Player player = getPlayerData().player;
        if (player == null) {
            return;
        }

        if (player.swinging && player.getXRot() >= 70.0f && player.getUseItem() != null && player.getUseItem().getItem()
                instanceof BlockItem && getPlayerData().fastTick >= 20 && player.tickCount - getPlayerData().lastSneakTick >= 30 && player.tickCount - getPlayerData().aboveVoidTicks >= 20) {
            boolean overAir = true;
            BlockPos blockPos = player.blockPosition().below(2);
            for (int i = 0; i < 4; ++i) {
                if (!(mc.level.getBlockState(blockPos).getBlock() instanceof AirBlock)) {
                    overAir = false;
                    break;
                }
                blockPos = blockPos.below();
            }
            if (overAir) {
                warn();
            }
        }
        if(getPlayerData().sneakTicks >= 3){
            warn();
        }
    }
}
