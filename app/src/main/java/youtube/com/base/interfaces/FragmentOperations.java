package youtube.com.base.interfaces;

import android.support.v4.app.Fragment;

public interface FragmentOperations {
	
	void switchFragment(Fragment fragment, boolean addToBackStack);
	
	void addFragment(Fragment fragment, boolean addToBackStack);
	
	Fragment getSecondaryFragment();
	
	Fragment findFragment(Class fragmentClass);
	
	void clearStack();
	
	void slideUp();
}