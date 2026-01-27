package net.caffeinemc.caffeineconfig;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
// import net.fabricmc.loader.api.FabricLoader;
// import net.fabricmc.loader.api.ModContainer;
// import net.fabricmc.loader.api.metadata.CustomValue;
// import net.fabricmc.loader.api.metadata.CustomValue.CvType;
// import net.fabricmc.loader.api.metadata.ModMetadata;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A mixin configuration object. Holds the {@link Option options} defined and
 * handles overrides.
 *
 * @see CaffeineConfig.Builder
 */
@SuppressWarnings("CanBeFinal")
public final class CaffeineConfig {
    private final Map<String, Option> options = new HashMap<>();
    private final Set<Option> optionsWithDependencies = new ObjectLinkedOpenHashSet<>();

    private final String modName;
    private Logger logger;

    private CaffeineConfig(String modName) {
        this.modName = modName;
    }

    public static CaffeineConfig.Builder builder(String modName) {
        CaffeineConfig config = new CaffeineConfig(modName);
        config.logger = LoggerFactory.getLogger(modName + " Config");

        String jsonKey = modName.toLowerCase() + ":options";

        return config.new Builder().withSettingsKey(jsonKey);
    }

    public String getModName() {
        return modName;
    }

    public Logger getLogger() {
        return logger;
    }

    // Adapters for LithiumConfig
    public Option getOption(String name) {
        return this.options.get(name);
    }

    public Map<String, Option> getOptions() {
        return this.options;
    }

    private void addOptionDependency(String optionName, String dependency, boolean requiredValue) {
        String mixinOptionName = getMixinOptionName(optionName);
        Option option = this.options.get(mixinOptionName);

        if (option == null) {
            throw new IllegalArgumentException(String.format("Option %s for dependency '%s depends on %s=%s' not found",
                    optionName, optionName, dependency, requiredValue));
        }

        String dependencyOptionName = getMixinOptionName(dependency);
        Option dependencyOption = this.options.get(dependencyOptionName);

        if (dependencyOption == null) {
            throw new IllegalArgumentException(String.format("Option %s for dependency '%s depends on %s=%s' not found",
                    dependency, optionName, dependency, requiredValue));
        }

        option.addDependency(dependencyOption, requiredValue);
        this.optionsWithDependencies.add(option);
    }

    private void addMixinOption(String mixin, boolean enabled) {
        String name = getMixinOptionName(mixin);

        if (this.options.putIfAbsent(name, new Option(name, enabled, false)) != null) {
            throw new IllegalStateException("Mixin option already defined: " + mixin);
        }
    }

    private void readProperties(Properties props) {
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            Option option = this.options.get(key);

            if (option == null) {
                logger.warn("No configuration key exists with name '{}', ignoring", key);
                continue;
            }

            boolean enabled;

            if (value.equalsIgnoreCase("true")) {
                enabled = true;
            } else if (value.equalsIgnoreCase("false")) {
                enabled = false;
            } else {
                logger.warn("Invalid value '{}' encountered for configuration key '{}', ignoring", value, key);
                continue;
            }

            option.setEnabled(enabled, true);
        }
    }

    private void applyModOverrides(String jsonKey) {
        // Fabric specific logic removed for Forge port
    }

    /*
     * private void applyModOverride(ModMetadata meta, String name, CustomValue
     * value) {
     * Option option = this.options.get(name);
     * 
     * if (option == null) {
     * logger.
     * warn("Mod '{}' attempted to override option '{}', which doesn't exist, ignoring"
     * , meta.getId(), name);
     * return;
     * }
     * 
     * if (value.getType() != CvType.BOOLEAN) {
     * logger.
     * warn("Mod '{}' attempted to override option '{}' with an invalid value, ignoring"
     * , meta.getId(), name);
     * return;
     * }
     * 
     * boolean enabled = value.getAsBoolean();
     * 
     * // disabling the option takes precedence over enabling
     * if (!enabled && option.isEnabled()) {
     * option.clearModsDefiningValue();
     * }
     * 
     * if (!enabled || option.isEnabled() || option.getDefiningMods().isEmpty()) {
     * option.addModOverride(enabled, meta.getId());
     * }
     * }
     */

    public Option getEffectiveOptionForMixin(String mixinClassName) {
        int lastSplit = 0;
        int nextSplit;

        Option option = null;

        while ((nextSplit = mixinClassName.indexOf('.', lastSplit)) != -1) {
            String key = getMixinOptionName(mixinClassName.substring(0, nextSplit));

            Option candidate = this.options.get(key);

            if (candidate != null) {
                option = candidate;

                if (!option.isEnabled()) {
                    return option;
                }
            }

            lastSplit = nextSplit + 1;
        }

        return option;
    }

    private boolean applyDependencies() {
        boolean changed = false;

        for (Option optionWithDependency : this.optionsWithDependencies) {
            changed |= optionWithDependency.disableIfDependenciesNotMet(logger);
        }

        return changed;
    }

    private static void writeDefaultConfig(Path file, String modName, String infoUrl) throws IOException {
        Path dir = file.getParent();

        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        } else if (!Files.isDirectory(dir)) {
            throw new IOException("The parent file is not a directory");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write(String.format("# This is the configuration file for %s.\n", modName));
            writer.write("# This file exists for debugging purposes and should not be configured otherwise.\n");
            writer.write("#\n");

            if (infoUrl != null) {
                writer.write("# You can find information on editing this file and all the available options here:\n");
                writer.write("# " + infoUrl + "\n");
                writer.write("#\n");
            }

            writer.write("# By default, this file will be empty except for this notice.\n");
        }
    }

    private static String getMixinOptionName(String name) {
        return "mixin." + name;
    }

    public int getOptionCount() {
        return this.options.size();
    }

    public int getOptionOverrideCount() {
        return (int) this.options.values()
                .stream()
                .filter(Option::isOverridden)
                .count();
    }

    public final class Builder {
        private boolean alreadyBuilt = false;

        private String infoUrl;
        private String jsonKey;

        private Builder() {
        }

        public Builder addMixinOption(String mixin, boolean enabled) {
            CaffeineConfig.this.addMixinOption(mixin, enabled);
            return this;
        }

        // Adapter for property files that already have "mixin." prefix
        public Builder addMixinRule(String ruleName, boolean enabled) {
            if (ruleName.startsWith("mixin.")) {
                return addMixinOption(ruleName.substring(6), enabled);
            }
            return addMixinOption(ruleName, enabled);
        }

        public Builder addRuleDependency(String ruleName, String dependencyName, boolean requiredState) {
            if (ruleName.startsWith("mixin."))
                ruleName = ruleName.substring(6);
            if (dependencyName.startsWith("mixin."))
                dependencyName = dependencyName.substring(6);
            return addOptionDependency(ruleName, dependencyName, requiredState);
        }

        public Builder addOptionDependency(String option, String dependency, boolean requiredValue) {
            CaffeineConfig.this.addOptionDependency(option, dependency, requiredValue);
            return this;
        }

        public Builder withLogger(Logger logger) {
            CaffeineConfig.this.logger = logger;
            return this;
        }

        public Builder withSettingsKey(String key) {
            this.jsonKey = key;
            return this;
        }

        public Builder withInfoUrl(String url) {
            this.infoUrl = url;
            return this;
        }

        public CaffeineConfig build(Path path) {
            if (alreadyBuilt) {
                throw new IllegalStateException("Cannot build a CaffeineConfig twice from the same builder");
            }

            if (Files.exists(path)) {
                Properties props = new Properties();

                try (InputStream fin = Files.newInputStream(path)) {
                    props.load(fin);
                } catch (IOException e) {
                    throw new RuntimeException("Could not load config file", e);
                }

                readProperties(props);
            } else {
                try {
                    writeDefaultConfig(path, modName, infoUrl);
                } catch (IOException e) {
                    logger.warn("Could not write default configuration file", e);
                }
            }

            applyModOverrides(jsonKey);

            // Check dependencies several times, because one iteration may disable a option
            // required by another option
            // This terminates because each additional iteration will disable one or more
            // options, and there is only a finite number of rules
            while (applyDependencies())
                ;

            this.alreadyBuilt = true;

            return CaffeineConfig.this;
        }
    }
}
