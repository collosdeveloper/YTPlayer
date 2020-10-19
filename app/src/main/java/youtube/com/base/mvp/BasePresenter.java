package youtube.com.base.mvp;

import android.support.annotation.NonNull;

import com.arellomobile.mvp.MvpPresenter;
import com.arellomobile.mvp.MvpView;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class BasePresenter<View extends MvpView> extends MvpPresenter<View> {
	private CompositeDisposable compositeCompositeDisposable = new CompositeDisposable();
	
	protected void unsubscribeOnDestroy(@NonNull Disposable disposable) {
		compositeCompositeDisposable.add(disposable);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		compositeCompositeDisposable.clear();
	}
}