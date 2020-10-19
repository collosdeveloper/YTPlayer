package youtube.com.screens.main.about;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arellomobile.mvp.presenter.InjectPresenter;

import org.schabi.newpipe.extractor.channel.ChannelInfo;

import javax.inject.Inject;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import youtube.com.BuildConfig;
import youtube.com.R;
import youtube.com.base.ui.BaseFragment;
import youtube.com.np.utils.Localization;
import youtube.com.repo.ChannelRepo;
import youtube.com.utils.GlideApp;
import youtube.com.utils.TextUtils;

public class AboutChannelFragment extends BaseFragment implements AboutChannelFragmentView {
	private static final String TAG = AboutChannelFragment.class.getSimpleName();
	
	@BindView(R.id.channel_banner_image)
	ImageView channelBanner;
	@BindView(R.id.channel_avatar_view)
	CircleImageView channelIco;
	@BindView(R.id.channel_title_view)
	TextView channelTitle;
	@BindView(R.id.channel_subscriber_view)
	TextView channelSubscriber;
	@BindView(R.id.channel_description_subtitle_view)
	TextView channelDescription;
	@BindView(R.id.facebook_link)
	TextView fbTxtLink;
	@BindView(R.id.twitter_link)
	TextView twitterTxtLink;
	
	@Inject
	ChannelRepo channelRepo;
	@InjectPresenter
	AboutChannelFragmentPresenter aboutChannelFragmentPresenter;
	
	@Override
	public int getLayoutResourceId() {
		return R.layout.fragment_channel_about;
	}
	
	@Override
	public void viewCreated(View view, @Nullable Bundle savedInstanceState) {
		channelRepo.getLiveData().observe(this,
				channelInfo -> setChannelData(channelInfo));
	}
	
	@Override
	public void attachView() {
		slideUp();
	}
	
	@Override
	public void setChannelData(ChannelInfo result) {
		channelTitle.setText(result.getName());
		
		GlideApp.with(channelBanner)
				.load(result.getBannerUrl())
				.placeholder(R.drawable.channel_banner)
				.error(R.drawable.channel_banner)
				.into(channelBanner);
		
		GlideApp.with(channelIco)
				.load(result.getAvatarUrl())
				.placeholder(R.drawable.buddy)
				.error(R.drawable.buddy)
				.into(channelIco);
		
		if (result.getSubscriberCount() != -1) {
			channelSubscriber.setText(Localization.localizeSubscribersCount(channelSubscriber.getContext(), result.getSubscriberCount()));
			channelSubscriber.setVisibility(View.VISIBLE);
		} else {
			channelSubscriber.setVisibility(View.GONE);
		}
		
		if (!TextUtils.isEmpty(result.getDescription())) {
			channelDescription.setText(result.getDescription());
		}
		
		String fbLink = BuildConfig.facebook_link;
		String twitterLink = BuildConfig.twitter_link;
		
		if (!TextUtils.isEmpty(fbLink)) {
			fbTxtLink.setText(Html.fromHtml(fbLink));
			fbTxtLink.setMovementMethod(LinkMovementMethod.getInstance());
		}
		
		if (!TextUtils.isEmpty(twitterLink)) {
			twitterTxtLink.setText(Html.fromHtml(twitterLink));
			twitterTxtLink.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}
}