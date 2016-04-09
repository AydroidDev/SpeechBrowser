package cn.hukecn.speechbrowser.util;

public class ParsePageType {
	//QQ�����¼ҳ
	public static final String MailLoginUrl = "ui.ptlogin2.qq.com/cgi-bin/login?style=";
	public static final int MailLoginTag = 1;
	
	//QQ�����½����ҳ
	public static final String MailHomePageUrl = "w.mail.qq.com/cgi-bin/today?sid=";
	public static final int MailHomePageTag = 8;
	
	//QQ�����ռ����б�ҳ
	public static final String MailListUrl = "w.mail.qq.com/cgi-bin/mail_list";
	public static final int MailListTag = 2;
	
	//QQ�����ʼ�����ҳ
	public static final String MailContentUrl = "w.mail.qq.com/cgi-bin/readmail";
	public static final int MailContentTag = 3;
	
	//��Ѷ���������б�
	public static final String NewsListUrl = "info.3g.qq.com/g/s?icfa=infocenter&aid=template&tid=news_guoneiss&i_f=703";
	public static final int NewsListTag = 4;
	//��Ѷ������������
	public static final String NewsContentUrl = "info.3g.qq.com/g/s?icfa=news_guoneiss&aid=news_ss&id=news_";
	public static final int NewsContentTag = 5;
	//��������
	public static final String SinaWeatherUrl = "weather1.sina.cn";
	public static final int SinaWeatherTag = 6;
	//�ٶ��������
	public static final String BaiduResultUrl = "m.baidu.com/s?word=";
	public static final int BaiduResultUrlTag = 7;
	
	
	public static final int NoSupportTag = 99;
	private ParsePageType(){};
	
	public static int getPageType(String url){
		if(url.indexOf(MailLoginUrl) != -1)
			return MailLoginTag;
		
		if(url.indexOf(MailHomePageUrl) != -1)
			return MailHomePageTag;
		
		if(url.indexOf(MailListUrl) != -1)
			return MailListTag;
		
		if(url.indexOf(MailContentUrl) != -1)
			return MailContentTag;
		
		if(url.indexOf(NewsListUrl) != -1)
			return NewsListTag;
		
		if(url.indexOf(NewsContentUrl) != -1)
			return NewsContentTag;
		
		if(url.indexOf(SinaWeatherUrl) != -1)
			return SinaWeatherTag;
		
		if(url.indexOf("m.baidu.com") != -1 && url.indexOf("/s?word=") != -1)
			return BaiduResultUrlTag;
		
		return NoSupportTag;
	}
}
