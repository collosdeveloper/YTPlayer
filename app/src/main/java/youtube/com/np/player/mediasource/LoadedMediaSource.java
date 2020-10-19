package youtube.com.np.player.mediasource;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.upstream.Allocator;

import java.io.IOException;

import youtube.com.np.player.playqueue.PlayQueueItem;

public class LoadedMediaSource implements ManagedMediaSource {
	
	private final MediaSource source;
	private final PlayQueueItem stream;
	private final long expireTimestamp;
	
	public LoadedMediaSource(@NonNull final MediaSource source,
	                         @NonNull final PlayQueueItem stream,
	                         final long expireTimestamp) {
		this.source = source;
		this.stream = stream;
		this.expireTimestamp = expireTimestamp;
	}
	
	public PlayQueueItem getStream() {
		return stream;
	}
	
	private boolean isExpired() {
		return System.currentTimeMillis() >= expireTimestamp;
	}
	
	@Override
	public void prepareSource(ExoPlayer player, boolean isTopLevelSource,
	                          MediaSource.SourceInfoRefreshListener listener) {
		source.prepareSource(player, isTopLevelSource, listener);
	}
	
	@Override
	public void maybeThrowSourceInfoRefreshError() throws IOException {
		source.maybeThrowSourceInfoRefreshError();
	}
	
	@Override
	public MediaPeriod createPeriod(MediaSource.MediaPeriodId id, Allocator allocator) {
		return source.createPeriod(id, allocator);
	}
	
	@Override
	public void releasePeriod(MediaPeriod mediaPeriod) {
		source.releasePeriod(mediaPeriod);
	}
	
	@Override
	public void releaseSource(MediaSource.SourceInfoRefreshListener listener) {
		source.releaseSource(listener);
	}
	
	@Override
	public void addEventListener(Handler handler, MediaSourceEventListener eventListener) {
		source.addEventListener(handler, eventListener);
	}
	
	@Override
	public void removeEventListener(MediaSourceEventListener eventListener) {
		source.removeEventListener(eventListener);
	}
	
	@Override
	public boolean shouldBeReplacedWith(@NonNull PlayQueueItem newIdentity,
	                                    final boolean isInterruptable) {
		return newIdentity != stream || (isInterruptable && isExpired());
	}
	
	@Override
	public boolean isStreamEqual(@NonNull PlayQueueItem stream) {
		return this.stream == stream;
	}
}