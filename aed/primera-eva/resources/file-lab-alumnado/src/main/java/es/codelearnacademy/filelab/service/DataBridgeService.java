package es.codelearnacademy.filelab.service;

import es.codelearnacademy.filelab.model.Producto;
import es.codelearnacademy.filelab.repository.IProductoRepository;

import java.nio.file.Path;
import java.util.List;

public class DataBridgeService {

    private final RepositoryFactory repositoryFactory;

    public DataBridgeService(RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    public int convert(FileFormat origenFormato, Path origen,
                       FileFormat destinoFormato, Path destino) {
        IProductoRepository repositorioOrigen = repositoryFactory.create(origenFormato, origen);
        List<Producto> productos = repositorioOrigen.findAll();
        IProductoRepository repositorioDestino = repositoryFactory.create(destinoFormato, destino);
        int convertidos = 0;
        for (Producto producto : productos) {
            if (repositorioDestino.create(producto)) {
                convertidos++;
            }
        }
        return convertidos;
    }
}
