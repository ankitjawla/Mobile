package edu.stevens.cs522;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BookStore extends Activity {

	static private String PROP_BOOK_TITLE = ""; 
	static private String PROP_BOOK_AUTHOR = ""; 
	static private String PROP_BOOK_ISBN = ""; 

	static final private int SEARCH_REQUEST = 0;
	static final private int CHECKOUT_REQUEST = SEARCH_REQUEST + 1;

	// No content provider for shopping cart, just remember one book for now.
	private String bookTitle = null;
	private String bookAuthor = null;
	private String bookISBN = null;

	private TextView titleText;
	private TextView authorText;
	private TextView isbnText;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cart);
		
		Resources res = getResources();
		PROP_BOOK_TITLE = res.getString(R.string.PROP_BOOK_TITLE);
		PROP_BOOK_AUTHOR = res.getString(R.string.PROP_BOOK_AUTHOR);
		PROP_BOOK_ISBN = res.getString(R.string.PROP_BOOK_ISBN);

		titleText = (TextView) findViewById(R.id.cart_title);
		authorText = (TextView) findViewById(R.id.cart_author);
		isbnText = (TextView) findViewById(R.id.cart_isbn);
		
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
			if (bookTitle == null) {
				Toast.makeText(this, "Empty Cart!", Toast.LENGTH_SHORT).show();
			} else {
				i = new Intent(this, Checkout.class);
				// TODO: Launch the Checkout sub-activity with the book information.
				i.putExtra("store_title", ((TextView) findViewById(R.id.cart_title)).getText().toString());
				i.putExtra("store_author", ((TextView) findViewById(R.id.cart_author)).getText().toString());
				i.putExtra("store_isbn", ((TextView) findViewById(R.id.cart_isbn)).getText().toString());
				startActivityForResult(i,CHECKOUT_REQUEST);
			}
			return true;
		case (R.id.main_delete):
			if (bookTitle == null) {
				Toast.makeText(this, "Empty Cart!", Toast.LENGTH_SHORT).show();
			} else {
				titleText.setText("");
				authorText.setText("");
				isbnText.setText("");
				bookTitle = bookAuthor = bookISBN = null;
			}
			return true;
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
				Bundle extras = intent.getExtras();
				assert extras != null;
				// TODO: Retrieve book data from SearchResult and display.
				bookTitle = extras.getString("sch_title");
				bookAuthor = extras.getString("sch_author");
				bookISBN = extras.getString("sch_isbn");
				
				titleText.setText(bookTitle);
				authorText.setText(bookAuthor);
				isbnText.setText(bookISBN);
				return;
			case RESULT_CANCELED:
				return;
			}
		case CHECKOUT_REQUEST:
			switch (resultCode) {
			case RESULT_OK:
				bookTitle = bookAuthor = bookISBN = null;
				titleText.setText("");
				authorText.setText("");
				isbnText.setText("");
				return;
			case RESULT_CANCELED:
				return;
			}
		}
	}

}