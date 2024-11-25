package db.dao.impl;

import db.dao.StatusDao;
import db.dto.StatusFilter;
import db.entity.StatusEntity;
import db.exception.*;
import db.util.ConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class StatusDaoImpl implements StatusDao<Integer, StatusEntity> {

    private static final String STATUS_ID = "id";
    private static final String STATUS_STATUS = "status";

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

    private static StatusDaoImpl instance;
    private final NewsDaoImpl newsDaoImpl = NewsDaoImpl.getInstance();
    private final PortalUserDaoImpl portalUserDaoImpl = PortalUserDaoImpl.getInstance();
    private final CategoryDaoImpl categoryDao = CategoryDaoImpl.getInstance();
    //   private final StatusDao statusDao = StatusDao.getInstance();

    private StatusDaoImpl() {
    }

    public static synchronized StatusDaoImpl getInstance() {
        if (instance == null) {
            instance = new StatusDaoImpl();
        }
        return instance;
    }

    @Override
    public boolean delete(Integer id) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(DELETE_SQL)) {
            preparedStatement.setLong(1, id);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException throwables) {
            throw new DaoExceptionDelete("Error deleting values from table",throwables);
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
            throw new DaoExceptionInsert("Error inserting values into table", throwables);
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
            throw new DaoExceptionUpdate("Error updating values in table", throwables);
        }
    }

    @Override
    public Optional<StatusEntity> findById(int id) {
        try (var connection = ConnectionManager.get()) {
            return findById(id, connection);
        } catch (SQLException throwables) {
            throw new DaoExceptionFindById("Error searching values by ID in table", throwables);
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
            throw new DaoExceptionFindById("Error searching values by ID in table", throwables);
        }
    }

    public List<StatusEntity> findAllByFilter(StatusFilter filter) {
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
            throw new DaoExceptionFindById("Error searching values by ID in table", throwables);
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
            throw new DaoExceptionFindById("Error searching values by ID in table", throwables);
        }
    }

    private StatusEntity buildStatus(ResultSet resultSet) throws SQLException {
        return new StatusEntity(
                resultSet.getInt(STATUS_ID),
                resultSet.getString(STATUS_STATUS)
        );
    }
}
