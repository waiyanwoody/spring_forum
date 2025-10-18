package com.example.communityforum.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(
        name = "comments",
        indexes = {
                @Index(name = "idx_comments_deleted_at", columnList = "deleted_at"),
                @Index(name = "idx_comments_deleted_by", columnList =  "deleted_by")
        }
)
@Where(clause = "deleted_at IS NULL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    //parent comment for replies
    @ManyToOne
    @JoinColumn(name= "parent_comment_id")
    @JsonIgnore
    private Comment parentComment;

    //children replies for parent
    @OneToMany(mappedBy = "parentComment",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Comment> replies;

    //for soft delete
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private User deletedBy;

}
