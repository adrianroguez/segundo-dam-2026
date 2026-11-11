package es.ejemplo.cinefest.dao;

import es.ejemplo.cinefest.modelo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DaoIntegrationTest {

    private String jdbcUrl;

    @BeforeEach
    void setUp() throws IOException {
        File tempDb = File.createTempFile("cinefest_test_", ".sqlite3");
        tempDb.deleteOnExit();
        Files.copy(new File("datos/cinefest.sqlite3").toPath(), tempDb.toPath(), StandardCopyOption.REPLACE_EXISTING);
        jdbcUrl = "jdbc:sqlite:" + tempDb.getAbsolutePath();
    }

    @Test
    void testSedeDao() {
        SedeDao dao = new SedeDao(jdbcUrl);
        List<Sede> sedes = dao.listar();
        assertEquals(3, sedes.size());

        Optional<Sede> sede1 = dao.buscarPorId(1);
        assertTrue(sede1.isPresent());
        assertEquals("Auditorio Atlántico", sede1.get().getNombre());

        Sede nueva = new Sede();
        nueva.setNombre("Nueva Sede");
        nueva.setMunicipio("Valencia");
        int nuevoId = dao.insertar(nueva);
        assertTrue(nuevoId > 0);

        nueva.setMunicipio("Alicante");
        assertEquals(1, dao.actualizar(nueva));

        assertEquals(1, dao.eliminar(nuevoId));

        List<Object[]> aforos = dao.aforoTotalPorSede();
        assertEquals(3, aforos.size());
    }

    @Test
    void testSalaDao() {
        SalaDao dao = new SalaDao(jdbcUrl);
        List<Sala> salas = dao.listar();
        assertEquals(4, salas.size());

        List<Object[]> salasConSede = dao.salasConSede();
        assertEquals(4, salasConSede.size());

        List<Sala> sinSesiones = dao.salasSinSesiones();
        assertEquals(0, sinSesiones.size());
    }

    @Test
    void testPeliculaDao() {
        PeliculaDao dao = new PeliculaDao(jdbcUrl);
        List<Pelicula> peliculas = dao.listar();
        assertEquals(9, peliculas.size());

        List<Pelicula> dramas = dao.buscarPorGenero("Drama");
        assertEquals(3, dramas.size());

        List<Pelicula> sinSesiones = dao.peliculasSinSesiones();
        assertEquals(1, sinSesiones.size());
        assertEquals("Islas Invisibles", sinSesiones.get(0).getTitulo());

        List<Object[]> mejorValoradas = dao.mejorValoradas(2);
        assertEquals(4, mejorValoradas.size());

        List<Object[]> masVendidas = dao.masVendidasQueMedia();
        assertEquals(3, masVendidas.size());
    }

    @Test
    void testEspectadorDao() {
        EspectadorDao dao = new EspectadorDao(jdbcUrl);
        List<Espectador> activos = dao.listar();
        assertEquals(7, activos.size());

        Optional<Espectador> porEmail = dao.buscarPorEmail("ana@example.org");
        assertTrue(porEmail.isPresent());

        List<Espectador> sinEntradas = dao.espectadoresSinEntradas();
        assertEquals(2, sinEntradas.size());

        List<Object[]> ranking = dao.rankingEspectadores();
        assertEquals(8, ranking.size());

        List<Object[]> variasSedes = dao.espectadoresVariasSedes(2);
        assertEquals(6, variasSedes.size());
    }

    @Test
    void testEntradaDaoYCancelacionC30() {
        EntradaDao dao = new EntradaDao(jdbcUrl);
        List<Entrada> entradas = dao.listar();
        assertEquals(22, entradas.size());

        List<Object[]> detalles = dao.entradasConDetalles();
        assertEquals(22, detalles.size());

        List<Object[]> porEspectador = dao.entradasPorEspectador(1);
        assertEquals(4, porEspectador.size());

        List<Object[]> ventasDia = dao.ventasPorDia();
        assertEquals(9, ventasDia.size());

        // Cancelación C30
        int cancel1 = dao.cancelarEntrada(1);
        assertEquals(1, cancel1);

        // Cancelar de nuevo debe fallar
        int cancelRepetida = dao.cancelarEntrada(1);
        assertEquals(0, cancelRepetida);
    }

    @Test
    void testSesionDaoYVentaC29() {
        SesionDao dao = new SesionDao(jdbcUrl);
        List<Sesion> sesiones = dao.listar();
        assertEquals(12, sesiones.size());

        List<Object[]> sinVentas = dao.sesionesSinVentas();
        assertEquals(2, sinVentas.size());

        List<Object[]> agotadas = dao.sesionesAgotadas();
        assertEquals(1, agotadas.size());

        List<Object[]> rankingRecaudacion = dao.rankingRecaudacion();
        assertEquals(3, rankingRecaudacion.size());

        List<Object[]> ingresosSede = dao.ingresosPorSede();
        assertEquals(3, ingresosSede.size());

        // Venta atómica C29: sesión 1 tiene 4/5 plazas. Espectador 8 (Hugo) compra la última plaza.
        int venta1 = dao.venderEntrada(1, 8, "2026-11-10");
        assertEquals(1, venta1);

        // Venta a otro espectador debe fallar por aforo completo
        int ventaLlena = dao.venderEntrada(1, 7, "2026-11-10");
        assertEquals(0, ventaLlena);

        // Venta al mismo espectador debe fallar por unicidad
        int ventaDuplicada = dao.venderEntrada(1, 8, "2026-11-10");
        assertEquals(0, ventaDuplicada);
    }

    @Test
    void testValoracionDao() {
        ValoracionDao dao = new ValoracionDao(jdbcUrl);
        List<Valoracion> valoraciones = dao.listar();
        assertEquals(12, valoraciones.size());

        List<Object[]> detalles = dao.valoracionesConDetalles();
        assertEquals(12, detalles.size());

        List<Object[]> sinVal = dao.peliculasSinValoraciones();
        assertEquals(2, sinVal.size());
    }
}
