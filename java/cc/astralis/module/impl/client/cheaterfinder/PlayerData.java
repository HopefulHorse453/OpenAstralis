package cc.astralis.module.impl.client.cheaterfinder;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class PlayerData {
    public double speed;
    public int aboveVoidTicks;
    public int fastTick;
    public int autoBlockTicks;
    public int ticksExisted;
    public int lastSneakTick;
    public double posZ;
    public int sneakTicks;
    public int noSlowTicks;
    public double posY;
    public boolean sneaking;
    public double posX;
    public double serverPosX;
    public double serverPosY;
    public double serverPosZ;
    public String name;
    public Player player;

    public void update(Player player) {
        final int ticksExisted = player.tickCount;

        this.player = player;
        this.name = player.getName().getString();
        this.posX = player.getX() - player.xo;
        this.posY = player.getY() - player.yo;
        this.posZ = player.getZ() - player.zo;
        this.speed = Math.max(Math.abs(this.posX), Math.abs(this.posZ));

        if (this.speed >= 0.07) {
            this.fastTick++;
            this.ticksExisted = ticksExisted;
        } else {
            this.fastTick = 0;
        }

        if (Math.abs(this.posY) >= 0.1) {
            this.aboveVoidTicks = ticksExisted;
        }

        if (player.isShiftKeyDown()) {
            this.lastSneakTick = ticksExisted;
        }

        if (player.attackAnim > 0.0F && player.isBlocking()) {
            this.autoBlockTicks++;
        } else {
            this.autoBlockTicks = 0;
        }

        if (player.isSprinting() && player.isUsingItem()) {
            this.noSlowTicks++;
        } else {
            this.noSlowTicks = 0;
        }

        if (player.getXRot() >= 70.0f) {
            ItemStack held = player.getMainHandItem();
            if (!held.isEmpty() && held.getItem() instanceof BlockItem) {
                if (player.swinging) {
                    if (!this.sneaking && player.isShiftKeyDown()) {
                        this.sneakTicks++;
                    } else {
                        this.sneakTicks = 0;
                    }
                }
            }
        } else {
            this.sneakTicks = 0;
        }
    }

    public void updateSneak(Player player) {
        this.sneaking = player.isShiftKeyDown();
    }

    public void updateServerPos(Player player) {
        this.serverPosX = player.getX();
        this.serverPosY = player.getY();
        this.serverPosZ = player.getZ();
    }
}
