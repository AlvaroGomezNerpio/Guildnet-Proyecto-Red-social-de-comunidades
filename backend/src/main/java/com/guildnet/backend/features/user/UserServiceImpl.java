package com.guildnet.backend.features.user;

import com.guildnet.backend.features.user.dto.UpdateUserRequest;
import com.guildnet.backend.features.user.dto.UserDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getProfileImage(),
                        user.getTags()
                ))
                .toList();
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileImage(),
                user.getTags());
    }

    @Transactional
    public UserDTO updateUserProfile(UpdateUserRequest request, MultipartFile imageFile, User authUser) {
        // Recuperar desde DB por ID → así Hibernate lo gestiona correctamente
        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getTags() != null) {
            user.setTags(request.getTags());
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            user.setProfileImage(saveUserProfileImage(imageFile));
        }

        user = userRepository.save(user);

        // Forzar carga de tags dentro de la sesión activa
        user.getTags().size();

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileImage(),
                user.getTags()
        );
    }


    public void updateUserTags(User user, List<String> tags) {
        user.setTags(tags);
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Método privado para modularizar guardado de imagen
    private String saveUserProfileImage(MultipartFile imageFile) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = UUID.randomUUID() + "_" + timestamp + ".png";
            Path uploadPath = Paths.get("uploads/users");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(filename);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "http://localhost:8080/uploads/users/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la nueva imagen", e);
        }
    }
}

