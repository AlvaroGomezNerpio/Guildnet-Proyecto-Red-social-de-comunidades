package com.guildnet.backend.features.like;

import com.guildnet.backend.features.communityProfile.CommunityProfile;
import com.guildnet.backend.features.post.Post;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "likes")
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
