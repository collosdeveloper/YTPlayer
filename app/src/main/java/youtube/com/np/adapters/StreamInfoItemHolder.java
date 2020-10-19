package youtube.com.np.adapters;

import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamType;

import youtube.com.R;
import youtube.com.np.utils.Localization;
import youtube.com.utils.GlideApp;

public class StreamInfoItemHolder extends InfoItemHolder {
	public final ImageView itemThumbnailView;
	public final TextView itemVideoTitleView;
	public final TextView itemDurationView;
	public final TextView itemAdditionalDetails;
	
	public StreamInfoItemHolder(InfoItemBuilder infoItemBuilder, ViewGroup parent) {
		super(infoItemBuilder, R.layout.video_item, parent);
		
		itemAdditionalDetails = itemView.findViewById(R.id.itemAdditionalDetails);
		itemThumbnailView = itemView.findViewById(R.id.itemThumbnailView);
		itemVideoTitleView = itemView.findViewById(R.id.itemVideoTitleView);
		itemDurationView = itemView.findViewById(R.id.itemDurationView);
	}
	
	@Override
	public void updateFromItem(final InfoItem infoItem) {
		if (!(infoItem instanceof StreamInfoItem)) return;
		final StreamInfoItem item = (StreamInfoItem) infoItem;
		
		itemVideoTitleView.setText(item.getName());
		
		if (item.getDuration() > 0) {
			itemDurationView.setText(Localization.getDurationString(item.getDuration()));
			itemDurationView.setBackgroundColor(ContextCompat.getColor(itemBuilder.getContext(),
					R.color.duration_background_color));
			itemDurationView.setVisibility(View.VISIBLE);
		} else if (item.getStreamType() == StreamType.LIVE_STREAM) {
			itemDurationView.setText(R.string.duration_live);
			itemDurationView.setBackgroundColor(ContextCompat.getColor(itemBuilder.getContext(),
					R.color.live_duration_background_color));
			itemDurationView.setVisibility(View.VISIBLE);
		} else {
			itemDurationView.setVisibility(View.GONE);
		}
		
		// Default thumbnail is shown on error, while loading and if the url is empty
		GlideApp.with(itemThumbnailView)
				.load(item.getThumbnailUrl())
				.placeholder(R.drawable.dummy_thumbnail)
				.error(R.drawable.dummy_thumbnail)
				.into(itemThumbnailView);
		
		itemView.setOnClickListener(view -> {
			if (itemBuilder.getOnStreamSelectedListener() != null) {
				itemBuilder.getOnStreamSelectedListener().selected(item);
			}
		});
		
		switch (item.getStreamType()) {
			case AUDIO_STREAM:
			case VIDEO_STREAM:
			case LIVE_STREAM:
			case AUDIO_LIVE_STREAM:
				enableLongClick(item);
				break;
			case FILE:
			case NONE:
			default:
				disableLongClick();
				break;
		}
		
		itemAdditionalDetails.setText(getStreamInfoDetailLine(item));
	}
	
	private String getStreamInfoDetailLine(final StreamInfoItem infoItem) {
		String viewsAndDate = "";
		/** Remove 11.09.18
		if (infoItem.getViewCount() >= 0) {
			viewsAndDate = Localization.shortViewCount(itemBuilder.getContext(), infoItem.getViewCount());
		}*/
		if (!TextUtils.isEmpty(infoItem.getUploadDate())) {
			if (viewsAndDate.isEmpty()) {
				viewsAndDate = infoItem.getUploadDate();
			} else {
				viewsAndDate += " • " + infoItem.getUploadDate();
			}
		}
		return viewsAndDate;
	}
	
	private void enableLongClick(final StreamInfoItem item) {
		itemView.setLongClickable(true);
		itemView.setOnLongClickListener(view -> {
			if (itemBuilder.getOnStreamSelectedListener() != null) {
				itemBuilder.getOnStreamSelectedListener().held(item);
			}
			return true;
		});
	}
	
	private void disableLongClick() {
		itemView.setLongClickable(false);
		itemView.setOnLongClickListener(null);
	}
}