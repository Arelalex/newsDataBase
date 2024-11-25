package db.mapper.impl;

import db.dto.PortalUserFilter;
import db.entity.PortalUserEntity;
import db.mapper.PortalUserMapper;

public class PortalUserMapperImpl implements PortalUserMapper {

    private static PortalUserMapperImpl instance;

    private PortalUserMapperImpl() {
    }

    public static PortalUserMapperImpl getInstance() {
        if (instance == null) {
            instance = new PortalUserMapperImpl();
        }
        return instance;
    }

    @Override
    public PortalUserEntity toEntity(PortalUserFilter dto) {
        return PortalUserEntity
                .builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .build();
    }

    @Override
    public PortalUserFilter toDto(PortalUserEntity portalUser) {
        return PortalUserFilter
                .builder()
                .firstName(portalUser.getFirstName())
                .lastName(portalUser.getLastName())
                .nickname(portalUser.getNickname())
                .email(portalUser.getEmail())
                .build();
    }

}
