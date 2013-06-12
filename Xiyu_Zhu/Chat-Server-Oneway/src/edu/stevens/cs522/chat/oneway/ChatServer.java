/*********************************************************************

    Chat server: accept chat messages from clients.
    
    Sender name and GPS coordinates are encoded
    in the messages, and stripped off upon receipt.

    Copyright (c) 2012 Stevens Institute of Technology

**********************************************************************/
package edu.stevens.cs522.chat.oneway;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class ChatServer extends Activity {

	final static public String TAG = "ChatServer";
	final static boolean debug = true;

	static final private int MENU_SHOW_PEERS = Menu.FIRST;
	
	/*
	 * Socket used both for sending and receiving
	 */
	DatagramSocket serverSocket; 

	/*
	 * True as long as we don't get socket errors
	 */
	boolean socketOK = true; 

	/*
	 * TODO: Declare a listview for messages, and an adapter for displaying messages.
	 */
	ArrayAdapter<String> messages;
	ListView msgList;
	/*
	 * End Todo
	 */
	
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
		 * TODO: Link the array adapter and the listview for messages received.
		 */
		msgList = (ListView) findViewById(R.id.msgList);
		messages = new ArrayAdapter<String>(this, R.layout.message);
		msgList.setAdapter(messages);
		/*
		 * End Todo
		 */

        next = (Button)findViewById(R.id.next);
        next.setOnClickListener(nextListener);
        
	}

	/*
	 * On click listener for the send button
	 */
	private OnClickListener nextListener = new OnClickListener() {
		public void onClick(View v) {
			nextMessage();
		}
	};

	private void nextMessage() {
		byte[] receiveData = new byte[1024];

		DatagramPacket receivePacket = new DatagramPacket(receiveData,
				receiveData.length);

		try {
			serverSocket.receive(receivePacket);
			Log.i(TAG, "Received a packet");

			InetAddress sourceIPAddress = receivePacket.getAddress();
			Log.d(TAG, "Source IP Address: " + sourceIPAddress);
			
			String msgContents[] = new String(receivePacket.getData(), 0, receivePacket.getLength()).split(":");
			String name = msgContents[0];
			String host = receivePacket.getAddress().getHostAddress();
			String port = Integer.toString(receivePacket.getPort());
			String latitude = msgContents[1];
			String longitude = msgContents[2];
			String message = msgContents[3];
			
			Log.i(TAG, "Received from " + name + ":" + "(" + latitude + "," + longitude + ")" + ": " + message);
			
			/*
			 * TODO: Add sender and message to the listview for displaying messages.
			 */
			messages.add(name + ":" + "(" + latitude + "," + longitude + ")" + ": " + message);
			msgList.setAdapter(messages);

			/*
			 * End Todo
			 */

		} catch (Exception e) {
			Log.e(TAG, "Problems receiving packet: " + e.getMessage());
			socketOK = false;
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



}