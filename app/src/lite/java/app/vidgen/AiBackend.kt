package app.vidgen

import android.content.Context
import android.graphics.Bitmap

/** Lite flavor: no AI model support, keeps the APK tiny. */
object AiBackend {
    const val SUPPORTED = false
    fun available(ctx: Context) = false
    fun status(ctx: Context) = "AI mode not included in this build (lite)"
    fun frames(ctx: Context, prompt: String, count: Int): Sequence<Bitmap> =
        throw UnsupportedOperationException("AI not included in lite build")
}
