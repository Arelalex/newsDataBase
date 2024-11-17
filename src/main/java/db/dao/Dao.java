package db.dao;

import java.util.List;
import java.util.Optional;

public interface Dao<K, E> {

    boolean delete(K id);

    E save(E entity);

    void update(E entity);

    Optional<E> findById(int id);

    List<E> findAll();
}
