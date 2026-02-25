package cc.astralis.util.io;

import cc.astralis.Astralis;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import cc.astralis.interfaces.IAccess;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import java.awt.Font;

public class FileUtil implements IAccess {

    public static void writeJsonToFile(JsonObject json, String path) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(path);

            gson.toJson(json, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Font loadFont(ResourceLocation fontIdentifier) {
        try {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            Optional<Resource> resource = resourceManager.getResource(fontIdentifier);

            try (InputStream is = resource.get().open()) {
                return Font.createFont(Font.TRUETYPE_FONT, is);
            }
        } catch (Exception e) {
            Astralis.LOGGER.error("Failed To Load Font: " + e.getMessage());
            return null;
        }
    }

    private static Path resolveFontPath(ResourceLocation fontIdentifier) {
        try {
            InputStream fontStream = mc.getResourceManager().getResource(fontIdentifier).get().open();

            Path tempFontPath = Paths.get(System.getProperty("java.io.tmpdir"), fontIdentifier.getPath());
            Files.createDirectories(tempFontPath.getParent());

            Files.copy(fontStream, tempFontPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return tempFontPath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final Map<String, ResourceLocation> TEXTURE_CACHE = new ConcurrentHashMap<>();

    public static ResourceLocation getCachedIdentifier(Font[] fonts, char from, char to, float size) {
        String key = Arrays.stream(fonts)
                .map(f -> f.getName() + f.getStyle() + f.getSize())
                .collect(Collectors.joining()) + from + to + size;

        return TEXTURE_CACHE.computeIfAbsent(key, k ->
                generateGlyphMapIdentifier(fonts, from, to, size));
    }

    public static ResourceLocation generateGlyphMapIdentifier(Font[] fonts, char from, char to, float size) {
        String fontHash = Arrays.stream(fonts)
                .map(f -> f.getName() + f.getStyle() + f.getSize())
                .collect(Collectors.joining());

        String path = "glyphs/" +
                StringUtil.sha1Hash(fontHash) + "/" +
                (int)from + "-" + (int)to + "-" +
                (int)size;

        return ResourceLocation.fromNamespaceAndPath("renderer", path);
    }

    public record FontEntry(String name, String path) {
        @Override
        public String toString() {
            return name + " (" + path + ")";
        }
    }

    public static List<FontEntry> getAllInstalledFonts() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return getAllFontsWindows();
        } else {
            return getAllFontsLinux();
        }
    }

    private static List<FontEntry> getAllFontsLinux() {
        List<FontEntry> fonts = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"fc-list"});
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    int colonIndex = line.indexOf(':');
                    if (colonIndex == -1) continue;

                    String path = line.substring(0, colonIndex).trim();
                    String names = line.substring(colonIndex + 1).trim();

                    String fontName = names.split(":")[0].trim();

                    fonts.add(new FontEntry(fontName, path));
                }
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fonts;
    }

    private static List<FontEntry> getAllFontsWindows() {
        List<FontEntry> fonts = new ArrayList<>();
        File fontDir = new File(System.getenv("WINDIR") + "\\Fonts");
        scanFontsWindows(fontDir, fonts);
        return fonts;
    }

    private static void scanFontsWindows(File dir, List<FontEntry> fonts) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                scanFontsWindows(f, fonts);
            } else {
                String name = f.getName().toLowerCase();
                if (name.endsWith(".ttf") || name.endsWith(".otf")) {
                    try {
                        java.awt.Font font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, f);
                        fonts.add(new FontEntry(font.getFontName(), f.getAbsolutePath()));
                    } catch (Exception ignored) {}
                }
            }
        }
    }
}
