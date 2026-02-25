package cc.astralis.module.impl.client.cheaterfinder;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.module.impl.client.cheaterfinder.checks.AutoblockCheck;
import cc.astralis.module.impl.client.cheaterfinder.checks.NoslowCheck;
import cc.astralis.module.impl.client.cheaterfinder.checks.ScaffoldCheck;
import cc.astralis.module.impl.client.cheaterfinder.checks.SimulationCheck;
import cc.astralis.ui.notifications.render.Notification;
import cc.astralis.ui.notifications.NotificationBuilder;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.util.render.ChatUtil;
import java.util.*;
import java.util.regex.Pattern;
import net.minecraft.world.entity.player.Player;

public class CheaterFinderModule extends Module {
    public final BooleanProperty sendChatMessage = new BooleanProperty("Send Chat message", false),
            sendNotification = new BooleanProperty("Send Notification", false),
            alertSuspiciousName = new BooleanProperty("Alert if Suspicious Name", true);

    private final List<String> suspiciousPrefixes = Arrays.asList("Mikan_", "tzi_", "EKKOREE_", "zzxgp_");
    private final Pattern suspiciousNamePattern = Pattern.compile(
            "^([a-zA-Z]+_[a-zA-Z0-9]{5}_\\d{2}_\\d{2}$)|" +
                    "^[a-zA-Z0-9]{3}_[a-zA-Z0-9]{4}_[a-zA-Z0-9]{3}$" +
                    "|(^[a-zA-Z]{7}_[a-zA-Z]{7}\\d{2}$)"
    );

    private final Set<String> alertedPlayers = new HashSet<>();
    private final HashMap<String, Check> checks = new HashMap<>();
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>(); // <- cache

    public CheaterFinderModule() {
        super(Category.EXPLOIT);
        registerProperties(sendNotification, sendChatMessage, alertSuspiciousName);

        List<Check> checkList = List.of(
                new AutoblockCheck(),
                new NoslowCheck(),
                new ScaffoldCheck(),
                new SimulationCheck()
        );

        for (Check check : checkList) {
            checks.put(check.getName(), check);
        }
    }

    @Override
    public void onEnable() {
        alertedPlayers.clear();
        playerDataMap.clear(); // clear cache on enable
        super.onEnable();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.level == null) return;

        for (Player player : mc.level.players()) {
            if (player == mc.player || player.isDeadOrDying()) continue;

            String playerName = player.getName().getString();
            UUID playerId = player.getUUID();

            if (alertSuspiciousName.getProperty()) {
                boolean isSuspicious = suspiciousPrefixes.stream()
                        .anyMatch(prefix -> playerName.toLowerCase().startsWith(prefix.toLowerCase())) ||
                        suspiciousNamePattern.matcher(playerName).matches();

                if (isSuspicious && !alertedPlayers.contains(playerName)) {
                    String message = "Suspicious player name detected: " + playerName;

                    if (sendNotification.getProperty()) {
                        NotificationBuilder.create()
                                .notification(message, Notification.NotificationType.WARNING)
                                .duration(1000)
                                .build();
                    }

                    if (sendChatMessage.getProperty()) {
                        ChatUtil.print(message);
                    }

                    alertedPlayers.add(playerName);
                }
            }

            PlayerData data = playerDataMap.computeIfAbsent(playerId, id -> new PlayerData());
            data.update(player);
            data.updateSneak(player);
            data.updateServerPos(player);

            for (Check check : checks.values()) {
                check.setPlayerData(data);
                check.onMotion(event);
            }
        }
    }
}