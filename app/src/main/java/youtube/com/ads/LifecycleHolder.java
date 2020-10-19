package youtube.com.ads;

public interface LifecycleHolder {
	
	boolean isDestroyed();
	
	boolean isVisible();
	
	void onAdFailed(AdType type);
}