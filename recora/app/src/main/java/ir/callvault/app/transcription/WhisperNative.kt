/*
 * CallVault: FOSS call recording, self-contained over embedded ADB
 *  Copyright (C) 2026-present The CallVault Authors
 *  This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 *  The full license text is available in the LICENSE file at the root of this project.
 *  This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package ir.callvault.app.transcription

object WhisperNative {
    private val loaded: Boolean = runCatching {
        System.loadLibrary("whispercv")
        true
    }.getOrDefault(false)

    fun systemInfo(libDir: String): String =
        if (loaded) runCatching { nativeSystemInfo(libDir) }.getOrDefault("") else ""

    fun initContext(modelPath: String, libDir: String): Long =
        if (loaded) runCatching { nativeInitContext(modelPath, libDir) }.getOrDefault(0L) else 0L

    fun freeContext(ptr: Long) {
        if (loaded && ptr != 0L) runCatching { nativeFreeContext(ptr) }
    }

    fun transcribe(
        ptr: Long,
        audio: FloatArray,
        threads: Int,
        language: String?,
        prompt: String?,
        vadModelPath: String?,
        beamSize: Int,
        maxTextCtx: Int
    ) {
        if (loaded && ptr != 0L) {
            runCatching {
                nativeTranscribe(ptr, audio, threads, language, prompt, vadModelPath, beamSize, maxTextCtx)
            }
        }
    }

    fun vadSegmentCount(ptr: Long): Int =
        if (loaded && ptr != 0L) runCatching { nativeVadSegmentCount(ptr) }.getOrDefault(0) else 0

    fun vadSegmentStartMs(ptr: Long, index: Int): Long =
        if (loaded && ptr != 0L) runCatching { nativeVadSegmentStartMs(ptr, index) }.getOrDefault(0L) else 0L

    fun vadSegmentEndMs(ptr: Long, index: Int): Long =
        if (loaded && ptr != 0L) runCatching { nativeVadSegmentEndMs(ptr, index) }.getOrDefault(0L) else 0L

    fun requestAbort() {
        if (loaded) runCatching { nativeRequestAbort() }
    }

    fun progressPercent(): Int =
        if (loaded) runCatching { nativeProgressPercent() }.getOrDefault(0) else 0

    fun segmentCount(ptr: Long): Int =
        if (loaded && ptr != 0L) runCatching { nativeSegmentCount(ptr) }.getOrDefault(0) else 0

    fun segmentStartMs(ptr: Long, index: Int): Long =
        if (loaded && ptr != 0L) runCatching { nativeSegmentStartMs(ptr, index) }.getOrDefault(0L) else 0L

    fun segmentEndMs(ptr: Long, index: Int): Long =
        if (loaded && ptr != 0L) runCatching { nativeSegmentEndMs(ptr, index) }.getOrDefault(0L) else 0L

    fun segmentText(ptr: Long, index: Int): String =
        if (loaded && ptr != 0L) runCatching { nativeSegmentText(ptr, index) }.getOrDefault("") else ""

    fun segmentWords(ptr: Long, index: Int): String =
        if (loaded && ptr != 0L) runCatching { nativeSegmentWords(ptr, index) }.getOrDefault("") else ""

    @JvmStatic
    private external fun nativeSystemInfo(libDir: String): String
    @JvmStatic
    private external fun nativeInitContext(modelPath: String, libDir: String): Long
    @JvmStatic
    private external fun nativeFreeContext(ptr: Long)
    @JvmStatic
    private external fun nativeTranscribe(
        ptr: Long,
        audio: FloatArray,
        threads: Int,
        language: String?,
        prompt: String?,
        vadModelPath: String?,
        beamSize: Int,
        maxTextCtx: Int
    )
    @JvmStatic
    private external fun nativeVadSegmentCount(ptr: Long): Int
    @JvmStatic
    private external fun nativeVadSegmentStartMs(ptr: Long, index: Int): Long
    @JvmStatic
    private external fun nativeVadSegmentEndMs(ptr: Long, index: Int): Long
    @JvmStatic
    private external fun nativeRequestAbort()
    @JvmStatic
    private external fun nativeProgressPercent(): Int
    @JvmStatic
    private external fun nativeSegmentCount(ptr: Long): Int
    @JvmStatic
    private external fun nativeSegmentStartMs(ptr: Long, index: Int): Long
    @JvmStatic
    private external fun nativeSegmentEndMs(ptr: Long, index: Int): Long
    @JvmStatic
    private external fun nativeSegmentText(ptr: Long, index: Int): String
    @JvmStatic
    private external fun nativeSegmentWords(ptr: Long, index: Int): String
}
