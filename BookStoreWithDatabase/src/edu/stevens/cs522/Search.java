package edu.stevens.cs522;

import edu.stevens.cs522.CartDbAdapter.DatabaseHelper;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class Search extends FragmentActivity {
	
	/*
	 * For now, search just adds the specified book to the shopping cart.
	 */
	//static final private int MENU_BUY = Menu.FIRST;
	//static final private int MENU_CANCEL = Menu.FIRST+1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.search, menu);
		// TODO: Add menu for BUY and CANCEL
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		Intent intent = new Intent(this, BookStore.class);
		switch (item.getItemId()) {
		case (R.id.search_buy):
			buyBook();
			setResult(RESULT_OK, intent);
			finish();
			return true;
		case (R.id.search_cancel):
			setResult(RESULT_CANCELED, intent);
			finish();
			return true;
			// TODO: Add CANCEL option handling.
		}
		return false;
	}
	
	public void buyBook(){
		/*
		 * Add the specified book to the shopping cart.
		 */
		
		EditText titleText = (EditText) findViewById(R.id.search_title);
		EditText authorText = (EditText) findViewById(R.id.search_author);
		EditText isbnText = (EditText) findViewById(R.id.search_isbn);
		EditText priceText = (EditText) findViewById(R.id.search_price);
	
		String title = titleText.getText().toString();
		String author = authorText.getText().toString();
		String isbn = isbnText.getText().toString();
		String price = priceText.getText().toString();
		
		//ctAdapter = new CartDbAdapter(this);
		//ctAdapter.open();
		//ctAdapter.createItem(title, author, isbn, price);
		
		ContentValues contentvalues = new ContentValues();
		contentvalues.put(CartDbAdapter.KEY_TITLE, title);
		contentvalues.put(CartDbAdapter.KEY_AUTHOR, author);
		contentvalues.put(CartDbAdapter.KEY_ISBN, isbn);
		contentvalues.put(CartDbAdapter.KEY_PRICE, price);
		ContentResolver dbresolver = getContentResolver();
		dbresolver.insert(CartDbAdapter.CONTENT_URI, contentvalues);

		// TODO: Add the book to the shopping cart.
		
	}
	
}