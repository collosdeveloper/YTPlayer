package youtube.com.ads;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import youtube.com.BuildConfig;
import youtube.com.R;

public class AdActivity extends AppCompatActivity implements LifecycleHolder {
	private InterstitialAd interstitial;
	private boolean visible;
	private ProgressBar progressBar;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ad);
		progressBar = findViewById(R.id.progress);
		MobileAds.initialize(this, getString(R.string.admob_ads_app_id));
		loadInterstitial();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		visible = true;
		if (interstitial != null && interstitial.isLoaded()) {
			interstitial.show();
			interstitial = null;
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		visible = false;
	}
	
	private void loadInterstitial() {
		interstitial = new InterstitialAd(this);
		if (BuildConfig.DEBUG) {
			interstitial.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
		} else {
			interstitial.setAdUnitId(getString(R.string.admob_ads_banner_id));
		}
		AdRequest adRequest = new AdRequest.Builder().build();
		progressBar.setVisibility(View.VISIBLE);
		interstitial.setAdListener(new com.google.android.gms.ads.AdListener() {
			
			@Override
			public void onAdClosed() {
				finish();
			}
			
			@Override
			public void onAdLoaded() {
				if (isDestroyed()) return;
				progressBar.setVisibility(View.INVISIBLE);
				if (visible) {
					interstitial.show();
					interstitial = null;
				}
			}
			
			@Override
			public void onAdFailedToLoad(int i) {
				onAdFailed(AdType.INTERSTITIAL);
			}
		});
		interstitial.loadAd(adRequest);
	}
	
	
	@Override
	public boolean isVisible() {
		return visible;
	}
	
	@Override
	public void onAdFailed(AdType type) {
		if (isDestroyed()) return;
		switch (type) {
			case INTERSTITIAL:
				finish();
				break;
		}
	}
}