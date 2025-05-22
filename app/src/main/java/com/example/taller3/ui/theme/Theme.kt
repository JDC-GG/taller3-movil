package com.example.taller3.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Asegúrate de que los colores que definimos antes estén en este mismo archivo
// o importados si están en otro archivo de colores.
// Por ejemplo:
// val BlancoPuro = Color(0xFFFFFFFF)
// val BlancoGrisaceo = Color(0xFFF8F8F8)
// val GrisClaro = Color(0xFFE0E0E0)
// val AzulPrimario = Color(0xFF2196F3)
// val AzulClaro = Color(0xFFBBDEFB)
// val AzulOscuro = Color(0xFF1976D2)
// val AzulProfundo = Color(0xFF0D47A1)
// val Negro = Color(0xFF000000)
// val GrisOscuro = Color(0xFF424242)


private val DarkColorScheme = darkColorScheme(
    primary = AzulProfundo,         // Azul oscuro profundo para elementos primarios (botones, etc.)
    onPrimary = BlancoPuro,         // Texto blanco sobre azul profundo
    secondary = AzulOscuro,         // Azul ligeramente más claro para elementos secundarios
    onSecondary = BlancoPuro,       // Texto blanco sobre azul oscuro
    background = GrisOscuro,        // Fondo oscuro general (casi negro)
    onBackground = BlancoGrisaceo,  // Texto blanco grisáceo sobre fondo oscuro
    surface = AzulOscuro,           // Superficies como tarjetas, campos de texto en modo oscuro (puedes usar un gris más claro si prefieres)
    onSurface = BlancoGrisaceo,     // Texto sobre superficies en modo oscuro
    // Opcional: puedes añadir más colores aquí
    // tertiary = ...
    // error = ...
)

private val LightColorScheme = lightColorScheme(
    primary = AzulPrimario,         // Azul brillante para elementos primarios (botones)
    onPrimary = BlancoPuro,         // Texto blanco sobre botones azules
    secondary = AzulClaro,          // Azul muy claro para elementos secundarios (como iconos o detalles)
    onSecondary = AzulOscuro,       // Texto azul oscuro sobre secundarios
    background = BlancoGrisaceo,    // Fondo general muy claro
    onBackground = Negro,           // Texto negro sobre fondo claro
    surface = BlancoPuro,           // Superficies como tarjetas, campos de texto
    onSurface = Negro,              // Texto negro sobre superficies blancas
    // Opcional: puedes añadir más colores aquí
    // tertiary = ...
    // error = ...

    /* Puedes eliminar estos comentarios y los colores originales de Material Design
       si ya no los necesitas y quieres una paleta más limpia y enfocada en azul/blanco.
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun taller3Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Mantener en false a menos que quieras usar colores del sistema en Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Asegúrate de que 'Typography' también esté definido en tu archivo
        content = content
    )
}