package com.practica2.practica2pom;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AdminController {

    private static final String SEED =
            "INSERT OR IGNORE INTO categorias(nombre) VALUES ('Gatos'),('Perros'),('Pájaros')";

    private final JdbcTemplate db;

    public AdminController(JdbcTemplate db) {
        this.db = db;
    }

    @GetMapping("/health")
    public ApiResponse health() {
        return ApiResponse.ok(List.of(Map.of("HOLA", 2)));
    }

    @PostMapping("/backup")
    public ApiResponse backup() throws IOException {
        Path dir = Files.createDirectories(Path.of("backups").toAbsolutePath());
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String file = dir.resolve("backup_" + stamp + ".db").toString();
        db.execute("VACUUM INTO '" + file.replace("'", "''") + "'");
        return ApiResponse.ok(List.of(Map.of("file", file)));
    }

    @DeleteMapping("/database")
    @Transactional
    public ApiResponse clear() {
        db.execute("PRAGMA defer_foreign_keys = ON");
        List<String> tables = db.queryForList(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'",
                String.class);
        for (String t : tables) {
            db.execute("DELETE FROM \"" + t + "\"");
        }
        db.execute("DELETE FROM sqlite_sequence");
        db.execute(SEED);
        return ApiResponse.ok(List.of());
    }
}