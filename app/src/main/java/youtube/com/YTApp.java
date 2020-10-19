package youtube.com;

import android.app.Activity;
import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.Fragment;

import com.orhanobut.hawk.Hawk;

import org.acra.ACRA;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.ConfigurationBuilder;
import org.acra.sender.ReportSenderFactory;
import org.schabi.newpipe.extractor.NewPipe;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.support.HasSupportFragmentInjector;
import io.reactivex.annotations.NonNull;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.MissingBackpressureException;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import ren.yale.android.cachewebviewlib.CacheType;
import ren.yale.android.cachewebviewlib.WebViewCacheInterceptor;
import ren.yale.android.cachewebviewlib.WebViewCacheInterceptorInst;
import youtube.com.ads.Scheduler;
import youtube.com.di.DaggerAppComponent;
import youtube.com.np.activities.ErrorActivity;
import youtube.com.np.utils.Downloader;
import youtube.com.np.utils.ExtractorHelper;
import youtube.com.np.utils.StateSaver;
import youtube.com.np.utils.UserAction;
import youtube.com.np.utils.report.AcraReportSenderFactory;
import youtube.com.screens.error.YTErrorActivity;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;
import static youtube.com.utils.LogUtils.LOGE;

public class YTApp extends MultiDexApplication implements HasActivityInjector, HasSupportFragmentInjector {
	@SuppressWarnings("unchecked")
	private static final Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses
			= new Class[]{AcraReportSenderFactory.class};
	
	@Inject
	DispatchingAndroidInjector<Activity> activityInjector;
	@Inject
	DispatchingAndroidInjector<Fragment> fragmentInjector;
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		
		 // initACRA();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		initDagger();
		initHawk();
		initNP();
		initWebCache();
		initCrashScreen();
		initPopupAds();
		configureRxJavaErrorHandler();
	}
	
	private void initPopupAds() {
		boolean admob_ads_started = Hawk.get("admob_ads_started", false);
		if (BuildConfig.admob_ads_enabled && !admob_ads_started) {
			Scheduler.scheduleAdFromApp(this);
			Hawk.put("admob_ads_started", true);
		}
	}
	
	private void initDagger() {
		DaggerAppComponent
				.builder()
				.application(this)
				.build()
				.inject(this);
	}
	
	private void initHawk() {
		Hawk.init(this)
				.build();
	}
	
	private void initNP() {
		NewPipe.init(getDownloader());
		StateSaver.init(this);
	}
	
	private void initWebCache() {
		WebViewCacheInterceptor.Builder builder =  new WebViewCacheInterceptor.Builder(this);
		// Set cache path, default getCacheDir, name CacheWebViewCache
		String cachePathName = BuildConfig.appName + "WebViewCache";
		builder.setCachePath(new File(this.getCacheDir(), cachePathName))
				.setCacheSize(1024 * 1024 * 100) // Set cache size, default 25Mb
				.setConnectTimeoutSecond(10) // Set http connect timeout, default 20 seconds
				.setReadTimeoutSecond(10) // Set http read timeout, default 20 seconds
				.setCacheType(CacheType.FORCE); // Set cache modal is normal, default is force cache static modal
		WebViewCacheInterceptorInst.getInstance().init(builder);
	}
	
	private void initACRA() {
		try {
			final ACRAConfiguration acraConfig = new ConfigurationBuilder(this)
					.setReportSenderFactoryClasses(reportSenderFactoryClasses)
					.setBuildConfigClass(BuildConfig.class)
					.build();
			ACRA.init(this, acraConfig);
		} catch (ACRAConfigurationException ace) {
			ace.printStackTrace();
			ErrorActivity.reportError(this, ace, null, null,
					ErrorActivity.ErrorInfo.make(UserAction.SOMETHING_ELSE, "none",
							"Could not initialize ACRA crash report", R.string.app_ui_crash));
		}
	}
	
	private void initCrashScreen() {
		CaocConfig.Builder.create()
				.backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
				.showErrorDetails(BuildConfig.DEBUG)
				.logErrorOnRestart(false)
				.minTimeBetweenCrashesMs(2000)
				.errorActivity(YTErrorActivity.class)
				.apply();
	}
	
	protected Downloader getDownloader() {
		return Downloader.init(null);
	}
	
	@Override
	public AndroidInjector<Activity> activityInjector() {
		return activityInjector;
	}
	
	@Override
	public AndroidInjector<Fragment> supportFragmentInjector() {
		return fragmentInjector;
	}
	
	private void configureRxJavaErrorHandler() {
		// https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
		RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
			@Override
			public void accept(@NonNull Throwable throwable) throws Exception {
				LOGE(TAG, "RxJavaPlugins.ErrorHandler called with -> : " +
						"throwable = [" + throwable.getClass().getName() + "]");
				
				if (throwable instanceof UndeliverableException) {
					// As UndeliverableException is a wrapper, get the cause of it to get the "real" exception
					throwable = throwable.getCause();
				}
				
				final List<Throwable> errors;
				if (throwable instanceof CompositeException) {
					errors = ((CompositeException) throwable).getExceptions();
				} else {
					errors = Collections.singletonList(throwable);
				}
				
				for (final Throwable error : errors) {
					if (isThrowableIgnored(error)) return;
					if (isThrowableCritical(error)) {
						reportException(error);
						return;
					}
				}
				
				// Out-of-lifecycle exceptions should only be reported if a debug user wishes so,
				// When exception is not reported, log it
				LOGE(TAG, "RxJavaPlugin: Undeliverable Exception received: ", throwable);
			}
			
			private boolean isThrowableIgnored(@NonNull final Throwable throwable) {
				// Don't crash the application over a simple network problem
				return ExtractorHelper.hasAssignableCauseThrowable(throwable,
						IOException.class, SocketException.class, // network api cancellation
						InterruptedException.class, InterruptedIOException.class); // blocking code disposed
			}
			
			private boolean isThrowableCritical(@NonNull final Throwable throwable) {
				// Though these exceptions cannot be ignored
				return ExtractorHelper.hasAssignableCauseThrowable(throwable,
						NullPointerException.class, IllegalArgumentException.class, // bug in app
						OnErrorNotImplementedException.class, MissingBackpressureException.class,
						IllegalStateException.class); // bug in operator
			}
			
			private void reportException(@NonNull final Throwable throwable) {
				// Throw uncaught exception that will trigger the report system
				Thread.currentThread().getUncaughtExceptionHandler()
						.uncaughtException(Thread.currentThread(), throwable);
			}
		});
	}
}