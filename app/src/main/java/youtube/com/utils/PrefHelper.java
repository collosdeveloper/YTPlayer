package youtube.com.utils;

import com.orhanobut.hawk.Hawk;

import static youtube.com.utils.PrefHelper.Keys.NEED_TO_SHOW_ADS_KEY;

public class PrefHelper {
	public class Keys {
		public static final String SEARCH_LANGUAGE_KEY = "search_language_key";
		public static final String DEF_VIDEO_FORMAT_KEY = "default_video_format_key";
		public static final String MOBILE_DATA_USAGE_KEY = "limit_mobile_data_usage_key";
		public static final String DEF_AUDIO_FORMAT_KEY = "default_audio_format_key";
		public static final String SHOW_HIGHER_RESOLUTION_KEY = "show_higher_resolutions_key";
		public static final String LAST_ORIENTATION_LANDSCAPE_KEY = "last_orientation_landscape_key";
		public static final String RESUME_ON_AUDIO_FOCUS_GAIN_KEY = "resume_on_audio_focus_gain_key";
		public static final String PLAYER_GESTURE_CONTROLS_KEY = "player_gesture_controls_key";
		public static final String USE_OLD_PLAYER_KEY = "use_old_player_key";
		public static final String POPUP_REMEMBER_SIZE_POS_KEY = "popup_remember_size_pos_key";
		public static final String USE_INEXACT_SEEK_KEY = "use_inexact_seek_key";
		public static final String AUTO_QUEUE_KEY = "auto_queue_key";
		public static final String SCREEN_BRIGHTNESS_KEY = "screen_brightness_key";
		public static final String SCREEN_BRIGHTNESS_TIMESTAMP_KEY = "screen_brightness_timestamp_key";
		public static final String MINIMIZE_ON_EXIT_KEY = "minimize_on_exit_key";
		public static final String NEED_TO_SHOW_ADS_KEY = "need_to_show_ads_key";
	}
	
	public static <T> boolean save(String key, T object) {
		return Hawk.put(key, object);
	}
	
	public static <T> T get(String key) {
		return Hawk.get(key);
	}
	
	public static <T> T get(String key, T defaultValue) {
		return Hawk.get(key, defaultValue);
	}
	
	public static boolean delete(String key) {
		return Hawk.delete(key);
	}
	
	public static boolean clear() {
		return Hawk.deleteAll();
	}
	
	public static boolean isNeedToShowAds() {
		int count = get(NEED_TO_SHOW_ADS_KEY, 1);
		if(count % 5 == 0) {
			count++;
			save(NEED_TO_SHOW_ADS_KEY, count);
			return true;
		} else {
			count++;
			save(NEED_TO_SHOW_ADS_KEY, count);
			return false;
		}
	}
}