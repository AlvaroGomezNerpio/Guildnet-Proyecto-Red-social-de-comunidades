package com.guildnet.backend.features.post;

import com.guildnet.backend.features.category.PostCategory;
import com.guildnet.backend.features.community.Community;
import com.guildnet.backend.features.like.Like;
import com.guildnet.backend.features.postcomment.PostComment;
import com.guildnet.backend.features.profile.CommunityProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Length;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Column(nullable = false, length = 200)
    private String title;

    /** HTML generado por el editor (saneado antes de guardarlo). */
    @Lob
    @Column(nullable = false, length = Length.LONG32)
    private String content;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag", nullable = false, length = 30)
    private List<String> tags = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private CommunityProfile profile;

    @ManyToOne(optional = false)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    /** Categoría oficial de la comunidad (opcional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private PostCategory category;

    @Builder.Default
    @OrderBy("id ASC")
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    /** Número de likes, calculado en la propia consulta (evita cargar la colección). */
    @Setter(AccessLevel.NONE)
    @Formula("(select count(*) from post_likes l where l.post_id = id)")
    private int likeCount;

    /** Número de comentarios, calculado en la propia consulta. */
    @Setter(AccessLevel.NONE)
    @Formula("(select count(*) from post_comments c where c.post_id = id)")
    private int commentCount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /** Momento en que se fijó en la comunidad; null si no está fijada. */
    @Setter(AccessLevel.NONE)
    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;
}
