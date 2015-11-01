package com.zjulist.dao;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UrlsDAO {
	private DBOpenHandler helper;
	private SQLiteDatabase db;
	public UrlsDAO(Context context)
	{
		helper = new DBOpenHandler(context);
	}
	
	public long add(Tb_urls tb_urls)
	{
		db = helper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("url", tb_urls.getUrl());
		long id = db.insert("urls", "url", contentValues);
		return id;
	}
	
	public int findId(String url)
	{
		int id = -1;
		db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select id from urls where url = ?", new String[]{url});
		if(cursor.moveToNext())
		{
			id = cursor.getInt(0);
		}
		return id;
	}

}
