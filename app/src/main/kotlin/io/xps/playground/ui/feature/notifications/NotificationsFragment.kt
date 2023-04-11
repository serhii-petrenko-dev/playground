package io.xps.playground.ui.feature.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Context.SHORTCUT_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_CALL
import androidx.core.app.NotificationCompat.CATEGORY_MESSAGE
import androidx.core.app.NotificationCompat.CATEGORY_PROGRESS
import androidx.core.app.NotificationCompat.CATEGORY_PROMO
import androidx.core.app.NotificationCompat.CATEGORY_RECOMMENDATION
import androidx.core.app.NotificationCompat.EXTRA_NOTIFICATION_ID
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.BuildConfig
import io.xps.playground.MainActivity
import io.xps.playground.R
import io.xps.playground.databinding.FragmentComposeBinding
import io.xps.playground.extensions.toast
import io.xps.playground.tools.viewBinding
import io.xps.playground.ui.composables.BaseColumn
import io.xps.playground.ui.composables.ListItem
import io.xps.playground.ui.composables.ScreenTittle
import io.xps.playground.ui.theme.PlaygroundTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

const val KEY_TEXT_REPLY = "${BuildConfig.APPLICATION_ID}.reply_action"
const val REPLY_ACTION = "${BuildConfig.APPLICATION_ID}.reply"
const val DISMISS_ACTION = "${BuildConfig.APPLICATION_ID}.dismiss"
const val DEFAULT_GROUP_ID = "${BuildConfig.APPLICATION_ID}.group"
const val DEFAULT_CHANNEL_ID = "${BuildConfig.APPLICATION_ID}.default"
const val IMPORTANT_CHANNEL_ID = "${BuildConfig.APPLICATION_ID}.important"

@AndroidEntryPoint
class NotificationsFragment : Fragment(R.layout.fragment_compose) {

    private val binding by viewBinding(FragmentComposeBinding::bind)
    private val viewModel by viewModels<NotificationsViewModel>()

    private val notificationManager by lazy {
        NotificationManagerCompat.from(requireContext())
    }

    private val defaultChannelGroup = "default_group"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askPermissionIfNeeded()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createNotificationChannels()
        registerLifecycleEvents()
        val sampleItems = samples()

        binding.containerCompose.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.containerCompose.setContent {
            PlaygroundTheme {
                NotificationsScreen(sampleItems)
            }
        }
    }

    private fun registerLifecycleEvents() {
        val receiver = NotificationActionBroadcastReceiver()
        val observer = object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                requireContext().unregisterReceiver(receiver)
            }

            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                val filter = IntentFilter().apply {
                    addAction(REPLY_ACTION)
                    addAction(DISMISS_ACTION)
                }
                val receiverFlags = ContextCompat.RECEIVER_NOT_EXPORTED
                ContextCompat.registerReceiver(requireContext(), receiver, filter, receiverFlags)
            }
        }
        lifecycle.addObserver(observer)
    }

    @Composable
    private fun NotificationsScreen(items: List<ListItem>) {
        BaseColumn {
            Surface(modifier = Modifier.fillMaxSize()) {
                LazyColumn {
                    item {
                        ScreenTittle(text = stringResource(id = R.string.notifications))
                    }
                    items(items) {
                        ListItem(
                            tittle = stringResource(id = it.content),
                            drawable = R.drawable.ic_notifications,
                            onClick = {
                                handleClick(it)
                            }
                        )
                    }
                    item {
                        Box(modifier = Modifier.navigationBarsPadding())
                    }
                }
            }
        }
    }

    private fun handleClick(item: ListItem) {
        when (item.content) {
            R.string.notification_sample_basic -> basicNotification()
            R.string.notification_sample_big_text -> bigTextNotification()
            R.string.notification_sample_big_picture -> bigPictureNotification()
            R.string.notification_sample_actions -> notificationWithActions()
            R.string.notification_sample_reply -> directReplyNotification()
            R.string.notification_sample_progress -> notificationWithProgress()
            R.string.notification_sample_urgent -> urgentNotification()
            R.string.notification_sample_inbox -> inboxNotification()
            R.string.notification_sample_messaging -> messagingNotification()
            R.string.notification_sample_custom -> customNotification()
            R.string.notification_sample_group -> notificationWithGroup()
            R.string.notification_sample_group_conversation -> groupConversationNotification()
            R.string.notification_sample_conversation -> conversationNotification()
            R.string.notification_sample_bubble -> bubbleNotification()
            R.string.notification_sample_media -> mediaControlsNotification()
            R.string.notification_sample_query -> readNotificationChannelSettings(
                DEFAULT_CHANNEL_ID
            )

            R.string.notification_sample_settings -> openNotificationChannelSettings(
                DEFAULT_CHANNEL_ID
            )

            R.string.notification_sample_delete -> {
                notificationManager.deleteNotificationChannel((DEFAULT_CHANNEL_ID))
            }
        }
    }

    private fun notificationBase(channelId: String = DEFAULT_CHANNEL_ID) =
        NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("My notification")
            .setContentText("Much longer text that cannot fit one line...")
            .withAppIntent()
            .setPriority(
                if (channelId == IMPORTANT_CHANNEL_ID) {
                    NotificationCompat.PRIORITY_MAX
                } else {
                    NotificationCompat.PRIORITY_DEFAULT
                }
            )

    private fun basicNotification() {
        val builder = notificationBase().setColor(Color.GREEN)
        notify(builder.build())
    }

    private fun bigTextNotification() {
        val builder = notificationBase().setStyle(
            NotificationCompat.BigTextStyle().bigText(
                "Much longer text that cannot fit one line..." +
                        "Much longer text that cannot fit one line... " +
                        "Much longer text that cannot fit one line... " +
                        "Much longer text that cannot fit one line... " +
                        "Much longer text that cannot fit one line... " +
                        "Much longer text that cannot fit one line... " +
                        "Much longer text that cannot fit one line... " +
                        "Much longer text that cannot fit one line..."
            )
        ).setCategory(CATEGORY_PROMO)
        notify(builder.build())
    }

    private fun bigPictureNotification() {
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.android)
            val builder = notificationBase().setStyle(
                NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(null)
            ).setLargeIcon(bitmap).setCategory(CATEGORY_PROMO)
            withContext(Dispatchers.Main) {
                notify(builder.build())
            }
        }
    }

    private fun notificationWithActions() {
        val context = requireContext()
        val notificationId = Random.nextInt()

        val actionIntent = Intent(DISMISS_ACTION).apply {
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val pendingActionIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            actionIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val builder = notificationBase().addAction(
            R.drawable.ic_notifications,
            getString(R.string.notification_dismiss_label),
            pendingActionIntent
        ).setCategory(CATEGORY_RECOMMENDATION)

        notify(builder.build(), notificationId)
    }

    private fun directReplyNotification() {
        val context = requireContext()
        val notificationId = Random.nextInt()

        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
            setLabel(resources.getString(R.string.notification_reply_label))
            build()
        }

        val actionIntent = Intent(REPLY_ACTION).apply {
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            actionIntent,
            PendingIntent.FLAG_MUTABLE
        )
        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.ic_notifications,
            getString(R.string.notification_reply_label),
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        val notification = notificationBase()
            .addAction(replyAction)
            .setCategory(CATEGORY_MESSAGE)
            .build()
        notify(notification, notificationId)
    }

    private fun notificationWithProgress() {
        val context = requireContext()
        val notificationId = Random.nextInt()

        val actionIntent = Intent(DISMISS_ACTION).apply {
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val pendingActionIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            actionIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val builder = notificationBase()
            .setOnlyAlertOnce(true)
            .setCategory(CATEGORY_PROGRESS)
            .setVisibility(VISIBILITY_SECRET)
            .addAction(
                R.drawable.ic_notifications,
                getString(R.string.notification_cancel_label),
                pendingActionIntent
            )

        val atLeastM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        if (atLeastM) {
            builder.setProgress(100, 0, false)
        } else {
            builder.setProgress(0, 0, true)
        }

        notify(builder.build(), notificationId)

        if (atLeastM) {
            startPostingProgress(context, notificationId, builder)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startPostingProgress(
        context: Context,
        notificationId: Int,
        builder: NotificationCompat.Builder
    ) {
        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        lifecycleScope.launch {
            var progress = 0
            var canceled = false
            while (!canceled) {
                delay(100)
                if (manager.activeNotifications.any { it.id == notificationId }) {
                    progress++
                    builder.setProgress(100, progress, false)
                    notify(builder.build(), notificationId)
                } else {
                    canceled = true
                }
                if (progress >= 100) {
                    notificationManager.cancel(notificationId)
                }
            }
        }
    }

    private fun urgentNotification() {
        val context = requireContext()
        val notificationId = Random.nextInt()

        val fullScreenIntent = Intent(context, MainActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val actionIntent = Intent(DISMISS_ACTION).apply {
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val pendingActionIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            actionIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = notificationBase(IMPORTANT_CHANNEL_ID)
            .setOngoing(true)
            .setFullScreenIntent(
                fullScreenPendingIntent,
                true
            ).addAction(
                R.drawable.ic_notifications,
                getString(R.string.notification_dismiss_label),
                pendingActionIntent
            ).setCategory(CATEGORY_CALL)
        notify(builder.build(), notificationId)
    }

    private fun inboxNotification() {
        val notificationId = Random.nextInt()
        val builder = notificationBase().setStyle(
            NotificationCompat.InboxStyle()
                .addLine("Message 1")
                .addLine("Message 2")
        )
        notify(builder.build(), notificationId)
    }

    private fun messagingNotification() {
        val context = requireContext()
        val notificationId = Random.nextInt()
        val person = Person.Builder()
            .setName("John Doe")
            .setKey("key")
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_launcher_background))
            .setImportant(true)
            .build()

        val message1 = NotificationCompat.MessagingStyle.Message(
            "Message 1",
            System.currentTimeMillis(),
            person
        )
        val message2 = NotificationCompat.MessagingStyle.Message(
            "Message 2",
            System.currentTimeMillis(),
            person
        )
        val builder = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .withAppIntent()
            .setStyle(
                NotificationCompat.MessagingStyle(person)
                    .setConversationTitle("My conversation name")
                    .setGroupConversation(true)
                    .addMessage(message1)
                    .addMessage(message2)
            )
        notify(builder.build(), notificationId)
    }

    private fun mediaControlsNotification() {
        // Requires MediaSession
        // https://developer.android.com/develop/ui/views/notifications/expanded?media-style
        requireContext().toast(R.string.todo)
    }

    private fun customNotification() {
        val notificationLayout = RemoteViews(
            BuildConfig.APPLICATION_ID,
            R.layout.notification_layout
        )
        val builder = notificationBase()
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayout)
            .setCategory(CATEGORY_PROMO)
        notify(builder.build())
    }

    private fun notificationWithGroup() {
        val builder = notificationBase().setGroup(DEFAULT_GROUP_ID)
        notify(builder.build())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val context = requireContext()
            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            var count = manager.activeNotifications.size
            if (count > 1) {
                count = if (count > 2) count - 1 else count
                val summaryNotification = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
                    .setContentTitle("Group Summary")
                    .setContentText("$count new messages") // For API < 24 devices support
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setStyle(
                        NotificationCompat.InboxStyle()
                            .setSummaryText("$count new messages")
                    )
                    .setGroup(DEFAULT_GROUP_ID)
                    .setGroupSummary(true) // <---
                notify(summaryNotification.build(), 100)
            }
        }
    }

    private fun conversationNotification() {
        val context = requireContext()
        val notificationId = Random.nextInt()

        val person = Person.Builder()
            .setName("John Doe")
            .setKey("key")
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_launcher_background))
            .setImportant(true)
            .build()

        // Create a sharing shortcut.
        val shortcutId = "${BuildConfig.APPLICATION_ID}.shortcut"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val category = "${BuildConfig.APPLICATION_ID}.conversation"
            val shortcut = ShortcutInfo.Builder(context, shortcutId)
                .setCategories(setOf(category))
                .setIntent(Intent(Intent.ACTION_DEFAULT))
                .setLongLived(true)
                .setShortLabel(person.name!!)
                .build()
            val shortcutManager = context.getSystemService(SHORTCUT_SERVICE) as ShortcutManager
            shortcutManager.pushDynamicShortcut(shortcut)
        }

        val message1 = NotificationCompat.MessagingStyle.Message(
            "Message 1",
            System.currentTimeMillis(),
            person
        )
        val message2 = NotificationCompat.MessagingStyle.Message(
            "Message 2",
            System.currentTimeMillis(),
            person
        )

        // Create a notification, referencing the sharing shortcut.
        val builder = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .withAppIntent()
            .setShortcutId(shortcutId)
            .setStyle(
                NotificationCompat.MessagingStyle(person)
                    .setConversationTitle("My conversation")
                    .setGroupConversation(true)
                    .addMessage(message1)
                    .addMessage(message2)
            )
        notify(builder.build(), notificationId)
    }

    private fun groupConversationNotification() {
        val context = requireContext()
        val notificationId = Random.nextInt()

        val person = Person.Builder()
            .setName("John Doe")
            .setKey("key")
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_launcher_background))
            .setImportant(true)
            .build()

        val person2 = Person.Builder()
            .setName("Peter Doe")
            .setKey("key1")
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_launcher_background))
            .setImportant(true)
            .build()

        // Create a sharing shortcut.
        val shortcutId = "${BuildConfig.APPLICATION_ID}.shortcut"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val category = "${BuildConfig.APPLICATION_ID}.conversation"
            val shortcut = ShortcutInfo.Builder(context, shortcutId)
                .setCategories(setOf(category))
                .setIntent(Intent(Intent.ACTION_DEFAULT))
                .setLongLived(true)
                .setShortLabel(person.name!!)
                .build()
            val shortcutManager = context.getSystemService(SHORTCUT_SERVICE) as ShortcutManager
            shortcutManager.pushDynamicShortcut(shortcut)
        }

        val message1 = NotificationCompat.MessagingStyle.Message(
            "Message 1",
            System.currentTimeMillis(),
            person
        )
        val message2 = NotificationCompat.MessagingStyle.Message(
            "Message 2",
            System.currentTimeMillis(),
            person
        )

        // Create a notification, referencing the sharing shortcut.
        val builder = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .withAppIntent()
            .setShortcutId(shortcutId)
            .addPerson(person)
            .addPerson(person2)
            .setStyle(
                NotificationCompat.MessagingStyle(person)
                    .setConversationTitle("My conversation")
                    .setGroupConversation(true)
                    .addMessage(message1)
                    .addMessage(message2)
            )
        notify(builder.build(), notificationId)
    }

    private fun bubbleNotification() {
        val context = requireContext()
        val notificationId = Random.nextInt()

        val person = Person.Builder()
            .setName("John Doe")
            .setKey("key")
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_launcher_background))
            .setImportant(true)
            .build()

        // Create a sharing shortcut.
        val shortcutId = "${BuildConfig.APPLICATION_ID}.shortcut"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val category = "${BuildConfig.APPLICATION_ID}.conversation"
            val shortcut = ShortcutInfo.Builder(context, shortcutId)
                .setCategories(setOf(category))
                .setIntent(Intent(Intent.ACTION_DEFAULT))
                .setLongLived(true)
                .setShortLabel(person.name!!)
                .build()
            val shortcutManager = context.getSystemService(SHORTCUT_SERVICE) as ShortcutManager
            shortcutManager.pushDynamicShortcut(shortcut)
        }

        val message1 = NotificationCompat.MessagingStyle.Message(
            "Message 1",
            System.currentTimeMillis(),
            person
        )

        // Create a notification, referencing the sharing shortcut.
        var builder = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .withAppIntent()
            .setShortcutId(shortcutId)
            .setStyle(
                NotificationCompat.MessagingStyle(person)
                    .setConversationTitle("My conversation")
                    .addMessage(message1)
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Create a bubble intent.
            val target = Intent(context, BubbleActivity::class.java)
            val bubbleIntent = PendingIntent.getActivity(
                context,
                0,
                target,
                PendingIntent.FLAG_MUTABLE
            )

            // Create a bubble metadata.
            val bubbleData = NotificationCompat.BubbleMetadata.Builder(
                bubbleIntent,
                IconCompat.createWithResource(context, R.drawable.ic_notifications)
            ).setDesiredHeight(600).build()
            builder = builder.setBubbleMetadata(bubbleData)
        }

        notify(builder.build(), notificationId)
    }

    private fun NotificationCompat.Builder.withAppIntent(): NotificationCompat.Builder {
        val context = requireContext()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        setContentIntent(pendingIntent)
        setAutoCancel(true)
        return this
    }

    private fun notify(notification: Notification, notificationId: Int = Random.nextInt()) {
        try {
            notificationManager.notify(notificationId, notification)
        } catch (_: SecurityException) {
        }
    }

    private fun readNotificationChannelSettings(channelId: String) {
        val message = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(channelId)
            if (channel != null) {
                "Channel with id $channelId has importance: ${channel.importance}"
            } else {
                "Channel with id $channelId not found"
            }
        } else {
            "Notification channels not supported"
        }
        requireContext().toast(message)
    }

    private fun openNotificationChannelSettings(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID)
                putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
            }
            startActivity(intent)
        } else {
            requireContext().toast("Notification channels not supported")
        }
    }

    private fun createNotificationChannels() {
        val context = requireContext()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelGroup = NotificationChannelGroup(
                defaultChannelGroup,
                context.getString(R.string.app_name)
            )
            notificationManager.createNotificationChannelGroup(channelGroup)

            val defaultChannel = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                getString(R.string.notification_channel_default_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            defaultChannel.description =
                getString(R.string.notification_channel_default_description)
            defaultChannel.group = defaultChannelGroup

            val importantChannel = NotificationChannel(
                IMPORTANT_CHANNEL_ID,
                getString(R.string.notification_channel_important_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            importantChannel.description =
                getString(R.string.notification_channel_important_description)
            importantChannel.group = defaultChannelGroup

            // Register the channel with the system.
            // You can't change the importance or other notification behaviors after this.
            notificationManager.createNotificationChannel(defaultChannel)
            notificationManager.createNotificationChannel(importantChannel)
        }
    }

    private fun askPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationManager.areNotificationsEnabled()) {
                viewModel.askPermission(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun samples() = listOf(
        ListItem(R.string.notification_sample_basic),
        ListItem(R.string.notification_sample_big_text),
        ListItem(R.string.notification_sample_big_picture),
        ListItem(R.string.notification_sample_actions),
        ListItem(R.string.notification_sample_reply),
        ListItem(R.string.notification_sample_progress),
        ListItem(R.string.notification_sample_urgent),
        ListItem(R.string.notification_sample_inbox),
        ListItem(R.string.notification_sample_messaging),
        ListItem(R.string.notification_sample_custom),
        ListItem(R.string.notification_sample_group),
        ListItem(R.string.notification_sample_conversation),
        ListItem(R.string.notification_sample_group_conversation),
        ListItem(R.string.notification_sample_bubble),
        ListItem(R.string.notification_sample_media),
        ListItem(R.string.notification_sample_query),
        ListItem(R.string.notification_sample_settings),
        ListItem(R.string.notification_sample_delete)
    )

    data class ListItem(@StringRes val content: Int)
}
