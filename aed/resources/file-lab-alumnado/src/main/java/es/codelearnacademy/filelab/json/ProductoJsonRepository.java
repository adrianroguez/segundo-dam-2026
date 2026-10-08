package es.codelearnacademy.filelab.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import es.codelearnacademy.filelab.model.Producto;
import es.codelearnacademy.filelab.repository.AbstractFileRepository;
import es.codelearnacademy.filelab.repository.IProductoRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ProductoJsonRepository
        extends AbstractFileRepository<Producto, Long>
        implements IProductoRepository {

    private final Path path;
    private final ObjectMapper mapper;

    public ProductoJsonRepository(Path path) {
        this(path, new ObjectMapper());
    }

    public ProductoJsonRepository(Path path, ObjectMapper mapper) {
        this.path = path;
        this.mapper = mapper;
    }

    @Override
    protected Long getId(Producto producto) {
        return producto.id();
    }

    @Override
    protected List<Producto> readAll() throws IOException {
        if (!Files.exists(path)) {
            return List.of();
        }
        CollectionType listType = mapper.getTypeFactory()
                .constructCollectionType(List.class, Producto.class);
        return mapper.readValue(path.toFile(), listType);
    }

    @Override
    protected void writeAll(List<Producto> productos) throws IOException {
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), productos);
    }
}
