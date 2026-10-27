package es.codelearnacademy.filelab.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import es.codelearnacademy.filelab.model.Vehiculo;
import es.codelearnacademy.filelab.repository.AbstractFileRepository;
import es.codelearnacademy.filelab.repository.IVehiculoRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class VehiculoJsonRepository
        extends AbstractFileRepository<Vehiculo, String>
        implements IVehiculoRepository {

    private final Path path;
    private final ObjectMapper mapper = new ObjectMapper();

    public VehiculoJsonRepository(Path path) {
        this.path = path;
    }

    @Override
    protected String getId(Vehiculo vehiculo) {
        return vehiculo.matricula();
    }

    @Override
    protected List<Vehiculo> readAll() throws IOException {
        if (!Files.exists(path)) {
            return List.of();
        }
        CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, Vehiculo.class);
        return mapper.readValue(path.toFile(), listType);
    }

    @Override
    protected void writeAll(List<Vehiculo> vehiculos) throws IOException {
        if (path.getParent() != null || !Files.exists(path.getParent())) {
            Files.createDirectory(path.getParent());
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), vehiculos);
    }
}
