package db.dto;

public record PortalUserFilter(int limit,
                               int offset,
                               String firstName,
                               String lastName,
                               String nickname,
                               String email) {
}
