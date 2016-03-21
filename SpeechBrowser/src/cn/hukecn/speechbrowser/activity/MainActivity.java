package cn.hukecn.speechbrowser.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import cn.hukecn.speechbrowser.JsonParser;
import cn.hukecn.speechbrowser.R;
import cn.hukecn.speechbrowser.Shake;
import cn.hukecn.speechbrowser.Shake.ShakeListener;
import cn.hukecn.speechbrowser.StaticString;
import cn.hukecn.speechbrowser.bean.MailListBean;
import cn.hukecn.speechbrowser.bean.NewsBean;
import cn.hukecn.speechbrowser.bean.WeatherBean;
import cn.hukecn.speechbrowser.http.MyHttp;
import cn.hukecn.speechbrowser.http.MyHttp.HttpCallBackListener;
import cn.hukecn.speechbrowser.util.BaiduSearch;
import cn.hukecn.speechbrowser.util.CutWebView;
import cn.hukecn.speechbrowser.util.CutWebView.ReceiveHTMLListener;
import cn.hukecn.speechbrowser.util.CutWebView.ShouldOverrideUrlListener;
import cn.hukecn.speechbrowser.util.PraseCommand;
import cn.hukecn.speechbrowser.util.PraseMailContent;
import cn.hukecn.speechbrowser.util.PraseMailList;
import cn.hukecn.speechbrowser.util.PraseNews;
import cn.hukecn.speechbrowser.util.PraseTencentNews;
import cn.hukecn.speechbrowser.util.PraseWeatherHtml;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import android.accounts.Account;
import android.accounts.OnAccountsUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements ShakeListener,ReceiveHTMLListener,ShouldOverrideUrlListener{
	List<Integer> cmdList = new ArrayList<Integer>();
	private SoundPool sp;//����һ��SoundPool
	private int music;//����һ��������load������������suondID
	private int newsNumber = -1;
	private static Vibrator mVibrator;
	Button btn_exit = null;
	Button btn_stop = null;
	Button btn_setting = null;
	int browserState = PraseCommand.Cmd_Original;
	long lastTime = 0l;
	long lastShakeTime = 0l;
	String mailCookie = "";
	String msid = "";
	// ������д����
	private SpeechRecognizer mIat;
	// ������дUI
	private RecognizerDialog mIatDialog;
	TextView title = null;
	TextView tv_info = null;
	SpeechSynthesizer mTts;
	List<NewsBean> newsList = new ArrayList<NewsBean>();
	List<MailListBean> mailList = new ArrayList<MailListBean>();
	CutWebView webView = null;
	public LocationClient mLocationClient = null;
	BDLocation location = null;
	public BDLocationListener myListener = new MyLocationListener();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID +"=568fba83");   
	
		mIatDialog = new RecognizerDialog(MainActivity.this, mInitListener);
		mIatDialog.setListener(mRecognizerDialogListener);
		
		tv_info = (TextView) findViewById(R.id.info);		
		webView = (CutWebView) findViewById(R.id.webview);
		webView.setOnReceiveHTMLListener(this);
		webView.setOnShouldOverrideUrlListener(this);
		title = (TextView) findViewById(R.id.title);
		btn_exit = (Button) findViewById(R.id.btn_exit);
		
		btn_exit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				exitApp();
			}
		});
		
		btn_stop = (Button)findViewById(R.id.btn_stop);
		btn_stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(mTts.isSpeaking())
					mTts.stopSpeaking();
			}
		});
		
		btn_setting = (Button) findViewById(R.id.btn_setting);
		btn_setting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,SettingActivity.class);
				startActivity(intent);
			}
		});
		
		mTts= SpeechSynthesizer.createSynthesizer(this, null);  
		mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoqi");
		mTts.setParameter(SpeechConstant.SPEED, "50");
		mTts.setParameter(SpeechConstant.VOLUME, "50");
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //�����ƶ�  
		
		sp= new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
		music = sp.load(this, R.raw.shake, 1);
	
        mVibrator = (Vibrator)getSystemService(Service.VIBRATOR_SERVICE);  

        mLocationClient = new LocationClient(getApplicationContext());     //����LocationClient��
        mLocationClient.registerLocationListener(myListener);    //ע���������
        initLocation();
	}
	private InitListener mInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
		}
	};
	
	private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {
			//tv_info.setText("");
			List<String> list= JsonParser.parseIatResult(results.getResultString());

			long current = System.currentTimeMillis();
			if(list.get(0).equals("��") || list.get(0).equals(""))
				return ;
			if(list.get(list.size() -1).equals("��"))
				list.remove(list.size()-1);
			
			Calendar c = Calendar.getInstance();  
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			int secend = c.get(Calendar.SECOND);
			
			if(current - lastTime > 800)
			{
				browserState = PraseCommand.prase(list);
				lastTime = current;
				if(browserState != PraseCommand.Cmd_NewsNum)
					cmdList.add(browserState);
				
				switch (browserState) {
				case PraseCommand.Cmd_Search:
					cmdSearch(list);
					break;
				case PraseCommand.Cmd_News:
					cmdReadNews();
					
					break;
				case PraseCommand.Cmd_Weather:
					String cityname = "";
					for(String item:list)
						cityname += item;
					
					if(cityname.indexOf("����") != -1)
						cityname = cityname.replace("����", "");
					
					cmdWeather(cityname);
					
//					else
//					{
//						cityname = location.getCity();
//						if(cityname != null && cityname.length() > 0)
//						{
//							cityname = cityname.replace("��", "");
//							cmdWeather(cityname);
//						}else
//						{
//							cmdWeather("�人");
//						}
//					}
					break;
				
				case PraseCommand.Cmd_NewsNum:
//					tv_info.append(PraseCommand.praseNewsIndex(list)+"\n");
					if(cmdList.size() == 0)
					{
						mTts.startSpeaking("ָ�������������ȷָ��",mSynListener);
						break;
					}
					
					if(cmdList.get(cmdList.size() -1) == PraseCommand.Cmd_Mail)
					{
						//������ʼ�����
						if(mailList != null && mailList.size()>0)
						{
							browserState = PraseCommand.Cmd_Mail_MailContent;
							readMailContent(PraseCommand.praseNewsIndex(list));
						}else
							mTts.startSpeaking("��ȡ�ʼ�����ʧ�ܣ����Ժ�����",mSynListener);
						break;
					}
					
					if(cmdList.get(cmdList.size() -1) == PraseCommand.Cmd_News)
					{
						//�������������
						if(newsList != null && newsList.size() > 0)
						{	
							readNewsContent(PraseCommand.praseNewsIndex(list));
						}else
							mTts.startSpeaking("��ȡ��������ʧ�ܣ����Ժ�����",mSynListener);
						break;
					}
					
					mTts.startSpeaking("ָ�������������ȷָ��",mSynListener);
					break;
				case PraseCommand.Cmd_Location:
//					if(mLocationClient.isStarted())
//						mLocationClient.stop();
//			        mLocationClient.start();
					mTts.startSpeaking("����ǰλ�ڣ�"+location.getAddrStr()+"����",mSynListener);
					String url = "http://m.baidu.com/s?word="+location.getProvince()+location.getCity();
					webView.loadUrl(url);
					break;
				case PraseCommand.Cmd_Exit:
					mTts.startSpeaking("���ڹرջ����ˡ�����", mSynListener);
					handler.sendEmptyMessageDelayed(0, 3000);
					break;
				case PraseCommand.Cmd_Mail:
					cmdMail();
					break;
				case PraseCommand.Cmd_Err:
				case PraseCommand.Cmd_Other:
				default:
					mTts.startSpeaking("ָ�������������ȷָ��",mSynListener);
					break;
				}
			}
		}
		/**
		 * ʶ��ص�����.
		 */
		public void onError(SpeechError error) {
			//showTip(error.getPlainDescription(true));
		}
	};
	
	private void cmdMail()
	{
		/*
		f:xhtmlmp
		delegate_url:
		f:xhtmlmp
		action:
		tfcont:
		uin:hukecn
		aliastype:@qq.com
		pwd:00220388066
		mss:1
		mtk:
		btlogin:��¼
		*/
		
		String url = StaticString.mailLogin;
		String postDate = "f=xhtmlmp&uin=hukecn&aliastype=@qq.com&pwd=00220388066&mss=1&btlogin=��½";
		webView.postUrl(url, EncodingUtils.getBytes(postDate, "utf-8"));
	}
	
	protected void readMailContent(int praseNewsIndex) {
		// TODO Auto-generated method stub
		if(praseNewsIndex > mailList.size())
			mTts.startSpeaking("�����������ڣ�����������ָ��", mSynListener);
		else
			webView.loadUrl(mailList.get(praseNewsIndex - 1).mailUrl);
	}

	private void readNewsContent(final int praseNewsIndex) {
		// TODO Auto-generated method stub
		newsNumber = praseNewsIndex;
		if(praseNewsIndex > newsList.size())
			mTts.startSpeaking("�����������ڣ�����������ָ��", mSynListener);
		else
			webView.loadUrl(newsList.get(praseNewsIndex - 1).newsUrl);
	}
	@Override
	public void onShake() {
		// TODO Auto-generated method stub
		if(mTts.isSpeaking())
			mTts.stopSpeaking();
		if(System.currentTimeMillis() - lastShakeTime > 1200)
		{	
			mVibrator.vibrate(500);
			sp.play(music, 1, 1, 0, 0, 1);
			mIatDialog.show();
		}
		lastShakeTime = System.currentTimeMillis();
	}
	
	
	private SynthesizerListener mSynListener = new SynthesizerListener()
	{  
	    //�Ự�����ص��ӿڣ�û�д���ʱ��errorΪnull  
	    public void onCompleted(SpeechError error) {
	    	btn_stop.setText("��ʼ����");
	    	//Toast.makeText(getApplicationContext(), "completed", Toast.LENGTH_SHORT).show();
	    }  
	    //������Ȼص�  
	    //percentΪ�������0~100��beginPosΪ������Ƶ���ı��п�ʼλ�ã�endPos��ʾ������Ƶ���ı��н���λ�ã�infoΪ������Ϣ��  
	    public void onBufferProgress(int percent, int beginPos, int endPos, String info) {}  
	    //��ʼ����  
	    public void onSpeakBegin() {
	    	//Toast.makeText(getApplicationContext(), "Begin", Toast.LENGTH_SHORT).show();
	    	btn_stop.setText("ֹͣ����");
	    }  
	    
	    
	    //��ͣ����  
	    public void onSpeakPaused() {
	    	//Toast.makeText(getApplicationContext(), "pause", Toast.LENGTH_SHORT).show();
	    }  
	    //���Ž��Ȼص�  
	    //percentΪ���Ž���0~100,beginPosΪ������Ƶ���ı��п�ʼλ�ã�endPos��ʾ������Ƶ���ı��н���λ��.  
	    public void onSpeakProgress(int percent, int beginPos, int endPos) {}  
	    //�ָ����Żص��ӿ�  
	    public void onSpeakResumed() {}  
	//�Ự�¼��ص��ӿ�  
	    public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
	    	if(arg0 == SpeechEvent.EVENT_TTS_CANCEL)
	    		btn_stop.setText("��ʼ����");
	    }  
	};
	
	
	private void cmdWeather(final String cityname) {
		// TODO Auto-generated method stub
		MyHttp.get(StaticString.weatherUrl, "cityname="+cityname, new HttpCallBackListener() 
		{
			@Override
			public void onHttpCallBack(int statusCode, String responseStr) {
				// TODO Auto-generated method stub
				if(statusCode == 200)
				{
					//˵������ȷ�ĳ�����
					try {
						JSONObject jsonObject = new JSONObject(responseStr);
						int errNum = jsonObject.getInt("errNum");
						if(errNum == 0)
						{
							JSONObject jsonData = jsonObject.getJSONObject("retData");
//							String city = jsonData.getString("city");
//							
//							if(city == null || city.length() == 0)
//							{
//								city= "�人";
//							}
							String url = "http://m.baidu.com/s?word="+cityname+"����";
							webView.loadUrl(url);
							
							
							String windStr = jsonData.getString("WS");
							int start = windStr.indexOf("(");
							if(start > 0)
								windStr = windStr.substring(0,start);
							
							mTts.startSpeaking(jsonData.getString("city")+",����������"+
									jsonData.getString("weather")+",������£�"+
									jsonData.getString("h_tmp")+"���϶ȣ�������£�"+
									jsonData.getString("l_tmp")+"���϶ȣ�"+
									jsonData.getString("WD")+"��������"+
									windStr, mSynListener);
							
							return;
						}
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
					}
				}
				//û��˵����ȷ�ĳ�����,Ĭ�ϱ�������
				String cityname = "test";
				if(cityname != null && cityname.length() > 0)
				{
					cityname = location.getCity();
					cityname = cityname.replace("��", "");
					MyHttp.get(StaticString.weatherUrl, "cityname="+cityname, new HttpCallBackListener()
					{
						@Override
						public void onHttpCallBack(int statusCode, String responseStr) {
							// TODO Auto-generated method stub
							if(statusCode == 200)
							{
								try {
									JSONObject jsonObject = new JSONObject(responseStr);
									int errNum = jsonObject.getInt("errNum");
									if(errNum == 0)
									{
										JSONObject jsonData = jsonObject.getJSONObject("retData");
										String citycode = jsonData.getString("city");
										if(citycode == null || citycode.length() == 0)
										{
											citycode= "�人";
										}
										
										String url = "http://m.baidu.com/s?word="+ citycode +"����";
										webView.loadUrl(url);
										
										String windStr = jsonData.getString("WS");
										int start = windStr.indexOf("(");
										if(start > 0)
											windStr = windStr.substring(0,start);
										
										mTts.startSpeaking(jsonData.getString("city")+",����������"+
												jsonData.getString("weather")+",������£�"+
												jsonData.getString("h_tmp")+"���϶ȣ�������£�"+
												jsonData.getString("l_tmp")+"���϶ȣ�"+
												jsonData.getString("WD")+"��������"+
												windStr, mSynListener);
										return;
										}	
											
									} catch(JSONException e) {
										Toast.makeText(getApplicationContext(), "����ʧ��", Toast.LENGTH_SHORT).show();
										mTts.startSpeaking("��ȡ����ʧ�ܣ����Ժ�����",mSynListener);
									}
								}else
								{
									Toast.makeText(getApplicationContext(), "����ʧ��", Toast.LENGTH_SHORT).show();
									mTts.startSpeaking("��ȡ����ʧ�ܣ����Ժ�����",mSynListener);
								}
						}});	
					}
				return;
			}
		});
	}
	
	private void cmdReadNews(){
		webView.loadUrl(PraseTencentNews.HOMEURL);
//		
//		PraseTencentNews.getNewsList(new NewsCallback() {
//			@Override
//			public void onNewsListCallBack(List<NewsBean> list) {
//				// TODO Auto-generated method stub
//				newsList = list;
//				String titleStr = "";
//				for(int i = 1;i <= list.size();i++)
//				{
//					titleStr += "��"+i+"����"+list.get(i-1).newsTitle+"\n";	
////					tv_info.append(i+"��"+list.get(i-1).newsTitle+"\n");
//				}
//				mTts.startSpeaking("����Ϊ�������������š�\n" + titleStr, mSynListener);
//			}
//
//			@Override
//			public void onNewsContentCallBack(String content) {
//				// TODO Auto-generated method stub
//				//��ȡ����ʱ����ʵ�ָ÷���
//			}
//		});
	}
	
	private void cmdSearch(List<String> list) {
		String str = "";
		for(String temp:list)
		{
			str += temp;
		}
		str = str.replace("����", "");
		String url = "http://m.baidu.com/s?word="+str;
		webView.loadUrl(url);
	}
	private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Battery_Saving);//��ѡ��Ĭ�ϸ߾��ȣ����ö�λģʽ���߾��ȣ��͹��ģ����豸
        option.setCoorType("bd09ll");//��ѡ��Ĭ��gcj02�����÷��صĶ�λ�������ϵ
        int span=0;
        option.setScanSpan(span);//��ѡ��Ĭ��0��������λһ�Σ����÷���λ����ļ����Ҫ���ڵ���1000ms������Ч��
        option.setIsNeedAddress(true);//��ѡ�������Ƿ���Ҫ��ַ��Ϣ��Ĭ�ϲ���Ҫ
        option.setOpenGps(false);//��ѡ��Ĭ��false,�����Ƿ�ʹ��gps
        //option.setLocationNotify(true);//��ѡ��Ĭ��false�������Ƿ�gps��Чʱ����1S1��Ƶ�����GPS���
        option.setIsNeedLocationDescribe(true);//��ѡ��Ĭ��false�������Ƿ���Ҫλ�����廯�����������BDLocation.getLocationDescribe��õ�����������ڡ��ڱ����찲�Ÿ�����
        //option.setIsNeedLocationPoiList(true);//��ѡ��Ĭ��false�������Ƿ���ҪPOI�����������BDLocation.getPoiList��õ�
        option.setIgnoreKillProcess(false);//��ѡ��Ĭ��true����λSDK�ڲ���һ��SERVICE�����ŵ��˶������̣������Ƿ���stop��ʱ��ɱ��������̣�Ĭ�ϲ�ɱ��  
        option.SetIgnoreCacheException(false);//��ѡ��Ĭ��false�������Ƿ��ռ�CRASH��Ϣ��Ĭ���ռ�
        option.setEnableSimulateGps(false);//��ѡ��Ĭ��false�������Ƿ���Ҫ����gps��������Ĭ����Ҫ
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }
	
	
	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation arg0) {
			location = arg0;
			//Toast.makeText(getApplicationContext(), arg0.getCity(), Toast.LENGTH_SHORT).show();
			//mTts.startSpeaking(arg0.getLocationDescribe(), mSynListener);
		}
	}
	
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				exitApp();
				break;
			case 1:
				List<String> resList = (List<String>) msg.obj;
				
				break;
			}
		}
	};
	
	protected void onResume() {
		Shake.registerListener(this, this);
		super.onResume();
	}
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Shake.removeListener();
		if(mTts.isSpeaking())
			mTts.stopSpeaking();
		mLocationClient.stop();
		mLocationClient.unRegisterLocationListener(myListener);
		super.onDestroy();
	}
	
	private void exitApp(){
		Shake.removeListener();
		if(mTts.isSpeaking())
			mTts.stopSpeaking();
		mLocationClient.stop();
		mLocationClient.unRegisterLocationListener(myListener);
		finish();
		System.exit(0);
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	 
	 	@Override
	    public void onBackPressed() {
	 		if(mTts.isSpeaking())
				mTts.stopSpeaking();
	 		browserState = PraseCommand.Cmd_Original;
	        if(webView.canGoBack())
	            webView.goBack();
	        else
	            super.onBackPressed();
	    }
	 	
	    public static void writeFileSdcard(String fileName,String message)
	    { 
	    	try {  
	    	    File file = new File(Environment.getExternalStorageDirectory(),  
	    	            "c.txt");  
	    	        FileOutputStream fos = new FileOutputStream(file, false);  
	    	 
	    	           fos.write(message.getBytes("utf-8"));  
	    	           fos.close();  
	    	           //Toast.makeText(getApplicationContext(), "д��ɹ�", Toast.LENGTH_SHORT).show();
	    	} catch (Exception e) {
	    	    e.printStackTrace();  
	    	}  
	    }
	    
		@Override
		public void onReceiveHTML(String html) {
			// TODO Auto-generated method stub
			//Document doc = Jsoup.parse(html);
			//title.setText(doc.getElementsByTag("title").get(0).text());
			switch (browserState) {
			case PraseCommand.Cmd_Search:
				List<String> resList = BaiduSearch.praseSearchResultList(html);
				if(resList.size() != 0)
				{
					for(int i = 1;i <= resList.size();i++)
						tv_info.append("��"+i+"����"+resList.get(i-1)+"\n");
					mTts.startSpeaking("���������������"+tv_info.getText().toString(), mSynListener);
				}else
				{
					mTts.startSpeaking("�������������ԡ�", mSynListener);
				}
				break;
			case PraseCommand.Cmd_News:
				writeFileSdcard("",html);
				newsList = PraseTencentNews.getNewsList(html);
				String titleStr = "";
				for(int i = 1;i <= newsList.size();i++)
				{
					titleStr += "��"+i+"����"+newsList.get(i-1).newsTitle+"\n";
				}
				mTts.startSpeaking("����Ϊ�������������š�\n" + titleStr, mSynListener);
				break;
			case PraseCommand.Cmd_NewsNum:
				String content = PraseTencentNews.getNewsContent(html);
				mTts.startSpeaking("��" + newsNumber + "�����ţ����⣺" + newsList.get(newsNumber-1).newsTitle+content, mSynListener);
				break;
			case PraseCommand.Cmd_Mail:
				//Log.e("111",webView.getCookie());
				mailCookie = webView.getCookie();
				int cookieStart = mailCookie.indexOf("msid=") + 5;
				int cookieEnd = mailCookie.indexOf(";", cookieStart);
				if(cookieStart != -1 && cookieEnd != -1 && cookieEnd > cookieStart)
				{
					msid = mailCookie.substring(cookieStart,cookieEnd);
					browserState = PraseCommand.Cmd_Mail_Home;
					
					//webView.loadUrl(StaticString.mailHome + msid + "&first=1&bmkey=");
					webView.loadUrl(StaticString.mailHome2 + msid);

					mTts.startSpeaking("���ڵ�½����...", mSynListener);
				}else
				{
					mTts.startSpeaking("�����½ʧ�ܣ����Ժ�����", mSynListener);
				}
				break;
			case PraseCommand.Cmd_Weather:
				//Toast.makeText(getApplicationContext(), "����", Toast.LENGTH_SHORT).show();
//				List<WeatherBean> list = PraseWeatherHtml.praseWeatherList(html);
//				if(list.size() == 0)
//				{
//					mTts.startSpeaking("��ȡ������Ϣʧ�ܣ����Ժ�����", mSynListener);
//				}else
//				{
//					int start = html.indexOf("��������")+6;
//					int end = html.indexOf("<", start);
//					String weatherStr = "";
//					if(start != -1 && end != -1 && end > start)
//					{
//						String cityname = html.substring(start,end);
//						if(cityname != null && cityname.length() > 0)
//							weatherStr += "����Ϊ������"+cityname+"δ����������״����";
//					}
//					if(weatherStr.length() == 0)
//						weatherStr += "����Ϊ������δ����������״����";
//
//					for(WeatherBean bean:list)
//					{
//						weatherStr+= bean.date +"������"+bean.weather+"���¶ȣ�"+bean.temp+"��\n";
//					}
//					mTts.startSpeaking(weatherStr, mSynListener);
//				}
				
				
				break;
			case PraseCommand.Cmd_Mail_Home:
			{
				browserState = PraseCommand.Cmd_Mail_InBox;
				webView.loadUrl(StaticString.mailInboxList2+msid+"&folderid=1&page=0&pagesize=10&sorttype=time&t=mail_list&loc=today,,,151&version=html");
				break;
			}
			case PraseCommand.Cmd_Mail_InBox:				
				Toast.makeText(getApplicationContext(), "�ʼ��б�", Toast.LENGTH_LONG).show();
				if(html.length() > 0)
				{
					mailList = PraseMailList.parseMailList(html);
					if(mailList.size() == 0)
						mTts.startSpeaking("�ʼ��б��ȡʧ�ܣ����Ժ�����", mSynListener);
					else
					{
						String mailListStr = "���"+mailList.size()+"���ʼ���Ϣ���£�";
						for(int i = 1;i <= mailList.size();i++)
						{
							mailListStr += "��"+i+"����"+mailList.get(i-1).descStr+"��";
						}
						mTts.startSpeaking(mailListStr, mSynListener);
					}
				}else
				{
					mTts.startSpeaking("�ʼ��б��ȡʧ�ܣ����Ժ�����", mSynListener);
				}
				break;
			case PraseCommand.Cmd_Mail_MailContent:
			{
				if(html.length() > 0)
				{
					String mailContent = PraseMailContent.praseMailContent(html);
					mTts.startSpeaking("�ʼ��������£�"+mailContent, mSynListener);
				}
				else
				{
					mTts.startSpeaking("�ʼ������ȡʧ�ܣ����Ժ�����", mSynListener);
				}
			}
			default:
				
				break;
			}
			
		}

		@Override
		public void onShouldOverrideUrl(String url) {
			// TODO Auto-generated method stub
			browserState = PraseCommand.Cmd_Original;
			if(mTts.isSpeaking())
				mTts.stopSpeaking();
		}
}
