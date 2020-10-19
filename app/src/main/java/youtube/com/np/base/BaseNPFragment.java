package youtube.com.np.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import icepick.Icepick;
import icepick.State;
import youtube.com.base.ui.BaseFragment;

import static youtube.com.utils.LogUtils.LOGD;

public abstract class BaseNPFragment extends BaseFragment {
	private static final String TAG = BaseNPFragment.class.getSimpleName();
	
	protected AppCompatActivity activity;
	
	// These values are used for controlling fragments when they are part of the front page
	@State
	protected boolean useAsFrontPage = false;
	protected boolean isVisibleToUser = false;
	
	public void useAsFrontPage(boolean value) {
		useAsFrontPage = value;
	}
	
	// ----- Fragment's Lifecycle -----
	
	@Override
	public void onAttachToContext(Context context) {
		super.onAttachToContext(context);
		activity = (AppCompatActivity) context;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		activity = null;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		LOGD(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
		super.onCreate(savedInstanceState);
		Icepick.restoreInstanceState(this, savedInstanceState);
		if (savedInstanceState != null) onRestoreInstanceState(savedInstanceState);
	}
	
	
	@Override
	public void onViewCreated(View rootView, Bundle savedInstanceState) {
		super.onViewCreated(rootView, savedInstanceState);
		LOGD(TAG, "onViewCreated() called with: rootView = [" + rootView + "], savedInstanceState = [" + savedInstanceState + "]");
		initViews(rootView, savedInstanceState);
		initListeners();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Icepick.saveInstanceState(this, outState);
	}
	
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		this.isVisibleToUser = isVisibleToUser;
	}
	
	// ----- Init -----
	
	protected void initViews(View rootView, Bundle savedInstanceState) {
	}
	
	protected void initListeners() {
	}
	
	protected FragmentManager getFM() {
		return getParentFragment() == null
				? getFragmentManager()
				: getParentFragment().getFragmentManager();
	}
}