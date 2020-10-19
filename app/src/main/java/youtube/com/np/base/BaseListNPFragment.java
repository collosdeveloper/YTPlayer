package youtube.com.np.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.List;
import java.util.Queue;

import youtube.com.R;
import youtube.com.np.adapters.InfoListAdapter;
import youtube.com.np.interfaces.ListViewContract;
import youtube.com.np.utils.OnClickGesture;
import youtube.com.np.utils.OnScrollBelowItemsListener;
import youtube.com.np.utils.StateSaver;

import static youtube.com.np.utils.AnimationUtils.animateView;
import static youtube.com.utils.LogUtils.LOGD;

public abstract class BaseListNPFragment<I, N> extends BaseStateNPFragment<I>
		implements ListViewContract<I, N>, StateSaver.WriteRead {
	private static final String TAG = BaseListNPFragment.class.getSimpleName();
	
	protected InfoListAdapter infoListAdapter;
	protected RecyclerView itemsList;
	
	@Override
	public void onAttachToContext(Context context) {
		super.onAttachToContext(context);
		infoListAdapter = new InfoListAdapter(activity);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		StateSaver.onDestroy(savedState);
	}
	
	protected StateSaver.SavedState savedState;
	
	@Override
	public String generateSuffix() {
		// Naive solution, but it's good for now (the items don't change)
		return "." + infoListAdapter.getItemsList().size() + ".list";
	}
	
	@Override
	public void writeTo(Queue<Object> objectsToSave) {
		objectsToSave.add(infoListAdapter.getItemsList());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void readFrom(@NonNull Queue<Object> savedObjects) throws Exception {
		infoListAdapter.getItemsList().clear();
		infoListAdapter.getItemsList().addAll((List<InfoItem>) savedObjects.poll());
	}
	
	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		savedState = StateSaver.tryToSave(activity.isChangingConfigurations(), savedState, bundle, this);
	}
	
	@Override
	protected void onRestoreInstanceState(@NonNull Bundle bundle) {
		super.onRestoreInstanceState(bundle);
		savedState = StateSaver.tryToRestore(bundle, this);
	}
	
	protected View getListHeader() {
		return activity.getLayoutInflater().inflate(R.layout.rv_videos_header_layout, itemsList, false);
	}
	
	protected View getListFooter() {
		return activity.getLayoutInflater().inflate(R.layout.load_more_footer_layout, itemsList, false);
	}
	
	protected RecyclerView.LayoutManager getListLayoutManager() {
		return new LinearLayoutManager(activity);
	}
	
	@Override
	protected void initViews(View rootView, Bundle savedInstanceState) {
		super.initViews(rootView, savedInstanceState);
		
		itemsList = rootView.findViewById(R.id.video_items_list);
		itemsList.setLayoutManager(getListLayoutManager());
		itemsList.setHasFixedSize(true);
		
		infoListAdapter.setFooter(getListFooter());
		infoListAdapter.setHeader(getListHeader());
		
		itemsList.setAdapter(infoListAdapter);
		itemsList.setItemAnimator(new DefaultItemAnimator());
	}
	
	protected void onItemSelected(InfoItem selectedItem) {
		LOGD(TAG, "onItemSelected() called with: selectedItem = [" + selectedItem + "]");
	}
	
	@Override
	protected void initListeners() {
		super.initListeners();
		infoListAdapter.setOnStreamSelectedListener(new OnClickGesture<StreamInfoItem>() {
			@Override
			public void selected(StreamInfoItem selectedItem) {
				onStreamSelected(selectedItem);
			}
			
			@Override
			public void held(StreamInfoItem selectedItem) {
			}
		});
		itemsList.clearOnScrollListeners();
		itemsList.addOnScrollListener(new OnScrollBelowItemsListener() {
			@Override
			public void onScrolledDown(RecyclerView recyclerView) {
				onScrollToBottom();
			}
		});
	}
	
	protected void onStreamSelected(StreamInfoItem selectedItem) {
		onItemSelected(selectedItem);
	}
	
	protected void onScrollToBottom() {
		if (hasMoreItems() && !isLoading.get()) {
			loadMoreItems();
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		LOGD(TAG, "onCreateOptionsMenu() called with: menu = [" + menu + "], inflater = [" + inflater + "]");
		super.onCreateOptionsMenu(menu, inflater);
		ActionBar supportActionBar = activity.getSupportActionBar();
		if (supportActionBar != null) {
			supportActionBar.setDisplayShowTitleEnabled(true);
			if (useAsFrontPage) {
				supportActionBar.setDisplayHomeAsUpEnabled(false);
			} else {
				supportActionBar.setDisplayHomeAsUpEnabled(true);
			}
		}
	}
	
	protected abstract void loadMoreItems();
	
	protected abstract boolean hasMoreItems();
	
	@Override
	public void hideLoading() {
		super.hideLoading();
		animateView(itemsList, true, 300);
	}
	
	@Override
	public void showError(String message, boolean showRetryButton) {
		super.showError(message, showRetryButton);
		showListFooter(false);
		animateView(itemsList, false, 200);
	}
	
	@Override
	public void showEmptyState() {
		super.showEmptyState();
		showListFooter(false);
	}
	
	@Override
	public void showListFooter(final boolean show) {
		itemsList.post(() -> {
			if (infoListAdapter != null && itemsList != null) {
				infoListAdapter.showFooter(show);
			}
		});
	}
	
	@Override
	public void handleNextItems(N result) {
		isLoading.set(false);
	}
}