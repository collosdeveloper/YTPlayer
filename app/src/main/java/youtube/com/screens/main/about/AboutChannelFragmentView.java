package youtube.com.screens.main.about;

import com.arellomobile.mvp.MvpView;

import org.schabi.newpipe.extractor.channel.ChannelInfo;

public interface AboutChannelFragmentView extends MvpView {
	void setChannelData(ChannelInfo channelInfo);
	
	void attachView();
}