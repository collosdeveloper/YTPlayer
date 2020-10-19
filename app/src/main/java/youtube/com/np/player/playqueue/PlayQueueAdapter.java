package youtube.com.np.player.playqueue;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import youtube.com.R;
import youtube.com.np.player.playqueue.events.AppendEvent;
import youtube.com.np.player.playqueue.events.ErrorEvent;
import youtube.com.np.player.playqueue.events.MoveEvent;
import youtube.com.np.player.playqueue.events.PlayQueueEvent;
import youtube.com.np.player.playqueue.events.RemoveEvent;
import youtube.com.np.player.playqueue.events.SelectEvent;
import youtube.com.np.utils.FallbackViewHolder;

import static youtube.com.utils.LogUtils.LOGE;

public class PlayQueueAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final String TAG = PlayQueueAdapter.class.toString();
	
	private static final int ITEM_VIEW_TYPE_ID = 0;
	private static final int FOOTER_VIEW_TYPE_ID = 1;
	
	private final PlayQueueItemBuilder playQueueItemBuilder;
	private final PlayQueue playQueue;
	private boolean showFooter = false;
	private View footer = null;
	
	private Disposable playQueueReactor;
	
	public class HFHolder extends RecyclerView.ViewHolder {
		public HFHolder(View v) {
			super(v);
			view = v;
		}
		public View view;
	}
	
	public PlayQueueAdapter(final Context context, final PlayQueue playQueue) {
		if (playQueue.getBroadcastReceiver() == null) {
			throw new IllegalStateException("Play Queue has not been initialized.");
		}
		
		this.playQueueItemBuilder = new PlayQueueItemBuilder(context);
		this.playQueue = playQueue;
		
		playQueue.getBroadcastReceiver().toObservable().subscribe(getReactor());
	}
	
	private Observer<PlayQueueEvent> getReactor() {
		return new Observer<PlayQueueEvent>() {
			@Override
			public void onSubscribe(@NonNull Disposable d) {
				if (playQueueReactor != null) playQueueReactor.dispose();
				playQueueReactor = d;
			}
			
			@Override
			public void onNext(@NonNull PlayQueueEvent playQueueMessage) {
				if (playQueueReactor != null) onPlayQueueChanged(playQueueMessage);
			}
			
			@Override
			public void onError(@NonNull Throwable e) {}
			
			@Override
			public void onComplete() {
				dispose();
			}
		};
		
	}
	
	private void onPlayQueueChanged(final PlayQueueEvent message) {
		switch (message.type()) {
			case RECOVERY:
				// Do nothing.
				break;
			case SELECT:
				final SelectEvent selectEvent = (SelectEvent) message;
				notifyItemChanged(selectEvent.getOldIndex());
				notifyItemChanged(selectEvent.getNewIndex());
				break;
			case APPEND:
				final AppendEvent appendEvent = (AppendEvent) message;
				notifyItemRangeInserted(playQueue.size(), appendEvent.getAmount());
				break;
			case ERROR:
				final ErrorEvent errorEvent = (ErrorEvent) message;
				if (!errorEvent.isSkippable()) {
					notifyItemRemoved(errorEvent.getErrorIndex());
				}
				notifyItemChanged(errorEvent.getErrorIndex());
				notifyItemChanged(errorEvent.getQueueIndex());
				break;
			case REMOVE:
				final RemoveEvent removeEvent = (RemoveEvent) message;
				notifyItemRemoved(removeEvent.getRemoveIndex());
				notifyItemChanged(removeEvent.getQueueIndex());
				break;
			case MOVE:
				final MoveEvent moveEvent = (MoveEvent) message;
				notifyItemMoved(moveEvent.getFromIndex(), moveEvent.getToIndex());
				break;
			case INIT:
			case REORDER:
			default:
				notifyDataSetChanged();
				break;
		}
	}
	
	public void dispose() {
		if (playQueueReactor != null) playQueueReactor.dispose();
		playQueueReactor = null;
	}
	
	public void setSelectedListener(final PlayQueueItemBuilder.OnSelectedListener listener) {
		playQueueItemBuilder.setOnSelectedListener(listener);
	}
	
	public void unsetSelectedListener() {
		playQueueItemBuilder.setOnSelectedListener(null);
	}
	
	public void setFooter(View footer) {
		this.footer = footer;
		notifyItemChanged(playQueue.size());
	}
	
	public void showFooter(final boolean show) {
		showFooter = show;
		notifyItemChanged(playQueue.size());
	}
	
	public List<PlayQueueItem> getItems() {
		return playQueue.getStreams();
	}
	
	@Override
	public int getItemCount() {
		int count = playQueue.getStreams().size();
		if(footer != null && showFooter) count++;
		return count;
	}
	
	@Override
	public int getItemViewType(int position) {
		if(footer != null && position == playQueue.getStreams().size() && showFooter) {
			return FOOTER_VIEW_TYPE_ID;
		}
		
		return ITEM_VIEW_TYPE_ID;
	}
	
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
		switch(type) {
			case FOOTER_VIEW_TYPE_ID:
				return new HFHolder(footer);
			case ITEM_VIEW_TYPE_ID:
				return new PlayQueueItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.play_queue_item, parent, false));
			default:
				LOGE(TAG, "Attempting to create view holder with undefined type: " + type);
				return new FallbackViewHolder(new View(parent.getContext()));
		}
	}
	
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if(holder instanceof PlayQueueItemHolder) {
			final PlayQueueItemHolder itemHolder = (PlayQueueItemHolder) holder;
			
			// Build the list item
			playQueueItemBuilder.buildStreamInfoItem(itemHolder, playQueue.getStreams().get(position));
			
			// Check if the current item should be selected/highlighted
			final boolean isSelected = playQueue.getIndex() == position;
			itemHolder.itemSelected.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
			itemHolder.itemView.setSelected(isSelected);
		} else if(holder instanceof HFHolder && position == playQueue.getStreams().size() && footer != null && showFooter) {
			((HFHolder) holder).view = footer;
		}
	}
}