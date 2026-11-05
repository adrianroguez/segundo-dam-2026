package es.ejemplo.biblioteca.modelo;

public final class Socio {
    private Integer id;
    private String nombre;
    private String email;
    private String fechaAlta;

    public Socio(Integer id, String nombre, String email, String fechaAlta) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.fechaAlta = fechaAlta;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(String fechaAlta) {
        this.fechaAlta = fechaAlta;
    }
}
