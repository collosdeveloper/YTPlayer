package youtube.com.screens.main.photos;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.arellomobile.mvp.InjectViewState;

import ren.yale.android.cachewebviewlib.WebViewCacheInterceptorInst;
import youtube.com.BuildConfig;
import youtube.com.base.mvp.BasePresenter;

import static youtube.com.utils.LogUtils.LOGI;

@InjectViewState
public class InstaPhotosFragmentPresenter extends BasePresenter<InstaPhotosFragmentView> {
	private static final String TAG = InstaPhotosFragmentPresenter.class.getSimpleName();
	
	private final String URL_INSTA_FILTER = "www.instagram.com";
	
	@Override
	protected void onFirstViewAttach() {
		super.onFirstViewAttach();
		LOGI(TAG, "onFirstViewAttach");
	}
	
	@Override
	public void attachView(InstaPhotosFragmentView view) {
		super.attachView(view);
		LOGI(TAG, "attachView");
		
		view.setWebViewClient();
		view.loadUrl(BuildConfig.instagram_link);
	}
	
	public WebViewClient getWebViewClient(WebView webView) {
		return new WebViewClient(){
			
			@TargetApi(Build.VERSION_CODES.LOLLIPOP)
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				String url = request.getUrl().toString();
				if (url.contains(URL_INSTA_FILTER)) {
					WebViewCacheInterceptorInst.getInstance().loadUrl(webView, request.getUrl().toString());
				}
				return true;
			}
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.contains(URL_INSTA_FILTER)) {
					WebViewCacheInterceptorInst.getInstance().loadUrl(webView, url);
				}
				return true;
			}
			
			@TargetApi(Build.VERSION_CODES.LOLLIPOP)
			@Nullable
			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
				return WebViewCacheInterceptorInst.getInstance().interceptRequest(request);
			}
			
			@Nullable
			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
				return WebViewCacheInterceptorInst.getInstance().interceptRequest(url);
			}
		};
	}
	
	public void setWebViewSettings(WebView webView) {
		WebSettings webSettings = webView.getSettings();
		
		webSettings.setJavaScriptEnabled(true);
		
		webSettings.setDomStorageEnabled(true);
		webSettings.setAllowFileAccess(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(false);
		webSettings.setDisplayZoomControls(false);
		
		webSettings.setDefaultTextEncodingName("UTF-8");
		
		webSettings.setAllowFileAccessFromFileURLs(true);
		webSettings.setAllowUniversalAccessFromFileURLs(true);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			CookieManager cookieManager = CookieManager.getInstance();
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			webSettings.setMixedContentMode(
					WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
		}
	}
}