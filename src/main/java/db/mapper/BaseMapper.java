package db.mapper;

public interface BaseMapper<E, F> {
    E toEntity(F dto);

    F toDto(E entity);
}
