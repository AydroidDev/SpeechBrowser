package cn.hukecn.speechbrowser.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class ParseMailContent {

	public static String praseMailContent(String html){
		String title = Jsoup.parse(html).getElementsByClass("readmail_item_head_titleText").get(0).text();
		String mailContent="";
		if(title.length() > 0)
			mailContent += "�ʼ����⣺"+title+"��\n";
//		else
//		{
//			mailContent = "�ʼ�����ʧ�ܣ����Ժ����ԡ�";
//			return mailContent;
//		}

		int start = -1;
		int end = -1;
		start = html.indexOf("readmail_item_contentNormal qmbox");
		end = html.indexOf("readmail_attachWrap",start);
		String htmlContent;
		if(start != -1 && end != -1)
		{
			htmlContent = html.substring(start - 12,end - 12);
			mailContent += "�ʼ����ݣ�"+Jsoup.parse(htmlContent).text();
		}else
		{
			start = html.indexOf("readmail_item_contentConversation qmbox");
			end = html.indexOf("readmail_attachWrap",start);
			if(start != -1 && end != -1)
			{
				htmlContent = html.substring(start - 12,end - 12);
				mailContent += "�ʼ����ݣ�"+Jsoup.parse(htmlContent).text();
			}else
				mailContent += "�ʼ������ݲ�֧���Ķ�";
		}
		
		return mailContent;
	}
}
