package youtube.com.np.utils;

import java.util.concurrent.TimeUnit;

public class ServiceHelper {
	
	public static long getCacheExpirationMillis() {
		return TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);
	}
}