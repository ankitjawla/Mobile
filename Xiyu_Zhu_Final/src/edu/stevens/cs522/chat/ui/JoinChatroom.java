package edu.stevens.cs522.chat.ui;

import java.net.InetAddress;
import java.net.UnknownHostException;

import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.messages.ChatService;
import edu.stevens.cs522.chat.messages.IChatService;
import edu.stevens.cs522.chat.messages.MessageUtils;
import edu.stevens.cs522.chat.messages.ChatService.LocalBinder;
import edu.stevens.cs522.chat.providers.ChatContent;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class JoinChatroom extends Activity implements ISendMessage{

	String chatroom_refresh = "chatroom_refresh";
	EditText ip;
	EditText name;
	EditText port;
	TextView warntext;
	
	String chatroom_IP;
	String chatroom_name;
	int chatroom_port;
	InetAddress IP;
	String message_type = "request";
	String message = "request";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join_chatroom);
		ip = (EditText)findViewById(R.id.chatroom_addIP);
		name = (EditText)findViewById(R.id.chatroom_addname);
		port = (EditText)findViewById(R.id.chatroom_addport);
		
		/*
		 * bind service
		 */
		Intent intent = new Intent(this, ChatService.class);
        bindService(intent, chatConnection, Context.BIND_AUTO_CREATE);
        this.startService(new Intent(this, ChatService.class));

	}

	/*
	 * Service binder.
	 */
	private IChatService chatService;

	/*
	 * TODO: Handle the connection with the service.	 * 
	 * Handle ALL service connections here.
	 */
	
	private ServiceConnection chatConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
        	LocalBinder binder = (LocalBinder) service;
            chatService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	chatService = null; 
        }
    };

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_join_chatroom, menu);
		return true;
	}
	
	public void onClick(View view){
		chatroom_IP = ip.getText().toString();
		chatroom_port = Integer.parseInt(port.getText().toString());
		try {
			IP = InetAddress.getByName(chatroom_IP);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
		
		}
		chatroom_name = name.getText().toString();
		
		
		ContentValues values = new ContentValues();
		values.put(ChatContent.Chatrooms.NAME, chatroom_name);
		values.put(ChatContent.Chatrooms.IP, chatroom_IP);
		values.put(ChatContent.Chatrooms.PORT, chatroom_port);
		
		String[] projection = new String[] { ChatContent.Chatrooms.NAME };
		String where = ChatContent.Chatrooms.NAME + "= ?";
		String[] selectionArgs = new String[] { chatroom_name };
		
		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(ChatContent.Chatrooms.CONTENT_URI, projection, where, selectionArgs, null);

		if (!c.moveToFirst()) {
			CreateChatroom.server_flag = 0;
			// If there is no record of this chatroom, then send a join request to the IP address.
			
			cr.insert(ChatContent.Chatrooms.CONTENT_URI, values);
			
			send(IP, chatroom_port, message, chatroom_name, message_type);

			this.finish();
		} else {
			String text = "Already exits!";
			warntext = (TextView)findViewById(R.id.warnText);
			warntext.setText(text);
		}

	}

	@Override
	public void send(InetAddress addr, int port, String message, String chatroom, String type) {
		// TODO Auto-generated method stub
		MessageUtils.sendWithInfo(chatService, addr, port, chatroom, type);		
	}
	
	public void onDestroy() {
		super.onDestroy();
		if (isFinishing()) {
			/*
			 * TODO unbind the service.
			 */
			 unbindService(chatConnection);
			/*
			 * End Todo
			 */
		}
	}

}
