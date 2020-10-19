package youtube.com.base.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.MvpAppCompatFragment;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import youtube.com.base.interfaces.BaseFragmentOperations;
import youtube.com.base.interfaces.FragmentOperations;

public abstract class BaseFragment extends MvpAppCompatFragment implements BaseFragmentOperations {
	private FragmentOperations fragmentOperations;
	private Unbinder unbinder;
	
	@TargetApi(23)
	@Override
	public final void onAttach(Context context) {
		AndroidSupportInjection.inject(this);
		super.onAttach(context);
		onAttachToContext(context);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public final void onAttach(Activity activity) {
		super.onAttach(activity);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			onAttachToContext(activity);
		}
	}
	
	protected void onAttachToContext(Context context) {
		fragmentOperations = (FragmentOperations) context;
	}
	
	public void switchFragment(Fragment fragment, boolean addToBackStack) {
		fragmentOperations.switchFragment(fragment, addToBackStack);
	}
	
	public void addFragment(Fragment fragment, boolean addToBackStack) {
		fragmentOperations.addFragment(fragment, addToBackStack);
	}
	
	public void clearStack() {
		fragmentOperations.clearStack();
	}
	
	public void slideUp() {
		fragmentOperations.slideUp();
	}
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(getLayoutResourceId(), container, false);
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		unbinder = ButterKnife.bind(this, view);
		viewCreated(view, savedInstanceState);
	}
	
	@Override
	public void startActivity(Class<?> cls, boolean isNeedFinish) {
		Activity activity = getActivity();
		startActivity(new Intent(activity, cls), isNeedFinish);
	}
	
	@Override
	public void startActivity(Intent intent, boolean isNeedFinish) {
		Activity activity = getActivity();
		startActivity(intent);
		if (isNeedFinish) {
			activity.finish();
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		fragmentOperations = null;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		unbinder.unbind();
	}
	
	public boolean onBackPressed() {
		return true;
	}
}