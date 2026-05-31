# Presentación 3 — Backend y APIs

> **Sección a cargo de:** Base de datos, autenticación, APIs externas y seguridad del servidor.

---

## 1. ¿Qué es el Backend de CreeshApp?

El backend de CreeshApp **no fue construido desde cero**. En lugar de crear un servidor propio, se usó **Supabase** — una plataforma Backend-as-a-Service que ofrece:

- Base de datos **PostgreSQL** (relacional, robusta)
- **Autenticación** lista para usar (email/contraseña, OAuth)
- **Storage** para guardar imágenes
- **API REST automática** para cada tabla de la base de datos
- **Row Level Security** para proteger los datos de cada usuario

Además, la app se conecta a **3 APIs externas** que proveen contenido:

| API | ¿Para qué? | Costo |
|---|---|---|
| TheMealDB | Miles de recetas internacionales con fotos | Gratuita |
| MyMemory | Traducción automática inglés → español | Gratuita (1000 req/día) |
| RandomUser | Perfiles de cocineros con nombre, foto y país | Gratuita |

---

## 2. La Base de Datos (Supabase / PostgreSQL)

La base de datos tiene **7 tablas** principales:

### Tablas y su propósito

| Tabla | ¿Qué guarda? |
|---|---|
| `profiles` | Información del perfil de cada usuario (nombre, bio, país) |
| `recipes` | Recetas que los usuarios publican en la app |
| `ingredients` | Ingredientes de cada receta publicada |
| `favorites` | Recetas externas (TheMealDB) que el usuario guardó |
| `comments` | Comentarios en recetas de otros usuarios |
| `communities` | Las 9 comunidades temáticas de la app |
| `follows` | Relaciones de seguimiento entre usuarios y cocineros |

### Diagrama simplificado de relaciones

```
profiles ──────┬──── recipes ──── ingredients
               │         └──── comments
               ├──── favorites
               └──── follows
                         │
                    communities ──── recipes
```

### Trigger automático al registrarse

Cuando un usuario se registra, Supabase ejecuta automáticamente este proceso:

```
Usuario se registra → Supabase Auth crea registro en auth.users
                    → Trigger dispara función SQL
                    → Se crea registro en tabla "profiles"
                    → Usuario ya tiene perfil listo
```

Esto garantiza que **todo usuario registrado siempre tenga un perfil**.

---

## 3. Autenticación — Cómo funciona el Login

La app usa **JWT (JSON Web Tokens)** para la autenticación. Es el estándar moderno para APIs.

### Flujo simplificado:

```
1. Usuario escribe email y contraseña
2. App envía a Supabase: POST /auth/v1/token
3. Supabase verifica credenciales
4. Supabase responde con:
   - accessToken (JWT válido ~1 hora)
   - refreshToken (para renovar sin re-loguearse)
   - userId (UUID único del usuario)
5. App guarda todo en SharedPreferences (SessionManager)
6. Cada petición a la API lleva el header:
   Authorization: Bearer <accessToken>
```

### Renovación automática del token:

```
Cada vez que la app vuelve al frente (onResume):
→ App envía el refreshToken a Supabase
→ Supabase devuelve un nuevo accessToken
→ App actualiza el token guardado
→ Usuario nunca se da cuenta — sigue sin cerrar sesión
```

### ¿Qué pasa si el token expira sin refresh?

```
Petición con JWT vencido → Supabase responde 401
→ App detecta el 401 en el interceptor de OkHttp
→ clearSession() → redirige a Login
→ Mensaje: "Tu sesión ha expirado"
```

---

## 4. Las APIs Externas

### TheMealDB — Recetas

La principal fuente de contenido de la app. Provee más de **300 recetas** organizadas en categorías.

**Endpoints que usa la app:**

| Endpoint | Función |
|---|---|
| `GET /search.php?s={nombre}` | Buscar receta por nombre |
| `GET /lookup.php?i={id}` | Obtener detalle completo de una receta |
| `GET /random.php` | Receta completamente aleatoria |
| `GET /filter.php?c={categoria}` | Filtrar recetas por categoría (Chicken, Beef…) |
| `GET /categories.php` | Listar todas las categorías disponibles |

**Ejemplo de respuesta:**
```json
{
  "idMeal": "52772",
  "strMeal": "Teriyaki Chicken Casserole",
  "strCategory": "Chicken",
  "strInstructions": "Mix all ingredients...",
  "strMealThumb": "https://www.themealdb.com/images/...",
  "strIngredient1": "soy sauce",
  "strMeasure1": "3/4 cup"
}
```

> Las recetas vienen en **inglés**. Por eso existe la siguiente API.

---

### MyMemory — Traducción Automática

Traduce los títulos e instrucciones de las recetas de inglés a español en tiempo real.

**Cómo funciona en la app:**

```
RecipeDetailFragment abre una receta
→ RecipeViewModel.translateMeal() se llama
→ El texto se divide en fragmentos de 450 caracteres
   (límite de la API gratuita)
→ Cada fragmento se envía: GET /get?q={texto}&langpair=en|es
→ Los fragmentos traducidos se van mostrando progresivamente
→ El usuario ve la traducción aparecer mientras se carga
```

Esto da la experiencia de **traducción en vivo** que se ve en la app.

---

### RandomUser — Cocineros

Genera perfiles de personas realistas para los "cocineros" de la app.

**Petición:**
```
GET https://randomuser.me/api/?results=12&seed=creesh2024
```

Con el parámetro `seed=creesh2024` los mismos 12 cocineros aparecen siempre (resultados determinísticos). Se les asigna una especialidad culinaria basada en su posición en la lista.

---

## 5. Supabase Storage — Imágenes

Cuando un usuario publica una receta con foto, la imagen se sube al storage de Supabase.

**Flujo:**

```
Usuario selecciona foto → App convierte a bytes
→ PUT /storage/v1/object/recipes/{userId}/{timestamp}.jpg
→ Supabase guarda la imagen en el bucket "recipes"
→ Devuelve URL pública
→ App guarda esa URL en la tabla "recipes" (campo image_url)
→ Cualquier usuario puede ver la imagen directamente desde la URL
```

---

## 6. Seguridad — Row Level Security (RLS)

RLS es una función de PostgreSQL que **filtra qué filas puede ver o modificar cada usuario** a nivel de base de datos.

**Ejemplo en la tabla `recipes`:**

```sql
-- Un usuario solo puede VER sus propias recetas
CREATE POLICY "Ver mis recetas"
ON recipes FOR SELECT
USING (auth.uid() = author_id);

-- Un usuario solo puede INSERTAR recetas como autor
CREATE POLICY "Publicar receta"
ON recipes FOR INSERT
WITH CHECK (auth.uid() = author_id);
```

Aunque alguien obtuviera el JWT de otro usuario, **la base de datos misma lo bloquea**.

**Resumen de protecciones:**

| Amenaza | Protección |
|---|---|
| Robo de contraseña | Supabase la hashea, la app nunca la ve |
| Token robado | Expira en ~1 hora; refresh token de un solo uso |
| Acceso a datos ajenos | Row Level Security en todas las tablas |
| Conexiones sin cifrar | HTTPS obligatorio (network_security_config.xml) |
| Inactividad | Timeout de 30 minutos → redirige a Login |

---

## 7. Estructura del Backend en el proyecto

```
CreeshApp/
├── database/
│   └── schema.sql          ← Esquema completo de Supabase (tablas, triggers, RLS, seeds)
│
└── app/src/main/java/com/creesh/app/api/
    ├── SupabaseClient.kt          ← Retrofit para operaciones CRUD en Supabase
    ├── SupabaseAuthClient.kt      ← Retrofit para autenticación (login/register/refresh)
    ├── SupabaseStorageClient.kt   ← Retrofit para subida de imágenes
    ├── SupabaseApi.kt             ← Interfaz con todos los endpoints REST de Supabase
    ├── SupabaseAuthApi.kt         ← Interfaz para endpoints de autenticación
    ├── SupabaseStorageApi.kt      ← Interfaz para endpoints de storage
    ├── RetrofitClient.kt          ← Retrofit para TheMealDB
    ├── MealDbApi.kt               ← Interfaz con endpoints de TheMealDB
    ├── TranslationClient.kt       ← Retrofit para MyMemory
    ├── TranslationApi.kt          ← Interfaz de traducción
    ├── RandomUserClient.kt        ← Retrofit para RandomUser
    └── RandomUserApi.kt           ← Interfaz de cocineros
```

---

## 8. Puntos clave para mencionar

- El backend es **Supabase** — no hay un servidor propio que mantener o desplegar
- La base de datos es **PostgreSQL real** — no un servicio NoSQL simplificado
- La autenticación usa **JWT** — el estándar de la industria para APIs REST
- La seguridad es en **dos capas**: JWT en la app + RLS en la base de datos
- Las recetas de TheMealDB son **datos reales**, no simulados — vienen de una API pública con más de 300 recetas documentadas
- La traducción es **automática y en tiempo real** — no hay textos traducidos hardcodeados
