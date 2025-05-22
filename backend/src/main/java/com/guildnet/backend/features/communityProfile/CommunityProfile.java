package com.guildnet.backend.features.communityProfile;

import com.guildnet.backend.features.Community.Community;
import com.guildnet.backend.features.chatMessage.ChatMessage;
import com.guildnet.backend.features.like.Like;
import com.guildnet.backend.features.notification.Notification;
import com.guildnet.backend.features.post.Post;
import com.guildnet.backend.features.postComment.PostComment;
import com.guildnet.backend.features.profileComment.ProfileComment;
import com.guildnet.backend.features.role.Role;
import com.guildnet.backend.features.title.Title;
import com.guildnet.backend.features.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "community_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Lob
    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    private String profileImage;

    // Usuario al que pertenece este perfil
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Comunidad a la que pertenece este perfil
    @ManyToOne
    @JoinColumn(name = "community_id")
    private Community community;

    // Título destacado que este perfil tiene dentro de la comunidad (opcional)
    @ManyToOne
    @JoinColumn(name = "featured_title_id")
    private Title featuredTitle;

    // Publicaciones creadas por este perfil
    @OneToMany(mappedBy = "profile")
    private List<Post> posts;

    // Comentarios en publicaciones escritos por este perfil
    @OneToMany(mappedBy = "profile")
    private List<PostComment> postComments;

    // Comentarios que este perfil ha dejado en el perfil de otros usuarios
    @OneToMany(mappedBy = "authorProfile")
    private List<ProfileComment> profileCommentsWritten;

    // Comentarios que este perfil ha recibido en su propio perfil
    @OneToMany(mappedBy = "targetProfile")
    private List<ProfileComment> profileCommentsReceived;

    // Likes que este perfil ha dado a publicaciones
    @OneToMany(mappedBy = "profile")
    private List<Like> likes;

    // Mensajes enviados en el chat de la comunidad
    @OneToMany(mappedBy = "profile")
    private List<ChatMessage> chatMessages;

    // Roles que tiene este perfil en la comunidad (relación muchos a muchos)
    @ManyToMany
    @JoinTable(name = "profile_roles",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;

    // Títulos obtenidos por este perfil (relación muchos a muchos)
    @ManyToMany
    @JoinTable(name = "profile_titles",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "title_id"))
    private List<Title> titles;

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> receivedNotifications = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<Notification> sentNotifications = new ArrayList<>();
}
