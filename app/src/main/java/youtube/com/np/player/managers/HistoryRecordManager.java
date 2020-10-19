package youtube.com.np.player.managers;

import android.support.annotation.NonNull;

import org.schabi.newpipe.extractor.stream.StreamInfo;

import youtube.com.np.player.StreamStateEntity;
import youtube.com.utils.PrefHelper;

public class HistoryRecordManager {
	
	public HistoryRecordManager() {
	}
	
	public StreamStateEntity loadStreamState(final StreamInfo info) {
		return PrefHelper.get(info.getUrl());
	}
	
	public void saveStreamState(@NonNull final StreamInfo info, final long progressTime) {
		PrefHelper.save(info.getUrl(), new StreamStateEntity(111111L, progressTime));
	}
}