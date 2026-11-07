package de.kevin_stefan.minecraft.shared.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.util.LinkedHashSet;
import java.util.Set;

public class SharedPlugin implements Plugin<Project> {

    static final String MARKER = "META-INF/shared-libraries.txt";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaLibraryPlugin.class);

        Configuration shared = project.getConfigurations().create("sharedLibrary", c -> {
            c.setCanBeResolved(false);
            c.setCanBeConsumed(false);
            c.setDescription("Like 'compileOnlyApi', but also adds the dependencies to the libraries section of plugin.yml");
        });

        // java-library is applied above, so compileOnlyApi already exists
        project.getConfigurations().named("compileOnlyApi", c -> c.extendsFrom(shared));

        Logger logger = project.getLogger();
        Provider<Set<String>> own = project.getProviders().provider(() -> coordinates(shared, logger));

        TaskProvider<GenerateSharedLibrariesTask> generate = project.getTasks().register(
                "generateSharedLibraries", GenerateSharedLibrariesTask.class, t -> {
                    t.setDescription("Writes the declared sharedLibrary dependencies to a marker file");
                    t.getLibraries().set(own);
                    t.getOutputDir().set(project.getLayout().getBuildDirectory().dir("generated/sharedLibraries"));
                });

        // Always look at jars: for project dependencies the compile classpath would otherwise
        // contain the classes directory, which does not contain the marker (it lives in resources).
        FileCollection runtimeJars = jars(project, "runtimeClasspath");
        FileCollection compileJars = jars(project, "compileClasspath");

        // Own marker first, then everything found on the classpaths
        FileCollection sources = project.files(generate.flatMap(GenerateSharedLibrariesTask::getOutputDir), runtimeJars, compileJars);

        project.getTasks().named("processResources", ProcessResources.class, task -> {
            // Adds the marker to the jar and creates the task dependency
            task.from(generate.flatMap(GenerateSharedLibrariesTask::getOutputDir));

            // Re-run when a dependency's marker could have changed (own libraries are covered by 'from')
            task.getInputs().files(runtimeJars, compileJars)
                    .withPropertyName("sharedLibraryClasspath")
                    .withPathSensitivity(PathSensitivity.NONE);

            // Plain class instead of a lambda: no Project reference at execution time (configuration cache)
            task.doLast(new PatchPluginYmlAction(sources));
        });

        excludeMarkerFromShadowJar(project);
    }

    private static void excludeMarkerFromShadowJar(Project project) {
        project.getTasks().withType(Jar.class).configureEach(jar -> {
            if (jar.getName().equals("shadowJar")) {
                jar.exclude(MARKER);
            }
        });
    }

    /**
     * Returns the resolved files of a configuration, always as jars.
     * <p>
     * {@code compileClasspath} normally resolves project dependencies to their classes directory,
     * which does not contain the {@link #MARKER} file (it is written to the resources output).
     * Requesting {@link LibraryElements#JAR} makes Gradle resolve the packaged jar instead, so the
     * marker can be found for external and project dependencies alike.
     * <p>
     * The returned collection is lazy: nothing is resolved until it is iterated or used as a task input.
     *
     * @param project       the project owning the configuration
     * @param configuration the name of a resolvable configuration, e.g. {@code runtimeClasspath}
     * @return a lazy file collection containing the jars of the configuration
     */
    private static FileCollection jars(Project project, String configuration) {
        return project.getConfigurations().getByName(configuration).getIncoming()
                .artifactView(view -> view.attributes(attrs -> attrs.attribute(
                        LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
                        project.getObjects().named(LibraryElements.class, LibraryElements.JAR))))
                .getFiles();
    }

    /**
     * Converts the dependencies declared in the {@code sharedLibrary} configuration into their
     * {@code group:name:version} coordinates, the format used by the {@code libraries} section of {@code plugin.yml}.
     * <p>
     * Only external module dependencies with both a group and a version can be expressed this way.
     * Anything else is skipped with a warning.
     *
     * @param shared the {@code sharedLibrary} configuration
     * @param logger used to warn about skipped dependencies
     * @return the coordinates in declaration order, without duplicates
     */
    private static Set<String> coordinates(Configuration shared, Logger logger) {
        Set<String> result = new LinkedHashSet<>();
        for (Dependency dep : shared.getDependencies()) {
            if (dep instanceof ExternalModuleDependency && dep.getGroup() != null && dep.getVersion() != null) {
                result.add(dep.getGroup() + ":" + dep.getName() + ":" + dep.getVersion());
            } else {
                logger.warn("sharedLibrary: ignoring '{}' - only external dependencies with group and version can be listed in plugin.yml", dep);
            }
        }
        return result;
    }
}
