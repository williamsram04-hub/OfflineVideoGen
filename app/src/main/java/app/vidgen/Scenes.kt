package app.vidgen

import android.graphics.*
import kotlin.math.*

/** Procedural scenes drawn on a software Canvas. Deterministic per (prompt, time). */
object Scenes {
    val STYLES = listOf("Aurora", "Particles", "Waves")

    private val bg = Paint()
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    private fun hsv(h: Float, s: Float, v: Float, a: Int = 255): Int {
        val c = Color.HSVToColor(floatArrayOf(((h % 360f) + 360f) % 360f, s, v))
        return Color.argb(a, Color.red(c), Color.green(c), Color.blue(c))
    }

    private fun rnd(i: Int, k: Int): Float {
        var x = i * 374761393 + k * 668265263
        x = (x xor (x ushr 13)) * 1274126177
        x = x xor (x ushr 16)
        return (x and 0xffff) / 65535f
    }

    fun draw(c: Canvas, w: Int, h: Int, t: Float, dur: Float, style: Int, prompt: String) {
        val seed = prompt.hashCode()
        val hue = (seed and 0x1ff) % 360
        // background gradient that slowly rotates
        val a = t * 0.3f
        val cx = w / 2f
        val cy = h / 2f
        val r = hypot(cx, cy)
        bg.shader = LinearGradient(
            cx - cos(a) * r, cy - sin(a) * r, cx + cos(a) * r, cy + sin(a) * r,
            hsv(hue + t * 6, 0.75f, 0.28f), hsv(hue + 70 + t * 6, 0.8f, 0.10f),
            Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bg)

        when (style) {
            0 -> aurora(c, w, h, t, hue)
            1 -> particles(c, w, h, t, hue)
            else -> waves(c, w, h, t, hue)
        }
        caption(c, w, h, t, dur, prompt)
    }

    private fun aurora(c: Canvas, w: Int, h: Int, t: Float, hue: Int) {
        for (i in 0 until 7) {
            val px = w * (0.5f + 0.42f * sin(t * (0.25f + rnd(i, 1) * 0.4f) + i * 1.7f))
            val py = h * (0.5f + 0.38f * cos(t * (0.20f + rnd(i, 2) * 0.4f) + i * 2.3f))
            val rad = h * (0.45f + 0.3f * rnd(i, 3))
            fill.shader = RadialGradient(
                px, py, rad,
                intArrayOf(hsv(hue + i * 28f + t * 8, 0.8f, 1f, 150), Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP
            )
            c.drawCircle(px, py, rad, fill)
        }
        fill.shader = null
    }

    private fun particles(c: Canvas, w: Int, h: Int, t: Float, hue: Int) {
        fill.shader = null
        for (i in 0 until 260) {
            val speed = 0.03f + rnd(i, 4) * 0.12f
            val y = ((rnd(i, 1) - t * speed) % 1f + 1f) % 1f
            val x = rnd(i, 2) + 0.03f * sin(t * 0.8f + i)
            val rad = 2f + rnd(i, 3) * 10f
            val tw = 0.5f + 0.5f * sin(t * 2f + i)
            fill.color = hsv(hue + rnd(i, 5) * 80f, 0.5f, 1f, (60 + 150 * tw).toInt())
            c.drawCircle(x * w, y * h, rad, fill)
        }
    }

    private val path = Path()
    private fun waves(c: Canvas, w: Int, h: Int, t: Float, hue: Int) {
        fill.shader = null
        for (k in 0 until 6) {
            path.reset()
            path.moveTo(0f, h.toFloat())
            val base = h * (0.45f + k * 0.09f)
            var x = 0
            while (x <= w) {
                val y = base + sin(x * 0.004f + t * (0.6f + k * 0.15f) + k) * h * 0.06f +
                    sin(x * 0.011f - t * 0.9f + k * 2f) * h * 0.02f
                path.lineTo(x.toFloat(), y)
                x += 12
            }
            path.lineTo(w.toFloat(), h.toFloat())
            path.close()
            fill.color = hsv(hue + k * 22f, 0.7f, 0.9f - k * 0.09f, 110)
            c.drawPath(path, fill)
        }
    }

    private fun caption(c: Canvas, w: Int, h: Int, t: Float, dur: Float, prompt: String) {
        val words = prompt.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (words.isEmpty()) return
        // group into lines of up to 3 words, spread evenly over the video
        val lines = words.chunked(3).map { it.joinToString(" ") }
        val per = dur / lines.size
        val idx = min((t / per).toInt(), lines.size - 1)
        val lt = (t - idx * per) / per // 0..1 within this line
        val fadeIn = min(1f, lt / 0.15f)
        val fadeOut = min(1f, (1f - lt) / 0.15f)
        val alpha = (255 * min(fadeIn, fadeOut)).coerceIn(0f, 255f).toInt()
        val scale = 0.92f + 0.08f * fadeIn + 0.06f * lt
        text.textSize = h * 0.11f * scale
        text.alpha = alpha
        text.setShadowLayer(h * 0.02f, 0f, h * 0.008f, Color.argb(alpha / 2, 0, 0, 0))
        c.drawText(lines[idx], w / 2f, h / 2f + text.textSize * 0.35f, text)
    }
}
