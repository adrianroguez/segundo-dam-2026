package es.codelearnacademy.filelab.persona.csv;

import es.codelearnacademy.filelab.model.Persona;
import es.codelearnacademy.filelab.model.Producto;
import es.codelearnacademy.filelab.persona.IPersonaRepository;
import es.codelearnacademy.filelab.repository.AbstractFileRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
import java.util.*;

public class PersonaCsvRepository extends AbstractFileRepository<Persona, String> implements IPersonaRepository {
    private final Path path;

    public PersonaCsvRepository(Path path) {
        this.path = path;
    }

    @Override
    protected String getId(Persona p) {
        return p.dni();
    }

    @Override
    protected List<Persona> readAll() {
        if (!Files.exists(path)) {
            return List.of();
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreSurroundingSpaces(true)
                    .build();
            CSVParser parser = format.parse(reader);
            List<Persona> personas = new ArrayList<>();
            for (CSVRecord record : parser) {
                String dni = record.get("dni");
                String nombre = record.get("nombre");
                String email = record.get("email");
                int edad = Integer.parseInt(record.get("edad"));
                boolean activo = Boolean.parseBoolean(record.get("activo"));
                personas.add(new Persona(dni, nombre, email, edad, activo));
            }
            return personas;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void writeAll(List<Persona> personas) throws IOException {
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("dni", "nombre", "email", "edad", "activo")
                     .build())) {
            for (Persona p : personas) {
                printer.printRecord(p.dni(), p.nombre(), p.email(), p.edad(), p.activo());
            }
            printer.flush();
        }
    }
}
