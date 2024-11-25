package db.dao.impl;

import db.dao.NewsDao;
import db.dto.NewsFilter;
import db.entity.CategoryEntity;
import db.entity.NewsEntity;
import db.entity.PortalUserEntity;
import db.exception.*;
import db.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class NewsDaoImpl implements NewsDao<Long, NewsEntity> {

    private static NewsDaoImpl instance = new NewsDaoImpl();
    private static final String PORTAL_USER_ID = "id";
    private static final String USER_FIRST_NAME = "first_name";
    private static final String USER_LAST_NAME = "last_name";
    private static final String NICKNAME = "nickname";
    private static final String USER_EMAIL = "email";
    private static final String USER_PASSWORD = "password";
    private static final String IMAGE = "image";
    private static final String FOREIGN_ROLE_ID = "role_id";

    private static final String CATEGORY_ID = "id";
    private static final String CATEGORY_CATEGORY = "category";

    private static final String NEWS_ID = "id";
    private static final String NEWS_TITLE = "title";
    private static final String NEWS_DESCRIPTION = "description";
    private static final String NEWS_CONTENT = "content";
    private static final String NEWS_CREATED_AT = "created_at";
    private static final String NEWS_UPDATE_AT = "updated_at";
    private static final String NEWS_IMAGE = "image";

    private static final String FOREIGN_STATUS_ID = "status_id";

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

    private final PortalUserDaoImpl userDao = PortalUserDaoImpl.getInstance();
    private final CategoryDaoImpl categoryDao = CategoryDaoImpl.getInstance();
    private final StatusDaoImpl statusDaoImpl = StatusDaoImpl.getInstance();
    private final RoleDaoImpl roleDaoImpl = RoleDaoImpl.getInstance();

    public static synchronized NewsDaoImpl getInstance() {
        if (instance == null) {
            instance = new NewsDaoImpl();
        }
        return instance;
    }

    @Override
    public boolean delete(Long id) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(DELETE_SQL)) {
            preparedStatement.setLong(1, id);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException throwables) {
            throw new DaoExceptionDelete("Error deleting values from table",throwables);
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
            throw new DaoExceptionInsert("Error inserting values into table", throwables);
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
            throw new DaoExceptionUpdate("Error updating values in table", throwables);
        }
    }

    @Override
    public Optional<NewsEntity> findById(int id) {
        try (var connection = ConnectionManager.get()) {
            return findById(id, connection);
        } catch (SQLException throwables) {
            throw new DaoExceptionFindById("Error searching values by ID in table", throwables);
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
            throw new DaoExceptionFindById("Error searching values by ID in table", throwables);
        }
    }

    public List<NewsEntity> findAllByFilter(NewsFilter filter) {
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
            throw new DaoExceptionFindAll("Error searching for values in table", throwables);
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
            throw new DaoExceptionFindAll("Error searching for values in table", throwables);
        }
    }

    private NewsEntity buildNews(ResultSet resultSet) throws SQLException {
        var userEntity = new PortalUserEntity(
                resultSet.getInt(PORTAL_USER_ID),
                resultSet.getString(USER_FIRST_NAME),
                resultSet.getString(USER_LAST_NAME),
                resultSet.getString(NICKNAME),
                resultSet.getString(USER_EMAIL),
                resultSet.getString(USER_PASSWORD),
                resultSet.getString(IMAGE),
                roleDaoImpl.findById(resultSet.getInt(FOREIGN_ROLE_ID),
                        resultSet.getStatement().getConnection()).orElse(null)
        );
        var category = new CategoryEntity(
                resultSet.getInt(CATEGORY_ID),
                resultSet.getString(CATEGORY_CATEGORY)
        );

        return new NewsEntity(
                resultSet.getInt(NEWS_ID),
                resultSet.getString(NEWS_TITLE),
                resultSet.getString(NEWS_DESCRIPTION),
                resultSet.getString(NEWS_CONTENT),
                resultSet.getTimestamp(NEWS_CREATED_AT).toLocalDateTime(),
                resultSet.getTimestamp(NEWS_UPDATE_AT).toLocalDateTime(),
                resultSet.getString(NEWS_IMAGE),
                userDao.findById(resultSet.getInt(PORTAL_USER_ID),
                        resultSet.getStatement().getConnection()).orElse(null),
                categoryDao.findById(resultSet.getInt(CATEGORY_ID),
                        resultSet.getStatement().getConnection()).orElse(null),
                statusDaoImpl.findById(resultSet.getInt(FOREIGN_STATUS_ID),
                        resultSet.getStatement().getConnection()).orElse(null)
        );
    }

}
