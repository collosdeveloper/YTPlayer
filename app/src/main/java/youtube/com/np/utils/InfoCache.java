package youtube.com.np.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import org.schabi.newpipe.extractor.Info;

import java.util.Map;

import static youtube.com.utils.LogUtils.LOGD;


public final class InfoCache {
	private static final String TAG = InfoCache.class.getSimpleName();
	
	private static final InfoCache instance = new InfoCache();
	private static final int MAX_ITEMS_ON_CACHE = 60;
	
	/**
	 * Trim the cache to this size
	 */
	private static final int TRIM_CACHE_TO = 30;
	
	private static final LruCache<String, CacheData> lruCache = new LruCache<>(MAX_ITEMS_ON_CACHE);
	
	private InfoCache() {
		// No instance
	}
	
	public static InfoCache getInstance() {
		return instance;
	}
	
	@Nullable
	public Info getFromKey(int serviceId, @NonNull String url) {
		LOGD(TAG, "getFromKey() called with: serviceId = [" + serviceId + "], url = [" + url + "]");
		synchronized (lruCache) {
			return getInfo(lruCache, keyOf(serviceId, url));
		}
	}
	
	public void putInfo(int serviceId, @NonNull String url, @NonNull Info info) {
		LOGD(TAG, "putInfo() called with: info = [" + info + "]");
		
		final long expirationMillis = ServiceHelper.getCacheExpirationMillis();
		synchronized (lruCache) {
			final CacheData data = new CacheData(info, expirationMillis);
			lruCache.put(keyOf(serviceId, url), data);
		}
	}
	
	public void removeInfo(int serviceId, @NonNull String url) {
		LOGD(TAG, "removeInfo() called with: serviceId = [" + serviceId + "], url = [" + url + "]");
		synchronized (lruCache) {
			lruCache.remove(keyOf(serviceId, url));
		}
	}
	
	public void clearCache() {
		LOGD(TAG, "clearCache() called");
		synchronized (lruCache) {
			lruCache.evictAll();
		}
	}
	
	public void trimCache() {
		LOGD(TAG, "trimCache() called");
		synchronized (lruCache) {
			removeStaleCache(lruCache);
			lruCache.trimToSize(TRIM_CACHE_TO);
		}
	}
	
	public long getSize() {
		synchronized (lruCache) {
			return lruCache.size();
		}
	}
	
	@NonNull
	private static String keyOf(final int serviceId, @NonNull final String url) {
		return serviceId + url;
	}
	
	private static void removeStaleCache(@NonNull final LruCache<String, CacheData> cache) {
		for (Map.Entry<String, CacheData> entry : cache.snapshot().entrySet()) {
			final CacheData data = entry.getValue();
			if (data != null && data.isExpired()) {
				cache.remove(entry.getKey());
			}
		}
	}
	
	@Nullable
	private static Info getInfo(@NonNull final LruCache<String, CacheData> cache,
	                            @NonNull final String key) {
		final CacheData data = cache.get(key);
		if (data == null) return null;
		
		if (data.isExpired()) {
			cache.remove(key);
			return null;
		}
		
		return data.info;
	}
	
	final private static class CacheData {
		final private long expireTimestamp;
		final private Info info;
		
		private CacheData(@NonNull final Info info, final long timeoutMillis) {
			this.expireTimestamp = System.currentTimeMillis() + timeoutMillis;
			this.info = info;
		}
		
		private boolean isExpired() {
			return System.currentTimeMillis() > expireTimestamp;
		}
	}
}