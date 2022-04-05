package ac.grim.grimac.manager;

import ac.grim.grimac.GrimAC;
import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.utils.anticheat.LogUtil;
import github.scarsz.configuralize.DynamicConfig;
import github.scarsz.configuralize.Language;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ConfigManager {
    @Getter
    private final DynamicConfig config;
    @Getter
    private final File configFile = new File(GrimAPI.INSTANCE.getPlugin().getDataFolder(), "config.yml");
    @Getter
    private final File messagesFile = new File(GrimAPI.INSTANCE.getPlugin().getDataFolder(), "messages.yml");
    @Getter
    private final File discordFile = new File(GrimAPI.INSTANCE.getPlugin().getDataFolder(), "discord.yml");
    @Getter
    private final File punishFile = new File(GrimAPI.INSTANCE.getPlugin().getDataFolder(), "punishments.yml");

    public ConfigManager() {
        upgrade();

        // load config
        GrimAPI.INSTANCE.getPlugin().getDataFolder().mkdirs();
        config = new DynamicConfig();
        config.addSource(GrimAC.class, "config", getConfigFile());
        config.addSource(GrimAC.class, "messages", getMessagesFile());
        config.addSource(GrimAC.class, "discord", getDiscordFile());
        config.addSource(GrimAC.class, "punishments", getPunishFile());

        String languageCode = System.getProperty("user.language").toUpperCase();

        try {
            config.setLanguage(Language.valueOf(languageCode));
        } catch (IllegalArgumentException ignored) { // not a valid language code
        }

        // Logic for system language
        if (!config.isLanguageAvailable(config.getLanguage())) {
            String lang = languageCode.toUpperCase();
            LogUtil.info("Unknown user language " + lang + ".");
            LogUtil.info("If you fluently speak " + lang + " as well as English, see the GitHub repo to translate it!");
            config.setLanguage(Language.EN);
        }

        try {
            config.saveAllDefaults(false);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save default config files", e);
        }

        try {
            config.loadAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    private void upgrade() {
        removeLegacyTwoPointOne();
    }

    private void removeLegacyTwoPointOne() {
        File config = new File(GrimAPI.INSTANCE.getPlugin().getDataFolder(), "config.yml");
        if (config.exists()) {
            // If config doesn't have config-version, it's a legacy config
            try {
                String configString = new String(Files.readAllBytes(config.toPath()));

                if (!configString.contains("config-version")) {
                    Files.move(config.toPath(), new File(GrimAPI.INSTANCE.getPlugin().getDataFolder(), "config-2.1.old.yml").toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
