package com.guildnet.backend.features.post;

import com.guildnet.backend.features.community.Community;
import com.guildnet.backend.features.profile.CommunityProfile;
import com.guildnet.backend.features.like.Like;
import com.guildnet.backend.features.postcomment.PostComment;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    @Builder.Default
    @ElementCollection
    private List<String> tags = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Perfil de comunidad que creó la publicación
    @ManyToOne
    @JoinColumn(name = "profile_id")
    private CommunityProfile profile;

    @ManyToOne
    @JoinColumn(name = "community_id")
    private Community community;

    @Builder.Default
    @OrderBy("id ASC")
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    /** Número de likes, calculado en la propia consulta (evita cargar la colección). */
    @Setter(AccessLevel.NONE)
    @Formula("(select count(*) from likes l where l.post_id = id)")
    private int likeCount;

    /** Número de comentarios, calculado en la propia consulta. */
    @Setter(AccessLevel.NONE)
    @Formula("(select count(*) from post_comments c where c.post_id = id)")
    private int commentCount;
}
