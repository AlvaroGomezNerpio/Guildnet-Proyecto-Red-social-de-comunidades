# 🛠️ Instrucciones para ejecutar Guildnet (backend + frontend)

Este documento explica cómo ejecutar localmente los dos proyectos que forman Guildnet: el **backend** (Spring Boot) y el **frontend** (Angular).

---

## 🔙 Backend (Spring Boot)

### 📁 Ubicación: `backend/`

### ✅ Requisitos previos:
- Java 17 o superior
- Maven
- MySQL o PostgreSQL (según el proyecto)
- IntelliJ IDEA (recomendado)

### 🚀 Pasos para ejecutar:

1. Abre IntelliJ IDEA y selecciona la carpeta `backend/` como proyecto.
2. Configura la conexión a la base de datos en `src/main/resources/application.properties`.
3. Ejecuta la aplicación desde la clase principal `BackendApplication`.
4. El backend se iniciará normalmente en `http://localhost:8080`.

---

## 🔮 Frontend (Angular)

### 📁 Ubicación: `frontend/`

### ✅ Requisitos previos:
- Node.js (versión 18 o superior recomendada)
- Angular CLI (`npm install -g @angular/cli`)
- VS Code (opcional)

### 🚀 Pasos para ejecutar:

```bash
cd frontend
npm install      # Instala dependencias
ng serve         # Ejecuta el servidor de desarrollo
```

5. Abre el navegador en: [http://localhost:4200](http://localhost:4200)

---

## 📡 Comunicación backend ↔ frontend

- El frontend se conecta al backend mediante HTTP usando las rutas definidas en `/api/v1/...`.
- Asegúrate de que los puertos no entren en conflicto.
- Si es necesario, usa un proxy Angular (`proxy.conf.json`) para evitar CORS durante el desarrollo.

---

## ⚠️ Notas

- Los tokens de sesión se manejan con JWT.
- Si haces cambios en el modelo de base de datos, recuerda aplicar migraciones si las usas.
- El backend debe estar corriendo antes de probar funcionalidades que hagan peticiones desde Angular.
