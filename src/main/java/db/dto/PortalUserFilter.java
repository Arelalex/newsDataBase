package db.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortalUserFilter {
    private int limit;
    private int offset;
    private Integer id;
    private String firstName;
    private String lastName;
    private String nickname;
    private String email;

    public PortalUserFilter(int limit, int offset, String firstName, String lastName, String nickname, String email) {
        this.limit = limit;
        this.offset = offset;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.email = email;
    }
}
