package com.tommasocodella.androdmtp.services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PersistentStorage extends SQLiteOpenHelper{
	private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "AndroDMTPPersistentStorage";
    
    public static final String PARAMS_TABLE = "params";
    
	public PersistentStorage(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + PARAMS_TABLE + "(paramID INTEGER PRIMARY KEY, param VARCHAR(45), value VARCHAR(128));");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}


}
