# ErasUsersService: guía para agentes

Servicio de autenticación y usuarios de ERAS: Spring Boot 4.0.3 en Java 21, con Spring Security y JWT stateless (jjwt), JPA y H2. El código está en `users_service/` y el detalle en `README`.

- En producción, `https://www.erastcg.com/api/auth/*` llega a este servicio a través de nginx en el servidor. El resto de `/api` va a DriveManager.
- El front guarda el JWT en `localStorage` y lo envía como bearer token. No hay cookies, y CSRF está desactivado.
- CORS: los orígenes configurados más `cors.app-origins` (`CORS_APP_ORIGINS`, por defecto `https://localhost`, el origen de la app Android de Capacitor).

## Compilar y probar

```bash
cd users_service && ./mvnw -q test
```

`contextLoads` falla en local por variables de entorno que faltan; en el CI pasa.

## Despliegue

- Rama → PR a **`develop`** (despliega staging) → PR `develop` → **`main`** (despliega producción). Producción **espera la aprobación del dueño**; un agente no puede aprobar.
- El CI publica la imagen en GHCR y despliega con `deploy-aws.yml` del repo infra.

## Repos relacionados

- **DriveManager**: el servidor del juego y el motor.
- **eras-web**: el front, la PWA y la app Android.
- **infra**: AWS.
