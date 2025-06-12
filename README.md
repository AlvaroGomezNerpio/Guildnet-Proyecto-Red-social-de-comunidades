# Proyecto: Guildnet - Red social de comunidades temáticas

## Introducción

Guildnet es una **red social temática** centrada en la creación, gestión e interacción dentro de comunidades. El proyecto busca fomentar espacios personalizados donde los usuarios puedan expresarse, compartir contenido, participar en conversaciones y colaborar en entornos que reflejen sus intereses.

La aplicación permite a cada usuario tener un perfil básico global y perfiles independientes por comunidad, con funcionalidades similares a plataformas como Discord o Reddit, pero con un enfoque propio, más visual y blog-like.

## Objetivos del proyecto

* Registro y autenticación de usuarios.
* Creación y unión a comunidades temáticas.
* Gestión de publicaciones enriquecidas.
* Comentarios y reacciones (likes).
* Gestión de perfiles personalizados en cada comunidad.
* Sistema de roles, permisos y títulos.
* Notificaciones de interacción.
* Chat en tiempo real con WebSockets.

## Tecnologías utilizadas

* **Frontend**: Angular + TypeScript + Quill.js (editor)
* **Backend**: Spring Boot + Java + WebSocket
* **Base de datos**: MySQL
* **Autenticación**: JWT + Spring Security
* **Control de versiones**: Git + GitHub

Repositorio del proyecto: [https://github.com/usuario/guildnet](https://github.com/usuario/guildnet)

## Módulos funcionales

### 1. Usuarios

* Registro / inicio de sesión
* Perfil global con username, correo e imagen

### 2. Comunidades

* Creación con imagen, banner, reglas y descripción
* Filtros y buscador por nombre o tags
* Sistema de roles y permisos personalizados

### 3. Perfiles dentro de comunidades

* Nombre, imagen y descripción enriquecida por comunidad
* Asignación de roles y títulos destacados
* Comentarios en el perfil dentro de la comunidad

### 4. Publicaciones

* Editor enriquecido (Quill)
* Comentarios y likes
* Edición y eliminación

### 5. Notificaciones

* Al recibir comentarios, likes, títulos o reportes
* Visualización y marcado como leídas

### 6. Chat en tiempo real

* Canal general por comunidad
* Envió y recepción mediante WebSocket
* Historial de mensajes guardado en base de datos

## Casos de uso destacados

| Código | Descripción                                |
| ------ | ------------------------------------------ |
| CU1    | Registro e inicio de sesión                |
| CU2    | Crear/editar perfil global                 |
| CU3    | Crear y unirse a comunidades               |
| CU4    | Crear y gestionar publicaciones            |
| CU5    | Comentar, dar like, recibir notificaciones |
| CU6    | Chat en tiempo real por comunidad          |
| CU7    | Gestión de roles, títulos y permisos       |
| CU8    | Editar o eliminar comentarios del perfil   |

## Base de datos (resumen validado)

El modelo relacional se compone de las siguientes entidades principales:

* **users**: Datos básicos y de acceso.
* **communities**: Información de la comunidad.
* **community\_profiles**: Perfil del usuario dentro de cada comunidad.
* **roles / permissions / role\_permissions**: Control de accesos.
* **titles / profile\_titles**: Títulos asignables por comunidad.
* **posts / post\_comments / likes**: Publicaciones y reacciones.
* **chat\_messages**: Chat en tiempo real.
* **notifications**: Sistema de notificaciones.
* **reports**: Reportes de contenido.

Las relaciones entre tablas están correctamente normalizadas y verificadas.

## Arquitectura del sistema

Arquitectura cliente-servidor dividida en tres capas:

### Frontend (Angular)

* SPA responsive
* Comunicación REST + WebSocket
* Manejo de token JWT

### Backend (Spring Boot)

* API RESTful segura
* Control de roles y permisos
* WebSocket para chat

### Base de datos (MySQL)

* Modelo relacional validado
* Datos cifrados y relaciones por claves foráneas

## Requisitos no funcionales

* Interfaz responsive
* Seguridad en contraseñas y operaciones
* Modularidad y escalabilidad
* Carga rápida (<2s por acción en condiciones normales)

## Desarrollo

* El proyecto se ha desarrollado en **GitHub**, con control de versiones, issues y ramas organizadas.
* Se ha seguido una metodología iterativa incremental.
* Las pruebas se han realizado mediante JUnit, MockMvc y Postman.

## Conclusiones y vías futuras

El proyecto cumple los objetivos planteados, con una plataforma funcional, segura y modular.

### Vías futuras

* Moderación automática mediante bot contra contenido inadecuado (insultos, lenguaje ofensivo, etc.).
* Integración de subida de multimedia en chat.
* Sistema de recompensas/gamificación.
* Aplicación móvil nativa.
