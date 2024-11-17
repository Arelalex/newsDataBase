package db.dao.impl;

import db.dao.*;
import db.dto.CommentFilter;
import db.entity.CommentEntity;
import db.entity.NewsEntity;
import db.entity.PortalUserEntity;
import db.entity.StatusEntity;
import db.exception.DaoException;
import db.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class CommentDao implements Dao<Long, CommentEntity> {

    private static final CommentDao INSTANCE = new CommentDao();

    private static final String DELETE_SQL = """
            DELETE FROM comment
            WHERE id = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO comment (content, created_at, updated_at, attachment, news_id, user_id, status_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?);
            """;

    private static final String UPDATE_SQL = """
                    UPDATE comment
                    SET content = ?,
                        created_at = ?,
                        updated_at = ?,
                        attachment = ?,
                        news_id = ?,
                        user_id = ?,
                        status_id = ?
                    WHERE id = ?
            """;

    private static final String FIND_ALL_SQL = """
            SELECT id,
            content,
            created_at,
            updated_at,
            attachment,
            news_id,
            user_id,
            status_id
            FROM comment
            """;

    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            WHERE comment.id = ?
            """;

    private final NewsDao newsDao = NewsDao.getInstance();
    private final PortalUserDao portalUserDao = PortalUserDao.getInstance();
    private final CategoryDao categoryDao = CategoryDao.getInstance();
    private final StatusDao statusDao = StatusDao.getInstance();
    private final RoleDao roleDao = RoleDao.getInstance();

    private CommentDao() {
    }

    public static CommentDao getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean delete(Long id) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(DELETE_SQL)) {
            preparedStatement.setLong(1, id);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    @Override
    public CommentEntity save(CommentEntity entity) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, entity.getContent());
            preparedStatement.setDate(2, Date.valueOf(entity.getCreateAt().toLocalDate()));
            preparedStatement.setDate(3, Date.valueOf(entity.getUpdateAt().toLocalDate()));
            preparedStatement.setString(4, entity.getAttachment());
            preparedStatement.setLong(5, entity.getNews().getId());
            preparedStatement.setInt(6, entity.getUser().getId());
            preparedStatement.setInt(7, entity.getStatus().getId());

            preparedStatement.executeUpdate();

            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                entity.setId(generatedKeys.getInt("id"));
            }
            return entity;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    @Override
    public void update(CommentEntity entity) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, entity.getContent());
            preparedStatement.setDate(2, Date.valueOf(entity.getCreateAt().toLocalDate()));
            preparedStatement.setDate(3, Date.valueOf(entity.getUpdateAt().toLocalDate()));
            preparedStatement.setString(4, entity.getAttachment());
            preparedStatement.setLong(5, entity.getNews().getId());
            preparedStatement.setInt(6, entity.getUser().getId());
            preparedStatement.setInt(7, entity.getStatus().getId());

            preparedStatement.setLong(8, entity.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    @Override
    public Optional<CommentEntity> findById(int id) {
        try (var connection = ConnectionManager.get()) {
            return findById(id, connection);
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    public Optional<CommentEntity> findById(int id, Connection connection) {
        try (var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setLong(1, id);

            var resultSet = preparedStatement.executeQuery();
            CommentEntity commentEntity = null;
            if (resultSet.next()) {
                commentEntity = buildComment(resultSet);
            }
            return Optional.ofNullable(commentEntity);
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    public List<CommentEntity> findAll(CommentFilter filter) {
        List<Object> parameters = new ArrayList<>();
        List<String> whereSql = new ArrayList<>();
        if (filter.content() != null) {
            whereSql.add("content LIKE ?");
            parameters.add("%" + filter.content() + "%");
        }
        if (filter.createdAt() != null) {
            whereSql.add("created_at = ?");
            parameters.add("%" + filter.createdAt() + "%");
        }
        if (filter.updateAt() != null) {
            whereSql.add("updated_at = ?");
            parameters.add("%" + filter.updateAt() + "%");
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
            List<CommentEntity> commentEntities = new ArrayList<>();
            while (resultSet.next()) {
                commentEntities.add(buildComment(resultSet));
            }
            return commentEntities;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    @Override
    public List<CommentEntity> findAll() {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            var resultSet = preparedStatement.executeQuery();
            List<CommentEntity> commentEntities = new ArrayList<>();
            while (resultSet.next()) {
                commentEntities.add(buildComment(resultSet));
            }
            return commentEntities;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    private CommentEntity buildComment(ResultSet resultSet) throws SQLException {
        var newsEntity = new NewsEntity(
                resultSet.getInt("id"),
                resultSet.getString("title"),
                resultSet.getString("description"),
                resultSet.getString("content"),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("update_at").toLocalDateTime(),
                resultSet.getString("image"),
                portalUserDao.findById(resultSet.getInt("user_id"),
                        resultSet.getStatement().getConnection()).orElse(null),
                categoryDao.findById(resultSet.getInt("category_id"),
                        resultSet.getStatement().getConnection()).orElse(null),
                statusDao.findById(resultSet.getInt("status_id"),
                        resultSet.getStatement().getConnection()).orElse(null)
        );
        var userEntity = new PortalUserEntity(
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

        var statusEntity = new StatusEntity(
                resultSet.getInt("id"),
                resultSet.getString("status")
        );

        return new CommentEntity(
                resultSet.getInt("id"),
                resultSet.getString("connect"),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("updated_at").toLocalDateTime(),
                resultSet.getString("attachment"),
                newsDao.findById(resultSet.getInt("news_id"),
                        resultSet.getStatement().getConnection()).orElse(null),
                portalUserDao.findById(resultSet.getInt("user_id"),
                        resultSet.getStatement().getConnection()).orElse(null),
                statusDao.findById(resultSet.getInt("status_id"),
                        resultSet.getStatement().getConnection()).orElse(null)
        );
    }
}
