package db.service;

import db.dto.PortalUserFilter;

import java.util.List;

public interface PortalUserService {

    PortalUserFilter findById(Integer id);

    List<PortalUserFilter> findAll();

    PortalUserFilter save(PortalUserFilter portalUserFilter);

    PortalUserFilter update(PortalUserFilter portalUserFilter);

    void delete(PortalUserFilter portalUserFilter);

}
