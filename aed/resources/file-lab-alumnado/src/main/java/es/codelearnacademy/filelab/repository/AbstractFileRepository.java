package es.codelearnacademy.filelab.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractFileRepository<T, ID> implements IRepository<T, ID> {

    @Override
    public List<T> findAll() {
        try {
            return readAll();
        } catch (IOException e) {
            return List.of();
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        if (id == null) {
            return Optional.empty();
        }
        try {
            return readAll().stream().filter(t -> Objects.equals(getId(t), id)).findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean create(T entity) {
        if (entity == null) {
            return false;
        }
        try {
            List<T> entities = readAll();
            for (T entidadBuscada : entities) {
                if (getId(entidadBuscada).equals(getId(entity))) {
                    return false;
                }
            }
            entities.add(entity);
            writeAll(entities);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean update(T entity) {
        if (entity == null) {
            return false;
        }
        try {
            List<T> entities = readAll();
            for (int i = 0; i < entities.size(); i++) {
                if (getId(entities.get(i)).equals(getId(entity))) {
                    entities.set(i, entity);
                    writeAll(entities);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean delete(ID id) {
        if (id == null) {
            return false;
        }
        try {
            List<T> entities = readAll();
            for (int i = 0; i < entities.size(); i++) {
                if (getId(entities.get(i)).equals(id)) {
                    entities.remove(i);
                    writeAll(entities);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    protected abstract ID getId(T entity);

    protected abstract List<T> readAll() throws IOException;

    protected abstract void writeAll(List<T> entities) throws IOException;
}
