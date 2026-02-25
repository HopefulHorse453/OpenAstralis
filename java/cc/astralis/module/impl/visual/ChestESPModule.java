package cc.astralis.module.impl.visual;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.render.Render3DEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.ColorProperty;
import cc.astralis.util.player.StorageUtil;
import cc.astralis.util.render.Render3DUtil;
import java.awt.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import static cc.astralis.util.player.StorageUtil.collectLoadedAroundPlayer;

public class ChestESPModule extends Module {
    private final ColorProperty color = new ColorProperty("Color", Color.red);

    public ChestESPModule() {
        super(Category.VISUAL);
        this.registerProperties(color);
    }

    @Override
    public void onEnable() {
        StorageUtil.setCollectData(true);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        StorageUtil.setCollectData(false);
        super.onDisable();
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        collectLoadedAroundPlayer();
        for (BlockPos blockPos : StorageUtil.snapshotAll()) {
            Render3DUtil.drawBoxESP(event.getMatricies(), new AABB(blockPos), color.getProperty(),150);
        }
    }
}