package youtube.com.np.utils;

import org.schabi.newpipe.extractor.Info;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.stream.StreamInfo;

import java.io.InterruptedIOException;
import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;
import youtube.com.np.NPConstants;

import static youtube.com.utils.LogUtils.LOGD;

public final class ExtractorHelper {
	private static final String TAG = ExtractorHelper.class.getSimpleName();
	private static final InfoCache cache = InfoCache.getInstance();
	
	private ExtractorHelper() {
		// No instance
	}
	
	public static Single<ListExtractor.InfoItemsPage> getMoreChannelItems(final int serviceId,
	                                                                      final String url,
	                                                                      final String nextStreamsUrl) {
		checkServiceId(serviceId);
		return Single.fromCallable(() ->
				ChannelInfo.getMoreItems(NewPipe.getService(serviceId), url, nextStreamsUrl));
	}
	
	private static void checkServiceId(int serviceId) {
		if(serviceId == NPConstants.NO_SERVICE_ID) {
			throw new IllegalArgumentException("serviceId is NO_SERVICE_ID");
		}
	}
	
	public static Single<List<String>> suggestionsFor(final int serviceId,
	                                                  final String query,
	                                                  final String contentCountry) {
		checkServiceId(serviceId);
		return Single.fromCallable(() ->
				NewPipe.getService(serviceId)
						.getSuggestionExtractor()
						.suggestionList(query, contentCountry));
	}
	
	public static Single<StreamInfo> getStreamInfo(final int serviceId,
	                                               final String url,
	                                               boolean forceLoad) {
		checkServiceId(serviceId);
		return checkCache(forceLoad, serviceId, url, Single.fromCallable(() ->
				StreamInfo.getInfo(NewPipe.getService(serviceId), url)));
	}
	
	public static Single<ChannelInfo> getChannelInfo(final int serviceId,
	                                                 final String url,
	                                                 boolean forceLoad) {
		checkServiceId(serviceId);
		return checkCache(forceLoad, serviceId, url, Single.fromCallable(() ->
				ChannelInfo.getInfo(NewPipe.getService(serviceId), url)));
	}
	
	public static Single<PlaylistInfo> getPlaylistInfo(final int serviceId,
	                                                   final String url,
	                                                   boolean forceLoad) {
		checkServiceId(serviceId);
		return checkCache(forceLoad, serviceId, url, Single.fromCallable(() ->
				PlaylistInfo.getInfo(NewPipe.getService(serviceId), url)));
	}
	
	public static Single<KioskInfo> getKioskInfo(final int serviceId,
	                                             final String url,
	                                             final String contentCountry,
	                                             boolean forceLoad) {
		return checkCache(forceLoad, serviceId, url, Single.fromCallable(() ->
				KioskInfo.getInfo(NewPipe.getService(serviceId), url, contentCountry)));
	}

    // ----- Utils -----
	
	/**
	 * Check if we can load it from the cache (forceLoad parameter), if we can't,
	 * load from the network (Single loadFromNetwork)
	 * and put the results in the cache.
	 */
	private static <I extends Info> Single<I> checkCache(boolean forceLoad,
	                                                     int serviceId,
	                                                     String url,
	                                                     Single<I> loadFromNetwork) {
		checkServiceId(serviceId);
		loadFromNetwork = loadFromNetwork.doOnSuccess(info -> cache.putInfo(serviceId, url, info));
		
		Single<I> load;
		if (forceLoad) {
			cache.removeInfo(serviceId, url);
			load = loadFromNetwork;
		} else {
			load = Maybe.concat(ExtractorHelper.loadFromCache(serviceId, url),
					loadFromNetwork.toMaybe())
					.firstElement() // Take the first valid
					.toSingle();
		}
		
		return load;
	}
	
	/**
	 * Default implementation uses the {@link InfoCache} to get cached results
	 */
	public static <I extends Info> Maybe<I> loadFromCache(final int serviceId, final String url) {
		checkServiceId(serviceId);
		return Maybe.defer(() -> {
			// Noinspection unchecked
			I info = (I) cache.getFromKey(serviceId, url);
			LOGD(TAG, "loadFromCache() called, info > " + info);
			
			// Only return info if it's not null (it is cached)
			if (info != null) {
				return Maybe.just(info);
			}
			
			return Maybe.empty();
		});
	}
	
	/**
	 * Check if throwable have the cause that can be assignable from the causes to check.
	 *
	 * @see Class#isAssignableFrom(Class)
	 */
	public static boolean hasAssignableCauseThrowable(Throwable throwable,
	                                                  Class<?>... causesToCheck) {
		// Check if getCause is not the same as cause (the getCause is already the root),
		// as it will cause a infinite loop if it is
		Throwable cause, getCause = throwable;
		
		// Check if throwable is a subclass of any of the filtered classes
		final Class throwableClass = throwable.getClass();
		for (Class<?> causesEl : causesToCheck) {
			if (causesEl.isAssignableFrom(throwableClass)) {
				
				return true;
			}
		}
		
		// Iteratively checks if the root cause of the throwable is a subclass of the filtered class
		while ((cause = throwable.getCause()) != null && getCause != cause) {
			getCause = cause;
			final Class causeClass = cause.getClass();
			for (Class<?> causesEl : causesToCheck) {
				if (causesEl.isAssignableFrom(causeClass)) {
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Check if throwable have the exact cause from one of the causes to check.
	 */
	public static boolean hasExactCauseThrowable(Throwable throwable, Class<?>... causesToCheck) {
		// Check if getCause is not the same as cause (the getCause is already the root),
		// as it will cause a infinite loop if it is
		Throwable cause, getCause = throwable;
		
		for (Class<?> causesEl : causesToCheck) {
			if (throwable.getClass().equals(causesEl)) {
				
				return true;
			}
		}
		
		while ((cause = throwable.getCause()) != null && getCause != cause) {
			getCause = cause;
			for (Class<?> causesEl : causesToCheck) {
				if (cause.getClass().equals(causesEl)) {
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Check if throwable have Interrupted* exception as one of its causes.
	 */
	public static boolean isInterruptedCaused(Throwable throwable) {
		return ExtractorHelper.hasExactCauseThrowable(throwable,
				InterruptedIOException.class,
				InterruptedException.class);
	}
}