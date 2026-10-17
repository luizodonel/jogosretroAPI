package com.retro.jogos.controller;

import com.retro.jogos.dto.GameResponseDTO;
import com.retro.jogos.service.GameStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameStorageService storageService;

    public GameController(GameStorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<List<GameResponseDTO>> listGames(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String system
    ) {
        List<GameResponseDTO> games = storageService.getAllGames(search, system);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponseDTO> getGame(@PathVariable Long id) {
        return storageService.getGameDTOById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @RequestMapping(value = "/{id}/rom", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<Resource> streamRom(@PathVariable Long id, org.springframework.http.HttpMethod method) {
        try {
            Resource resource = storageService.getRomResource(id);
            if (resource == null || !resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            long contentLength = resource.contentLength();

            ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                    .contentLength(contentLength)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition, Content-Length, Accept-Ranges");

            if (org.springframework.http.HttpMethod.HEAD.equals(method)) {
                return builder.build();
            }

            return builder.body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/cover")
    public ResponseEntity<Resource> getCover(@PathVariable Long id) {
        try {
            Resource resource = storageService.getCoverResource(id);
            if (resource == null || !resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String filename = resource.getFilename();
            MediaType mediaType = MediaType.IMAGE_JPEG;
            if (filename != null) {
                if (filename.toLowerCase().endsWith(".png")) mediaType = MediaType.IMAGE_PNG;
                else if (filename.toLowerCase().endsWith(".webp")) mediaType = MediaType.parseMediaType("image/webp");
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadGame(
            @RequestParam(value = "rom") MultipartFile romFile,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "system", required = false) String system,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "cover", required = false) MultipartFile coverFile,
            @RequestParam(value = "coverUrl", required = false) String coverUrl
    ) {
        try {
            GameResponseDTO savedGame = storageService.saveGame(
                    title,
                    system,
                    year,
                    genre,
                    description,
                    romFile,
                    coverFile,
                    coverUrl
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(savedGame);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Falha ao gravar arquivo de ROM: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGame(@PathVariable Long id) {
        boolean removed = storageService.deleteGame(id);
        if (removed) {
            return ResponseEntity.ok(Map.of("message", "Jogo excluído com sucesso"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/{id}/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadSaveState(
            @PathVariable Long id,
            @RequestParam("saveFile") MultipartFile saveFile
    ) {
        try {
            storageService.storeSaveState(id, saveFile);
            return ResponseEntity.ok(Map.of("message", "Save state persistido com sucesso"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Falha ao gravar save: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/save")
    public ResponseEntity<Resource> downloadSaveState(@PathVariable Long id) {
        try {
            Resource resource = storageService.getSaveStateResource(id);
            if (resource == null || !resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"game_" + id + ".state\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
