# VidGen – offline 1080p video generator (Android)

Two build flavors from one codebase:

| Flavor | What it is | Size (est.) |
|---|---|---|
| `lite` | Fully offline procedural generator (Aurora / Particles / Waves + animated captions from your prompt). No dependencies. | ~1 MB |
| `ai`   | Same, plus an "AI model" style that runs a user-supplied ONNX model via ONNX Runtime and upscales to 1080p. | ~15-25 MB + model |

Output: H.264 MP4, 1920x1080, 30 fps, ~14 Mbps, hardware-encoded, rendered faster than real time,
saved to `Movies/VidGen`. Android 10+ (minSdk 29).

## Get the APK
1. Create a GitHub repo and push this folder.
2. Actions tab -> "Build APK" -> download the `vidgen-apks` artifact
   (`app-lite-release.apk`, `app-ai-release.apk`).
   Or locally with Android Studio / SDK: `gradle assembleLiteRelease`.

## AI flavor
Copy a `model.onnx` into `Android/data/app.vidgen.ai/files/`. The model loader is real, but
tokenising the prompt, the denoise loop and decoding output tensors are model-specific and are
a TODO in `app/src/ai/java/app/vidgen/AiBackend.kt` (`frames()`).

## Status
Not compiled or run in the authoring environment (no Android SDK access). Expect to fix small
build errors on first compile.
