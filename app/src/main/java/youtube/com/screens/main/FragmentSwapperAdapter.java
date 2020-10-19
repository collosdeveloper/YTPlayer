package youtube.com.screens.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import youtube.com.base.ui.adaptablebottomnavigation.adapter.FragmentStateAdapter;
import youtube.com.screens.main.about.AboutChannelFragment;
import youtube.com.screens.main.photos.InstaPhotosFragment;
import youtube.com.screens.main.videos.ChannelVideosFragment;

public class FragmentSwapperAdapter extends FragmentStateAdapter {
	private static final int INDEX_VIDEOS = 0;
	private static final int INDEX_PHOTOS = 1;
	private static final int INDEX_ABOUT = 2;
	
	private static final int FRAGMENT_COUNT = 3;
	
	public FragmentSwapperAdapter(FragmentManager fm) {
		super(fm);
	}
	
	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case INDEX_VIDEOS:
				ChannelVideosFragment fragment = ChannelVideosFragment.getInstance();
				fragment.useAsFrontPage(true);
				return fragment;
			case INDEX_PHOTOS:
				return new InstaPhotosFragment();
			case INDEX_ABOUT:
				return new AboutChannelFragment();
		}
		return null;
	}
	
	@Override
	public int getCount() {
		return FRAGMENT_COUNT;
	}
}