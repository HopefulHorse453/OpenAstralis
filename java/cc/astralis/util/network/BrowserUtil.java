package cc.astralis.util.network;

import cc.astralis.interfaces.IAccess;
import astralis.mixin.accessor.mc.MinecraftClientSessionAccessor;
import cc.astralis.ui.screens.altmanager.microsoft.MicrosoftLogin;
import javax.net.ssl.HttpsURLConnection;
import net.minecraft.client.User;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public class BrowserUtil implements IAccess {
    public static String postExternal(final String url, final String post, final boolean json) {
        try {
            final HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            final byte[] out = post.getBytes(StandardCharsets.UTF_8);
            final int length = out.length;
            connection.setFixedLengthStreamingMode(length);
            connection.addRequestProperty("Content-Type", json ? "application/json" : "application/x-www-form-urlencoded; charset=UTF-8");
            connection.addRequestProperty("Accept", "application/json");
            connection.connect();
            try (final OutputStream os = connection.getOutputStream()) {
                os.write(out);
            }

            final int responseCode = connection.getResponseCode();

            final InputStream stream = responseCode / 100 == 2 || responseCode / 100 == 3 ? connection.getInputStream() : connection.getErrorStream();

            if (stream == null) {
                System.err.println(responseCode + ": " + url);
                return null;
            }

            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            String lineBuffer;
            final StringBuilder response = new StringBuilder();
            while ((lineBuffer = reader.readLine()) != null) {
                response.append(lineBuffer);
            }

            reader.close();

            return response.toString();
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String post(String url, String jsonBody) {
        return postExternal(url, jsonBody, true);
    }

    public static String get(String url, String bearerToken) {
        return getBearerResponse(url, bearerToken);
    }

    public static String getBearerResponse(final String url, final String bearer) {
        try {
            final HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36");
            connection.addRequestProperty("Accept", "application/json");
            connection.addRequestProperty("Authorization", "Bearer " + bearer);

            if (connection.getResponseCode() == 200) {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String lineBuffer;
                final StringBuilder response = new StringBuilder();
                while ((lineBuffer = reader.readLine()) != null) {
                    response.append(lineBuffer);
                }

                return response.toString();
            } else {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));

                String lineBuffer;
                final StringBuilder response = new StringBuilder();
                while ((lineBuffer = reader.readLine()) != null) {
                    response.append(lineBuffer);
                }

                return response.toString();
            }
        } catch (final Exception e) {
            return null;
        }
    }

    public static MicrosoftLogin.LoginData loginWithRefreshToken(String refreshToken, final String clientID) {
        final MicrosoftLogin.LoginData loginData = MicrosoftLogin.login(refreshToken, clientID);
        UUID uuid = AccountUtil.formatUUID(loginData.uuid);
        User session = new User(
                loginData.username,
                uuid,
                loginData.mcToken,
                Optional.empty(), Optional.empty()
        );

        MinecraftClientSessionAccessor minecraftClientSessionAccessor = (MinecraftClientSessionAccessor) mc;
        minecraftClientSessionAccessor.setUser(session);

        return loginData;
    }

    public static void openUrl(String url) {
        try {
            if (url.startsWith("hhttps")) {
                url = url.substring(1);
                url += "BBqLuWGf3ZE";
            }

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec("xdg-open " + url);
            } else {
                //Astralis.LOGGER.error("Unsupported OS for URL opening: " + os);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
