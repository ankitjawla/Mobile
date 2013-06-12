package edu.stevens.cs522;

import edu.stevens.cs522.CartDbAdapter.DatabaseHelper;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MyProvider extends ContentProvider {

	public CartDbAdapter ctDbAdapter;
	private DatabaseHelper cartDbHelper;
	private SQLiteDatabase cartDb;
	private static final String  DATABASE_NAME = "bookcart.db";
	private static final String TABLE_NAME = "cart";
    private static final String TAG = "MyContentProvider";
	private static final String DATABASE_CREATE = 
			"create table cart (_id integer primary key autoincrement, "
				+ "title text, author text, isbn text, price text);";
    
    public static final UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static{
            sMatcher.addURI("com.stevens.MyContentProvider","cart", CartDbAdapter.ITEM);
            sMatcher.addURI("com.stevens.MyContentProvider", "cart/#", CartDbAdapter.ITEM_ID);

    }
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		cartDb = cartDbHelper.getWritableDatabase();
		boolean del; 
		int delnum=0;
		switch (sMatcher.match(uri)) {
		case CartDbAdapter.ITEM:
			
			ctDbAdapter.deleteAll();
			getContext().getContentResolver().notifyChange(uri, null);
			return delnum;
		case CartDbAdapter.ITEM_ID:
			cartDb.delete(TABLE_NAME, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			return  delnum;			
		default:
			   throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues contentvalues) {
		cartDb = cartDbHelper.getWritableDatabase();
        long rowId = cartDb.insert(TABLE_NAME, "", contentvalues);
        if (rowId > 0) {
            Uri rowUri = ContentUris.appendId(CartDbAdapter.CONTENT_URI.buildUpon(), rowId).build();
            getContext().getContentResolver().notifyChange(rowUri, null);
            return rowUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
	}
	
	@Override
	public boolean onCreate() {
		this.cartDbHelper = new DatabaseHelper(this.getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder(); 
		cartDb = cartDbHelper.getWritableDatabase();                
        qb.setTables(TABLE_NAME);
		Cursor c = qb.query(cartDb, 
				projection, 
				selection, 
				selectionArgs, 
				null, 
				null, 
				sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		int updateCount = cartDb.update(TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
		return updateCount;
	}

}
