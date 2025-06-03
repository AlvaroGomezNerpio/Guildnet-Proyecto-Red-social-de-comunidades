package com.guildnet.backend.features.post;

import com.guildnet.backend.features.post.dto.PostCreateDTO;
import com.guildnet.backend.features.post.dto.PostDTO;
import com.guildnet.backend.features.post.dto.PostDetailDTO;
import com.guildnet.backend.features.post.dto.PostUpdateDTO;

import java.util.List;

public interface PostService {
    PostDTO createPost(PostCreateDTO postCreateDTO);
    PostDTO updatePost(Long id, PostCreateDTO postCreateDTO);
    void deletePost(Long id);
    PostDetailDTO getPostById(Long id);
    List<PostDTO> getPostsByProfileId(Long profileId);
    List<PostDTO> getPostsByCommunityId(Long communityId); // Nuevo
    List<PostDTO> searchPostsByTitleInCommunity(Long communityId, String title);
    List<PostDTO> searchByTagsInCommunity(Long communityId, List<String> tags);
    List<PostDTO> searchByTitleAndTagsInCommunity(Long communityId, String title, List<String> tags);
    List<PostDTO> getAllPostsInCommunity(Long communityId);

}

