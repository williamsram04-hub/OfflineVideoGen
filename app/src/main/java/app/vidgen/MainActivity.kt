package app.vidgen

import android.app.Activity
import android.content.ContentValues
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.WindowManager
import android.widget.*

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var bar: ProgressBar
    private lateinit var go: Button

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
        }
        fun label(t: String) = TextView(this).apply { text = t; setPadding(0, pad / 2, 0, 0) }

        val prompt = EditText(this).apply {
            hint = "Describe your video / caption text"
            setText("Dream big and create something beautiful")
        }
        val style = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity, android.R.layout.simple_spinner_dropdown_item,
                Scenes.STYLES + if (AiBackend.SUPPORTED) listOf("AI model") else emptyList()
            )
        }
        val secs = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText("8")
        }
        go = Button(this).apply { text = "Generate 1080p video" }
        bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        status = TextView(this).apply { gravity = Gravity.START }

        root.addView(label("Prompt")); root.addView(prompt)
        root.addView(label("Style")); root.addView(style)
        root.addView(label("Length (seconds, 1-60)")); root.addView(secs)
        root.addView(go); root.addView(bar); root.addView(status)
        setContentView(ScrollView(this).apply { addView(root) })

        if (AiBackend.SUPPORTED) status.text = AiBackend.status(this)

        go.setOnClickListener {
            val dur = (secs.text.toString().toIntOrNull() ?: 8).coerceIn(1, 60)
            generate(prompt.text.toString().ifBlank { "Hello" }, style.selectedItemPosition, dur)
        }
    }

    private fun generate(prompt: String, style: Int, dur: Int) {
        go.isEnabled = false
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val fps = 30
        val total = dur * fps
        bar.max = total
        bar.progress = 0
        status.text = "Rendering..."
        val name = "vidgen_${System.currentTimeMillis()}.mp4"

        Thread {
            var uri: android.net.Uri? = null
            try {
                val cv = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, name)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/VidGen")
                }
                uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, cv)!!
                val pfd = contentResolver.openFileDescriptor(uri, "w")!!
                val t0 = System.currentTimeMillis()
                pfd.use {
                    val w = VideoWriter(it.fileDescriptor, fps = fps)
                    val progress = { n: Int -> runOnUiThread { bar.progress = n } }
                    if (style >= Scenes.STYLES.size) {
                        w.renderBitmaps(AiBackend.frames(this, prompt, total), total, progress)
                    } else {
                        w.render(total, progress) { c, i ->
                            Scenes.draw(c, w.width, w.height, i / fps.toFloat(), dur.toFloat(), style, prompt)
                        }
                    }
                }
                val sec = (System.currentTimeMillis() - t0) / 1000
                runOnUiThread { status.text = "Saved to Movies/VidGen/$name (took ${sec}s)" }
            } catch (t: Throwable) {
                uri?.let { contentResolver.delete(it, null, null) }
                runOnUiThread { status.text = "Failed: ${t.message ?: t.javaClass.simpleName}" }
            } finally {
                runOnUiThread {
                    go.isEnabled = true
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }.start()
    }
}
