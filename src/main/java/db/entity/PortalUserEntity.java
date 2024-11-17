package db.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortalUserEntity {

    private Integer id;
    private String firstName;
    private String lastName;
    private String nickname;
    private String email;
    private String password;
    private String image;
    private RoleEntity role;
    // private List<News> news;

    public PortalUserEntity(String firstName, String lastName, String nickname, String email, String password, String image, RoleEntity role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.image = image;
        this.role = role;
    }

}
