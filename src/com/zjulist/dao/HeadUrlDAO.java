package com.zjulist.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class HeadUrlDAO {
	private DBOpenHandler helper;
	private SQLiteDatabase db;
	public HeadUrlDAO(Context context)
	{
		helper = new DBOpenHandler(context);
	}
	public void add(Tb_headUrl tb_headUrl)
	{
		db = helper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("main_id", tb_headUrl.getMain_id());
		contentValues.put("url",tb_headUrl.getUrl());
		db.insert("headurl", "main_id", contentValues);
	}

}
