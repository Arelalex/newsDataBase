package db.dto;

public record StatusFilter(
        int limit,
        int offset,
        String status) {
}
