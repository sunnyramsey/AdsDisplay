package com.list.prophetclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.layout;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;


/*
 *  Deal with connection of prophet server
 * 
 */
public class PredictionClient {
	public long predictValue;
	private String serverAddress;
	private int port;
	
	private String serverData;
	private Socket socket;
	private ProphetWebViewClient webViewClient;
	
	public ArrayList<DelayRequestInfo> toDoList;
	
	public PredictionClient()
	{
		//serverAddress = "10.214.148.120";
		serverAddress = "165.124.182.177";
		port = 7778;
		toDoList = new ArrayList<DelayRequestInfo>();
	}
	
	public PredictionClient(ProphetWebViewClient webViewClient)
	{
		serverAddress = "10.214.148.120";
		//serverAddress = "165.124.182.177";
		port = 7778;
		toDoList = new ArrayList<DelayRequestInfo>();
		this.webViewClient = webViewClient;
	}
	
	
	public void setServerAddress(String address,int port)
	{
		this.serverAddress = address;
		this.port = port;
		Log.i("Predict:server change",address+":"+port);
	}
	public void sendPredictRequest(JSONObject requestInfo,DelayRequestInfo delayRequestInfo)
	{
		String requestString = "0";
		requestString = requestString + requestInfo.toString();
		PredictThread thread = new PredictThread(requestString,delayRequestInfo);
		thread.start();
	}
	
	public void sendDelayRequest(JSONObject delayInfo)
	{
		
		String delayString = "1";
		delayString = delayString + delayInfo.toString();
		DelayThread thread = new DelayThread(delayString);
		thread.start();
		
	}
	
	
	//add the rtts to current webinfo object
	public void insertRttInfo(String url,HashMap<String,Long> result)
	{
		//insert rtt info to curwebinfo
		boolean badvalue = false; 
		if(!url.equals(webViewClient.curWebInfo.url))
			return;
		Iterator<Map.Entry<String,Long>> iter = result.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,Long> entry = (Entry<String, Long>) iter.next(); 
			String host_url = entry.getKey();
			Long host_rtt = entry.getValue();
			if(host_rtt == 3000)
			{
				badvalue = true;
				webViewClient.curDelayRequestInfo.delay = 18000;			
			}
			String rtt_info = host_url+":"+host_rtt+"ms";
			webViewClient.curWebInfo.rttList.add(rtt_info);
		}
		if(webViewClient.curDelayRequestInfo.isSend == false && badvalue == true)
			webViewClient.curDelayRequestInfo.sendDelayInfo();
	}
	
	
	//deal with the undo list
	public void onPageFinished()
	{
		
		if(toDoList.size()!=0)
		{
			Log.i("Prophet:predict-old", "deal to do list");
			for(int i=0;i<toDoList.size();i++)
			{
				Log.i("Predict:OLD",toDoList.get(i).url);
				toDoList.get(i).getRtt();
			}
		}
	}
	
	class PredictThread extends Thread
	{
		private String requestString;
		private DelayRequestInfo delayRequestInfo;
		public PredictThread(String request,DelayRequestInfo delayRequestInfo) {
			// TODO Auto-generated constructor stub
			this.requestString = request;
			this.delayRequestInfo = delayRequestInfo;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				delayRequestInfo.pingLock.lock();
				
				socket = new Socket(serverAddress, port);
				
				//init socket;
				socket.setSoTimeout(10000);
				OutputStream os=socket.getOutputStream();
				InputStreamReader is= new InputStreamReader(socket.getInputStream());
				PrintStream ps=new PrintStream(os);
				BufferedReader br = new BufferedReader(is);
			    
				//send request to server
				ps.print(this.requestString+"\r\n");
				
				//recv info from server
				if((serverData=br.readLine())!=null)
				{
					Log.i("Prophet:predict",serverData);
				}
				
				Log.i("Prophet:predict","predict finish");
				
				if(serverData!= null)
				{
				try {
					JSONObject serverObject = new JSONObject(serverData);
					if(webViewClient != null)
					{
//						UIThread thread = new UIThread(String.valueOf(serverObject.getInt("delay")));
//						mainActivity.runOnUiThread(thread);
					}
					int predict_value = serverObject.getInt("predict");
					webViewClient.curWebInfo.predict_value = predict_value;
				    webViewClient.prophetInterface.onPredictFinished(predict_value);
					
					String predictHistoryString = serverObject.getString("delay");
					if(predictHistoryString.equals("None"))
					{
						//mainActivity.curWebInfo.predict_value = 0;
					}else{
						try{
							String src_data = predictHistoryString.substring(1, predictHistoryString.length()-2);
							Log.i("Prophet:predict-history-src",src_data);
							String[] value_str = src_data.split(",");
							//int[] values = new int[value_str.length];
							for(int i=0;i<value_str.length;i++)
							{
								webViewClient.curWebInfo.historyList.add(value_str[i]+"ms");
								//values[i] = Integer.parseInt(value_str[i]);
							}
							//Arrays.sort(values);
							//mainActivity.curWebInfo.predict_value = values[values.length/2];
						}catch(Exception e)
						{
							Log.e("Prophet:predict-history-src","Parse History Value Failed!");
						}
					}	
					delayRequestInfo.addHosts(serverObject);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.e("Prophet:predict","server data error");
					e.printStackTrace();
				}
				}
			
				socket.close();			
				delayRequestInfo.getRtt();
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				Log.e("Prophet:predict","unknowhost");
				e.printStackTrace();
			} catch (SocketTimeoutException e) {
				// TODO: handle exception
				Log.e("Prophet:predict","socket timeout");
				try {
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				delayRequestInfo.pingLock.unlock();
			}
			
			
		}

	}
	
	class DelayThread extends Thread
	{
		public String delayRequest;
		public DelayThread(String request) {
			// TODO Auto-generated constructor stub
			this.delayRequest = request;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				socket = new Socket(serverAddress, port);
				
				//init socket;
				OutputStream os=socket.getOutputStream();
				PrintStream ps=new PrintStream(os);
			    
				//send delay to server
				ps.print(delayRequest+"\r\n");
				
				Log.i("Prophet:predict","send delay finish");
				socket.close();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				Log.e("Prophet:predict","unknowhost");
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}


}
