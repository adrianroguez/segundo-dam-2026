package es.codelearnacademy.filelab.io;

import java.nio.file.Path;

public class PathService {

    public Path crear(String primero, String... partes) {
        return Path.of(primero, partes);
    }

    public String nombre(Path path) {
        if (path == null) {
            throw new NullPointerException("El path no puede ser null");
        }
        return path.getFileName().toString();
    }

    public Path padre(Path path) {
        return path.getParent();
    }

    public Path absoluto(Path path) {
        return path.toAbsolutePath();
    }

    public Path normalizar(Path path) {
        return path.normalize();
    }

    public boolean esAbsoluto(Path path) {
        return path.isAbsolute();
    }

    public Path resolver(Path base, String otro) {
        return base.resolve(otro);
    }

    public Path relativizar(Path base, Path destino) {
        return base.relativize(destino);
    }

    public String extension(Path path) {
        if (path == null) {
            throw new NullPointerException("El path no puede ser null");
        }
        String fichero = nombre(path);
        int punto = fichero.lastIndexOf(".");
        if (punto <= 0 || punto == fichero.length() - 1) {
            return "";
        }
        return fichero.substring(punto + 1);
    }
}
