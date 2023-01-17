package io.xps.playground.domain

import kotlinx.coroutines.flow.Flow

interface DataStoreManager {

    suspend fun storeUri(uri: String?)

    fun getUri(): Flow<String?>

}