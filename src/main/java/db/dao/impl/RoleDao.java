package db.dao.impl;

import db.dao.Dao;
import db.dto.RoleFilter;
import db.enam.Roles;
import db.entity.RoleEntity;
import db.exception.DaoException;
import db.util.ConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoleDao implements Dao<Integer, RoleEntity> {

    private static final String DELETE_SQL = """
            DELETE FROM role
            WHERE id = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO role (role) 
            VALUES (?);
            """;

    private static final String UPDATE_SQL = """
            UPDATE role
            SET role = ?
            WHERE id = ?
            """;

    private static final String FIND_ALL_SQL = """
              SELECT id, 
             role
             FROM role
            """;

    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            WHERE role.id = ?
            """;

    private static final String FIND_BY_ROLE_SQL = """
            SELECT id
            FROM role
            WHERE role.role = ?
            """;

    private static RoleDao INSTANCE = new RoleDao();
    private final PortalUserDao portalUserDao = PortalUserDao.getInstance();
    private final RoleDao roleDao = RoleDao.getInstance();

    private RoleDao() {
    }

    public static RoleDao getInstance() {
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
    public RoleEntity save(RoleEntity roleEntity) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, roleEntity.getRole().name());

            preparedStatement.executeUpdate();

            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                roleEntity.setId(generatedKeys.getInt("id"));
            }
            return roleEntity;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    @Override
    public void update(RoleEntity roleEntity) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, roleEntity.getRole().name());
            preparedStatement.setInt(2, roleEntity.getId());


            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    @Override
    public Optional<RoleEntity> findById(int id) {
        try (var connection = ConnectionManager.get()) {

            return findById(id, connection);
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    public Optional<RoleEntity> findById(int id, Connection connection) {
        try (var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setLong(1, id);

            var resultSet = preparedStatement.executeQuery();
            RoleEntity role = null;
            if (resultSet.next()) {
                role = buildRole(resultSet);
            }
            return Optional.ofNullable(role);
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    public Optional<RoleEntity> findByRole(Roles role, Connection connection) {
        try (var preparedStatement = connection.prepareStatement(FIND_BY_ROLE_SQL)) {
            preparedStatement.setString(1, role.name());

            var resultSet = preparedStatement.executeQuery();
            RoleEntity roleEmpty = null;
            if (resultSet.next()) {
                roleEmpty = buildRole(resultSet);
            }
            return Optional.ofNullable(roleEmpty);
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    public List<RoleEntity> findAll(RoleFilter filter) {
        List<Object> parameters = new ArrayList<>();
        List<String> whereSql = new ArrayList<>();
        if (filter.role() != null) {
            whereSql.add("role LIKE ?");
            parameters.add("%" + filter.role() + "%");
        }
        parameters.add(filter.limit());
        parameters.add(filter.offset());

        var where = whereSql.stream()
                .collect(Collectors.joining(" AND ", " WHERE ", " LIMIT ? OFFSET ? "));

        var sql = FIND_ALL_SQL + where;  // если в фильтре не будет параметров то нужно добавить пустую строку вместо where

        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(sql)) {

            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            System.out.println(preparedStatement);
            var resultSet = preparedStatement.executeQuery();
            List<RoleEntity> roleList = new ArrayList<>();
            while (resultSet.next()) {
                roleList.add(buildRole(resultSet));
            }
            return roleList;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    private RoleEntity buildRole(ResultSet resultSet) throws SQLException {
        return new RoleEntity(
                resultSet.getInt("id"),
                Roles.valueOf(resultSet.getString("role"))
        );
    }

    @Override
    public List<RoleEntity> findAll() {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            var resultSet = preparedStatement.executeQuery();
            List<RoleEntity> roles = new ArrayList<>();
            while (resultSet.next()) {
                roles.add(buildRole(resultSet));
            }
            return roles;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }
}
