package com.guildnet.backend.auth;

import com.guildnet.backend.security.jwt.JwtUtils;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.features.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(
            @RequestPart("user") RegisterRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(409).body("El correo ya está en uso.");
        }

        String imageUrl = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Ruta de la carpeta de usuarios
                Path uploadPath = Paths.get("uploads/users/");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Nombre único
                String uniqueName = UUID.randomUUID() + "_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".png";

                // Ruta final del archivo
                Path filePath = uploadPath.resolve(uniqueName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Generamos URL pública para guardarla en la base de datos
                imageUrl = "http://localhost:8080/uploads/users/" + uniqueName;

            } catch (IOException e) {
                return ResponseEntity.status(500).body("Error al guardar la imagen");
            }
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .profileImage(imageUrl)
                .build();

        userRepository.save(newUser);
        String token = jwtUtils.generateToken(newUser);

        return ResponseEntity.status(201).body(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow();
            String token = jwtUtils.generateToken(user);
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }
}

