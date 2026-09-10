package com.example.data

import com.example.R
import com.example.model.Album
import com.example.model.Artist
import com.example.model.LyricLine
import com.example.model.Song

object MusicRepository {

    val initialSongs: List<Song> = listOf(
        Song(
            id = "song_1",
            title = "Midnight Echoes",
            artist = "Aura Synthetica",
            album = "Neon Horizons",
            durationSeconds = 234,
            isFavorite = true,
            drawableRes = R.drawable.cover_neon_dreams,
            gradientColors = listOf(0xFF8B5CF6, 0xFF06B6D4),
            baseFrequencyHz = 220f,
            lyrics = listOf(
                LyricLine(0, "Luces de neón en la ciudad dormida"),
                LyricLine(14, "El eco suave de tus pasos en la avenida"),
                LyricLine(28, "Frecuencias flotando en el aire nocturno"),
                LyricLine(45, "Caminando entre sombras violetas y azul profundo"),
                LyricLine(65, "Todo se siente liviano como un suspiro"),
                LyricLine(85, "Midnight Echoes resonando en la mente"),
                LyricLine(110, "El tiempo se detiene cuando suena este acorde"),
                LyricLine(135, "Vibramos juntos hasta que amanezca"),
                LyricLine(160, "El horizonte brilla con una nueva luz"),
                LyricLine(190, "Ecos infinitos en la oscuridad")
            )
        ),
        Song(
            id = "song_2",
            title = "Sunset Boulevard",
            artist = "Solaris Wave",
            album = "Golden Hour",
            durationSeconds = 198,
            isFavorite = true,
            drawableRes = R.drawable.cover_sunset_vibes,
            gradientColors = listOf(0xFFF97316, 0xFFEC4899),
            baseFrequencyHz = 261.63f,
            lyrics = listOf(
                LyricLine(0, "Atardecer dorado sobre la costa"),
                LyricLine(12, "El viento cálido despeina los recuerdos"),
                LyricLine(26, "Colores naranja y rosa cubren el cielo"),
                LyricLine(42, "Manejando hacia donde el sol se oculta"),
                LyricLine(60, "Melodías nostálgicas que reconfortan el alma"),
                LyricLine(80, "Golden hour en Sunset Boulevard"),
                LyricLine(105, "Cada segundo se siente eterno"),
                LyricLine(130, "La noche nos abraza suavemente"),
                LyricLine(160, "El resplandor dorado nunca se apaga")
            )
        ),
        Song(
            id = "song_3",
            title = "Velvet Rain",
            artist = "Lofi Bloom",
            album = "Midnight Coffee",
            durationSeconds = 172,
            isFavorite = false,
            drawableRes = R.drawable.cover_midnight_echoes,
            gradientColors = listOf(0xFF10B981, 0xFF3B82F6),
            baseFrequencyHz = 196f,
            lyrics = listOf(
                LyricLine(0, "Gotas de lluvia golpean suave la ventana"),
                LyricLine(15, "Una taza caliente y calma en la mirada"),
                LyricLine(32, "Acordes de terciopelo que acompañan el silencio"),
                LyricLine(50, "Paz profunda en este rincón del mundo"),
                LyricLine(70, "Lluvia de terciopelo que lava las dudas"),
                LyricLine(95, "Solo el sonido del compás y la calma"),
                LyricLine(120, "Tiempo para respirar y simplemente estar"),
                LyricLine(145, "Hasta que la tormenta se convierta en sol")
            )
        ),
        Song(
            id = "song_4",
            title = "Starlight Odyssey",
            artist = "Aura Synthetica",
            album = "Neon Horizons",
            durationSeconds = 256,
            isFavorite = true,
            drawableRes = R.drawable.cover_neon_dreams,
            gradientColors = listOf(0xFF6366F1, 0xFFA855F7),
            baseFrequencyHz = 293.66f,
            lyrics = listOf(
                LyricLine(0, "Navegando a través de constelaciones infinitas"),
                LyricLine(18, "El espacio profundo susurra melodías cósmicas"),
                LyricLine(38, "Partículas de luz bailando alrededor"),
                LyricLine(58, "Una odisea interestelar sin mapa ni prisa"),
                LyricLine(85, "Brillo de estrellas iluminando el viaje"),
                LyricLine(115, "Gravedad cero en el pulso de la música"),
                LyricLine(145, "Más allá de las galaxias conocidas"),
                LyricLine(180, "Donde el sonido se convierte en luz eterna")
            )
        ),
        Song(
            id = "song_5",
            title = "Crimson Skyline",
            artist = "Solaris Wave",
            album = "Golden Hour",
            durationSeconds = 210,
            isFavorite = false,
            drawableRes = R.drawable.cover_sunset_vibes,
            gradientColors = listOf(0xFFEF4444, 0xFFF59E0B),
            baseFrequencyHz = 174.61f,
            lyrics = listOf(
                LyricLine(0, "Cielo carmesí sobre los rascacielos"),
                LyricLine(16, "Reflejos de fuego en las ventanas de cristal"),
                LyricLine(35, "El pulso urbano despierta con el ritmo"),
                LyricLine(55, "Bajo este manto rojo ardiente caminamos"),
                LyricLine(80, "La ciudad no duerme, late con nosotros"),
                LyricLine(110, "Energía pura vibrando en cada nota"),
                LyricLine(140, "La línea del horizonte arde con fuerza"),
                LyricLine(175, "Eterno resplandor en la penumbra")
            )
        ),
        Song(
            id = "song_6",
            title = "Deep Resonance",
            artist = "Subtle Pulse",
            album = "Acoustic Silence",
            durationSeconds = 245,
            isFavorite = false,
            drawableRes = null,
            gradientColors = listOf(0xFF14B8A6, 0xFF0EA5E9),
            baseFrequencyHz = 146.83f,
            lyrics = listOf(
                LyricLine(0, "Vibraciones profundas en el centro del ser"),
                LyricLine(20, "Ondas acústicas que limpian la mente"),
                LyricLine(45, "En la resonancia encontramos serenidad"),
                LyricLine(75, "Frecuencias armónicas que restauran el equilibrio"),
                LyricLine(110, "El silencio y el tono en perfecta comunión"),
                LyricLine(150, "Profundo, claro, absoluto"),
                LyricLine(190, "Un eco que permanece")
            )
        ),
        Song(
            id = "song_7",
            title = "Tokyo Reverie",
            artist = "Lofi Bloom",
            album = "Midnight Coffee",
            durationSeconds = 188,
            isFavorite = true,
            drawableRes = R.drawable.cover_midnight_echoes,
            gradientColors = listOf(0xFFEC4899, 0xFF8B5CF6),
            baseFrequencyHz = 246.94f,
            lyrics = listOf(
                LyricLine(0, "Calles iluminadas en Shibuya al anochecer"),
                LyricLine(15, "Pasos ligeros bajo paraguas transparentes"),
                LyricLine(35, "Sonidos de sintetizador mezclados con la brisa"),
                LyricLine(60, "Un sueño despierto en Tokyo Reverie"),
                LyricLine(90, "Melancolía dulce que acompaña el camino"),
                LyricLine(120, "La belleza de lo efímero"),
                LyricLine(150, "Hasta la próxima estación")
            )
        ),
        Song(
            id = "song_8",
            title = "Electric Aurora",
            artist = "Aura Synthetica",
            album = "Cyber Pulse",
            durationSeconds = 222,
            isFavorite = false,
            drawableRes = R.drawable.cover_neon_dreams,
            gradientColors = listOf(0xFF3B82F6, 0xFF06B6D4),
            baseFrequencyHz = 261.63f,
            lyrics = listOf(
                LyricLine(0, "Auroras eléctricas ondeando en el norte"),
                LyricLine(18, "Luz polar danzando con precisión magnética"),
                LyricLine(42, "Cables y naturaleza en sintonía futurista"),
                LyricLine(70, "El cielo se enciende en verde y turquesa"),
                LyricLine(100, "Pulso eléctrico que viaja por las venas"),
                LyricLine(135, "Voz digital que canta a la inmensidad"),
                LyricLine(170, "Un amanecer electromagnético")
            )
        ),
        Song(
            id = "song_9",
            title = "Golden Mirage",
            artist = "Solaris Wave",
            album = "Golden Hour",
            durationSeconds = 265,
            isFavorite = false,
            drawableRes = R.drawable.cover_sunset_vibes,
            gradientColors = listOf(0xFFF59E0B, 0xFFEF4444),
            baseFrequencyHz = 220f,
            lyrics = listOf(
                LyricLine(0, "Espejismo en el desierto al caer la tarde"),
                LyricLine(22, "Dunas doradas que parecen olas del mar"),
                LyricLine(50, "Calor suave que se transforma en misterio"),
                LyricLine(85, "Una ilusión que brilla más que la realidad"),
                LyricLine(125, "Buscando el oasis de la melodía"),
                LyricLine(165, "El viento borra las huellas en la arena"),
                LyricLine(210, "El horizonte dorado permanece intacto")
            )
        ),
        Song(
            id = "song_10",
            title = "Whispers in the Mist",
            artist = "Subtle Pulse",
            album = "Acoustic Silence",
            durationSeconds = 195,
            isFavorite = true,
            drawableRes = null,
            gradientColors = listOf(0xFF64748B, 0xFF334155),
            baseFrequencyHz = 164.81f,
            lyrics = listOf(
                LyricLine(0, "Niebla densa sobre el bosque matutino"),
                LyricLine(18, "Susurros entre los árboles antiguos"),
                LyricLine(40, "La calma precede al despertar del mundo"),
                LyricLine(70, "Gotas de rocío en cada hoja verde"),
                LyricLine(105, "Paz que solo la soledad sabia comprende"),
                LyricLine(140, "El aire limpio que renueva el espíritu"),
                LyricLine(170, "Y la niebla se disipa despacio")
            )
        )
    )

    val artists: List<Artist> = listOf(
        Artist(
            name = "Aura Synthetica",
            songCount = 3,
            albumCount = 2,
            drawableRes = R.drawable.cover_neon_dreams,
            gradientColors = listOf(0xFF8B5CF6, 0xFF06B6D4)
        ),
        Artist(
            name = "Solaris Wave",
            songCount = 3,
            albumCount = 1,
            drawableRes = R.drawable.cover_sunset_vibes,
            gradientColors = listOf(0xFFF97316, 0xFFEC4899)
        ),
        Artist(
            name = "Lofi Bloom",
            songCount = 2,
            albumCount = 1,
            drawableRes = R.drawable.cover_midnight_echoes,
            gradientColors = listOf(0xFF10B981, 0xFF3B82F6)
        ),
        Artist(
            name = "Subtle Pulse",
            songCount = 2,
            albumCount = 1,
            drawableRes = null,
            gradientColors = listOf(0xFF14B8A6, 0xFF6366F1)
        )
    )

    val albums: List<Album> = listOf(
        Album(
            id = "album_1",
            title = "Neon Horizons",
            artist = "Aura Synthetica",
            year = 2025,
            songCount = 2,
            drawableRes = R.drawable.cover_neon_dreams,
            gradientColors = listOf(0xFF8B5CF6, 0xFF06B6D4)
        ),
        Album(
            id = "album_2",
            title = "Golden Hour",
            artist = "Solaris Wave",
            year = 2024,
            songCount = 3,
            drawableRes = R.drawable.cover_sunset_vibes,
            gradientColors = listOf(0xFFF97316, 0xFFEC4899)
        ),
        Album(
            id = "album_3",
            title = "Midnight Coffee",
            artist = "Lofi Bloom",
            year = 2025,
            songCount = 2,
            drawableRes = R.drawable.cover_midnight_echoes,
            gradientColors = listOf(0xFF10B981, 0xFF3B82F6)
        ),
        Album(
            id = "album_4",
            title = "Cyber Pulse",
            artist = "Aura Synthetica",
            year = 2026,
            songCount = 1,
            drawableRes = R.drawable.cover_neon_dreams,
            gradientColors = listOf(0xFF3B82F6, 0xFF8B5CF6)
        ),
        Album(
            id = "album_5",
            title = "Acoustic Silence",
            artist = "Subtle Pulse",
            year = 2024,
            songCount = 2,
            drawableRes = null,
            gradientColors = listOf(0xFF14B8A6, 0xFF0EA5E9)
        )
    )
}
