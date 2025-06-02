package com.guildnet.backend.features.permission;

public enum PermissionType {
    EDIT_POST,         // Poder editar post de otros usuarios
    DELETE_POST,       // Poder borrar cualquier post
    EDIT_COMMENTS,     // Poder editar comentarios de otros usuarios
    DELETE_COMMENTS,   // Poder borrar cualquier comentario
    ASSIGN_ROLES,      // Poder crear y asignar roles a usuarios
    ASSIGN_TITLES,     // Poder crear y asignar títulos a usuarios
    BAN_USER           // Poder eliminar usuarios de la comunidad
}

