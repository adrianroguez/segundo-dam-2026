package es.ejemplo.biblioteca.modelo;

public final class PrestamosPorSocio {
    private Integer idSocio;
    private String nombre;
    private Long totalPrestamos;

    public PrestamosPorSocio(Integer idSocio, String nombre, Long totalPrestamos) {
        this.idSocio = idSocio;
        this.nombre = nombre;
        this.totalPrestamos = totalPrestamos;
    }

    public Integer getIdSocio() {
        return idSocio;
    }

    public void setIdSocio(Integer idSocio) {
        this.idSocio = idSocio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getTotalPrestamos() {
        return totalPrestamos;
    }

    public void setTotalPrestamos(Long totalPrestamos) {
        this.totalPrestamos = totalPrestamos;
    }
}
