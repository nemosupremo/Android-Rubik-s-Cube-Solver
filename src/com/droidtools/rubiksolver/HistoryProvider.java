package com.droidtools.rubiksolver;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class HistoryProvider extends ContentProvider {

	public static final String HISTORY_TABLE_NAME = "history";

	private static HashMap<String, String> sHistroyMap;

	private static final int HISTORY = 1;
	private static final int HISTORY_ID = 2;

	private static final UriMatcher sUriMatcher;

	public static final String ID = "hid";
	public static final String NAME = "name";
	public static final String MOVES = "moves";
	public static final String STATE = "state";
	public static final String COLORS = "colors";
	//public static final String COLOR1 = "color1";
	//public static final String COLOR2 = "color2";

	public static final String DEFAULT_SORT_ORDER = "hid ASC";

	public static final String AUTHORITY = "com.droidtools.rubiksolver.HistoryProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/history");

	public static final String[] PROJECTION = new String[] {
			ID, // 0
			NAME, // 1
			MOVES, // 1
			STATE, // 1
			COLORS,
	};

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, "history", HISTORY);
		sUriMatcher.addURI(AUTHORITY, "history/#", HISTORY_ID);

		sHistroyMap = new HashMap<String, String>();
		sHistroyMap.put(ID, ID);
		sHistroyMap.put(NAME, NAME);
		sHistroyMap.put(MOVES, MOVES);
		sHistroyMap.put(STATE, STATE);
		sHistroyMap.put(COLORS, COLORS);
	}

	private HistoryDatabaseHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		mOpenHelper = new HistoryDatabaseHelper(getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case HISTORY:
			count = db.delete(HISTORY_TABLE_NAME, selection, selectionArgs);
			break;

		case HISTORY_ID:
			String historyId = uri.getPathSegments().get(1);
			count = db.delete(HISTORY_TABLE_NAME, ID
					+ "="
					+ historyId
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case HISTORY:
			return "vnd.android.cursor.dir/vnd.droidtools.rubiksolver.history";

		case HISTORY_ID:
			return "vnd.android.cursor.item/vnd.droidtools.rubiksolver.history";

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// Validate the requested uri
		if (sUriMatcher.match(uri) != HISTORY) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		if (values.containsKey(MOVES) == false) {
			throw new IllegalArgumentException("No moves set.");
		}
		if (values.containsKey(STATE) == false) {
			throw new IllegalArgumentException("No state set.");
		}
		if (values.containsKey(COLORS) == false) {
			throw new IllegalArgumentException("No colors set.");
		}
		if (values.containsKey(NAME) == false) {
			throw new IllegalArgumentException("No name set.");
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(HISTORY_TABLE_NAME, COLORS, values);
		if (rowId > 0) {
			Uri feedUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(feedUri, null);
			return feedUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (sUriMatcher.match(uri)) {
		case HISTORY:
			qb.setTables(HISTORY_TABLE_NAME);
			qb.setProjectionMap(sHistroyMap);
			break;

		case HISTORY_ID:
			qb.setTables(HISTORY_TABLE_NAME);
			qb.setProjectionMap(sHistroyMap);
			qb.appendWhere(ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case HISTORY:
			count = db.update(HISTORY_TABLE_NAME, values, selection,
					selectionArgs);
			break;

		case HISTORY_ID:
			String feedId = uri.getPathSegments().get(1);
			count = db.update(HISTORY_TABLE_NAME, values, ID
					+ "="
					+ feedId
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
