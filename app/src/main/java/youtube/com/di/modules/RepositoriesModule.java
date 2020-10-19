package youtube.com.di.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import youtube.com.repo.ChannelRepo;

@Module()
public class RepositoriesModule {
	@Singleton
	@Provides
	ChannelRepo provideChannelRepo() {
		return new ChannelRepo();
	}
}
