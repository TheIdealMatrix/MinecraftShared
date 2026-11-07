package de.kevin_stefan.minecraft.shared.gradle;

import org.gradle.api.GradleException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Line based plugin.yml editing: keeps comments, ordering and line endings intact.
 */
class PluginYmlPatcher {

    private static final Pattern LIBRARIES_KEY = Pattern.compile("^libraries:\\s*(#.*)?$");
    private static final Pattern ITEM = Pattern.compile("^(\\s*)-\\s+(.*)$");

    private PluginYmlPatcher() {
    }

    /**
     * @return true if the file was changed
     */
    static boolean patch(Path file, Collection<String> libraries) throws IOException {
        String content = Files.readString(file, StandardCharsets.UTF_8);
        String newline = content.contains("\r\n") ? "\r\n" : "\n";
        List<String> lines = new ArrayList<>(content.lines().toList());
        if (!apply(lines, libraries)) {
            return false;
        }
        Files.writeString(file, String.join(newline, lines) + newline, StandardCharsets.UTF_8);
        return true;
    }

    /**
     * @return true if lines were added
     */
    static boolean apply(List<String> lines, Collection<String> libraries) {
        if (libraries.isEmpty()) {
            return false;
        }

        int key = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (LIBRARIES_KEY.matcher(line).matches()) {
                key = i;
                break;
            }
            if (line.startsWith("libraries:")) {
                throw new GradleException("plugin.yml: 'libraries' must be a block list ('- group:artifact:version'), inline lists are not supported");
            }
        }

        if (key == -1) {
            lines.add("libraries:");
            for (String lib : libraries) {
                lines.add("  - " + lib);
            }
            return true;
        }

        Set<String> existing = new HashSet<>();
        String indent = null;
        int insertAt = key + 1;
        for (int i = key + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank() || line.stripLeading().startsWith("#")) {
                continue;
            }
            Matcher m = ITEM.matcher(line);
            if (!m.matches()) {
                break; // next key: the list is over
            }
            if (indent == null) {
                indent = m.group(1); // follow the file's own indentation
            }
            existing.add(clean(m.group(2)));
            insertAt = i + 1;
        }
        if (indent == null) {
            indent = "  ";
        }

        List<String> add = new ArrayList<>();
        for (String lib : libraries) {
            if (!existing.contains(lib)) {
                add.add(indent + "- " + lib);
            }
        }
        if (add.isEmpty()) {
            return false;
        }
        lines.addAll(insertAt, add);
        return true;
    }

    private static String clean(String raw) {
        String v = raw.strip();
        int comment = v.indexOf(" #");
        if (comment >= 0) {
            v = v.substring(0, comment).strip();
        }
        if (v.length() >= 2 && ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'")))) {
            v = v.substring(1, v.length() - 1);
        }
        return v;
    }
}
