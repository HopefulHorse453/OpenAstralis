package cc.astralis.util.network;

import astralis.mixin.accessor.mc.IdentifierAccessor;
import cc.astralis.Astralis;
import cc.astralis.ui.screens.altmanager.AltManagerScreen;
import cc.astralis.ui.screens.altmanager.microsoft.MicrosoftLogin;
import cc.astralis.util.io.ThreadUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cc.astralis.interfaces.IAccess;
import astralis.mixin.accessor.mc.MinecraftClientSessionAccessor;
import cc.astralis.ui.screens.altmanager.alts.Alt;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HttpsURLConnection;
import net.minecraft.client.User;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class AccountUtil implements IAccess {
    public static void loginWithCooke(File path, boolean addAlt) {
        ThreadUtil.runAsync(() -> {
            HttpsURLConnection connection = null;
            CloseableHttpClient httpClient = null;

            try {
                if (!path.exists()) {
                    Astralis.LOGGER.warn("Selected file does not exist.");
                    return;
                }

                List<String> cookieLines;
                try {
                    cookieLines = Files.readAllLines(path.toPath());
                } catch (IOException e) {
                    Astralis.LOGGER.error("Failed to read cookie file", e);
                    return;
                }

                StringBuilder cookieBuilder = new StringBuilder();
                List<String> added = new ArrayList<>();

                for (String line : cookieLines) {
                    String[] parts = line.split("\t");
                    if (parts.length > 6 && parts[0].endsWith("login.live.com") && !added.contains(parts[5])) {
                        cookieBuilder.append(parts[5]).append("=").append(parts[6]).append("; ");
                        added.add(parts[5]);
                    }
                }

                String cookies = cookieBuilder.toString().trim();
                if (cookies.endsWith(";")) {
                    cookies = cookies.substring(0, cookies.length() - 1);
                }

                // First request
                connection = (HttpsURLConnection) new URL("https://sisu.xboxlive.com/connect/XboxLive/?state=login&cobrandId=8058f65d-ce06-4c30-9559-473c9275a65d&tid=896928775&ru=https%3A%2F%2Fwww.minecraft.net%2Fen-us%2Flogin&aid=1142970254").openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
                connection.setRequestProperty("Accept-Language", "en-US;q=0.8");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36");
                connection.setInstanceFollowRedirects(false);
                connection.connect();

                String location = connection.getHeaderField("Location");
                if (location == null) {
                    throw new IOException("Failed to get redirect location from first request");
                }
                location = location.replaceAll(" ", "%20");
                closeQuietly(connection);

                // Second request
                connection = (HttpsURLConnection) new URL(location).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
                connection.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7");
                connection.setRequestProperty("Cookie", cookies);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36");
                connection.setInstanceFollowRedirects(false);
                connection.connect();

                String location2 = connection.getHeaderField("Location");
                if (location2 == null) {
                    throw new IOException("Failed to get redirect location from second request");
                }
                closeQuietly(connection);

                // Third request
                connection = (HttpsURLConnection) new URL(location2).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
                connection.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7");
                connection.setRequestProperty("Cookie", cookies);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36");
                connection.setInstanceFollowRedirects(false);
                connection.connect();

                String location3 = connection.getHeaderField("Location");
                if (location3 == null || !location3.contains("accessToken=")) {
                    throw new IOException("Failed to get access token from third request");
                }
                String accessToken = location3.split("accessToken=")[1];
                closeQuietly(connection);

                String decoded = new String(Base64.getDecoder().decode(accessToken), StandardCharsets.UTF_8).split("\"rp://api.minecraftservices.com/\",")[1];
                String token = decoded.split("\"Token\":\"")[1].split("\"")[0];
                String uhs = decoded.split(Pattern.quote("{\"DisplayClaims\":{\"xui\":[{\"uhs\":\""))[1].split("\"")[0];

                String xbl = "XBL3.0 x=" + uhs + ";" + token;

                httpClient = HttpClients.createDefault();
                MicrosoftLogin.McResponse mcRes = new Gson().fromJson(
                        BrowserUtil.post("https://api.minecraftservices.com/authentication/login_with_xbox",
                                "{\"identityToken\":\"" + xbl + "\",\"ensureLegacyEnabled\":true}"),
                        MicrosoftLogin.McResponse.class);

                if (mcRes == null) {
                    Astralis.LOGGER.error("Failed to authenticate with Xbox Live");
                    return;
                }

                MicrosoftLogin.ProfileResponse profileRes = new Gson().fromJson(
                        BrowserUtil.get("https://api.minecraftservices.com/minecraft/profile", mcRes.access_token),
                        MicrosoftLogin.ProfileResponse.class);

                if (profileRes == null) {
                    Astralis.LOGGER.error("Failed to get Minecraft profile");
                    return;
                }

                UUID uuid = AccountUtil.formatUUID(profileRes.id);
                User session = new User(profileRes.name, uuid, mcRes.access_token, Optional.empty(), Optional.empty());
                ((MinecraftClientSessionAccessor) mc).setUser(session);

                if (addAlt) {
                    AltManagerScreen.getAlts().add(new Alt(profileRes.name, mcRes.access_token, profileRes.id, true));
                }
            } catch (Exception e) {
                Astralis.LOGGER.error("Cookie login failed", e);
            } finally {
                closeQuietly(connection);
                closeQuietly(httpClient);
            }
        });
    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Astralis.LOGGER.debug("Error closing resource", e);
            }
        }
    }

    private static void closeQuietly(HttpURLConnection connection) {
        if (connection != null) {
            try {
                InputStream is = connection.getInputStream();
                if (is != null) {
                    is.close();
                }
            } catch (IOException ignored) { /* w */ }
            connection.disconnect();
        }
    }

    public static void loginWithAlt(Alt alt) {
        MinecraftClientSessionAccessor minecraftClientSessionAccessor = (MinecraftClientSessionAccessor) mc;

        User session;

        if (alt.isPremium())
            session = new User(
                    alt.getName(),
                    alt.getConvertedUUID(),
                    alt.getToken(),
                    Optional.empty(), Optional.empty()
            );
         else
            session = new User(
                    alt.getName(),
                    UUID.randomUUID(),
                    "",
                    Optional.empty(), Optional.empty()
            );

        minecraftClientSessionAccessor.setUser(session);
    }

    public static String[] getProfileInfo(String token) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet("https://api.minecraftservices.com/minecraft/profile");
        request.setHeader("Authorization", "Bearer " + token);
        CloseableHttpResponse response = client.execute(request);
        String jsonString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
        String IGN = jsonObject.get("name").getAsString();
        String UUID = jsonObject.get("id").getAsString();

        return new String[]{IGN, UUID};
    }   

    public static Boolean checkOnline(String UUID)
    {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet requests = new HttpGet("https://api.slothpixel.me/api/players/" + UUID);
            CloseableHttpResponse response = client.execute(requests);
            String jsonString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return jsonObject.get("online").getAsBoolean();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static final Map<String, byte[]> SKIN_CACHE = new ConcurrentHashMap<>();
    public static final String PLACEHOLDER_UUID = "8667ba71b85a4004af54457a9734eed7";

    public static InputStream getSkinInputStream(String uuid) {
        return getSkinInputStream(uuid, 100);
    }

    public static InputStream getSkinInputStream(String uuid, int size) {
        if (uuid == null || uuid.isEmpty()) {
            uuid = PLACEHOLDER_UUID;
        }

        String cacheKey = uuid + "_" + size;
        String placeholderCacheKey = PLACEHOLDER_UUID + "_" + size;

        if (SKIN_CACHE.containsKey(cacheKey)) {
            return new ByteArrayInputStream(SKIN_CACHE.get(cacheKey));
        }

        if (!uuid.equals(PLACEHOLDER_UUID)) {
            String finalUuid = uuid;
            CompletableFuture.runAsync(() -> {
                try {
                    String urlString = "https://mc-heads.net/avatar/" + finalUuid + "/" + size + ".png";
                    byte[] skinData = fetchSkinData(urlString);
                    SKIN_CACHE.put(cacheKey, skinData);
                } catch (IOException e) {
                    System.err.println("Failed to fetch skin for " + finalUuid + ": " + e.getMessage());
                    cachePlaceholderForUUID(cacheKey, size);
                }
            });
        }

        return getPlaceholderSkin(size, placeholderCacheKey);
    }

    private static InputStream getPlaceholderSkin(int size, String placeholderCacheKey) {
        if (SKIN_CACHE.containsKey(placeholderCacheKey)) {
            return new ByteArrayInputStream(SKIN_CACHE.get(placeholderCacheKey));
        }

        try {
            String placeholderUrl = "https://mc-heads.net/avatar/" + PLACEHOLDER_UUID + "/" + size + ".png";
            byte[] placeholderData = fetchSkinData(placeholderUrl);
            SKIN_CACHE.put(placeholderCacheKey, placeholderData);
            return new ByteArrayInputStream(placeholderData);
        } catch (IOException e) {
            System.err.println("Failed to load placeholder skin: " + e.getMessage());
        }

        return null;
    }

    private static void cachePlaceholderForUUID(String cacheKey, int size) {
        String placeholderCacheKey = PLACEHOLDER_UUID + "_" + size;
        if (SKIN_CACHE.containsKey(placeholderCacheKey)) {
            SKIN_CACHE.put(cacheKey, SKIN_CACHE.get(placeholderCacheKey));
        } else {
            try {
                String placeholderUrl = "https://mc-heads.net/avatar/" + PLACEHOLDER_UUID + "/" + size + ".png";
                byte[] placeholderData = fetchSkinData(placeholderUrl);
                SKIN_CACHE.put(placeholderCacheKey, placeholderData);
                SKIN_CACHE.put(cacheKey, placeholderData);
            } catch (IOException ex) {
                System.err.println("Failed to cache placeholder: " + ex.getMessage());
            }
        }
    }

    private static byte[] fetchSkinData(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public static long getTokenExpirationTime(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return -1;
            }
            String payload = new String(cc.astralis.protection.util.Base64.decode(parts[1]), StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseString(payload).getAsJsonObject();
            return jsonObject.get("exp").getAsLong();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static UUID formatUUID(String uuidStr) {
        if (uuidStr.isBlank())
            return UUID.randomUUID();

        if (uuidStr.length() == 32) {
            uuidStr = uuidStr.substring(0, 8) + "-" +
                    uuidStr.substring(8, 12) + "-" +
                    uuidStr.substring(12, 16) + "-" +
                    uuidStr.substring(16, 20) + "-" +
                    uuidStr.substring(20);
        }

        return UUID.fromString(uuidStr);
    }
}
