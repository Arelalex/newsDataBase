package db.dto;

public record CategoryFilter(int limit,
                             int offset,
                             String category) {
}
