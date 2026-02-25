package cc.astralis.module.impl.visual;

import cc.astralis.Astralis;
import cc.astralis.drag.Draggable;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.interfaces.access.IDrag;
import astralis.mixin.accessor.render.PlayerListHudAccessor;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.module.impl.combat.AntiBotModule;
import cc.astralis.util.network.BedWarsUtil;
import cc.astralis.skija.utils.SkijaUtil;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;

public class PlayerStatsModule extends Module {
    private final Draggable draggable;
    private float width = 225;

    public PlayerStatsModule() {
        super(Category.VISUAL);
        draggable = new Draggable(this.getName(), 300, 20, 200, 20);
        IDrag.draggables.add(draggable);
    }

    public CopyOnWriteArrayList<PlayerInfo> getPlayerList() {
        if (mc.getConnection() == null) {
            return new CopyOnWriteArrayList<>();
        }

        // Gets all the players and sorts them the same way as in the Player List Hud.
        List<PlayerInfo> playerInfoMap = new ArrayList<>(mc.getConnection().getListedOnlinePlayers());
        playerInfoMap.sort(PlayerListHudAccessor.getEntryOrdering());

        CopyOnWriteArrayList<PlayerInfo> playerInfoSet = new CopyOnWriteArrayList<>();

        for (PlayerInfo playerEntry : playerInfoMap) {
            if (AntiBotModule.isBot(playerEntry)) {
                continue;
            }

            playerInfoSet.add(playerEntry);
        }

        return playerInfoSet;
    }

    public String getDisplayName(PlayerInfo playerEntry) {
        if (playerEntry == null) {
            return "";
        }

        String displayName;

        if (playerEntry.getTabListDisplayName() != null) {
            displayName = playerEntry.getTabListDisplayName().getString();
        } else {
            PlayerTeam team = playerEntry.getTeam();
            String playerName = playerEntry.getProfile().name();

            if (team != null) {
                Component formatted = PlayerTeam.formatNameForTeam(team, Component.literal(playerName));
                displayName = formatted.getString();
            } else {
                displayName = playerName;
            }
        }

        displayName = displayName
                .replaceAll("§[0-9a-fk-or]", "")
                .replaceAll("(?<=\\s|^)[RBGYAWPS](?=\\s|$)", "");

        return displayName.trim();
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
     if (mc.player == null || mc.isLocalServer())
            return;

        List<PlayerInfo> entries = getPlayerList();

        final int x = (int) draggable.getX(), y = (int) draggable.getY();

        final int headerHeight = 16, playerHeight = 14;
        final float radius = !entries.isEmpty() ? 0 : 4;

        SkijaUtil.roundedRectangleGradientVarying(
                x, y,
                width, headerHeight,
                4, 4, radius, radius,
                Astralis.getInstance().getFirstColor(),
                Astralis.getInstance().getSecondColor(), false
        );

        product_regular_10.drawCenteredStringWithShadowToo(
                "Player List",
                x + (width / 2),
                y + (headerHeight / 2),
                Color.white
        );

        int index = 0;
        int itemY = y + headerHeight;

        final Color bgColor = new Color(20, 20, 30, 200);

        SkijaUtil.roundedRectangleVarying(
                x, y + headerHeight,
                width, headerHeight + (entries.size() * playerHeight),
                0, 0, 4, 4, bgColor
        );

        for (PlayerInfo entry : entries) {
            Color playerBgColor = index % 2 == 0 ? new Color(30, 30, 40, 200) : new Color(25, 25, 35, 200);
            SkijaUtil.rectangle(x, itemY, width, playerHeight, playerBgColor);

            String displayName = getDisplayName(entry);

            String[] playerInfo = new String[9];
            playerInfo[0] = displayName;

            String[] playerStats = BedWarsUtil.getPlayerStatsIfCached(displayName);

            if (playerStats != null) {
                System.arraycopy(playerStats, 0, playerInfo, 1, Math.min(playerStats.length, 8));
            } else {
                Arrays.fill(playerInfo, 1, 9, "Loading");
                BedWarsUtil.fetchPlayerStatsAsync(displayName);
            }

            int currentX = x + 3;
            float dynamicWidth = 0;

            for (int i = 0; i < playerInfo.length; i++) {
                Color textColor = i == 0 ? Color.WHITE : new Color(180, 180, 180);

                float textWidth = product_regular_10.getStringWidth(playerInfo[i]);
                dynamicWidth += textWidth + 12;

                product_regular_10.drawString(
                        playerInfo[i],
                        currentX,
                        itemY + (playerHeight / 2) - 5,
                        textColor);

                currentX += (int) (textWidth + 12);
            }

            if (dynamicWidth > width) {
                width = dynamicWidth;
            }

            itemY += playerHeight;
            index++;
        }
    }
}
