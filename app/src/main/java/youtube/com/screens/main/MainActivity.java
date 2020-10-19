package youtube.com.screens.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.arellomobile.mvp.presenter.InjectPresenter;

import javax.inject.Inject;

import butterknife.BindView;
import youtube.com.R;
import youtube.com.base.ui.BaseActivity;
import youtube.com.base.ui.adaptablebottomnavigation.view.AdaptableBottomNavigationView;
import youtube.com.base.ui.adaptablebottomnavigation.view.ViewSwapper;
import youtube.com.repo.ChannelRepo;
import youtube.com.screens.main.about.AboutChannelFragment;
import youtube.com.screens.main.photos.InstaPhotosFragment;
import youtube.com.screens.main.videos.ChannelVideosFragment;
import youtube.com.utils.BottomNavigationViewBehavior;
import youtube.com.utils.TextUtils;

import static youtube.com.utils.LogUtils.LOGD;

public class MainActivity extends BaseActivity implements MainActivityView {
	private static final String TAG = MainActivity.class.getSimpleName();
	
	@Inject
	ChannelRepo channelRepo;
	
	@BindView(R.id.fragment_container)
	ViewSwapper fragmentContainer;
	@BindView(R.id.bottom_navigation_view)
	AdaptableBottomNavigationView navView;
	
	private BottomNavigationViewBehavior bottomNavigationViewBehavior;
	
	@InjectPresenter
	MainActivityPresenter mainActivityPresenter;
	
	@Override
	public int getLayoutResourceId() {
		return R.layout.activity_main;
	}
	
	@Override
	public boolean isHasFragment() {
		return true;
	}
	
	@Override
	public void slideUp() {
		bottomNavigationViewBehavior.slideUp(navView);
	}
	
	@Override
	public void initSwapper() {
		fragmentContainer.setAdapter(new FragmentSwapperAdapter(getSupportFragmentManager()));
		navView.setupWithViewSwapper(fragmentContainer);
		bottomNavigationViewBehavior = new BottomNavigationViewBehavior();
		CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) navView.getLayoutParams();
		layoutParams.setBehavior(bottomNavigationViewBehavior);
	}
	
	@Override
	public void initUI(Bundle savedInstanceState) {
		channelRepo.getLiveData().observe(this,
				channelInfo -> {
					if (channelInfo != null) {
						navView.setVisibility(View.VISIBLE);
					}
				});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.channel_action, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_item_openInBrowser:
				openUrlInBrowser();
				return true;
			case R.id.menu_item_share:
				shareUrl();
				return true;
		}
		
		return(super.onOptionsItemSelected(item));
	}
	
	private void openUrlInBrowser() {
		channelRepo.getLiveData().observe(this,
				channelInfo -> {
					if (channelInfo != null && !TextUtils.isEmpty(channelInfo.getUrl())) {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(channelInfo.getUrl()));
						startActivity(Intent.createChooser(intent, getString(R.string.share_dialog_title)));
					} else {
						showError(getString(R.string.msg_wait));
					}
				});
	}
	
	private void shareUrl() {
		channelRepo.getLiveData().observe(this,
				channelInfo -> {
					if (channelInfo != null && !TextUtils.isEmpty(channelInfo.getName())) {
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType("text/plain");
						intent.putExtra(Intent.EXTRA_SUBJECT, channelInfo.getName());
						intent.putExtra(Intent.EXTRA_TEXT, channelInfo.getUrl());
						startActivity(Intent.createChooser(intent, getString(R.string.share_dialog_title)));
					} else {
						showError(getString(R.string.msg_wait));
					}
				});
	}
	
	@Override
	public void goToVideoFragment(boolean addToBackStack) {
		ChannelVideosFragment fragment = ChannelVideosFragment.getInstance();
		fragment.useAsFrontPage(true);
		switchFragment(fragment, addToBackStack);
	}
	
	@Override
	public void goToPhotoFragment() {
		switchFragment(new InstaPhotosFragment(), false);
	}
	
	@Override
	public void goToAboutChannelFragment() {
		switchFragment(new AboutChannelFragment(), false);
	}
	
	@Override
	public void showError(String error) {
		LOGD(TAG, "showError : " + error);
	}
}