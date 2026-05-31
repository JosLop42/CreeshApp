-- CreeshApp - Base de datos
-- Pega esto en Supabase > SQL Editor y presiona Run


-- PERFILES DE USUARIO
-- Supabase guarda el email/password en auth.users
-- Aqui guardamos el resto del perfil
CREATE TABLE profiles (
    id          UUID        PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username    TEXT        UNIQUE NOT NULL,
    full_name   TEXT        NOT NULL DEFAULT '',
    bio         TEXT        DEFAULT '',
    avatar_url  TEXT,
    country     TEXT,
    specialty   TEXT,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- Crear perfil automaticamente cuando alguien se registra
CREATE OR REPLACE FUNCTION create_profile_on_signup()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO profiles (id, username, full_name)
    VALUES (
        NEW.id,
        split_part(NEW.email, '@', 1),
        split_part(NEW.email, '@', 1)
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_signup
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION create_profile_on_signup();


-- CATEGORIAS DE COMIDA
CREATE TABLE categories (
    id      SERIAL  PRIMARY KEY,
    name    TEXT    UNIQUE NOT NULL,
    name_es TEXT    NOT NULL
);

INSERT INTO categories (name, name_es) VALUES
    ('Beef',          'Carnes'),
    ('Chicken',       'Pollo'),
    ('Seafood',       'Mariscos'),
    ('Vegetarian',    'Vegetariano'),
    ('Vegan',         'Vegano'),
    ('Pasta',         'Pasta'),
    ('Dessert',       'Postres'),
    ('Breakfast',     'Desayunos'),
    ('Miscellaneous', 'Variado'),
    ('Chinese',       'Cocina Asiatica'),
    ('Mexican',       'Mexicana');


-- RECETAS
CREATE TABLE recipes (
    id              UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
    author_id       UUID        REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    title           TEXT        NOT NULL,
    title_es        TEXT,
    description     TEXT,
    image_url       TEXT,
    instructions    TEXT,
    instructions_es TEXT,
    category_id     INT         REFERENCES categories(id) ON DELETE SET NULL,
    country         TEXT,
    prep_time_min   INT,
    cook_time_min   INT,
    servings        INT         DEFAULT 2,
    difficulty      TEXT        CHECK (difficulty IN ('facil', 'medio', 'dificil')),
    external_id     TEXT,
    is_published    BOOLEAN     DEFAULT true,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);


-- INGREDIENTES
CREATE TABLE ingredients (
    id          SERIAL  PRIMARY KEY,
    recipe_id   UUID    REFERENCES recipes(id) ON DELETE CASCADE NOT NULL,
    name        TEXT    NOT NULL,
    quantity    TEXT,
    unit        TEXT,
    sort_order  INT     DEFAULT 0
);


-- SEGUIMIENTOS (quien sigue a quien)
CREATE TABLE follows (
    follower_id     UUID    REFERENCES profiles(id) ON DELETE CASCADE,
    following_id    UUID    REFERENCES profiles(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (follower_id, following_id)
);


-- FAVORITOS (recetas externas de TheMealDB)
-- user_id es TEXT para compatibilidad directa con el JWT uid de Supabase
CREATE TABLE favorites (
    id            SERIAL      PRIMARY KEY,
    user_id       TEXT        NOT NULL,
    meal_id       TEXT        NOT NULL,
    meal_title    TEXT        NOT NULL,
    meal_image    TEXT,
    meal_category TEXT,
    created_at    TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, meal_id)
);


-- COMENTARIOS
CREATE TABLE comments (
    id          UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id     UUID        REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    recipe_id   UUID        REFERENCES recipes(id) ON DELETE CASCADE NOT NULL,
    content     TEXT        NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);


-- COMUNIDADES
CREATE TABLE communities (
    id          SERIAL  PRIMARY KEY,
    name        TEXT    NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO communities (name, description) VALUES
    ('Gym Rats',           'Recetas altas en proteina para deportistas'),
    ('Vegano',             'Cocina vegana'),
    ('Vegetariano',        'Recetas sin carne'),
    ('Amantes de la Carne','Recetas con carne'),
    ('Mariscos',           'Pescados y mariscos'),
    ('Postres y Dulces',   'Reposteria y postres'),
    ('Pasta e Italiana',   'Cocina italiana'),
    ('Cocina Asiatica',    'Gastronomia asiatica'),
    ('Variado',            'Todo tipo de recetas');


-- MIEMBROS DE COMUNIDADES
CREATE TABLE community_members (
    community_id    INT     REFERENCES communities(id) ON DELETE CASCADE,
    user_id         UUID    REFERENCES profiles(id) ON DELETE CASCADE,
    joined_at       TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (community_id, user_id)
);


-- MENSAJES DIRECTOS
CREATE TABLE messages (
    id          UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
    sender_id   UUID        REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    receiver_id UUID        REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    content     TEXT        NOT NULL,
    is_read     BOOLEAN     DEFAULT false,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);


-- PERMISOS (Row Level Security)
ALTER TABLE profiles          ENABLE ROW LEVEL SECURITY;
ALTER TABLE recipes           ENABLE ROW LEVEL SECURITY;
ALTER TABLE ingredients       ENABLE ROW LEVEL SECURITY;
ALTER TABLE follows           ENABLE ROW LEVEL SECURITY;
ALTER TABLE favorites         ENABLE ROW LEVEL SECURITY;
ALTER TABLE comments          ENABLE ROW LEVEL SECURITY;
ALTER TABLE communities       ENABLE ROW LEVEL SECURITY;
ALTER TABLE community_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages          ENABLE ROW LEVEL SECURITY;

CREATE POLICY "ver perfiles"           ON profiles          FOR SELECT USING (true);
CREATE POLICY "crear perfil"           ON profiles          FOR INSERT WITH CHECK (true);
CREATE POLICY "editar mi perfil"       ON profiles          FOR UPDATE USING (auth.uid() = id);

CREATE POLICY "ver recetas"            ON recipes           FOR SELECT USING (is_published = true OR auth.uid() = author_id);
CREATE POLICY "crear receta"           ON recipes           FOR INSERT WITH CHECK (auth.uid() = author_id);
CREATE POLICY "editar receta"          ON recipes           FOR UPDATE USING (auth.uid() = author_id);
CREATE POLICY "borrar receta"          ON recipes           FOR DELETE USING (auth.uid() = author_id);

CREATE POLICY "ver ingredientes"       ON ingredients       FOR SELECT USING (true);
CREATE POLICY "gestionar ingredientes" ON ingredients       FOR ALL USING (EXISTS (SELECT 1 FROM recipes r WHERE r.id = recipe_id AND r.author_id = auth.uid()));

CREATE POLICY "ver follows"            ON follows           FOR SELECT USING (true);
CREATE POLICY "seguir"                 ON follows           FOR INSERT WITH CHECK (auth.uid() = follower_id);
CREATE POLICY "dejar de seguir"        ON follows           FOR DELETE USING (auth.uid() = follower_id);

CREATE POLICY "ver favoritos"          ON favorites         FOR SELECT USING (true);
CREATE POLICY "guardar favorito"       ON favorites         FOR INSERT WITH CHECK (true);
CREATE POLICY "quitar favorito"        ON favorites         FOR DELETE USING (true);

CREATE POLICY "ver comentarios"        ON comments          FOR SELECT USING (true);
CREATE POLICY "comentar"               ON comments          FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "borrar comentario"      ON comments          FOR DELETE USING (auth.uid() = user_id);

CREATE POLICY "ver comunidades"        ON communities       FOR SELECT USING (true);
CREATE POLICY "ver miembros"           ON community_members FOR SELECT USING (true);
CREATE POLICY "unirse"                 ON community_members FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "salir"                  ON community_members FOR DELETE USING (auth.uid() = user_id);

CREATE POLICY "ver mensajes"           ON messages          FOR SELECT USING (auth.uid() = sender_id OR auth.uid() = receiver_id);
CREATE POLICY "enviar mensaje"         ON messages          FOR INSERT WITH CHECK (auth.uid() = sender_id);
