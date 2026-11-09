package es.ejemplo.biblioteca.modelo;

public class Socio {
    private Integer idSocio;
    private String nombre;
    private String email;
    private String telefono;
    private String fechaAlta;
    private Boolean activo;

    public Socio() {
    }

    public Socio(Integer idSocio, String nombre, String email, String telefono, String fechaAlta, Boolean activo) {
        this.idSocio = idSocio;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.fechaAlta = fechaAlta;
        this.activo = activo;
    }

    public Integer getIdSocio() {
        return idSocio;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getFechaAlta() {
        return fechaAlta;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setIdSocio(Integer valor) {
        this.idSocio = valor;
    }

    public void setNombre(String valor) {
        this.nombre = valor;
    }

    public void setEmail(String valor) {
        this.email = valor;
    }

    public void setTelefono(String valor) {
        this.telefono = valor;
    }

    public void setFechaAlta(String valor) {
        this.fechaAlta = valor;
    }

    public void setActivo(Boolean valor) {
        this.activo = valor;
    }
}
