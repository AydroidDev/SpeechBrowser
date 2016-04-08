package cn.hukecn.speechbrowser.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class ParseBaiduResult {
	//��ðٶȵ�����ҳ�棬ǰ100���������  
    public static String getHTML(String key) throws IOException  
    {  
        StringBuilder sb=new StringBuilder();  
        String path="http://www.baidu.com/s?tn=ichuner&lm=-1&word="+URLEncoder.encode(key,"gb2312")+"&rn=100";   
        URL url=new URL(path);  
        BufferedReader breader=new BufferedReader(new InputStreamReader(url.openStream()));  
        String line=null;  
        while((line=breader.readLine())!=null)  
        {  
            sb.append(line);  
        }  
        return sb.toString();  
    }  
      
    //��HTML������ȡ����ȡ��100��URL�������ժҪ  
    private static String[][] parseHTML(String key)  
    {  
        String page=null;  
        try  
        {  
            page=getHTML(key);  
        }  
        catch(Exception ex)  
        {  
            ex.printStackTrace();  
        }  
        String[][] pageContent_list=new String[100][3];  
        if(page!=null)  
        {  
            String regx="<table.*?</table>";  
            Pattern pattern=Pattern.compile(regx);  
            Matcher matcher=pattern.matcher(page);  
            for(int i=0;i<101;i++)  
            {  
                if(matcher.find())  
                {  
                    if(i==0)  
                    {  
                        continue;  
                    }  
                    //���table�е�����  
                    String table_content=matcher.group().toString();  
                    String reg_URL="href=\"(.*?)\"";  
                    Pattern pattern_URL=Pattern.compile(reg_URL);  
                    Matcher matcher_URL=pattern_URL.matcher(table_content);  
                    String page_URL=null;  
                    if(matcher_URL.find())  
                    {  
                        page_URL=matcher_URL.group().toString();  
                    }  
                    page_URL=page_URL.substring(6);  
                    //�õ���URL  
                    page_URL=page_URL.substring(0,page_URL.length()-1);  
                    String reg_title="<a.+?href\\s*=\\s*[\"]?(.+?)[\"|\\s].+?>(.+?)</a>";  
                    Pattern patter_title=Pattern.compile(reg_title);  
                    Matcher matcher_title=patter_title.matcher(table_content);  
                    String page_title=null;  
                    if(matcher_title.find())  
                    {  
                        //�õ��˱���  
                        page_title=matcher_title.group().toString();  
                    }  
                    //��table_content����ȡ������  
                    String page_content = null;  
                    page_content = table_content.substring(table_content.lastIndexOf("</h3>")+5);  
                      
                    pageContent_list[i-1][0]=page_URL;  
                    pageContent_list[i-1][1]=page_title;  
                    pageContent_list[i-1][2]=page_content;  
                  }  
                }  
           }  
           return pageContent_list;  
    }  
    
    public static String[][] baidu(String key){
//    	String[][] result = parseHTML(key);
//    	for(int i=0;i<result.length;i++) {  
//            Log.e("00","��"+(i+1)+"�������");  
//            Log.e("00","URL:"+result[i][0]);  
//            Log.e("00","����:"+result[i][1]);  
//            Log.e("00","ժҪ:"+result[i][2]);  
//        }  
    	return null;
    }
}
