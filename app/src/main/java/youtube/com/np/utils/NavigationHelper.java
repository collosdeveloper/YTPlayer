package youtube.com.np.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import youtube.com.np.activities.MainVideoPlayer;
import youtube.com.np.player.VideoPlayer;
import youtube.com.np.player.playqueue.PlayQueue;

public class NavigationHelper {
	
	public static void playOnMainPlayer(final Context context, final PlayQueue queue) {
		final Intent playerIntent = getPlayerIntent(context, MainVideoPlayer.class, queue);
		playerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(playerIntent);
	}
	
	/*//////////////////////////////////////////////////////////////////////////
    // Players
    //////////////////////////////////////////////////////////////////////////*/
	
	@NonNull
	public static Intent getPlayerIntent(@NonNull final Context context,
	                                     @NonNull final Class targetClazz,
	                                     @NonNull final PlayQueue playQueue) {
		return getPlayerIntent(context, targetClazz, playQueue, null);
	}
	
	@NonNull
	public static Intent getPlayerIntent(@NonNull final Context context,
	                                     @NonNull final Class targetClazz,
	                                     @NonNull final PlayQueue playQueue,
	                                     @Nullable final String quality) {
		Intent intent = new Intent(context, targetClazz);
		
		final String cacheKey = SerializedCache.getInstance().put(playQueue, PlayQueue.class);
		if (cacheKey != null) intent.putExtra(VideoPlayer.PLAY_QUEUE_KEY, cacheKey);
		if (quality != null) intent.putExtra(VideoPlayer.PLAYBACK_QUALITY, quality);
		
		return intent;
	}
}