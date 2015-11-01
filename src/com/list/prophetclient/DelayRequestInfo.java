package com.list.prophetclient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.bool;
import android.R.integer;
import android.util.Log;


/*
 * Save the delay information;
 * Ping all related hosts;
 * 
 */

public class DelayRequestInfo {
	public String url;
	public int signal;
	public long delay;
	public String wifi_name;
	public String coarse_loc;
	public String model;
	public String timestamp;
	public String netType;
	public ArrayList<String> hosts;
	public HashMap<String,Long> rtts;
	
	private final ReentrantLock lock = new ReentrantLock();
	public final ReentrantLock pingLock = new ReentrantLock();
	private ExecutorService pingThreadService;

	public PredictionClient client;
	public boolean isSend;
	public boolean isPingFinished;
	public boolean isOld;
	
	public DelayRequestInfo(String url,int signal,String wifi_name,String coarce_loc,String model,String timestamp,String netType)
	{
		this.url = url;
		this.signal = signal;
		this.wifi_name = wifi_name;
		this.coarse_loc = coarce_loc;
		this.model = model;
		this.timestamp = timestamp;
		this.netType = netType;
		this.hosts = new ArrayList<String>();
		this.rtts = new HashMap<String, Long>();
		this.isSend = false;
		this.isPingFinished = false;
		this.isOld = false;
	}
	
	public void addHosts(JSONObject info)
	{
		JSONArray array;
		try {
			array = info.getJSONArray("hosts");
			for(int i=0;i<array.length();i++)
			{
				JSONObject obj;
				obj = array.getJSONObject(i);
				hosts.add(obj.getString("url"));
				//Log.i("Predict:host",obj.getString("url"));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.i("Predict:host","No hosts return");
			e.printStackTrace();
		}
		
	}
	
	public void getRtt()
	{
		new Thread()
		{
			public void run()
			{
				if(isPingFinished == true && isOld == true)
				{
					client.sendDelayRequest(getJSONObject());
					return ;
				}
					
				pingLock.lock();
				try {
					//pingThreadService=Executors.newCachedThreadPool();
					pingThreadService  = Executors.newFixedThreadPool(4);
					for(int i=0;i<hosts.size();i++)
					{
						PingThread thread=new PingThread(hosts.get(i));
					    pingThreadService.execute(thread);
					}
					Log.i("Prophet:predict-ping","PING-WAIT");
					try {
						 pingThreadService.shutdown();
					     pingThreadService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.i("Prophet:predict-ping","PING-FINISHED");
					isPingFinished = true;
					
					//if this object is not the current webpage,send the delay to server
					if(isOld == false){
						client.insertRttInfo(url, rtts);
					}
					else {
						client.sendDelayRequest(getJSONObject());
					}
					
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					Log.e("Prophet:predict-ping","PING failed");
				}
				finally
				{
					isPingFinished = true;
					pingLock.unlock();
				}
				
			}
		}.start();
	}
	
	public void sendDelayInfo()
	{
		new Thread(){
			public void run()
			{
				if(isSend == true)
					return;
				else {
					isSend = true;
					while(isPingFinished == false)
					{
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							Log.e("Predict","Send wait error");
							e.printStackTrace();
						}
					}
					if(delay>18000)
					{
						//force decrease to 18000 
						delay = 18000;
					}
					client.sendDelayRequest(getJSONObject());
				}
				
			}
		}.start();

	}
	
	public JSONObject getJSONObject()
	{
		JSONObject object = new JSONObject();
		try {
			object.put("url",this.url);
			object.put("signal",String.valueOf(this.signal));
			object.put("wifi_name",this.wifi_name);
			object.put("coarse_location",this.coarse_loc);
			object.put("model",this.model);
			if(delay == 0)
			{
				object.put("delay", 18000);
			}
			else
			{
				object.put("delay",this.delay);
			}			
			object.put("timestamp",this.timestamp);
			object.put("net_type",this.netType);
			
			JSONArray array = new JSONArray();
			Iterator<Map.Entry<String,Long>> iter = rtts.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String,Long> entry = (Entry<String, Long>) iter.next(); 
				String host_url = entry.getKey();
				Long host_rtt = entry.getValue();
				JSONObject array_item = new JSONObject();
				array_item.put(host_url,String.valueOf(host_rtt));
				array.put(array_item);
			}
			
			object.put("rtts", array);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e("Predict:info","Parse Info failed");
			e.printStackTrace();
		}
		
		Log.i("Predict:delay",object.toString());
		return object;
	}
	
	private InetAddress parseHostName(String hostName)
	{
		try {
			return InetAddress.getByName(hostName);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	private class PingThread extends Thread{
		
		private String src;
		
		public PingThread(String src)
		{
			this.src=src;
		}
		
		public void run()
		{
			Log.i("Predict:ping-src",src);
			try{
				Integer port = 0;
				String host = "";
				if(src.startsWith("http"))
				{
					port = 80;
					host = src.substring(7,src.length());
				}
				else if(src.startsWith("https"))
				{
					port = 443;
					host = src.substring(8,src.length());
				}
				else {
					Log.i("Predict:ping","error host port");
					return;
				}
				InetAddress address=parseHostName(host);
			//Log.i("Predict:ip", host+":"+address.getHostAddress());
			
				if(address!=null)
				{
					InetSocketAddress socketAddress=new InetSocketAddress(address,port);
			    
					long[] times=new long[3];
					try {
        	    	  for(int i=0;i<3;i++)
        	    	  {
        	    	  Socket socket=new Socket();
        	    	  long startTime=System.currentTimeMillis();
        		      //socket.connect(socketAddress);
        	    	  long connectTime = 3000;
        	    	  try{
        	    		  socket.connect(socketAddress, 3000);
                          long endTime=System.currentTimeMillis();
                          connectTime=endTime-startTime;
                          socket.close();
        	    	  }catch(SocketTimeoutException e)
        	    	  {
        	    		  Log.e("Predict:ping-timeout","set ++++ timeout");
        	    	  }
        	    	     times[i]=connectTime;
        	    	  }
        	    	  
        	    	  Arrays.sort(times);
        	    	  long finalTime=times[1];
        	    	  //Log.i("Predict:ping",String.valueOf(finalTime));
        	    	  Log.i("Predict:ping-finish-one",src+String.valueOf(finalTime));
                      try {
  		            	   lock.lock();
  		                   rtts.put(src,finalTime);
					   } catch (Exception e) {
							// TODO: handle exception
							Log.i("Predict:ping","LOCK-ERROR");
							e.printStackTrace();
					   }
  		               finally{
  		            	   lock.unlock();
  		               }
                  
					}
					catch(IllegalArgumentException e) {
        		        Log.i("Predict:ping","invalid address"+src);
        	         	rtts.put(src, 0L);
					}
					catch (IOException e) {
				        // TODO Auto-generated catch block
        		        Log.i("Predict:ping","connect error"+src);
        		        rtts.put(src, 0L);
        		        e.printStackTrace();
			        }
				}
			}catch(Exception e)
			{
				Log.e("Predict:ping","ping thread error");
			}

		}
	}
	
}
