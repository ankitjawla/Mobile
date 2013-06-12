/*********************************************************************

    Client for sending chat messages to the server..

    Copyright (c) 2012 Stevens Institute of Technology

 **********************************************************************/
package edu.stevens.cs522.chat.oneway;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.support.v4.app.NavUtils;

public class Gps extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gps);
		// Show the Up button in the action bar.
		//getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_gps, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void setApp(View view) {
    	Intent intent = new Intent(this, ChatClient.class);
        EditText editname = (EditText) findViewById(R.id.edit_name);
        EditText editlongitude = (EditText) findViewById(R.id.edit_longitude);
        EditText editlatitude = (EditText) findViewById(R.id.edit_latitude);
        String name = editname.getText().toString();
        String longitude = editlongitude.getText().toString();
        String latitude = editlatitude.getText().toString();
        intent.putExtra("setname", name);
        intent.putExtra("setlongitude", longitude);
        intent.putExtra("setlatitude", latitude);
        startActivity(intent);
        }
}
