package es.codelearnacademy.filelab.io;

import java.io.File;
import java.nio.file.Path;

public class FileService {

    public boolean existe(File file) {
        if (file == null) {
            throw new NullPointerException("El file no puede ser null");
        }
        return file.exists();
    }

    public boolean esArchivo(File file) {
        if (file == null) {
            throw new NullPointerException("El file no puede ser null");
        }
        return file.isFile();
    }

    public boolean esDirectorio(File file) {
        if (file == null) {
            throw new NullPointerException("El file no puede ser null");
        }
        return file.isDirectory();
    }

    public String nombre(File file) {
        if (file == null) {
            throw new NullPointerException("El file no puede ser null");
        }
        return file.getName();
    }

    public File padre(File file) {
        if (file == null) {
            throw new NullPointerException("El file no puede ser null");
        }
        return file.getParentFile();
    }

    public Path convertirAPath(File file) {
        if (file == null) {
            throw new NullPointerException("El file no puede ser null");
        }
        return file.toPath();
    }

    public File convertirAFile(Path path) {
        if (path == null) {
            throw new NullPointerException("El path no puede ser null");
        }
        return path.toFile();
    }
}
