package es.codelearnacademy.filelab.service;

import es.codelearnacademy.filelab.config.PropertiesConfig;

import java.nio.file.Path;
import java.util.Optional;

public class ConfiguredDataBridge {

    private final PropertiesConfig config;
    private final DataBridgeService bridge;

    public ConfiguredDataBridge(PropertiesConfig config, DataBridgeService bridge) {
        this.config = config;
        this.bridge = bridge;
    }

    public int execute() {
        Optional<String> inFormat = config.get("input.format");
        Optional<String> inFile = config.get("input.file");
        Optional<String> outFormat = config.get("output.format");
        Optional<String> outFile = config.get("output.file");

        if (inFormat.isEmpty() || inFile.isEmpty() || outFormat.isEmpty() || outFile.isEmpty()) {
            return 0;
        }

        try {
            FileFormat origenFormato = FileFormat.from(inFormat.get());
            Path origenPath = Path.of(inFile.get());
            FileFormat destinoFormato = FileFormat.from(outFormat.get());
            Path destinoPath = Path.of(outFile.get());

            return bridge.convert(origenFormato, origenPath, destinoFormato, destinoPath);
        } catch (Exception e) {
            return 0;
        }
    }
}
