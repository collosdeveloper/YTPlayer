package youtube.com.screens.error;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;
import youtube.com.R;

public final class YTErrorActivity extends AppCompatActivity {
	
	@SuppressLint("PrivateResource")
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_yt_error);
		
		//Close/restart button logic:
		//If a class if set, use restart.
		//Else, use close and just finish the app.
		//It is recommended that you follow this logic if implementing a custom error activity.
		Button restartButton = findViewById(R.id.customactivityoncrash_error_activity_restart_button);
		
		final CaocConfig config = CustomActivityOnCrash.getConfigFromIntent(getIntent());
		
		if (config == null) {
			//This should never happen - Just finish the activity to avoid a recursive crash.
			finish();
			return;
		}
		
		if (config.isShowRestartButton() && config.getRestartActivityClass() != null) {
			restartButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CustomActivityOnCrash.restartApplication(YTErrorActivity.this, config);
				}
			});
		} else {
			restartButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CustomActivityOnCrash.closeApplication(YTErrorActivity.this, config);
				}
			});
		}
		
		Button moreInfoButton = findViewById(R.id.customactivityoncrash_error_activity_more_info_button);
		
		if (config.isShowErrorDetails()) {
			moreInfoButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//We retrieve all the error data and show it
					
					AlertDialog dialog = new AlertDialog.Builder(YTErrorActivity.this)
							.setTitle(R.string.customactivityoncrash_error_activity_error_details_title)
							.setMessage(CustomActivityOnCrash.getAllErrorDetailsFromIntent(YTErrorActivity.this, getIntent()))
							.setPositiveButton(R.string.customactivityoncrash_error_activity_error_details_close, null)
							.setNeutralButton(R.string.customactivityoncrash_error_activity_error_details_copy,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											copyErrorToClipboard();
										}
									})
							.show();
					TextView textView = dialog.findViewById(android.R.id.message);
					if (textView != null) {
						textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.customactivityoncrash_error_activity_error_details_text_size));
					}
				}
			});
		} else {
			moreInfoButton.setVisibility(View.GONE);
		}
		
		Integer defaultErrorActivityDrawableId = config.getErrorDrawable();
		ImageView errorImageView = findViewById(R.id.customactivityoncrash_error_activity_image);
		
		if (defaultErrorActivityDrawableId != null) {
			errorImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), defaultErrorActivityDrawableId, getTheme()));
		}
	}
	
	private void copyErrorToClipboard() {
		String errorInformation = CustomActivityOnCrash.getAllErrorDetailsFromIntent(YTErrorActivity.this, getIntent());
		
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		
		//Are there any devices without clipboard...?
		if (clipboard != null) {
			ClipData clip = ClipData.newPlainText(getString(R.string.customactivityoncrash_error_activity_error_details_clipboard_label), errorInformation);
			clipboard.setPrimaryClip(clip);
			Toast.makeText(YTErrorActivity.this, R.string.customactivityoncrash_error_activity_error_details_copied, Toast.LENGTH_SHORT).show();
		}
	}
}