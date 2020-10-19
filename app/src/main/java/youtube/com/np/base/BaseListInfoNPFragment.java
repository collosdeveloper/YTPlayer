package youtube.com.np.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.ListInfo;

import java.util.Queue;

import icepick.State;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static youtube.com.utils.LogUtils.LOGD;

public abstract class BaseListInfoNPFragment<I extends ListInfo>
		extends BaseListNPFragment<I, ListExtractor.InfoItemsPage> {
	private static final String TAG = BaseListInfoNPFragment.class.getSimpleName();
	
	@State
	protected String name;
	@State
	protected String url;
	
	protected I currentInfo;
	protected String currentNextPageUrl;
	protected Disposable currentWorker;
	
	@Override
	protected void initViews(View rootView, Bundle savedInstanceState) {
		super.initViews(rootView, savedInstanceState);
		showListFooter(hasMoreItems());
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (currentWorker != null) currentWorker.dispose();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// Check if it was loading when the fragment was stopped/paused,
		if (wasLoading.getAndSet(false)) {
			if (hasMoreItems() && infoListAdapter.getItemsList().size() > 0) {
				loadMoreItems();
			} else {
				doInitialLoadLogic();
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (currentWorker != null) currentWorker.dispose();
		currentWorker = null;
	}
	
	@Override
	public void writeTo(Queue<Object> objectsToSave) {
		super.writeTo(objectsToSave);
		objectsToSave.add(currentInfo);
		objectsToSave.add(currentNextPageUrl);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void readFrom(@NonNull Queue<Object> savedObjects) throws Exception {
		super.readFrom(savedObjects);
		currentInfo = (I) savedObjects.poll();
		currentNextPageUrl = (String) savedObjects.poll();
	}
	
	// Load and handle
	protected void doInitialLoadLogic() {
		LOGD(TAG, "doInitialLoadLogic() called");
		if (currentInfo == null) {
			startLoading(false);
		} else handleResult(currentInfo);
	}
	
	/**
	 * Implement the logic to load the info from the network.<br/>
	 * You can use the default implementations from ExtractorHelper.
	 *
	 * @param forceLoad allow or disallow the result to come from the cache
	 */
	protected abstract Single<I> loadResult(boolean forceLoad);
	
	@Override
	public void startLoading(boolean forceLoad) {
		super.startLoading(forceLoad);
		showListFooter(false);
		currentInfo = null;
		if (currentWorker != null) currentWorker.dispose();
		currentWorker = loadResult(forceLoad)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe((@NonNull I result) -> {
					isLoading.set(false);
					currentInfo = result;
					currentNextPageUrl = result.getNextPageUrl();
					handleResult(result);
				}, (@NonNull Throwable throwable) -> onError(throwable));
	}
	
	/**
	 * Implement the logic to load more items<br/>
	 * You can use the default implementations from ExtractorHelper
	 */
	protected abstract Single<ListExtractor.InfoItemsPage> loadMoreItemsLogic();
	
	protected void loadMoreItems() {
		isLoading.set(true);
		
		if (currentWorker != null) currentWorker.dispose();
		currentWorker = loadMoreItemsLogic()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe((@io.reactivex.annotations.NonNull ListExtractor.InfoItemsPage InfoItemsPage) -> {
					isLoading.set(false);
					handleNextItems(InfoItemsPage);
				}, (@io.reactivex.annotations.NonNull Throwable throwable) -> {
					isLoading.set(false);
					onError(throwable);
				});
	}
	
	@Override
	public void handleNextItems(ListExtractor.InfoItemsPage result) {
		super.handleNextItems(result);
		currentNextPageUrl = result.getNextPageUrl();
		infoListAdapter.addInfoItemList(result.getItems());
		showListFooter(hasMoreItems());
	}
	
	@Override
	protected boolean hasMoreItems() {
		return !TextUtils.isEmpty(currentNextPageUrl);
	}
	
	@Override
	public void handleResult(@NonNull I result) {
		super.handleResult(result);
		name = result.getName();
		if (infoListAdapter.getItemsList().size() == 0) {
			if (result.getRelatedItems().size() > 0) {
				infoListAdapter.addInfoItemList(result.getRelatedItems());
				showListFooter(hasMoreItems());
			} else {
				infoListAdapter.clearStreamItemList();
				showEmptyState();
			}
		}
	}
	
	protected void setInitialData(String url, String name) {
		this.url = url;
		this.name = !TextUtils.isEmpty(name) ? name : "";
	}
}