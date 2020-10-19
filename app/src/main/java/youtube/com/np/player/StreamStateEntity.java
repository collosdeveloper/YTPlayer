package youtube.com.np.player;

public class StreamStateEntity {
	private long streamUid;
	private long progressTime;
	
	public StreamStateEntity(long streamUid, long progressTime) {
		this.streamUid = streamUid;
		this.progressTime = progressTime;
	}
	
	public long getStreamUid() {
		return streamUid;
	}
	
	public void setStreamUid(long streamUid) {
		this.streamUid = streamUid;
	}
	
	public long getProgressTime() {
		return progressTime;
	}
	
	public void setProgressTime(long progressTime) {
		this.progressTime = progressTime;
	}
}