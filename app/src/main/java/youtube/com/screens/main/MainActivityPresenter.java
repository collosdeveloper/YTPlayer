package youtube.com.screens.main;

import android.support.design.widget.BottomNavigationView;

import com.arellomobile.mvp.InjectViewState;

import youtube.com.R;
import youtube.com.base.mvp.BasePresenter;

@InjectViewState
public class MainActivityPresenter extends BasePresenter<MainActivityView> {
	
	@Override
	public void attachView(MainActivityView view) {
		super.attachView(view);
	}
	
	@Override
	protected void onFirstViewAttach() {
		super.onFirstViewAttach();
		
		getViewState().initSwapper();
	}
	
	public void setNavigationItemSelectedListener(BottomNavigationView navigation) {
		navigation.setOnNavigationItemSelectedListener(item -> {
			switch (item.getItemId()) {
				case R.id.nav_video:
					getViewState().goToVideoFragment(false);
					return true;
				case R.id.nav_photo:
					getViewState().goToPhotoFragment();
					return true;
				case R.id.nav_about_channel:
					getViewState().goToAboutChannelFragment();
					return true;
			}
			return false;
		});
	}
}