package com.guildnet.backend.features.postComment;

import com.guildnet.backend.features.communityProfile.CommunityProfile;
import com.guildnet.backend.features.post.Post;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Publicación a la que pertenece este comentario
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // Perfil de comunidad que ha hecho el comentario
    @ManyToOne
    @JoinColumn(name = "profile_id")
    private CommunityProfile profile;
}
