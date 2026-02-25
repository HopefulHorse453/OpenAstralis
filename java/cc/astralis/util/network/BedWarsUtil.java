package cc.astralis.util.network;

import cc.astralis.util.io.StringUtil;
import cc.astralis.util.io.ThreadUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class BedWarsUtil {
    private static final String apiURL = "https://bwstats.shivam.pro/user/";

    private static final ConcurrentHashMap<String, String[]> PLAYER_STATS_CACHE = new ConcurrentHashMap<>();

    public static String[] getPlayerStatsIfCached(String playerName) {
        return PLAYER_STATS_CACHE.get(playerName);
    }

    public static void fetchPlayerStatsAsync(String playerName) {
        if (playerName == null || PLAYER_STATS_CACHE.containsKey(playerName)) return;

        ThreadUtil.runAsync(() -> {
            try {
                String stats = getPlayerStatList(playerName);
                String[] allStats = new String[] {
                        "Wins: " + extractWins(stats),
                        "Loses: " + extractLosses(stats),
                        "GP: " + getGamesPlayed(stats),
                        "W/L: " + extractWinLossRatio(stats),
                        "KDR: " + extractKDR(stats),
                        "FKDR: " + extractFKDR(stats),
                        "BBLR: " + extractBBLR(stats),
                        "WS: " +extractWS(stats)
                };
                PLAYER_STATS_CACHE.put(playerName, allStats);
            } catch (Exception e) {
                PLAYER_STATS_CACHE.put(playerName, new String[] {
                        "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A"
                });
            }
        });
    }


    private static String getPlayerStatList(String playerName) throws Exception {
            URL url = new URL(apiURL + StringUtil
                    .stripControlCodes(playerName));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
    }

    private static String extractWins(String stats) {
        return extractStat(stats, "Wins: ", ",");
    }

    private static String extractLosses(String stats) {
        return extractStat(stats, "Losses: ", ",");
    }

    private static String getGamesPlayed(String stats) {
        return extractStat(stats, "Games Played: ", ",");
    }

    private static String extractWinLossRatio(String stats) {
        return extractStat(stats, "Win/Loss Ratio: ", " /");
    }

    private static String extractKDR(String stats) {
        return extractStat(stats, "K/D Ratio (KDR): ", ",");
    }

    private static String extractFKDR(String stats) {
        return extractStat(stats, "Final K/D Ratio (FKDR): ", " /");
    }

    private static String extractBBLR(String stats) {
        return extractStat(stats, "Beds B/L Ratio (BBLR): ", " /");
    }

    private static String extractWS(String stats) {
        return extractStat(stats, "Winstreak: ", ",");
    }

    private static String extractStat(String stats, String startKey, String endKey) {
        int startIndex = stats.indexOf(startKey);
        if (startIndex == -1) {
            return "N/A";
        }
        startIndex += startKey.length();
        int endIndex = stats.indexOf(endKey, startIndex);
        if (endIndex == -1) {
            endIndex = stats.length();
        }
        if (stats.substring(startIndex, endIndex).trim().equals("Infinity")) {
            return "NA";
        }
        return stats.substring(startIndex, endIndex).trim();
    }
}
