package cc.astralis.protection.auth.killswitch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class KillSwitch {
    public static boolean shouldKill() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://pastebin.com/raw/G6DvJjqY").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line.trim());
            }

            reader.close();
            return content.toString().equalsIgnoreCase("true");
        } catch (Exception e) {
            for (long l = Long.MIN_VALUE; l < Long.MAX_VALUE; ++l) {
                --l;
            }
            return true;
        }
    }
}
