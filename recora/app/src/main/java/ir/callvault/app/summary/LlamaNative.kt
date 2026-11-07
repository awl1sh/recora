/*
 * CallVault: FOSS call recording, self-contained over embedded ADB
 *  Copyright (C) 2026-present The CallVault Authors
 *  This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 *  The full license text is available in the LICENSE file at the root of this project.
 *  This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package ir.callvault.app.summary

object LlamaNative {
    private val loaded: Boolean = runCatching {
        System.loadLibrary("llamacv")
        true
    }.getOrDefault(false)

    fun systemInfo(libDir: String): String =
        if (loaded) runCatching { nativeSystemInfo(libDir) }.getOrDefault("") else ""

    fun initContext(modelPath: String, libDir: String): Long =
        if (loaded) runCatching { nativeInitContext(modelPath, libDir) }.getOrDefault(0L) else 0L

    fun freeContext(ptr: Long) {
        if (loaded && ptr != 0L) runCatching { nativeFreeContext(ptr) }
    }

    fun countTokens(ptr: Long, text: String): Int =
        if (loaded && ptr != 0L) runCatching { nativeCountTokens(ptr, text) }.getOrDefault(-1) else -1

    fun generate(
        ptr: Long,
        prompt: String,
        maxTokens: Int,
        threads: Int,
        grammar: String?
    ): String =
        if (loaded && ptr != 0L) {
            runCatching { nativeGenerate(ptr, prompt, maxTokens, threads, grammar) }.getOrDefault("")
        } else ""

    fun requestAbort() {
        if (loaded) runCatching { nativeRequestAbort() }
    }

    fun grammarAccepted(ptr: Long, grammar: String): Boolean =
        if (loaded && ptr != 0L) runCatching { nativeGrammarAccepted(ptr, grammar) }.getOrDefault(false) else false

    @JvmStatic
    private external fun nativeSystemInfo(libDir: String): String
    @JvmStatic
    private external fun nativeInitContext(modelPath: String, libDir: String): Long
    @JvmStatic
    private external fun nativeFreeContext(ptr: Long)
    @JvmStatic
    private external fun nativeCountTokens(ptr: Long, text: String): Int
    @JvmStatic
    private external fun nativeGenerate(
        ptr: Long,
        prompt: String,
        maxTokens: Int,
        threads: Int,
        grammar: String?
    ): String
    @JvmStatic
    private external fun nativeRequestAbort()
    @JvmStatic
    private external fun nativeGrammarAccepted(ptr: Long, grammar: String): Boolean
}
