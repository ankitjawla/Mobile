package edu.stevens.cs522.chat.ui;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.stevens.cs522.chat.MainActivity;
import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.messages.ChatService;
import edu.stevens.cs522.chat.messages.ChatService.LocalBinder;
import edu.stevens.cs522.chat.messages.IChatService;
import edu.stevens.cs522.chat.messages.MessageUtils;
import edu.stevens.cs522.chat.providers.ChatContent;
import edu.stevens.cs522.chat.providers.ChatroomProvider.DatabaseHelper;
import edu.stevens.cs522.chat.providers.PeerInfoProvider;
import edu.stevens.cs522.chat.rooms.Chatroom;
import edu.stevens.cs522.chat.rooms.Chatrooms;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * An activity representing a list of ChatRooms. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ChatRoomDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ChatRoomListFragment} and the item details (if present) is a
 * {@link ChatRoomDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link IChatRoomManager} interface to listen for item
 * selections.
 */
public class ChatRoomListActivity extends Activity implements
		ISendMessage, ChatRoomDetailFragment.SendMessageWithRoomName {

	private final static String TAG = ChatRoomListActivity.class.getCanonicalName();
	
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean isTwoPane;
	public String sender;
	public double latitude;
	public double longitude;
	String create_room = "create_room";
	private String selected;
	private List<String> rooms;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chatroom_list);
		
		ContentResolver cr = getContentResolver();
		
		
		if (findViewById(R.id.chatroom_detail_container) != null) {
			Log.i(TAG, "Executing in two-pane mode.");
			
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			isTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((ChatRoomListFragment) getFragmentManager()
					.findFragmentById(R.id.chatroom_list))
					.setActivateOnItemClick(true);
			
		} else {
			Log.i(TAG, "Executing in single-pane mode.");
		}

		/*
		 * TODO: Start the background service that will receive messages from
		 * peers, and bind to the service.
		 */
		Intent intent = new Intent(this, ChatService.class);
        bindService(intent, chatConnection, Context.BIND_AUTO_CREATE);
        this.startService(new Intent(this, ChatService.class));
		/*
		 * End Todo
		 */
        	Intent fromothers = getIntent();
        	if(fromothers.getAction() == "detailed")
        	{
        		
        	}
        	else if(fromothers.getAction() == "MainActivity"){
        		Bundle extras = fromothers.getExtras();
        	    if(!extras.getString("chat_name").isEmpty()){
            	    sender = extras.getString("chat_name");
        	    } else {
        	    	sender = "defaulte";
        	    }
        	    if(!extras.getString("chat_latitude").isEmpty()){
        		    latitude = Double.parseDouble(extras.getString("chat_latitude"));
        	    } else {
        	    	latitude = 0;
        	    }
        	    if(!extras.getString("chat_longitude").isEmpty()){
        			longitude = Double.parseDouble(extras.getString("chat_longitude"));
        	    } else {
        	    	longitude = 0;
        	    }     		
        	}
        
		registerReceiver(receiver, filter);
		// To Do: If exposing deep links into your app, handle intents here.
	}
	
	private BroadcastReceiver receiver = new ChatReceiver();
	private IntentFilter filter = new IntentFilter(ChatService.NEW_MESSAGE_BROADCAST);
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	public void onStop(){
		if(receiver != null){
			unregisterReceiver(receiver);			
		}
		super.onStop();
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
	
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_create:
            	Intent intent_create = new Intent(this,CreateChatroom.class);
            	startActivity(intent_create);
                return true;
            case R.id.menu_join:
            	Intent intent_add = new Intent(this,JoinChatroom.class);
            	startActivity(intent_add);
                return true;            	
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	public void onItemSelected(String id) {
		selected = id;

		if (isTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(ChatRoomDetailFragment.CHATROOM_ID_KEY, id);
			arguments.putString("CHATROOM_NAME", selected);
			ChatRoomDetailFragment fragment = new ChatRoomDetailFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.replace(R.id.chatroom_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, ChatRoomDetailActivity.class);
			detailIntent.putExtra("detail_name", sender);
			detailIntent.putExtra("detail_latitude", String.valueOf(latitude));
			detailIntent.putExtra("detail_longitude", String.valueOf(longitude));
			detailIntent.putExtra("detail_chatroom", selected);
			Log.d(TAG, "Sender:" + sender + " " + latitude + " " + longitude);
			detailIntent.putExtra(ChatRoomDetailFragment.CHATROOM_ID_KEY, id);
			startActivity(detailIntent);
		}
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
	/*
	 * End Todo
	 */
	
	/*
	 * The detail fragment calls into the activity to invoke the 
	 * service operation for sending a message.
	 */
	@Override
	public void send(InetAddress addr, int port, String message, String chatroom, String type) {
		Log.i(TAG, "Sending message on tablet.");
//		MessageUtils.send(this, chatService, addr, port, message);
//		Intent fromParent = getIntent();
//		Bundle extras = fromParent.getExtras();
//	    String sender = extras.getString("chat_name");
//	    double latitude = Double.parseDouble(extras.getString("chat_latitude"));
//		double longitude = Double.parseDouble(extras.getString("chat_longitude"));
		if(MainActivity.latitude.isEmpty()){
			MainActivity.latitude = "0.0";
		}
		if(MainActivity.longitude.isEmpty()){
			MainActivity.longitude = "0.0";
		}
		MessageUtils.sendWithInfo(chatService, MainActivity.name, Double.parseDouble(MainActivity.latitude), Double.parseDouble(MainActivity.longitude), addr, port, message, chatroom, type);
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
		Cursor c = cr.query(ChatContent.Chatrooms.CONTENT_URI, projection, ChatContent.Chatrooms.NAME + "='" + selected + "'", null, null);	
		return c;
	}

	@Override
	public Cursor serverReturnCursor() {
		// TODO Auto-generated method stub
		String[] projection = new String[] { ChatContent.Peers.HOST, ChatContent.Peers.PORT };
		String where = ChatContent.Peers.CHATROOM + "= ?";
		String[] selectionArgs = new String[] { selected };	
		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(ChatContent.Peers.CONTENT_URI, projection, where, selectionArgs, null);	
		return c;
	}
	
	public void showIP(){
		try{
			for(NetworkInterface intf : Collections.list(NetworkInterface.getNetworkInterfaces())){
				for (InetAddress addr : Collections.list(intf.getInetAddresses())){
					if(!addr.isLoopbackAddress()){
						String myIP = addr.getHostAddress();
						Toast.makeText(this, "Your IP address is: " + myIP, Toast.LENGTH_LONG).show();
					}
				}
			}
			throw new RuntimeException("No network connections found.");
		} catch(Exception ex){
			Toast.makeText(this, "Error getting IP address: " + ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		}
	}

}
