package edu.stevens.cs522.chat.ui;

import java.net.InetAddress;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.messages.ChatService;
import edu.stevens.cs522.chat.messages.ChatService.LocalBinder;
import edu.stevens.cs522.chat.messages.IChatService;
import edu.stevens.cs522.chat.messages.MessageUtils;
import edu.stevens.cs522.chat.providers.ChatContent;


/**
 * An activity representing a single ChatRoom detail screen. This activity is
 * only used on handset devices. On tablet-size devices, item details are
 * presented side-by-side with a list of items in a {@link ChatRoomListActivity}
 * .
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link ChatRoomDetailFragment}.
 */
public class ChatRoomDetailActivity extends Activity implements ISendMessage, ChatRoomDetailFragment.SendMessageWithRoomName {
	
	private static String TAG = ChatRoomDetailActivity.class.getCanonicalName();
	
	private ChatRoomDetailFragment fragment;

	private String sender;
	private double latitude;
	private double longitude;
	public String chatroom;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chatroom_detail);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(ChatRoomDetailFragment.CHATROOM_ID_KEY, getIntent()
					.getStringExtra(ChatRoomDetailFragment.CHATROOM_ID_KEY));
			arguments.putString("CHATROOM_NAME", getIntent().getExtras().getString("detail_chatroom"));
			fragment = new ChatRoomDetailFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.add(R.id.chatroom_detail_container, fragment).commit();
		}
		
		/*
		 * TODO: Bind to the background service that will receive messages from
		 * peers.  This keeps the service running even if parent activity is destroyed.
		 */
		Intent intent = new Intent(this, ChatService.class);
        bindService(intent, chatConnection, Context.BIND_AUTO_CREATE);
        this.startService(new Intent(this, ChatService.class));
        
        Intent fromParent = getIntent();
		Bundle extras = fromParent.getExtras();
	    sender = extras.getString("detail_name");
//	    Log.d("sender_nameOOOOOOOOOOOOOOOOOOOO", sender);
	    latitude = Double.parseDouble(extras.getString("detail_latitude"));
		longitude = Double.parseDouble(extras.getString("detail_longitude"));
		chatroom = extras.getString("detail_chatroom");
		Log.d(TAG, "From Detail Activity Sender:" + sender + " " + latitude + " " + longitude);
        registerReceiver(receiver, filter);
		/*
		 * End Todo
		 */

	}
	
	/*
	 * Service binder.
	 */
	private IChatService chatService;

	/*
	 * TODO: Handle the connection with the service.
	 * 
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
            // This is called when the Home (Up) button is pressed
            // in the Action Bar.
            Intent parentActivityIntent = new Intent(this, ChatRoomListActivity.class);
            parentActivityIntent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            parentActivityIntent.setAction("detailed");
            startActivity(parentActivityIntent);
            finish();
            return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private BroadcastReceiver receiver = new ChatReceiver();
	private IntentFilter filter = new IntentFilter(ChatService.NEW_MESSAGE_BROADCAST);
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}
	
	public class ChatReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			ChatRoomDetailFragment detailFrag = (ChatRoomDetailFragment)
					getFragmentManager().findFragmentById(R.id.chatroom_detail_container);
			if (detailFrag != null) {
				detailFrag.messageAdapter.notifyDataSetChanged();
				Log.d(TAG, "received a broadcast!!!!!!!!!!");
			}
		}	
	}
	
	/*
	 * End Todo
	 */

	/*
	 * The detail fragment calls into the activity to invoke the 
	 * service operation for sending a message.
	 */
	@Override
	public void send(InetAddress addr, int port, String message, String chatroom,  String type) {
		Log.i(TAG, "Sending message on phone.");
		String message_type = "message";
//		MessageUtils.send(this, chatService, addr, port, message);
		MessageUtils.sendWithInfo(chatService, sender, longitude, latitude, addr, port, message, chatroom, message_type);
	}

	@Override
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

	@Override
	public Cursor returnCursor() {
		// TODO Auto-generated method stub
		ContentResolver cr = getContentResolver();
		String[] projection = new String[] { ChatContent.Chatrooms.IP, ChatContent.Chatrooms.PORT };
		Cursor c = cr.query(ChatContent.Chatrooms.CONTENT_URI, projection, ChatContent.Chatrooms.NAME + "='" + chatroom + "'", null, null);	
		return c;
	}

	@Override
	public Cursor serverReturnCursor() {
		// TODO Auto-generated method stub
		String[] projection = new String[] { ChatContent.Peers.HOST, ChatContent.Peers.PORT };
		String where = ChatContent.Peers.CHATROOM + "= ?";
		String[] selectionArgs = new String[] { chatroom };	
		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(ChatContent.Peers.CONTENT_URI, projection, where, selectionArgs, null);	
		return c;
	}

}
