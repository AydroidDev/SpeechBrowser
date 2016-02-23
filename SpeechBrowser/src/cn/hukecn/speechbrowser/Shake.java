package cn.hukecn.speechbrowser;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Shake{
	
	private static SensorManager mSensorManager;
	private static ShakeListener mlistener = null;
	
	private Shake(Context context,ShakeListener listener) {}
	
	public static void registerListener(Context context,ShakeListener listener){
		//��ȡ�������������  
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);  
        //�𶯷���
      //���ٶȴ�����  
		mSensorManager.registerListener(seListener,  
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),  
		//����SENSOR_DELAY_UI��SENSOR_DELAY_FASTEST��SENSOR_DELAY_GAME�ȣ�  
		//���ݲ�ͬӦ�ã���Ҫ�ķ�Ӧ���ʲ�ͬ���������ʵ������趨  
				SensorManager.SENSOR_DELAY_NORMAL);
		
		mlistener = listener;
	}
	
	public static void removeListener(){
		mSensorManager.unregisterListener(seListener);
	}
	
	
	
	public interface ShakeListener{
		public void onShake();	
		}


	public static SensorEventListener seListener = new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			int sensorType = event.sensor.getType();  
			  
			  //values[0]:X�ᣬvalues[1]��Y�ᣬvalues[2]��Z��
			  float[] values = event.values;  
			  if(sensorType == Sensor.TYPE_ACCELEROMETER){  
				   if((Math.abs(values[0])>25 || Math.abs(values[1])>25 || Math.abs(values[2])>25)){
					   //ҡ���ֻ�������button����ʾ����Ϊ��    
					  if(mlistener != null)
					  {
						  mlistener.onShake();
					  }
				   }
			  }
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}
	};
	
}

