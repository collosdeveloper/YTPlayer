package youtube.com.screens.main.videos;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.jakewharton.rxbinding2.view.RxView;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import youtube.com.BuildConfig;
import youtube.com.R;
import youtube.com.np.NPConstants;
import youtube.com.np.base.BaseListInfoNPFragment;
import youtube.com.np.player.playqueue.ChannelPlayQueue;
import youtube.com.np.player.playqueue.PlayQueue;
import youtube.com.np.utils.ExtractorHelper;
import youtube.com.np.utils.NavigationHelper;
import youtube.com.np.utils.UserAction;
import youtube.com.repo.ChannelRepo;

public class ChannelVideosFragment extends BaseListInfoNPFragment<ChannelInfo> implements ChannelVideosFragmentView {
	private static final String TAG = ChannelVideosFragment.class.getSimpleName();
	
	@Inject
	ChannelRepo channelRepo;
	
	@BindView(R.id.button_play_all)
	Button btnPlayAll;
	@InjectPresenter
	ChannelVideosFragmentPresenter channelVideosFragmentPresenter;
	
	public static ChannelVideosFragment getInstance() {
		ChannelVideosFragment instance = new ChannelVideosFragment();
		instance.setInitialData(BuildConfig.channel_link, BuildConfig.appName);
		return instance;
	}
	
	@Override
	public int getLayoutResourceId() {
		return R.layout.fragment_channel_videos;
	}
	
	@Override
	public void viewCreated(View view, @Nullable Bundle savedInstanceState) {
		RxView.clicks(btnPlayAll)
				.throttleFirst(3, TimeUnit.SECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(o -> NavigationHelper.playOnMainPlayer(activity, getPlayQueue()));
	}
	
	@Override
	protected void onStreamSelected(StreamInfoItem selectedItem) {
		super.onStreamSelected(selectedItem);
		NavigationHelper.playOnMainPlayer(activity, getPlayQueue(selectedItem));
	}
	
	private PlayQueue getPlayQueue() {
		return getPlayQueue(null);
	}
	
	private PlayQueue getPlayQueue(StreamInfoItem selectedItem) {
		int index = 0;
		int calcIndex = 0;
		final List<StreamInfoItem> streamItems = new ArrayList<>();
		for(InfoItem i : infoListAdapter.getItemsList()) {
			if(i instanceof StreamInfoItem) {
				streamItems.add((StreamInfoItem) i);
				if (selectedItem != null && selectedItem.getName().equals(i.getName())) {
					index = calcIndex;
				}
				calcIndex++;
			}
		}
		return new ChannelPlayQueue(
				currentInfo.getServiceId(),
				currentInfo.getUrl(),
				currentInfo.getNextPageUrl(),
				streamItems,
				index
		);
	}
	
	// ----- Load and handle -----
	
	@Override
	protected Single<ListExtractor.InfoItemsPage> loadMoreItemsLogic() {
		return ExtractorHelper.getMoreChannelItems(NPConstants.YT_SERVICE_ID, url, currentNextPageUrl);
	}
	
	@Override
	protected Single<ChannelInfo> loadResult(boolean forceLoad) {
		return ExtractorHelper.getChannelInfo(NPConstants.YT_SERVICE_ID, url, forceLoad);
	}
	
	@Override
	public void handleResult(@NonNull ChannelInfo result) {
		super.handleResult(result);
		
		if (!result.getErrors().isEmpty()) {
			showSnackBarError(result.getErrors(), UserAction.REQUESTED_CHANNEL,
					NewPipe.getNameOfService(result.getServiceId()), result.getUrl(), 0);
		} else {
			channelRepo.addData(result);
			channelVideosFragmentPresenter.onResultSuccess();
		}
	}
	
	@Override
	public void enablePlayAllBtn(boolean enable) {
		btnPlayAll.setClickable(enable);
		btnPlayAll.setEnabled(enable);
	}
	
	@Override
	public void handleNextItems(ListExtractor.InfoItemsPage result) {
		super.handleNextItems(result);
		
		if (!result.getErrors().isEmpty()) {
			showSnackBarError(result.getErrors(), UserAction.REQUESTED_CHANNEL,
					NewPipe.getNameOfService(NPConstants.YT_SERVICE_ID),
					"Get next page of : " + url, R.string.general_error);
		}
	}
	
	@Override
	protected boolean onError(Throwable exception) {
		if (super.onError(exception)) return true;
		
		int errorId = exception instanceof ExtractionException ? R.string.parsing_error : R.string.general_error;
		onUnrecoverableError(exception,
				UserAction.REQUESTED_CHANNEL,
				NewPipe.getNameOfService(NPConstants.YT_SERVICE_ID),
				url,
				errorId);
		return true;
	}
}