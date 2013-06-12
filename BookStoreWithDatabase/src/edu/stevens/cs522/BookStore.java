package edu.stevens.cs522;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.app.LoaderManager;

public class BookStore extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	static final private int SEARCH_REQUEST = 0;
	static final private int CHECKOUT_REQUEST = SEARCH_REQUEST + 1;

	private CartDbAdapter cartDbAdapter;
	
	long position;
	private SimpleCursorAdapter adapter;
	private CursorLoader loader;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the layout
		setContentView(R.layout.cart);

		// Open the database and display its contents in the list view
		cartDbAdapter = new CartDbAdapter(this);
		cartDbAdapter.open();
		fillData();
	}

	private void fillData() {
		String[] to = new String[] { CartDbAdapter.KEY_TITLE, CartDbAdapter.KEY_AUTHOR };
        int[] from = new int[] { R.id.cart_row_title, R.id.cart_row_author };
        ListView listview = new ListView(this);
        
        getLoaderManager().initLoader(0, null, this);
        // Now create a list adaptor that encapsulates the result of a DB query
        adapter = new SimpleCursorAdapter(
                this,       // Context.
                R.layout.cart_row,  // Specify the row template to use 
                null,          // Cursor encapsulates the DB query result.
                to, 		// Array of cursor columns to bind to.
                from,      // Parallel array of which layout objects to bind to those columns.
        		0);
        // Bind to our new adapter.
        listview.setAdapter(adapter);
        setContentView(listview);
        listview.setOnItemClickListener(new ClickListView());
	}
	
	public class ClickListView implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) { 			
			if(arg0.getTag() != null){				
				position = -1;
               ((ListView)arg0).setTag(null);
			}
			else{
				arg0.setTag(arg1);
				position=arg3;
				}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		Intent i;

		switch (item.getItemId()) {
		case (R.id.main_search):
			i = new Intent(this, Search.class);
			startActivityForResult(i, SEARCH_REQUEST);
			return true;
		case (R.id.main_checkout):
			 Cursor countCursor = getContentResolver().query(CartDbAdapter.CONTENT_URI,
		                new String[] {"count(*) AS count"},
		                null,
		                null,
		                null);
		        countCursor.moveToFirst();
		        int count = countCursor.getInt(0);
			if (count>0) {
				i = new Intent(this, Checkout.class);
				i.putExtra("count", count);
				startActivityForResult(i,CHECKOUT_REQUEST);
			} else {
				Toast.makeText(this, "Empty cart!", Toast.LENGTH_SHORT).show();
			}
			return true;
		case (R.id.main_delete):
		String where = "_id =" + position;  
		Uri uri=ContentUris.withAppendedId(CartDbAdapter.CONTENT_URI,position);
		ContentResolver resolver = getContentResolver();
		if(position!=-1)
			{
			if (adapter!=null) {
		    resolver.delete(uri, where, null);
		    getLoaderManager().restartLoader(0, null, this);
		    fillData();
		    return true;
		     }	
			else {
				Toast.makeText(this, "no item chosen!", Toast.LENGTH_SHORT).show();
				return false;
			}
			}
		return false;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		switch (requestCode) {
		case SEARCH_REQUEST:
			switch (resultCode) {
			case RESULT_OK:
				getLoaderManager().restartLoader(0, null, this);
				fillData();
				return;
			case RESULT_CANCELED:
				return;
			}
		case CHECKOUT_REQUEST:
			switch (resultCode) {
			case RESULT_OK:	
	    		cartDbAdapter.deleteAll();
	    		fillData();
				return;
			case RESULT_CANCELED:
				return;	
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		if (adapter!=null) {			
			  loader = new CursorLoader(this,
					  CartDbAdapter.CONTENT_URI, null, null, null, null);
		}
			  return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		this.adapter.swapCursor(arg1);
        adapter.swapCursor(arg1);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		this.adapter.changeCursor(null);
	}
}