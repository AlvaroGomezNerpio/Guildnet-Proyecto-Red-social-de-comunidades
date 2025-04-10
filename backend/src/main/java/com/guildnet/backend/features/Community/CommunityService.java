package com.guildnet.backend.features.Community;

import com.guildnet.backend.features.Community.dto.CommunityResponse;
import com.guildnet.backend.features.Community.dto.CreateCommunityRequest;
import com.guildnet.backend.features.Community.dto.UpdateCommunityRequest;
import com.guildnet.backend.features.user.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface CommunityService {

    // Crear una nueva comunidad a partir de una petición, su creador y archivos de imagen opcionales
    CommunityResponse createCommunity(CreateCommunityRequest request, User owner, MultipartFile imageFile, MultipartFile bannerFile);

    // Obtener todas las comunidades
    List<Community> getAllCommunities();

    // Obtener una comunidad por su ID
    Optional<Community> getCommunityById(Long id);

    // Buscar comunidades por nombre
    List<Community> searchCommunitiesByName(String name);

    // Buscar comunidades por una sola etiqueta (texto parcial incluido)
    List<Community> searchCommunitiesByTag(String tag);

    // Buscar comunidades por múltiples etiquetas (texto parcial incluido)
    List<Community> searchByTags(List<String> tags);

    // Buscar comunidades por nombre + lista de etiquetas (texto parcial incluido)
    List<Community> searchByNameAndTags(String name, List<String> tags);

    // Actualizar comunidad por ID, incluyendo datos e imágenes
    Optional<Community> updateCommunity(Long id, UpdateCommunityRequest request, MultipartFile imageFile, MultipartFile bannerFile);

    // Eliminar comunidad
    void deleteCommunity(Long id);

    // Guardar imagen en disco y devolver la URL
    String saveImage(MultipartFile file) throws IOException;
}


