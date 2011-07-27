package com.droidtools.rubiksolver;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryDatabaseHelper extends SQLiteOpenHelper {
	private static final int VERSION = 7;
	private static final String NAME = "droidtools.rubik.history.db";
	

	public HistoryDatabaseHelper(Context context) {
		super(context, NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + HistoryProvider.HISTORY_TABLE_NAME + " (" 
				+ HistoryProvider.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ HistoryProvider.NAME + " TEXT NOT NULL," 
				+ HistoryProvider.MOVES  + " BLOB NOT NULL," 
				+ HistoryProvider.STATE  + " BLOB NOT NULL," 
				+ HistoryProvider.COLORS  + " BLOB NOT NULL" 
				+ ");");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + HistoryProvider.HISTORY_TABLE_NAME);
		onCreate(db);
	}

}
