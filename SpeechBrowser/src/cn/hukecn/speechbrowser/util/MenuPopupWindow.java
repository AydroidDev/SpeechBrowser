package cn.hukecn.speechbrowser.util;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.hukecn.speechbrowser.R;

public class MenuPopupWindow extends PopupWindow implements OnClickListener{
	
	TextView btn_m_bookmark,btn_m_setting,btn_m_email,btn_m_exit;
	OnClickListener listener = null;
	Context context;
	int height;
	int width;
	Window window = null;
	
	public MenuPopupWindow(Context mContext,OnClickListener listener,Window window) {
		// TODO Auto-generated constructor stub
		this.listener = listener;
		this.context = mContext;
		this.window = window;
		
		 final View contentView = LayoutInflater.from(mContext).inflate(R.layout.menu_popup_window, null);
		 btn_m_bookmark = (TextView) contentView.findViewById(R.id.btn_m_bookmark);
		 btn_m_email = (TextView) contentView.findViewById(R.id.btn_m_email);
		 btn_m_exit = (TextView) contentView.findViewById(R.id.btn_m_exit);
		 btn_m_setting = (TextView) contentView.findViewById(R.id.btn_m_setting);
		 
		 btn_m_bookmark.setOnClickListener(this);
		 btn_m_email.setOnClickListener(this);
		 btn_m_exit.setOnClickListener(this);
		 btn_m_setting.setOnClickListener(this);
		 
		 
		 int w = View.MeasureSpec.makeMeasureSpec(0,
	                View.MeasureSpec.UNSPECIFIED);
	     int h = View.MeasureSpec.makeMeasureSpec(0,
	                View.MeasureSpec.UNSPECIFIED);
	     contentView.measure(w, h);
	     height = contentView.getMeasuredHeight();
	     width = contentView.getMeasuredWidth();
		 
		 
		 this.setContentView(contentView);  
	        //����SelectPicPopupWindow��������Ŀ�  
	        this.setWidth(LayoutParams.MATCH_PARENT);  
	        //����SelectPicPopupWindow��������ĸ�  
	        this.setHeight(LayoutParams.WRAP_CONTENT);  
	        //����SelectPicPopupWindow��������ɵ��  
	        this.setFocusable(true);  
	        //����SelectPicPopupWindow�������嶯��Ч��  
//	        this.setAnimationStyle(android.R.style.AnimBottom);  
	        //ʵ����һ��ColorDrawable��ɫΪ��͸��  
//	        ColorDrawable dw = new ColorDrawable(0xb0000000);  
	        //����SelectPicPopupWindow��������ı���  
//	        this.setBackgroundDrawable(dw);  
	        
	        setBackgroundDrawable(new BitmapDrawable());
	        setOutsideTouchable(true);
	}
	
	public void showPopupWindow(View v)
	{
		int[] location = new int[2];
      	v.getLocationOnScreen(location);
        showAtLocation(v, Gravity.NO_GRAVITY,0,location[1]-height);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(listener != null)
			listener.onClick(v);
	}
}
