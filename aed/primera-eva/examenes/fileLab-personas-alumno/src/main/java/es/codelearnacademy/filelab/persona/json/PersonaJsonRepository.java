package es.codelearnacademy.filelab.persona.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import es.codelearnacademy.filelab.model.Persona;
import es.codelearnacademy.filelab.model.Producto;
import es.codelearnacademy.filelab.persona.IPersonaRepository;
import es.codelearnacademy.filelab.repository.AbstractFileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PersonaJsonRepository extends AbstractFileRepository<Persona, String> implements IPersonaRepository {
    private final Path path;
    private final ObjectMapper mapper;

    public PersonaJsonRepository(Path path) {
        this(path, new ObjectMapper());
    }

    public PersonaJsonRepository(Path path, ObjectMapper mapper) {
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
        CollectionType listType = mapper.getTypeFactory()
                .constructCollectionType(List.class, Persona.class);
        return mapper.readValue(path.toFile(), listType);
    }

    @Override
    protected void writeAll(List<Persona> personas) throws IOException {
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), personas);
    }
}
