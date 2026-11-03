/** Configuración para ejecutar GuildNet en local. */
export const environment = {
  production: false,
  /** URL del backend (Spring Boot). */
  apiUrl: 'http://localhost:8080',
  /** Endpoint WebSocket (STOMP) del chat. */
  wsUrl: 'ws://localhost:8080/ws',
};
