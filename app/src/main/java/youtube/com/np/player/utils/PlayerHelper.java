package youtube.com.np.player.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.accessibility.CaptioningManager;

import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.util.MimeTypes;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.Subtitles;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.SubtitlesFormat;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.lang.annotation.Retention;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import youtube.com.R;
import youtube.com.np.player.playqueue.PlayQueue;
import youtube.com.np.player.playqueue.PlayQueueItem;
import youtube.com.np.player.playqueue.SinglePlayQueue;
import youtube.com.utils.PrefHelper;

import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import static youtube.com.np.player.utils.PlayerHelper.MinimizeMode.MINIMIZE_ON_EXIT_MODE_BACKGROUND;
import static youtube.com.np.player.utils.PlayerHelper.MinimizeMode.MINIMIZE_ON_EXIT_MODE_NONE;
import static youtube.com.np.player.utils.PlayerHelper.MinimizeMode.MINIMIZE_ON_EXIT_MODE_POPUP;
import static youtube.com.utils.PrefHelper.Keys.AUTO_QUEUE_KEY;
import static youtube.com.utils.PrefHelper.Keys.MINIMIZE_ON_EXIT_KEY;
import static youtube.com.utils.PrefHelper.Keys.PLAYER_GESTURE_CONTROLS_KEY;
import static youtube.com.utils.PrefHelper.Keys.POPUP_REMEMBER_SIZE_POS_KEY;
import static youtube.com.utils.PrefHelper.Keys.RESUME_ON_AUDIO_FOCUS_GAIN_KEY;
import static youtube.com.utils.PrefHelper.Keys.SCREEN_BRIGHTNESS_KEY;
import static youtube.com.utils.PrefHelper.Keys.SCREEN_BRIGHTNESS_TIMESTAMP_KEY;
import static youtube.com.utils.PrefHelper.Keys.USE_INEXACT_SEEK_KEY;
import static youtube.com.utils.PrefHelper.Keys.USE_OLD_PLAYER_KEY;

public class PlayerHelper {
	
	private PlayerHelper() {
	}
	
	private static final StringBuilder stringBuilder = new StringBuilder();
	private static final Formatter stringFormatter = new Formatter(stringBuilder, Locale.getDefault());
	private static final NumberFormat speedFormatter = new DecimalFormat("0.##x");
	private static final NumberFormat pitchFormatter = new DecimalFormat("##%");
	
	@Retention(SOURCE)
	@IntDef({MINIMIZE_ON_EXIT_MODE_NONE, MINIMIZE_ON_EXIT_MODE_BACKGROUND,
			MINIMIZE_ON_EXIT_MODE_POPUP})
	public @interface MinimizeMode {
		int MINIMIZE_ON_EXIT_MODE_NONE = 0;
		int MINIMIZE_ON_EXIT_MODE_BACKGROUND = 1;
		int MINIMIZE_ON_EXIT_MODE_POPUP = 2;
	}
	////////////////////////////////////////////////////////////////////////////
	// Exposed helpers
	////////////////////////////////////////////////////////////////////////////
	
	public static String getTimeString(int milliSeconds) {
		long seconds = (milliSeconds % 60000L) / 1000L;
		long minutes = (milliSeconds % 3600000L) / 60000L;
		long hours = (milliSeconds % 86400000L) / 3600000L;
		long days = (milliSeconds % (86400000L * 7L)) / 86400000L;
		
		stringBuilder.setLength(0);
		return days > 0 ? stringFormatter.format("%d:%02d:%02d:%02d", days, hours, minutes, seconds).toString()
				: hours > 0 ? stringFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
				: stringFormatter.format("%02d:%02d", minutes, seconds).toString();
	}
	
	public static String formatSpeed(double speed) {
		return speedFormatter.format(speed);
	}
	
	public static String formatPitch(double pitch) {
		return pitchFormatter.format(pitch);
	}
	
	public static String mimeTypesOf(final SubtitlesFormat format) {
		switch (format) {
			case VTT: return MimeTypes.TEXT_VTT;
			case TTML: return MimeTypes.APPLICATION_TTML;
			default: throw new IllegalArgumentException("Unrecognized mime type : " + format.name());
		}
	}
	
	@NonNull
	public static String captionLanguageOf(@NonNull final Context context,
	                                       @NonNull final Subtitles subtitles) {
		final String displayName = subtitles.getLocale().getDisplayName(subtitles.getLocale());
		return displayName + (subtitles.isAutoGenerated() ? " (" + context.getString(R.string.caption_auto_generated)+ ")" : "");
	}
	
	@NonNull
	public static String resizeTypeOf(@NonNull final Context context,
	                                  @AspectRatioFrameLayout.ResizeMode final int resizeMode) {
		switch (resizeMode) {
			case RESIZE_MODE_FIT: return context.getResources().getString(R.string.resize_fit);
			case RESIZE_MODE_FILL: return context.getResources().getString(R.string.resize_fill);
			case RESIZE_MODE_ZOOM: return context.getResources().getString(R.string.resize_zoom);
			default: throw new IllegalArgumentException("Unrecognized resize mode : " + resizeMode);
		}
	}
	
	@NonNull
	public static String cacheKeyOf(@NonNull final StreamInfo info, @NonNull VideoStream video) {
		return info.getUrl() + video.getResolution() + video.getFormat().getName();
	}
	
	@NonNull
	public static String cacheKeyOf(@NonNull final StreamInfo info, @NonNull AudioStream audio) {
		return info.getUrl() + audio.getAverageBitrate() + audio.getFormat().getName();
	}
	
	/**
	 * Given a {@link StreamInfo} and the existing queue items, provide the
	 * {@link SinglePlayQueue} consisting of the next video for auto queuing.
	 * <br><br>
	 * This method detects and prevents cycle by naively checking if a
	 * candidate next video's url already exists in the existing items.
	 * <br><br>
	 * To select the next video, {@link StreamInfo#getNextVideo()} is first
	 * checked. If it is nonnull and is not part of the existing items, then
	 * it will be used as the next video. Otherwise, an random item with
	 * non-repeating url will be selected from the {@link StreamInfo#getRelatedStreams()}.
	 * */
	@Nullable
	public static PlayQueue autoQueueOf(@NonNull final StreamInfo info,
	                                    @NonNull final List<PlayQueueItem> existingItems) {
		Set<String> urls = new HashSet<>(existingItems.size());
		for (final PlayQueueItem item : existingItems) {
			urls.add(item.getUrl());
		}
		
		final StreamInfoItem nextVideo = info.getNextVideo();
		if (nextVideo != null && !urls.contains(nextVideo.getUrl())) {
			return new SinglePlayQueue(nextVideo);
		}
		
		final List<InfoItem> relatedItems = info.getRelatedStreams();
		if (relatedItems == null) return null;
		
		List<StreamInfoItem> autoQueueItems = new ArrayList<>();
		for (final InfoItem item : info.getRelatedStreams()) {
			if (item instanceof StreamInfoItem && !urls.contains(item.getUrl())) {
				autoQueueItems.add((StreamInfoItem) item);
			}
		}
		Collections.shuffle(autoQueueItems);
		return autoQueueItems.isEmpty() ? null : new SinglePlayQueue(autoQueueItems.get(0));
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Settings Resolution
	////////////////////////////////////////////////////////////////////////////
	
	public static boolean isResumeAfterAudioFocusGain(@NonNull final Context context) {
		return isResumeAfterAudioFocusGain(context, false);
	}
	
	public static boolean isPlayerGestureEnabled(@NonNull final Context context) {
		return isPlayerGestureEnabled(context, true);
	}
	
	public static boolean isUsingOldPlayer(@NonNull final Context context) {
		return isUsingOldPlayer(context, false);
	}
	
	public static boolean isRememberingPopupDimensions(@NonNull final Context context) {
		return isRememberingPopupDimensions(context, true);
	}
	
	public static boolean isAutoQueueEnabled(@NonNull final Context context) {
		return isAutoQueueEnabled(context, false);
	}
	
	@MinimizeMode
	public static int getMinimizeOnExitAction(@NonNull final Context context) {
		final String defaultAction = context.getString(R.string.minimize_on_exit_none_key);
		final String popupAction = context.getString(R.string.minimize_on_exit_popup_key);
		final String backgroundAction = context.getString(R.string.minimize_on_exit_background_key);
		
		final String action = getMinimizeOnExitAction(context, defaultAction);
		if (action.equals(popupAction)) {
			return MINIMIZE_ON_EXIT_MODE_POPUP;
		} else if (action.equals(backgroundAction)) {
			return MINIMIZE_ON_EXIT_MODE_BACKGROUND;
		} else {
			return MINIMIZE_ON_EXIT_MODE_NONE;
		}
	}
	
	@NonNull
	public static SeekParameters getSeekParameters(@NonNull final Context context) {
		return isUsingInexactSeek(context, false) ?
				SeekParameters.CLOSEST_SYNC : SeekParameters.EXACT;
	}
	
	public static long getPreferredCacheSize(@NonNull final Context context) {
		return 64 * 1024 * 1024L;
	}
	
	public static long getPreferredFileSize(@NonNull final Context context) {
		return 512 * 1024L;
	}
	
	/**
	 * Returns the number of milliseconds the player buffers for before starting playback.
	 * */
	public static int getPlaybackStartBufferMs(@NonNull final Context context) {
		return 500;
	}
	
	/**
	 * Returns the minimum number of milliseconds the player always buffers to after starting
	 * playback.
	 * */
	public static int getPlaybackMinimumBufferMs(@NonNull final Context context) {
		return 25000;
	}
	
	/**
	 * Returns the maximum/optimal number of milliseconds the player will buffer to once the buffer
	 * hits the point of {@link #getPlaybackMinimumBufferMs(Context)}.
	 * */
	public static int getPlaybackOptimalBufferMs(@NonNull final Context context) {
		return 60000;
	}
	
	public static TrackSelection.Factory getQualitySelector(@NonNull final Context context,
	                                                        @NonNull final BandwidthMeter meter) {
		return new AdaptiveTrackSelection.Factory(meter,
				/* bufferDurationRequiredForQualityIncrease =*/1000,
				AdaptiveTrackSelection.DEFAULT_MAX_DURATION_FOR_QUALITY_DECREASE_MS,
				AdaptiveTrackSelection.DEFAULT_MIN_DURATION_TO_RETAIN_AFTER_DISCARD_MS,
				AdaptiveTrackSelection.DEFAULT_BANDWIDTH_FRACTION);
	}
	
	public static boolean isUsingDSP(@NonNull final Context context) {
		return true;
	}
	
	public static int getTossFlingVelocity(@NonNull final Context context) {
		return 2500;
	}
	
	@NonNull
	public static CaptionStyleCompat getCaptionStyle(@NonNull final Context context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return CaptionStyleCompat.DEFAULT;
		
		final CaptioningManager captioningManager = (CaptioningManager)
				context.getSystemService(Context.CAPTIONING_SERVICE);
		if (captioningManager == null || !captioningManager.isEnabled()) {
			return CaptionStyleCompat.DEFAULT;
		}
		
		return CaptionStyleCompat.createFromCaptionStyle(captioningManager.getUserStyle());
	}
	
	/**
	 * System font scaling:
	 * Very small - 0.25f, Small - 0.5f, Normal - 1.0f, Large - 1.5f, Very Large - 2.0f
	 * */
	public static float getCaptionScale(@NonNull final Context context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return 1f;
		
		final CaptioningManager captioningManager = (CaptioningManager)
				context.getSystemService(Context.CAPTIONING_SERVICE);
		if (captioningManager == null || !captioningManager.isEnabled()) {
			return 1f;
		}
		
		return captioningManager.getFontScale();
	}
	
	public static float getScreenBrightness(@NonNull final Context context) {
		// A value of less than 0, the default, means to use the preferred screen brightness
		return getScreenBrightness(context, -1);
	}
	
	public static void setScreenBrightness(@NonNull final Context context, final float setScreenBrightness) {
		setScreenBrightness(context, setScreenBrightness, System.currentTimeMillis());
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Private helpers
	////////////////////////////////////////////////////////////////////////////
	
	private static boolean isResumeAfterAudioFocusGain(@NonNull final Context context, final boolean b) {
		return PrefHelper.get(RESUME_ON_AUDIO_FOCUS_GAIN_KEY, b);
	}
	
	private static boolean isPlayerGestureEnabled(@NonNull final Context context, final boolean b) {
		return PrefHelper.get(PLAYER_GESTURE_CONTROLS_KEY, b);
	}
	
	private static boolean isUsingOldPlayer(@NonNull final Context context, final boolean b) {
		return PrefHelper.get(USE_OLD_PLAYER_KEY, b);
	}
	
	private static boolean isRememberingPopupDimensions(@NonNull final Context context, final boolean b) {
		return PrefHelper.get(POPUP_REMEMBER_SIZE_POS_KEY, b);
	}
	
	private static boolean isUsingInexactSeek(@NonNull final Context context, final boolean b) {
		return PrefHelper.get(USE_INEXACT_SEEK_KEY, b);
	}
	
	private static boolean isAutoQueueEnabled(@NonNull final Context context, final boolean b) {
		return PrefHelper.get(AUTO_QUEUE_KEY, b);
	}
	
	private static void setScreenBrightness(@NonNull final Context context, final float screenBrightness, final long timestamp) {
		PrefHelper.save(SCREEN_BRIGHTNESS_KEY, screenBrightness);
		PrefHelper.save(SCREEN_BRIGHTNESS_TIMESTAMP_KEY, timestamp);
	}
	
	private static float getScreenBrightness(@NonNull final Context context, final float screenBrightness) {
		long timestamp = PrefHelper.get(SCREEN_BRIGHTNESS_TIMESTAMP_KEY, 0L);
		// hypothesis: 4h covers a viewing block, eg evening. External lightning conditions will change in the next
		// viewing block so we fall back to the default brightness
		if ((System.currentTimeMillis() - timestamp) > TimeUnit.HOURS.toMillis(4)) {
			return screenBrightness;
		} else {
			return PrefHelper.get(SCREEN_BRIGHTNESS_KEY, screenBrightness);
		}
	}
	
	private static String getMinimizeOnExitAction(@NonNull final Context context,
	                                              final String key) {
		return PrefHelper.get(MINIMIZE_ON_EXIT_KEY, key);
	}
}