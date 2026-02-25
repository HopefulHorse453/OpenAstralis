package cc.astralis.util.gif;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.*;

public class GifFrameLoadingUtil {
    private static final String CACHE_DIR = "astralis/gif_frames/";
    public static final Map<String, String[]> textureCache = new HashMap<>();

    public static String[] cacheGifFrames(String gifPath) throws IOException {
        if (textureCache.containsKey(gifPath)) {
            return textureCache.get(gifPath);
        }

        Files.createDirectories(Paths.get(CACHE_DIR));
        String prefix = getFilePrefix(gifPath);

        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
        if (!readers.hasNext()) throw new IOException("No GIF reader found");
        ImageReader reader = readers.next();

        try (ImageInputStream input = ImageIO.createImageInputStream(new File(gifPath))) {
            reader.setInput(input);
            int numFrames = reader.getNumImages(true);
            String[] framePaths = new String[numFrames];

            BufferedImage master = null;
            int canvasWidth = 0;
            int canvasHeight = 0;

            IIOMetadata globalMeta = reader.getStreamMetadata();
            if (globalMeta != null) {
                Node tree = globalMeta.getAsTree("javax_imageio_gif_stream_1.0");
                NodeList children = tree.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node node = children.item(i);
                    if (node.getNodeName().equals("LogicalScreenDescriptor")) {
                        NamedNodeMap attr = node.getAttributes();
                        canvasWidth = Integer.parseInt(attr.getNamedItem("logicalScreenWidth").getNodeValue());
                        canvasHeight = Integer.parseInt(attr.getNamedItem("logicalScreenHeight").getNodeValue());
                        break;
                    }
                }
            }

            for (int i = 0; i < numFrames; i++) {
                BufferedImage frame = reader.read(i);
                IIOMetadata metadata = reader.getImageMetadata(i);
                String disposal = "none";
                int x = 0, y = 0, w = frame.getWidth(), h = frame.getHeight();

                Node root = metadata.getAsTree("javax_imageio_gif_image_1.0");
                NodeList children = root.getChildNodes();

                for (int j = 0; j < children.getLength(); j++) {
                    Node nodeItem = children.item(j);
                    if (nodeItem.getNodeName().equals("ImageDescriptor")) {
                        NamedNodeMap map = nodeItem.getAttributes();
                        x = Integer.parseInt(map.getNamedItem("imageLeftPosition").getNodeValue());
                        y = Integer.parseInt(map.getNamedItem("imageTopPosition").getNodeValue());
                        w = Integer.parseInt(map.getNamedItem("imageWidth").getNodeValue());
                        h = Integer.parseInt(map.getNamedItem("imageHeight").getNodeValue());
                    }
                    if (nodeItem.getNodeName().equals("GraphicControlExtension")) {
                        NamedNodeMap map = nodeItem.getAttributes();
                        disposal = map.getNamedItem("disposalMethod").getNodeValue();
                    }
                }

                if (master == null) {
                    master = new BufferedImage(
                            canvasWidth > 0 ? canvasWidth : frame.getWidth(),
                            canvasHeight > 0 ? canvasHeight : frame.getHeight(),
                            BufferedImage.TYPE_INT_ARGB
                    );
                    Graphics2D g2d = master.createGraphics();
                    g2d.setBackground(new Color(0, 0, 0, 0));
                    g2d.clearRect(0, 0, master.getWidth(), master.getHeight());
                    g2d.dispose();
                }


                Graphics2D g2 = master.createGraphics();
                g2.drawImage(frame, x, y, null);
                g2.dispose();

                // Save current frame
                BufferedImage copy = new BufferedImage(master.getColorModel(),
                        master.copyData(null),
                        master.isAlphaPremultiplied(),
                        null);

                String framePath = CACHE_DIR + prefix + "_frame_" + i + ".png";
                ImageIO.write(copy, "png", new File(framePath));
                framePaths[i] = framePath;

                // Handle disposal
                if (disposal.equals("restoreToBackgroundColor")) {
                    Graphics2D clear = master.createGraphics();
                    clear.setComposite(AlphaComposite.Clear);
                    clear.fillRect(x, y, w, h);
                    clear.dispose();
                } else if (disposal.equals("restoreToPrevious")) {
                    // Not handled — could add stack logic if needed
                }
            }

            textureCache.put(gifPath, framePaths);
            return framePaths;
        } finally {
            reader.dispose();
        }
    }

    private static String getFilePrefix(String filePath) {
        String filename = new File(filePath).getName();
        return filename.replaceAll("[^a-zA-Z0-9_-]", "").replace(".gif", "");
    }

    public static void clearCache() throws IOException {
        Path cachePath = Paths.get(CACHE_DIR);
        if (Files.exists(cachePath)) {
            Files.walk(cachePath)
                    .sorted((a, b) -> b.compareTo(a))
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}
