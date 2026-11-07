package com.mikepenz.aboutlibraries.ui.compose.android

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun produceLibraries(@RawRes resId: Int): State<Libs?> {
    val context = LocalContext.current
    return produceState<Libs?>(initialValue = null, resId) {
        value = withContext(Dispatchers.IO) {
            Libs.Builder().withJson(context, resId).build()
        }
    }
}
