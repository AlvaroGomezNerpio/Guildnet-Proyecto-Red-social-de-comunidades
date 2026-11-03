package com.guildnet.backend.auth;

import com.guildnet.backend.common.exception.ConflictException;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.features.user.UserRepository;
import com.guildnet.backend.security.jwt.JwtService;
import com.guildnet.backend.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final FileStorageService fileStorageService;

    @Transactional
    public AuthResponse register(RegisterRequest request, MultipartFile image) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("El correo ya está en uso");
        }

        User user = User.builder()
                .displayName(request.username().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .profileImage(fileStorageService.storeImage(image, FileStorageService.Folder.USERS))
                .build();

        userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(user));
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        // Lanza AuthenticationException (→ 401) si las credenciales no son válidas
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

        User user = userRepository.findByEmail(email).orElseThrow();
        return new AuthResponse(jwtService.generateToken(user));
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
