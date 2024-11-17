package db.dto;

public record RoleFilter(int limit,
                         int offset,
                         String role) {
}
