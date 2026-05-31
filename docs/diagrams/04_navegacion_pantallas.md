# Diagrama 4 — Navegación entre Pantallas

> Muestra todas las pantallas de la app y cómo el usuario navega entre ellas.

## Mapa de navegación completo

```mermaid
graph TD
    classDef auth fill:#ffd6d6,stroke:#cc0000
    classDef main fill:#d6f0ff,stroke:#0066cc
    classDef detail fill:#d6ffd6,stroke:#006600
    classDef social fill:#fff3d6,stroke:#cc8800

    START([App inicia]) --> CHECK{¿Hay sesión\nguardada?}
    CHECK -- Sí --> HOME
    CHECK -- No --> LOGIN

    LOGIN[LoginFragment\nEmail + contraseña]:::auth --> HOME
    LOGIN --> REGISTER[RegisterFragment\nCrear cuenta]:::auth
    REGISTER --> HOME[HomeFragment\nReceta destacada + menú]:::main

    HOME --> DISCOVER[DiscoverFragment\nExplorar y buscar recetas]:::main
    HOME --> FAVORITES[FavoritesFragment\nMis favoritos]:::main
    HOME --> UPLOAD[UploadRecipeFragment\nPublicar receta]:::main
    HOME --> COMMUNITIES[CommunitiesFragment\nComunidades temáticas]:::main
    HOME --> PROFILE[ProfileFragment\nMi perfil]:::main

    DISCOVER --> DETAIL[RecipeDetailFragment\nReceta de TheMealDB\ncon traducción automática]:::detail
    FAVORITES --> DETAIL
    COMMUNITIES --> DISCOVER

    DETAIL --> CHEF[ChefProfileFragment\nPerfil del cocinero]:::social
    CHEF --> DETAIL

    PROFILE --> MYDETAIL[MyRecipeDetailFragment\nMi receta + comentarios]:::detail
    PROFILE --> CHEF

    UPLOAD --> PROFILE
    MYDETAIL --> PROFILE

    HOME -- "Cerrar sesión" --> CONFIRM{¿Confirmar\ncerrar sesión?}
    CONFIRM -- Sí --> LOGIN
    CONFIRM -- No --> HOME
```

## Leyenda de colores

| Color | Tipo de pantalla |
|---|---|
| Rojo | Autenticación (Login / Registro) |
| Azul | Pantallas principales (barra inferior) |
| Verde | Pantallas de detalle |
| Amarillo | Pantallas sociales (cocineros) |

## Todas las pantallas y su función

| Pantalla | ¿Qué hace? | ¿Cómo se llega? |
|---|---|---|
| `LoginFragment` | Inicio de sesión con email y contraseña | Al abrir la app sin sesión |
| `RegisterFragment` | Crear una cuenta nueva | Desde Login |
| `HomeFragment` | Landing: receta destacada + 4 accesos rápidos | Tras login / barra inferior |
| `DiscoverFragment` | Buscar y explorar recetas por nombre o categoría | Desde Home o Comunidades |
| `RecipeDetailFragment` | Ver receta completa, traducida, con cocinero y botón favorito | Desde Discover o Favoritos |
| `FavoritesFragment` | Ver todas las recetas guardadas con ❤️ | Desde Home o barra inferior |
| `UploadRecipeFragment` | Publicar una receta propia con imágenes e ingredientes | Desde Home |
| `ProfileFragment` | Ver mi perfil: stats, mis recetas, cocineros seguidos | Barra inferior |
| `MyRecipeDetailFragment` | Ver el detalle de mi receta, editar, ver comentarios | Desde Mi Perfil |
| `ChefProfileFragment` | Ver el perfil de un cocinero, seguirlo, ver sus recetas | Desde Detalle de receta o Perfil |
| `CommunitiesFragment` | Lista de comunidades para filtrar recetas por tema | Desde Home |

## Navegación técnica

La app usa **Android Navigation Component** con un único `NavGraph`. Toda la navegación ocurre dentro de `MainActivity` usando un `NavHostFragment`.

```
MainActivity
└── NavHostFragment (contenedor)
    ├── nav_graph.xml (define todas las rutas)
    └── BottomNavigationView (Home, Mi Perfil, Salir)
```
