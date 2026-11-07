package de.kevin_stefan.minecraft.shared.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@CacheableTask
public abstract class GenerateSharedLibrariesTask extends DefaultTask {

    @Input
    public abstract SetProperty<String> getLibraries();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @Inject
    protected abstract FileSystemOperations getFileSystem();

    @TaskAction
    public void generate() throws IOException {
        // Start clean so neither a stale marker nor an empty META-INF directory is left behind
        getFileSystem().delete(spec -> spec.delete(getOutputDir().getAsFileTree()));

        if (getLibraries().get().isEmpty()) {
            return;
        }
        Path marker = getOutputDir().get().getAsFile().toPath().resolve(SharedPlugin.MARKER);
        Files.createDirectories(marker.getParent());
        Files.writeString(marker, String.join("\n", getLibraries().get()) + "\n", StandardCharsets.UTF_8);
    }
}
