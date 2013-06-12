package edu.stevens.cs522.chat.ui;

import java.net.InetAddress;
import java.util.List;

import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.messages.ChatService;
import edu.stevens.cs522.chat.messages.IChatService;
import edu.stevens.cs522.chat.messages.MessageInfo;
import edu.stevens.cs522.chat.messages.MessageUtils;
import edu.stevens.cs522.chat.rooms.Chatrooms;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

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
		IChatRoomManager, ISendMessage {

	private final static String TAG = ChatRoomListActivity.class.getCanonicalName();
	
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean isTwoPane;
	
	ChatRoomDetailFragment fragment;
	public String sender;
	public double latitude;
	public double longitude;
	IntentFilter filter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chatroom_list);

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
		filter = new IntentFilter(ChatService.NEW_MESSAGE_BROADCAST);
		registerReceiver(updater, filter);
		Intent intent = new Intent(this, ChatService.class);
		startService(intent);
		bindService(intent, connection, BIND_AUTO_CREATE);
		/*
		 * End Todo
		 */
		Intent Listintent=getIntent();
		sender=Listintent.getExtras().getString("name");
		latitude=Double.parseDouble(Listintent.getExtras().getString("latitude"));
		longitude=Double.parseDouble(Listintent.getExtras().getString("longitude"));
		rooms = Chatrooms.createChatrooms();

		// To Do: If exposing deep links into your app, handle intents here.
	}

	private Chatrooms rooms;

	/**
	 * Callback method from {@link IChatRoomManager} indicating
	 * that the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		if (isTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(ChatRoomDetailFragment.CHATROOM_ID_KEY, id);
			fragment = new ChatRoomDetailFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.replace(R.id.chatroom_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent Detailintent = new Intent(this, ChatRoomDetailActivity.class);
			Detailintent.putExtra("Listname", sender);
			Detailintent.putExtra("Listlatitude", String.valueOf(latitude));
			Detailintent.putExtra("Listlongitude", String.valueOf(longitude));
			Log.d(TAG, "Sender:" + sender + " " + latitude + " " + longitude);
			Detailintent.putExtra(ChatRoomDetailFragment.CHATROOM_ID_KEY, id);
			startActivity(Detailintent);
		}
	}
	
	/**
	 * Callback method from {@link IChatRoomManager} indicating
	 * that the item with the given ID was selected.
	 */
	@Override
	public List<String> getRoomNames() {
		return rooms.getRoomNames();
	}
	
	
	/*
	 * Service binder.
	 */
	private IChatService service;

	/*
	 * TODO: Handle the connection with the service.
	 * 
	 * Handle ALL service connections here.
	 */
	private ServiceConnection connection = new ServiceConnection() {		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			ChatRoomListActivity.this.service = ((ChatService.ChatBinder)service).getService();
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			ChatRoomListActivity.this.service = null;
		}
	};
	
	/*
	 * End Todo
	 */
	
	/*
	 * TODO: Since the content provider for messages received is now updated on
	 * a background thread, it sends a broadcast to the UI to tell it to update
	 * the cursor. The UI should register a broadcast receiver that will change
	 * the cursor for the messages adapter. Call into the child fragment, which
	 * has access to the UI, for the update. Check that the fragment is there
	 * with the logic:
	 * 
	 * if (findViewById(R.id.chatroom_detail_container) != null) ...
	 */

	BroadcastReceiver updater = new BroadcastReceiver() {

		@Override
			public void onReceive(Context context, Intent intent) {
			Log.d("TAG", "update ui list");
			if (findViewById(R.id.chatroom_detail_container) != null) {
				getLoaderManager().restartLoader(0, null, fragment.loaderCallbacks);
			}
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
	public void send(InetAddress addr, int port, String message) {
		Log.i(TAG, "Sending message on tablet.");
		Intent intent=getIntent();
		String sender=intent.getExtras().getString("name");
		double latitude=Double.parseDouble(intent.getExtras().getString("latitude"));
		double longitude=Double.parseDouble(intent.getExtras().getString("longitude"));
		MessageUtils.send(this, service, addr, sender, longitude, latitude, port, message);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (isFinishing()) {
			/*
			 * TODO unbind the service.
			 */
			unregisterReceiver(updater);
			unbindService(connection);
			stopService(new Intent(this, ChatService.class));
			/*
			 * End Todo
			 */
		}
	}
}
