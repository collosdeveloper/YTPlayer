package youtube.com.base.ui.view;

import android.content.Context;
import android.os.Build;
import android.support.v4.widget.CircularProgressDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ProgressBar;

import youtube.com.R;

public class MaterialProgressBar extends ProgressBar {
	// Same dimensions as medium-sized native Material progress bar
	private static final int RADIUS_DP = 16;
	private static final int WIDTH_DP = 4;
	
	public MaterialProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		// The version range is more or less arbitrary - you might want to modify it
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
				|| Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
			
			final DisplayMetrics metrics = getResources().getDisplayMetrics();
			final float screenDensity = metrics.density;
			
			CircularProgressDrawable drawable = new CircularProgressDrawable(context);
			drawable.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
			drawable.setCenterRadius(RADIUS_DP * screenDensity);
			drawable.setStrokeWidth(WIDTH_DP * screenDensity);
			setIndeterminateDrawable(drawable);
		}
	}
}