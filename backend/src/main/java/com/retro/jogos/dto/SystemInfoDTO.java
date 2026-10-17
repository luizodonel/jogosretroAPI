package com.retro.jogos.dto;

import java.util.List;

public record SystemInfoDTO(
        String core,
        String name,
        List<String> extensions,
        String color,
        String icon,
        long gameCount
) {}
