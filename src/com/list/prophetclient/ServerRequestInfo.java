package com.list.prophetclient;

import org.json.JSONException;
import org.json.JSONObject;

import android.R.anim;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/*
    Save the information for predict request.
*/

public class ServerRequestInfo {
	public String url;
	public int signal;
	public String wifi_name;
	public String coarse_loc;
	public String model;
	public int net_type_base;
	public String netType;
	
	public ServerRequestInfo(String url)
	{
		this.url = url;
	}
	
	public JSONObject getJSONObject()
	{
		JSONObject object = new JSONObject();
		try {
			object.put("url", url);
			object.put("signal",String.valueOf(signal));
			object.put("wifi_name",wifi_name);
			object.put("coarse_location", coarse_loc);
			object.put("model", model);
			object.put("net_type", netType);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("PhoneInfo",object.toString());
		return object;
	}

	

}
