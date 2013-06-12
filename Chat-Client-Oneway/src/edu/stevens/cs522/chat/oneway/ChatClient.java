/*********************************************************************

    Client for sending chat messages to the server..

    Copyright (c) 2012 Stevens Institute of Technology

 **********************************************************************/
package edu.stevens.cs522.chat.oneway;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/*
 * @author dduggan
 * 
 */
public class ChatClient extends Activity {

	final static private String TAG = "ChatClient";

	/*
	 * Socket used for sending
	 */
	DatagramSocket clientSocket;

	/*
	 * Widgets for dest address, message text, send button.
	 */
	EditText dest;
	EditText msg;
	Button send;

	/*
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		dest = (EditText) findViewById(R.id.dest_text);

		msg = (EditText) findViewById(R.id.message_text);

		send = (Button) findViewById(R.id.send_button);
		send.setOnClickListener(sendListener);

		try {
			int clientPort = Integer.parseInt(getString(R.string.app_port));
			clientSocket = new DatagramSocket(clientPort);

		} catch (Exception e) {
			Log.e(TAG, "Cannot open socket: " + e.getMessage());
			return;
		}

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
			InetAddress destAddr = InetAddress.getByName(dest.getText().toString());

			String theNewMessage = msg.getText().toString();

			this.sendMessage(destAddr, theNewMessage);
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
	public void sendMessage(InetAddress destAddr, String msg) throws IOException {

		/*
		 * Append client info to the front of the message.
		 */
		String sender = getString(R.string.user_name);
		String latitude = getString(R.string.latitude);
		String longitude = getString(R.string.longitude);
		String line = sender + ":" + latitude + ":" + longitude + ":" + msg;
		byte[] sendData = line.getBytes();

		int destPort = Integer.parseInt(getString(R.string.target_port_default));

		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, destAddr, destPort);

		clientSocket.send(sendPacket);

		Log.i(TAG, "Sent packet: " + msg);

	}

}