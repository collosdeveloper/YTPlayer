package youtube.com.np.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.schabi.newpipe.extractor.InfoItem;

public abstract class InfoItemHolder extends RecyclerView.ViewHolder {
	protected final InfoItemBuilder itemBuilder;
	
	public InfoItemHolder(InfoItemBuilder infoItemBuilder, int layoutId, ViewGroup parent) {
		super(LayoutInflater.from(infoItemBuilder.getContext()).inflate(layoutId, parent, false));
		this.itemBuilder = infoItemBuilder;
	}
	
	public abstract void updateFromItem(final InfoItem infoItem);
}