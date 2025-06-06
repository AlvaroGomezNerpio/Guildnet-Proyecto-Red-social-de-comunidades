package com.guildnet.backend.features.Community;

import com.guildnet.backend.features.chatMessage.ChatMessage;
import com.guildnet.backend.features.communityProfile.CommunityProfile;
import com.guildnet.backend.features.post.Post;
import com.guildnet.backend.features.role.Role;
import com.guildnet.backend.features.title.Title;
import com.guildnet.backend.features.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "communities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Lob
    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    @Lob
    @Column(columnDefinition = "MEDIUMTEXT")
    private String rules;

    private String image;

    private String banner;

    @ElementCollection
    private List<String> tags;

    // Relación con el usuario que creó la comunidad (propietario)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Lista de perfiles asociados a esta comunidad (usuarios que forman parte de ella)
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityProfile> communityProfiles;

    // Lista de publicaciones realizadas en esta comunidad
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    // Lista de roles definidos dentro de esta comunidad
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Role> roles;

    // Lista de títulos que se pueden asignar dentro de esta comunidad
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Title> titles;

    // Lista de mensajes enviados en el chat de esta comunidad
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages;

}
