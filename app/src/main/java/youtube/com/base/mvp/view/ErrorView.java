package youtube.com.base.mvp.view;

import com.arellomobile.mvp.MvpView;

public interface ErrorView extends MvpView {
	void showError(String error);
}