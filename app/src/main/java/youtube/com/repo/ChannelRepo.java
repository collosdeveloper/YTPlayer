package youtube.com.repo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import org.schabi.newpipe.extractor.channel.ChannelInfo;

public class ChannelRepo {
	private MutableLiveData<ChannelInfo> channelLiveData = new MutableLiveData<>();
	
	public LiveData<ChannelInfo> getLiveData() {
		return channelLiveData;
	}
	
	public void addData(ChannelInfo channelInfo) {
		channelLiveData.setValue(channelInfo);
	}
}