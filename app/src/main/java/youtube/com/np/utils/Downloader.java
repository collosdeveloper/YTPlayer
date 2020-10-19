package youtube.com.np.utils;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Downloader implements org.schabi.newpipe.extractor.Downloader {
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";
	
	private static Downloader instance;
	private String cookies;
	private OkHttpClient client;
	
	private Downloader(OkHttpClient.Builder builder) {
		this.client = builder
				.readTimeout(30, TimeUnit.SECONDS)
				.build();
	}
	
	/**
	 * It's recommended to call exactly once in the entire lifetime of the application.
	 *
	 * @param builder if null, default builder will be used
	 */
	public static Downloader init(@Nullable OkHttpClient.Builder builder) {
		return instance = new Downloader(builder != null ? builder : new OkHttpClient.Builder());
	}
	
	public static Downloader getInstance() {
		return instance;
	}
	
	public String getCookies() {
		return this.cookies;
	}
	
	public void setCookies(String cookies) {
		this.cookies = cookies;
	}
	
	/**
	 * Get the size of the content that the url is pointing by firing a HEAD request.
	 *
	 * @param url an url pointing to the content
	 * @return the size of the content, in bytes
	 */
	public long getContentLength(String url) throws IOException {
		Response response = null;
		try {
			final Request request = new Request.Builder()
					.head().url(url)
					.addHeader("User-Agent", USER_AGENT)
					.build();
			response = client.newCall(request).execute();
			
			return Long.parseLong(response.header("Content-Length"));
		} catch (NumberFormatException e) {
			throw new IOException("Invalid content length", e);
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}
	
	/**
	 * Download the text file at the supplied URL as in download(String),
	 * but set the HTTP header field "Accept-Language" to the supplied string.
	 *
	 * @param siteUrl  the URL of the text file to return the contents of
	 * @param language the language (usually a 2-character code) to set as the preferred language
	 * @return the contents of the specified text file
	 */
	@Override
	public String download(String siteUrl, String language) throws IOException, ReCaptchaException {
		Map<String, String> requestProperties = new HashMap<>();
		requestProperties.put("Accept-Language", language);
		return download(siteUrl, requestProperties);
	}
	
	/**
	 * Download the text file at the supplied URL as in download(String),
	 * but set the HTTP headers included in the customProperties map.
	 *
	 * @param siteUrl          the URL of the text file to return the contents of
	 * @param customProperties set request header properties
	 * @return the contents of the specified text file
	 * @throws IOException
	 */
	@Override
	public String download(String siteUrl, Map<String, String> customProperties) throws IOException, ReCaptchaException {
		return getBody(siteUrl, customProperties).string();
	}
	
	public InputStream stream(String siteUrl) throws IOException {
		try {
			return getBody(siteUrl, Collections.emptyMap()).byteStream();
		} catch (ReCaptchaException e) {
			throw new IOException(e.getMessage(), e.getCause());
		}
	}
	
	private ResponseBody getBody(String siteUrl, Map<String, String> customProperties) throws IOException, ReCaptchaException {
		final Request.Builder requestBuilder = new Request.Builder()
				.method("GET", null).url(siteUrl)
				.addHeader("User-Agent", USER_AGENT);
		
		for (Map.Entry<String, String> header : customProperties.entrySet()) {
			requestBuilder.addHeader(header.getKey(), header.getValue());
		}
		
		if (!TextUtils.isEmpty(this.cookies)) {
			requestBuilder.addHeader("Cookie", this.cookies);
		}
		
		final Request request = requestBuilder.build();
		final Response response = client.newCall(request).execute();
		final ResponseBody body = response.body();
		
		if (response.code() == 429) {
			throw new ReCaptchaException("reCaptcha Challenge requested");
		}
		
		if (body == null) {
			response.close();
			return null;
		}
		
		return body;
	}
	
	/**
	 * Download (via HTTP) the text file located at the supplied URL, and return its contents.
	 * Primarily intended for downloading web pages.
	 *
	 * @param siteUrl the URL of the text file to download
	 * @return the contents of the specified text file
	 */
	@Override
	public String download(String siteUrl) throws IOException, ReCaptchaException {
		return download(siteUrl, Collections.emptyMap());
	}
}