package com.guildnet.backend.features.postComment;

import com.guildnet.backend.features.postComment.dto.PostComentDTO;
import com.guildnet.backend.features.postComment.dto.PostCommentCreateDTO;
import com.guildnet.backend.features.postComment.dto.PostCommentUpdateDTO;

import java.util.List;

public interface PostCommentService {

    PostComentDTO createComment(PostCommentCreateDTO postCommentCreateDTO);
    PostComentDTO updateComment(Long commentId, PostCommentUpdateDTO postCommentUpdateDTO);
    List<PostComentDTO> getCommentsByPostId(Long postId);
    void deleteComment(Long commentId);

}
