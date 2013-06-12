package edu.stevens.cs522;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class Checkout extends Activity {
	static final private int MENU_ORDER = Menu.FIRST;
	static final private int MENU_CANCEL = Menu.FIRST + 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checkout);

		Resources res = getResources();
		String PROP_BOOK_TITLE = res.getString(R.string.PROP_BOOK_TITLE);
		String PROP_BOOK_AUTHOR = res.getString(R.string.PROP_BOOK_AUTHOR);
		String PROP_BOOK_ISBN = res.getString(R.string.PROP_BOOK_ISBN);

		TextView titleText = (TextView) findViewById(R.id.checkout_title);
		TextView authorText = (TextView) findViewById(R.id.checkout_author);
		TextView isbnText = (TextView) findViewById(R.id.checkout_isbn);

		// TODO: Retrieve book data from BookStore and display on checkout screen.
		Intent i = getIntent();
		Bundle extras = i.getExtras();
		titleText.setText(i.getStringExtra("store_title"));
		authorText.setText(i.getStringExtra("store_author"));
		isbnText.setText(i.getStringExtra("store_isbn"));		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		/*
		 * TODO: Add an options menu with ORDER and CANCEL items
		 */
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.check, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		/*
		 * TODO: Replace MENU_ORDER and MENU_CANCEL with resids from menu.
		 */
		

		switch (item.getItemId()) {
		case R.id.check_order:
			Toast.makeText(this, "Book ordered!", Toast.LENGTH_SHORT).show();
			setResult(RESULT_OK);
			finish();
			return true;
		case R.id.check_cancel:
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}
		return false;
	}

}