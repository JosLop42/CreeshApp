# Presentación 2 — Frontend (Estructura Android)

> **Sección a cargo de:** Arquitectura de la app Android, patrón MVVM, ciclo de vida, navegación, LiveData y cómo todo se conecta.

---

## 1. El patrón MVVM — Por qué existe

Antes de MVVM, Android tenía un problema grave: toda la lógica vivía dentro del `Activity` o `Fragment`. Si el usuario rotaba el teléfono, Android destruía y recreaba la pantalla — y todos los datos que estaban en memoria se perdían. La app tenía que volver a llamar a la API desde cero.

MVVM resuelve esto separando el código en 3 capas con responsabilidades claras:

| Capa | Responsabilidad | Ejemplo en CreeshApp |
|---|---|---|
| **View** | Mostrar datos y capturar eventos | `DiscoverFragment`, `RecipeDetailFragment` |
| **ViewModel** | Lógica de negocio, mantener estado | `RecipeViewModel`, `AuthViewModel`, `SocialViewModel` |
| **Model** | Fuentes de datos | APIs (Retrofit), Supabase, data classes |

**La clave:** el `ViewModel` sobrevive a la rotación de pantalla. La `View` se destruye y se recrea, pero cuando vuelve a crear sus observers, el `ViewModel` ya tiene los datos listos.

```
Teléfono rota:
  View (Fragment) → se destruye y se recrea
  ViewModel       → NO se destruye, mantiene sus LiveData intactos
  
  Nuevo Fragment → observa el mismo LiveData → datos ya están ahí
```

---

## 2. Los 3 ViewModels de la app

CreeshApp tiene exactamente 3 ViewModels, uno por área de responsabilidad:

### RecipeViewModel
El más grande. Maneja todo lo relacionado con recetas:
- Cargar recetas aleatorias (`loadDiscoverRecipes()`)
- Cargar Hidden Gems (`loadHiddenGems()`)
- Buscar recetas (`searchMeals()`)
- Favoritos (cargar, agregar, quitar)
- Traducción automática (`translateMeal()`)
- Publicar recetas propias (`publishRecipe()`)
- Mis recetas (`loadMyRecipes()`)

### AuthViewModel
Solo maneja autenticación:
- `login(email, password)` — llama a Supabase Auth
- `register(email, password, confirmPassword)` — crea cuenta nueva
- Expone `authState: LiveData<AuthState>`

### SocialViewModel
Maneja lo social:
- Cargar cocineros (`loadChefs()` desde RandomUser API)
- Seguir/dejar de seguir cocineros
- Asignar cocinero a una receta (`getChefForMeal()`)

---

## 3. Cómo funciona LiveData — El corazón de MVVM

`LiveData` es un contenedor de datos que notifica automáticamente a quienes lo observan cuando cambia su valor.

Así se define en `RecipeViewModel`:

```kotlin
// MutableLiveData: se puede cambiar desde dentro del ViewModel
private val _randomMeals = MutableLiveData<List<Meal>>()

// LiveData: solo lectura para el Fragment (no puede modificar, solo observar)
val randomMeals: LiveData<List<Meal>> = _randomMeals
```

Y así se observa en `DiscoverFragment`:

```kotlin
viewModel.randomMeals.observe(viewLifecycleOwner) { meals ->
    // Este bloque se ejecuta CADA VEZ que randomMeals cambia
    discoverAdapter.submitList(meals)
    binding.swipeRefresh.isRefreshing = false
}
```

**Por qué `viewLifecycleOwner` y no `this`?**
Un Fragment puede existir sin su vista (entre `onDestroyView` y `onCreateView`). Si usamos `this`, el observer seguiría activo y podría intentar actualizar una vista que ya no existe — crash. `viewLifecycleOwner` une el observer al ciclo de vida de la vista, no del Fragment.

---

## 4. El ciclo de vida de un Fragment

Cada Fragment pasa por un ciclo de vida estricto. Entender esto es clave para entender el código:

```
onCreateView()     → infla el XML, crea la vista, devuelve binding.root
       ↓
onViewCreated()    → la vista ya existe, aquí se conectan observers, adapters, listeners
       ↓
[El Fragment está visible y activo]
       ↓
onDestroyView()    → la vista se destruye, AQUÍ se hace _binding = null
```

Ejemplo real en `DiscoverFragment`:

```kotlin
override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
): View {
    // Inflamos el XML → creamos los objetos View en memoria
    _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
    return binding.root   // devolvemos la vista raíz
}

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // Aquí ya tenemos la vista. Conectamos todo.
    setupAdapters()
    setupObservers()
    setupSearch()
    viewModel.loadHiddenGems()
    viewModel.loadDiscoverRecipes()
}

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null   // Evitamos memory leak: soltamos la referencia a la vista
}
```

**¿Por qué `_binding = null` en `onDestroyView`?**
El Fragment puede seguir vivo en memoria después de que su vista se destruye (por ejemplo, al navegar a otra pantalla). Si no limpiamos la referencia al binding, el Fragment retiene toda la vista en memoria — lo que se llama un **memory leak**.

---

## 5. ViewBinding — Acceso seguro a las vistas

ViewBinding genera automáticamente una clase Kotlin para cada archivo XML. Esto elimina los `findViewById()` que podían causar crashes en runtime.

```kotlin
// Sin ViewBinding — frágil
val button = findViewById<Button>(R.id.btnLogin)  // crash si el ID no existe

// Con ViewBinding — seguro en tiempo de compilación
binding.btnLogin.setOnClickListener { login() }   // error en compilación, no en runtime
```

El patrón estándar en la app usa una variable nullable que se limpia al destruir la vista:

```kotlin
private var _binding: FragmentDiscoverBinding? = null
private val binding get() = _binding!!  // el !! es seguro porque solo se accede entre onCreateView y onDestroyView
```

---

## 6. activityViewModels — ViewModels compartidos entre pantallas

En la app, los ViewModels se obtienen con `activityViewModels()`:

```kotlin
private val viewModel: RecipeViewModel by activityViewModels()
```

Esto significa que el mismo `RecipeViewModel` es compartido por **todos los Fragments** que lo pidan. No se crea uno nuevo por Fragment.

**¿Para qué sirve esto?**
Cuando el usuario toca una receta en `DiscoverFragment`, el Fragment guarda la receta seleccionada en el ViewModel:

```kotlin
viewModel.setSelectedMeal(meal)
findNavController().navigate(R.id.action_discoverFragment_to_recipeDetailFragment)
```

Luego `RecipeDetailFragment` lee esa misma receta del mismo ViewModel:

```kotlin
viewModel.selectedMeal.observe(viewLifecycleOwner) { meal ->
    // aquí está la receta que eligió el usuario en DiscoverFragment
    binding.tvRecipeTitle.text = meal.name
}
```

Sin `activityViewModels()`, el `RecipeDetailFragment` tendría un ViewModel diferente y no vería la receta seleccionada.

---

## 7. Flujo completo: abrir una receta

Para entender cómo todo se conecta, esto es lo que pasa cuando el usuario toca una receta:

```
1. DiscoverFragment — usuario toca una tarjeta de receta
   ↓
   discoverAdapter = RecipeAdapter { meal ->
       viewModel.setSelectedMeal(meal)   // guarda en LiveData
       findNavController().navigate(...)  // navega
   }

2. RecipeDetailFragment se crea (onCreateView → onViewCreated)
   ↓
   viewModel.selectedMeal.observe(viewLifecycleOwner) { meal ->
       meal ?: return@observe  // si es null, no hace nada

       // Si la receta no tiene instrucciones (vino de un filtro),
       // pide el detalle completo a TheMealDB
       if (meal.instructions == null) {
           viewModel.getMealById(meal.id)
           return@observe
       }

       // Carga la imagen con Glide
       Glide.with(this).load(meal.thumbnail).into(binding.ivRecipeHeader)

       // Muestra datos básicos
       binding.tvRecipeTitle.text = meal.name
       binding.tvInstructions.text = "Traduciendo..."  // placeholder

       // Inicia la traducción en background
       viewModel.translateMeal(meal)
   }

3. Mientras tanto, translateMeal() trabaja en background
   ↓
   viewModel.translatedContent.observe(viewLifecycleOwner) { content ->
       // Cuando termina, actualiza el texto
       binding.tvRecipeTitle.text  = content.name
       binding.tvInstructions.text = content.instructions
   }
```

---

## 8. Coroutines — Cómo la app hace trabajo en background

Todas las llamadas a APIs se hacen en **coroutines**, que son funciones que pueden pausarse y reanudarse sin bloquear el hilo principal.

En Android, el hilo principal (UI thread) es el que dibuja la pantalla. Si se bloquea — aunque sea por 2 segundos esperando una respuesta de red — la app se congela y Android muestra "La app no responde".

```kotlin
// En RecipeViewModel:
fun loadDiscoverRecipes() {
    viewModelScope.launch {       // inicia una coroutine en el scope del ViewModel
        _isLoading.value = true   // actualiza LiveData → Fragment muestra spinner
        try {
            val letters = listOf("c", "b", "s", "p")
            val meals = mutableListOf<Meal>()
            for (letter in letters) {
                val response = api.getMealsByLetter(letter)  // suspende aquí, espera la respuesta
                response.meals?.take(3)?.let { meals.addAll(it) }
            }
            _randomMeals.value = meals.shuffled().take(10)  // actualiza LiveData
            _error.value = null
        } catch (e: UnknownHostException) {
            _error.value = "Sin conexión a internet"
        } finally {
            _isLoading.value = false  // esconde el spinner
        }
    }
}
```

`viewModelScope.launch` inicia la coroutine ligada al ciclo de vida del ViewModel. Si el usuario cierra la pantalla mientras carga, la coroutine se cancela automáticamente.

---

## 9. Navegación — NavComponent y MainActivity

Toda la navegación ocurre dentro de `MainActivity`, que contiene un `NavHostFragment`. El `nav_graph.xml` define todos los destinos y las rutas entre ellos.

`MainActivity` tiene una lógica importante: esconde la barra de navegación inferior cuando el usuario está en pantallas de detalle o autenticación:

```kotlin
navController.addOnDestinationChangedListener { _, destination, _ ->
    val hideNav = destination.id in setOf(
        R.id.recipeDetailFragment,
        R.id.chefProfileFragment,
        R.id.loginFragment,
        R.id.registerFragment,
        R.id.myRecipeDetailFragment
    )
    binding.bottomNavigation.visibility = if (hideNav) View.GONE else View.VISIBLE
}
```

Para navegar entre pantallas desde un Fragment:

```kotlin
// Ir a una pantalla específica
findNavController().navigate(R.id.action_discoverFragment_to_recipeDetailFragment)

// Volver atrás (equivalente al botón de atrás)
findNavController().navigateUp()
```

---

## 10. RecyclerView y Adaptadores

`RecyclerView` es el componente que muestra listas eficientes. Recicla las vistas que salen de la pantalla para reutilizarlas con nuevos datos, en lugar de crear miles de vistas.

Cada lista necesita un adaptador. En `DiscoverFragment` hay dos:

```kotlin
// Carrusel horizontal (Hidden Gems)
hiddenGemsAdapter = RecipeHorizontalAdapter { meal ->
    viewModel.setSelectedMeal(meal)
    findNavController().navigate(R.id.action_discoverFragment_to_recipeDetailFragment)
}
binding.rvHiddenGems.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
binding.rvHiddenGems.adapter = hiddenGemsAdapter

// Cuadrícula 2 columnas (Descubrir)
discoverAdapter = RecipeAdapter { meal ->
    viewModel.setSelectedMeal(meal)
    findNavController().navigate(R.id.action_discoverFragment_to_recipeDetailFragment)
}
binding.rvDiscoverRecipes.layoutManager = GridLayoutManager(context, 2)
binding.rvDiscoverRecipes.adapter = discoverAdapter
```

Cuando llegan datos nuevos, se actualiza con `submitList()`:

```kotlin
viewModel.randomMeals.observe(viewLifecycleOwner) { meals ->
    discoverAdapter.submitList(meals)   // RecyclerView calcula qué cambió y actualiza solo eso
}
```

**Tabla de adaptadores en la app:**

| Adaptador | Tipo de lista | ¿Dónde se usa? |
|---|---|---|
| `RecipeAdapter` | Grid 2 columnas | Discover |
| `RecipeHorizontalAdapter` | Carrusel horizontal | Home y Discover (Hidden Gems) |
| `FavoriteAdapter` | Grid de favoritos | Favorites |
| `MyRecipeAdapter` | Mis recetas publicadas | Profile |
| `ChefAdapter` | Cocineros seguidos | Profile |
| `CommentAdapter` | Comentarios | MyRecipeDetail |
| `CommunityAdapter` | Comunidades temáticas | Communities |
| `IngredientAdapter` | Ingredientes de una receta | RecipeDetail |

---

## 11. SessionManager — La sesión del usuario

`SessionManager` es un objeto singleton (existe una sola instancia en toda la app) que guarda los datos del usuario en `SharedPreferences` — almacenamiento persistente del dispositivo.

```kotlin
object SessionManager {
    fun saveSession(userId, token, email, refreshToken)  // login exitoso
    fun getToken(): String?        // JWT para autorizar peticiones
    fun getUserId(): String?       // UUID del usuario en Supabase
    fun isLoggedIn(): Boolean      // ¿hay sesión activa?
    fun isSessionExpired(): Boolean  // timeout de 30 min
    fun saveDisplayName(name)      // persiste nombre del usuario
    fun saveFollowedChefs(...)     // persiste cocineros seguidos
    fun clearSession()             // logout — borra solo tokens
}
```

**Decisión importante en el código:** `clearSession()` borra los tokens pero NO el nombre ni los cocineros seguidos. Estos están guardados con la clave `display_name_$userId` y `follows_$userId`. Si el mismo usuario vuelve a iniciar sesión, recupera todo su estado:

```kotlin
fun clearSession() {
    prefs.edit()
        .remove("user_id")      // se borra
        .remove("token")        // se borra
        .remove("email")        // se borra
        .remove("last_active")  // se borra
        // "display_name_$userId" y "follows_$userId" NO se borran
        .apply()
}
```

---

## 12. Decisiones de diseño UI

- **Naranja primario `#F4831F`** — color consistente en toda la app
- **Material Design** — `MaterialButton`, `TextInputLayout` con el estilo estándar de Google
- **Header naranja fijo** en todas las pantallas para identidad visual
- **Estados vacíos en todas las listas** — si no hay datos, se muestra un mensaje amigable en lugar de una lista en blanco
- **Bottom nav oculta** en pantallas de detalle y autenticación para no distraer
- **Animaciones slide** entre pantallas (definidas en `res/anim/`)

---

## Preguntas frecuentes

**¿Por qué una Activity y no varias?**
El patrón de "Single Activity" es el estándar moderno en Android. Una Activity actúa como contenedor; los Fragments son las pantallas. Esto permite transiciones animadas, compartir ViewModels y un backstack de navegación limpio.

**¿Cuál es la diferencia entre `randomMeals` y `_randomMeals`?**
`_randomMeals` es `MutableLiveData` — se puede escribir, pero solo desde dentro del ViewModel (es `private`). `randomMeals` es `LiveData` — solo lectura para quien lo observe desde afuera. Esto evita que un Fragment modifique datos que no le corresponden.

**¿Qué es `submitList()` y por qué es mejor que `notifyDataSetChanged()`?**
`submitList()` usa `DiffUtil` por debajo: compara la lista nueva con la anterior elemento a elemento y solo anima los cambios específicos. `notifyDataSetChanged()` redibuja toda la lista de golpe, lo que es más lento y no tiene animación.

**¿Por qué `viewLifecycleOwner` en los observers?**
Un Fragment puede existir en memoria sin su vista (al navegar a otra pantalla y volver). Si usamos `this` como owner, el observer sigue activo durante ese tiempo y podría intentar actualizar una vista que no existe → crash. `viewLifecycleOwner` desactiva el observer automáticamente cuando la vista se destruye.

**¿Qué pasa si no hay internet?**
El ViewModel captura `UnknownHostException` y actualiza `_error.value` con un mensaje. El Fragment observa ese `error` LiveData y muestra el panel de "sin conexión" con el botón "Reintentar".
