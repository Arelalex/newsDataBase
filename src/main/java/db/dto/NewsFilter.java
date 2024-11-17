package db.dto;

public record NewsFilter(int limit,
                         int offset,
                         String title,
                         String description,
                         String content,
                         String createdAt,
                         String updateAt) {
}
