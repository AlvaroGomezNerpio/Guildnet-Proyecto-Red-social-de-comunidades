package com.guildnet.backend.features.title;

import com.guildnet.backend.features.Community.Community;
import com.guildnet.backend.features.communityProfile.CommunityProfile;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "titles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Title {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String textColor;

    private String backgroundColor;

    @ManyToMany(mappedBy = "titles")
    private List<CommunityProfile> profiles = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "community_id")
    private Community community;
}
