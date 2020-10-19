package youtube.com.base.interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

public interface BaseFragmentOperations {
	int getLayoutResourceId();
	
	void viewCreated(View view, @Nullable Bundle savedInstanceState);
	
	void startActivity(Class<?> cls, boolean isNeedFinish);
	
	void startActivity(Intent intent, boolean isNeedFinish);
}