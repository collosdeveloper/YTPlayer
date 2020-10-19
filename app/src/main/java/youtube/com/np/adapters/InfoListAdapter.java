package youtube.com.np.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.ArrayList;
import java.util.List;

import youtube.com.np.utils.FallbackViewHolder;
import youtube.com.np.utils.OnClickGesture;

import static youtube.com.utils.LogUtils.LOGD;

public class InfoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final String TAG = InfoListAdapter.class.getSimpleName();
	
	private static final int HEADER_TYPE = 0;
	private static final int FOOTER_TYPE = 1;
	
	private static final int MINI_STREAM_HOLDER_TYPE = 0x100;
	private static final int STREAM_HOLDER_TYPE = 0x101;
	private static final int MINI_CHANNEL_HOLDER_TYPE = 0x200;
	private static final int CHANNEL_HOLDER_TYPE = 0x201;
	private static final int MINI_PLAYLIST_HOLDER_TYPE = 0x300;
	private static final int PLAYLIST_HOLDER_TYPE = 0x301;
	
	private final InfoItemBuilder infoItemBuilder;
	private final ArrayList<InfoItem> infoItemList;
	private boolean useMiniVariant = false;
	private boolean showFooter = false;
	private View header = null;
	private View footer = null;
	
	public class HFHolder extends RecyclerView.ViewHolder {
		public View view;
		
		public HFHolder(View v) {
			super(v);
			view = v;
		}
	}
	
	public InfoListAdapter(Activity a) {
		infoItemBuilder = new InfoItemBuilder(a);
		infoItemList = new ArrayList<>();
	}
	
	public void setOnStreamSelectedListener(OnClickGesture<StreamInfoItem> listener) {
		infoItemBuilder.setOnStreamSelectedListener(listener);
	}
	
	public void useMiniItemVariants(boolean useMiniVariant) {
		this.useMiniVariant = useMiniVariant;
	}
	
	public void addInfoItemList(List<InfoItem> data) {
		if (data != null) {
			LOGD(TAG, "addInfoItemList() before > infoItemList.size() = " + infoItemList.size() + ", data.size() = " + data.size());
			
			int offsetStart = sizeConsideringHeaderOffset();
			infoItemList.addAll(data);
			
			LOGD(TAG, "addInfoItemList() after > offsetStart = " + offsetStart + ", infoItemList.size() = " + infoItemList.size() + ", header = " + header + ", footer = " + footer + ", showFooter = " + showFooter);
			
			notifyItemRangeInserted(offsetStart, data.size());
			
			if (footer != null && showFooter) {
				int footerNow = sizeConsideringHeaderOffset();
				notifyItemMoved(offsetStart, footerNow);
				
				LOGD(TAG, "addInfoItemList() footer from " + offsetStart + " to " + footerNow);
			}
		}
	}
	
	public void addInfoItem(InfoItem data) {
		if (data != null) {
			LOGD(TAG, "addInfoItem() before > infoItemList.size() = " + infoItemList.size() + ", thread = " + Thread.currentThread());
			
			int positionInserted = sizeConsideringHeaderOffset();
			infoItemList.add(data);
			
			LOGD(TAG, "addInfoItem() after > position = " + positionInserted + ", infoItemList.size() = " + infoItemList.size() + ", header = " + header + ", footer = " + footer + ", showFooter = " + showFooter);
			
			notifyItemInserted(positionInserted);
			
			if (footer != null && showFooter) {
				int footerNow = sizeConsideringHeaderOffset();
				notifyItemMoved(positionInserted, footerNow);
				
				LOGD(TAG, "addInfoItem() footer from " + positionInserted + " to " + footerNow);
			}
		}
	}
	
	public void clearStreamItemList() {
		if (infoItemList.isEmpty()) {
			return;
		}
		
		infoItemList.clear();
		notifyDataSetChanged();
	}
	
	public void setHeader(View header) {
		boolean changed = header != this.header;
		this.header = header;
		if (changed) notifyDataSetChanged();
	}
	
	public void setFooter(View view) {
		this.footer = view;
	}
	
	public void showFooter(boolean show) {
		LOGD(TAG, "showFooter() called with: show = [" + show + "]");
		if (show == showFooter) return;
		
		showFooter = show;
		if (show) notifyItemInserted(sizeConsideringHeaderOffset());
		else notifyItemRemoved(sizeConsideringHeaderOffset());
	}
	
	
	private int sizeConsideringHeaderOffset() {
		int i = infoItemList.size() + (header != null ? 1 : 0);
		LOGD(TAG, "sizeConsideringHeaderOffset() called → " + i);
		return i;
	}
	
	public ArrayList<InfoItem> getItemsList() {
		return infoItemList;
	}
	
	@Override
	public int getItemCount() {
		int count = infoItemList.size();
		if (header != null) count++;
		if (footer != null && showFooter) count++;
		
		LOGD(TAG, "getItemCount() called, count = " + count + ", infoItemList.size() = " + infoItemList.size() + ", header = " + header + ", footer = " + footer + ", showFooter = " + showFooter);
		
		return count;
	}
	
	@Override
	public int getItemViewType(int position) {
		LOGD(TAG, "getItemViewType() called with: position = [" + position + "]");
		
		if (header != null && position == 0) {
			return HEADER_TYPE;
		} else if (header != null) {
			position--;
		}
		if (footer != null && position == infoItemList.size() && showFooter) {
			return FOOTER_TYPE;
		}
		final InfoItem item = infoItemList.get(position);
		switch (item.getInfoType()) {
			case STREAM:
				return useMiniVariant ? MINI_STREAM_HOLDER_TYPE : STREAM_HOLDER_TYPE;
			case CHANNEL:
				return useMiniVariant ? MINI_CHANNEL_HOLDER_TYPE : CHANNEL_HOLDER_TYPE;
			case PLAYLIST:
				return useMiniVariant ? MINI_PLAYLIST_HOLDER_TYPE : PLAYLIST_HOLDER_TYPE;
			default:
				return -1;
		}
	}
	
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
		LOGD(TAG, "onCreateViewHolder() called with: parent = [" + parent + "], type = [" + type + "]");
		switch (type) {
			case HEADER_TYPE:
				return new HFHolder(header);
			case FOOTER_TYPE:
				return new HFHolder(footer);
			case STREAM_HOLDER_TYPE:
				return new StreamInfoItemHolder(infoItemBuilder, parent);
			default:
				return new FallbackViewHolder(new View(parent.getContext()));
		}
	}
	
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		LOGD(TAG, "onBindViewHolder() called with: holder = [" + holder.getClass().getSimpleName() + "], position = [" + position + "]");
		if (holder instanceof InfoItemHolder) {
			// If header isn't null, offset the items by -1
			if (header != null) position--;
			
			((InfoItemHolder) holder).updateFromItem(infoItemList.get(position));
		} else if (holder instanceof HFHolder && position == 0 && header != null) {
			((HFHolder) holder).view = header;
		} else if (holder instanceof HFHolder && position == sizeConsideringHeaderOffset() && footer != null && showFooter) {
			((HFHolder) holder).view = footer;
		}
	}
}