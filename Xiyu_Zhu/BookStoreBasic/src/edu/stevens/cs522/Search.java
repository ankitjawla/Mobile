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

public class Search extends Activity {
	static final private int MENU_BUY = Menu.FIRST;
	static final private int MENU_CANCEL = Menu.FIRST + 1;

	static private String PROP_BOOK_TITLE = ""; 
	static private String PROP_BOOK_AUTHOR = ""; 
	static private String PROP_BOOK_ISBN = ""; 
	
	private EditText titleText;
	private EditText authorText;
	private EditText isbnText;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		/*
		 * TODO: Add menu with BUY and CANCEL items
		 */
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.search, menu);
	    
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		/*
		 * TODO: Replace MENU_BUY with resid from menu.
		 */

		switch (item.getItemId()) {
		case (R.id.search_buy):
			buyBook();
			return true;
			// TODO: Add CANCEL option handling.
		case (R.id.search_cancel):
			finish();
			return true;
		}
		return false;
	}

	public void buyBook() {
		titleText = (EditText) findViewById(R.id.search_title);
		authorText = (EditText) findViewById(R.id.search_author);
		isbnText = (EditText) findViewById(R.id.search_isbn);
		String bookTitle = titleText.getText().toString();
		String bookAuthor = authorText.getText().toString();
		String bookISBN = isbnText.getText().toString();

		Resources res = getResources();
		PROP_BOOK_TITLE = res.getString(R.string.PROP_BOOK_TITLE);
		PROP_BOOK_AUTHOR = res.getString(R.string.PROP_BOOK_AUTHOR);
		PROP_BOOK_ISBN = res.getString(R.string.PROP_BOOK_ISBN);
		
		// TODO: Transfer book data back to BookStore.
		Intent i=new Intent(this, BookStore.class);
		i.putExtra("sch_title",bookTitle);
		i.putExtra("sch_author",bookAuthor);
		i.putExtra("sch_isbn",bookISBN);
		setResult(Activity.RESULT_OK,i);
		finish();
	}

}