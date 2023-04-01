package io.xps.playground.data

import android.net.Uri
import io.xps.playground.domain.ContentRepo
import io.xps.playground.domain.DataStoreManager
import kotlinx.coroutines.flow.map

class ContentRepoImpl(private val dataStore: DataStoreManager) : ContentRepo {

    override suspend fun storeUri(uri: Uri?) {
        dataStore.storeUri(uri?.let { uri.toString() })
    }

    override fun readUri() = dataStore.getUri().map { uri ->
        uri?.let { Uri.parse(it) }
    }
}
