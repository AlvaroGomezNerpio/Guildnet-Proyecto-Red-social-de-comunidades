package com.guildnet.backend.features.post;

import com.guildnet.backend.features.Community.Community;
import com.guildnet.backend.features.communityProfile.CommunityProfile;
import com.guildnet.backend.features.like.Like;
import com.guildnet.backend.features.postComment.PostComment;
import jakarta.persistence.*;
import lombok.*;

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

    private String content;

    // Lista de etiquetas asociadas a la publicación (guardada como colección embebida)
    @ElementCollection
    private List<String> tags;

    // Perfil de comunidad que creó la publicación
    @ManyToOne
    @JoinColumn(name = "profile_id")
    private CommunityProfile profile;

    @ManyToOne
    @JoinColumn(name = "community_id")
    private Community community;


    // Lista de comentarios asociados a esta publicación
    @OneToMany(mappedBy = "post")
    private List<PostComment> comments;

    // Lista de "me gusta" asociados a esta publicación
    @OneToMany(mappedBy = "post")
    private List<Like> likes;

}
