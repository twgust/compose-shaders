import androidx.compose.animation.Animatable
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

data class TrianglePart(val path: Path, val brush: Brush, val hitTest: (Offset) -> Boolean)

@Composable
fun TriangularButton(
    modifier: Modifier = Modifier,
    side: Dp = 200.dp,
    onRedClick: () -> Unit,
    onWhiteClick: () -> Unit,
    onOrangeClick: () -> Unit,
) {
    val density = LocalDensity.current
    val sidePx = with(density) { side.toPx() }
    val heightPx = (sidePx * sqrt(3.0) / 2).toFloat()
    var redBrush by remember {
        mutableStateOf<Brush>(
            Brush.linearGradient(
                colors = listOf(
                    Color(
                        0xFFF4F4F4
                    ), Color(0xFF343232), Color(0xFFF4F4F4)
                )
            )
        )
    }
    var whiteBrush by remember {
        mutableStateOf<Brush>(
            Brush.linearGradient(
                colors = listOf(
                    Color(
                        0xFFFFFAFA
                    ), Color(0xFF9F9A9A), Color(
                        0xFFFCFCFC
                    )
                )
            )
        )
    }
    var orangeBrush by remember {
        mutableStateOf<Brush>(
            Brush.linearGradient(
                colors = listOf(
                    Color(
                        0xFFEBD39C
                    ), Color(0xFF58585A), Color(0x692F2E2E)
                )
            )
        )
    }
    val redPart = remember {
        TrianglePart(
            path = Path().apply {
                moveTo(sidePx / 2, 0f)
                lineTo(0f, heightPx)
                lineTo(sidePx / 2, heightPx / 2)
                close()
            },
            brush = redBrush,
            hitTest = { offset ->
                offsetIsInTriangle(
                    offset,
                    Offset(sidePx / 2, 0f),
                    Offset(0f, heightPx + 16),
                    Offset(sidePx / 2, heightPx / 2)
                )
            }
        )
    }

    val orangePart = remember {
        TrianglePart(
            path = Path().apply {
                moveTo(sidePx / 2, 0f)
                lineTo(sidePx, heightPx)
                lineTo(sidePx / 2, heightPx / 2)
                close()
            },
            brush = whiteBrush,
            hitTest = { offset ->
                offsetIsInTriangle(
                    offset,
                    Offset(sidePx / 2, 0f),
                    Offset(sidePx, heightPx),
                    Offset.Infinite
                )
            }
        )
    }

    val whitePart = remember {
        TrianglePart(
            path = Path().apply {
                moveTo(0f, heightPx)
                lineTo(sidePx, heightPx )
                lineTo(sidePx / 2, heightPx / 2)
                close()
            },
            brush = orangeBrush,
            hitTest = { offset ->
                offsetIsInTriangle(
                    offset,
                    Offset(0f, heightPx - 128),
                    Offset(sidePx, heightPx - 128),
                    Offset(sidePx / 2, heightPx / 2)
                )
            }
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(side)
    ) {
        Part(
            modifier = Modifier
                .padding(end = 16.dp)
                .size(side)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (redPart.hitTest(offset)) {

                        }
                        if (whitePart.hitTest(offset)) {

                        }
                        if (orangePart.hitTest(offset)) {

                        }
                    }
                },
            part = redPart
        )
        Part(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(side)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (redPart.hitTest(offset)) {

                        }
                        if (whitePart.hitTest(offset)) {

                        }
                        if (orangePart.hitTest(offset)) {

                        }
                    }
                },
            part = orangePart
        )
        Part(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(side)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (redPart.hitTest(offset)) {
                            onRedClick()
                        }
                        if (whitePart.hitTest(offset)) {
                            onOrangeClick()
                        }
                        if (orangePart.hitTest(offset)) {
                            onWhiteClick()
                        }
                    }
                },
            part = whitePart
        )
    }
}

@Composable
fun Part(modifier: Modifier, part: TrianglePart) {
    Canvas(modifier = modifier) {
        drawPath(
            path = part.path,
            brush = part.brush
        )
    }
}

fun offsetIsInTriangle(p: Offset, a: Offset, b: Offset, c: Offset): Boolean {
    val d1 = sign(p, a, b)
    val d2 = sign(p, b, c)
    val d3 = sign(p, c, a)
    val hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0)
    val hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0)
    return !(hasNeg && hasPos)
}

fun sign(p1: Offset, p2: Offset, p3: Offset): Float {
    return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y)
}

@Preview(showBackground = true)
@Composable
fun TriangularButtonPreview() {
    MaterialTheme {
        var initScale = 1f
        val targetScale = initScale * 1.25f
        var initDegrees = 360f
        val targetDegrees = initDegrees + 360f
        val a = remember { mutableStateOf(androidx.compose.animation.core.Animatable(initDegrees)) }
        val b = remember { mutableStateOf(androidx.compose.animation.core.Animatable(initScale)) }
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 450.dp)
    ) {
        TriangularButton(
            modifier = Modifier.graphicsLayer {},
            onRedClick = {
                println("red clicked")
                /* handle red part click */
            },
            onWhiteClick = { println("white clicked") /* handle white part click */ },
            onOrangeClick = { println("orange clicked")/* handle orange part click */ }
        )
    }
}

