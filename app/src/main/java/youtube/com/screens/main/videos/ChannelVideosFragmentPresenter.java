package youtube.com.screens.main.videos;

import com.arellomobile.mvp.InjectViewState;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import youtube.com.base.mvp.BasePresenter;

@InjectViewState
public class ChannelVideosFragmentPresenter extends BasePresenter<ChannelVideosFragmentView> {
	private static final String TAG = ChannelVideosFragmentPresenter.class.getSimpleName();
	
	@Override
	protected void onFirstViewAttach() {
		super.onFirstViewAttach();
		
		getViewState().enablePlayAllBtn(false);
	}
	
	@Override
	public void attachView(ChannelVideosFragmentView view) {
		super.attachView(view);
	}
	
	public void onResultSuccess() {
		unsubscribeOnDestroy(Observable.just(true)
				.delay(3, TimeUnit.SECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(enable -> getViewState().enablePlayAllBtn(enable)));
	}
}