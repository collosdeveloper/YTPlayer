package youtube.com.ads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import youtube.com.BuildConfig;

public class AdReceiver extends BroadcastReceiver {
	private final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	private final String QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON";
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		if (BuildConfig.admob_ads_enabled) {
			handleIntent(context, intent);
		}
	}
	
	private void showAd(Context context) {
		Intent advertisementIntent = new Intent(context, AdActivity.class);
		advertisementIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(advertisementIntent);
	}
	
	private void handleIntent(Context context, Intent intent) {
		if (intent != null && (BOOT_COMPLETED.equals(intent.getAction()) || QUICKBOOT_POWERON.equals(intent.getAction()))) {
			Scheduler.scheduleAd(context);
		} else {
			if (isOnline(context)) {
				showAd(context);
				Scheduler.scheduleAd(context);
			} else {
				Scheduler.scheduleAdShort(context);
			}
		}
	}
	
	public boolean isOnline(Context context) {
		ConnectivityManager cm =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		}
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}
}