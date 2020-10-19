package youtube.com.base.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.arellomobile.mvp.MvpAppCompatActivity;

import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import youtube.com.R;
import youtube.com.base.interfaces.BackPressable;
import youtube.com.base.interfaces.BaseActivityOperations;
import youtube.com.base.interfaces.FragmentOperations;

public abstract class BaseActivity extends MvpAppCompatActivity implements BaseActivityOperations,
		FragmentOperations {
	private static final String TAG = BaseActivity.class.getSimpleName();
	
	private FragmentManager fragmentManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		AndroidInjection.inject(this);
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResourceId());
		ButterKnife.bind(this);
		initUI(savedInstanceState);
		if (isHasFragment()) {
			fragmentManager = getSupportFragmentManager();
		}
	}
	
	@Override
	public void switchFragment(Fragment fragment, boolean addToBackStack) {
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.container, fragment, fragment.getClass().getSimpleName());
		if (addToBackStack) {
			transaction.addToBackStack(fragment.getClass().getSimpleName());
		}
		transaction.commit();
	}
	
	@Override
	public void addFragment(Fragment fragment, boolean addToBackStack) {
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.add(R.id.container, fragment, fragment.getClass().getSimpleName());
		if (addToBackStack) {
			transaction.addToBackStack(fragment.getClass().getSimpleName());
		}
		transaction.commit();
	}
	
	@Override
	public Fragment getSecondaryFragment() {
		return fragmentManager.findFragmentById(R.id.container);
	}
	
	@Override
	public void onBackPressed() {
		Fragment currentFragment = getSecondaryFragment();
		if (currentFragment != null && currentFragment instanceof BackPressable) {
			if (((BackPressable) currentFragment).onBackPressed()) {
				super.onBackPressed();
			}
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	public Fragment findFragment(Class fragmentClass) {
		return fragmentManager.findFragmentByTag(fragmentClass.getSimpleName());
	}
	
	@Override
	public void clearStack() {
		fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}
	
	@Override
	public void startActivity(Class<?> cls, boolean isNeedFinish) {
		startActivity(new Intent(this, cls), isNeedFinish);
	}
	
	@Override
	public void startActivity(Intent intent, boolean isNeedFinish) {
		startActivity(intent);
		if (isNeedFinish) {
			finish();
		}
	}
}