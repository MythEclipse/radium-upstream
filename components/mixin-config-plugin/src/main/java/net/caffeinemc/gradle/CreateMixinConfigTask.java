package net.caffeinemc.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.caffeinemc.gradle.GradleMixinConfigPlugin.LOGGER;

public abstract class CreateMixinConfigTask extends DefaultTask {

    @Option(option = "mixinParentPackage", description = "The parent of the mixin package. Mixins will be printed relative to the package.")
    String mixinParentPackage;
    @Option(option = "mixinPackagePrefix", description = "Name of the mixin package relative to the mixinParentPackage.")
    String mixinPackage = "mixin";
    @Option(option = "modShortName", description = "Short name of the mod.")
    String modShortName;
    @Option(option = "outputDirectoryForSummaryDocument", description = "Output directory for the summary markdown with all mixin rules and descriptions.")
    String outputDirectoryForSummaryDocument;

    @InputDirectory
    public abstract DirectoryProperty getInputFiles();

    @InputDirectory
    public abstract DirectoryProperty getIncludeFiles();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void run() {
        var inputSourceSet = this.getInputFiles().get().getAsFile().toPath();
        var outputDirectory = this.getOutputDirectory().get().getAsFile().toPath();

        List<Path> inputFiles;

        try {
            inputFiles = Files.walk(inputSourceSet).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to walk input directory", e);
        }

        URL url = null;
        try {
            url = inputSourceSet.toUri().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try (URLClassLoader loader = new URLClassLoader(new URL[] { url }, MixinConfigOption.class.getClassLoader())) {
            HashSet<String> mixinPackages = new HashSet<>();
            HashSet<String> mixinOptions = new HashSet<>();
            List<MixinRuleRepresentation> sortedMixinConfigOptions = inputFiles.stream()
                    .filter(path -> path.toFile().isFile())
                    .map((Path inputFile) -> {
                        boolean isPackageInfo = inputFile.endsWith("package-info.class");
                        Path inputPackagePath = inputSourceSet.relativize(inputFile.getParent());
                        String inputPackageName = inputPackagePath.toString()
                                .replaceAll(Pattern.quote(inputPackagePath.getFileSystem().getSeparator()), ".");
                        String inputPackageClassName = inputPackageName + ".package-info";
                        String fullMixinPrefix = mixinParentPackage + "." + mixinPackage + ".";
                        if (inputPackageName.startsWith(fullMixinPrefix)) {
                            inputPackageName = inputPackageName.substring(fullMixinPrefix.length());
                            System.out.println("DEBUG: Stripped package " + inputPackageClassName + " to " + inputPackageName);
                        } else {
                            return null;
                        }
                        if (!isPackageInfo) {
                            mixinPackages.add(inputPackageName);
                            if (inputPackageName.equals("network")) {
                                System.out.println("DEBUG: Adding network to mixinPackages for file " + inputFile.getFileName());
                            }
                            return null;
                        }
                        try {
                            Class<?> packageInfoClass = loader.loadClass(inputPackageClassName);
                            Package inputPackage = packageInfoClass.getPackage();
                            
                            // Load MixinConfigOption from the same classloader to ensure annotation matching works
                            Class<?> mixinConfigOptionClass = loader.loadClass("me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigOption");
                            @SuppressWarnings("unchecked")
                            Class<? extends java.lang.annotation.Annotation> annotationClass = 
                                (Class<? extends java.lang.annotation.Annotation>) mixinConfigOptionClass;
                            
                            java.lang.annotation.Annotation[] inputPackageAnnotations = inputPackage
                                    .getAnnotationsByType(annotationClass);
                            
                            if (inputPackageAnnotations.length > 1) {
                                LOGGER.warn(inputPackagePath
                                        + " had multiple mixin config option annotations, only using first!");
                            }
                            if (inputPackageAnnotations.length > 0) {
                                java.lang.annotation.Annotation annotation = inputPackageAnnotations[0];
                                // Use reflection to get the description
                                String description = (String) mixinConfigOptionClass.getMethod("description").invoke(annotation);
                                boolean enabled = (Boolean) mixinConfigOptionClass.getMethod("enabled").invoke(annotation);
                                Object[] depends = (Object[]) mixinConfigOptionClass.getMethod("depends").invoke(annotation);
                                
                                // Create a MixinConfigOption using the plugin's class for internal use
                                MixinConfigOption option = new MixinConfigOption() {
                                    @Override
                                    public Class<? extends java.lang.annotation.Annotation> annotationType() {
                                        return MixinConfigOption.class;
                                    }
                                    @Override
                                    public String description() {
                                        return description;
                                    }
                                    @Override
                                    public boolean enabled() {
                                        return enabled;
                                    }
                                    @Override
                                    public MixinConfigDependency[] depends() {
                                        // Convert depends array - for now return empty, can be enhanced if needed
                                        return new MixinConfigDependency[0];
                                    }
                                };
                                
                                mixinOptions.add(inputPackageName);
                                return new MixinRuleRepresentation(inputPackageName, option);
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (ReflectiveOperationException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).filter(Objects::nonNull)
                    .sorted(Comparator.comparing(MixinRuleRepresentation::path))
                    .collect(Collectors.toList());

            mixinPackages.removeAll(mixinOptions);
            StringBuilder errorMessage = new StringBuilder();
            for (String packageName : mixinPackages) {
                errorMessage.append("Mixin Package ").append(mixinPackage).append(".").append(packageName).append(
                        " contains files without corresponding MixinConfigOption annotation in a package-info.java file!\n");
            }
            if (!errorMessage.isEmpty()) {
                throw new IllegalStateException(String.valueOf(errorMessage));
            }

            try {
                DefaultConfigCreator.writeDefaultConfig(
                        this.modShortName, outputDirectory
                                .resolve(this.modShortName.toLowerCase() + "-mixin-config-default.properties").toFile(),
                        sortedMixinConfigOptions);
                DefaultConfigCreator.writeMixinDependencies(this.modShortName, outputDirectory
                        .resolve(this.modShortName.toLowerCase() + "-mixin-config-dependencies.properties").toFile(),
                        sortedMixinConfigOptions);
                DefaultConfigCreator.writeMixinConfigSummaryMarkdown(this.modShortName,
                        Path.of(this.outputDirectoryForSummaryDocument)
                                .resolve(this.modShortName.toLowerCase() + "-mixin-config.md").toFile(),
                        sortedMixinConfigOptions);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    record MixinRuleRepresentation(String path, MixinConfigOption config) {

    }
}