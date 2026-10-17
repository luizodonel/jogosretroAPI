package com.retro.jogos.model;

import java.time.LocalDateTime;

public class Game {
    private Long id;
    private String title;
    private String systemCore;
    private String systemName;
    private Integer releaseYear;
    private String genre;
    private String description;
    private String romFileName;
    private Long romSizeBytes;
    private String coverFileName;
    private String customCoverUrl;
    private boolean hasSaveState;
    private LocalDateTime createdAt;
    private LocalDateTime lastPlayedAt;

    public Game() {
        this.createdAt = LocalDateTime.now();
    }

    public Game(Long id, String title, String systemCore, String systemName, Integer releaseYear,
                String genre, String description, String romFileName, Long romSizeBytes,
                String coverFileName, String customCoverUrl) {
        this.id = id;
        this.title = title;
        this.systemCore = systemCore;
        this.systemName = systemName;
        this.releaseYear = releaseYear;
        this.genre = genre;
        this.description = description;
        this.romFileName = romFileName;
        this.romSizeBytes = romSizeBytes;
        this.coverFileName = coverFileName;
        this.customCoverUrl = customCoverUrl;
        this.hasSaveState = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSystemCore() {
        return systemCore;
    }

    public void setSystemCore(String systemCore) {
        this.systemCore = systemCore;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRomFileName() {
        return romFileName;
    }

    public void setRomFileName(String romFileName) {
        this.romFileName = romFileName;
    }

    public Long getRomSizeBytes() {
        return romSizeBytes;
    }

    public void setRomSizeBytes(Long romSizeBytes) {
        this.romSizeBytes = romSizeBytes;
    }

    public String getCoverFileName() {
        return coverFileName;
    }

    public void setCoverFileName(String coverFileName) {
        this.coverFileName = coverFileName;
    }

    public String getCustomCoverUrl() {
        return customCoverUrl;
    }

    public void setCustomCoverUrl(String customCoverUrl) {
        this.customCoverUrl = customCoverUrl;
    }

    public boolean isHasSaveState() {
        return hasSaveState;
    }

    public void setHasSaveState(boolean hasSaveState) {
        this.hasSaveState = hasSaveState;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastPlayedAt() {
        return lastPlayedAt;
    }

    public void setLastPlayedAt(LocalDateTime lastPlayedAt) {
        this.lastPlayedAt = lastPlayedAt;
    }
}
