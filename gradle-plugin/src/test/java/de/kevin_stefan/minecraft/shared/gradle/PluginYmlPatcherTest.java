package de.kevin_stefan.minecraft.shared.gradle;

import org.gradle.api.GradleException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginYmlPatcherTest {

    @Test
    void appendsSectionWhenMissing() {
        List<String> lines = new ArrayList<>(List.of("name: X", "version: 1"));
        assertTrue(PluginYmlPatcher.apply(lines, List.of("a:b:1")));
        assertEquals(List.of("name: X", "version: 1", "libraries:", "  - a:b:1"), lines);
    }

    @Test
    void insertsIntoExistingListAndKeepsIndentation() {
        List<String> lines = new ArrayList<>(List.of("name: X", "libraries:", "- a:b:1", "main: Foo"));
        assertTrue(PluginYmlPatcher.apply(lines, List.of("c:d:2")));
        assertEquals(List.of("name: X", "libraries:", "- a:b:1", "- c:d:2", "main: Foo"), lines);
    }

    @Test
    void skipsQuotedDuplicates() {
        List<String> lines = new ArrayList<>(List.of("libraries:", "  - \"a:b:1\" # keep"));
        assertFalse(PluginYmlPatcher.apply(lines, List.of("a:b:1")));
        assertEquals(2, lines.size());
    }

    @Test
    void rejectsInlineList() {
        List<String> lines = new ArrayList<>(List.of("libraries: [a:b:1]"));
        assertThrows(GradleException.class, () -> PluginYmlPatcher.apply(lines, List.of("c:d:2")));
    }

}
