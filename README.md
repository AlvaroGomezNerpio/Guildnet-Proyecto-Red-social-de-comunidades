# Proyecto: Red social de comunidades

## Introducción

Este proyecto consiste en el desarrollo de una **red social temática** centrada en la creación y gestión de comunidades. El objetivo principal es permitir que los usuarios interactúen de forma organizada según sus intereses, creando espacios personalizados en los que puedan expresarse, compartir contenido y comunicarse con otros usuarios.

La aplicación se basa en un enfoque **modular, dinámico y personalizable**, permitiendo a los usuarios tener un perfil general básico y perfiles independientes dentro de cada comunidad a la que pertenezcan. El contenido publicado en las comunidades tendrá un formato enriquecido tipo blog, con soporte para texto, imágenes y estilos, lo que permite una experiencia más completa y creativa.

## Objetivos

Los objetivos concretos del proyecto son:

- O1: Permitir a los usuarios registrarse, iniciar sesión y gestionar su perfil general básico.
- O2: Permitir crear comunidades temáticas y unirse a ellas mediante un sistema de perfiles personalizados.
- O3: Facilitar la publicación de contenido enriquecido (tipo blog) dentro de cada comunidad.
- O4: Permitir comentar publicaciones y reaccionar con likes.
- O5: Gestionar roles dentro de las comunidades, permitiendo definir permisos personalizados.
- O6: Implementar un sistema de reportes para publicaciones y comentarios.
- O7: Ofrecer un sistema de notificaciones para interacciones y eventos importantes.
- O8: Proporcionar un entorno visual atractivo, funcional y responsive mediante Angular y Quill.js.

## Módulos principales

A continuación se describen los módulos clave de la aplicación:

### 1. Usuarios
- Registro, inicio de sesión y autenticación.
- Perfil general **básico** con datos mínimos: nombre de usuario, correo e imagen de perfil.
- Este perfil se usa únicamente como base para crear perfiles personalizados dentro de cada comunidad.

### 2. Comunidades
- Creación de comunidades temáticas con su propia imagen, banner, descripción y reglas.
- Sistema de búsqueda y filtrado de comunidades.
- Roles personalizados por comunidad (al estilo Discord): cada comunidad puede definir sus propios roles con nombre, color y permisos.

### 3. Perfiles en comunidad
- Cada usuario tiene un perfil distinto en cada comunidad a la que pertenece.
- Personalización independiente: nombre, imagen, descripción enriquecida.
- Sistema de títulos otorgados por moderadores/líderes, con posibilidad de seleccionar uno visible.
- Comentarios en el perfil dentro de la comunidad.

### 4. Publicaciones
- Creación de contenido tipo blog con editor enriquecido Quill.js.
- Inclusión de imágenes, texto formateado, enlaces, títulos, etc.
- Likes y comentarios.
- Posibilidad de edición/eliminación por parte del autor.
- Reportes por contenido indebido.

### 5. Comentarios
- Comentarios en publicaciones y perfiles dentro de las comunidades.
- Reportes y sistema de moderación.
- Comentarios con formato enriquecido limitado.

### 6. Reportes y moderación
- Sistema de reporte con motivos predefinidos.
- Moderadores y líderes reciben notificaciones para gestionar contenido reportado.
- Permite actuar sobre publicaciones o comentarios denunciados.

### 7. Notificaciones
- Notificación automática al recibir:
  - Un comentario o like en tu publicación.
  - Un nuevo título asignado.
  - Un reporte sobre tu contenido.
  - Un aviso de contenido pendiente si eres moderador/líder.

### 8. Chat en comunidades (tiempo real con WebSockets)
- Cada comunidad tendrá un único canal de chat disponible para todos sus miembros.
- El chat se crea automáticamente al crear la comunidad.
- Los mensajes se transmiten en tiempo real mediante **WebSockets**.
- Los usuarios deben estar autenticados y pertenecer a la comunidad para poder enviar o recibir mensajes.
- Los mensajes se almacenan en base de datos y pueden consultarse como historial.
- Se mostrará el nombre y avatar del usuario junto al mensaje.
- Se permite enviar mensajes de texto (en esta fase no se incluirán archivos o multimedia).

## Casos de uso

| Código | Caso de uso                                         |
|--------|-----------------------------------------------------|
| CU1    | Registrarse en la plataforma                        |
| CU2    | Iniciar sesión                                      |
| CU3    | Editar el perfil global del usuario                 |
| CU4    | Crear una comunidad                                 |
| CU5    | Buscar y unirse a una comunidad                     |
| CU6    | Gestionar el perfil dentro de una comunidad         |
| CU7    | Asignar roles y títulos dentro de una comunidad     |
| CU8    | Crear una publicación en una comunidad              |
| CU9    | Editar o eliminar una publicación propia            |
| CU10   | Comentar en una publicación                         |
| CU11   | Dar like a una publicación                          |
| CU12   | Reportar una publicación o comentario               |
| CU13   | Gestionar publicaciones y comentarios reportados    |
| CU14   | Recibir y consultar notificaciones                  |
| CU15   | Usar el chat en tiempo real de una comunidad        |
| CU16   | Eliminar o editar comentarios del perfil de comunidad |

### CU1 – Registrarse en la plataforma
**Descripción:** El usuario accede al formulario de registro, introduce sus datos personales y crea una cuenta en la plataforma.
**Actor principal:** Usuario no autenticado
**Flujo principal:**
1. El usuario abre el formulario de registro.
2. Introduce nombre de usuario, correo electrónico, contraseña y datos mínimos de perfil.
3. El sistema valida que el correo no esté en uso.
4. Si es válido, se crea la cuenta y se inicia sesión automáticamente o se envía correo de verificación (opcional).
**Resultado esperado:** Se crea una cuenta nueva y el usuario accede a la plataforma.

### CU2 – Iniciar sesión
**Descripción:** Un usuario registrado accede a la plataforma introduciendo su correo electrónico y contraseña.
**Actor principal:** Usuario registrado
**Flujo principal:**
1. El usuario accede al formulario de inicio de sesión.
2. Introduce su correo y contraseña.
3. El sistema valida las credenciales.
4. Si son correctas, inicia sesión y accede al panel principal.
**Resultado esperado:** El usuario entra en la plataforma con su cuenta.

### CU3 – Editar el perfil global del usuario
**Descripción:** El usuario modifica los datos básicos de su perfil global: nombre de usuario, correo e imagen de perfil.
**Actor principal:** Usuario autenticado
**Flujo principal:**
1. Accede a su perfil.
2. Pulsa en editar.
3. Cambia los datos básicos.
4. Guarda los cambios.
**Resultado esperado:** Los cambios se reflejan como base para nuevos perfiles en comunidades.

### CU4 – Crear una comunidad
**Descripción:** El usuario crea una nueva comunidad temática y se convierte automáticamente en su líder.
**Actor principal:** Usuario autenticado
**Flujo principal:**
1. Accede a la sección de comunidades.
2. Pulsa en "Crear comunidad".
3. Rellena los datos: nombre, descripción, imagen, banner, reglas.
4. Envía el formulario.
5. Se crea también automáticamente el chat asociado.
**Resultado esperado:** La comunidad aparece en el sistema y el usuario pasa a ser su líder.

### CU5 – Buscar y unirse a una comunidad
**Descripción:** Permite al usuario encontrar comunidades existentes y unirse a ellas.
**Actor principal:** Usuario autenticado
**Flujo principal:**
1. Accede a la sección de búsqueda de comunidades.
2. Usa filtros o buscador.
3. Entra en la comunidad deseada.
4. Pulsa "Unirse".
5. El sistema crea un perfil del usuario dentro de esa comunidad.
**Resultado esperado:** El usuario forma parte de la comunidad y puede interactuar.

### CU6 – Gestionar el perfil dentro de una comunidad
**Descripción:** El usuario puede modificar su perfil personalizado dentro de una comunidad.
**Actor principal:** Usuario autenticado y miembro de una comunidad
**Flujo principal:**
1. Entra a la comunidad.
2. Accede a su perfil.
3. Pulsa en editar.
4. Cambia datos como nombre, imagen, descripción, etc.
5. Guarda los cambios.
**Resultado esperado:** El perfil se actualiza dentro de esa comunidad.

### CU7 – Asignar roles y títulos dentro de una comunidad
**Descripción:** Permite a líderes o moderadores asignar roles y títulos a los miembros.
**Actor principal:** Usuario con rol de líder o moderador
**Flujo principal:**
1. Accede a la configuración de la comunidad.
2. Selecciona un miembro.
3. Asigna o modifica su rol.
4. Añade uno o varios títulos.
**Resultado esperado:** El usuario tiene nuevos permisos y títulos visibles en su perfil y publicaciones.

### CU8 – Crear una publicación en una comunidad
**Descripción:** El usuario crea una publicación tipo blog dentro de una comunidad.
**Actor principal:** Usuario autenticado y miembro de la comunidad
**Flujo principal:**
1. Entra a la comunidad.
2. Pulsa en "Nueva publicación".
3. Escribe contenido con el editor.
4. Publica.
**Resultado esperado:** La publicación aparece en el feed de la comunidad.

### CU9 – Editar o eliminar una publicación propia
**Descripción:** El autor de una publicación puede editarla o eliminarla.
**Actor principal:** Usuario autenticado y autor de la publicación
**Flujo principal:**
1. Localiza su publicación.
2. Pulsa en editar o eliminar.
3. Realiza los cambios o confirma la eliminación.
**Resultado esperado:** La publicación se actualiza o se elimina.

### CU10 – Comentar en una publicación
**Descripción:** Los usuarios pueden comentar publicaciones dentro de una comunidad.
**Actor principal:** Usuario autenticado y miembro de la comunidad
**Flujo principal:**
1. Visualiza una publicación.
2. Escribe un comentario.
3. Envía el comentario.
**Resultado esperado:** El comentario queda visible para otros usuarios.

### CU11 – Dar like a una publicación
**Descripción:** El usuario puede reaccionar positivamente a una publicación.
**Actor principal:** Usuario autenticado y miembro de la comunidad
**Flujo principal:**
1. Visualiza una publicación.
2. Pulsa el botón de like.
3. El sistema registra o elimina el like.
**Resultado esperado:** La publicación muestra el número actualizado de likes.

### CU12 – Reportar una publicación o comentario
**Descripción:** El usuario puede reportar contenido inadecuado.
**Actor principal:** Usuario autenticado y miembro de la comunidad
**Flujo principal:**
1. Visualiza una publicación o comentario.
2. Pulsa en "Reportar".
3. Selecciona un motivo de una lista.
**Resultado esperado:** El contenido queda marcado y se notifica a los moderadores/líderes.

### CU13 – Gestionar publicaciones y comentarios reportados
**Descripción:** Moderadores y líderes pueden revisar y actuar sobre contenido reportado.
**Actor principal:** Moderador o líder
**Flujo principal:**
1. Accede al panel de moderación.
2. Visualiza reportes.
3. Revisa el contenido.
4. Decide eliminarlo o mantenerlo.
**Resultado esperado:** El contenido reportado se gestiona correctamente.

### CU14 – Recibir y consultar notificaciones
**Descripción:** El usuario recibe alertas por interacciones importantes.
**Actor principal:** Usuario autenticado
**Flujo principal:**
1. Accede al sistema o permanece conectado.
2. Recibe notificaciones.
3. Visualiza o marca como leídas.
**Resultado esperado:** El usuario está informado sobre eventos importantes.

### CU15 – Usar el chat en tiempo real de una comunidad
**Descripción:** Permite a los miembros comunicarse mediante un chat general en tiempo real.
**Actor principal:** Usuario autenticado y miembro de la comunidad
**Flujo principal:**
1. Accede a la comunidad.
2. Entra a la sección de chat.
3. Escribe y envía mensajes.
**Resultado esperado:** Los mensajes se envían y reciben en tiempo real mediante WebSockets.

### CU16 – Eliminar o editar comentarios del perfil de comunidad
**Descripción:** Permite a los usuarios eliminar comentarios recibidos o editar los propios dentro de su perfil de comunidad.
**Actor principal:** Usuario autenticado
**Flujo principal:**
1. Accede a su perfil en una comunidad.
2. Localiza el comentario.
3. Pulsa "Eliminar" o "Editar".
**Resultado esperado:** El comentario desaparece o se actualiza.

## Requisitos funcionales

A continuación se listan los requisitos funcionales derivados de los casos de uso del sistema:

1. El sistema debe permitir a los usuarios registrarse con nombre de usuario, correo electrónico y contraseña.
2. El sistema debe permitir a los usuarios iniciar sesión con sus credenciales.
3. El sistema debe permitir editar los datos básicos del perfil global (nombre, correo, imagen).
4. El sistema debe permitir la creación de comunidades con nombre, imagen, banner, descripción y reglas.
5. El sistema debe permitir buscar comunidades y unirse a ellas.
6. El sistema debe crear automáticamente un perfil personalizado para el usuario al unirse a una comunidad.
7. El sistema debe permitir editar el perfil dentro de una comunidad con nombre, imagen y descripción enriquecida.
8. Los líderes o moderadores deben poder asignar roles y títulos a los miembros de su comunidad.
9. Los usuarios deben poder crear publicaciones dentro de comunidades con formato enriquecido.
10. El sistema debe permitir editar o eliminar las publicaciones propias.
11. Los usuarios deben poder comentar publicaciones dentro de las comunidades.
12. Los usuarios deben poder dar like a publicaciones dentro de comunidades.
13. Los usuarios deben poder reportar publicaciones o comentarios por motivos predefinidos.
14. Moderadores y líderes deben poder gestionar el contenido reportado (ver, revisar, eliminar o mantener).
15. El sistema debe generar notificaciones ante likes, comentarios, asignación de títulos o reportes.
16. El sistema debe permitir comunicarse en tiempo real en el chat general de cada comunidad mediante WebSockets.
17. El sistema debe permitir eliminar o editar los comentarios en el perfil de comunidad del usuario.
18. El sistema debe restringir las acciones según el rol y permisos del usuario dentro de cada comunidad.

### Entidades principales

#### Usuario
- id (PK)
- nombre_usuario
- correo (único)
- contraseña (cifrada)
- imagen_perfil

#### Comunidad
- id (PK)
- nombre
- descripcion
- reglas
- imagen
- banner

#### PerfilComunidad
- id (PK)
- id_usuario (FK → Usuario.id)
- id_comunidad (FK → Comunidad.id)
- nombre_personalizado
- imagen_personalizada
- descripcion (enriquecida)
- titulo_destacado_id (FK → Titulo.id, nullable)

#### Rol
- id (PK)
- id_comunidad (FK → Comunidad.id)
- nombre
- color_texto
- color_fondo

#### Permiso
- id (PK)
- nombre

#### RolPermiso
- id_rol (FK → Rol.id)
- id_permiso (FK → Permiso.id)

#### PerfilRol
- id_perfil (FK → PerfilComunidad.id)
- id_rol (FK → Rol.id)

#### Titulo
- id (PK)
- id_comunidad (FK → Comunidad.id)
- nombre
- color_texto
- color_fondo

#### PerfilTitulo
- id_perfil (FK → PerfilComunidad.id)
- id_titulo (FK → Titulo.id)

#### Publicacion
- id (PK)
- id_perfil (FK → PerfilComunidad.id)
- contenido (enriquecido)
- fecha_creacion
- fecha_modificacion

#### ComentarioPublicacion
- id (PK)
- id_publicacion (FK → Publicacion.id)
- id_perfil (FK → PerfilComunidad.id)
- contenido
- fecha

#### ComentarioPerfil
- id (PK)
- id_perfil_destino (FK → PerfilComunidad.id)
- id_perfil_autor (FK → PerfilComunidad.id)
- contenido
- fecha

#### Like
- id (PK)
- id_publicacion (FK → Publicacion.id)
- id_perfil (FK → PerfilComunidad.id)

#### Reporte
- id (PK)
- tipo ("publicacion" | "comentario")
- id_elemento (ID del elemento reportado)
- id_reportador (FK → PerfilComunidad.id)
- motivo (predefinido)
- fecha

#### Notificacion
- id (PK)
- id_usuario (FK → Usuario.id)
- tipo
- mensaje
- leida (boolean)
- fecha

#### MensajeChat
- id (PK)
- id_comunidad (FK → Comunidad.id)
- id_perfil (FK → PerfilComunidad.id)
- contenido
- fecha

## Arquitectura del sistema

La aplicación estará basada en una arquitectura de tipo **cliente-servidor**, separando claramente el frontend, backend y la base de datos. Esto favorece el mantenimiento, escalabilidad y la seguridad de la plataforma.

### 1. Frontend (cliente)
- Desarrollado con **Angular**.
- Interfaz responsive y dinámica.
- Usa **Quill.js** como editor de texto enriquecido.
- Comunicación con el backend a través de **HTTP REST** y **WebSockets**.
- Manejo de sesiones y autenticación con tokens JWT.
- Componentes principales:
  - Registro / Login
  - Home (descubrimiento de comunidades)
  - Vista de comunidad
  - Editor de publicaciones
  - Perfil de comunidad
  - Notificaciones y chat

### 2. Backend (servidor)
- Desarrollado con **Spring Boot**.
- Provee API RESTful para todas las operaciones (usuarios, comunidades, publicaciones, etc.).
- Control de acceso mediante roles y permisos.
- Seguridad con **Spring Security** y JWT.
- WebSockets integrados para el chat en tiempo real.
- Validación de datos y control de errores centralizado.
- Servicios principales:
  - Gestión de usuarios y perfiles
  - Gestión de comunidades y roles
  - Publicaciones, comentarios, likes, reportes
  - Notificaciones y mensajes de chat

### 3. Base de datos
- Utiliza **MySQL** o **PostgreSQL** como sistema gestor.
- Modelo relacional basado en las entidades descritas anteriormente.
- Relación clara entre usuarios, comunidades, publicaciones y reacciones.
- Índices en campos clave para mejorar el rendimiento.

### 4. Comunicación y flujo
- El cliente (Angular) realiza peticiones HTTP al backend para cargar y modificar datos.
- El backend responde con JSON estructurado.
- Para el chat, se abre un canal WebSocket entre cliente y servidor.
- Se aplican controles de autenticación y autorización en cada capa.

## API web

Todas las URIs tendrán el prefijo común `/api/v1`.

### Recursos principales

| Recurso                      | URI                                 |
|------------------------------|--------------------------------------|
| Registro de usuario          | `/auth/register`                     |
| Inicio de sesión             | `/auth/login`                        |
| Usuarios                     | `/usuarios`                          |
| Comunidades                  | `/comunidades`                       |
| Comunidad individual         | `/comunidades/{id}`                  |
| Perfiles de comunidad        | `/comunidades/{id}/perfiles`         |
| Publicaciones                | `/comunidades/{id}/publicaciones`    |
| Publicación individual       | `/publicaciones/{id}`                |
| Comentarios de publicación   | `/publicaciones/{id}/comentarios`    |
| Comentarios de perfil        | `/perfiles/{id}/comentarios`         |
| Likes                        | `/publicaciones/{id}/likes`          |
| Roles                        | `/comunidades/{id}/roles`            |
| Títulos                      | `/comunidades/{id}/titulos`          |
| Reportes                     | `/reportes`                          |
| Notificaciones               | `/notificaciones`                    |
| Chat de comunidad            | `/comunidades/{id}/chat`             |

La representación de todos los recursos será en formato **JSON**.


#### Registro de usuario
- **Método:** `POST`  
- **URI:** `/api/v1/auth/register`  
- **Cuerpo:**
```json
{
  "nombreUsuario": "ejemplo",
  "correo": "correo@ejemplo.com",
  "password": "secreta"
}
```
- **Respuestas:**
  - `201 Created`: Usuario registrado correctamente.
  - `400 Bad Request`: Datos inválidos o faltantes.
  - `409 Conflict`: El correo ya está en uso.

---

#### Inicio de sesión
- **Método:** `POST`  
- **URI:** `/api/v1/auth/login`  
- **Cuerpo:**
```json
{
  "correo": "correo@ejemplo.com",
  "password": "secreta"
}
```
- **Respuestas:**
  - `200 OK`: Devuelve token JWT y datos básicos del usuario.
  - `401 Unauthorized`: Credenciales inválidas.

---

#### Crear comunidad
- **Método:** `POST`  
- **URI:** `/api/v1/comunidades`  
- **Cuerpo:**
```json
{
  "nombre": "Comunidad X",
  "descripcion": "Una comunidad temática",
  "reglas": "Reglas claras",
  "imagen": "url_imagen",
  "banner": "url_banner"
}
```
- **Respuestas:**
  - `201 Created`: Comunidad creada correctamente.
  - `400 Bad Request`: Datos inválidos.

---

#### Obtener comunidades
- **Método:** `GET`  
- **URI:** `/api/v1/comunidades`  
- **Respuestas:**
  - `200 OK`: Lista de comunidades disponibles.

---

#### Unirse a una comunidad
- **Método:** `POST`  
- **URI:** `/api/v1/comunidades/{id}/unirse`  
- **Respuestas:**
  - `201 Created`: Perfil en la comunidad creado.
  - `400 Bad Request`: Ya eres miembro o error de datos.

---

#### Crear publicación
- **Método:** `POST`  
- **URI:** `/api/v1/comunidades/{id}/publicaciones`  
- **Cuerpo:**
```json
{
  "contenido": "<p>Texto enriquecido con Quill</p>"
}
```
- **Respuestas:**
  - `201 Created`: Publicación creada.
  - `400 Bad Request`: Contenido inválido.

---

#### Obtener publicaciones de una comunidad
- **Método:** `GET`  
- **URI:** `/api/v1/comunidades/{id}/publicaciones`  
- **Respuestas:**
  - `200 OK`: Lista de publicaciones.

---

#### Comentar en publicación
- **Método:** `POST`  
- **URI:** `/api/v1/publicaciones/{id}/comentarios`  
- **Cuerpo:**
```json
{
  "contenido": "Buen post!"
}
```
- **Respuestas:**
  - `201 Created`: Comentario añadido.
  - `400 Bad Request`: Comentario vacío o inválido.

---

#### Dar like a una publicación
- **Método:** `POST`  
- **URI:** `/api/v1/publicaciones/{id}/likes`  
- **Respuestas:**
  - `200 OK`: Like añadido o removido.

---

#### Reportar publicación o comentario
- **Método:** `POST`  
- **URI:** `/api/v1/reportes`  
- **Cuerpo:**
```json
{
  "tipo": "publicacion",
  "idElemento": 123,
  "motivo": "Contenido ofensivo"
}
```
- **Respuestas:**
  - `201 Created`: Reporte enviado.
  - `400 Bad Request`: Motivo no válido o faltan campos.

---

#### Obtener notificaciones
- **Método:** `GET`  
- **URI:** `/api/v1/notificaciones`  
- **Respuestas:**
  - `200 OK`: Lista de notificaciones del usuario.

---

#### Enviar mensaje al chat de comunidad (WebSocket)
- **Método:** `POST` (por WebSocket)  
- **Canal WebSocket:** `/ws/comunidades/{id}/chat`  
- **Cuerpo del mensaje:**
```json
{
  "contenido": "Hola a todos!"
}
```
- **Respuesta:** Se emite a todos los usuarios conectados a esa comunidad en tiempo real.

---

### Gestión de roles

#### Crear rol personalizado
- **Método:** `POST`  
- **URI:** `/api/v1/comunidades/{id}/roles`  
- **Cuerpo:**
```json
{
  "nombre": "Moderador",
  "colorTexto": "#ffffff",
  "colorFondo": "#007bff",
  "permisos": ["moderar_comentarios", "asignar_titulos"]
}
```
- **Respuestas:**
  - `201 Created`: Rol creado exitosamente.
  - `400 Bad Request`: Datos incompletos o inválidos.

---

#### Listar roles de una comunidad
- **Método:** `GET`  
- **URI:** `/api/v1/comunidades/{id}/roles`  
- **Respuestas:**
  - `200 OK`: Lista de roles de la comunidad.

---

#### Asignar rol a un miembro
- **Método:** `POST`  
- **URI:** `/api/v1/comunidades/{id}/roles/asignar`  
- **Cuerpo:**
```json
{
  "idPerfil": 123,
  "idRol": 5
}
```
- **Respuestas:**
  - `200 OK`: Rol asignado correctamente.
  - `400 Bad Request`: Error de datos o falta de permisos.

---

### Gestión de títulos

#### Crear título personalizado
- **Método:** `POST`  
- **URI:** `/api/v1/comunidades/{id}/titulos`  
- **Cuerpo:**
```json
{
  "nombre": "Veterano",
  "colorTexto": "#000",
  "colorFondo": "#ffc107"
}
```
- **Respuestas:**
  - `201 Created`: Título creado correctamente.
  - `400 Bad Request`: Error en los datos.

---

#### Listar títulos de la comunidad
- **Método:** `GET`  
- **URI:** `/api/v1/comunidades/{id}/titulos`  
- **Respuestas:**
  - `200 OK`: Lista de títulos disponibles en la comunidad.

---

#### Asignar título a un miembro
- **Método:** `POST`  
- **URI:** `/api/v1/comunidades/{id}/titulos/asignar`  
- **Cuerpo:**
```json
{
  "idPerfil": 123,
  "idTitulo": 4
}
```
- **Respuestas:**
  - `200 OK`: Título asignado correctamente.
  - `400 Bad Request`: Datos inválidos o falta de permisos.



## Requisitos no funcionales

1. El sistema debe ser accesible desde navegadores modernos (Chrome, Firefox, Edge).
2. La interfaz debe ser responsive y adaptarse correctamente a dispositivos móviles y de escritorio.
3. La aplicación debe estar desarrollada con Angular en el frontend y Spring Boot en el backend.
4. Las contraseñas deben almacenarse cifradas de forma segura.
5. Solo los usuarios autenticados deben poder acceder a funcionalidades protegidas.
6. Los roles y permisos deben validarse en cada operación sensible.
7. Las acciones del usuario deben ejecutarse con una latencia inferior a 2 segundos en condiciones normales.
8. El sistema debe permitir escalar horizontalmente si el número de usuarios aumenta.
9. El código debe estar modularizado para facilitar el mantenimiento y la evolución del sistema.
10. Se debe proporcionar una documentación técnica básica para facilitar futuras ampliaciones.
