/*********************************************************************

    Chat app: exchange messages with other instances of the app.
    
    Copyright (c) 2012 Stevens Institute of Technology

 **********************************************************************/

package edu.stevens.cs522.chat.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater.Filter;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;


public class ChatApp extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	final static public String TAG = ChatApp.class.getCanonicalName();
	/*
	 * Adapter for displaying received messages.
	 */
	CursorAdapter messageAdapter;
	/*
	 * Widgets for dest address, message text, send button.
	 */
	private ListView msglist;
	MyReceiver receiver;
	SimpleCursorAdapter adapter = null;
	EditText destHost;
	EditText destPort;
	EditText msg;
	Button send;
	CursorLoader loader;
	IntentFilter filter;
	/*
	 * Service binder.
	 */
	private IChatService serviceBinder;
	/*
	 * TODO: Handle the connection with the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	    	serviceBinder = ((ChatService.ChatBinder)service).getService();
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	    	serviceBinder = null;
	    }
	};

	/*
	 * End Todo
	 */

	/*
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		destHost = (EditText) findViewById(R.id.dest_text);

		destPort = (EditText) findViewById(R.id.port_text);

		msg = (EditText) findViewById(R.id.message_text);

		/*
		 * TODO: Messages content provider should be linked to the listview
		 * named "msgList" in the UI: 1. Build a cursor that projects Messages
		 * content. See makeMessageCursor(). 2. Use a SimpleCursorAdapter to
		 * adapt this cursor for msgList listview. 3. Use messages_row layout
		 * for the list of messages
		 */
		msglist = (ListView)findViewById(R.id.msgList);
		String[] from = {ChatContent.Messages.SENDER, ChatContent.Messages.MESSAGE};
		int[] to = {R.id.messages_sender, R.id.messages_message};
		//.initLoader(0, null, this)
		adapter = new SimpleCursorAdapter(
				this, 
				R.layout.messages_row, 
				null, 
				from, 
				to,
				0);
		msglist.setAdapter(adapter);
		makeMessageCursor();		
		/*
		 * End Todo
		 */
		send = (Button) findViewById(R.id.send_button);
		send.setOnClickListener(sendListener);
		/*
		 * TODO: Start the background service that will receive messages from
		 * peers.
		 */
		Intent i = new Intent(this, ChatService.class);
	    bindService(i, mConnection, Context.BIND_AUTO_CREATE);
	    this.startService(i);
	    
		filter = new IntentFilter(ChatService.NEW_MESSAGE_BROADCAST);
		receiver = new MyReceiver();
		registerReceiver(receiver,filter);
		Log.i(TAG, "registerReceiver: " + receiver);
	/*
		 * End Todo
		 */
	}

	/*
	 * TODO: Since the content provider for messages received is now updated on
	 * a background thread, it sends a broadcast to the UI to tell it to update
	 * the cursor. The UI should register a broadcast receiver that will change
	 * the cursor for the messages adapter.
	 */
	
	public class MyReceiver extends BroadcastReceiver {
		private Context context;
		@Override
        public void onReceive(final Context context, Intent intent) {
			this.context = context;
			//adapter.notifyDataSetChanged();		
			getSupportLoaderManager().restartLoader(0, null, ChatApp.this);
            }
	}
	/*
	 * End Todo
	 */

	protected void makeMessageCursor() {
		/*
		 * TODO: managedQuery is deprecated, use CursorLoader instead!
		 */
		//Cursor c = managedQuery(ChatContent.Messages.CONTENT_URI, projection,
		//		null, null, null);
		getSupportLoaderManager().initLoader(0, null, this);
	}

	/*
	 * On click listener for the send button
	 */
	private OnClickListener sendListener = new OnClickListener() {
		public void onClick(View v) {	
			postMessage();
		}
	};

	/*
	 * Send the message in the msg EditText
	 */
	private void postMessage() {
		try {
			/*
			 * On the emulator, which does not support WIFI stack, we'll send to
			 * (an AVD alias for) the host loopback interface, with the server
			 * port on the host redirected to the server port on the server AVD.
			 */
			InetAddress targetAddr = InetAddress.getByName(destHost.getText().toString());

			int targetPort = Integer.parseInt(destPort.getText().toString());

			String theNewMessage = msg.getText().toString();
			
			this.sendMessage(targetAddr, targetPort, theNewMessage);
			
		} catch (UnknownHostException e) {
			Log.e(TAG, "Unknown host exception: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IO exception: " + e.getMessage());
		}
		msg.setText("");
	}
	/*
	 * Send a UDP packet
	 */
	public void sendMessage(InetAddress destAddr, int destPort, String msg)
			throws IOException {
		/*
		 * Append client info to the front of the message.
		 */
		String sender = getString(R.string.user_name);
		String latitude = getString(R.string.latitude);
		String longitude = getString(R.string.longitude);
		String line = sender + ":" + latitude + ":" + longitude + ":" + msg;
		byte[] sendData = line.getBytes();

		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, destAddr, destPort);

		serviceBinder.send(sendPacket);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopService(new Intent(this, ChatService.class));
	}

	/*
	 * Options menu includes an option to list all peers from whom we have
	 * received communication.
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		this.getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		Intent i;

		switch (item.getItemId()) {
		case (R.id.show_peers):
			i = new Intent(this, ShowPeers.class);
			startActivity(i);
			return true;
		}
		return false;
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(adapter!=null){
			loader =  new CursorLoader(this, ChatContent.Messages.CONTENT_URI, null, null, null, null);
		}
		return loader;
	}

	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		this.adapter.changeCursor(arg1);
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
		this.adapter.changeCursor(null);
	}
}
