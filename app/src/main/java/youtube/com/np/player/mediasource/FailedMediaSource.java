package youtube.com.np.player.mediasource;

import android.support.annotation.NonNull;


import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.source.BaseMediaSource;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.upstream.Allocator;

import java.io.IOException;

import youtube.com.np.player.playqueue.PlayQueueItem;

import static youtube.com.utils.LogUtils.LOGE;

public class FailedMediaSource extends BaseMediaSource implements ManagedMediaSource {
	private final String TAG = "FailedMediaSource@" + Integer.toHexString(hashCode());
	
	public static class FailedMediaSourceException extends Exception {
		FailedMediaSourceException(String message) {
			super(message);
		}
		
		FailedMediaSourceException(Throwable cause) {
			super(cause);
		}
	}
	
	public static final class MediaSourceResolutionException extends FailedMediaSourceException {
		public MediaSourceResolutionException(String message) {
			super(message);
		}
	}
	
	public static final class StreamInfoLoadException extends FailedMediaSourceException {
		public StreamInfoLoadException(Throwable cause) {
			super(cause);
		}
	}
	
	private final PlayQueueItem playQueueItem;
	private final FailedMediaSourceException error;
	
	private final long retryTimestamp;
	
	public FailedMediaSource(@NonNull final PlayQueueItem playQueueItem,
	                         @NonNull final FailedMediaSourceException error,
	                         final long retryTimestamp) {
		this.playQueueItem = playQueueItem;
		this.error = error;
		this.retryTimestamp = retryTimestamp;
	}
	
	/**
	 * Permanently fail the play queue item associated with this source, with no hope of retrying.
	 * The error will always be propagated to ExoPlayer.
	 * */
	public FailedMediaSource(@NonNull final PlayQueueItem playQueueItem,
	                         @NonNull final FailedMediaSourceException error) {
		this.playQueueItem = playQueueItem;
		this.error = error;
		this.retryTimestamp = Long.MAX_VALUE;
	}
	
	public PlayQueueItem getStream() {
		return playQueueItem;
	}
	
	public FailedMediaSourceException getError() {
		return error;
	}
	
	private boolean canRetry() {
		return System.currentTimeMillis() >= retryTimestamp;
	}
	
	@Override
	public void maybeThrowSourceInfoRefreshError() throws IOException {
		throw new IOException(error);
	}
	
	@Override
	public MediaPeriod createPeriod(MediaPeriodId id, Allocator allocator) {
		return null;
	}
	
	@Override
	public void releasePeriod(MediaPeriod mediaPeriod) {}
	
	
	@Override
	protected void prepareSourceInternal(ExoPlayer player, boolean isTopLevelSource) {
		LOGE(TAG, "Loading failed source: ", error);
	}
	
	@Override
	protected void releaseSourceInternal() {}
	
	@Override
	public boolean shouldBeReplacedWith(@NonNull final PlayQueueItem newIdentity,
	                                    final boolean isInterruptable) {
		return newIdentity != playQueueItem || canRetry();
	}
	
	@Override
	public boolean isStreamEqual(@NonNull PlayQueueItem stream) {
		return playQueueItem == stream;
	}
}