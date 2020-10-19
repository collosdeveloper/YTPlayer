package youtube.com.screens.main.about;

import com.arellomobile.mvp.InjectViewState;

import youtube.com.base.mvp.BasePresenter;

@InjectViewState
public class AboutChannelFragmentPresenter extends BasePresenter<AboutChannelFragmentView> {
	private static final String TAG = AboutChannelFragmentPresenter.class.getSimpleName();
	
	@Override
	protected void onFirstViewAttach() {
		super.onFirstViewAttach();
	}
	
	@Override
	public void attachView(AboutChannelFragmentView view) {
		super.attachView(view);
		
		view.attachView();
	}
}