package youtube.com.utils;

import android.util.Log;

import youtube.com.BuildConfig;

public class LogUtils {
	public static final String SHORT_DEBUG_TAG = LogUtils.class.getName();
	// Max Logcat TAG Name 23 symbol.
	public static final boolean DEVELOPER_MODE = BuildConfig.DEBUG;
	
	public static void LOGD(String message) {
		if (DEVELOPER_MODE && message != null) {
			Log.d(SHORT_DEBUG_TAG, message);
		}
	}
	
	public static void LOGD(final String tag, String message) {
		if (DEVELOPER_MODE && message != null) {
			Log.d(tag, message);
		}
	}
	
	public static void LOGD(final String tag, String message, Throwable cause) {
		if (DEVELOPER_MODE && message != null) {
			Log.d(tag, message, cause);
		}
	}
	
	public static void LOGI(final String tag, String message) {
		if (DEVELOPER_MODE && message != null) {
			Log.i(tag, message);
		}
	}
	
	public static void LOGI(final String tag, String message, Throwable cause) {
		if (DEVELOPER_MODE && message != null) {
			Log.i(tag, message, cause);
		}
	}
	
	public static void LOGE(final String tag, String message) {
		if (DEVELOPER_MODE && message != null) {
			Log.e(tag, message);
		}
	}
	
	public static void LOGE(final String tag, String message, Throwable cause) {
		if (DEVELOPER_MODE && message != null) {
			Log.e(tag, message, cause);
		}
	}
	
	public static void LOGW(final String tag, String message) {
		if (DEVELOPER_MODE && message != null) {
			Log.w(tag, message);
		}
	}
	
	public static void LOGW(final String tag, String message, Throwable cause) {
		if (DEVELOPER_MODE && message != null) {
			Log.w(tag, message, cause);
		}
	}
}