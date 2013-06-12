/*********************************************************************

    Chat server: accept chat messages from clients.
    
    Sender name and GPS coordinates are encoded
    in the messages, and stripped off upon receipt.

    Copyright (c) 2012 Stevens Institute of Technology

 **********************************************************************/

package edu.stevens.cs522.chat.persistent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ChatServer extends FragmentActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>{

	final static public String TAG = "ChatServer";
	final static boolean debug = true;

	static final private int MENU_SHOW_PEERS = Menu.FIRST;
	
	private android.support.v4.content.CursorLoader c;
	private ListView msglist;
	SimpleCursorAdapter adapter = null;
	/*
	 * Socket used both for sending and receiving
	 */
	DatagramSocket serverSocket;
	
	/*
	 * Adapter for displaying received messages.
	 */
	CursorAdapter messageAdapter;

	/*
	 * True as long as we don't get socket errors
	 */
	boolean socketOK = true;

	Button next;

	/*
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		try {
			/*
			 * Get port information from the resources.
			 */
			int port = Integer.parseInt(this.getString(R.string.app_port));
			serverSocket = new DatagramSocket(port);
		} catch (Exception e) {
			Log.e(TAG, "Cannot open socket" + e.getMessage());
			return;
		}
		/*
		 * TODO: Messages content provider should be linked to the listview
		 * named "msgList" in the UI:
		 * 1. Build a cursor that projects Messages content.  See makeMessageCursor().
		 * 2. Use a SimpleCursorAdapter to adapt this cursor for msgList listview.
		 * 3. Use messages_row layout for the list of messages
		 */
		msglist = (ListView)findViewById(R.id.msgList);
		next = (Button) findViewById(R.id.next);
		next.setOnClickListener(nextListener);
		
		String[] from = {ChatContent.Messages.SENDER, ChatContent.Messages.MESSAGE};
		int[] to = {R.id.messages_sender, R.id.messages_message};
		adapter = new SimpleCursorAdapter(this, R.layout.messages_row, null, from, to);
		msglist.setAdapter(adapter);
		
		makeMessageCursor();
//		String[] from = {ChatContent.Messages.SENDER, ChatContent.Messages.MESSAGE};
//		int[] to = {R.id.messages_sender, R.id.messages_message};
//		adapter = new SimpleCursorAdapter(this, R.layout.messages_row, null, from, to);
//		msglist.setAdapter(adapter);
//        getLoaderManager().initLoader(0, null, this);
		/*
		 * End Todo
		 */
	}
	
	protected void makeMessageCursor () {
		/*
		 * TODO: managedQuery is deprecated, use CursorLoader instead!
		 */		
        getSupportLoaderManager().initLoader(0, null, this);

	}

	/*
	 * On click listener for the send button
	 */
	private OnClickListener nextListener = new OnClickListener() {
		public void onClick(View v) {
			try {
				MessageInfo msg = nextMessage();
				addReceivedMessage(msg);
				addSender(msg);
				getSupportLoaderManager().restartLoader(0, null, ChatServer.this);
			} catch (IOException e) {
				Log.e(TAG, "Error while receiving message: " + e);
			}
		}
	};

	private MessageInfo nextMessage() throws IOException {
		byte[] receiveData = new byte[1024];

		DatagramPacket receivePacket = new DatagramPacket(receiveData,
				receiveData.length);

		serverSocket.receive(receivePacket);
		Log.i(TAG, "Received a packet");

		InetAddress sourceIPAddress = receivePacket.getAddress();
		Log.d(TAG, "Source IP Address: " + sourceIPAddress);

		String msgContents[] = new String(receivePacket.getData(), 0,
				receivePacket.getLength()).split(":");
		String name = msgContents[0];
		InetAddress host = receivePacket.getAddress();
		int port = receivePacket.getPort();
		double latitude = Double.parseDouble(msgContents[1]);
		double longitude = Double.parseDouble(msgContents[2]);
		String message = msgContents[3];
		
		Log.i(TAG, "Received from " + name + ": " + message);
		return new MessageInfo(name, host, port, latitude, longitude, message);
	}

	public void addReceivedMessage(MessageInfo msg) {
		/*
		 * Add sender and message to the content provider for received messages.
		 */
		ContentValues values = new ContentValues();
		values.put(ChatContent.Messages.SENDER, msg.getSender());
		values.put(ChatContent.Messages.MESSAGE, msg.getMessage());
		ContentResolver cr = getContentResolver();
		cr.insert(ChatContent.Messages.CONTENT_URI, values);
		/*
		 * Update the UI with the change.
		 */
	}

	public void addSender(MessageInfo msg) {

		/*
		 * TODO: Add sender information to content provider for peers
		 * information, if we have not already heard from them. If repeat
		 * message, update location information.
		 */
		ContentValues valuesUpdate = new ContentValues();
		String[] projection = {ChatContent.Peers._ID};
		valuesUpdate.put(ChatContent.Peers.HOST, msg.getSrcAddr());
		valuesUpdate.put(ChatContent.Peers.PORT, msg.getSrcPort());
		valuesUpdate.put(ChatContent.Peers.LATITUDE, msg.getLatitude());
		valuesUpdate.put(ChatContent.Peers.LONGITUDE, msg.getLongitude());
		
		ContentValues valuesInsert = new ContentValues(valuesUpdate);
		valuesInsert.put(ChatContent.Peers.NAME, msg.getSender());
		
		String sender = msg.getSender();
		
		String selection = ChatContent.Peers.NAME  + "='" + sender + "'";
		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(ChatContent.Peers.CONTENT_URI, projection, selection, null, null);
		if(c != null){
			cr.update(ChatContent.Peers.CONTENT_URI, valuesUpdate, selection, null);
		}
		else{
			cr.insert(ChatContent.Peers.CONTENT_URI, valuesInsert);
		}
	}

	/*
	 * Close the socket before exiting application
	 */
	public void closeSocket() {
		serverSocket.close();
	}

	/*
	 * If the socket is OK, then it's running
	 */
	boolean socketIsOK() {
		return socketOK;
	}

	/*
	 * Options menu includes an option to list all peers from whom we have
	 * received communication.
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_SHOW_PEERS, 0, R.string.menu_show_peers);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		Intent i;

		switch (item.getItemId()) {
		case (MENU_SHOW_PEERS):
			i = new Intent(this, ShowPeers.class);
			startActivity(i);
			return true;
		}
		return false;
	}

	public android.support.v4.content.Loader<Cursor> onCreateLoader(int arg0,
			Bundle arg1) {
		if(this.adapter != null){
			c =  new android.support.v4.content.CursorLoader(this, ChatContent.Messages.CONTENT_URI, null, null, null, null);
		}
		return c;
	}

	public void onLoadFinished(android.support.v4.content.Loader<Cursor> arg0,
			Cursor arg1) {
		// TODO Auto-generated method stub
		this.adapter.changeCursor(arg1);
	}

	public void onLoaderReset(android.support.v4.content.Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		this.adapter.changeCursor(null);
	}
}
