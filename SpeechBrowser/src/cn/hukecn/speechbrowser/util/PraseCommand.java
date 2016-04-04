package cn.hukecn.speechbrowser.util;

import java.util.List;

public class PraseCommand {
	public static final int Cmd_Original = -2;
	public static final int Cmd_Err = -1;
	public static final int Cmd_Search = 0;
	public static final int Cmd_Open = 1;
	public static final int Cmd_Weather = 2;
	public static final int Cmd_WeatherComCn = 12;
	public static final int Cmd_News = 3;
	public static final int Cmd_Mail = 4;
	public static final int Cmd_NewsNum = 5;
	public static final int Cmd_Location = 6;
	public static final int Cmd_Route = 7;
	public static final int Cmd_Exit = 8;
	public static final int Cmd_Mail_Home = 9;
	public static final int Cmd_Mail_InBox = 10;
	public static final int Cmd_Mail_MailContent = 11;
	public static final int Cmd_Other = 99;
	
	public static int prase(List<String> list)
	{
		String listStr = "";
		for(String temp:list)
		{
			listStr +=temp;
		}
		if(list.get(0).equals("��") || list.get(0).equals(""))
			return Cmd_Other;
		
		if(list.get(list.size() -1).equals("��"))
			list.remove(list.size()-1);
		
		if(listStr.indexOf("����") != -1)
		{
			//if(list.size() > 1)
				return Cmd_Weather;
			//else
			//	return Cmd_Err;
		}
		
		if(listStr.indexOf("����") != -1)
			return Cmd_Mail;
		
		if(listStr.indexOf("����") != -1)
			return Cmd_News;
		if(list.get(0).indexOf("����") != -1)
			return Cmd_Search;
		//if(list.get(0).indexOf("��") != -1)
			//return Cmd_Open;	
		if(list.get(list.size()-1).equals("��") &&list.get(0).equals("��")&&list.size() == 3)
			return Cmd_NewsNum;
		if(listStr.indexOf("��") != -1||listStr.indexOf("λ��") != -1)
			return Cmd_Location;
		if(listStr.indexOf("�ر�") != -1 || listStr.indexOf("�˳�") != -1)
			return Cmd_Exit;
		return 100;
	}
	
	public static int praseNewsIndex(List<String> list){
			String strNum = list.get(1);
			if(strNum.equals("һ"))
				return 1;
			if(strNum.equals("��"))
				return 2;
			if(strNum.equals("��"))
				return 3;
			if(strNum.equals("��"))
				return 4;
			if(strNum.equals("��"))
				return 5;
			if(strNum.equals("��"))
				return 6;
			if(strNum.equals("��"))
				return 7;
			if(strNum.equals("��"))
				return 8;
			if(strNum.equals("��"))
				return 9;
			if(strNum.equals("ʮ"))
				return 10;
			if(strNum.equals("ʮһ"))
				return 11;
			return Integer.parseInt(strNum);
	}
}
