package cn.hukecn.speechbrowser.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import cn.hukecn.speechbrowser.JsonParser;
import cn.hukecn.speechbrowser.R;
import cn.hukecn.speechbrowser.Shake;
import cn.hukecn.speechbrowser.Shake.ShakeListener;
import cn.hukecn.speechbrowser.StaticString;
import cn.hukecn.speechbrowser.bean.NewsBean;
import cn.hukecn.speechbrowser.http.MyHttp;
import cn.hukecn.speechbrowser.http.MyHttp.HttpCallBackListener;
import cn.hukecn.speechbrowser.util.PraseCommand;
import cn.hukecn.speechbrowser.util.PraseNews;
import cn.hukecn.speechbrowser.util.TencentNews;
import cn.hukecn.speechbrowser.util.TencentNews.NewsCallback;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import android.app.Activity;
import android.app.Service;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity implements ShakeListener{

	private SoundPool sp;//����һ��SoundPool
	private int music;//����һ��������load������������suondID
	private static Vibrator mVibrator;
	Button btn_start = null;
	long lastTime = 0l;
	long lastShakeTime = 0l;
	// ������д����
	private SpeechRecognizer mIat;
	// ������дUI
	private RecognizerDialog mIatDialog;
	
	private ScrollView scrollView = null;
	TextView tv_info = null;
	SpeechSynthesizer mTts;
	List<NewsBean> newsList = new ArrayList<NewsBean>();
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID +"=568fba83");   
	
		mIatDialog = new RecognizerDialog(MainActivity.this, mInitListener);
		mIatDialog.setListener(mRecognizerDialogListener);
		
		tv_info = (TextView) findViewById(R.id.info);		
		scrollView  = (ScrollView) findViewById(R.id.scrollview);
		
		mTts= SpeechSynthesizer.createSynthesizer(this, null);  
		mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoqi");
		mTts.setParameter(SpeechConstant.SPEED, "50");
		mTts.setParameter(SpeechConstant.VOLUME, "50");
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //�����ƶ�  
		
		//mTts.startSpeaking("��ӭʹ�ö๦�������������ҡ���ֻ����Է�������ָ��", mSynListener);
		
//		try {
//			Log.e("00",PraseBaiduResult.getHTML("��Ѷ��Ϸ"));
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		AsyncHttpClient client = new AsyncHttpClient();
//		client.get("http://m.baidu.com/s?word=��Ѷ��Ϸ", new AsyncHttpResponseHandler(){
//			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
//				
//					tv_info.setText(new String(arg2));
//				
//			}
//
//			@Override
//			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
//					Throwable arg3) {
//				// TODO Auto-generated method stub
//				
//			};
//		});
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
			
			tv_info.append(hour+":"+minute+":"+secend+"  ");
			for(String temp:list)
				tv_info.append(temp);
			tv_info.append("\n");
			
			scrollView.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					scrollView.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
			if(current - lastTime > 800)
			{
				//mTts.startSpeaking("����Ϊ��"+tv_info.getText(), mSynListener);
				int cmd = PraseCommand.prase(list);
				lastTime = current;
				switch (cmd) {
				case PraseCommand.Cmd_Search:
					cmdSearch(list);
					break;
				case PraseCommand.Cmd_News:
					cmdReadNews();
					break;
				case PraseCommand.Cmd_Weather:
					cmdWeather(list.get(list.size()-2));
					break;
				case PraseCommand.Cmd_Err:
				case PraseCommand.Cmd_Other:
					mTts.startSpeaking("ָ�������������ȷָ��",mSynListener);
					break;
				case PraseCommand.Cmd_NewsNum:
//					tv_info.append(PraseCommand.praseNewsIndex(list)+"\n");
					if(newsList != null)
						readNewsContent(PraseCommand.praseNewsIndex(list));
					else
						mTts.startSpeaking("ָ�������������ȷָ��",mSynListener);
					break;
				case PraseCommand.Cmd_Location:
					if(mLocationClient.isStarted())
						mLocationClient.stop();
			        mLocationClient.start();;
			        break;
				case PraseCommand.Cmd_Exit:
					mTts.startSpeaking("���ڹرջ����ˡ�����", mSynListener);
					handler.sendEmptyMessageDelayed(0, 3000);
					break;
				default:
					tv_info.append(results.getResultString()+"\n");
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
	private void readNewsContent(final int praseNewsIndex) {
		// TODO Auto-generated method stub
		//mTts.startSpeaking(, mSynListener);
		TencentNews.getNewsContent(newsList.get(praseNewsIndex - 1).newsUrl, new NewsCallback() {
			
			@Override
			public void onNewsListCallBack(List<NewsBean> list) {
				// TODO Auto-generated method stub
				//��ȡ��������ʱ����ʵ��
			}
			
			@Override
			public void onNewsContentCallBack(String content) {
				// TODO Auto-generated method stub
				tv_info.append(content);
				mTts.startSpeaking("��" + praseNewsIndex + "�����ţ����⣺" + newsList.get(praseNewsIndex-1).newsTitle+content, mSynListener);
			}
		});
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
	
	
	private SynthesizerListener mSynListener = new SynthesizerListener(){  
	    //�Ự�����ص��ӿڣ�û�д���ʱ��errorΪnull  
	    public void onCompleted(SpeechError error) {}  
	    //������Ȼص�  
	    //percentΪ�������0~100��beginPosΪ������Ƶ���ı��п�ʼλ�ã�endPos��ʾ������Ƶ���ı��н���λ�ã�infoΪ������Ϣ��  
	    public void onBufferProgress(int percent, int beginPos, int endPos, String info) {}  
	    //��ʼ����  
	    public void onSpeakBegin() {}  
	    //��ͣ����  
	    public void onSpeakPaused() {}  
	    //���Ž��Ȼص�  
	    //percentΪ���Ž���0~100,beginPosΪ������Ƶ���ı��п�ʼλ�ã�endPos��ʾ������Ƶ���ı��н���λ��.  
	    public void onSpeakProgress(int percent, int beginPos, int endPos) {}  
	    //�ָ����Żص��ӿ�  
	    public void onSpeakResumed() {}  
	//�Ự�¼��ص��ӿ�  
	    public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {}  
	};
	
	
	private void cmdWeather(final String cityname) {
		// TODO Auto-generated method stub
		MyHttp.get(StaticString.weatherUrl, "cityname="+cityname, new HttpCallBackListener() {
			
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
							String l_tmp = jsonData.getString("l_tmp");
							String h_tmp = jsonData.getString("h_tmp");
							String temp = jsonData.getString("temp");
							String weather = jsonData.getString("weather");
							String wd = jsonData.getString("WD");
							String ws = jsonData.getString("WS");
							int start = ws.indexOf("(");
							if(start != -1)
							{
								ws = ws.substring(0, start);
							}

							mTts.startSpeaking("����"+cityname+"������"+weather+
									"���¶�"+l_tmp+"��"+h_tmp+"���϶ȣ���ǰ���£�"+temp+"���϶ȣ�"+
									"������"+ws,mSynListener);
							tv_info.append("����"+cityname+"������"+weather+
									"\n�¶ȣ�"+l_tmp+"-"+h_tmp+"��\n��ǰ���£�"+temp+"��\n"+
									"������"+ws+"\n");
							return;
						}
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				mTts.startSpeaking("��������ȷ�Ĺ��ڳ��������磺�人����",mSynListener);
			}
		});
	}
	
	private void cmdReadNews(){
		//tv_info.setText("");
//		MyHttp.get(StaticString.newsUrl,
//				"channelId=5572a109b3cdc86cf39001db&page=1", new HttpCallBackListener() {
//					@Override
//					public void onHttpCallBack(int statusCode, String responseStr) {
//						// TODO Auto-generated method stub
//						
//						List<NewsBean> list = PraseNews.prase(responseStr);
//						
//						String newsStr = "";
//						for(int i = 0;i < list.size();i++)
//						{
//							newsStr+=i+1 + "��" + list.get(i).newsTitle+"\n";
//						}
//						
//						tv_info.append(newsStr);
//						mTts.startSpeaking("����Ϊ��������������.\n"+newsStr, mSynListener);
//					}
//				});
		
		TencentNews.getNewsList(new NewsCallback() {
			@Override
			public void onNewsListCallBack(List<NewsBean> list) {
				// TODO Auto-generated method stub
				newsList = list;
				String titleStr = "";
				for(int i = 1;i <= list.size();i++)
				{
					titleStr += "��"+i+"����"+list.get(i-1).newsTitle+"\n";	
					tv_info.append(i+"��"+list.get(i-1).newsTitle+"\n");
				}
				mTts.startSpeaking("����Ϊ�������������š�\n" + titleStr, mSynListener);
			}

			@Override
			public void onNewsContentCallBack(String content) {
				// TODO Auto-generated method stub
				//��ȡ����ʱ����ʵ�ָ÷���
			}
		});
	}
	
	private void cmdSearch(List<String> list) {
		
		String str = "";
		for(String temp:list)
		{
			str += temp;
		}
		
		//mTts.startSpeaking("����Ϊ��"+str, mSynListener);
		str = str.replace("����", "");
//		String url = "http://www.baidu.com/s?wd="+str+"&ie=UTF-8";
		String url = "http://m.baidu.com/s?word="+str;
//		Intent intent = new Intent(this,WebviewActivity.class);
//		intent.putExtra("url", url);
//		startActivity(intent);
		
			MyHttp.get(url, "", new HttpCallBackListener() {
				
				@Override
				public void onHttpCallBack(int statusCode, String responseStr) {
					// TODO Auto-generated method stub
					//tv_info.append(responseStr+"\n_________________________\n");

					Document doc = Jsoup.parse(responseStr);
					//Element result = doc.getElementById("page-res");
					Elements elements = doc.getElementsByClass("result_title");
//					Elements elements = results.getElementsByClass("result c-result c-clk-recommend");
							
					String speakStr = "";
					String tvter = "";
					for(int i = 1;i <= elements.size();i++)
					{
						speakStr += "��"+i+"����"+elements.get(i-1).text()+'\n';
						tvter += i + "��"+elements.get(i-1).text()+"\n";
					}
					mTts.startSpeaking("���������������"+speakStr, mSynListener);
					tv_info.append(tvter);
				}
			});	
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
    }
	
	
	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation arg0) {
			// TODO Auto-generated method stub
			tv_info.append(arg0.getLocationDescribe()+"\n");
			mTts.startSpeaking(arg0.getLocationDescribe(), mSynListener);
		}
	}
	
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			exitApp();
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

}
