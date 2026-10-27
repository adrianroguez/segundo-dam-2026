package es.codelearnacademy.filelab.service;

public enum FileFormat {
    CSV,
    JSON,
    XML;

    public static FileFormat from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("El formato no puede ser null");
        }
        try {
            return FileFormat.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Formato no soportado: " + value, e);
        }
    }
}
