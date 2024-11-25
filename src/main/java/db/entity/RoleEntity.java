package db.entity;

import db.enums.Roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleEntity {
    private Integer id;
    private Roles role;

    public RoleEntity(Roles role) {
        this.role = role;
    }

}
