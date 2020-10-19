package youtube.com.screens.main.photos;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.arellomobile.mvp.presenter.InjectPresenter;

import butterknife.BindView;
import ren.yale.android.cachewebviewlib.WebViewCacheInterceptorInst;
import youtube.com.R;
import youtube.com.base.interfaces.BackPressable;
import youtube.com.base.ui.BaseFragment;

public class InstaPhotosFragment extends BaseFragment implements InstaPhotosFragmentView, BackPressable {
	private static final String TAG = InstaPhotosFragment.class.getSimpleName();
	
	@BindView(R.id.loading_progress_bar)
	ProgressBar progressBar;
	@BindView(R.id.insta_webview)
	WebView instaWebView;
	@InjectPresenter
	InstaPhotosFragmentPresenter instaPhotosFragmentPresenter;
	
	@Override
	public int getLayoutResourceId() {
		return R.layout.fragment_insta_photos;
	}
	
	@Override
	public void viewCreated(View view, @Nullable Bundle savedInstanceState) {
	}
	
	@Override
	public void setWebViewClient() {
		slideUp();
		
		instaPhotosFragmentPresenter.setWebViewSettings(instaWebView);
		instaWebView.setWebViewClient(instaPhotosFragmentPresenter.getWebViewClient(instaWebView));
		instaWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView webView1, int newProgress){
				if (newProgress == 100){
					if (progressBar != null) {
						progressBar.setVisibility(View.GONE);
					}
				}
			}
		});
	}
	
	@Override
	public void loadUrl(String url) {
		progressBar.setVisibility(View.VISIBLE);
		WebViewCacheInterceptorInst.getInstance().loadUrl(instaWebView, url);
	}
	
	@Override
	public boolean onBackPressed() {
		if (instaWebView.canGoBack()) {
			instaWebView.goBack();
			return false;
		}
		
		return true;
	}
}