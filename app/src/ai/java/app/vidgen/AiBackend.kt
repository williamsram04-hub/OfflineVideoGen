package app.vidgen

import ai.onnxruntime.OrtEnvironment
import android.content.Context
import android.graphics.Bitmap
import java.io.File

/**
 * AI flavor: loads a user-supplied ONNX text-to-video / text-to-image model from
 *   <app files dir>/model.onnx   (copy it there with: adb push, or the Files app via
 *   Android/data/app.vidgen.ai/files/model.onnx)
 *
 * The ONNX Runtime session loading is real. The prompt tokenisation, diffusion loop and
 * output-tensor -> Bitmap decoding are model-specific, so they live in [frames] and must
 * be filled in for the model you choose (see README). Frames returned here are upscaled
 * to 1920x1080 by the encoder automatically.
 */
object AiBackend {
    const val SUPPORTED = true

    private fun modelFile(ctx: Context) =
        File(ctx.getExternalFilesDir(null), "model.onnx")

    fun available(ctx: Context) = modelFile(ctx).exists()

    fun status(ctx: Context): String {
        val f = modelFile(ctx)
        if (!f.exists()) return "AI model missing: put model.onnx in ${f.parent}"
        return try {
            val env = OrtEnvironment.getEnvironment()
            env.createSession(f.absolutePath).use { "AI model OK, inputs: ${it.inputNames}" }
        } catch (t: Throwable) {
            "AI model failed to load: ${t.message}"
        }
    }

    fun frames(ctx: Context, prompt: String, count: Int): Sequence<Bitmap> {
        val f = modelFile(ctx)
        require(f.exists()) { "model.onnx not found" }
        val env = OrtEnvironment.getEnvironment()
        val session = env.createSession(f.absolutePath)
        // TODO(model-specific): tokenise `prompt`, run the diffusion/denoise loop with
        // session.run(...), decode output tensors into Bitmaps, yield `count` frames.
        session.close()
        throw NotImplementedError(
            "Wire up pre/post-processing for your specific ONNX model in AiBackend.frames()"
        )
    }
}
