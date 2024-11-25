package db.service.impl;

import db.dao.PortalUserDao;
import db.dao.impl.PortalUserDaoImpl;
import db.dto.PortalUserFilter;
import db.entity.PortalUserEntity;
import db.mapper.PortalUserMapper;
import db.mapper.impl.PortalUserMapperImpl;
import db.service.PortalUserService;

import java.util.List;
import java.util.Optional;

public class PortalUserServiceImpl implements PortalUserService {

    private static PortalUserServiceImpl instance;
    private final PortalUserDao portalUserDao = PortalUserDaoImpl.getInstance();
    private final PortalUserMapper portalUserMapper = PortalUserMapperImpl.getInstance();

    private PortalUserServiceImpl() {
    }
    public static synchronized PortalUserServiceImpl getInstance() {
        if (instance == null) {
            instance = new PortalUserServiceImpl();
        }
        return instance;
    }

    @Override
    public PortalUserFilter findById(Integer id) {
       return portalUserDao.findById(id)
               .map(entity -> portalUserMapper.toDto((PortalUserEntity) entity))
               .isPresent() ? PortalUserFilter.builder().build() : null;
    }

    @Override
    public List<PortalUserFilter> findAll() {
        return portalUserDao.findAll()
                .stream()
                .map(entity -> portalUserMapper.toDto((PortalUserEntity) entity))
                .toList();
    }

    @Override
    public PortalUserFilter save(PortalUserFilter portalUserFilter) {
        return null;
    }

    @Override
    public PortalUserFilter update(PortalUserFilter portalUserFilter) {
        return null;
    }

    @Override
    public void delete(PortalUserFilter portalUserFilter) {

    }
}
