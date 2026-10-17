package com.retro.jogos.dto;

import java.time.LocalDateTime;

public record GameResponseDTO(
        Long id,
        String title,
        String systemCore,
        String systemName,
        Integer releaseYear,
        String genre,
        String description,
        String romFileName,
        Long romSizeBytes,
        String coverUrl,
        boolean hasSaveState,
        LocalDateTime createdAt,
        LocalDateTime lastPlayedAt
) {}
