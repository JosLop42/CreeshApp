# CreeshApp — Guía Completa del Proyecto

> Documentación técnica completa de la aplicación. Cubre concepto, arquitectura, frontend y backend.

---

## ¿Qué es CreeshApp?

CreeshApp es una aplicación móvil Android para descubrir, guardar y compartir recetas de cocina. Los usuarios pueden explorar miles de recetas internacionales, guardar sus favoritas, seguir a cocineros, publicar sus propias recetas y participar en comunidades temáticas.

**Stack tecnológico:**
- **Frontend:** Android (Kotlin), arquitectura MVVM
- **Backend:** Supabase (PostgreSQL + Auth + Storage)
- **APIs externas:** TheMealDB (recetas), RandomUser (cocineros), MyMemory (traducción)

---

## Diagramas

### 1. Arquitectura MVVM

```mermaid
graph TD
    subgraph Vista["Vista (Fragments + XML)"]
        F1[HomeFragment]
        F2[DiscoverFragment]
        F3[FavoritesFragment]
        F4[ProfileFragment]
        F5[UploadRecipeFragment]
        F6[LoginFragment / RegisterFragment]
    end

    subgraph ViewModels
        RV[RecipeViewModel]
        AV[AuthViewModel]
        SV[SocialViewModel]
    end

    subgraph Clientes_API
        SC[SupabaseClient]
        SAC[SupabaseAuthClient]
        SSC[SupabaseStorageClient]
        RC[RetrofitClient - TheMealDB]
        TC[TranslationClient]
        RUC[RandomUserClient]
    end

    subgraph Backend
        SDB[(Supabase DB)]
        SAUTH[Supabase Auth]
        SSTORE[Supabase Storage]
        MEAL[TheMealDB API]
        TRANS[MyMemory API]
        RAND[RandomUser API]
    end

    F1 & F2 & F3 & F4 & F5 --> RV
    F6 --> AV
    F4 --> SV

    RV --> SC --> SDB
    RV --> RC --> MEAL
    RV --> TC --> TRANS
    RV --> SSC --> SSTORE
    AV --> SAC --> SAUTH
    SV --> RUC --> RAND
```

---

### 2. Diagrama Entidad-Relación (Base de datos)

```mermaid
erDiagram
    profiles {
        uuid id PK
        text username
        text full_name
        text bio
        text avatar_url
        text country
        text specialty
        timestamptz created_at
    }
    recipes {
        uuid id PK
        uuid author_id FK
        text title
        text description
        text instructions
        text image_url
        int category_id FK
        int community_id FK
        boolean is_published
        timestamptz created_at
    }
    ingredients {
        serial id PK
        uuid recipe_id FK
        text name
        text quantity
        text unit
        int sort_order
    }
    favorites {
        serial id PK
        text user_id
        text meal_id
        text meal_title
        text meal_image
        text meal_category
        timestamptz created_at
    }
    comments {
        uuid id PK
        uuid user_id FK
        uuid recipe_id FK
        text content
        timestamptz created_at
    }
    communities {
        serial id PK
        text name
        text description
    }
    follows {
        uuid follower_id FK
        uuid following_id FK
        timestamptz created_at
    }

    profiles ||--o{ recipes : "publica"
    profiles ||--o{ comments : "escribe"
    profiles ||--o{ follows : "sigue"
    recipes ||--o{ ingredients : "tiene"
    recipes ||--o{ comments : "recibe"
    communities ||--o{ recipes : "agrupa"
```

---

### 3. Casos de Uso

```mermaid
graph LR
    U((Usuario))

    U --> A[Registrarse]
    U --> B[Iniciar sesión]
    U --> C[Explorar recetas]
    U --> D[Buscar recetas]
    U --> E[Ver detalle de receta]
    U --> F[Guardar favorito]
    U --> G[Seguir cocinero]
    U --> H[Filtrar por comunidad]
    U --> I[Subir receta propia]
    U --> J[Ver mis recetas]
    U --> K[Comentar receta]
    U --> L[Ver perfil]
    U --> M[Editar nombre de perfil]
    U --> N[Cerrar sesión]

    E --> F
    E --> G
    E --> K
```

---

### 4. Flujo de Autenticación

```mermaid
sequenceDiagram
    actor U as Usuario
    participant App
    participant SM as SessionManager
    participant SA as Supabase Auth

    U->>App: Ingresa email y contraseña
    App->>SA: POST /auth/v1/token
    SA-->>App: JWT + Refresh Token + User ID

    App->>SM: saveSession(userId, jwt, email, refreshToken)
    App->>U: Navega al Home

    Note over App,SA: Cada vez que la app vuelve al frente (onResume)
    App->>SA: POST /auth/v1/token (grant_type=refresh_token)
    SA-->>App: Nuevo JWT
    App->>SM: Actualiza token

    Note over App,SA: Si el JWT expiró antes del refresh
    SA-->>App: 401 Unauthorized
    App->>SM: clearSession()
    App->>U: Redirige a Login + "Sesión expirada"
```

---

### 5. Navegación entre Pantallas

```mermaid
graph TD
    LOGIN[Login] --> HOME[Home]
    LOGIN --> REGISTER[Registro]
    REGISTER --> HOME

    HOME --> DISCOVER[Descubrir]
    HOME --> COMMUNITIES[Comunidades]
    HOME --> UPLOAD[Subir Receta]
    HOME --> FAVORITES[Favoritos]

    DISCOVER --> DETAIL[Detalle Receta]
    FAVORITES --> DETAIL
    COMMUNITIES --> DISCOVER

    DETAIL --> CHEF[Perfil Cocinero]
    CHEF --> DETAIL

    HOME --> PROFILE[Mi Perfil]
    PROFILE --> MYDETAIL[Detalle Mi Receta]
    PROFILE --> CHEF

    MYDETAIL --> PROFILE
```

---

## Estructura del Proyecto

```
CreeshApp/
├── app/src/main/java/com/creesh/app/
│   ├── adapters/          # Adaptadores para RecyclerViews
│   │   ├── ChefAdapter
│   │   ├── CommentAdapter
│   │   ├── FavoriteAdapter
│   │   ├── MyRecipeAdapter
│   │   ├── RecipeAdapter / RecipeHorizontalAdapter
│   │   └── CommunityAdapter
│   ├── api/               # Clientes HTTP y APIs
│   │   ├── SupabaseClient          (REST API principal)
│   │   ├── SupabaseAuthClient      (Autenticación)
│   │   ├── SupabaseStorageClient   (Subida de imágenes)
│   │   ├── RetrofitClient          (TheMealDB)
│   │   ├── TranslationClient       (MyMemory)
│   │   ├── RandomUserClient        (Cocineros)
│   │   └── models/                 (Data classes)
│   ├── fragments/         # Pantallas de la app
│   │   ├── LoginFragment / RegisterFragment
│   │   ├── HomeFragment
│   │   ├── DiscoverFragment
│   │   ├── FavoritesFragment
│   │   ├── ProfileFragment
│   │   ├── UploadRecipeFragment
│   │   ├── RecipeDetailFragment
│   │   ├── MyRecipeDetailFragment
│   │   ├── ChefProfileFragment
│   │   └── CommunitiesFragment
│   ├── viewmodel/         # Lógica de negocio (MVVM)
│   │   ├── RecipeViewModel   (recetas, favoritos, traducción)
│   │   ├── AuthViewModel     (login, registro)
│   │   └── SocialViewModel   (cocineros, follows)
│   └── utils/
│       └── SessionManager    (tokens, datos de sesión)
├── app/src/main/res/
│   ├── layout/            # Interfaces XML
│   └── navigation/        # Grafo de navegación
└── database/
    └── schema.sql         # Esquema completo de Supabase
```

---

## Flujo completo de una receta

1. Usuario abre la app → JWT se refresca automáticamente
2. `HomeFragment` carga una receta destacada desde TheMealDB
3. Usuario presiona "Descubrir" → `DiscoverFragment` carga recetas aleatorias
4. Usuario toca una receta → `RecipeDetailFragment` muestra el detalle
5. TheMealDB devuelve el contenido en inglés → `TranslationClient` lo traduce al español
6. Usuario toca ❤️ → `RecipeViewModel.toggleFavorite()` → POST a Supabase `favorites`
7. Favorito queda guardado permanentemente en la base de datos

---

## Seguridad

- Contraseñas manejadas exclusivamente por Supabase Auth (nunca se almacenan en la app)
- JWT guardado en SharedPreferences, nunca en texto visible
- Row Level Security (RLS) en todas las tablas: cada usuario solo accede a sus propios datos
- Timeout de sesión por inactividad (30 minutos)
- Refresh automático del token para mantener la sesión activa
