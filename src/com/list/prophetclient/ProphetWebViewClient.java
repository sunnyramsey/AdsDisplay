package com.list.prophetclient;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.R.bool;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ProphetWebViewClient extends WebViewClient {
	private Context appContext;
	private long pageStartTime ;
	private boolean redictFinishTag;
	private WifiInfo curWifiInfo;
    private WifiManager wifi_service;
    private SignalStrength curSignal;
    private  PredictionClient predictionClient;
    private  Timer visitedTimer;
    private WebView webView;
    
    public WebInfo curWebInfo;
    public DelayRequestInfo curDelayRequestInfo;
    public ProphetInterface prophetInterface;
    
    
    private static Lock signal_lock = new ReentrantLock();
	
	public ProphetWebViewClient(Context context,ProphetInterface prophetInterface,WebView webView) {
		// TODO Auto-generated constructor stub
		this.appContext = context;
		this.prophetInterface = prophetInterface;
		this.webView = webView;
		this.webView.addJavascriptInterface(new JsInterface(), "dom");
		
		pageStartTime = 0;
		redictFinishTag = true;
		predictionClient = new PredictionClient(this);
		
		wifi_service = (WifiManager)appContext.getSystemService(Context.WIFI_SERVICE);
		TelephonyManager telephoneManager = (TelephonyManager)appContext.getSystemService(Context.TELEPHONY_SERVICE);  
		telephoneManager.listen(new SignalListener(),PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}
	
	public void setPredictServer(String address,int port)
	{
		predictionClient.setServerAddress(address, port);
	}
	

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		// TODO Auto-generated method stub
		Log.i("Prophet:browser","onPageStarted:"+url);

		//first pagestart event
		if(redictFinishTag == true)
		{
			if(visitedTimer != null)
			{
				visitedTimer.cancel();
				visitedTimer = null;
			}
			visitedTimer = new Timer();
			visitedTimer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(curDelayRequestInfo.isSend == false)
					{
						curDelayRequestInfo.delay = 18000;
						curDelayRequestInfo.sendDelayInfo();	
					}
				}
					
			}, 18000);
			
			curWebInfo = new WebInfo(url);
			
			//send request to server
			ServerRequestInfo serverRequestInfo = new ServerRequestInfo(url);
			getPhoneInfo(serverRequestInfo);		
			
			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			java.util.Date date = new java.util.Date(System.currentTimeMillis());
			
			if(curDelayRequestInfo!=null && curDelayRequestInfo.isSend == false)
			{
				//add to prediction client to do list;
				curDelayRequestInfo.isOld = true;
				Log.i("Prophet:browser","save last delay");
				predictionClient.toDoList.add(curDelayRequestInfo);
			}
			
			//create delayrequest object to save delay information
			curDelayRequestInfo = new DelayRequestInfo(serverRequestInfo.url, serverRequestInfo.signal,serverRequestInfo.wifi_name,
						serverRequestInfo.coarse_loc,serverRequestInfo.model,sDateFormat.format(date),serverRequestInfo.netType);
			
			curDelayRequestInfo.client = predictionClient;
			
			predictionClient.sendPredictRequest(serverRequestInfo.getJSONObject(),curDelayRequestInfo);
			

			curWebInfo.setInfoByDelayRequest(serverRequestInfo);
			
			Log.i("Prophet:browser","getStartTime");
			pageStartTime = System.currentTimeMillis();
			redictFinishTag = false;

		}
		
		super.onPageStarted(view, url, favicon);
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		// TODO Auto-generated method stub
		Log.i("Prophet:browser","onPageFinished:"+url);
		//after doupdatevisitedhistory event
		if(redictFinishTag == true)
		{
			Log.i("Prophet:browser","redirection is finished");
			prophetInterface.onRealPageFinished();
			if(curDelayRequestInfo.isSend == false)
			{
				if(curDelayRequestInfo.delay == 0)
				{
					curDelayRequestInfo.delay = System.currentTimeMillis() - pageStartTime;
				}
				curDelayRequestInfo.sendDelayInfo();
			}
			predictionClient.onPageFinished();
		}
		super.onPageFinished(view, url);
	}

	@Override
	public void doUpdateVisitedHistory(WebView view, String url,
			boolean isReload) {
		// TODO Auto-generated method stub
		Log.i("Prophet:browser","doUpdateVisitedHistory:"+url);
		if(redictFinishTag == false)
		{
			redictFinishTag = true;
		}	
		view.loadUrl("javascript:" + 
				"window.addEventListener('DOMContentLoaded', function() {" +
			        "window.dom.logDomContentLoaded();})"
			    );
		view.loadUrl("javascript:" + 
				"window.addEventListener('load', function() {" +
			        "window.dom.logOnLoad();})"
			    );
		super.doUpdateVisitedHistory(view, url, isReload);
	}
	
	class SignalListener extends PhoneStateListener
	{

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			// TODO Auto-generated method stub
			signal_lock.lock();
			try
			{
				curSignal = signalStrength;
			}
			finally
			{
				signal_lock.unlock();
			}
			super.onSignalStrengthsChanged(signalStrength);
		}
		
	}
	
	public void getPhoneInfo(ServerRequestInfo info)
	{
		int mobileNetType= -1;
		
		//Get Phone model
		info.model = android.os.Build.MODEL;
		Log.i("Prophet:phoneInfo","MODEL:"+info.model);
		//Get Network type
		ConnectivityManager connectMgr = (ConnectivityManager)appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = connectMgr.getActiveNetworkInfo();
		if(netinfo.getType() == ConnectivityManager.TYPE_WIFI)
		{
			//WIFI
			WifiInfo wifiInfo = wifi_service.getConnectionInfo();
			info.signal = wifiInfo.getRssi();
			info.wifi_name = wifiInfo.getSSID().replace("\"", "");
			info.netType = "WIFI";
			
			Log.i("Prophet:phoneInfo","WIFI:"+info.wifi_name);
			Log.i("Prophet:phoneInfo","signal:"+info.signal);
		}
		else if(netinfo.getType() == ConnectivityManager.TYPE_MOBILE){
			//Mobile
			mobileNetType = netinfo.getSubtype();
			info.netType = parseNetSubType(mobileNetType);
			info.wifi_name = "";
			info.signal = getSignalStrength(mobileNetType);
			Log.i("Prophet:phoneInfo","signal:"+info.signal);
		}
		
		//Get Coarse_location
		info.coarse_loc = getCoarseLocation(info);	
	}
	
	private String parseNetSubType(int subtype)
	{
		String type = "";
		switch (subtype) {
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
	
	private int getSignalStrength(int type)
	{
		int dbm = 0;
		if(type == TelephonyManager.NETWORK_TYPE_LTE)
		{
			String ssignal = curSignal.toString();
			String[] parts = ssignal.split(" ");
			int asu = Integer.parseInt(parts[8]);
			dbm = asu*2-113;
			Log.i("Prophet:phoneInfo","LTE:"+dbm);
			return dbm;
		}
		else if(type == TelephonyManager.NETWORK_TYPE_CDMA        
                || type == TelephonyManager.NETWORK_TYPE_1xRTT )
		{
			dbm = curSignal.getCdmaDbm();
			Log.i("Prophet:phoneInfo","CDMA:"+dbm);
			return dbm;
		}
		else if(type == TelephonyManager.NETWORK_TYPE_EVDO_0  
                || type == TelephonyManager.NETWORK_TYPE_EVDO_A 
                || type == TelephonyManager.NETWORK_TYPE_EVDO_B)
		{
		
			dbm = curSignal.getEvdoDbm();
			Log.i("Prophet:phoneInfo","EVDO:"+dbm);
			return dbm;
		}
		else if(curSignal.isGsm())
		{
			int asu = curSignal.getGsmSignalStrength();
			dbm = asu*2-113;
			Log.i("Prophet:phoneInfo","GSM:"+dbm);
			return dbm;
		}
		else{
			 dbm = 0;
		}
		return dbm;
	}
	
	private String getCoarseLocation(ServerRequestInfo requestInfo)
	{
		String info="NULL";
		TelephonyManager manager = (TelephonyManager)appContext.getSystemService(Context.TELEPHONY_SERVICE); 
		int type = manager.getNetworkType();
		requestInfo.net_type_base = type;
		
		try{
			GsmCellLocation gsm = ((GsmCellLocation) manager.getCellLocation());  
          if (gsm == null)  
          {  
              Log.e("Prophet:phoneInfo", "GsmCellLocation is null!!!");  
              return info;  
          }  
   
          int lac = gsm.getLac();  
          String mcc = manager.getNetworkOperator().substring(0, 3);  
          String mnc = manager.getNetworkOperator().substring(3, 5);  
          int cid = gsm.getCid();  
          //info = mcc+"/"+mnc+"/"+String.valueOf(lac)+"/"+cid;
          info = mcc+"/"+mnc+"/"+String.valueOf(lac);
          Log.i("Prophet:phoneInfo", "GSM:"+info);
          
//          List<NeighboringCellInfo> list = manager.getNeighboringCellInfo();  
//          int size = list.size();  
//          Log.i("Prophet:phoneInfo", "GSM-neighboring-size:"+String.valueOf(size));
//          for (int i = 0; i < size; i++) {  
//
//              CellIDInfo cellinfo = new CellIDInfo();
//              cellinfo.cellID = String.valueOf(list.get(i).getCid());   
//              curWebInfo.cellList.add(cellinfo);
//          }  
          return info;
		}catch (ClassCastException e)
		{
			Log.i("Prophet:phoneInfo","NOT GSM");
		}
		
		try{
			CdmaCellLocation cdma = (CdmaCellLocation) manager.getCellLocation();     
          if (cdma == null)  
          {  
              Log.e("Prophet:phoneInfo", "CdmaCellLocation is null!!!");  
              return info;  
          }  
            
          int lac = cdma.getNetworkId();  
          String mcc = manager.getNetworkOperator().substring(0, 3);  
          String mnc = String.valueOf(cdma.getSystemId());  
          int cid = cdma.getBaseStationId();
          //info = mcc+"/"+mnc+"/"+String.valueOf(lac)+"/"+cid;
          info = mcc+"/"+mnc+"/"+String.valueOf(lac);
          Log.i("Prophet:phoneInfo", "CDMA:"+info);
          
//          List<NeighboringCellInfo> list = manager.getNeighboringCellInfo();  
//          int size = list.size();  
//          Log.i("Prophet:phoneInfo", "CDMA-neighboring-size:"+String.valueOf(size));
//          for (int i = 0; i < size; i++) {  
//
//              CellIDInfo cellinfo = new CellIDInfo();
//              cellinfo.cellID = String.valueOf(list.get(i).getCid());    
//              curWebInfo.cellList.add(cellinfo);
//          }  
          
          return info;
		}catch (ClassCastException e)
		{
			Log.i("Prophet:phoneInfo","NOT CDMA");
		}

		return info;
	}
	
	class JsInterface
	{
		@JavascriptInterface
		public void logDomContentLoaded()
		{
			long curTime = System.currentTimeMillis();
			long domDelay = curTime - pageStartTime;
			if(curWebInfo != null)
			{
				curWebInfo.dom_delay = domDelay;
			}
			if(curDelayRequestInfo != null)
			{
				curDelayRequestInfo.delay = domDelay;
				curDelayRequestInfo.sendDelayInfo();
			}
			Log.i("Prophet:browser","ONDOM:"+domDelay);
			prophetInterface.onDomContentLoaded();

		}
		
		@JavascriptInterface
		public void logOnLoad() {
			// TODO Auto-generated method stub
			long curTime = System.currentTimeMillis();	
			long loadDelay = curTime - pageStartTime;
			if(curWebInfo != null)
			{
				curWebInfo.onload_delay = loadDelay;
			}
			if(curDelayRequestInfo != null)
			{
				if(curDelayRequestInfo.delay == 0)
				{
					curDelayRequestInfo.delay = loadDelay;
					curDelayRequestInfo.sendDelayInfo();
				}
				
			}
			Log.i("Prophet:browser","ONLOAD:"+loadDelay);
			prophetInterface.onLoad();

		}
		
	}
	
	public interface ProphetInterface{
		void onDomContentLoaded();
		void onLoad();
		void onPredictFinished();
		void onRealPageFinished();
	}
	



}
