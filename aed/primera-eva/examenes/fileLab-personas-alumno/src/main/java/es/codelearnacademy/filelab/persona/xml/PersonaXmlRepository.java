package es.codelearnacademy.filelab.persona.xml;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import es.codelearnacademy.filelab.model.Persona;
import es.codelearnacademy.filelab.persona.IPersonaRepository;
import es.codelearnacademy.filelab.repository.AbstractFileRepository;

import java.nio.file.*;
import java.io.*;
import java.util.*;

public class PersonaXmlRepository extends AbstractFileRepository<Persona, String> implements IPersonaRepository {
    private final Path path;
    private final XmlMapper mapper;

    public PersonaXmlRepository(Path path) {
        this(path, new XmlMapper());
    }

    public PersonaXmlRepository(Path path, XmlMapper mapper) {
        this.path = path;
        this.mapper = mapper;
    }

    @Override
    protected String getId(Persona p) {
        return p.dni();
    }

    @Override
    protected List<Persona> readAll() throws IOException {
        if (!Files.exists(path)) {
            return List.of();
        }
        DocumentoPersonas doc = mapper.readValue(path.toFile(), DocumentoPersonas.class);
        return doc.getPersonas() != null ? doc.getPersonas() : List.of();
    }

    @Override
    protected void writeAll(List<Persona> personas) throws IOException {
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        DocumentoPersonas doc = new DocumentoPersonas(personas);
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), doc);
    }
}
