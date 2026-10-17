package com.retro.jogos.controller;

import com.retro.jogos.dto.SystemInfoDTO;
import com.retro.jogos.service.GameStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/systems")
public class SystemController {

    private final GameStorageService storageService;

    public SystemController(GameStorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<List<SystemInfoDTO>> getSystems() {
        return ResponseEntity.ok(storageService.getSystemsOverview());
    }
}
