package com.example.taller3.ui.theme

import androidx.compose.ui.graphics.Color

// Colores de Material Design (referencia, puedes eliminarlos si solo quieres azul y blanco)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Nueva paleta de Blancos y Azules

// Tonos de Blanco y Gris muy claro
val BlancoPuro = Color(0xFFFFFFFF)       // Blanco absoluto, ideal para texto en fondos oscuros
val BlancoGrisaceo = Color(0xFFF8F8F8)   // Blanco muy suave, casi gris muy claro para fondos generales
val GrisClaro = Color(0xFFE0E0E0)        // Un gris claro para separadores o elementos sutiles

// Tonos de Azul
val AzulPrimario = Color(0xFF2196F3)     // Un azul brillante y estándar para elementos principales (botones, cabeceras)
val AzulClaro = Color(0xFFBBDEFB)        // Un azul muy claro para fondos secundarios o resaltados sutiles
val AzulOscuro = Color(0xFF1976D2)       // Un azul más oscuro para estados de hover, o texto en fondos claros
val AzulProfundo = Color(0xFF0D47A1)     // Un azul muy oscuro, casi marino, para elementos de contraste fuerte o texto

// Tonos de Negro y Gris para texto y fondos oscuros
val Negro = Color(0xFF000000)            // Negro absoluto para texto en fondos claros
val GrisOscuro = Color(0xFF424242)       // Un gris oscuro para texto secundario o fondos oscuros

// Ejemplos de uso con nombres más descriptivos para tu tema
val FondoPrincipalClaro = BlancoGrisaceo
val FondoPrincipalOscuro = GrisOscuro // O incluso AzulProfundo si quieres un modo oscuro muy azul
val TextoPrincipalClaro = Negro
val TextoPrincipalOscuro = BlancoPuro
val BotonPrimario = AzulPrimario
val BotonSecundario = AzulClaro
val ContornoElemento = GrisClaro