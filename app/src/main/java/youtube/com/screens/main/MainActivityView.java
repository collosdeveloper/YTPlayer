package youtube.com.screens.main;

import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import youtube.com.base.mvp.view.ErrorView;

public interface MainActivityView extends ErrorView {
	void initSwapper();
	
	@StateStrategyType(SkipStrategy.class)
	void goToVideoFragment(boolean addToBackStack);
	
	@StateStrategyType(SkipStrategy.class)
	void goToPhotoFragment();
	
	@StateStrategyType(SkipStrategy.class)
	void goToAboutChannelFragment();
}