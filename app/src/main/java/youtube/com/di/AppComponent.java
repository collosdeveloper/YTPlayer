package youtube.com.di;


import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import youtube.com.YTApp;
import youtube.com.di.modules.ActivityBindingModule;
import youtube.com.di.modules.AppModule;
import youtube.com.di.modules.FragmentBindingModule;
import youtube.com.di.modules.RepositoriesModule;

@Singleton
@Component(modules = {
		AppModule.class,
		RepositoriesModule.class,
		ActivityBindingModule.class,
		FragmentBindingModule.class,
		AndroidSupportInjectionModule.class
})
public interface AppComponent extends AndroidInjector<YTApp> {
	
	@Component.Builder
	interface Builder {
		@BindsInstance
		AppComponent.Builder application(Application application);
		
		AppComponent build();
	}
}