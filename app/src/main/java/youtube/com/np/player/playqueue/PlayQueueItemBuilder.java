package youtube.com.np.player.playqueue;

import android.content.Context;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import youtube.com.R;
import youtube.com.np.utils.Localization;
import youtube.com.utils.GlideApp;

public class PlayQueueItemBuilder {
	private static final String TAG = PlayQueueItemBuilder.class.toString();
	
	public interface OnSelectedListener {
		void selected(PlayQueueItem item, View view);
		void held(PlayQueueItem item, View view);
		void onStartDrag(PlayQueueItemHolder viewHolder);
	}
	
	private OnSelectedListener onItemClickListener;
	
	public PlayQueueItemBuilder(final Context context) {}
	
	public void setOnSelectedListener(OnSelectedListener listener) {
		this.onItemClickListener = listener;
	}
	
	public void buildStreamInfoItem(final PlayQueueItemHolder holder, final PlayQueueItem item) {
		if (!TextUtils.isEmpty(item.getTitle())) holder.itemVideoTitleView.setText(item.getTitle());
		/** Remove 11.09.18
		holder.itemAdditionalDetailsView.setText(Localization.concatenateStrings(item.getUploader(),
				NewPipe.getNameOfService(item.getServiceId()))); */
		
		if (item.getDuration() > 0) {
			holder.itemDurationView.setText(Localization.getDurationString(item.getDuration()));
		} else {
			holder.itemDurationView.setVisibility(View.GONE);
		}
		
		GlideApp.with(holder.itemThumbnailView)
				.load(item.getThumbnailUrl())
				.placeholder(R.drawable.dummy_thumbnail)
				.error(R.drawable.dummy_thumbnail)
				.into(holder.itemThumbnailView);
		
		holder.itemRoot.setOnClickListener(view -> {
			if (onItemClickListener != null) {
				onItemClickListener.selected(item, view);
			}
		});
		
		holder.itemRoot.setOnLongClickListener(view -> {
			if (onItemClickListener != null) {
				onItemClickListener.held(item, view);
				return true;
			}
			return false;
		});
		
		holder.itemThumbnailView.setOnTouchListener(getOnTouchListener(holder));
		holder.itemHandle.setOnTouchListener(getOnTouchListener(holder));
	}
	
	private View.OnTouchListener getOnTouchListener(final PlayQueueItemHolder holder) {
		return (view, motionEvent) -> {
			view.performClick();
			if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN
					&& onItemClickListener != null) {
				onItemClickListener.onStartDrag(holder);
			}
			return false;
		};
	}
}