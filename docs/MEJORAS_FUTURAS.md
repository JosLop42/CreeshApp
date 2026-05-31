# CreeshApp — Mejoras Futuras

> Este documento describe qué funciona de forma limitada hoy y qué se puede mejorar en versiones futuras. Está organizado por área para que sea fácil de priorizar.

---

## Índice

1. [Subida de imágenes](#1-subida-de-imágenes)
2. [Categorización inteligente de recetas](#2-categorización-inteligente-de-recetas)
3. [Búsqueda y descubrimiento](#3-búsqueda-y-descubrimiento)
4. [Comunidades](#4-comunidades)
5. [Perfil de usuario](#5-perfil-de-usuario)
6. [Seguridad](#6-seguridad)
7. [Base de datos y rendimiento](#7-base-de-datos-y-rendimiento)
8. [Funciones sociales](#8-funciones-sociales)
9. [Experiencia de usuario general](#9-experiencia-de-usuario-general)
10. [Funciones completamente nuevas](#10-funciones-completamente-nuevas)

---

## 1. Subida de imágenes

### Estado actual

Al subir una receta, el usuario puede adjuntar una foto. Sin embargo, la imagen se sube sin ningún tipo de validación previa: si es demasiado grande, si el formato es incorrecto, o si la conexión falla a la mitad, la app puede quedarse colgada o mostrar la receta sin imagen sin explicar por qué.

Hay otro problema más silencioso: si la imagen se sube correctamente a Supabase Storage pero luego falla al guardar la receta en la base de datos, la imagen queda ahí ocupando espacio sin ninguna receta que la use. Con el tiempo, el storage se llena de imágenes huérfanas.

### Mejoras propuestas

**Validación antes de subir:**
- Limitar el tamaño máximo a 5 MB antes de siquiera intentar subir
- Verificar que el archivo sea realmente una imagen (JPG, PNG, WEBP)
- Comprimir automáticamente la imagen antes de subirla para reducir tiempos y uso de almacenamiento

**Subida más robusta:**
- Mostrar una barra de progreso real mientras se sube la imagen
- Reintentar automáticamente si falla por un problema de red temporal
- Si la receta no se crea en la BD tras subir la imagen, eliminar la imagen también (para no dejar archivos huérfanos)

**Mejor feedback al usuario:**
- Mostrar un preview de la foto antes de confirmar
- Si la foto falla pero la receta se guarda, decirle al usuario de forma clara y darle la opción de agregar la foto después

---

## 2. Categorización inteligente de recetas

### Estado actual

Cuando un usuario sube una receta, elige manualmente a qué comunidad pertenece (Vegano, Gym Rats, Mariscos, etc.). No hay ninguna validación ni sugerencia. Esto significa que un usuario puede subir una receta de pastel y ponerla en la comunidad de "Pasta e Italiana", o subir algo con carne y marcarlo como "Vegano". El resultado es que las comunidades se llenan de recetas que no corresponden.

Además, las recetas que los usuarios suben nunca aparecen en la sección de Descubrir ni en los filtros de comunidades — solo se pueden ver en el perfil del autor. En la práctica, el contenido de la comunidad viene únicamente de TheMealDB, no de los usuarios reales de la app.

### Mejoras propuestas

**Validación básica de consistencia:**
- Al elegir la comunidad "Vegano" o "Vegetariano", advertir si los ingredientes contienen carne, pollo o mariscos
- Lista de ingredientes incompatibles por comunidad para detección simple

**Sugerencia automática de comunidad:**
- Al escribir el título y los ingredientes, sugerir automáticamente qué comunidad encaja mejor
- Podría usarse la API de TheMealDB para detectar la categoría más cercana, o un modelo simple de palabras clave

**Sistema de etiquetas:**
- Además de la comunidad, permitir al usuario agregar etiquetas libres (sin gluten, rápido, para niños, etc.)
- Las etiquetas permiten una búsqueda mucho más precisa que las 9 comunidades actuales

**Moderación ligera:**
- Usuarios pueden reportar recetas en la comunidad equivocada
- Umbral simple: si X reportes, la receta se oculta automáticamente hasta revisión

---

## 3. Búsqueda y descubrimiento

### Estado actual

La búsqueda solo funciona sobre las recetas de TheMealDB. Si un usuario publica su propia receta, nadie puede encontrarla buscando — solo el autor puede verla en su perfil. Además, la búsqueda dispara una petición a la API con cada letra que el usuario escribe. Si alguien busca "chocolate", se hacen 9 peticiones en menos de un segundo.

Las recetas de la pantalla de Descubrir son siempre las mismas: el algoritmo busca recetas cuyo nombre empieza con las letras "c", "b", "s", "p", toma 3 de cada una y las mezcla. El resultado es que los mismos platos aparecen casi siempre.

### Mejoras propuestas

**Búsqueda unificada:**
- Buscar en TheMealDB y en las recetas publicadas por usuarios al mismo tiempo
- Mostrar ambas fuentes con una separación visual clara ("Recetas internacionales" / "De la comunidad")

**Búsqueda con delay (debounce):**
- Esperar 400ms después del último carácter antes de lanzar la petición
- Esto reduce las llamadas a la API de 9 a 1 en la práctica y mejora la experiencia

**Descubrir más variado:**
- Rotar las recetas que se muestran en la pantalla de Descubrir usando más letras o aleatoriedad real
- Incluir recetas de usuarios destacados o recientemente publicadas

**Paginación:**
- Cargar las recetas de a 20, con scroll infinito para pedir las siguientes
- Actualmente se cargan todas de golpe, lo que se vuelve lento con muchas recetas

---

## 4. Comunidades

### Estado actual

Las comunidades están escritas directamente en el código de la app. Aunque la base de datos tiene una tabla `communities` con 9 registros, la app no la consulta: los nombres, descripciones y contadores de miembros son texto estático. El contador "12.4k miembros" que aparece en pantalla no está conectado a ningún dato real. Si se agrega una comunidad nueva en la base de datos, no aparecerá en la app sin actualizar el código.

Tampoco existe el concepto de "unirse" a una comunidad. El usuario puede filtrar recetas por comunidad, pero no hay membresía real.

### Mejoras propuestas

**Comunidades dinámicas:**
- Cargar la lista de comunidades desde la base de datos en lugar de tenerla en el código
- Agregar nuevas comunidades desde la base de datos sin necesidad de actualizar la app

**Membresía real:**
- Botón "Unirse" en cada comunidad
- Contador real de miembros basado en la tabla `community_members` (ya existe en la BD, solo falta usarla)
- Sección "Mis comunidades" en el perfil

**Recetas de usuarios en comunidades:**
- Cuando el usuario filtra por una comunidad, ver tanto las recetas de TheMealDB como las recetas que usuarios subieron a esa comunidad
- Actualmente solo aparecen las externas

**Feed de comunidad:**
- Una sección dentro de cada comunidad que muestre las recetas publicadas más recientes

---

## 5. Perfil de usuario

### Estado actual

El perfil solo permite cambiar el nombre. El resto de los campos del schema de la base de datos (bio, país, especialidad, foto de perfil) existen en la tabla pero nunca se cargan ni se muestran en la app. La foto de perfil siempre muestra las iniciales, sin opción de cambiarla. Los botones "Cambiar contraseña" y "Preferencias de email" en la pantalla de Ajustes solo muestran un mensaje de aviso y no hacen nada.

### Mejoras propuestas

**Edición completa del perfil:**
- Editar bio, país, especialidad y foto de perfil
- La foto ya tiene soporte en Supabase Storage, solo falta conectarlo

**Cambio de contraseña real:**
- Supabase Auth permite cambiar contraseña con el endpoint `/auth/v1/user`
- Solo necesita ser conectado al botón existente

**Estadísticas del perfil:**
- Cantidad de recetas publicadas (conectado a datos reales)
- Cantidad de comentarios recibidos
- Seguidores reales desde la tabla `follows`

**Recetas propias en búsqueda:**
- Las recetas del usuario actualmente solo son visibles en su perfil
- Podrían aparecer en los resultados de búsqueda globales

---

## 6. Seguridad

### Estado actual

Hay dos problemas concretos en la seguridad actual:

Primero, la clave de acceso a Supabase Storage está escrita directamente en el código fuente de la app. Cualquier persona que descargue el APK puede extraerla y usarla para acceder al storage.

Segundo, la tabla `favorites` en la base de datos tiene sus reglas de seguridad configuradas con `USING (true)`, lo que significa que cualquier usuario autenticado puede ver, agregar o eliminar los favoritos de cualquier otro usuario.

### Mejoras propuestas

**Credenciales fuera del código:**
- Mover todas las claves API a `local.properties` (que está en `.gitignore`) o a variables de entorno en el build
- Usar `BuildConfig` para acceder a ellas en el código sin exponerlas en el repositorio

**Corregir RLS en favoritos:**
```sql
-- Cambiar la política actual por esto:
CREATE POLICY "ver propios favoritos"
ON favorites FOR SELECT
USING (user_id = auth.uid()::text);

CREATE POLICY "guardar favorito"
ON favorites FOR INSERT
WITH CHECK (user_id = auth.uid()::text);

CREATE POLICY "quitar favorito"
ON favorites FOR DELETE
USING (user_id = auth.uid()::text);
```

**Validación de tamaño de imagen:**
- Limitar el tamaño en la app antes de intentar subir, para evitar crashes de memoria con imágenes muy grandes

---

## 7. Base de datos y rendimiento

### Estado actual

Las consultas a la base de datos no tienen límite de resultados. Si un usuario tiene muchas recetas, se descargan todas a la vez. No hay índices en las columnas más consultadas, por lo que las búsquedas se vuelven lentas conforme crece la base de datos. Tampoco existe un mecanismo para borrar registros sin perderlos (soft delete), lo que hace difícil recuperar algo eliminado por error.

### Mejoras propuestas

**Paginación en todas las listas:**
- Usar `LIMIT` y `OFFSET` en todas las queries (recetas, comentarios, favoritos)
- Cargar de a 20 registros con scroll infinito

**Índices de rendimiento:**
```sql
CREATE INDEX idx_recipes_author ON recipes(author_id);
CREATE INDEX idx_favorites_user ON favorites(user_id);
CREATE INDEX idx_comments_recipe ON comments(recipe_id);
CREATE INDEX idx_recipes_community ON recipes(community_id);
```

**Soft delete:**
- Agregar columna `deleted_at` en recetas y comentarios
- Ocultar en lugar de borrar, para poder recuperar si fue un error

**Campo `updated_at` en recetas:**
- Permitir editar recetas después de publicarlas
- El campo `updated_at` llevaría registro de cuándo fue la última modificación

---

## 8. Funciones sociales

### Estado actual

La tabla `follows` existe en la base de datos y los cocineros virtuales se pueden "seguir", pero el seguimiento se guarda localmente en el teléfono (SharedPreferences), no en la base de datos. Si el usuario cambia de teléfono o reinstala la app, pierde sus seguimientos. Además, no hay notificaciones ni manera de saber cuándo alguien nuevo publicó algo.

La tabla de mensajes directos existe en el schema pero nunca fue implementada en la app.

### Mejoras propuestas

**Seguimiento persistente en la nube:**
- Guardar los follows en la tabla `follows` de Supabase en lugar de en el teléfono
- Migrar del guardado local al guardado en la nube

**Feed de seguidos:**
- Una sección en Home que muestre las últimas recetas de los cocineros o usuarios que sigues

**Notificaciones push:**
- Cuando alguien comenta tu receta
- Cuando alguien empieza a seguirte
- Cuando un usuario que sigues publica algo nuevo

**Mensajes directos:**
- La tabla `messages` ya existe en la base de datos
- Implementar la pantalla de chat que complementa la funcionalidad social

---

## 9. Experiencia de usuario general

### Estado actual

Algunos detalles de UX que actualmente están incompletos o podrían mejorar:

- Al escribir una búsqueda, se lanza una petición con cada letra (sin espera)
- No hay indicador de carga en algunas pantallas mientras los datos llegan
- Si el usuario no tiene internet, la app falla sin un mensaje claro
- Los favoritos solo pueden quitarse desde la pantalla de detalle, no desde la lista de favoritos
- No hay forma de editar o eliminar una receta propia una vez publicada
- La pantalla de ajustes tiene botones que no funcionan

### Mejoras propuestas

**Modo offline básico:**
- Guardar en caché las últimas recetas vistas
- Mostrar contenido guardado cuando no hay internet, con un aviso de que no está actualizado

**Editar y eliminar recetas propias:**
- Formulario de edición con los mismos campos que la subida
- Confirmación antes de eliminar

**Quitar favorito desde la lista:**
- Swipe hacia un lado en la lista de favoritos para quitar directamente
- Sin necesidad de entrar al detalle

**Mejor estado de carga:**
- Skeleton screens (siluetas grises) mientras cargan las listas
- Mejor que un spinner que no comunica progreso

**Compartir receta:**
- Botón para compartir el nombre y la imagen de una receta por WhatsApp, Instagram u otras apps

---

## 10. Funciones completamente nuevas

Estas no existen en ninguna forma actualmente pero son la dirección natural de la app:

**Sistema de valoraciones (estrellas o likes entre usuarios):**
- Actualmente solo se puede guardar como favorito (propio)
- Un sistema de "me gusta" o puntuación de 1–5 estrellas en recetas de usuarios

**Planificador de menú semanal:**
- El usuario arrastra recetas a días de la semana
- La app genera la lista de compras con todos los ingredientes consolidados

**Lista de compras inteligente:**
- Al guardar una receta como favorita, opción de agregar sus ingredientes a la lista de compras
- La lista agrupa ingredientes repetidos y suma cantidades

**Recetas por ingredientes disponibles:**
- El usuario indica qué ingredientes tiene en casa
- La app sugiere qué recetas puede preparar con lo que tiene

**Versión web / PWA:**
- Complemento web para los usuarios que prefieren ver recetas desde el computador

---

## Prioridad sugerida

| Prioridad | Mejora | Impacto |
|---|---|---|
| **Alta** | Validación y compresión de imágenes al subir | Evita crashes y storage sucio |
| **Alta** | Corregir RLS en tabla favorites | Seguridad real de datos |
| **Alta** | Mover API keys fuera del código fuente | Seguridad del proyecto |
| **Alta** | Categorización correcta al subir receta | Integridad del contenido |
| **Media** | Recetas de usuarios visibles en búsqueda y comunidades | Cierra el ciclo de publicación |
| **Media** | Paginación en listas | Rendimiento con más usuarios |
| **Media** | Seguimiento de cocineros en la nube | Persistencia real |
| **Media** | Edición y eliminación de recetas propias | Funcionalidad básica esperada |
| **Baja** | Mensajes directos | Ya tiene tabla en BD |
| **Baja** | Lista de compras y planificador | Diferenciador de valor |
