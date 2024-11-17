package db.dao.impl;

import db.dao.Dao;
import db.dto.PortalUserFilter;
import db.enam.Roles;
import db.entity.PortalUserEntity;
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

import static java.util.stream.Collectors.joining;

public class PortalUserDao implements Dao<Integer, PortalUserEntity> {

    private static final String DELETE_SQL = """
            DELETE FROM portal_user
            WHERE id = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO portal_user (first_name, last_name, nickname, email, password, image, role_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?);
            """;

    private static final String UPDATE_SQL = """
            UPDATE portal_user
            SET first_name = ?,
                last_name = ?,
                nickname = ?,
                email = ?,
                password = ?,
                image = ?,
                role_id = ?
            WHERE id = ?
            """;

    private static final String FIND_ALL_SQL = """
                        SELECT u.id,
                               u.first_name,
                               u.last_name,
                               u.nickname,
                               u.email,
                            u.password,
                            u.image,
                            u.role_id,            
                            r.role
                        FROM portal_user u
                        LEFT JOIN role r
                        ON u.role_id = r.id
            """;

    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            WHERE u.id = ?
            """;

    private static final PortalUserDao INSTANCE = new PortalUserDao();
    private final RoleDao roleDao = RoleDao.getInstance();

    private PortalUserDao() {
    }

    public static PortalUserDao getInstance() { // все сервисы которые будут работать с DAO будут вызыват его через гетИнстанс
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
    public PortalUserEntity save(PortalUserEntity portalUser) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, portalUser.getFirstName());
            preparedStatement.setString(2, portalUser.getLastName());
            preparedStatement.setString(3, portalUser.getNickname());
            preparedStatement.setString(4, portalUser.getEmail());
            preparedStatement.setString(5, portalUser.getPassword());
            preparedStatement.setString(6, portalUser.getImage());
            preparedStatement.setInt(7, portalUser.getRole().getId());

            preparedStatement.executeUpdate();

            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                portalUser.setId(generatedKeys.getInt("id"));
            }
            return portalUser;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    @Override
    public void update(PortalUserEntity portalUser) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, portalUser.getFirstName());
            preparedStatement.setString(2, portalUser.getLastName());
            preparedStatement.setString(3, portalUser.getNickname());
            preparedStatement.setString(4, portalUser.getEmail());
            preparedStatement.setString(5, portalUser.getPassword());
            preparedStatement.setString(6, portalUser.getImage());
            preparedStatement.setInt(7, portalUser.getRole().getId());

            preparedStatement.setInt(8, portalUser.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    @Override
    public Optional<PortalUserEntity> findById(int id) {
        try (var connection = ConnectionManager.get()) {
            return findById(id, connection);
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }


    public Optional<PortalUserEntity> findById(int id, Connection connection) {
        try (var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setInt(1, id);

            var resultSet = preparedStatement.executeQuery();
            PortalUserEntity portalUser = null;
            if (resultSet.next()) {
                portalUser = buildUser(resultSet);

            }
            return Optional.ofNullable(portalUser);
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }


    public List<PortalUserEntity> findAll(PortalUserFilter filter) {
        List<Object> parameters = new ArrayList<>();
        List<String> whereSql = new ArrayList<>();
        if (filter.firstName() != null) {
            whereSql.add("first_name LIKE ?");
            parameters.add("%" + filter.firstName() + "%");
        }
        if (filter.lastName() != null) {
            whereSql.add("last_name = ?");
            parameters.add("%" + filter.lastName() + "%");
        }
        if (filter.nickname() != null) {
            whereSql.add("nickname = ?");
            parameters.add("%" + filter.nickname() + "%");
        }
        if (filter.email() != null) {
            whereSql.add("email = ?");
            parameters.add("%" + filter.email() + "%");
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
            List<PortalUserEntity> portalUsers = new ArrayList<>();
            while (resultSet.next()) {
                portalUsers.add(buildUser(resultSet));
            }
            return portalUsers;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    @Override
    public List<PortalUserEntity> findAll() {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            var resultSet = preparedStatement.executeQuery();
            List<PortalUserEntity> tickets = new ArrayList<>();
            while (resultSet.next()) {
                tickets.add(buildUser(resultSet));
            }
            return tickets;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    private PortalUserEntity buildUser(ResultSet resultSet) throws SQLException {
        var roleEntity = new RoleEntity(
                resultSet.getInt("id"),
                Roles.valueOf(resultSet.getString("role"))
        );

        return new PortalUserEntity(
                resultSet.getInt("id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("nickname"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                resultSet.getString("image"),
                roleDao.findById(resultSet.getInt("role_id"),
                        resultSet.getStatement().getConnection()).orElse(null)
        );
    }
}
