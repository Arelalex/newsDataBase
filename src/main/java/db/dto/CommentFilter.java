package db.dto;

public record CommentFilter(int limit,
                            int offset,
                            String content,
                            String createdAt,
                            String updateAt) {

}
