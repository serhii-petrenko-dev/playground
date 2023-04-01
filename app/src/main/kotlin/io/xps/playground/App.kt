package io.xps.playground

import android.app.Application
import androidx.core.os.bundleOf
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import logcat.AndroidLogcatLogger
import logcat.LogPriority

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        analytics = FirebaseAnalytics.getInstance(this)
        AndroidLogcatLogger.installOnDebuggableApp(this, minPriority = LogPriority.VERBOSE)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        analytics.setAnalyticsCollectionEnabled(true)
        analytics.setUserId("Serhii Petrenko")
    }

    companion object {

        private lateinit var analytics: FirebaseAnalytics

        var simpleCacheInstance: SimpleCache? = null

        fun logScreenView(screenName: String) {
            analytics.logEvent(
                FirebaseAnalytics.Event.SCREEN_VIEW,
                bundleOf(FirebaseAnalytics.Param.SCREEN_NAME to screenName)
            )

            analytics.logEvent(
                "another_custom_event",
                bundleOf("${FirebaseAnalytics.Event.SCREEN_VIEW}_custom" to screenName)
            )

            analytics.logEvent(
                "custom_event",
                bundleOf("${FirebaseAnalytics.Event.SCREEN_VIEW}_error" to "email_error")
            )
        }
    }
}
