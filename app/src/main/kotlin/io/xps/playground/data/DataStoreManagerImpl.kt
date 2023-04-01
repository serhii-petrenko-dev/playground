package io.xps.playground.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.xps.playground.domain.DataStoreManager
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("ktlint:experimental:property-naming")
private val Context.getDataStore by preferencesDataStore(name = "PlaygroundStore")

@Singleton
class DataStoreManagerImpl @Inject constructor(
    @ApplicationContext val context: Context
) : DataStoreManager {

    private val dataStore = context.getDataStore

    override suspend fun storeUri(uri: String?) {
        dataStore.edit { preferences ->
            if (uri != null) {
                preferences[PreferencesKeys.IMAGE_URI] = uri
            } else {
                preferences.remove(PreferencesKeys.IMAGE_URI)
            }
        }
    }

    override fun getUri() = dataStore.data.catch { exception ->
        if (exception is IOException) {
            Log.e("DataStoreManagerImpl", "Error reading preferences.", exception)
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[PreferencesKeys.IMAGE_URI]
    }

    private object PreferencesKeys {
        val IMAGE_URI = stringPreferencesKey("image_uri")
    }
}
