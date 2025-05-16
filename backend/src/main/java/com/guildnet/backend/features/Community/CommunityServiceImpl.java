package com.guildnet.backend.features.Community;

import com.guildnet.backend.features.Community.dto.CommunityResponse;
import com.guildnet.backend.features.Community.dto.CreateCommunityRequest;
import com.guildnet.backend.features.Community.dto.UpdateCommunityRequest;
import com.guildnet.backend.features.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityRepository communityRepository;

    @Override
    public List<Community> getAllCommunities() {
        return communityRepository.findAll();
    }

    @Override
    public Optional<Community> getCommunityById(Long id) {
        return communityRepository.findById(id);
    }

    @Override
    public List<Community> searchCommunitiesByName(String name) {
        return communityRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Community> searchCommunitiesByTag(String tag) {
        return communityRepository.findAll().stream()
                .filter(c -> c.getTags() != null && c.getTags().stream().anyMatch(
                        t -> t.toLowerCase().contains(tag.toLowerCase())
                ))
                .toList();
    }

    @Override
    public List<Community> searchByNameAndTags(String name, List<String> tags) {
        return communityRepository.findAll().stream()
                .filter(c -> c.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(c -> c.getTags() != null && tags.stream().anyMatch(
                        inputTag -> c.getTags().stream().anyMatch(
                                t -> t.toLowerCase().contains(inputTag.toLowerCase())
                        )
                ))
                .toList();
    }

    @Override
    public List<Community> searchByTags(List<String> tags) {
        return communityRepository.findAll().stream()
                .filter(c -> c.getTags() != null && tags.stream().anyMatch(
                        inputTag -> c.getTags().stream().anyMatch(
                                t -> t.toLowerCase().contains(inputTag.toLowerCase())
                        )
                ))
                .toList();
    }

    @Override
    public CommunityResponse createCommunity(CreateCommunityRequest request, User owner, MultipartFile imageFile, MultipartFile bannerFile) {
        try {
            Community community = Community.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .rules(request.getRules())
                    .owner(owner)
                    .tags(request.getTags())
                    .build();

            if (imageFile != null && !imageFile.isEmpty()) {
                community.setImage(saveImage(imageFile));
            }

            if (bannerFile != null && !bannerFile.isEmpty()) {
                community.setBanner(saveImage(bannerFile));
            }

            Community saved = communityRepository.save(community);

            return new CommunityResponse(
                    saved.getId(),
                    saved.getName(),
                    saved.getDescription(),
                    saved.getRules(),
                    saved.getImage(),
                    saved.getBanner(),
                    saved.getTags(),
                    saved.getOwner().getId()
            );
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar las imágenes", e);
        }
    }

    @Override
    public Optional<Community> updateCommunity(Long id, UpdateCommunityRequest request, MultipartFile imageFile, MultipartFile bannerFile) {
        return communityRepository.findById(id).map(community -> {
            community.setName(request.getName());
            community.setDescription(request.getDescription());
            community.setRules(request.getRules());
            community.setTags(request.getTags());

            try {
                if (imageFile != null && !imageFile.isEmpty()) {
                    String imageUrl = saveImage(imageFile);
                    community.setImage(imageUrl);
                }

                if (bannerFile != null && !bannerFile.isEmpty()) {
                    String bannerUrl = saveImage(bannerFile);
                    community.setBanner(bannerUrl);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar las imágenes", e);
            }

            return communityRepository.save(community);
        });
    }

    @Override
    public void deleteCommunity(Long id) {
        communityRepository.deleteById(id);
    }

    @Override
    public String saveImage(MultipartFile file) throws IOException {
        String uniqueName = UUID.randomUUID() + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".png";

        Path uploadPath = Paths.get("uploads/communities/");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "http://localhost:8080/uploads/communities/" + uniqueName;
    }

    @Override
    public List<Community> getCommunitiesByUser(User user) {
        return communityRepository.findCommunitiesByUserId(user.getId());
    }

    @Override
    public List<Community> getSuggestedCommunities(User user) {
        if (user.getTags() == null || user.getTags().isEmpty()) {
            return List.of();
        }
        return communityRepository.findSuggestedCommunities(user.getTags(), user.getId());
    }

    @Override
    public List<Community> getMostPopularCommunities() {
        return communityRepository.findCommunitiesOrderBySubscribersDesc();
    }



}


