package cn.hukecn.speechbrowser.location;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.iflytek.cloud.InitListener;

import android.content.Context;
import cn.hukecn.speechbrowser.DAO.MyDataBase;
import cn.hukecn.speechbrowser.bean.LocationBean;

public class BaseAppLocation {

//	private Context context = null;
	private LocationClient mLocationClient = null;
	private BDLocation location = null;
	private MyLocationListener listener = new MyLocationListener();
	private static BaseAppLocation instance;
	private BaseAppLocation(){};

	public void init(Context context){
//		this.context = context;
		mLocationClient = new LocationClient(context);     //����LocationClient��
		mLocationClient.registerLocationListener(listener);    //ע���������
		initLocation();
		
	}
	public static BaseAppLocation getInstance()
	{
		if(instance == null)
		{
			instance = new BaseAppLocation();
			
		}
		return instance;
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
	
	public  class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation arg0) {
			//Toast.makeText(getApplicationContext(), arg0.getCity(), Toast.LENGTH_SHORT).show();
			//mTts.startSpeaking(arg0.getLocationDescribe(), mSynListener);
//			MyDataBase db = MyDataBase.getInstance();
//			LocationBean bean = new LocationBean();
//			bean.latitude = arg0.getLatitude()+"";
//			bean.longitude = arg0.getLongitude()+"";
//			bean.time = arg0.getTime()+"";
//			long log = db.insert(bean);
			location = arg0;
		}
	}
	
	public  BDLocation getLocation(){
		return location;
	}
	
	public  void removeLocationListener(){
		mLocationClient.stop();
		mLocationClient.unRegisterLocationListener(listener);
	}
	
	
}
