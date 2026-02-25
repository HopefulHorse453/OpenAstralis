package astralis.mixin.accessor.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Comparator;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;

@Mixin(PlayerTabOverlay.class)
public interface PlayerListHudAccessor {
    @Accessor("PLAYER_COMPARATOR")
    static Comparator<PlayerInfo> getEntryOrdering() {
        throw new AssertionError();
    }
}

