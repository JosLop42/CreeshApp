package com.creesh.app.api.models

import com.google.gson.annotations.SerializedName
import kotlin.math.abs

// ── randomuser.me response models ─────────────────────────────────────────────

data class RandomUserResponse(
    @SerializedName("results") val results: List<RandomUser>
)

data class RandomUser(
    @SerializedName("name")     val name: RandomUserName,
    @SerializedName("email")    val email: String,
    @SerializedName("picture")  val picture: RandomUserPicture,
    @SerializedName("login")    val login: RandomUserLogin,
    @SerializedName("location") val location: RandomUserLocation
)

data class RandomUserName(
    @SerializedName("first") val first: String,
    @SerializedName("last")  val last: String
)

data class RandomUserPicture(
    @SerializedName("large")     val large: String,
    @SerializedName("medium")    val medium: String,
    @SerializedName("thumbnail") val thumbnail: String
)

data class RandomUserLogin(
    @SerializedName("username") val username: String
)

data class RandomUserLocation(
    @SerializedName("country") val country: String
)

// ── App-level Chef model ───────────────────────────────────────────────────────

data class Chef(
    val id: String,
    val fullName: String,
    val username: String,
    val photoUrl: String,
    val country: String,
    val specialty: String,
    val bio: String,
    val followerCount: Int,
    val recipeCount: Int
)

private val SPECIALTIES = listOf(
    "Cocina Mediterránea", "Cocina Asiática", "Repostería & Postres",
    "Cocina Mexicana", "Comida Italiana", "Gastronomía Molecular",
    "Cocina Vegetariana", "BBQ & Parrillas", "Cocina Francesa",
    "Cocina Peruana", "Fusión Internacional", "Street Food"
)

private val BIOS = listOf(
    "Apasionado por los sabores auténticos y la cocina de temporada.",
    "Chef con más de 10 años de experiencia en alta cocina.",
    "Explorando el mundo a través de sus recetas y tradiciones culinarias.",
    "Especialista en técnicas clásicas con reinterpretaciones modernas.",
    "Creo que la buena comida une a las personas. ¡Únete a mi comunidad!",
    "Del huerto a la mesa: recetas frescas y llenas de sabor.",
    "Transformando ingredientes simples en experiencias memorables.",
    "Mi cocina es mi laboratorio. Cada plato, un experimento con amor.",
    "Viajero gastronómico compartiendo recetas de todo el mundo.",
    "La cocina tradicional de mi abuela, con un toque contemporáneo.",
    "Promoviendo la alimentación sana sin sacrificar el sabor.",
    "Street food y comfort food: lo mejor de la cocina callejera."
)

fun RandomUser.toChef(): Chef {
    val hash = abs(login.username.hashCode())
    return Chef(
        id          = login.username,
        fullName    = "${name.first} ${name.last}",
        username    = "@${login.username}",
        photoUrl    = picture.large,
        country     = location.country,
        specialty   = SPECIALTIES[hash % SPECIALTIES.size],
        bio         = BIOS[hash % BIOS.size],
        followerCount = 500 + (hash % 9500),
        recipeCount   = 5   + (hash % 95)
    )
}

// Chefs locales usados como fallback si la red falla
fun fallbackChefs(): List<Chef> = listOf(
    Chef("chef_carlos",  "Carlos Martínez", "@carloschef",
        "https://randomuser.me/api/portraits/men/32.jpg",
        "España", "Cocina Mediterránea",
        "Apasionado por los sabores del mar y la huerta.", 4820, 67),
    Chef("chef_ana",     "Ana López",       "@anacocina",
        "https://randomuser.me/api/portraits/women/44.jpg",
        "México", "Cocina Mexicana",
        "Rescatando las recetas tradicionales de mi abuela.", 7340, 89),
    Chef("chef_marco",   "Marco Rossi",     "@marcorossi",
        "https://randomuser.me/api/portraits/men/55.jpg",
        "Italia", "Comida Italiana",
        "La pasta perfecta es un arte que lleva años dominar.", 9100, 112),
    Chef("chef_yuki",    "Yuki Tanaka",     "@yukitanaka",
        "https://randomuser.me/api/portraits/women/61.jpg",
        "Japón", "Cocina Asiática",
        "Fusionando la cocina japonesa con ingredientes locales.", 6250, 54),
    Chef("chef_laura",   "Laura García",    "@laurapostres",
        "https://randomuser.me/api/portraits/women/22.jpg",
        "Francia", "Repostería & Postres",
        "El dulce es un lenguaje universal. ¡Endúlzate la vida!", 11200, 78),
    Chef("chef_diego",   "Diego Herrera",   "@diegobbq",
        "https://randomuser.me/api/portraits/men/71.jpg",
        "Argentina", "BBQ & Parrillas",
        "El asado perfecto requiere tiempo, paciencia y buen fuego.", 8900, 43),
    Chef("chef_sofia",   "Sofía Chen",      "@sofiafusion",
        "https://randomuser.me/api/portraits/women/33.jpg",
        "Perú", "Fusión Internacional",
        "Donde la tradición peruana se encuentra con el mundo.", 5600, 61),
    Chef("chef_pierre",  "Pierre Dubois",   "@pierrechef",
        "https://randomuser.me/api/portraits/men/18.jpg",
        "Francia", "Cocina Francesa",
        "La cuisine française es arte en cada bocado.", 7800, 94),
    Chef("chef_priya",   "Priya Sharma",    "@priyavegg",
        "https://randomuser.me/api/portraits/women/55.jpg",
        "India", "Cocina Vegetariana",
        "Demostrando que la cocina sin carne puede ser extraordinaria.", 4300, 72),
    Chef("chef_miguel",  "Miguel Torres",   "@miguelstreet",
        "https://randomuser.me/api/portraits/men/42.jpg",
        "Colombia", "Street Food",
        "La comida callejera es la cocina del pueblo, sin filtros.", 6100, 58),
    Chef("chef_aiko",    "Aiko Nakamura",   "@aikogastro",
        "https://randomuser.me/api/portraits/women/78.jpg",
        "Japón", "Gastronomía Molecular",
        "La ciencia al servicio del sabor. Comida que sorprende.", 3900, 35),
    Chef("chef_lucia",   "Lucía Ramírez",   "@luciamex",
        "https://randomuser.me/api/portraits/women/91.jpg",
        "México", "Cocina Mexicana",
        "Los chiles, la masa y el amor: los tres pilares de mi cocina.", 8500, 103)
)
