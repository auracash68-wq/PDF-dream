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
import com.example.ui.theme.SweetBlue
import com.example.ui.theme.SweetBlueDark
import com.example.ui.theme.SweetEmerald
import com.example.ui.theme.SweetOrange
import com.example.ui.theme.SweetOrangeDark

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

                // Main Orange Body Base (Faceted fold)
                val basePath = Path().apply {
                    moveTo(w * 0.20f, h * 0.08f)
                    lineTo(w * 0.65f, h * 0.08f)
                    lineTo(w * 0.88f, h * 0.28f)
                    lineTo(w * 0.88f, h * 0.85f)
                    lineTo(w * 0.20f, h * 0.85f)
                    close()
                }
                drawPath(basePath, SweetOrangeDark)

                // Top-Left Front Orange Origami Fold
                val orangeFold = Path().apply {
                    moveTo(w * 0.20f, h * 0.32f)
                    lineTo(w * 0.70f, h * 0.08f)
                    lineTo(w * 0.50f, h * 0.85f)
                    lineTo(w * 0.20f, h * 0.85f)
                    close()
                }
                drawPath(orangeFold, SweetOrange)

                // Deep Tech Blue Fold
                val blueFold1 = Path().apply {
                    moveTo(w * 0.50f, h * 0.85f)
                    lineTo(w * 0.88f, h * 0.45f)
                    lineTo(w * 0.88f, h * 0.85f)
                    close()
                }
                drawPath(blueFold1, SweetBlueDark)

                // Dynamic Front Tech Blue Facet
                val blueFold2 = Path().apply {
                    moveTo(w * 0.20f, h * 0.85f)
                    lineTo(w * 0.82f, h * 0.35f)
                    lineTo(w * 0.88f, h * 0.68f)
                    lineTo(w * 0.50f, h * 0.85f)
                    close()
                }
                drawPath(blueFold2, SweetBlue)

                // Top Right Fold Notch
                val notchPath = Path().apply {
                    moveTo(w * 0.65f, h * 0.08f)
                    lineTo(w * 0.88f, h * 0.28f)
                    lineTo(w * 0.65f, h * 0.28f)
                    close()
                }
                drawPath(notchPath, Color(0xFFFF9E00))

                // Emerald Green Checkmark Badge inside fold
                val checkPath = Path().apply {
                    moveTo(w * 0.68f, h * 0.20f)
                    lineTo(w * 0.74f, h * 0.27f)
                    lineTo(w * 0.88f, h * 0.12f)
                    lineTo(w * 0.84f, h * 0.08f)
                    lineTo(w * 0.74f, h * 0.22f)
                    lineTo(w * 0.70f, h * 0.18f)
                    close()
                }
                drawPath(checkPath, SweetEmerald)
            }
        }

        // Floating PRO badge at bottom-right
        if (showProBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .background(SweetEmerald, RoundedCornerShape(12.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "✓ PRO",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
