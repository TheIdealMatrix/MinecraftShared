package de.kevin_stefan.minecraft.shared.paper;

import de.kevin_stefan.minecraft.shared.common.SharedLogger;
import de.kevin_stefan.minecraft.shared.common.plugin.SharedPluginLanguage;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.spigot.SpigotSerializer;
import dev.dejvokep.boostedyaml.utils.format.NodeRole;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class SharedPaperPlugin extends JavaPlugin {

    private static SharedLogger logger;
    private static YamlDocument config;
    private static SharedPluginLanguage language;

    @Override
    public void onEnable() {
        config = initializeConfig();
        logger = new SharedLogger(getLogger(), config.getBoolean("debug", false));
        language = new SharedPluginLanguage(config);
    }

    public static SharedLogger getSharedLogger() {
        return logger;
    }

    public static YamlDocument getPluginConfig() {
        return config;
    }

    public static SharedPluginLanguage getLanguage() {
        return language;
    }

    private YamlDocument initializeConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        InputStream defaultConfigStream = getResource("config.yml");

        if (defaultConfigStream == null) {
            throw new RuntimeException("No default config.yml file found inside the plugins resources folder");
        }

        LoaderSettings loaderSettings = LoaderSettings.builder().setAutoUpdate(true).build();
        UpdaterSettings updaterSettings = UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build();
        GeneralSettings generalSettings = GeneralSettings.builder().setSerializer(SpigotSerializer.getInstance()).build();
        DumperSettings dumperSettings = DumperSettings.builder().setScalarFormatter((tag, value, role, def) -> {
            if (tag != Tag.STR) {
                return def;
            }
            return role == NodeRole.KEY ? ScalarStyle.PLAIN : ScalarStyle.DOUBLE_QUOTED;
        }).build();

        try {
            return YamlDocument.create(configFile, defaultConfigStream, loaderSettings, updaterSettings, generalSettings, dumperSettings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
