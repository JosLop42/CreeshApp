# Diagrama 1 — Arquitectura MVVM de CreeshApp

> Muestra cómo se organiza la app en capas: Vista, ViewModel y fuentes de datos.

```mermaid
graph TD
    subgraph Vista["VISTA — Fragments (lo que el usuario ve)"]
        F1[HomeFragment]
        F2[DiscoverFragment]
        F3[FavoritesFragment]
        F4[ProfileFragment]
        F5[UploadRecipeFragment]
        F6[RecipeDetailFragment]
        F7[LoginFragment / RegisterFragment]
    end

    subgraph VM["VIEWMODEL — Lógica de negocio"]
        RV[RecipeViewModel\nrecetas · favoritos · traducción · búsqueda]
        AV[AuthViewModel\nlogin · registro]
        SV[SocialViewModel\ncocineros · follows]
    end

    subgraph API["CLIENTES API — Comunicación con servidores"]
        SC[SupabaseClient\nREST API]
        SAC[SupabaseAuthClient\nAutenticación]
        SSC[SupabaseStorageClient\nSubida de imágenes]
        RC[RetrofitClient\nTheMealDB]
        TC[TranslationClient\nMyMemory]
        RUC[RandomUserClient\nCocineros]
    end

    subgraph EXT["SERVICIOS EXTERNOS — Backends en la nube"]
        SDB[(Supabase\nPostgreSQL)]
        SAUTH[Supabase Auth\nJWT]
        SSTORE[Supabase Storage\nImágenes]
        MEAL[TheMealDB\nRecetas internacionales]
        TRANS[MyMemory\nTraducción EN→ES]
        RAND[RandomUser\nCocineros aleatorios]
    end

    F1 & F2 & F3 & F4 & F5 & F6 --> RV
    F7 --> AV
    F4 --> SV

    RV --> SC --> SDB
    RV --> RC --> MEAL
    RV --> TC --> TRANS
    RV --> SSC --> SSTORE
    AV --> SAC --> SAUTH
    SV --> RUC --> RAND
```

## ¿Por qué MVVM?

| Sin MVVM | Con MVVM |
|---|---|
| El Fragment llama directamente a la API | El Fragment solo observa datos |
| Si el teléfono rota, se pierde todo | El ViewModel sobrevive a la rotación |
| Las pantallas son difíciles de reutilizar | La lógica está separada de la UI |
| Imposible hacer pruebas unitarias | El ViewModel se puede probar sin Android |

**Flujo de datos:** Fragment observa LiveData → ViewModel hace la petición → API Client llama al servidor → respuesta actualiza LiveData → Fragment se actualiza automáticamente.
