package es.ejemplo.biblioteca.modelo;

public class Autor {
    private Integer idAutor;
    private String nombre;
    private String nacionalidad;
    private Integer anioNacimiento;

    public Autor() {
    }

    public Autor(Integer idAutor, String nombre, String nacionalidad, Integer anioNacimiento) {
        this.idAutor = idAutor;
        this.nombre = nombre;
        this.nacionalidad = nacionalidad;
        this.anioNacimiento = anioNacimiento;
    }

    public Integer getIdAutor() {
        return idAutor;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public Integer getAnioNacimiento() {
        return anioNacimiento;
    }

    public void setIdAutor(Integer valor) {
        this.idAutor = valor;
    }

    public void setNombre(String valor) {
        this.nombre = valor;
    }

    public void setNacionalidad(String valor) {
        this.nacionalidad = valor;
    }

    public void setAnioNacimiento(Integer valor) {
        this.anioNacimiento = valor;
    }
}
