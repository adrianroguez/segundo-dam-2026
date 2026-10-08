package es.codelearnacademy.filelab.csv;

import es.codelearnacademy.filelab.model.Producto;
import es.codelearnacademy.filelab.repository.AbstractFileRepository;
import es.codelearnacademy.filelab.repository.IProductoRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProductoCsvRepository
        extends AbstractFileRepository<Producto, Long>
        implements IProductoRepository {

    private final Path path;

    public ProductoCsvRepository(Path path) {
        this.path = path;
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
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreSurroundingSpaces(true)
                    .build();
            CSVParser parser = format.parse(reader);
            List<Producto> productos = new ArrayList<>();
            for (CSVRecord record : parser) {
                long id = Long.parseLong(record.get("id"));
                String nombre = record.get("nombre");
                double precio = Double.parseDouble(record.get("precio"));
                int stock = Integer.parseInt(record.get("stock"));
                productos.add(new Producto(id, nombre, precio, stock));
            }
            return productos;
        }
    }

    @Override
    protected void writeAll(List<Producto> productos) throws IOException {
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("id", "nombre", "precio", "stock")
                     .build())) {
            for (Producto p : productos) {
                printer.printRecord(p.id(), p.nombre(), p.precio(), p.stock());
            }
            printer.flush();
        }
    }
}
