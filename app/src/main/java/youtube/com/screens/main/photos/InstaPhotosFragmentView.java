package youtube.com.screens.main.photos;

import com.arellomobile.mvp.MvpView;

public interface InstaPhotosFragmentView extends MvpView {
	void setWebViewClient();
	
	void loadUrl(String url);
}