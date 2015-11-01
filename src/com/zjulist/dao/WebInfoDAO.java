package com.zjulist.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WebInfoDAO {
	private DBOpenHandler helper;
	private SQLiteDatabase db;
	public WebInfoDAO(Context context)
	{
		helper = new DBOpenHandler(context);
	}
	public void add(Tb_webinfo tb_webinfo)
	{
		db = helper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("main_id", tb_webinfo.getMain_id());
		contentValues.put("dom", tb_webinfo.getDom());
		contentValues.put("load", tb_webinfo.getLoad());
		contentValues.put("timestamp", tb_webinfo.getTimestamp());
		contentValues.put("nettype", tb_webinfo.getNettype());
		contentValues.put("signal", tb_webinfo.getSignal());
		db.insert("webinfo", "signal", contentValues);
	}
	

}
