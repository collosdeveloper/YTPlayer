package youtube.com.di.modules;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import youtube.com.screens.main.about.AboutChannelFragment;
import youtube.com.screens.main.photos.InstaPhotosFragment;
import youtube.com.screens.main.videos.ChannelVideosFragment;

@Module
public abstract class FragmentBindingModule {
	@ContributesAndroidInjector()
	abstract ChannelVideosFragment channelVideosFragmentInjector();
	
	@ContributesAndroidInjector()
	abstract InstaPhotosFragment instaPhotosFragmentInjector();
	
	@ContributesAndroidInjector()
	abstract AboutChannelFragment aboutChannelFragmentInjector();
}