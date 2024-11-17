package db.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsEntity {

    private Integer id;
    private String title;
    private String description;
    private String content;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private String image;
    private PortalUserEntity user;
    private CategoryEntity category;
    private StatusEntity status;


}
