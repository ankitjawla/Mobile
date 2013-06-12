package edu.stevens.cs522.chat.ui;

import java.net.InetAddress;

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
import android.view.MenuItem;
import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.messages.ChatService;
import edu.stevens.cs522.chat.messages.IChatService;
import edu.stevens.cs522.chat.messages.MessageUtils;

/**
 * An activity representing a single ChatRoom detail screen. This activity is
 * only used on handset devices. On tablet-size devices, item details are
 * presented side-by-side with a list of items in a {@link ChatRoomListActivity}
 * .
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link ChatRoomDetailFragment}.
 */
public class ChatRoomDetailActivity extends Activity implements ISendMessage {

	@SuppressWarnings("unused")
	private static String TAG = ChatRoomDetailActivity.class.getCanonicalName();

	private ChatRoomDetailFragment fragment;
	private String sender;
	private double latitude;
	private double longitude;
	IntentFilter filter;

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
			arguments.putString(ChatRoomDetailFragment.CHATROOM_ID_KEY,
					getIntent().getStringExtra(ChatRoomDetailFragment.CHATROOM_ID_KEY));
			fragment = new ChatRoomDetailFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction().add(R.id.chatroom_detail_container, fragment)
					.commit();
		}
		/*
		 * TODO: Bind to the background service that will receive messages from
		 * peers. This keeps the service running even if parent activity is
		 * destroyed.
		 */
		filter = new IntentFilter(ChatService.NEW_MESSAGE_BROADCAST);
		Intent intent = new Intent(this, ChatService.class);
		startService(intent);
		bindService(intent, connection, BIND_AUTO_CREATE);
		registerReceiver(updater, filter);
		/*
		 * End Todo
		 */

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
			ChatRoomDetailActivity.this.service = ((ChatService.ChatBinder) service).getService();
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			ChatRoomDetailActivity.this.service = null;
		}
	};

	/*
	 * End To do
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This is called when the Home (Up) button is pressed
			// in the Action Bar.
			Intent parentActivityIntent = new Intent(this, ChatRoomListActivity.class);
			parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(parentActivityIntent);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

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
	 * The detail fragment calls into the activity to invoke the service
	 * operation for sending a message.
	 */
	@Override
	public void send(InetAddress addr, int port, String message) {
		Log.i(TAG, "Sending message on phone.");
		Intent intent=getIntent();
		sender=intent.getExtras().getString("Listname");
		latitude=Double.parseDouble(intent.getExtras().getString("Listlatitude"));
		longitude=Double.parseDouble(intent.getExtras().getString("Listlongitude"));
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
