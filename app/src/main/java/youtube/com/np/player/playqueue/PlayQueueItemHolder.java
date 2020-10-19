package youtube.com.np.player.playqueue;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import youtube.com.R;

public class PlayQueueItemHolder extends RecyclerView.ViewHolder {
	public final TextView itemVideoTitleView, itemDurationView;
	public final ImageView itemSelected, itemThumbnailView, itemHandle;
	
	public final View itemRoot;
	
	public PlayQueueItemHolder(View v) {
		super(v);
		itemRoot = v.findViewById(R.id.itemRoot);
		itemVideoTitleView = v.findViewById(R.id.itemVideoTitleView);
		itemDurationView = v.findViewById(R.id.itemDurationView);
		itemSelected = v.findViewById(R.id.itemSelected);
		itemThumbnailView = v.findViewById(R.id.itemThumbnailView);
		itemHandle = v.findViewById(R.id.itemHandle);
	}
}