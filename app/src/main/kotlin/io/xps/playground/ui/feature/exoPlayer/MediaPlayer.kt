package io.xps.playground.ui.feature.exoPlayer

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import io.xps.playground.App
import io.xps.playground.tools.TAG
import java.io.File
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy

private const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"

class MediaPlayer(private val context: Context) : DefaultLifecycleObserver {

    private var dataSourceFactory: DataSource.Factory? = null
    private var httpDataSourceFactory: HttpDataSource.Factory? = null
    private var downloadCache: Cache? = null
    private var downloadDirectory: File? = null
    private var databaseProvider: DatabaseProvider? = null

    val player: ExoPlayer by lazy { init() }

    private fun init(): ExoPlayer {
        Log.d(TAG, "init")
        val renderersFactory: RenderersFactory = buildRenderersFactory(context)
        val mediaSourceFactory: MediaSource.Factory =
            DefaultMediaSourceFactory(getDataSourceFactory())
        return ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }

    fun prepare(mediaUrl: String) {
        Log.d(TAG, "prepare $mediaUrl")
        player.setMediaItem(MediaItem.fromUri(mediaUrl), 3)
        player.playWhenReady = true
        player.prepare()
    }

    fun play() {
        Log.d(TAG, "play")
        if (!player.isPlaying) {
            Log.d(TAG, "play actually")
            player.play()
        }
    }

    fun pause() {
        Log.d(TAG, "pause")
        if (player.isPlaying) {
            Log.d(TAG, "pause actually")
            player.pause()
        }
    }

    fun stop() {
        Log.d(TAG, "stop")
        player.stop()
        player.seekToDefaultPosition()
    }

    fun release() {
        Log.d(TAG, "release")
        stop()
        player.release()
    }

    private fun buildRenderersFactory(
        context: Context,
        preferExtensionRenderer: Boolean = false
    ): RenderersFactory {
        val extensionRendererMode = if (preferExtensionRenderer) {
            DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
        } else {
            DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF
        }
        return DefaultRenderersFactory(context.applicationContext)
            .setExtensionRendererMode(extensionRendererMode)
    }

    private fun getDataSourceFactory(): DataSource.Factory {
        var factory = dataSourceFactory
        if (factory == null) {
            val upstreamFactory = DefaultDataSource.Factory(
                context.applicationContext,
                getHttpDataSourceFactory()
            )
            factory = buildReadOnlyCacheDataSource(
                upstreamFactory,
                getDownloadCache(context.applicationContext)
            )
            dataSourceFactory = factory
        }
        return factory
    }

    private fun buildReadOnlyCacheDataSource(
        upstreamFactory: DataSource.Factory,
        cache: Cache
    ): CacheDataSource.Factory {
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    private fun getHttpDataSourceFactory(): HttpDataSource.Factory {
        var factory = httpDataSourceFactory
        if (factory == null) {
            val cookieManager = CookieManager()
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
            CookieHandler.setDefault(cookieManager)
            factory = DefaultHttpDataSource.Factory()
            httpDataSourceFactory = factory
        }
        return factory
    }

    private fun getDownloadCache(context: Context): Cache {
        var cache = downloadCache
        if (cache == null) {
            cache = App.simpleCacheInstance
            if (cache == null) {
                val downloadContentDirectory = File(
                    getDownloadDirectory(context),
                    DOWNLOAD_CONTENT_DIRECTORY
                )
                val maxCacheSize = 500 * 1024 * 1024
                cache = SimpleCache(
                    downloadContentDirectory,
                    LeastRecentlyUsedCacheEvictor(maxCacheSize.toLong()),
                    getDatabaseProvider(context)
                )
                App.simpleCacheInstance = cache
                downloadCache = cache
            }
        }
        return cache
    }

    private fun getDownloadDirectory(context: Context): File? {
        var directory = downloadDirectory
        if (directory == null) {
            directory = context.getExternalFilesDir(null)
            if (directory == null) directory = context.filesDir
            downloadDirectory = directory
        }
        return directory
    }

    private fun getDatabaseProvider(context: Context): DatabaseProvider {
        var provider = databaseProvider
        if (provider == null) {
            provider = StandaloneDatabaseProvider(context)
            databaseProvider = provider
        }

        return provider
    }
}
