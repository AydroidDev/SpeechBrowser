package cn.hukecn.speechbrowser.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.GeolocationPermissions.Callback;
import android.widget.ProgressBar;

public class CutWebView extends WebView{

	String cookieStr = "";
	Context context = null;
	String instantUrl = "";
	CutWebCallback listener = null;
	ReceiveHtmlListener htmlListener = null;
	ReceiveMessageListener messageListener = null;
	Handler handler = new Handler(){
		public void handleMessage(Message msg) 
		{
			switch (msg.what) {
			case 0:
				if(htmlListener != null)
					htmlListener.onReceiveHTML(instantUrl,(String)msg.obj);
				break;
			case 1:
				if(messageListener != null)
					messageListener.onReceiveMessage(msg.arg1);
				break;
			}
		};
	};
	private ProgressBar progressbar;
	@TargetApi(23)
	@SuppressLint("SetJavaScriptEnabled")
	public CutWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
        progressbar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressbar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,0,0));
        addView(progressbar);
        setWebChromeClient(new WebChromeClient());
        setWebViewClient(new WebViewCLient());
        WebSettings settings = getSettings();
        settings.setAllowFileAccess(true);
//        settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
        settings.setUserAgentString("Mozilla/5.0 (Linux; U; Android 5.1.1; zh-cn; PLK-UL00 Build/HONORPLK-UL00) AppleWebKit/537.36 (KHTML, like Gecko)Version/4.0 MQQBrowser/5.3 Mobile Safari/537.36");
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setAppCacheEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setAppCacheMaxSize(1024*1024*5);
        String appCachePath = context.getApplicationContext().getCacheDir().getAbsolutePath();
        settings.setAppCachePath(appCachePath);
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setGeolocationEnabled(true); 
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSaveFormData(true);
        settings.setSupportMultipleWindows(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDefaultTextEncodingName("utf-8");
        String dir = context.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
//      //���õ���λ
      //���ö�λ�����ݿ�·��
        settings.setGeolocationDatabasePath(dir);
        
        addJavascriptInterface(new JSLinster(),"HTML");
//        addJavascriptInterface(this, "call");
	}
	
	public void setCutWebViewCallback(CutWebCallback listener){
		this.listener = listener;
	}
	
	public void setReceiveHtmlListener(ReceiveHtmlListener listener)
	{
		this.htmlListener = listener;
	}
	
	public void setReceiveMessageListener(ReceiveMessageListener listener){
		this.messageListener = listener;
	}

	 public class WebChromeClient extends android.webkit.WebChromeClient {
	        @Override
	        public void onProgressChanged(WebView view, int newProgress) {
	            if (newProgress == 100) {
	                progressbar.setVisibility(GONE);
	                if (listener != null) {
						listener.onPageFinished(getUrl());
					}
	            } else {
	                if (progressbar.getVisibility() == GONE)
	                    progressbar.setVisibility(VISIBLE);
	                progressbar.setProgress(newProgress);
	            }
	            super.onProgressChanged(view, newProgress);
	        }
	        
	        @Override
	        public void onReceivedTitle(WebView view, String title) {
	        // TODO Auto-generated method stub
	        	if(listener != null)
	        		listener.onReceiveTitle(title);
//	        	super.onReceivedTitle(view, title);
	        }
	        
	        @Override
	        public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
	        // TODO Auto-generated method stub
	        	callback.invoke(origin, true, false);
	        	super.onGeolocationPermissionsShowPrompt(origin, callback);
	        }

	    }
	 
	 public class WebViewCLient extends WebViewClient{
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        	
	            if(listener != null)
	            	listener.onShouldOverrideUrl(url);
//	   		 if(url.indexOf("ui.ptlogin2.qq.com") == -1 && url.indexOf("w.mail.qq.com") == -1)
//					getSettings().setUserAgentString("Mozilla/5.0 (Linux; U; Android 5.1.1; zh-cn; PLK-UL00 Build/HONORPLK-UL00) AppleWebKit/537.36 (KHTML, like Gecko)Version/4.0 MQQBrowser/5.3 Mobile Safari/537.36");
//				else
//			        getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");

	            return false;
	        }
	        @Override
	        public void onPageFinished(WebView view, String url) {
	        	instantUrl = url;
//	        	view.loadUrl("javascript:window.HTML.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
	        	CookieManager cookieManager = CookieManager.getInstance();
	            cookieStr = cookieManager.getCookie(url);
//	            if (listener != null) {
//					listener.onPageFinished(url);
//				}
	        	super.onPageFinished(view, url);
	        }
	    }
	
	 @SuppressLint("AddJavascriptInterface")
	    public class JSLinster{
	        @JavascriptInterface
	        public void getHtml(String html)
	        {
	        	html = "<html>"+html+"</html>";
				Message msg = handler.obtainMessage();
				msg.obj = html;
				msg.what = 0;
				handler.sendMessage(msg);
	        }
	        @JavascriptInterface
	        public void onClick(int i)
	        {
				Message msg = handler.obtainMessage();
				msg.arg1 = i;
				msg.what = 1;
				handler.sendMessage(msg);
	        }
	        
	    }
	 
	 public interface CutWebCallback{
		 public void onReceiveTitle(String title);
		 public void onShouldOverrideUrl(String url);
		 public void onPageFinished(String url);
		 public void onLoadUrl(String url);
	 }
	 
	 public interface ReceiveHtmlListener{
		 public void onReceiveHTML(String url,String html);
	 }
	 
	 public interface ReceiveMessageListener{
		 public void onReceiveMessage(int tag);
	 }
	 
//	 public interface ReceiveHTMLListener{
//		 public void onReceiveHTML(String url,String html);
//	 }
//	 
//	 public interface ReceiveTitleListener{
//		 public void onReceiveTitle(String title);
//	 }
//	 
//	 public interface ReceiveMessageListener{
//		 public void onReceiveMessage(int tag);
//	 }
	 @Override
	public void loadUrl(String url) {
		// TODO Auto-generated method stub
//   		 if(url.indexOf("ui.ptlogin2.qq.com") == -1 && url.indexOf("w.mail.qq.com") == -1)
//				getSettings().setUserAgentString("Mozilla/5.0 (Linux; U; Android 5.1.1; zh-cn; PLK-UL00 Build/HONORPLK-UL00) AppleWebKit/537.36 (KHTML, like Gecko)Version/4.0 MQQBrowser/5.3 Mobile Safari/537.36");
//			else
//		        getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
		if(url != null && url.indexOf("file") == -1 &&listener != null)
			listener.onLoadUrl(url);
		super.loadUrl(url);
	}
	 
	 public String getCookie(){
		 return cookieStr;
	 }
	 
//	 public interface ShouldOverrideUrlListener{
//		 public void onShouldOverrideUrl(String url);
//	 }
	 
}
