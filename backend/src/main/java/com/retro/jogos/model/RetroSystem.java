package com.retro.jogos.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum RetroSystem {
    SNES("snes", "Super Nintendo", Arrays.asList(".sfc", ".smc"), "#8B5CF6", "🎮"),
    GBA("gba", "Game Boy Advance", Arrays.asList(".gba"), "#EC4899", "🕹️"),
    NES("nes", "NES (Nintendinho)", Arrays.asList(".nes"), "#EF4444", "👾"),
    SEGA_MD("segaMD", "Mega Drive / Genesis", Arrays.asList(".md", ".gen", ".bin"), "#3B82F6", "⚡"),
    GBC("gbc", "Game Boy Color", Arrays.asList(".gbc"), "#10B981", "🎨"),
    GB("gb", "Game Boy", Arrays.asList(".gb"), "#F59E0B", "🟩");

    private final String core;
    private final String displayName;
    private final List<String> fileExtensions;
    private final String accentColor;
    private final String icon;

    RetroSystem(String core, String displayName, List<String> fileExtensions, String accentColor, String icon) {
        this.core = core;
        this.displayName = displayName;
        this.fileExtensions = fileExtensions;
        this.accentColor = accentColor;
        this.icon = icon;
    }

    public String getCore() {
        return core;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getFileExtensions() {
        return fileExtensions;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public String getIcon() {
        return icon;
    }

    public static Optional<RetroSystem> fromCore(String core) {
        if (core == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(s -> s.getCore().equalsIgnoreCase(core.trim()))
                .findFirst();
    }

    public static Optional<RetroSystem> detectByFileName(String fileName) {
        if (fileName == null) return Optional.empty();
        String lower = fileName.toLowerCase();
        for (RetroSystem system : values()) {
            for (String ext : system.getFileExtensions()) {
                if (lower.endsWith(ext)) {
                    return Optional.of(system);
                }
            }
        }
        return Optional.empty();
    }
}
