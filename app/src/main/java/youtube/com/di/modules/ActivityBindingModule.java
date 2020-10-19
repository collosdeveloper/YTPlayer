package youtube.com.di.modules;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import youtube.com.screens.main.MainActivity;

@Module
public abstract class ActivityBindingModule {
	@ContributesAndroidInjector()
	abstract MainActivity mainActivityInjector();
}