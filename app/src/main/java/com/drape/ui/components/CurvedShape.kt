package com.drape.ui.components

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.LayoutDirection

/**
 * Genera una forma concava personalizzata per la barra di navigazione inferiore.
 *
 * @param xOffset La posizione orizzontale del centro dell'incavo (notch).
 * @param cutoutWidth La larghezza totale dell'incavo.
 * @param cutoutHeight La profondità dell'incavo.
 */
fun curvedShape(
    xOffset: Float,
    cutoutWidth: Float,
    cutoutHeight: Float
): GenericShape = GenericShape { size: Size, _: LayoutDirection ->
    val width = size.width
    val height = size.height
    
    // La "spalla" della curva rende il passaggio più fluido
    val shoulderWidth = cutoutWidth * 0.4f
    val halfCutout = cutoutWidth / 2f

    reset()
    moveTo(0f, 0f)
    
    // Tratto iniziale fino alla spalla sinistra
    lineTo(xOffset - halfCutout - shoulderWidth, 0f)
    
    // Curva di Bézier in entrata nell'incavo
    cubicTo(
        x1 = xOffset - halfCutout, 
        y1 = 0f,
        x2 = xOffset - halfCutout, 
        y2 = cutoutHeight,
        x3 = xOffset, 
        y3 = cutoutHeight
    )
    
    // Curva di Bézier in uscita dall'incavo
    cubicTo(
        x1 = xOffset + halfCutout, 
        y1 = cutoutHeight,
        x2 = xOffset + halfCutout, 
        y2 = 0f,
        x3 = xOffset + halfCutout + shoulderWidth, 
        y3 = 0f
    )

    lineTo(width, 0f)
    lineTo(width, height)
    lineTo(0f, height)
    close()
}
