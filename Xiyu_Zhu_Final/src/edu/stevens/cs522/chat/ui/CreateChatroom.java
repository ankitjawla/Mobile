package edu.stevens.cs522.chat.ui;

import edu.stevens.cs522.chat.MainActivity;
import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.providers.ChatContent;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class CreateChatroom extends Activity {

	public static int server_flag = 0;
	
	String create_room = "create_room";
	EditText input;
	TextView warntext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_chatroom);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_create_chatroom, menu);
		return true;
	}
	
	public void onClick(View view){
		input = (EditText)findViewById(R.id.chatroom_name);
		String chatroom_name = input.getText().toString();
		
		ContentValues values = new ContentValues();
		values.put(ChatContent.Chatrooms.NAME, chatroom_name);
		values.put(ChatContent.Chatrooms.OWNER, MainActivity.name);
//		values.put(ChatContent.Chatrooms.IP, "unknown");
//		values.put(ChatContent.Chatrooms.PORT, "unknown");
		
		String[] projection = new String[] { ChatContent.Chatrooms.NAME };
		String where = ChatContent.Chatrooms.NAME + "= ?";
		String[] selectionArgs = new String[] { chatroom_name };
		
		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(ChatContent.Chatrooms.CONTENT_URI, projection, where, selectionArgs, null);

		if (!c.moveToFirst()) {
			server_flag = 1;
			cr.insert(ChatContent.Chatrooms.CONTENT_URI, values);
			Log.i("insert!!!!", "insert done!!!!!!!!!!!!!!!!!!!!!");

			this.finish();
		} else {
			String text = "Already exits!";
			warntext = (TextView)findViewById(R.id.warnText);
			warntext.setText(text);
		}

	}
}
