# Diagrama 3 — Flujo de Autenticación

> Muestra cómo funciona el login, el registro, la renovación automática del token y el cierre de sesión.

## Flujo completo

```mermaid
sequenceDiagram
    actor U as Usuario
    participant App as App (Android)
    participant SM as SessionManager
    participant SA as Supabase Auth

    rect rgb(230, 245, 255)
        Note over U,SA: REGISTRO (primera vez)
        U->>App: Ingresa email + contraseña
        App->>SA: POST /auth/v1/signup
        SA-->>App: JWT + Refresh Token + User ID
        App->>SM: saveSession(userId, jwt, email, refreshToken)
        SA-->>SA: Trigger crea perfil en tabla profiles
        App->>U: Navega al Home
    end

    rect rgb(230, 255, 230)
        Note over U,SA: LOGIN (ya tiene cuenta)
        U->>App: Email + contraseña
        App->>SA: POST /auth/v1/token?grant_type=password
        SA-->>App: JWT + Refresh Token
        App->>SM: saveSession(...)
        App->>U: Navega al Home
    end

    rect rgb(255, 250, 230)
        Note over App,SA: REFRESH AUTOMÁTICO (onResume de la app)
        App->>SM: getRefreshToken()
        SM-->>App: refreshToken guardado
        App->>SA: POST /auth/v1/token?grant_type=refresh_token
        SA-->>App: Nuevo JWT válido
        App->>SM: Actualiza el token
    end

    rect rgb(255, 235, 235)
        Note over App,SA: TOKEN EXPIRADO (sin refresh a tiempo)
        App->>SA: Cualquier petición con JWT vencido
        SA-->>App: 401 Unauthorized
        App->>SM: clearSession()
        App->>U: Redirige a Login + mensaje "Sesión expirada"
    end

    rect rgb(245, 235, 255)
        Note over U,SA: LOGOUT MANUAL
        U->>App: Toca "Salir" en el menú
        App->>U: AlertDialog de confirmación
        U->>App: Confirma
        App->>SM: clearSession()
        App->>U: Redirige a Login
    end
```

## ¿Qué guarda SessionManager?

```
SharedPreferences "creesh_session"
├── user_id          → UUID del usuario en Supabase
├── token            → JWT actual (expira en ~1 hora)
├── email            → Email del usuario
├── refresh_token    → Para renovar el JWT sin re-loguearse
├── display_name     → Nombre visible del usuario
└── followed_chefs   → IDs de cocineros seguidos (separados por coma)
```

## Seguridad

| Riesgo | Solución implementada |
|---|---|
| Contraseña expuesta | Nunca se almacena — Supabase Auth la maneja |
| Token robado | JWT de corta duración (~1h) + refresh token |
| Inactividad | Timeout de 30 minutos — redirige a Login |
| Acceso no autorizado | Row Level Security en todas las tablas |
| Man-in-the-middle | HTTPS obligatorio en toda la app |
