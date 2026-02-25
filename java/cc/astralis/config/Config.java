package cc.astralis.config;

import cc.astralis.Astralis;
import java.io.File;
import net.minecraft.client.Minecraft;

public abstract class Config {
    public File moduleDirectory = new File(
            Minecraft.getInstance().gameDirectory,
            "/" + Astralis.NAME.toLowerCase() + "/Configs"
    );
    public File baseDirectory = new File(
            Minecraft.getInstance().gameDirectory,
            "/" + Astralis.NAME.toLowerCase()
    );

    public abstract void writeConfig();
    public abstract void loadConfig();
}
