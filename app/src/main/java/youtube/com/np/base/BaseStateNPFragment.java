package youtube.com.np.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;

import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import youtube.com.R;
import youtube.com.np.activities.ReCaptchaActivity;
import youtube.com.np.interfaces.ViewContract;
import youtube.com.np.utils.ExtractorHelper;
import youtube.com.np.utils.InfoCache;
import youtube.com.np.utils.UserAction;

import static youtube.com.np.utils.AnimationUtils.animateView;
import static youtube.com.utils.LogUtils.LOGD;
import static youtube.com.utils.LogUtils.LOGE;
import static youtube.com.utils.LogUtils.LOGW;

public abstract class BaseStateNPFragment<I> extends BaseNPFragment implements ViewContract<I> {
	private static final String TAG = BaseStateNPFragment.class.getSimpleName();
	
	@State
	protected AtomicBoolean wasLoading = new AtomicBoolean();
	protected AtomicBoolean isLoading = new AtomicBoolean();
	
	@BindView(R.id.empty_state_view)
	protected View emptyStateView;
	@BindView(R.id.loading_progress_bar)
	protected ProgressBar loadingProgressBar;
	@BindView(R.id.error_panel)
	protected View errorPanelRoot;
	@BindView(R.id.error_button_retry)
	protected Button errorButtonRetry;
	@BindView(R.id.error_message_view)
	protected TextView errorTextView;
	
	@Override
	public void onViewCreated(View rootView, Bundle savedInstanceState) {
		super.onViewCreated(rootView, savedInstanceState);
		doInitialLoadLogic();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		wasLoading.set(isLoading.get());
	}
	
	// ----- Init -----
	
	@Override
	protected void initListeners() {
		super.initListeners();
		RxView.clicks(errorButtonRetry)
				.debounce(300, TimeUnit.MILLISECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(o -> onRetryButtonClicked());
	}
	
	protected void onRetryButtonClicked() {
		reloadContent();
	}
	
	public void reloadContent() {
		startLoading(true);
	}
	
	// ----- Load -----
	
	protected void doInitialLoadLogic() {
		startLoading(true);
	}
	
	protected void startLoading(boolean forceLoad) {
		LOGD(TAG, "startLoading() called with: forceLoad = [" + forceLoad + "]");
		showLoading();
		isLoading.set(true);
	}
	
	// ----- Contract -----
	
	@Override
	public void showLoading() {
		if (emptyStateView != null) animateView(emptyStateView, false, 150);
		if (loadingProgressBar != null) animateView(loadingProgressBar, true, 400);
		animateView(errorPanelRoot, false, 150);
	}
	
	@Override
	public void hideLoading() {
		if (emptyStateView != null) animateView(emptyStateView, false, 150);
		if (loadingProgressBar != null) animateView(loadingProgressBar, false, 0);
		animateView(errorPanelRoot, false, 150);
	}
	
	@Override
	public void showEmptyState() {
		isLoading.set(false);
		if (emptyStateView != null) animateView(emptyStateView, true, 200);
		if (loadingProgressBar != null) animateView(loadingProgressBar, false, 0);
		animateView(errorPanelRoot, false, 150);
	}
	
	@Override
	public void showError(String message, boolean showRetryButton) {
		LOGE(TAG, "showError() called with: message = [" + message + "], showRetryButton = [" + showRetryButton + "]");
		isLoading.set(false);
		InfoCache.getInstance().clearCache();
		hideLoading();
		
		errorTextView.setText(message);
		if (showRetryButton) animateView(errorButtonRetry, true, 600);
		else animateView(errorButtonRetry, false, 0);
		animateView(errorPanelRoot, true, 300);
	}
	
	@Override
	public void handleResult(I result) {
		LOGD(TAG, "handleResult() called with: result = [" + result + "]");
		hideLoading();
	}
	
	// ----- Error handling -----
	
	/**
	 * Default implementation handles some general exceptions
	 *
	 * @return if the exception was handled
	 */
	protected boolean onError(Throwable exception) {
		LOGD(TAG, "onError() called with: exception = [" + exception + "]");
		isLoading.set(false);
		
		if (isDetached() || isRemoving()) {
			LOGW(TAG, "onError() is detached or removing = [" + exception + "]");
			return true;
		}
		
		if (ExtractorHelper.isInterruptedCaused(exception)) {
			LOGW(TAG, "onError() isInterruptedCaused! = [" + exception + "]");
			return true;
		}
		
		if (exception instanceof ReCaptchaException) {
			onReCaptchaException();
			return true;
		} else if (exception instanceof IOException) {
			showError(getString(R.string.network_error), true);
			return true;
		}
		
		return false;
	}
	
	public void onReCaptchaException() {
		LOGD(TAG, "onReCaptchaException() called");
		Toast.makeText(activity, R.string.recaptcha_request_toast, Toast.LENGTH_LONG).show();
		// Starting ReCaptcha Challenge Activity
		startActivityForResult(new Intent(activity, ReCaptchaActivity.class), ReCaptchaActivity.RECAPTCHA_REQUEST);
		
		showError(getString(R.string.recaptcha_request_toast), false);
	}
	
	public void onUnrecoverableError(Throwable exception, UserAction userAction, String serviceName, String request, @StringRes int errorId) {
		onUnrecoverableError(Collections.singletonList(exception), userAction, serviceName, request, errorId);
	}
	
	public void onUnrecoverableError(List<Throwable> exception, UserAction userAction, String serviceName, String request, @StringRes int errorId) {
		LOGD(TAG, "onUnrecoverableError() called with: exception = [" + exception + "]");
		
		if (serviceName == null) serviceName = "none";
		if (request == null) request = "none";
		// TODO NEED FIX IT
//		ErrorActivity.reportError(getContext(), exception, MainActivity.class, null, ErrorActivity.ErrorInfo.make(userAction, serviceName, request, errorId));
	}
	
	public void showSnackBarError(Throwable exception, UserAction userAction, String serviceName, String request, @StringRes int errorId) {
		showSnackBarError(Collections.singletonList(exception), userAction, serviceName, request, errorId);
	}
	
	/**
	 * Show a SnackBar and only call ErrorActivity#reportError IF we a find a valid view (otherwise the error screen appears)
	 */
	public void showSnackBarError(List<Throwable> exception, UserAction userAction, String serviceName, String request, @StringRes int errorId) {
		LOGD(TAG, "showSnackBarError() called with: exception = [" + exception + "], userAction = [" + userAction + "], request = [" + request + "], errorId = [" + errorId + "]");
		View rootView = activity != null ? activity.findViewById(android.R.id.content) : null;
		if (rootView == null && getView() != null) rootView = getView();
		if (rootView == null) return;
		// TODO NEED FIX IT
//		ErrorActivity.reportError(getContext(), exception, MainActivity.class, rootView,
//				ErrorActivity.ErrorInfo.make(userAction, serviceName, request, errorId));
	}
}