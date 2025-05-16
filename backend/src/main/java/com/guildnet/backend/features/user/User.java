package com.guildnet.backend.features.user;

import com.guildnet.backend.features.communityProfile.CommunityProfile;
import com.guildnet.backend.features.notification.Notification;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchConnectionDetails;

import java.util.List;

@Entity
@Table(name = "users") // Renombramos la tabla a "users"
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements org.springframework.security.core.userdetails.UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(unique = true)
    private String email;

    private String password;

    private String profileImage;

    @ElementCollection
    private List<String> tags;


    @Override
    public String getUsername() {
        return email;
    } // Utilizamos el correo como nombre de usuario para login

    public String getTrueUsername() {
        return username;
    } // Devuelve el nombre visible del usuario

    // No implementamos roles aún, devolvemos una lista vacía
    @Override
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        return java.util.Collections.emptyList();
    }

    // Relación uno a muchos con los perfiles del usuario en diferentes comunidades
    @OneToMany(mappedBy = "user")
    private List<CommunityProfile> communityProfiles;

    // Métodos obligatorios para UserDetails, devolvemos true por defecto
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}

