package youtube.com.ads;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class Scheduler {
	public static final long HOUR = 3600 * 1000;
	public static final long SHOW_AD_TIMEOUT_VERY_LONG = 4 * HOUR;
	public static final long SHOW_AD_TIMEOUT_LONG = 2 * HOUR;
	public static final long SHOW_AD_TIMEOUT_SHORT = HOUR / 2;
	
	public static void scheduleAdFromApp(Context context) {
		scheduleAd(context, SHOW_AD_TIMEOUT_VERY_LONG);
	}
	
	public static void scheduleAd(Context context) {
		scheduleAd(context, SHOW_AD_TIMEOUT_LONG);
	}
	
	public static void scheduleAdShort(Context context) {
		scheduleAd(context, SHOW_AD_TIMEOUT_SHORT);
	}
	
	private static void scheduleAd(Context context, long time) {
		Intent intent = new Intent(context, AdReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time, pendingIntent);
		} else {
			alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time, pendingIntent);
		}
	}
}