package db.dao.impl;

import db.dao.Dao;
import db.dto.StatusFilter;
import db.entity.StatusEntity;
import db.exception.DaoException;
import db.util.ConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class StatusDao implements Dao<Integer, StatusEntity> {

    private static final String DELETE_SQL = """
            DELETE FROM status
            WHERE id = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO status (status) 
            VALUES (?);
            """;

    private static final String UPDATE_SQL = """
            UPDATE status
            SET status = ?
            WHERE id = ?
            """;

    private static final String FIND_ALL_SQL = """
            SELECT id,
                status
            FROM status
            """;

    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            WHERE status.id = ?
            """;

    private static StatusDao INSTANCE = new StatusDao();
    private final NewsDao newsDao = NewsDao.getInstance();
    private final PortalUserDao portalUserDao = PortalUserDao.getInstance();
    private final CategoryDao categoryDao = CategoryDao.getInstance();
    //   private final StatusDao statusDao = StatusDao.getInstance();

    private StatusDao() {
    }

    public static StatusDao getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean delete(Integer id) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(DELETE_SQL)) {
            preparedStatement.setLong(1, id);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    @Override
    public StatusEntity save(StatusEntity statusEntity) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, statusEntity.getStatus());

            preparedStatement.executeUpdate();

            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                statusEntity.setId(generatedKeys.getInt("id"));
            }
            return statusEntity;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    @Override
    public void update(StatusEntity statusEntity) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, statusEntity.getStatus());

            preparedStatement.setLong(2, statusEntity.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    @Override
    public Optional<StatusEntity> findById(int id) {
        try (var connection = ConnectionManager.get()) {
            return findById(id, connection);
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    public Optional<StatusEntity> findById(int id, Connection connection) {
        try (var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setInt(1, id);

            var resultSet = preparedStatement.executeQuery();
            StatusEntity statusEntity = null;
            if (resultSet.next()) {
                statusEntity = buildStatus(resultSet);
            }
            return Optional.ofNullable(statusEntity);
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    public List<StatusEntity> findAll(StatusFilter filter) {
        List<Object> parameters = new ArrayList<>();
        List<String> whereSql = new ArrayList<>();
        if (filter.status() != null) {
            whereSql.add("status LIKE ?");
            parameters.add("%" + filter.status() + "%");
        }
        parameters.add(filter.limit());
        parameters.add(filter.offset());
        var where = whereSql.stream()
                .collect(joining(" AND ", " WHERE ", " LIMIT ? OFFSET ? "));

        var sql = FIND_ALL_SQL + where;

        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            System.out.println(preparedStatement);
            var resultSet = preparedStatement.executeQuery();
            List<StatusEntity> statusEntities = new ArrayList<>();
            while (resultSet.next()) {
                statusEntities.add(buildStatus(resultSet));
            }
            return statusEntities;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    @Override
    public List<StatusEntity> findAll() {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            var resultSet = preparedStatement.executeQuery();
            List<StatusEntity> statusEntities = new ArrayList<>();
            while (resultSet.next()) {
                statusEntities.add(buildStatus(resultSet));
            }
            return statusEntities;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    private StatusEntity buildStatus(ResultSet resultSet) throws SQLException {
        return new StatusEntity(
                resultSet.getInt("id"),
                resultSet.getString("status")
        );
    }
}
