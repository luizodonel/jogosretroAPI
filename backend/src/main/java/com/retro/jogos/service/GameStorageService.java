package com.retro.jogos.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.retro.jogos.dto.GameResponseDTO;
import com.retro.jogos.dto.SystemInfoDTO;
import com.retro.jogos.model.Game;
import com.retro.jogos.model.RetroSystem;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class GameStorageService {

    private static final Logger log = LoggerFactory.getLogger(GameStorageService.class);

    @Value("${retro.storage.path:storage}")
    private String storageBasePath;

    private Path rootStorage;
    private Path romsDir;
    private Path coversDir;
    private Path savesDir;
    private Path catalogFile;

    private final Map<Long, Game> gamesMap = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);
    private final ObjectMapper objectMapper;

    public GameStorageService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    public void init() {
        try {
            this.rootStorage = Paths.get(storageBasePath).toAbsolutePath().normalize();
            this.romsDir = rootStorage.resolve("roms");
            this.coversDir = rootStorage.resolve("covers");
            this.savesDir = rootStorage.resolve("saves");
            this.catalogFile = rootStorage.resolve("catalog.json");

            Files.createDirectories(romsDir);
            Files.createDirectories(coversDir);
            Files.createDirectories(savesDir);

            loadCatalog();

            if (gamesMap.isEmpty()) {
                seedInitialGames();
            }

            log.info("GameStorageService initialized with {} games in catalog.", gamesMap.size());
        } catch (IOException e) {
            log.error("Failed to initialize storage directories: {}", e.getMessage(), e);
        }
    }

    private void loadCatalog() {
        if (!Files.exists(catalogFile)) {
            return;
        }

        try {
            List<Game> savedGames = objectMapper.readValue(catalogFile.toFile(), new TypeReference<List<Game>>() {});
            long maxId = 0;
            for (Game g : savedGames) {
                // Verify if ROM file still exists
                Path romPath = romsDir.resolve(g.getRomFileName());
                if (Files.exists(romPath)) {
                    try {
                        g.setRomSizeBytes(Files.size(romPath));
                    } catch (IOException ignored) {}
                }

                Path savePath = savesDir.resolve(g.getId() + ".state");
                g.setHasSaveState(Files.exists(savePath));

                gamesMap.put(g.getId(), g);
                if (g.getId() != null && g.getId() > maxId) {
                    maxId = g.getId();
                }
            }
            idSequence.set(maxId + 1);
        } catch (Exception e) {
            log.warn("Could not load catalog.json: {}. Starting fresh.", e.getMessage());
        }
    }

    private synchronized void saveCatalog() {
        try {
            List<Game> list = new ArrayList<>(gamesMap.values());
            list.sort(Comparator.comparing(Game::getTitle, String.CASE_INSENSITIVE_ORDER));
            objectMapper.writeValue(catalogFile.toFile(), list);
        } catch (IOException e) {
            log.error("Failed to persist catalog.json: {}", e.getMessage(), e);
        }
    }

    private void seedInitialGames() {
        // Sample homebrew / public domain demonstrations
        Game g1 = new Game(
                idSequence.getAndIncrement(),
                "Super Mario World (Demo / Homebrew)",
                "snes",
                "Super Nintendo",
                1990,
                "Platformer",
                "Clássico de plataforma da era 16-bit com física precisa e design atemporal.",
                "smw-demo.sfc",
                524288L,
                null,
                "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=600&auto=format&fit=crop&q=80"
        );
        createDummyRomIfNotExists(g1.getRomFileName(), 512 * 1024);

        Game g2 = new Game(
                idSequence.getAndIncrement(),
                "Pokemon Emerald (Demo / GBA)",
                "gba",
                "Game Boy Advance",
                2004,
                "RPG / Adventure",
                "Aventura lendária na região de Hoenn com batalhas épicas e exploração.",
                "emerald-demo.gba",
                16777216L,
                null,
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80"
        );
        createDummyRomIfNotExists(g2.getRomFileName(), 1024 * 1024);

        Game g3 = new Game(
                idSequence.getAndIncrement(),
                "Sonic The Hedgehog (Sega 16-Bit)",
                "segaMD",
                "Mega Drive / Genesis",
                1991,
                "Action / Speed",
                "O clássico velocista azul que marcou a rivalidade histórica dos anos 90.",
                "sonic-demo.md",
                1048576L,
                null,
                "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=600&auto=format&fit=crop&q=80"
        );
        createDummyRomIfNotExists(g3.getRomFileName(), 512 * 1024);

        Game g4 = new Game(
                idSequence.getAndIncrement(),
                "The Legend of Zelda (NES)",
                "nes",
                "NES (Nintendinho)",
                1986,
                "Action RPG",
                "A jornada pioneira de Link pelo reino de Hyrule em busca dos fragmentos da Triforce.",
                "zelda-demo.nes",
                262144L,
                null,
                "https://images.unsplash.com/photo-1579373903781-fd5c0c30c4cd?w=600&auto=format&fit=crop&q=80"
        );
        createDummyRomIfNotExists(g4.getRomFileName(), 256 * 1024);

        gamesMap.put(g1.getId(), g1);
        gamesMap.put(g2.getId(), g2);
        gamesMap.put(g3.getId(), g3);
        gamesMap.put(g4.getId(), g4);
        saveCatalog();
    }

    private void createDummyRomIfNotExists(String fileName, int sizeBytes) {
        Path filePath = romsDir.resolve(fileName);
        if (!Files.exists(filePath)) {
            try {
                byte[] data = new byte[Math.min(sizeBytes, 64 * 1024)];
                Arrays.fill(data, (byte) 0xEA); // NOP instructions in many architectures
                Files.write(filePath, data, StandardOpenOption.CREATE);
            } catch (IOException e) {
                log.warn("Could not create demo ROM file {}: {}", fileName, e.getMessage());
            }
        }
    }

    public List<GameResponseDTO> getAllGames(String search, String system) {
        return gamesMap.values().stream()
                .filter(g -> {
                    if (system != null && !system.isBlank() && !system.equalsIgnoreCase("all")) {
                        if (!g.getSystemCore().equalsIgnoreCase(system)) {
                            return false;
                        }
                    }
                    if (search != null && !search.isBlank()) {
                        String query = search.toLowerCase().trim();
                        boolean matchesTitle = g.getTitle() != null && g.getTitle().toLowerCase().contains(query);
                        boolean matchesGenre = g.getGenre() != null && g.getGenre().toLowerCase().contains(query);
                        boolean matchesSystem = g.getSystemName() != null && g.getSystemName().toLowerCase().contains(query);
                        return matchesTitle || matchesGenre || matchesSystem;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(Game::getTitle, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Optional<Game> getGameById(Long id) {
        return Optional.ofNullable(gamesMap.get(id));
    }

    public Optional<GameResponseDTO> getGameDTOById(Long id) {
        return getGameById(id).map(this::toResponseDTO);
    }

    public synchronized GameResponseDTO saveGame(
            String title,
            String systemCore,
            Integer year,
            String genre,
            String description,
            MultipartFile romFile,
            MultipartFile coverFile,
            String customCoverUrl
    ) throws IOException {

        if (romFile == null || romFile.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de ROM é obrigatório.");
        }

        String originalRomName = StringUtilsClean(romFile.getOriginalFilename());
        RetroSystem retroSystem = RetroSystem.fromCore(systemCore)
                .or(() -> RetroSystem.detectByFileName(originalRomName))
                .orElse(RetroSystem.SNES);

        Long newId = idSequence.getAndIncrement();
        String safeRomFileName = "game_" + newId + "_" + originalRomName;
        Path targetRomPath = romsDir.resolve(safeRomFileName);
        Files.copy(romFile.getInputStream(), targetRomPath, StandardCopyOption.REPLACE_EXISTING);

        String savedCoverFileName = null;
        if (coverFile != null && !coverFile.isEmpty()) {
            String originalCoverName = StringUtilsClean(coverFile.getOriginalFilename());
            savedCoverFileName = "cover_" + newId + "_" + originalCoverName;
            Path targetCoverPath = coversDir.resolve(savedCoverFileName);
            Files.copy(coverFile.getInputStream(), targetCoverPath, StandardCopyOption.REPLACE_EXISTING);
        }

        Game game = new Game();
        game.setId(newId);
        game.setTitle((title != null && !title.isBlank()) ? title.trim() : originalRomName.replaceFirst("[.][^.]+$", ""));
        game.setSystemCore(retroSystem.getCore());
        game.setSystemName(retroSystem.getDisplayName());
        game.setReleaseYear(year != null ? year : 1995);
        game.setGenre((genre != null && !genre.isBlank()) ? genre.trim() : "Retrô");
        game.setDescription((description != null && !description.isBlank()) ? description.trim() : "Jogo adicionado pelo usuário.");
        game.setRomFileName(safeRomFileName);
        game.setRomSizeBytes(Files.size(targetRomPath));
        game.setCoverFileName(savedCoverFileName);
        game.setCustomCoverUrl(customCoverUrl);
        game.setHasSaveState(false);
        game.setCreatedAt(LocalDateTime.now());

        gamesMap.put(newId, game);
        saveCatalog();

        return toResponseDTO(game);
    }

    public synchronized boolean deleteGame(Long id) {
        Game game = gamesMap.remove(id);
        if (game == null) {
            return false;
        }

        try {
            if (game.getRomFileName() != null) {
                Files.deleteIfExists(romsDir.resolve(game.getRomFileName()));
            }
            if (game.getCoverFileName() != null) {
                Files.deleteIfExists(coversDir.resolve(game.getCoverFileName()));
            }
            Files.deleteIfExists(savesDir.resolve(id + ".state"));
            Files.deleteIfExists(savesDir.resolve(id + ".sav"));
        } catch (IOException e) {
            log.warn("Error cleaning up files for game {}: {}", id, e.getMessage());
        }

        saveCatalog();
        return true;
    }

    public Resource getRomResource(Long id) throws MalformedURLException {
        Game game = gamesMap.get(id);
        if (game == null || game.getRomFileName() == null) {
            return null;
        }

        Path romPath = romsDir.resolve(game.getRomFileName()).normalize();
        if (!Files.exists(romPath)) {
            return null;
        }

        game.setLastPlayedAt(LocalDateTime.now());
        return new UrlResource(romPath.toUri());
    }

    public Resource getCoverResource(Long id) throws MalformedURLException {
        Game game = gamesMap.get(id);
        if (game == null || game.getCoverFileName() == null) {
            return null;
        }

        Path coverPath = coversDir.resolve(game.getCoverFileName()).normalize();
        if (!Files.exists(coverPath)) {
            return null;
        }

        return new UrlResource(coverPath.toUri());
    }

    public void storeSaveState(Long id, MultipartFile saveFile) throws IOException {
        Game game = gamesMap.get(id);
        if (game == null) {
            throw new IllegalArgumentException("Jogo não encontrado: " + id);
        }

        Path savePath = savesDir.resolve(id + ".state");
        try (InputStream in = saveFile.getInputStream()) {
            Files.copy(in, savePath, StandardCopyOption.REPLACE_EXISTING);
        }

        game.setHasSaveState(true);
        saveCatalog();
    }

    public Resource getSaveStateResource(Long id) throws MalformedURLException {
        Path savePath = savesDir.resolve(id + ".state");
        if (!Files.exists(savePath)) {
            return null;
        }
        return new UrlResource(savePath.toUri());
    }

    public List<SystemInfoDTO> getSystemsOverview() {
        return Arrays.stream(RetroSystem.values())
                .map(sys -> {
                    long count = gamesMap.values().stream()
                            .filter(g -> g.getSystemCore().equalsIgnoreCase(sys.getCore()))
                            .count();
                    return new SystemInfoDTO(
                            sys.getCore(),
                            sys.getDisplayName(),
                            sys.getFileExtensions(),
                            sys.getAccentColor(),
                            sys.getIcon(),
                            count
                    );
                })
                .collect(Collectors.toList());
    }

    private GameResponseDTO toResponseDTO(Game game) {
        String coverUrl = game.getCustomCoverUrl();
        if (game.getCoverFileName() != null) {
            coverUrl = "http://localhost:8080/api/games/" + game.getId() + "/cover";
        }
        if (coverUrl == null || coverUrl.isBlank()) {
            coverUrl = getDefaultCoverForSystem(game.getSystemCore());
        }

        return new GameResponseDTO(
                game.getId(),
                game.getTitle(),
                game.getSystemCore(),
                game.getSystemName(),
                game.getReleaseYear(),
                game.getGenre(),
                game.getDescription(),
                game.getRomFileName(),
                game.getRomSizeBytes(),
                coverUrl,
                game.isHasSaveState(),
                game.getCreatedAt(),
                game.getLastPlayedAt()
        );
    }

    private String getDefaultCoverForSystem(String core) {
        if ("gba".equalsIgnoreCase(core)) {
            return "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=600&auto=format&fit=crop&q=80";
        } else if ("segaMD".equalsIgnoreCase(core)) {
            return "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=600&auto=format&fit=crop&q=80";
        } else if ("nes".equalsIgnoreCase(core)) {
            return "https://images.unsplash.com/photo-1579373903781-fd5c0c30c4cd?w=600&auto=format&fit=crop&q=80";
        } else {
            return "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?w=600&auto=format&fit=crop&q=80";
        }
    }

    private String StringUtilsClean(String name) {
        if (name == null) return "rom_" + System.currentTimeMillis() + ".bin";
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
