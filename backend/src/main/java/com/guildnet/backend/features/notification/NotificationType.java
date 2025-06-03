package com.guildnet.backend.features.notification;

public enum NotificationType {
    // Comentarios
    COMMENT_PROFILE,         // Comentaron en tu perfil de la comunidad
    COMMENT_POST,            // Comentaron en un post tuyo
    // Reacciones
    LIKE_POST,               // Dieron like a un post tuyo
    // Sistema y comunidad
    SYSTEM,                  // Se actualizaron reglas o descripción de la comunidad (para todos los miembros)
    WELCOME,                 // Mensaje de bienvenida al unirse a una comunidad
    ACHIEVEMENT_UNLOCKED,    // Tu publicación ha recibido más de 10 likes
    // Títulos
    TITLE_ASSIGNED,          // Se te ha asignado un título
    TITLE_REMOVED,           // Se te ha eliminado un título
    // Roles
    ROLE_ASSIGNED,           // Se te ha asignado un rol
    ROLE_REMOVED             // Se te ha eliminado un rol
}

