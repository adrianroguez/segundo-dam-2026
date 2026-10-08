package es.codelearnacademy.filelab.csv;

import es.codelearnacademy.filelab.model.Vehiculo;
import es.codelearnacademy.filelab.repository.AbstractFileRepository;
import es.codelearnacademy.filelab.repository.IVehiculoRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VehiculoCsvRepository
        extends AbstractFileRepository<Vehiculo, String>
        implements IVehiculoRepository {

    private final Path path;

    public VehiculoCsvRepository(Path path) {
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
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreSurroundingSpaces(true)
                    .build();
            CSVParser parser = format.parse(reader);
            List<Vehiculo> vehiculos = new ArrayList<>();
            for (CSVRecord record : parser) {
                String matricula = record.get("matricula");
                String marca = record.get("marca");
                String modelo = record.get("modelo");
                int anio = Integer.parseInt(record.get("anio"));
                vehiculos.add(new Vehiculo(matricula, marca, modelo, anio));
            }
            return vehiculos;
        }
    }

    @Override
    protected void writeAll(List<Vehiculo> vehiculos) throws IOException {
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectory(path.getParent());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader("matricula", "marca", "modelo", "anio").build())) {
            for (Vehiculo v : vehiculos) {
                printer.printRecord(v.matricula(), v.marca(), v.modelo(), v.anio());
            }
            printer.flush();
        }
    }
}
