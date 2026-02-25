package cc.astralis.util.network;

import cc.astralis.Astralis;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordUtil {
    private static Set<String> loggedContents = new HashSet<>();

    //todo: remove this on release or do it differently because this is unsafe
    public static void updateCommitID() {
        String botToken = "";
        String channelId = "1253949752119136312";

        try {
            URL url = new URL("https://discord.com/api/v10/channels/" + channelId + "/messages?limit=1");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bot " + botToken);
            connection.setRequestProperty("Content-Type", "application/json");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JSONArray messages = new JSONArray(response.toString());

                if (!messages.isEmpty()) {
                    JSONObject latestMessage = messages.getJSONObject(0);
                    String messageId = latestMessage.getString("id");
                    String content = latestMessage.getString("content");

                    StringBuilder fullContent = new StringBuilder(content);

                    if (latestMessage.has("embeds")) {
                        JSONArray embeds = latestMessage.getJSONArray("embeds");
                        for (int i = 0; i < embeds.length(); i++) {
                            JSONObject embed = embeds.getJSONObject(i);

                            if (embed.has("description")) {
                                fullContent.append("\n").append(embed.getString("description"));
                            }
                        }
                    }

                    Astralis.LOGGER.error("Fetching Commit ID");
                    Astralis.commitInfo = fullContent.toString();

                    Pattern pattern = Pattern.compile("`([a-f0-9]{7})`");
                    Matcher matcher = pattern.matcher(fullContent.toString());

                    if (matcher.find()) {
                        Astralis.commitID = matcher.group(1);
                    }

                    Astralis.LOGGER.error("Found Commit ID: " + fullContent.toString());
                } else {
                    Astralis.LOGGER.error("No messages found in the channel.");
                }
            } else {
                Astralis.LOGGER.error("Error: Received HTTP response code " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}