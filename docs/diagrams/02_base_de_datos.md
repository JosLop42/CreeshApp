# Diagrama 2 — Base de Datos (Entidad-Relación)

> Muestra las 7 tablas de Supabase (PostgreSQL) y cómo se relacionan entre sí.

```mermaid
erDiagram
    profiles {
        uuid id PK "ID del usuario (mismo que auth.users)"
        text username "Nombre único de usuario"
        text full_name "Nombre completo"
        text bio "Descripción del perfil"
        text avatar_url "URL de foto de perfil"
        text country "País"
        text specialty "Especialidad culinaria"
        timestamptz created_at "Fecha de registro"
    }

    recipes {
        uuid id PK "ID único de la receta"
        uuid author_id FK "Quién la publicó"
        text title "Título en inglés"
        text title_es "Título traducido"
        text description "Descripción"
        text instructions "Pasos en inglés"
        text instructions_es "Pasos traducidos"
        text image_url "URL de imagen en Storage"
        int category_id FK "Categoría"
        text difficulty "facil / medio / dificil"
        boolean is_published "¿Está publicada?"
        timestamptz created_at "Fecha de publicación"
    }

    ingredients {
        serial id PK
        uuid recipe_id FK "Receta a la que pertenece"
        text name "Nombre del ingrediente"
        text quantity "Cantidad"
        text unit "Unidad (g, ml, taza...)"
        int sort_order "Orden en la lista"
    }

    favorites {
        serial id PK
        text user_id "UUID del usuario"
        text meal_id "ID de TheMealDB"
        text meal_title "Título de la receta"
        text meal_image "URL de imagen"
        text meal_category "Categoría"
        timestamptz created_at "Cuándo se guardó"
    }

    comments {
        uuid id PK
        uuid user_id FK "Quién comentó"
        uuid recipe_id FK "En qué receta"
        text content "Texto del comentario"
        timestamptz created_at "Cuándo se publicó"
    }

    communities {
        serial id PK
        text name "Nombre (ej. Vegano)"
        text description "Descripción de la comunidad"
    }

    follows {
        uuid follower_id FK "Quien sigue"
        uuid following_id FK "A quien sigue"
        timestamptz created_at "Cuándo empezó a seguir"
    }

    profiles ||--o{ recipes : "publica"
    profiles ||--o{ comments : "escribe"
    profiles ||--o{ follows : "es seguidor en"
    profiles ||--o{ follows : "es seguido en"
    recipes ||--o{ ingredients : "tiene"
    recipes ||--o{ comments : "recibe"
    communities ||--o{ recipes : "agrupa"
```

## Notas importantes

- **`profiles`** se crea automáticamente cuando un usuario se registra (via trigger en Supabase)
- **`favorites`** guarda recetas de **TheMealDB** (externas), no recetas propias
- **`recipes`** son las recetas que los **usuarios publican** en la app
- **Row Level Security (RLS):** cada usuario solo puede ver y modificar sus propios registros
- La tabla **`auth.users`** es gestionada automáticamente por Supabase Auth — no aparece en el diagrama porque es interna

## Comunidades predefinidas

| ID | Nombre |
|---|---|
| 1 | Gym Rats |
| 2 | Vegano |
| 3 | Vegetariano |
| 4 | Amantes de la Carne |
| 5 | Mariscos |
| 6 | Postres y Dulces |
| 7 | Pasta e Italiana |
| 8 | Cocina Asiática |
| 9 | Variado |
