package edu.stevens.cs522;

import edu.stevens.cs522.CartDbAdapter.DatabaseHelper;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class Checkout extends FragmentActivity {
	static final private int MENU_ORDER = Menu.FIRST;
	static final private int MENU_CANCEL = Menu.FIRST + 1;

	private int nitems = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checkout);
		// TODO: Have title specify how many books are being purchased.	
		Intent i = getIntent();
		nitems = i.getExtras().getInt("count");
		TextView titleText = (TextView) findViewById(R.id.title);
		titleText.setText("Checking out "+nitems+" books.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.checkout, menu);
		/*
		 * TODO: Provide menu for ORDER and CANCEL.
		 */
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case R.id.check_order:
			String msg = nitems>1 ? "Books ordered!" : "Book ordered!";
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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