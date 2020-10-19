package youtube.com.np.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import youtube.com.np.utils.OnClickGesture;

public class InfoItemBuilder {
	private static final String TAG = InfoItemBuilder.class.toString();
	
	private final Context context;
	
	private OnClickGesture<StreamInfoItem> onStreamSelectedListener;
	
	public InfoItemBuilder(Context context) {
		this.context = context;
	}
	
	public View buildView(@NonNull ViewGroup parent, @NonNull final InfoItem infoItem) {
		return buildView(parent, infoItem, false);
	}
	
	public View buildView(@NonNull ViewGroup parent, @NonNull final InfoItem infoItem, boolean useMiniVariant) {
		InfoItemHolder holder = holderFromInfoType(parent, infoItem.getInfoType());
		holder.updateFromItem(infoItem);
		return holder.itemView;
	}
	
	private InfoItemHolder holderFromInfoType(@NonNull ViewGroup parent, @NonNull InfoItem.InfoType infoType) {
		switch (infoType) {
			case STREAM:
				return new StreamInfoItemHolder(this, parent);
			default:
				throw new RuntimeException("InfoType not expected = " + infoType.name());
		}
	}
	
	public Context getContext() {
		return context;
	}
	
	public OnClickGesture<StreamInfoItem> getOnStreamSelectedListener() {
		return onStreamSelectedListener;
	}
	
	public void setOnStreamSelectedListener(OnClickGesture<StreamInfoItem> listener) {
		this.onStreamSelectedListener = listener;
	}
}