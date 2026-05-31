# Presentación 3 — Backend y APIs

> **Sección a cargo de:** Cómo la app se comunica con los servidores, la base de datos, autenticación con JWT, Retrofit, y las APIs externas.

---

## 1. ¿Qué es el Backend de CreeshApp?

Una app Android no guarda datos directamente en el teléfono para que sean visibles por otros usuarios. Necesita un servidor que actúe como centro de datos: guarda los favoritos, las recetas publicadas, los comentarios, y autentica a los usuarios.

CreeshApp usa **Supabase** como backend. En lugar de construir un servidor desde cero en Node.js o Python, Supabase es una plataforma que entrega todo listo:

| Qué necesita la app | Qué da Supabase |
|---|---|
| Base de datos relacional | PostgreSQL completo |
| Login y registro | Supabase Auth con JWT |
| Guardar imágenes | Supabase Storage |
| Endpoints para leer y escribir datos | API REST automática por tabla |
| Control de acceso por usuario | Row Level Security (RLS) |

Además, la app consume **3 APIs externas** de terceros:

| API | Para qué | Gratuita |
|---|---|---|
| TheMealDB | Más de 300 recetas reales con fotos e instrucciones | Sí |
| MyMemory | Traducción automática inglés → español | Sí (1000 req/día) |
| RandomUser | Perfiles realistas de cocineros (foto, nombre, país) | Sí |

---

## 2. Retrofit — Cómo la app hace llamadas HTTP

`Retrofit` es la librería que convierte las llamadas HTTP en funciones de Kotlin. Sin ella, habría que escribir todo el código de red manualmente (crear la URL, abrir la conexión, leer el stream de bytes, parsear el JSON...).

### Cómo está configurado en la app

El `RetrofitClient` para TheMealDB se ve así en el código real:

```kotlin
object RetrofitClient {

    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)   // máximo 8s para conectar
        .readTimeout(10, TimeUnit.SECONDS)     // máximo 10s para recibir respuesta
        .writeTimeout(8, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)    // loguea todas las peticiones en Logcat
        .build()

    val api: MealDbApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())  // JSON → Kotlin automático
            .build()
            .create(MealDbApi::class.java)
    }
}
```

### La interfaz de endpoints

Cada endpoint es una función en una interfaz. Retrofit genera automáticamente la implementación:

```kotlin
interface MealDbApi {
    @GET("search.php")
    suspend fun searchMeals(@Query("s") name: String): MealResponse

    @GET("lookup.php")
    suspend fun getMealById(@Query("i") id: String): MealResponse

    @GET("filter.php")
    suspend fun getMealsByCategory(@Query("c") category: String): MealResponse

    @GET("search.php")
    suspend fun getMealsByLetter(@Query("f") letter: String): MealResponse
}
```

Cuando el ViewModel llama `api.searchMeals("pasta")`, Retrofit construye la URL `https://www.themealdb.com/api/json/v1/1/search.php?s=pasta`, hace la petición GET, recibe el JSON y lo convierte a un objeto `MealResponse` de Kotlin usando Gson. Todo automático.

### Gson — Conversión automática de JSON a Kotlin

Gson mapea el JSON de la API a las data classes de Kotlin:

```
JSON que llega de TheMealDB:
{
    "idMeal": "52772",
    "strMeal": "Teriyaki Chicken Casserole",
    "strCategory": "Chicken",
    "strInstructions": "Mix all ingredients...",
    "strMealThumb": "https://www.themealdb.com/images/..."
}

↓ Gson convierte automáticamente ↓

data class Meal(
    @SerializedName("idMeal")         val id: String,
    @SerializedName("strMeal")        val name: String,
    @SerializedName("strCategory")    val category: String?,
    @SerializedName("strInstructions") val instructions: String?,
    @SerializedName("strMealThumb")   val thumbnail: String?
)
```

---

## 3. OkHttp Interceptors — Headers automáticos

`OkHttp` es la capa de bajo nivel que hace las conexiones HTTP. Los **interceptors** son funciones que se ejecutan en cada petición antes de enviarla o en cada respuesta antes de procesarla.

El `SupabaseClient` usa un interceptor para agregar headers automáticamente a todas las peticiones:

```kotlin
private val httpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val token = SessionManager.getToken()  // JWT del usuario actual
        val builder = chain.request().newBuilder()
            .addHeader("apikey", KEY)                       // clave pública de Supabase
            .addHeader("Content-Type", "application/json")
        if (token != null) {
            builder.addHeader("Authorization", "Bearer $token")  // autentica al usuario
        }
        val response = chain.proceed(builder.build())  // ejecuta la petición
        
        // Si Supabase responde 401 (token inválido), cierra la sesión
        if (response.code == 401 && SessionManager.isLoggedIn()) {
            SessionManager.clearSession()
            SessionManager.onSessionExpired?.invoke()  // redirige a Login
        }
        response
    }
    .build()
```

**Esto significa:** ningún Fragment ni ViewModel tiene que preocuparse por agregar el token de autorización. El interceptor lo agrega automáticamente a cada petición que pasa por ese cliente.

---

## 4. La Base de Datos — PostgreSQL en Supabase

PostgreSQL es una de las bases de datos relacionales más usadas en el mundo. Supabase la expone con una API REST automática: cada tabla tiene endpoints GET, POST, PATCH y DELETE listos para usar sin escribir ningún código de servidor.

### Las 7 tablas de CreeshApp

```sql
-- 1. Perfil de cada usuario (se crea automáticamente al registrarse)
CREATE TABLE profiles (
    id          UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username    TEXT UNIQUE NOT NULL,
    full_name   TEXT NOT NULL,
    bio         TEXT,
    avatar_url  TEXT,
    country     TEXT,
    specialty   TEXT,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Recetas que los usuarios publican
CREATE TABLE recipes (
    id             UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    author_id      UUID REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    title          TEXT NOT NULL,
    title_es       TEXT,
    description    TEXT,
    image_url      TEXT,
    instructions   TEXT,
    instructions_es TEXT,
    community_id   INT REFERENCES communities(id),
    difficulty     TEXT CHECK (difficulty IN ('facil', 'medio', 'dificil')),
    is_published   BOOLEAN DEFAULT true,
    created_at     TIMESTAMPTZ DEFAULT NOW()
);

-- 3. Ingredientes de cada receta
CREATE TABLE ingredients (
    id         SERIAL PRIMARY KEY,
    recipe_id  UUID REFERENCES recipes(id) ON DELETE CASCADE NOT NULL,
    name       TEXT NOT NULL,
    quantity   TEXT,
    unit       TEXT,
    sort_order INT DEFAULT 0
);

-- 4. Recetas de TheMealDB guardadas como favoritas
CREATE TABLE favorites (
    id           SERIAL PRIMARY KEY,
    user_id      TEXT NOT NULL,
    meal_id      TEXT NOT NULL,
    meal_title   TEXT NOT NULL,
    meal_image   TEXT,
    meal_category TEXT,
    created_at   TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, meal_id)   -- un usuario no puede guardar el mismo favorito dos veces
);

-- 5. Comentarios en recetas de usuarios
CREATE TABLE comments (
    id         UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id    UUID REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    recipe_id  UUID REFERENCES recipes(id) ON DELETE CASCADE NOT NULL,
    content    TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. Las 9 comunidades temáticas
CREATE TABLE communities (
    id          SERIAL PRIMARY KEY,
    name        TEXT NOT NULL,
    description TEXT
);

-- 7. Seguimientos entre usuarios
CREATE TABLE follows (
    follower_id  UUID REFERENCES profiles(id) ON DELETE CASCADE,
    following_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    created_at   TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (follower_id, following_id)  -- evita duplicados
);
```

### API REST automática de Supabase

Supabase genera automáticamente endpoints para cada tabla. No hace falta escribir ningún controlador. Ejemplos de las URLs que usa la app:

```
GET  /rest/v1/recipes?author_id=eq.{userId}            → mis recetas
POST /rest/v1/recipes                                   → publicar receta
GET  /rest/v1/favorites?user_id=eq.{userId}            → mis favoritos
POST /rest/v1/favorites                                 → guardar favorito
DELETE /rest/v1/favorites?user_id=eq.{id}&meal_id=eq.{id}  → quitar favorito
GET  /rest/v1/comments?recipe_id=eq.{recipeId}         → comentarios de receta
POST /rest/v1/comments                                  → publicar comentario
```

La sintaxis `?author_id=eq.{userId}` es el sistema de filtros de Supabase (`eq` = equals, `gt` = greater than, etc.).

---

## 5. Trigger automático al registrarse

Cuando un usuario se registra, Supabase Auth crea un registro en su tabla interna `auth.users`. Pero la app necesita también un registro en la tabla `profiles`. Esto se hace automáticamente con un **trigger** de PostgreSQL:

```sql
-- Función que crea el perfil
CREATE OR REPLACE FUNCTION handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, username, full_name)
    VALUES (
        NEW.id,                                  -- mismo UUID que auth.users
        split_part(NEW.email, '@', 1),           -- "josue.lopez" si email es josue.lopez@...
        split_part(NEW.email, '@', 1)            -- nombre inicial igual al username
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger que llama a esa función
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE PROCEDURE handle_new_user();
```

El flujo completo:

```
Usuario se registra en la app
→ App envía: POST /auth/v1/signup
→ Supabase crea registro en auth.users
→ El trigger se dispara automáticamente
→ Se crea registro en tabla profiles con el mismo UUID
→ El usuario ya tiene perfil sin que la app haga nada extra
```

---

## 6. Autenticación — JWT en detalle

**JWT (JSON Web Token)** es el estándar de autenticación para APIs REST. Es el mismo sistema que usan Netflix, GitHub, Spotify.

### Qué es un JWT estructuralmente

Un JWT tiene 3 partes separadas por puntos:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyLWlkLTEyMyIsImVtYWlsIjoidGVzdEBleGFtcGxlLmNvbSIsImV4cCI6MTcwMDAwMDAwMH0.SIGNATURE
      HEADER (Base64)                           PAYLOAD (Base64)                                                      FIRMA
```

- **Header:** algoritmo de firma (`HS256`)
- **Payload:** datos del usuario (`sub`, `email`, `exp` — fecha de expiración)
- **Firma:** garantiza que el token no fue modificado

El payload decodificado se ve así:
```json
{
  "sub": "uuid-del-usuario",
  "email": "josue@ejemplo.com",
  "role": "authenticated",
  "exp": 1700000000
}
```

Supabase verifica la firma en cada petición. Si alguien modifica el token manualmente, la firma no coincide y Supabase rechaza la petición.

### El flujo de login en el código real

```kotlin
// En AuthViewModel.login():
fun login(email: String, password: String) {
    _authState.value = AuthState.Loading
    viewModelScope.launch {
        try {
            val response = authApi.signIn("password", AuthRequest(email.trim(), password))
            if (response.isSuccessful) {
                val body = response.body()
                val token  = body?.accessToken   // JWT
                val userId = body?.user?.id      // UUID del usuario
                if (token != null && userId != null) {
                    SessionManager.saveSession(userId, token, body.user.email, body.refreshToken)
                    _authState.value = AuthState.Success(userId)
                }
            } else {
                _authState.value = AuthState.Error(errorMessage(response.code()))
            }
        } catch (e: UnknownHostException) {
            _authState.value = AuthState.Error("Sin conexión a internet")
        }
    }
}
```

Y los mensajes de error según el código HTTP:

```kotlin
private fun errorMessage(code: Int) = when (code) {
    400 -> "Credenciales inválidas"
    422 -> "Email ya registrado o datos inválidos"
    429 -> "Demasiados intentos, espera un momento"
    else -> "Error del servidor ($code)"
}
```

### Refresh Token — Sesión sin interrupciones

El JWT dura ~1 hora. Para que el usuario no tenga que hacer login cada hora, existe el **refresh token** — un token de larga duración que permite obtener un nuevo JWT.

En `MainActivity`, cada vez que la app vuelve al frente (onResume):

```kotlin
override fun onResume() {
    super.onResume()
    // Primero verifica si expiró por inactividad (30 min)
    if (SessionManager.isSessionExpired()) {
        SessionManager.clearSession()
        navController.navigate(R.id.loginFragment, ...)
        return
    }
    // Si hay sesión activa, renueva el JWT silenciosamente
    if (SessionManager.isLoggedIn()) {
        tryRefreshToken()
    }
}

private fun tryRefreshToken() {
    val refreshToken = SessionManager.getRefreshToken() ?: return
    lifecycleScope.launch {
        try {
            val response = api.refreshToken("refresh_token", mapOf("refresh_token" to refreshToken))
            if (response.isSuccessful) {
                val body = response.body()
                // Guarda el nuevo JWT sin que el usuario se dé cuenta
                SessionManager.saveSession(userId, newToken, email, newRefresh)
            }
        } catch (e: Exception) { /* si falla, usa el token actual */ }
    }
}
```

---

## 7. Row Level Security (RLS) — Seguridad en la base de datos

RLS es una función de PostgreSQL que agrega una capa de seguridad directamente en la base de datos. Aunque alguien tenga el JWT de otro usuario, la base de datos misma bloquea el acceso a datos que no son suyos.

```sql
-- Ejemplo: políticas en la tabla recipes

-- Solo VER recetas publicadas (cualquier usuario autenticado puede ver las publicadas)
CREATE POLICY "ver recetas publicadas"
ON recipes FOR SELECT
USING (is_published = true OR author_id = auth.uid());

-- Solo INSERTAR como uno mismo (el author_id debe ser el usuario actual)
CREATE POLICY "publicar receta"
ON recipes FOR INSERT
WITH CHECK (auth.uid() = author_id);

-- Solo MODIFICAR las propias
CREATE POLICY "editar receta propia"
ON recipes FOR UPDATE
USING (auth.uid() = author_id);
```

`auth.uid()` es una función de Supabase que devuelve el UUID del usuario autenticado basándose en el JWT. Si el JWT pertenece al usuario A, `auth.uid()` devuelve el UUID del usuario A — independientemente de lo que la app le pase.

**En la práctica:** si un atacante robara el JWT del usuario A y tratara de borrar recetas del usuario B con `DELETE /recipes?author_id=eq.{userId_B}`, Supabase lo bloquearía en la base de datos, aunque la petición llegue con un JWT válido.

---

## 8. Supabase Storage — Imágenes de recetas

Cuando un usuario publica una receta con foto, el flujo es:

```
1. UploadRecipeFragment selecciona imagen con ActivityResultContracts.GetContent()
   ↓
2. Lee los bytes de la imagen:
   context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
   ↓
3. RecipeViewModel.publishRecipe() llama a SupabaseStorageClient.uploadImage()
   ↓
4. PUT /storage/v1/object/recipes/{userId}/{timestamp}.jpg
   Headers: Authorization: Bearer {JWT}, Content-Type: image/jpeg
   Body: bytes de la imagen
   ↓
5. Supabase devuelve la URL pública de la imagen
   ↓
6. App guarda esa URL en la columna image_url de la tabla recipes:
   POST /rest/v1/recipes  con { title, description, image_url: "https://..." }
   ↓
7. Cualquier usuario puede ver la imagen directamente desde esa URL pública
```

---

## 9. Las APIs Externas en detalle

### TheMealDB — La fuente de recetas

Provee más de 300 recetas internacionales reales organizadas en categorías. La app usa estos 5 endpoints:

| Llamada en la app | URL real | ¿Cuándo? |
|---|---|---|
| `getMealsByLetter("c")` | `/search.php?f=c` | Discover (cargar recetas) |
| `searchMeals("pasta")` | `/search.php?s=pasta` | Búsqueda del usuario |
| `getMealById("52772")` | `/lookup.php?i=52772` | Abrir detalle de receta |
| `getMealsByCategory("Chicken")` | `/filter.php?c=Chicken` | Filtrar por comunidad |
| `getCategories()` | `/categories.php` | Lista de categorías |

### MyMemory — Traducción en tiempo real

Las instrucciones de las recetas vienen en inglés. La app las traduce fragmento por fragmento:

```kotlin
fun translateMeal(meal: Meal) {
    viewModelScope.launch {
        // La API gratuita tiene límite de 450 caracteres por petición
        val instructions = meal.instructions ?: return@launch
        val chunks = instructions.chunked(450)
        
        val translatedChunks = chunks.map { chunk ->
            translationApi.translate(chunk, "en|es").responseData.translatedText
        }
        
        _translatedContent.value = TranslatedContent(
            name = translationApi.translate(meal.name, "en|es").responseData.translatedText,
            instructions = translatedChunks.joinToString(" "),
            ingredients = meal.getIngredientList().map { (ing, measure) ->
                val translatedIng = translationApi.translate(ing, "en|es").responseData.translatedText
                Pair(translatedIng, measure)
            }
        )
    }
}
```

Por eso al abrir una receta el usuario ve "Traduciendo..." y luego el texto aparece — la traducción ocurre mientras el Fragment ya está visible.

### RandomUser — Cocineros determinísticos

La app necesita mostrar siempre el mismo cocinero para la misma receta. Si fuera completamente aleatorio, cada vez que el usuario abriera una receta vería un cocinero diferente.

La solución es usar `seed=creesh2024` en la petición — RandomUser siempre devuelve los mismos 12 usuarios con ese seed. Y luego se asigna el cocinero usando el hash de la categoría:

```kotlin
fun getChefForMeal(meal: Meal): Chef? {
    val chefs = _chefs.value ?: return null
    if (chefs.isEmpty()) return null
    val index = Math.abs(meal.category.hashCode()) % chefs.size
    return chefs[index]  // siempre el mismo cocinero para la misma categoría
}
```

---

## 10. Estructura del código de red

```
app/src/main/java/com/creesh/app/api/
│
├── RetrofitClient.kt          ← Configura OkHttp + Retrofit para TheMealDB
├── MealDbApi.kt               ← @GET, @POST, @Query de TheMealDB
│
├── SupabaseClient.kt          ← OkHttp con interceptor de JWT para Supabase REST
├── SupabaseApi.kt             ← Endpoints CRUD de todas las tablas
│
├── SupabaseAuthClient.kt      ← OkHttp separado para autenticación (sin JWT)
├── SupabaseAuthApi.kt         ← login, registro, refresh token
│
├── SupabaseStorageClient.kt   ← Cliente manual OkHttp para subir imágenes
├── SupabaseStorageApi.kt      ← Interface Retrofit para storage
│
├── TranslationClient.kt       ← Retrofit para MyMemory
├── TranslationApi.kt          ← @GET de traducción
│
├── RandomUserClient.kt        ← Retrofit para RandomUser
├── RandomUserApi.kt           ← @GET de cocineros
│
└── models/
    ├── Meal.kt                ← Receta de TheMealDB (20 ingredientes posibles)
    ├── AuthModels.kt          ← AuthRequest, AuthResponse, AuthUser
    ├── RecipeModels.kt        ← RecipeRequest, RecipeResponse
    ├── FavoriteItem.kt        ← Favorito de Supabase
    ├── DetailModels.kt        ← RecipeDetail, IngredientItem, CommentItem
    ├── TranslationResponse.kt ← Respuesta de MyMemory
    └── User.kt                ← Chef + RandomUser models
```

---

## Preguntas frecuentes

**¿Por qué hay un cliente separado para autenticación (`SupabaseAuthClient`) y otro para datos (`SupabaseClient`)?**
Las peticiones de autenticación van a `/auth/v1/` y NO llevan el JWT del usuario — precisamente porque aún no se tiene. Las peticiones de datos van a `/rest/v1/` y SÍ llevan el JWT. Tenerlos separados permite que el interceptor de JWT solo se aplique donde corresponde.

**¿Qué es `suspend fun`?**
Es una función de Kotlin que puede pausarse sin bloquear el hilo. Cuando el ViewModel llama `api.searchMeals("pasta")`, la coroutine se pausa mientras espera la respuesta de red y el hilo de UI queda libre para dibujar la pantalla. Cuando llega la respuesta, la coroutine se reanuda.

**¿Qué es `@SerializedName`?**
Supabase y TheMealDB devuelven JSON con nombres en snake_case (`author_id`, `strMeal`). Las convenciones de Kotlin son camelCase (`authorId`, `name`). `@SerializedName("strMeal")` le dice a Gson: "cuando veas `strMeal` en el JSON, ponlo en la propiedad `name` de Kotlin".

**¿Por qué el token tiene solo 1 hora de duración?**
Si alguien robara el JWT (por ejemplo, interceptando el tráfico de red), solo podría usarlo durante 1 hora antes de que expire. El refresh token dura mucho más pero solo viaja por la red una vez por hora — mucho menos exposición.

**¿Qué es `object` en Kotlin (como `RetrofitClient`, `SessionManager`)?**
`object` es la forma de Kotlin de crear un singleton: una clase de la que solo existe una instancia en toda la app. `RetrofitClient` es un singleton para que no se creen múltiples instancias de Retrofit (que son pesadas). `SessionManager` es un singleton para que todos accedan al mismo estado de sesión.

**¿Qué pasa si el servidor de TheMealDB está caído?**
El ViewModel captura las excepciones de red (`UnknownHostException`, `SocketTimeoutException`) y actualiza `_error.value`. El Fragment observa ese error y muestra el panel de "sin conexión" con el botón "Reintentar".
