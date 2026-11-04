package com.guildnet.backend.features.title;

/** Actividad que se mide para asignar un título automático. */
public enum TitleMetric {
    /** Publicaciones escritas en la comunidad. */
    POSTS,
    /** Comentarios escritos en publicaciones de la comunidad. */
    COMMENTS,
    /** "Me gusta" recibidos en sus publicaciones. */
    LIKES_RECEIVED,
    /** Días desde que se unió a la comunidad. */
    DAYS_MEMBER
}
