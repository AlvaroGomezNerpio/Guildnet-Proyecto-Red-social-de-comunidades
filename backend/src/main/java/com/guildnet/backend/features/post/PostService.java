package com.guildnet.backend.features.post;

import com.guildnet.backend.features.post.dto.PostCreateDTO;
import com.guildnet.backend.features.post.dto.PostDTO;
import com.guildnet.backend.features.post.dto.PostDetailDTO;
import com.guildnet.backend.features.post.dto.PostUpdateDTO;

import java.util.List;

public interface PostService {
    PostDTO createPost(PostCreateDTO postCreateDTO);
    List<PostDTO> getAllPosts();
    PostDetailDTO getPostById(Long id);
    PostDTO updatePost(Long postId, PostUpdateDTO updateDTO);
    void deletePost(Long id);
}
