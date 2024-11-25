package db;

import db.dao.impl.PortalUserDaoImpl;
import db.dao.impl.RoleDaoImpl;
import db.dto.PortalUserFilter;
import db.dto.RoleFilter;
import db.enums.Roles;
import db.entity.PortalUserEntity;
import db.entity.RoleEntity;

public class DaoRunner {
    public static void main(String[] args) {

        var portalUserDao = PortalUserDaoImpl.getInstance();
        var roleDao = RoleDaoImpl.getInstance();

        // добавление записей в таблицу Ролей
        roleDao.save(new RoleEntity(Roles.MODERATOR));
        roleDao.save(new RoleEntity(Roles.USER));
        roleDao.save(new RoleEntity(Roles.GUEST));

        //поиск по ID Роли
        System.out.println(roleDao.findById(2));

        // поиск всех записей таблицы Роли
        System.out.println(roleDao.findAll());

        // поиск с использованием фильтра по названию Роли
        var roleFilter = new RoleFilter(2, 0, "GUEST");
        System.out.println(roleDao.findAllByFilter(roleFilter));

        // обновление записи в таблице Роли
        var maybeRole = roleDao.findById(2);
        System.out.println(maybeRole);

        maybeRole.ifPresent(roleEntity -> {
            roleEntity.setRole(Roles.GUEST);
            roleDao.update(roleEntity);
        });

        // удаление записи из Роли
        roleDao.delete(3);


        var roleModerator = roleDao.findById(1).orElseThrow();
        var roleUser = roleDao.findById(2).orElseThrow();
        var roleGuest = roleDao.findById(3).orElseThrow();

        // добавление записей в таблицу User
        portalUserDao.save(new PortalUserEntity("John", "Doe", "johndoe", "johndoe@example.com", "password50", "", roleModerator));
        portalUserDao.save(new PortalUserEntity("Jane", "Smith", "janesmith", "janesmith@example.com", "password22", "", roleUser));
        portalUserDao.save(new PortalUserEntity("Alex", "Brown", "alexbrown", "alexbrown@example.com", "password77", "", roleUser));
        portalUserDao.save(new PortalUserEntity("Chris", "Johnson", "chrisjohnson", "chrisjohnson@example.com", "password91", "", roleUser));
        portalUserDao.save(new PortalUserEntity("Anna", "Davis", "annadavis", "annadavis@example.com", "password17", "", roleUser));
        portalUserDao.save(new PortalUserEntity("Sam", "Wilson", "samwilson", "samwilson@example.com", "password2", "", roleUser));
        portalUserDao.save(new PortalUserEntity("Taylor", "Swift", "taylortaylor", "taylortaylor@example.com", "password80", "", roleGuest));
        portalUserDao.save(new PortalUserEntity("Jordan", "Taylor", "jordantaylor", "jordantaylor@example.com", "password38", "", roleGuest));
        portalUserDao.save(new PortalUserEntity("Pat", "Thomas", "patthomas", "patthomas@example.com", "password64", "", roleGuest));
        portalUserDao.save(new PortalUserEntity("Chris", "Anderson", "chrisanderson", "chrisanderson@example.com", "password12", "", roleGuest));

        // обновление записей в таблице User
        var maybeUser = portalUserDao.findById(2);
        System.out.println(maybeUser);

        maybeUser.ifPresent(portalUserEntity -> {
            portalUserEntity.setRole(roleModerator);
            portalUserDao.update(portalUserEntity);
        });

        // удаление записи из Роли
        portalUserDao.delete(10);

        // поиск юзера по Id
        System.out.println(portalUserDao.findById(1));

        // поиск всех записей таблицы Роли
        System.out.println(portalUserDao.findAll());

        // поиск с использованием фильтра по пользователю
        var userFilter = new PortalUserFilter(2, 0, "Alex", "Brown", "alexbrown", "alexbrown@example.com");
        System.out.println(portalUserDao.findAllByFilter(userFilter));
    }

}
