# 📚 Guía de Estudio — CreeshApp

> Documento completo para entender la arquitectura, clases, lógica y código del proyecto Android **CreeshApp**.  
> Léelo de arriba a abajo la primera vez. Después úsalo como referencia por sección.

---

## Tabla de contenidos

1. [¿Qué es CreeshApp?](#1-qué-es-creeshapp)
2. [Arquitectura del proyecto (MVVM)](#2-arquitectura-del-proyecto-mvvm)
3. [Estructura de carpetas](#3-estructura-de-carpetas)
4. [Capa de datos — API](#4-capa-de-datos--api)
   - [MealDbApi + RetrofitClient](#41-mealdbapi--retrofitclient)
   - [TranslationApi + TranslationClient](#42-translationapi--translationclient)
   - [RandomUserApi + RandomUserClient](#43-randomuserapi--randomuserclient)
5. [Modelos de datos](#5-modelos-de-datos)
   - [Meal.kt](#51-mealkt)
   - [User.kt / Chef](#52-userkt--chef)
   - [TranslationResponse.kt](#53-translationresponsekt)
6. [ViewModels](#6-viewmodels)
   - [RecipeViewModel](#61-recipeviewmodel)
   - [SocialViewModel](#62-socialviewmodel)
7. [Fragments (pantallas)](#7-fragments-pantallas)
   - [HomeFragment](#71-homefragment)
   - [DiscoverFragment](#72-discoverfragment)
   - [CommunitiesFragment](#73-communitiesfragment)
   - [FavoritesFragment](#74-favoritesfragment)
   - [UploadRecipeFragment](#75-uploadrecipefragment)
   - [RecipeDetailFragment](#76-recipedetailfragment)
   - [ChefProfileFragment](#77-chefprofilefragment)
   - [ProfileFragment](#78-profilefragment)
   - [SettingsFragment](#79-settingsfragment)
8. [Adapters (RecyclerView)](#8-adapters-recyclerview)
   - [RecipeAdapter](#81-recipeadapter)
   - [RecipeHorizontalAdapter](#82-recipehorizontaladapter)
   - [IngredientAdapter](#83-ingredientadapter)
   - [CommunityAdapter](#84-communityadapter)
   - [ChefAdapter](#85-chefadapter)
9. [Layouts XML](#9-layouts-xml)
10. [Navegación](#10-navegación)
11. [AndroidManifest y configuración de red](#11-androidmanifest-y-configuración-de-red)
12. [Conceptos clave explicados](#12-conceptos-clave-explicados)

---

## 1. ¿Qué es CreeshApp?

CreeshApp es una aplicación Android de recetas de cocina. Sus funciones principales son:

| Función | Descripción |
|---|---|
| Explorar recetas | Muestra recetas obtenidas de TheMealDB (API pública gratuita) |
| Buscar recetas | Busca por nombre o filtra por categoría |
| Ver detalle | Muestra ingredientes e instrucciones, traducidas al español |
| Favoritos | Guarda recetas favoritas en memoria local |
| Comunidades | Agrupa recetas por tipo/categoría |
| Subir receta | Formulario para que el usuario publique una receta |
| Perfil social | Cocineros que se pueden seguir (datos de randomuser.me) |
| Perfil propio | Pantalla con estadísticas del usuario |

---

## 2. Arquitectura del proyecto (MVVM)

El proyecto sigue el patrón **MVVM (Model - View - ViewModel)**. Es el patrón que recomienda Google para apps Android modernas.

```
┌─────────────────────────────────────────────────────┐
│                        VIEW                         │
│         Fragments + Layouts XML                     │
│  (solo muestran datos, no hacen lógica de negocio)  │
└────────────────────┬────────────────────────────────┘
                     │  observa LiveData
                     ▼
┌─────────────────────────────────────────────────────┐
│                    VIEWMODEL                        │
│       RecipeViewModel / SocialViewModel             │
│  (contiene la lógica, expone datos con LiveData)    │
└────────────────────┬────────────────────────────────┘
                     │  llama funciones
                     ▼
┌─────────────────────────────────────────────────────┐
│                      MODEL                         │
│   Retrofit APIs + Clases de datos (Meal, Chef...)  │
│         (obtiene y estructura los datos)            │
└─────────────────────────────────────────────────────┘
```

**¿Por qué MVVM?**
- La **View** (Fragment) no sabe de dónde vienen los datos.
- El **ViewModel** sobrevive rotaciones de pantalla (no se destruye al girar el teléfono).
- El **Model** (API/datos) no sabe nada de la pantalla.
- Todo se comunica mediante **LiveData**: el Fragment *observa* y reacciona cuando cambian los datos.

---

## 3. Estructura de carpetas

```
app/src/main/
├── java/com/creesh/app/
│   ├── MainActivity.kt               ← Actividad principal (única en la app)
│   ├── api/                          ← Todo lo relacionado con llamadas a internet
│   │   ├── MealDbApi.kt              ← Interfaz de endpoints de recetas
│   │   ├── RetrofitClient.kt         ← Configuración del cliente HTTP de recetas
│   │   ├── TranslationApi.kt         ← Interfaz del endpoint de traducción
│   │   ├── TranslationClient.kt      ← Configuración del cliente HTTP de traducción
│   │   ├── RandomUserApi.kt          ← Interfaz del endpoint de usuarios aleatorios
│   │   ├── RandomUserClient.kt       ← Configuración del cliente HTTP de usuarios
│   │   └── models/                   ← Clases que representan los datos de la API
│   │       ├── Meal.kt               ← Modelo de una receta
│   │       ├── User.kt               ← Modelo de usuario/chef + función de conversión
│   │       └── TranslationResponse.kt← Modelo de respuesta de traducción
│   ├── adapters/                     ← Adaptan listas de datos a RecyclerViews
│   │   ├── RecipeAdapter.kt          ← Cuadrícula de recetas
│   │   ├── RecipeHorizontalAdapter.kt← Carrusel horizontal de recetas
│   │   ├── IngredientAdapter.kt      ← Lista de ingredientes
│   │   ├── CommunityAdapter.kt       ← Lista de comunidades
│   │   └── ChefAdapter.kt            ← Lista horizontal de cocineros
│   ├── fragments/                    ← Cada pantalla de la app
│   │   ├── HomeFragment.kt
│   │   ├── DiscoverFragment.kt
│   │   ├── CommunitiesFragment.kt
│   │   ├── FavoritesFragment.kt
│   │   ├── UploadRecipeFragment.kt
│   │   ├── RecipeDetailFragment.kt
│   │   ├── ChefProfileFragment.kt
│   │   ├── ProfileFragment.kt
│   │   └── SettingsFragment.kt
│   └── viewmodel/
│       ├── RecipeViewModel.kt        ← Lógica de recetas, búsqueda, favoritos, traducción
│       └── SocialViewModel.kt        ← Lógica de cocineros y sistema de seguimiento
│
└── res/
    ├── layout/                       ← Archivos XML que definen la interfaz visual
    ├── menu/
    │   └── bottom_nav_menu.xml       ← Items del menú inferior
    ├── navigation/
    │   └── nav_graph.xml             ← Mapa de navegación entre pantallas
    ├── drawable/                     ← Íconos y fondos vectoriales
    ├── xml/
    │   └── network_security_config.xml← Política de seguridad de red
    ├── values/
    │   ├── colors.xml                ← Paleta de colores de la app
    │   ├── strings.xml               ← Textos de la app
    │   └── themes.xml                ← Estilos y temas visuales
    └── anim/                         ← Animaciones de transición entre pantallas
```

---

## 4. Capa de datos — API

### 4.1 MealDbApi + RetrofitClient

**¿Qué es Retrofit?**  
Retrofit es una librería que convierte automáticamente llamadas HTTP en funciones de Kotlin. En vez de escribir código de red manualmente, defines una *interfaz* y Retrofit hace el resto.

**`MealDbApi.kt`** — Define los endpoints disponibles:

```kotlin
interface MealDbApi {
    @GET("random.php")
    suspend fun getRandomMeal(): MealResponse

    @GET("search.php")
    suspend fun searchMeals(@Query("s") name: String): MealResponse

    @GET("search.php")
    suspend fun getMealsByLetter(@Query("f") letter: String): MealResponse

    @GET("lookup.php")
    suspend fun getMealById(@Query("i") id: String): MealResponse

    @GET("filter.php")
    suspend fun filterByCategory(@Query("c") category: String): MealResponse

    @GET("categories.php")
    suspend fun getCategories(): CategoryResponse
}
```

- `@GET("random.php")` → hace una petición GET a `https://www.themealdb.com/api/json/v1/1/random.php`
- `@Query("s")` → agrega `?s=nombre` a la URL
- `suspend` → indica que es una función asíncrona (corre en corrutinas, no bloquea la UI)
- El tipo de retorno (`MealResponse`) es la clase Kotlin que recibirá el JSON automáticamente

**`RetrofitClient.kt`** — Crea y configura el cliente HTTP:

```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)   // máximo 8s para conectar
        .readTimeout(10, TimeUnit.SECONDS)      // máximo 10s para recibir respuesta
        .build()

    val api: MealDbApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // JSON → Kotlin
            .build()
            .create(MealDbApi::class.java)
    }
}
```

- `object` → singleton: solo existe una instancia en toda la app
- `by lazy` → se crea solo cuando se usa por primera vez (eficiencia)
- `GsonConverterFactory` → convierte automáticamente el JSON de la API a clases Kotlin

---

### 4.2 TranslationApi + TranslationClient

Usa la API gratuita **MyMemory** para traducir texto de inglés a español.

```kotlin
interface TranslationApi {
    @GET("get")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langPair: String = "en|es"
    ): TranslationResponse
}
```

La URL resultante sería: `https://api.mymemory.translated.net/get?q=texto&langpair=en|es`

**Importante**: La API gratuita tiene un límite de palabras por día. Si se supera el límite, la app muestra el texto original en inglés (hay manejo de error en el ViewModel).

---

### 4.3 RandomUserApi + RandomUserClient

Usa la API gratuita **randomuser.me** para obtener perfiles de usuarios falsos que simulan ser cocineros.

```kotlin
interface RandomUserApi {
    @GET("api/")
    suspend fun getUsers(
        @Query("results") results: Int = 12,
        @Query("seed") seed: String = "creesh2024",   // seed fija = siempre los mismos usuarios
        @Query("inc") include: String = "name,email,picture,login,location"
    ): RandomUserResponse
}
```

- `seed = "creesh2024"` → el parámetro `seed` garantiza que siempre se devuelvan los mismos 12 usuarios, independientemente de cuándo se llame. Así los "cocineros" son consistentes.
- `inc` → indica qué campos devolver (evita descargar datos innecesarios)

---

## 5. Modelos de datos

Los modelos son clases Kotlin que representan los datos que llegan de la API en formato JSON.

### 5.1 Meal.kt

Representa una receta. Cada campo tiene la anotación `@SerializedName` que indica el nombre exacto en el JSON de la API:

```kotlin
data class Meal(
    @SerializedName("idMeal")        val id: String,
    @SerializedName("strMeal")       val name: String,
    @SerializedName("strCategory")   val category: String?,
    @SerializedName("strArea")       val area: String?,          // país de origen
    @SerializedName("strInstructions") val instructions: String?,
    @SerializedName("strMealThumb")  val thumbnail: String?,     // URL de la imagen
    // ... hasta 20 ingredientes y 20 medidas
    @SerializedName("strIngredient1")  val ingredient1: String?,
    @SerializedName("strMeasure1")     val measure1: String?,
    // ... ingredient2/measure2, ingredient3/measure3... hasta el 20
)
```

- `data class` → clase de datos en Kotlin: genera automáticamente `equals()`, `hashCode()` y `toString()`
- `String?` → el `?` indica que el campo puede ser `null` (la API puede no enviarlo)
- `@SerializedName` → necesario porque el JSON usa nombres como `"strMeal"` pero en Kotlin usamos `name`

**Función helper `getIngredientList()`:**

```kotlin
fun getIngredientList(): List<Pair<String, String>> {
    val ingredients = listOf(ingredient1, ingredient2, ..., ingredient20)
    val measures    = listOf(measure1, measure2, ..., measure20)
    return ingredients.zip(measures)
        .filter { (ing, _) -> !ing.isNullOrBlank() }  // elimina los vacíos
        .map    { (ing, meas) -> Pair(ing!!, meas?.trim() ?: "") }
}
```

- Junta los 20 ingredientes con sus 20 medidas usando `zip()`
- Filtra los que están vacíos (una receta puede tener 5 ingredientes, no siempre 20)
- Devuelve una lista de pares `(ingrediente, medida)` → ej: `("Flour", "2 cups")`

**`MealResponse`:**
```kotlin
data class MealResponse(
    @SerializedName("meals") val meals: List<Meal>?
)
```
La API siempre devuelve un objeto con una lista llamada `"meals"`. Puede ser `null` si no hay resultados.

---

### 5.2 User.kt / Chef

Este archivo tiene dos partes: los modelos de la API de randomuser.me y la clase `Chef` de la app.

**Modelos de randomuser.me:**
```kotlin
data class RandomUser(
    val name:     RandomUserName,     // { first: "Ana", last: "López" }
    val email:    String,
    val picture:  RandomUserPicture,  // { large: "url", medium: "url", thumbnail: "url" }
    val login:    RandomUserLogin,    // { username: "anacocina" }
    val location: RandomUserLocation  // { country: "México" }
)
```

**Clase `Chef` (modelo interno de la app):**
```kotlin
data class Chef(
    val id:            String,   // = username de randomuser
    val fullName:      String,   // "Ana López"
    val username:      String,   // "@anacocina"
    val photoUrl:      String,   // URL de la foto
    val country:       String,
    val specialty:     String,   // "Cocina Mexicana"
    val bio:           String,   // frase descriptiva
    val followerCount: Int,
    val recipeCount:   Int
)
```

**Función de conversión `RandomUser.toChef()`:**
```kotlin
fun RandomUser.toChef(): Chef {
    val hash = abs(login.username.hashCode())
    return Chef(
        id        = login.username,
        specialty = SPECIALTIES[hash % SPECIALTIES.size],  // asigna especialidad según hash
        bio       = BIOS[hash % BIOS.size],                // idem para la bio
        followerCount = 500 + (hash % 9500),               // número entre 500 y 10000
        recipeCount   = 5 + (hash % 95)                    // número entre 5 y 100
    )
}
```

- `hashCode()` → convierte el username en un número entero
- `hash % lista.size` → obtiene un índice válido para la lista (módulo)
- Esto garantiza que el mismo usuario **siempre** tenga la misma especialidad, bio y contadores

**`fallbackChefs()`** — Lista de 12 cocineros hardcodeados que se usan si no hay internet:
```kotlin
fun fallbackChefs(): List<Chef> = listOf(
    Chef("chef_carlos", "Carlos Martínez", "@carloschef",
         "https://randomuser.me/api/portraits/men/32.jpg", ...),
    // ... 11 más
)
```

---

### 5.3 TranslationResponse.kt

```kotlin
data class TranslationResponse(
    val responseData:   TranslationResponseData,
    val responseStatus: Int   // 200 = éxito, otro = error
)

data class TranslationResponseData(
    val translatedText: String
)
```

Representa la respuesta de MyMemory. Se verifica `responseStatus == 200` antes de usar el texto traducido.

---

## 6. ViewModels

Los ViewModels son el "cerebro" de la app. Contienen la lógica y exponen datos a los Fragments mediante **LiveData**.

**¿Qué es LiveData?**  
Es un contenedor de datos que los Fragments pueden *observar*. Cuando el valor cambia, el Fragment se actualiza automáticamente. Además, es consciente del ciclo de vida: si el Fragment está destruido, no actualiza nada.

### 6.1 RecipeViewModel

Gestiona todas las recetas: cargar, buscar, favoritos y traducción.

**Datos que expone:**
```kotlin
val randomMeals:      LiveData<List<Meal>>         // recetas para descubrir
val hiddenGems:       LiveData<List<Meal>>          // recetas poco comunes
val searchResults:    LiveData<List<Meal>>          // resultados de búsqueda/filtro
val selectedMeal:     LiveData<Meal>                // receta actualmente vista
val favorites:        LiveData<MutableList<Meal>>   // favoritos del usuario
val translatedContent:LiveData<TranslatedContent?>  // contenido traducido
val isLoading:        LiveData<Boolean>             // ¿está cargando?
val error:            LiveData<String?>             // mensaje de error o null
val activeCommunity:  LiveData<String?>             // comunidad activa en filtro
```

**Funciones principales:**

```kotlin
fun loadDiscoverRecipes()
```
Llama a la API 4 veces con letras ("c", "b", "s", "p"), toma 3 recetas de cada una, las mezcla y muestra 10. Así siempre hay variedad.

```kotlin
fun loadHiddenGems()
```
Busca recetas con letras poco comunes ("x", "y", "z"). Si no hay, usa "v" como fallback. Estas aparecen en el hero de la pantalla de inicio.

```kotlin
fun searchMeals(query: String)
fun filterByCategory(category: String, communityName: String? = null)
```
Busca recetas por nombre o filtra por categoría. Los resultados van a `searchResults`.

```kotlin
fun getMealById(id: String)
```
Se usa cuando una receta viene incompleta (sin instrucciones). La pantalla de detalle llama esto para obtener todos los datos.

```kotlin
fun toggleFavorite(meal: Meal)
fun isFavorite(mealId: String): Boolean
```
Agrega o quita una receta de favoritos. Los favoritos se guardan en memoria (se pierden al cerrar la app).

**Traducción:**
```kotlin
fun translateMeal(meal: Meal)
```
Traduce nombre, ingredientes e instrucciones. Proceso:
1. Limpia el texto (quita `\r\n`)
2. Divide el texto en chunks de máximo 450 caracteres (límite de la API)
3. Traduce cada chunk
4. Une los resultados

```kotlin
private suspend fun translateChunked(text: String): String
```
Función privada que divide el texto en fragmentos y llama a la API de traducción para cada uno. Si falla alguno, devuelve el fragmento original.

**Manejo de errores:**
```kotlin
} catch (e: UnknownHostException) {
    _error.value = "Sin conexión a internet"
} catch (e: SocketTimeoutException) {
    _error.value = "Tiempo de espera agotado"
} catch (e: Exception) {
    _error.value = "Error: ${e.message}"
}
```
Se capturan tres tipos de error distintos para dar mensajes claros al usuario.

---

### 6.2 SocialViewModel

Gestiona los cocineros (chefs) y el sistema de seguimiento.

**Inicialización:**
```kotlin
init {
    _chefs.value = fallbackChefs()  // carga chefs locales INMEDIATAMENTE
    loadChefsFromApi()              // en paralelo, intenta obtener datos reales
}
```
Esto garantiza que los cocineros estén disponibles desde el primer frame, sin esperar la red.

**`getChefForMeal(meal)`:**
```kotlin
fun getChefForMeal(meal: Meal): Chef? {
    val hash = abs((meal.category ?: meal.area ?: meal.id).hashCode())
    return list[hash % list.size]
}
```
Asigna un cocinero a cada receta de forma determinística: la misma receta siempre muestra el mismo cocinero, usando el hash de la categoría como índice.

**`toggleFollow(chefId)`:**
```kotlin
fun toggleFollow(chefId: String) {
    val set = _followedChefIds.value ?: mutableSetOf()
    if (set.contains(chefId)) set.remove(chefId) else set.add(chefId)
    _followedChefIds.value = set
}
```
Usa un `Set` (conjunto sin duplicados) para guardar los IDs de cocineros seguidos. Si ya está, lo quita; si no está, lo agrega.

---

## 7. Fragments (pantallas)

En Android, una **Activity** es como una ventana. Los **Fragments** son secciones dentro de esa ventana. CreeshApp tiene una sola Activity (`MainActivity`) y múltiples Fragments que se intercambian según la navegación.

Todos los Fragments siguen el mismo patrón:

```kotlin
class XFragment : Fragment() {
    private var _binding: FragmentXBinding? = null
    private val binding get() = _binding!!                    // acceso seguro al binding
    private val viewModel: RecipeViewModel by activityViewModels() // ViewModel compartido

    override fun onCreateView(...): View {
        _binding = FragmentXBinding.inflate(inflater, ...)    // inflar el XML
        return binding.root
    }

    override fun onViewCreated(view: View, ...) {
        // aquí se configuran listeners y observers
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null    // evitar memory leaks
    }
}
```

- `ViewBinding` (`FragmentXBinding`) → permite acceder a las vistas del XML sin `findViewById()`
- `by activityViewModels()` → el ViewModel es compartido entre todos los Fragments de la Activity
- `_binding = null` en `onDestroyView()` → obligatorio para evitar que el Fragment retenga vistas destruidas

---

### 7.1 HomeFragment

**Pantalla de inicio.** Muestra la imagen hero y 4 botones de acceso rápido.

**Lógica:**
- Llama `viewModel.loadHiddenGems()` para obtener la receta del hero
- Observa `hiddenGems`: cuando llega la primera receta, carga su imagen con Glide y muestra el nombre
- Observa `error`: si hay error de red, muestra el overlay con el mensaje y botón "Reintentar"
- Los 4 botones navegan a sus respectivos fragments usando el NavController

**Estados visuales del hero:**
```
Sin datos aún   → imagen placeholder de Android
Datos cargados  → imagen de la receta + nombre
Error de red    → overlay negro translúcido + mensaje + botón Reintentar
```

---

### 7.2 DiscoverFragment

**Pantalla de descubrimiento.** Muestra un buscador y una cuadrícula de recetas.

- Llama `loadDiscoverRecipes()` al iniciar
- Observa `isLoading` para mostrar/ocultar un `ProgressBar`
- Observa `randomMeals` para llenar la cuadrícula (RecyclerView en 2 columnas)
- Observa `searchResults` para mostrar resultados de búsqueda
- Al tocar una receta → `viewModel.setSelectedMeal(meal)` + navega al detalle

---

### 7.3 CommunitiesFragment

**Lista de comunidades/categorías.** Permite filtrar recetas por tipo de cocina.

- Carga las categorías de la API con `getCategories()`
- Muestra la lista con `CommunityAdapter`
- Al tocar una comunidad → llama `filterByCategory(nombre)` y navega a DiscoverFragment

---

### 7.4 FavoritesFragment

**Recetas guardadas.** Muestra las recetas marcadas como favoritas.

- Observa `viewModel.favorites` (lista en memoria)
- Si está vacía → muestra un texto "No tienes favoritos aún"
- Si tiene elementos → cuadrícula de 2 columnas con `RecipeAdapter`

---

### 7.5 UploadRecipeFragment

**Formulario para subir recetas.** Campos de texto para nombre, ingredientes e instrucciones.

- Por ahora es un formulario visual (no guarda datos en servidor)

---

### 7.6 RecipeDetailFragment

**Pantalla de detalle de una receta.** La más compleja de la app.

**Flujo completo:**
```
1. Recibe la receta desde selectedMeal (LiveData)
2. Si la receta no tiene instrucciones → llama getMealById() para obtenerla completa
3. Carga la imagen con Glide
4. Muestra ingredientes con IngredientAdapter (texto original mientras traduce)
5. Muestra "Traduciendo..." en instrucciones
6. Llama viewModel.translateMeal(meal) en background
7. Cuando llega translatedContent → actualiza título, ingredientes e instrucciones
8. Obtiene el cocinero con socialViewModel.getChefForMeal(meal)
9. Muestra la tarjeta del cocinero con foto, nombre, especialidad y botón Seguir
10. Al tocar la tarjeta → navega a ChefProfileFragment
```

**Tarjeta del cocinero:**
```kotlin
private fun showChefCard(chef: Chef) {
    binding.cardChef.visibility = View.VISIBLE
    // carga foto circular con Glide + circleCrop()
    // configura botón Seguir/Siguiendo
    // tap en la tarjeta → setSelectedChef + navegar a perfil
}
```

---

### 7.7 ChefProfileFragment

**Perfil de un cocinero.** Se accede tocando la tarjeta del cocinero en el detalle de receta.

- Obtiene el chef de `socialViewModel.selectedChef`
- Muestra foto circular, nombre, username, especialidad, país, bio, seguidores, recetas
- Botón **Seguir/Siguiendo** que cambia de estilo según el estado
- Lista de recetas relacionadas: llama `filterByCategory()` con la categoría correspondiente a la especialidad del chef
- Al tocar una receta → navega a RecipeDetailFragment

---

### 7.8 ProfileFragment

**Perfil del usuario (tab en el bottom nav).**

- Muestra datos del usuario (actualmente mockup estático)
- Contador de "Siguiendo" (se actualiza en tiempo real con `followedChefIds`)
- Contador de "Favoritos" (desde `RecipeViewModel.favorites`)
- Lista horizontal de cocineros que sigues (vacía si no sigues a nadie)
- Sección de cuenta: cambiar contraseña, email, privacidad

---

### 7.9 SettingsFragment

**Ajustes de la app.** Accesible desde el flujo de configuración.

- Cambiar contraseña
- Preferencias de email
- Modo oscuro (Switch)
- Ahorro de datos (Switch)
- Centro de ayuda
- Política de privacidad
- Botón cerrar sesión

---

## 8. Adapters (RecyclerView)

Un **RecyclerView** es una lista eficiente en Android que reutiliza las vistas. Para funcionar necesita un **Adapter** que le diga cómo mostrar cada elemento.

Todos los adapters del proyecto extienden `ListAdapter`, que es más eficiente que el `RecyclerView.Adapter` básico porque detecta automáticamente qué elementos cambiaron usando `DiffUtil`.

**Estructura de un Adapter:**
```kotlin
class MiAdapter(
    private val onItemClick: (Tipo) -> Unit   // lambda para manejar clics
) : ListAdapter<Tipo, MiAdapter.ViewHolder>(DiffCallback()) {

    // ViewHolder: representa una celda de la lista
    inner class ViewHolder(private val binding: ItemXBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Tipo) {
            binding.tvNombre.text = item.nombre
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(...) =
        ViewHolder(ItemXBinding.inflate(...))   // infla el XML del item

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))          // conecta datos con vista

    // DiffCallback: compara items para animaciones eficientes
    class DiffCallback : DiffUtil.ItemCallback<Tipo>() {
        override fun areItemsTheSame(o: Tipo, n: Tipo) = o.id == n.id
        override fun areContentsTheSame(o: Tipo, n: Tipo) = o == n
    }
}
```

### 8.1 RecipeAdapter
- Layout: `item_recipe.xml` (tarjeta cuadrada)
- Muestra: imagen, nombre, categoría/área
- Uso: cuadrícula en Discover y Favorites

### 8.2 RecipeHorizontalAdapter
- Layout: `item_recipe_horizontal.xml`
- Muestra: versión horizontal más compacta
- Uso: carruseles horizontales

### 8.3 IngredientAdapter
- Layout: `item_ingredient.xml`
- Muestra: nombre del ingrediente + cantidad
- Entrada: `List<Pair<String, String>>`
- Uso: lista en RecipeDetailFragment

### 8.4 CommunityAdapter
- Layout: `item_community.xml`
- Muestra: imagen de categoría + nombre
- Uso: lista en CommunitiesFragment

### 8.5 ChefAdapter
- Layout: `item_chef.xml` (80dp de ancho, orientación vertical)
- Muestra: foto circular, nombre y especialidad
- Uso: lista horizontal en ProfileFragment

---

## 9. Layouts XML

Los archivos XML en `res/layout/` definen la interfaz visual. Cada Fragment tiene su propio XML.

**Layouts principales:**

| Archivo | Descripción |
|---|---|
| `activity_main.xml` | Contiene el NavHostFragment (contenedor de pantallas) y el BottomNavigationView |
| `fragment_home.xml` | ConstraintLayout con imagen hero (55% pantalla) + 4 botones 2x2 |
| `fragment_discover.xml` | SearchView + ProgressBar + RecyclerView en cuadrícula |
| `fragment_communities.xml` | RecyclerView con lista de categorías |
| `fragment_favorites.xml` | RecyclerView en cuadrícula + texto de estado vacío |
| `fragment_recipe_detail.xml` | CoordinatorLayout con imagen de 280dp + tarjeta cocinero + ingredientes + instrucciones |
| `fragment_chef_profile.xml` | ScrollView con tarjeta de perfil + RecyclerView de recetas |
| `fragment_profile.xml` | Perfil del usuario con stats + lista de seguidos + sección cuenta |
| `fragment_settings.xml` | Lista vertical de opciones con Cards |
| `fragment_upload_recipe.xml` | Formulario de subida |

**`fragment_home.xml` — estructura del hero:**
```
ConstraintLayout
├── Guideline (55% vertical)
├── CardView [cardHero] (top → guideline)
│   └── FrameLayout
│       ├── ImageView [ivHeroImage]         ← imagen de fondo
│       ├── View (gradient_overlay)         ← degradado negro arriba
│       ├── LinearLayout [layoutHeroContent]← título + subtítulo + nombre receta
│       └── LinearLayout [layoutHeroError] ← overlay de error (visible solo si falla)
└── LinearLayout (guideline → bottom)
    ├── LinearLayout (row 1)
    │   ├── MaterialButton [btnDiscover]
    │   └── MaterialButton [btnCommunities]
    └── LinearLayout (row 2)
        ├── MaterialButton [btnUploadRecipe]
        └── MaterialButton [btnFavorites]
```

**`fragment_recipe_detail.xml` — estructura:**
```
CoordinatorLayout
└── NestedScrollView
    └── LinearLayout
        ├── FrameLayout (280dp) ← header con imagen
        │   ├── ImageView [ivRecipeHeader]
        │   ├── View (gradient)
        │   ├── ImageButton [btnBack]
        │   ├── ImageButton [btnFavorite]
        │   └── LinearLayout (título + categoría)
        ├── CardView [cardChef]  ← tarjeta del cocinero
        ├── TextView [tvTags]
        ├── TextView "Ingredientes"
        ├── RecyclerView [rvIngredients]
        ├── TextView "Instrucciones"
        └── TextView [tvInstructions]
```

---

## 10. Navegación

El proyecto usa el **Navigation Component** de Android Jetpack.

**`nav_graph.xml`** — Define todas las pantallas y las conexiones entre ellas:

```xml
<navigation app:startDestination="@id/homeFragment">
    <fragment android:id="@+id/homeFragment" ...>
        <action android:id="@+id/action_homeFragment_to_recipeDetailFragment"
                app:destination="@id/recipeDetailFragment"/>
    </fragment>
    <!-- más fragments y acciones -->
</navigation>
```

**`bottom_nav_menu.xml`** — Define los 3 tabs del menú inferior:
```xml
<item android:id="@+id/homeFragment"    android:title="Inicio"/>
<item android:id="@+id/profileFragment" android:title="Perfil"/>
<item android:id="@+id/logoutItem"      android:title="Salir"/>
```
⚠️ **Importante**: `logoutItem` NO es un destino de navegación. En `MainActivity` se intercepta su clic para mostrar un diálogo de confirmación.

**`MainActivity.kt`** — Configuración del Navigation Controller:

```kotlin
// Manejo manual del bottom nav para interceptar logout
binding.bottomNavigation.setOnItemSelectedListener { item ->
    when (item.itemId) {
        R.id.logoutItem -> { showLogoutDialog(); false }  // no navega
        else -> NavigationUI.onNavDestinationSelected(item, navController)
    }
}

// Ocultar bottom nav en pantallas de detalle
navController.addOnDestinationChangedListener { _, destination, _ ->
    binding.bottomNavigation.visibility = when (destination.id) {
        R.id.recipeDetailFragment,
        R.id.chefProfileFragment,
        R.id.settingsFragment -> View.GONE
        else -> View.VISIBLE
    }
}
```

**Navegar entre pantallas:**
```kotlin
// Usando una action definida en nav_graph.xml
findNavController().navigate(R.id.action_homeFragment_to_recipeDetailFragment)

// Navegando directamente a un destino por ID
findNavController().navigate(R.id.discoverFragment)

// Regresando a la pantalla anterior
findNavController().navigateUp()
```

---

## 11. AndroidManifest y configuración de red

**`AndroidManifest.xml`** — Archivo de configuración global de la app:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```
Sin esta línea, Android bloquea todas las llamadas de red.

```xml
android:networkSecurityConfig="@xml/network_security_config"
```
Apunta al archivo de configuración de seguridad de red.

**`network_security_config.xml`:**
```xml
<network-security-config>
    <base-config cleartextTrafficPermitted="false"/>  <!-- solo HTTPS -->
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">themealdb.com</domain>
        <domain includeSubdomains="true">randomuser.me</domain>
        <domain includeSubdomains="true">mymemory.translated.net</domain>
    </domain-config>
</network-security-config>
```

Declara explícitamente los dominios que usa la app y que solo se permite HTTPS (no HTTP).

---

## 12. Conceptos clave explicados

### Corrutinas (`suspend`, `viewModelScope.launch`)
```kotlin
viewModelScope.launch {
    val response = api.searchMeals(query)  // no bloquea la UI
}
```
Las corrutinas permiten hacer operaciones lentas (red, disco) sin bloquear la interfaz. `viewModelScope` garantiza que la corrutina se cancela si el ViewModel se destruye.

### LiveData + observe
```kotlin
// En el ViewModel:
private val _meals = MutableLiveData<List<Meal>>()
val meals: LiveData<List<Meal>> = _meals  // expuesto como solo-lectura

// En el Fragment:
viewModel.meals.observe(viewLifecycleOwner) { listaDeMeals ->
    adapter.submitList(listaDeMeals)  // se ejecuta cada vez que cambia
}
```
El Fragment no pregunta "¿hay nuevos datos?", simplemente *reacciona* cuando llegan.

### ViewBinding
```kotlin
_binding = FragmentHomeBinding.inflate(inflater, container, false)
binding.tvHeroRecipeName.text = "Hola"  // acceso directo sin findViewById
```
Genera automáticamente una clase por cada XML. Más seguro que `findViewById` (sin nulls inesperados).

### Glide (carga de imágenes)
```kotlin
Glide.with(this)
    .load(meal.thumbnail)   // URL de la imagen
    .centerCrop()           // recorta al centro
    .circleCrop()           // hace la imagen circular (para fotos de chefs)
    .placeholder(R.drawable.ic_placeholder)  // mientras carga
    .into(binding.ivHeroImage)  // ImageView destino
```
Glide descarga la imagen en background, la cachea y la muestra. Sin Glide, habría que hacer todo esto manualmente.

### `object` vs `class`
```kotlin
object RetrofitClient { ... }  // singleton: una sola instancia en toda la app
class RecipeViewModel { ... }  // clase normal: se pueden crear múltiples instancias
```

### `by lazy`
```kotlin
val api: MealDbApi by lazy { /* costoso de crear */ }
```
El objeto se crea solo cuando se usa por primera vez, no al iniciar la app.

### `?` y `?.` en Kotlin (null safety)
```kotlin
val category: String?        // puede ser null
category?.uppercase()        // solo llama si no es null
category ?: "Sin categoría"  // valor por defecto si es null
```
Kotlin obliga a manejar los nulls explícitamente, evitando NullPointerExceptions.

---

*Este documento cubre el 100% de la estructura del proyecto CreeshApp. Para profundizar en cualquier tema, busca el archivo correspondiente en el código fuente.*
