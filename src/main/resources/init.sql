/**
ПОЛЬЗОВАТЕЛЬ может быть автором многих НОВОСТЕЙ, либо вообще не быть автором
ПОЛЬЗОВАТЕЛЬ может оставлять много КОММЕНТАРИЕВ, либо вообще не оставлять
ПОЛЬЗОВАТЕЛь может иметь только одну РОЛЬ (модератор, user, guest)
РОЛЬ может иметь несколько ПРАВ доступа (чтение, создание, редактирование, удаление, может быть комментарирование) для НОВОСТЕЙ и КОММЕНТАРИЕВ
НОВОСТЬ может иметь только одноо автора - ПОЛЬЗОВАТЕЛЯ
НОВОСТЬ может иметь много КОММЕНТАРИЕВ
НОВОСТЬ может иметь только один статус в одно время
КОММЕНТАРИЙ может принадлежать только одной НОВОСТИ
КОММЕНТАРИЙ может иметь только одного автора - ПОЛЬЗОВАТЕЛЯ
КОММЕНТАРИЙ может иметь только один статус в одно время
КАТЕГОРИЯ может иметь много НОВОСТЕЙ

Ораничения:
1. Неавторизованный пользователь (guest) не может редактировать новости и оставлять комментарии
2. Обычный пользователь не может удалять и редактировать новости и комментарии

Действия по добавлению:
1. Добавить в систему нового ПОЛЬЗОВАТЕЛЯ
2. Добавить в систему НОВОСТЬ
3. Добавить в систему КОММЕНТАРИЙ

Действия по просмотру:
1. Посмотреть список всех пользователей системы
2. Посмотреть список всех новостей системы
3. Посмотреть список новостей созданных определенным пользователем
4. Посмотреть список всех комментариев к новости
5. Посмотреть список комментариев созданных определенным пользователем
6. Посмотреть новости по определенной дате
7. Посмотреть все новости по определенной категории
8. Удаление старых новостей
*/

CREATE DATABASE news_repository_main;


CREATE TABLE portal_user
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(128)        NOT NULL,
    last_name  VARCHAR(128)        NOT NULL,
    nickname   VARCHAR(256) UNIQUE NOT NULL,
    email      VARCHAR(256)        NOT NULL UNIQUE,
    password   VARCHAR(128)        NOT NULL,
    image      VARCHAR(256)
    -- right_id   INT REFERENCES user_right (id) NOT NULL,
    -- role_id    INT REFERENCES role (id)       NOT NULL UNIQUE
    -- news_id    BIGINT REFERENCES news (id),
    -- comment_id BIGINT REFERENCES comment (id)
);

CREATE TABLE role
(
    id      SERIAL PRIMARY KEY,
    role    VARCHAR(128)                    NOT NULL,
    user_id INT REFERENCES portal_user (id) NOT NULL UNIQUE
);

CREATE TABLE user_right
(
    id         SERIAL PRIMARY KEY,
    user_right VARCHAR(128)             NOT NULL,
    role_id    INT REFERENCES role (id) NOT NULL
);


CREATE TABLE category
(
    id       SERIAL PRIMARY KEY,
    category VARCHAR(128) NOT NULL
    -- news_id  BIGINT REFERENCES status (id) NOT NULL
    --  comment_id  BIGINT REFERENCES comment (id),
    -- user_id     INT REFERENCES portal_user (id) NOT NULL UNIQUE
);

CREATE TABLE news
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(128)                    NOT NULL,
    description VARCHAR(256)                    NOT NULL,
    content     TEXT                            NOT NULL,
    created_at  TIMESTAMP                       NOT NULL,
    updated_at  TIMESTAMP,
    image       VARCHAR(256),
    -- status_id   INT REFERENCES status (id) NOT NULL UNIQUE
    --  comment_id  BIGINT REFERENCES comment (id),
    user_id     INT REFERENCES portal_user (id) NOT NULL,
    category_id INT REFERENCES category (id)    NOT NULL
);


CREATE TABLE comment
(
    id         BIGSERIAL PRIMARY KEY,
    content    TEXT                            NOT NULL,
    created_at TIMESTAMP                       NOT NULL,
    updated_at TIMESTAMP,
    attachment BYTEA,
    news_id    BIGINT REFERENCES news (id)     NOT NULL,
    user_id    INT REFERENCES portal_user (id) NOT NULL
    -- status_id  INT REFERENCES status (id)      NOT NULL UNIQUE
);

CREATE TABLE status
(
    id         SERIAL PRIMARY KEY,
    status     VARCHAR(128)                   NOT NULL,
    news_id    BIGINT REFERENCES news (id)    NOT NULL UNIQUE,
    comment_id BIGINT REFERENCES comment (id) NOT NULL UNIQUE
);

ALTER TABLE portal_user
    ALTER COLUMN password TYPE VARCHAR(256);

INSERT INTO portal_user (id, first_name, last_name, nickname, email, password, image)
        VALUES (1, 'Иван', 'Иванов', 'Jonny',
                '123@gmail.com', '123456', null);

INSERT INTO role (id, role, user_id)
    VALUES (1, 'moderator', 1);

SELECT setval('user_right_id_seq', 1, false);

DROP TABLE user_right;

ALTER TABLE role DROP COLUMN user_id;

ALTER TABLE portal_user
    ADD COLUMN role_id INT;

ALTER TABLE portal_user ALTER COLUMN role_id SET NOT NULL;

ALTER TABLE portal_user
    ADD CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES role(id),
    ALTER COLUMN role_id SET NOT NULL;

ALTER TABLE status DROP COLUMN news_id;

ALTER TABLE status DROP COLUMN comment_id;

ALTER TABLE news
    ADD COLUMN status_id INT;

ALTER TABLE news
    ADD CONSTRAINT fk_role FOREIGN KEY (status_id) REFERENCES status(id),
    ALTER COLUMN status_id SET NOT NULL;

    SELECT id
    FROM role
    WHERE role = role.role;

    SELECT u.id,
           u.first_name,
           u.last_name,
           u.nickname,
           u.email,
           r.role
    FROM portal_user u
    LEFT JOIN role r
    ON u.role_id = r.id
    WHERE u.id = 1;
