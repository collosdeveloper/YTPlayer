package youtube.com.np.utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.PluralsRes;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import youtube.com.R;
import youtube.com.utils.PrefHelper;

import static youtube.com.utils.PrefHelper.Keys.SEARCH_LANGUAGE_KEY;

public class Localization {
	public final static String DOT_SEPARATOR = " • ";
	
	private Localization() {
	}
	
	@NonNull
	public static String concatenateStrings(final String... strings) {
		return concatenateStrings(Arrays.asList(strings));
	}
	
	@NonNull
	public static String concatenateStrings(final List<String> strings) {
		if (strings.isEmpty()) return "";
		
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(strings.get(0));
		
		for (int i = 1; i < strings.size(); i++) {
			final String string = strings.get(i);
			if (!TextUtils.isEmpty(string)) {
				stringBuilder.append(DOT_SEPARATOR).append(strings.get(i));
			}
		}
		
		return stringBuilder.toString();
	}
	
	public static Locale getPreferredLocale(Context context) {
		String languageCode = PrefHelper.get(SEARCH_LANGUAGE_KEY, context.getString(R.string.default_language_value));
		
		try {
			if (languageCode.length() == 2) {
				return new Locale(languageCode);
			} else if (languageCode.contains("_")) {
				String country = languageCode.substring(languageCode.indexOf("_"), languageCode.length());
				return new Locale(languageCode.substring(0, 2), country);
			}
		} catch (Exception ignored) {
		}
		
		return Locale.getDefault();
	}
	
	public static String localizeNumber(Context context, long number) {
		Locale locale = getPreferredLocale(context);
		NumberFormat nf = NumberFormat.getInstance(locale);
		return nf.format(number);
	}
	
	private static String formatDate(Context context, String date) {
		Locale locale = getPreferredLocale(context);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date datum = null;
		try {
			datum = formatter.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		
		return df.format(datum);
	}
	
	public static String localizeDate(Context context, String date) {
		Resources res = context.getResources();
		String dateString = res.getString(R.string.upload_date_text);
		
		String formattedDate = formatDate(context, date);
		return String.format(dateString, formattedDate);
	}
	
	public static String localizeViewCount(Context context, long viewCount) {
		return getQuantity(context, R.plurals.views, R.string.no_views, viewCount, localizeNumber(context, viewCount));
	}
	
	public static String localizeSubscribersCount(Context context, long subscriberCount) {
		return getQuantity(context, R.plurals.subscribers, R.string.no_subscribers, subscriberCount, localizeNumber(context, subscriberCount));
	}
	
	public static String localizeStreamCount(Context context, long streamCount) {
		return getQuantity(context, R.plurals.videos, R.string.no_videos, streamCount, localizeNumber(context, streamCount));
	}
	
	public static String shortCount(Context context, long count) {
		if (count >= 1000000000) {
			return Long.toString(count / 1000000000) + context.getString(R.string.short_billion);
		} else if (count >= 1000000) {
			return Long.toString(count / 1000000) + context.getString(R.string.short_million);
		} else if (count >= 1000) {
			return Long.toString(count / 1000) + context.getString(R.string.short_thousand);
		} else {
			return Long.toString(count);
		}
	}
	
	public static String shortViewCount(Context context, long viewCount) {
		return getQuantity(context, R.plurals.views, R.string.no_views, viewCount, shortCount(context, viewCount));
	}
	
	public static String shortSubscriberCount(Context context, long subscriberCount) {
		return getQuantity(context, R.plurals.subscribers, R.string.no_subscribers, subscriberCount, shortCount(context, subscriberCount));
	}
	
	private static String getQuantity(Context context, @PluralsRes int pluralId, @StringRes int zeroCaseStringId, long count, String formattedCount) {
		if (count == 0) return context.getString(zeroCaseStringId);
		
		// As we use the already formatted count, is not the responsibility of this method handle long numbers
		// (it probably will fall in the "other" category, or some language have some specific rule... then we have to change it)
		int safeCount = count > Integer.MAX_VALUE ? Integer.MAX_VALUE : count < Integer.MIN_VALUE ? Integer.MIN_VALUE : (int) count;
		return context.getResources().getQuantityString(pluralId, safeCount, formattedCount);
	}
	
	public static String getDurationString(long duration) {
		if (duration < 0) {
			duration = 0;
		}
		String output;
		long days = duration / (24 * 60 * 60L); /* greater than a day */
		duration %= (24 * 60 * 60L);
		long hours = duration / (60 * 60L); /* greater than an hour */
		duration %= (60 * 60L);
		long minutes = duration / 60L;
		long seconds = duration % 60L;
		
		// handle days
		if (days > 0) {
			output = String.format(Locale.US, "%d:%02d:%02d:%02d", days, hours, minutes, seconds);
		} else if (hours > 0) {
			output = String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
		} else {
			output = String.format(Locale.US, "%d:%02d", minutes, seconds);
		}
		return output;
	}
}