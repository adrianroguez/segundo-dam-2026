package es.codelearnacademy.filelab.xml;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import es.codelearnacademy.filelab.model.Producto;
import es.codelearnacademy.filelab.repository.AbstractFileRepository;
import es.codelearnacademy.filelab.repository.IProductoRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ProductoXmlRepository
        extends AbstractFileRepository<Producto, Long>
        implements IProductoRepository {

    private final Path path;
    private final XmlMapper mapper;

    public ProductoXmlRepository(Path path) {
        this(path, new XmlMapper());
    }

    public ProductoXmlRepository(Path path, XmlMapper mapper) {
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
        DocumentoProductos doc = mapper.readValue(path.toFile(), DocumentoProductos.class);
        return doc.getProductos() != null ? doc.getProductos() : List.of();
    }

    @Override
    protected void writeAll(List<Producto> productos) throws IOException {
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        DocumentoProductos doc = new DocumentoProductos(productos);
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), doc);
    }
}
