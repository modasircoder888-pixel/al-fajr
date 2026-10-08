package com.example.alfajr.ui.qibla

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alfajr.ui.components.WatchNavBar
import com.example.alfajr.ui.watchface.WatchTab
import com.example.alfajr.ui.watchface.WatchfaceUiState
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.DarkLcdBackground
import com.example.ui.theme.DigitalGreen
import com.example.ui.theme.DigitalGreenBright
import com.example.ui.theme.DigitalGreenDark
import com.example.ui.theme.DigitalGreenDim
import com.example.ui.theme.DigitalGreenMuted
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
    uiState: WatchfaceUiState,
    onTabSelected: (WatchTab) -> Unit,
    onRotateManual: (Float) -> Unit = {},
    onResetSensor: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val qState = uiState.qibla
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("qibla_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Navigation Bar (4 tabs: HOME, CAL, QIBLA, SET)
            WatchNavBar(
                currentTab = uiState.currentTab,
                onTabSelected = onTabSelected
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Heading Title
            Text(
                text = "AL QIBLA • القبلة",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = DigitalGreenDim,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Digital Compass Dial (Smooth animated needle)
            DigitalQiblaCompass(
                qiblaBearing = qState.qiblaBearing,
                compassHeading = qState.compassHeading,
                isFacingQibla = qState.isFacingQibla,
                modifier = Modifier.size(164.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Facing Kaaba status banner
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (qState.isFacingQibla) DigitalGreen else DigitalGreenDark)
                    .border(1.dp, if (qState.isFacingQibla) DigitalGreenBright else DigitalGreenMuted, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = qState.turnDirectionHint,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = if (qState.isFacingQibla) AmoledBlack else DigitalGreenBright,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Interactive Compass Calibration / Manual Adjust Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkLcdBackground)
                    .border(1.dp, DigitalGreenMuted, RoundedCornerShape(6.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DigitalGreenDark)
                        .clickable { onRotateManual(-15f) }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "◀ -15°",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DigitalGreenBright,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (qState.isManualMode) DigitalGreen else AmoledBlack)
                        .border(0.8.dp, DigitalGreenMuted, RoundedCornerShape(4.dp))
                        .clickable { onResetSensor() }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (qState.isManualMode) "SENSOR" else "CALIBRATE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (qState.isManualMode) AmoledBlack else DigitalGreenDim,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DigitalGreenDark)
                        .clickable { onRotateManual(15f) }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+15° ▶",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DigitalGreenBright,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Qibla Metadata Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DigitalGreenDark)
                    .border(1.dp, DigitalGreenMuted, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QIBLA BEARING",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DigitalGreenDim,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = "${qState.qiblaBearing.toInt()}° ${qState.cardinalDirection}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = DigitalGreenBright,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WATCH HEADING",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DigitalGreenDim,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = "${qState.compassHeading.toInt()}°",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = DigitalGreen,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DISTANCE",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DigitalGreenDim,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = if (qState.distanceKm == 0) "AT KAABA" else "${qState.distanceKm} KM",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = DigitalGreenBright,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CITY",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DigitalGreenDim,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = uiState.settings.coordinates.locationName.uppercase(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = DigitalGreenBright,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MODE",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DigitalGreenDim,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = when {
                            qState.isManualMode -> "MANUAL / ROTATE"
                            qState.hasCompassSensor -> "LIVE SENSOR"
                            else -> "GEODESIC"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (qState.hasCompassSensor && !qState.isManualMode) DigitalGreenBright else DigitalGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * High-performance digital Qibla compass dial.
 * Displays Cardinal directions (N, E, S, W) and turns with smooth animation.
 */
@Composable
fun DigitalQiblaCompass(
    qiblaBearing: Float,
    compassHeading: Float,
    isFacingQibla: Boolean,
    modifier: Modifier = Modifier
) {
    // Relative angle between Qibla and device heading
    var diff = qiblaBearing - compassHeading
    while (diff > 180f) diff -= 360f
    while (diff < -180f) diff += 360f

    val animatedAngle by animateFloatAsState(
        targetValue = diff,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "qibla_needle_rotation"
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = (size.width / 2f) - 6.dp.toPx()

        // Outer compass bezel
        drawCircle(
            color = DigitalGreenDark,
            radius = radius,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = DigitalGreenMuted,
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Inner dial track
        drawCircle(
            color = if (isFacingQibla) DigitalGreenMuted else Color(0xFF040E07),
            radius = radius * 0.76f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.dp.toPx())
        )

        // 30° degree tick marks with Major Cardinal ticks at 90°
        val degToRad = (Math.PI / 180.0)
        for (deg in 0 until 360 step 30) {
            val rad = (deg - 90) * degToRad
            val isMajor = deg % 90 == 0
            val tickLen = if (isMajor) 8.dp.toPx() else 4.dp.toPx()
            val startX = cx + (radius - tickLen) * cos(rad).toFloat()
            val startY = cy + (radius - tickLen) * sin(rad).toFloat()
            val endX = cx + radius * cos(rad).toFloat()
            val endY = cy + radius * sin(rad).toFloat()

            drawLine(
                color = if (isMajor) DigitalGreenBright else DigitalGreenMuted,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
            )
        }

        // Draw Cardinal labels (N, E, S, W) using native text paint
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#33FF77")
            textSize = 11.dp.toPx()
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.MONOSPACE
        }

        val textRadius = radius - 14.dp.toPx()
        // N
        drawContext.canvas.nativeCanvas.drawText("N", cx, cy - textRadius + 4.dp.toPx(), paint)
        // S
        drawContext.canvas.nativeCanvas.drawText("S", cx, cy + textRadius + 4.dp.toPx(), paint)
        // E
        drawContext.canvas.nativeCanvas.drawText("E", cx + textRadius, cy + 4.dp.toPx(), paint)
        // W
        drawContext.canvas.nativeCanvas.drawText("W", cx - textRadius, cy + 4.dp.toPx(), paint)

        // Qibla Pointer Arrow relative to top (North)
        val pointerRad = (animatedAngle - 90f) * degToRad
        val needleLen = radius * 0.72f
        val tipX = cx + needleLen * cos(pointerRad).toFloat()
        val tipY = cy + needleLen * sin(pointerRad).toFloat()

        val wingRad1 = (animatedAngle + 145f) * degToRad
        val wingRad2 = (animatedAngle - 145f) * degToRad
        val wingLen = radius * 0.28f
        val w1X = cx + wingLen * cos(wingRad1).toFloat()
        val w1Y = cy + wingLen * sin(wingRad1).toFloat()
        val w2X = cx + wingLen * cos(wingRad2).toFloat()
        val w2Y = cy + wingLen * sin(wingRad2).toFloat()

        val arrowPath = Path().apply {
            moveTo(tipX, tipY)
            lineTo(w1X, w1Y)
            lineTo(cx, cy)
            lineTo(w2X, w2Y)
            close()
        }

        val needleColor = if (isFacingQibla) DigitalGreenBright else DigitalGreen
        drawPath(
            path = arrowPath,
            color = needleColor
        )

        // Center Kaaba Cube
        val kaabaSize = 14.dp.toPx()
        drawRect(
            color = AmoledBlack,
            topLeft = Offset(cx - kaabaSize / 2, cy - kaabaSize / 2),
            size = Size(kaabaSize, kaabaSize)
        )
        drawRect(
            color = if (isFacingQibla) DigitalGreenBright else DigitalGreenMuted,
            topLeft = Offset(cx - kaabaSize / 2, cy - kaabaSize / 2),
            size = Size(kaabaSize, kaabaSize),
            style = Stroke(width = 1.5.dp.toPx())
        )
        // Golden Kiswa band on the Kaaba cube
        drawLine(
            color = if (isFacingQibla) DigitalGreenBright else DigitalGreen,
            start = Offset(cx - kaabaSize / 2, cy - kaabaSize * 0.2f),
            end = Offset(cx + kaabaSize / 2, cy - kaabaSize * 0.2f),
            strokeWidth = 2.dp.toPx()
        )
    }
}
