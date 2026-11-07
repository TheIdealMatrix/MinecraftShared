package de.kevin_stefan.minecraft.shared.gradle;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Runs after processResources and adds all collected shared libraries to the copied plugin.yml.
 */
class PatchPluginYmlAction implements Action<Task> {

    private final FileCollection sources;

    PatchPluginYmlAction(FileCollection sources) {
        this.sources = sources;
    }

    @Override
    public void execute(Task task) {
        Logger logger = task.getLogger();
        File pluginYml = new File(((ProcessResources) task).getDestinationDir(), "plugin.yml");
        if (!pluginYml.isFile()) {
            logger.info("sharedLibrary: {} not found - skipping", pluginYml);
            return;
        }

        Set<String> all = new LinkedHashSet<>();
        for (File file : sources) {
            all.addAll(MarkerReader.read(file, logger));
        }
        if (all.isEmpty()) {
            return;
        }

        try {
            if (PluginYmlPatcher.patch(pluginYml.toPath(), all)) {
                logger.lifecycle("sharedLibrary: added libraries to {}", pluginYml);
            }
        } catch (IOException e) {
            // Fail the build: a silently unpatched plugin.yml only shows up as a ClassNotFoundException on the server
            throw new GradleException("sharedLibrary: could not patch " + pluginYml, e);
        }
    }
}
