package youtube.com.base.interfaces;

import android.content.Intent;
import android.os.Bundle;

public interface BaseActivityOperations {
	int getLayoutResourceId();
	
	boolean isHasFragment();
	
	void initUI(Bundle savedInstanceState);
	
	void startActivity(Class<?> cls, boolean isNeedFinish);
	
	void startActivity(Intent intent, boolean isNeedFinish);
}