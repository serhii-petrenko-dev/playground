package io.xps.playground.domain

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface ContentRepo {

    suspend fun storeUri(uri: Uri?)

    fun readUri(): Flow<Uri?>
}
