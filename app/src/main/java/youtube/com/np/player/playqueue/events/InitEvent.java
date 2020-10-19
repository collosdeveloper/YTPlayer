package youtube.com.np.player.playqueue.events;

import youtube.com.np.player.playqueue.PlayQueueEventType;

public class InitEvent implements PlayQueueEvent {
	@Override
	public PlayQueueEventType type() {
		return PlayQueueEventType.INIT;
	}
}