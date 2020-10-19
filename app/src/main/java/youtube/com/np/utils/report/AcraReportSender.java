package youtube.com.np.utils.report;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import youtube.com.R;
import youtube.com.np.activities.ErrorActivity;
import youtube.com.np.utils.UserAction;

public class AcraReportSender implements ReportSender {
	
	@Override
	public void send(@NonNull Context context, @NonNull CrashReportData report) throws ReportSenderException {
		ErrorActivity.reportError(context, report,
				ErrorActivity.ErrorInfo.make(UserAction.UI_ERROR,"none",
						"App crash, UI failure", R.string.app_ui_crash));
	}
}