package es.codelearnacademy.filelab.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class PropertiesConfig {

    private final Path path;

    public PropertiesConfig(Path path) {
        this.path = path;
    }

    public Optional<String> get(String key) {
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
            return Optional.ofNullable(props.getProperty(key));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public String getOrDefault(String key, String defaultValue) {
        return get(key).orElse(defaultValue);
    }

    public Map<String, String> findAll() {
        if (!Files.exists(path)) {
            return Map.of();
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
            Map<String, String> result = new HashMap<>();
            for (String nombre : props.stringPropertyNames()) {
                result.put(nombre, props.getProperty(nombre));
            }
            return result;
        } catch (IOException e) {
            return Map.of();
        }
    }

    public boolean put(String key, String value) {
        Properties props = new Properties();
        try {
            if (Files.exists(path)) {
                try (InputStream in = Files.newInputStream(path)) {
                    props.load(in);
                }
            } else if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            props.setProperty(key, value);
            try (OutputStream out = Files.newOutputStream(path)) {
                props.store(out, null);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean remove(String key) {
        if (!Files.exists(path)) {
            return false;
        }
        Properties props = new Properties();
        try {
            try (InputStream in = Files.newInputStream(path)) {
                props.load(in);
            }
            props.remove(key);
            try (OutputStream out = Files.newOutputStream(path)) {
                props.store(out, null);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
