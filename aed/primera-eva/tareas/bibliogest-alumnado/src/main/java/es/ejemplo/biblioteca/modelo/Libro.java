package es.ejemplo.biblioteca.modelo;

public class Libro {
    private Integer idLibro;
    private String titulo;
    private String isbn;
    private String genero;
    private Integer anioPublicacion;
    private Integer ejemplares;
    private Integer idAutor;

    public Libro() {
    }

    public Libro(Integer idLibro, String titulo, String isbn, String genero, Integer anioPublicacion, Integer ejemplares, Integer idAutor) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.isbn = isbn;
        this.genero = genero;
        this.anioPublicacion = anioPublicacion;
        this.ejemplares = ejemplares;
        this.idAutor = idAutor;
    }

    public Integer getIdLibro() {
        return idLibro;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getGenero() {
        return genero;
    }

    public Integer getAnioPublicacion() {
        return anioPublicacion;
    }

    public Integer getEjemplares() {
        return ejemplares;
    }

    public Integer getIdAutor() {
        return idAutor;
    }

    public void setIdLibro(Integer valor) {
        this.idLibro = valor;
    }

    public void setTitulo(String valor) {
        this.titulo = valor;
    }

    public void setIsbn(String valor) {
        this.isbn = valor;
    }

    public void setGenero(String valor) {
        this.genero = valor;
    }

    public void setAnioPublicacion(Integer valor) {
        this.anioPublicacion = valor;
    }

    public void setEjemplares(Integer valor) {
        this.ejemplares = valor;
    }

    public void setIdAutor(Integer valor) {
        this.idAutor = valor;
    }
}
