package edu.stevens.cs522.chat;

import edu.stevens.cs522.chat.messages.ChatService;
import edu.stevens.cs522.chat.providers.ChatContent;
import edu.stevens.cs522.chat.ui.ChatRoomListActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView nameText;
	private TextView latitudeText;
	private TextView longitudeText;
	private Button send;
	Intent intent;
	
	public static String name;
	public static String latitude;
	public static String longitude;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		nameText = (TextView) findViewById(R.id.ChatName);
		latitudeText = (TextView) findViewById(R.id.ChatLatitude);
		longitudeText = (TextView) findViewById(R.id.ChatLongitude);
		send = (Button) findViewById(R.id.ChatButtonSend);
		send.setOnClickListener(sendListener);
		intent = new Intent(this, ChatRoomListActivity.class);
		intent.setAction("MainActivity");
	}

	private OnClickListener sendListener = new OnClickListener() {
		public void onClick(View v) {
			name = nameText.getText().toString();
			latitude = latitudeText.getText().toString();
			longitude = longitudeText.getText().toString();
			
			intent.putExtra("chat_name", name);
			intent.putExtra("chat_latitude", latitude);
			intent.putExtra("chat_longitude", longitude);
			startActivity(intent);
			}
		};
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}

