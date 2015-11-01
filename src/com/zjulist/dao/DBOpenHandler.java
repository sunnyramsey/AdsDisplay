package com.zjulist.dao;

import android.R.integer;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHandler extends SQLiteOpenHelper {

	public DBOpenHandler(Context context) {
		super(context, DBNAME,null,VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("create table urls(id integer primary key AUTOINCREMENT,url varchar(100))");
		db.execSQL("create table headurl(id integer primary key AUTOINCREMENT,main_id interger,url varchar(100),FOREIGN KEY(main_id) REFERENCES urls(id))");
		db.execSQL("create table webinfo(id integer primary key AUTOINCREMENT,main_id,dom interger,load integer,timestamp varchar(30),nettype varchar(10),signal integer,FOREIGN KEY(main_id) REFERENCES urls(id))");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	private static final int VERSION = 1;
	private static final String DBNAME="weblog.db";

}
