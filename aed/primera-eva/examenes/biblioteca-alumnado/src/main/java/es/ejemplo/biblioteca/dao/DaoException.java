package es.ejemplo.biblioteca.dao;


public class DaoException extends RuntimeException {
 public DaoException(String mensaje, Throwable causa) { super(mensaje,causa); }
}
