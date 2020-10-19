package youtube.com.np.interfaces;

public interface ListViewContract<I, N> extends ViewContract<I> {
	void showListFooter(boolean show);
	
	void handleNextItems(N result);
}