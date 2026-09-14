package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.Success

@Composable
fun OrigamiBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    showProBadge: Boolean = true
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Rounded container card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(12.dp, RoundedCornerShape(size * 0.26f))
                .clip(RoundedCornerShape(size * 0.26f))
                .background(Color.White)
                .padding(size * 0.12f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height

                // Main Body Base (Faceted fold)
                val basePath = Path().apply {
                    moveTo(w * 0.20f, h * 0.08f)
                    lineTo(w * 0.65f, h * 0.08f)
                    lineTo(w * 0.88f, h * 0.28f)
                    lineTo(w * 0.88f, h * 0.85f)
                    lineTo(w * 0.20f, h * 0.85f)
                    close()
                }
                drawPath(basePath, PrimaryDark)

                // Top-Left Front Origami Fold
                val frontFold = Path().apply {
                    moveTo(w * 0.20f, h * 0.32f)
                    lineTo(w * 0.70f, h * 0.08f)
                    lineTo(w * 0.50f, h * 0.85f)
                    lineTo(w * 0.20f, h * 0.85f)
                    close()
                }
                drawPath(frontFold, Primary)

                // Deep Royal Blue Fold
                val blueFold1 = Path().apply {
                    moveTo(w * 0.50f, h * 0.85f)
                    lineTo(w * 0.88f, h * 0.45f)
                    lineTo(w * 0.88f, h * 0.85f)
                    close()
                }
                drawPath(blueFold1, PrimaryDark)

                // Dynamic Front Blue Facet
                val blueFold2 = Path().apply {
                    moveTo(w * 0.20f, h * 0.85f)
                    lineTo(w * 0.82f, h * 0.35f)
                    lineTo(w * 0.88f, h * 0.68f)
                    lineTo(w * 0.50f, h * 0.85f)
                    close()
                }
                drawPath(blueFold2, Color(0xFF2563EB))

                // Top Right Fold Notch
                val notchPath = Path().apply {
                    moveTo(w * 0.65f, h * 0.08f)
                    lineTo(w * 0.88f, h * 0.28f)
                    lineTo(w * 0.65f, h * 0.28f)
                    close()
                }
                drawPath(notchPath, PrimaryLight)

                // White Checkmark Badge inside fold
                val checkPath = Path().apply {
                    moveTo(w * 0.68f, h * 0.20f)
                    lineTo(w * 0.74f, h * 0.27f)
                    lineTo(w * 0.88f, h * 0.12f)
                    lineTo(w * 0.84f, h * 0.08f)
                    lineTo(w * 0.74f, h * 0.22f)
                    lineTo(w * 0.70f, h * 0.18f)
                    close()
                }
                drawPath(checkPath, Color.White)
            }
        }

        // Floating PRO badge at bottom-right
        if (showProBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .background(Primary, RoundedCornerShape(12.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "PRO",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
