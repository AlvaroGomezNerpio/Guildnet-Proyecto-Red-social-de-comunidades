package com.guildnet.backend.features.like;

import com.guildnet.backend.features.profile.CommunityProfile;
import com.guildnet.backend.features.post.Post;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(name = "uk_like_post_profile", columnNames = {"post_id", "profile_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Publicación que ha recibido el "me gusta"
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // Perfil que dio el "me gusta"
    @ManyToOne
    @JoinColumn(name = "profile_id")
    private CommunityProfile profile;
}
