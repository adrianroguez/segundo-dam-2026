package es.ejemplo.gestion.dao;

import es.ejemplo.gestion.config.ConexionBD;
import es.ejemplo.gestion.exception.AplicationException;
import es.ejemplo.gestion.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ProductoDao {
    private  ConexionBD conexionBD;

    public ProductoDao(String url) {
        
    }


    public List<Producto> listar() {
        return null;
    }

    public Optional<Producto> buscarPorId(int id) {
        return null;
    }

    public boolean insertar(Producto producto) {
        return false;
    }

    public boolean actualizar(Producto producto) {
        return false;
    }

    public boolean eliminar(int id) {
        return false;
    }

    public List<Producto> buscarPorNombre(String texto) {
       return null;
    }

    public List<Producto> buscarPorPrecio(double minimo, double maximo) {
        return null;
    }

    public int contar() {
       return 0;
    }

    public double precioMedio() {
        return 0;
    }
}
