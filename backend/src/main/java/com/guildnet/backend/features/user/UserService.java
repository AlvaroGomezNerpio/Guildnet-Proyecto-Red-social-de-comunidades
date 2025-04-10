package com.guildnet.backend.features.user;

import com.guildnet.backend.features.user.dto.UpdateUserRequest;
import com.guildnet.backend.features.user.dto.UserDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface  UserService {

    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long id);
    void updateUserProfile(UpdateUserRequest request, MultipartFile file, User user);
    void deleteUser(Long id);

}
