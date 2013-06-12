/*********************************************************************

    Content provider for peer information for the chat program.
	We record host and port number for their chat programs
	as well as their locations.

    Copyright (c) 2012 Stevens Institute of Technology

**********************************************************************/

package edu.stevens.cs522.chat.providers;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/*
 * @author dduggan
 * 
 * Derived from the NotesList sample application
 *
 */
public class ChatroomProvider extends ContentProvider {
	
	private final static String TAG = ChatroomProvider.class.getCanonicalName();
	
	private static final String DATABASE_NAME = "chat.db";
	private static final int DATABASE_VERSION = 1;
	private static final String chatroom_TABLE_NAME = "chatrooms";
	
	private static HashMap<String, String> chatroomProjectionMap;
	
	private static final int CHATROOMS = 1;
	private static final int CHATROOM = 2;
	
	private static final UriMatcher uriMatcher;
	
	/*
	 * helper class to open, create, and upgrade the database file
	 */
	public static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d("tables!!!!!!!!$$$$$$$$$$$$$$$$$$$$$$$$", "_____________________________");
			db.execSQL("CREATE TABLE " + chatroom_TABLE_NAME + " ("
					+ ChatContent.Chatrooms._ID + " INTEGER PRIMARY KEY,"
					+ ChatContent.Chatrooms.NAME + " TEXT,"
					+ ChatContent.Chatrooms.IP + " TEXT,"
					+ ChatContent.Chatrooms.PORT + " INTEGER,"
					+ ChatContent.Chatrooms.OWNER + " TEXT "
					+ ");");
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + chatroom_TABLE_NAME);
			onCreate(db);
		}
	}
	
	private DatabaseHelper openHelper;

	@Override
	public boolean onCreate() {
		openHelper = new DatabaseHelper(getContext());
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch (uriMatcher.match(uri)) {
		case CHATROOMS:
			qb.setTables(chatroom_TABLE_NAME);
			qb.setProjectionMap(chatroomProjectionMap);
			break;
			
//		case PEER_ID:
//			qb.setTables(chatroom_TABLE_NAME);
//			qb.setProjectionMap(chatroomProjectionMap);
//			qb.appendWhere(ChatContent.Chatrooms._ID + "=" + uri.getPathSegments().get(1));
//			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		/*
		 * Use default sort order if not specified
		 */
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = ChatContent.Chatrooms.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}
	
		/*
		 * Query the database
		 */
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		
		/*
		 * Tell the cursor what uri to watch (for data changes)
		 */
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case CHATROOMS:
			return ChatContent.Chatrooms.CONTENT_TYPE;
			
//		case PEER_ID:
//			return ChatContent.Chatrooms.CONTENT_ITEM_TYPE;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (uriMatcher.match(uri) != CHATROOMS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		
		// Make sure fields are set
		if (values.containsKey(ChatContent.Chatrooms.NAME) == false) {
			values.put(ChatContent.Chatrooms.NAME, "Unknown");
		}
		if (values.containsKey(ChatContent.Chatrooms.IP) == false) {
			values.put(ChatContent.Chatrooms.IP, "1.1.1.1");
		}
		if (values.containsKey(ChatContent.Chatrooms.PORT) == false) {
			values.put(ChatContent.Chatrooms.PORT, 6666);
		}
		if (values.containsKey(ChatContent.Chatrooms.OWNER) == false) {
			values.put(ChatContent.Chatrooms.OWNER, "default");
		}

				
		SQLiteDatabase db = openHelper.getWritableDatabase();
		long rowId = db.insert(chatroom_TABLE_NAME, ChatContent.Chatrooms.NAME, values);
		if (rowId > 0) {
			Uri ChatroomUri = ContentUris.withAppendedId(ChatContent.Chatrooms.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(ChatroomUri, null);
			return ChatroomUri;
		}
	
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri)) {
		case CHATROOMS:
			count = db.delete(chatroom_TABLE_NAME, selection, selectionArgs);
			break;
			
		case CHATROOM: 
			String id = uri.getPathSegments().get(1);
			count = db.delete(chatroom_TABLE_NAME, ChatContent.Chatrooms._ID
					+ "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" 
							+ selection + ")" : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
	
		int count;
		switch (uriMatcher.match(uri)) {
		case CHATROOMS:
			count = db.update(chatroom_TABLE_NAME, values, selection, selectionArgs);
			break;
			
		case CHATROOM:
			String id = uri.getPathSegments().get(1);
			count = db.update(chatroom_TABLE_NAME, values, ChatContent.Chatrooms._ID
					+ "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" 
							+ selection + ")" : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(ChatContent.Chatrooms.AUTHORITY, null, CHATROOMS);
		uriMatcher.addURI(ChatContent.Chatrooms.AUTHORITY, "#", CHATROOM);
		
		chatroomProjectionMap = new HashMap<String, String>();
		chatroomProjectionMap.put(ChatContent.Chatrooms._ID, ChatContent.Chatrooms._ID);
		chatroomProjectionMap.put(ChatContent.Chatrooms.NAME, ChatContent.Chatrooms.NAME);
		chatroomProjectionMap.put(ChatContent.Chatrooms.IP, ChatContent.Chatrooms.IP);
		chatroomProjectionMap.put(ChatContent.Chatrooms.PORT, ChatContent.Chatrooms.PORT);
		chatroomProjectionMap.put(ChatContent.Chatrooms.OWNER, ChatContent.Chatrooms.OWNER);
	}

}
