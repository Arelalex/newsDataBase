package db.dao.impl;

import db.dao.Dao;
import db.dto.NewsFilter;
import db.entity.CategoryEntity;
import db.entity.NewsEntity;
import db.entity.PortalUserEntity;
import db.exception.DaoException;
import db.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class NewsDao implements Dao<Long, NewsEntity> {

    private static final NewsDao INSTANCE = new NewsDao();

    private static final String DELETE_SQL = """
            DELETE FROM news
            WHERE id = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO news (title, description, content, created_at, updated_at, image, user_id, category_id, status_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;

    private static final String UPDATE_SQL = """
            UPDATE news
            SET title = ?,
                description = ?,
                content = ?,
                created_at = ?,
                updated_at = ?,
                image = ?,
                user_id = ?,
                category_id = ?,
                status_id = ?
            WHERE id = ?
            """;

    private static final String FIND_ALL_SQL = """
            SELECT id,
                title,
                description,
                content,
                created_at,
                updated_at,
                image,
                user_id,
                category_id,
                status_id
            FROM news
            """;

    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            WHERE news.id = ?
            """;

    private final PortalUserDao userDao = PortalUserDao.getInstance();
    private final CategoryDao categoryDao = CategoryDao.getInstance();
    private final StatusDao statusDao = StatusDao.getInstance();
    private final RoleDao roleDao = RoleDao.getInstance();

    public static NewsDao getInstance() {
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
    public NewsEntity save(NewsEntity newsEntity) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, newsEntity.getTitle());
            preparedStatement.setString(2, newsEntity.getDescription());
            preparedStatement.setString(3, newsEntity.getContent());
            preparedStatement.setDate(4, Date.valueOf(newsEntity.getCreateAt().toLocalDate()));
            preparedStatement.setDate(5, Date.valueOf(newsEntity.getUpdateAt().toLocalDate()));
            preparedStatement.setString(6, newsEntity.getImage());
            preparedStatement.setInt(7, newsEntity.getUser().getId());
            preparedStatement.setInt(8, newsEntity.getCategory().getId());
            preparedStatement.setInt(9, newsEntity.getStatus().getId());

            preparedStatement.executeUpdate();

            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                newsEntity.setId(generatedKeys.getInt("id"));
            }
            return newsEntity;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    @Override
    public void update(NewsEntity newsEntity) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, newsEntity.getTitle());
            preparedStatement.setString(2, newsEntity.getDescription());
            preparedStatement.setString(3, newsEntity.getContent());
            preparedStatement.setDate(4, Date.valueOf(newsEntity.getCreateAt().toLocalDate()));
            preparedStatement.setDate(5, Date.valueOf(newsEntity.getUpdateAt().toLocalDate()));
            preparedStatement.setString(6, newsEntity.getImage());
            preparedStatement.setInt(7, newsEntity.getUser().getId());
            preparedStatement.setInt(8, newsEntity.getCategory().getId());
            preparedStatement.setInt(9, newsEntity.getStatus().getId());

            preparedStatement.setInt(10, newsEntity.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    @Override
    public Optional<NewsEntity> findById(int id) {
        try (var connection = ConnectionManager.get()) {
            return findById(id, connection);
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    public Optional<NewsEntity> findById(int id, Connection connection) {
        try (var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setLong(1, id);

            var resultSet = preparedStatement.executeQuery();
            NewsEntity newsEntity = null;
            if (resultSet.next()) {
                newsEntity = buildNews(resultSet);
            }
            return Optional.ofNullable(newsEntity);
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    public List<NewsEntity> findAll(NewsFilter filter) {
        List<Object> parameters = new ArrayList<>();
        List<String> whereSql = new ArrayList<>();
        if (filter.title() != null) {
            whereSql.add("title LIKE ?");
            parameters.add("%" + filter.title() + "%");
        }
        if (filter.description() != null) {
            whereSql.add("description = ?");
            parameters.add("%" + filter.title() + "%");
        }
        if (filter.content() != null) {
            whereSql.add("content = ?");
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
            List<NewsEntity> newsEntities = new ArrayList<>();
            while (resultSet.next()) {
                newsEntities.add(buildNews(resultSet));
            }
            return newsEntities;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    @Override
    public List<NewsEntity> findAll() {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            var resultSet = preparedStatement.executeQuery();
            List<NewsEntity> newsEntities = new ArrayList<>();
            while (resultSet.next()) {
                newsEntities.add(buildNews(resultSet));
            }
            return newsEntities;
        } catch (SQLException throwables) {
            throw new DaoException(throwables);
        }
    }

    private NewsEntity buildNews(ResultSet resultSet) throws SQLException {
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
        var category = new CategoryEntity(
                resultSet.getInt("id"),
                resultSet.getString("category")
        );

        return new NewsEntity(
                resultSet.getInt("id"),
                resultSet.getString("title"),
                resultSet.getString("description"),
                resultSet.getString("content"),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("update_at").toLocalDateTime(),
                resultSet.getString("image"),

                userDao.findById(resultSet.getInt("user_id"),
                        resultSet.getStatement().getConnection()).orElse(null),
                categoryDao.findById(resultSet.getInt("category_id"),
                        resultSet.getStatement().getConnection()).orElse(null),
                statusDao.findById(resultSet.getInt("category_id"),
                        resultSet.getStatement().getConnection()).orElse(null)
        );
    }

}
