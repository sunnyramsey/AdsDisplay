package com.list.prophetclient;

import java.util.ArrayList;

import com.zjulist.dao.HeadUrlDAO;
import com.zjulist.dao.Tb_headUrl;
import com.zjulist.dao.Tb_urls;
import com.zjulist.dao.Tb_webinfo;
import com.zjulist.dao.UrlsDAO;
import com.zjulist.dao.WebInfoDAO;

import android.R.integer;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class WebInfo {
	public String url;
	public long dom_delay;
	public long onload_delay;
	public String model;
	public String coarse_loc;
	public String wifi_name;
	public boolean isSaved;
	public Context context;
	public String netType;
	public int signal;
	public int predict_value;
	public long content_len;
	public ArrayList<String> rttList;
	public ArrayList<String> historyList;
	
	public WebInfo(String url)
	{
		this.url = url;
		isSaved = false;
		dom_delay = 0;
		onload_delay = 0;
		rttList = new ArrayList<String>();
		historyList = new ArrayList<String>();
		predict_value = 0;
		
	}
	
	public void setInfoByDelayRequest(ServerRequestInfo info)
	{
		this.model = info.model;
		this.signal = info.signal;
		this.coarse_loc = info.coarse_loc;
		this.wifi_name = info.wifi_name;
		this.netType = parseNetType(info.net_type_base);
	}
	
	public void showInfo()
	{
		Log.i("WEBINFO",url);
		//Log.i("WEBINFO","start_time:"+start_time+";end_time:"+end_time);
		Log.i("WEBINFO","DOM:"+dom_delay);
		Log.i("WEBINFO","LOAD:"+onload_delay);
		
	}
	public String parseNetType(int type_int)
	{
		String type = "";
		switch (type_int) {
		case TelephonyManager.NETWORK_TYPE_CDMA:
			type="CDMA";					
			break;
		case TelephonyManager.NETWORK_TYPE_EDGE:
			type="EDGE";					
			break;
		case TelephonyManager.NETWORK_TYPE_EHRPD:
			type="EHRPD";					
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			type="EVDO0";					
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			type="EVDOA";					
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			type="EVDOB";					
			break;
		case TelephonyManager.NETWORK_TYPE_GPRS:
			type="GPRS";					
			break;
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			type="HSDPA";					
			break;
		case TelephonyManager.NETWORK_TYPE_HSPA:
			type="HSPA";					
			break;
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			type="HSPAP";					
			break;
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			type="HSUPA";
			break;
		case TelephonyManager.NETWORK_TYPE_IDEN:
			type="IDEN";					
			break;
		case TelephonyManager.NETWORK_TYPE_LTE:
			type="LTE";					
			break;
		case TelephonyManager.NETWORK_TYPE_UMTS:
			type="UMTS";					
			break;
		default:
			type="UNKNOW";
			break;
		}
		return type;
	}
	public String toString()
	{
		String info= "";
		info += ("URL:" +this.url+"\r\n");
		info += ("COARSE_LOCTION:" +this.coarse_loc+"\r\n");
		info += ("BASE_NET_TYPE:" +this.netType+"\r\n");
		info += ("SIGNAL:" +this.signal+"dbm\r\n");
		if(dom_delay != 0)
		{
			info += ("DOMCONTENTLOADED:" +this.dom_delay+"ms\r\n");
		}
		if(onload_delay != 0)
		{
			info += ("ONLOAD:" +this.onload_delay+"ms\r\n");
		}
		if(predict_value != 0)
		{
			info += ("PREDICT:" +this.predict_value+"s\r\n");
		}
//		if(cellList.size()!=0)
//		{
//			info += "CELL:";
//			for(int i=0;i<cellList.size();i++)
//			{
//				info += (cellList.get(i).cellID+";");
//			}
//			info += "\r\n";
//		}
		if(content_len != 0)
		{
			info += "CONTENT-LENGTH:"+content_len+"\r\n";
		}
		
		return info;
	}
//	public void saveToDB()
//	{
//		if(isSaved == true)
//		{
//			Log.i("DB","already save");
//			return;
//		}
//		if(url.equals(""))
//		{
//			return;
//		}
//		else {
//			isSaved = true;
//			Log.i("DB:url",this.url);
//			Tb_urls tb_urls = new Tb_urls(this.url);
//			UrlsDAO urlsDAO = new UrlsDAO(context);
//			long exist_id = urlsDAO.findId(this.url);
//			long main_id;
//			if(exist_id == -1)
//			{
//				main_id = urlsDAO.add(tb_urls);
//				Log.i("DB:new_id",""+main_id);
//				if(this.head_src.size()!= 0)
//				{
//					HeadUrlDAO headUrlDAO = new HeadUrlDAO(context);
//					for(int i=0;i<this.head_src.size();i++)
//					{
//						Log.i("DB",this.head_src.get(i));
//						Tb_headUrl tb_headUrl= new Tb_headUrl();
//						tb_headUrl.setMain_id(main_id);
//						tb_headUrl.setUrl(head_src.get(i));
//						headUrlDAO.add(tb_headUrl);
//					}
//				}
//				else {
//					Log.i("DB","NONE");
//				}
//			}
//			else {
//				main_id = exist_id;
//				Log.i("DB:exist_id",""+main_id);
////				for(int i=0;i<this.head_src.size();i++)
////				{
////					Log.i("DB",this.head_src.get(i));
////				}
//			}
//			
//			Tb_webinfo tb_webinfo = new Tb_webinfo();
//			tb_webinfo.setMain_id(main_id);
//			tb_webinfo.setDom((int)dom_delay);
//			tb_webinfo.setLoad((int)onload_delay);
//			tb_webinfo.setTimestamp(timestamp);
//			tb_webinfo.setNettype(netType);
//			tb_webinfo.setSignal(signal);
//			Log.i("DB","DOM:"+dom_delay+";LOAD:"+onload_delay);
//			WebInfoDAO webInfoDAO = new WebInfoDAO(context);
//			webInfoDAO.add(tb_webinfo);
//		
//	
//		}
//	}
}
