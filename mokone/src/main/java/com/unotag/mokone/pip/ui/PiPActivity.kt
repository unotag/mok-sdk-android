package com.unotag.mokone.pip.ui

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.View
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnLayout
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.unotag.mokone.databinding.ActivityPipBinding
import com.unotag.mokone.inAppMessage.InAppMessageHandler
import com.unotag.mokone.inAppMessage.data.InAppMessageItem
import com.unotag.mokone.utils.MokLogger


class PiPActivity : AppCompatActivity() {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPipBinding.inflate(layoutInflater)
    }

    private val playbackStateListener: Player.Listener = playbackStateListener()
    private var player: Player? = null

    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L
    private var mVideoAspectVideo = Rational(9, 16)

    private lateinit var mVideoUrl: String
    private lateinit var mInAppMessageId: String
    private lateinit var mUserId: String


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val inAppMessageItem: InAppMessageItem? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("in_app_message_data", InAppMessageItem::class.java)
            } else {
                intent.getSerializableExtra("in_app_message_data") as InAppMessageItem
            }

        this.mVideoUrl = inAppMessageItem?.jsonData?.popupConfigs?.videoUrl ?: "NA"
        this.mInAppMessageId = inAppMessageItem?.inAppId ?: "NA"
        this.mUserId = inAppMessageItem?.clientId ?: "NA"

        // Configure parameters for the picture-in-picture mode. We do this at the first layout of
        // the MovieView because we use its layout position and size.
        viewBinding.videoView.doOnLayout { updatePictureInPictureParams(mVideoAspectVideo) }

        viewBinding.closeVideoIv.setOnClickListener {
            minimize()
        }
        markInAppMessageAsRead()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    public override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT > 23) {
            initializePlayer()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public override fun onResume() {
        super.onResume()
        // hideSystemUi()
        if (Build.VERSION.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT > 23) {
            releasePlayer()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(UnstableApi::class)
    private fun initializePlayer() {

        viewBinding.videoView.setShowPreviousButton(false)
        viewBinding.videoView.setShowNextButton(false)

        // ExoPlayer implements the Player interface
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                viewBinding.videoView.player = exoPlayer
                // Update the track selection parameters to only pick standard definition tracks
                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                    .buildUpon()
                    .setMaxVideoSizeSd()
                    .build()

                val mediaItem = MediaItem.Builder()
                    //.setUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                   // .setUri("https://assets.mixkit.co/videos/preview/mixkit-girls-leaving-easter-eggs-in-baskets-48597-large.mp4")
                    .setUri(mVideoUrl)
                    .setMimeType(MimeTypes.APPLICATION_MP4)
                    .build()

                exoPlayer.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.prepare()
            }


        // Configure parameters for the picture-in-picture mode. We do this at the first layout of
        // the MovieView because we use its layout position and size.
        // updatePictureInPictureParams(mVideoAspectVideo)
        minimize()
    }

    private fun releasePlayer() {
        player?.let { player ->
            playbackPosition = player.currentPosition
            mediaItemIndex = player.currentMediaItemIndex
            playWhenReady = player.playWhenReady
            player.removeListener(playbackStateListener)
            player.release()
        }
        player = null
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePictureInPictureParams(aspectRatio: Rational): PictureInPictureParams {
        // Calculate the aspect ratio of the PiP screen.
        // val aspectRatio = Rational(2,2)
        // The movie view turns into the picture-in-picture mode.

        val visibleRect = Rect()
        viewBinding.videoView.getGlobalVisibleRect(visibleRect)

        if (Build.VERSION.SDK_INT > 30) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                // Specify the portion of the screen that turns into the picture-in-picture mode.
                // This makes the transition animation smoother.
                .setSourceRectHint(visibleRect)
                // The screen automatically turns into the picture-in-picture mode when it is hidden
                // by the "Home" button.
                .setAutoEnterEnabled(true)
                .setSeamlessResizeEnabled(true)
                .build()
            setPictureInPictureParams(params)
            return params
        } else {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                // Specify the portion of the screen that turns into the picture-in-picture mode.
                // This makes the transition animation smoother.
                .setSourceRectHint(visibleRect)
                .build()
            setPictureInPictureParams(params)
            return params
        }

    }


    /**
     * Enters Picture-in-Picture mode.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun minimize() {
        enterPictureInPictureMode(updatePictureInPictureParams(mVideoAspectVideo))
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            viewBinding.closeVideoIv.visibility = View.GONE
        } else {
            viewBinding.closeVideoIv.visibility = View.VISIBLE
        }
    }


    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, viewBinding.videoView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }


    private fun markInAppMessageAsRead() {
        if (mUserId.isNotEmpty()) {
            val inAppMessageHandler = InAppMessageHandler(this, mUserId)
            inAppMessageHandler.markIAMReadInLocalAndServer(mInAppMessageId, null)
            inAppMessageHandler.markIAMAsSeenLocally(mInAppMessageId)
        } else {
            MokLogger.log(MokLogger.LogLevel.ERROR, "User Id is null, contact mok team")
        }
    }
}

private fun playbackStateListener() = object : Player.Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
        val stateString: String = when (playbackState) {
            ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
            ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
            ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
            ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
            else -> "UNKNOWN_STATE             -"
        }
        Log.d("TAG", "changed state to $stateString")
    }
}