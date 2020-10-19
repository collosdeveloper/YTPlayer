package youtube.com.np.player.playqueue.events;

import java.io.Serializable;

import youtube.com.np.player.playqueue.PlayQueueEventType;

public interface PlayQueueEvent extends Serializable {
	PlayQueueEventType type();
}