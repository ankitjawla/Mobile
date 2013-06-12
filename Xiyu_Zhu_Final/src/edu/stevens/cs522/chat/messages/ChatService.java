/*********************************************************************

    Chat service: accept chat messages from other peers.
    
    Sender name and GPS coordinates are encoded
    in the messages, and stripped off upon receipt.

    Copyright (c) 2012 Stevens Institute of Technology

 **********************************************************************/

package edu.stevens.cs522.chat.messages;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.gson.Gson;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import edu.stevens.cs522.chat.R;

public class ChatService extends Service implements IChatService,
		Handler.Callback {

	/*
	 * The chat service uses a background thread to receive messages sent by
	 * other devices, so the main UI thread does not block while waiting for a
	 * message. The content providers for messages and peer info are updated. A
	 * notification is placed in the UI, and may be used to bring the chat app
	 * to the foreground to see the messages that have been received.
	 */

	private static final String TAG = ChatService.class.getCanonicalName();
	
	public static final String NEW_MESSAGE_BROADCAST = "edu.stevens.cs522.chat.NewMessageBroadcast";

	Notification newMessageNotification;
	public static final int NOTIFICATION_ID = 1;

	/*
	 * Socket for communication with other instances.
	 */
	private DatagramSocket appSocket;
	
	/*
	 * Marshall and unmarshall messages as JSON using Gson.
	 */	
	private Gson gson = new Gson();

	@Override
	public void onCreate() {
		int icon = R.drawable.ic_launcher;
		String tickerText = "New message received";
		long when = System.currentTimeMillis();

		newMessageNotification = new Notification(icon, tickerText, when);

		try {
			appSocket = new DatagramSocket(
					Integer.parseInt(getString(R.string.app_port)));
		} catch (IOException e) {
			Log.e(TAG, "Cannot create socket." + e);
		}
	}

	private Handler sendHandler;

	private final String SEND_MESSAGE_KEY = "message";

	/*
	 * This runs on the UI thread to send a message.
	 */
	public void send(MessageInfo message) {
		Log.i(TAG, "Sending a message.");
		
		Bundle contents = new Bundle();
		contents.putSerializable(SEND_MESSAGE_KEY, message);

		Message messageToSend = new Message();
		messageToSend.setData(contents);

		sendHandler.sendMessage(messageToSend);
	}

	/*
	 * This is a callback that runs on the background sender thread to execute
	 * the (non-blocking) UDP send operation to send a message.
	 */
	public boolean handleMessage(Message sendMsg) {
		Bundle contents = sendMsg.getData();

		try {
			/*
			 * Unmarshall the message from the handler message.
			 */
			MessageInfo message = (MessageInfo)contents.getSerializable(SEND_MESSAGE_KEY);
			
			Log.i("Done!!!!!!!!!!!!!!!!!!!!!!!!", message.getType());
			/*
			 * Extract destination address information.
			 */
			InetAddress destAddr = message.getAddress();
			int destPort = message.getPort();
			/*
			 * Marshall the message to JSON data.
			 */
			byte[] sendData = gson.toJson(message).getBytes();
			/*
			 * Send the datagram packet.
			 */
			DatagramPacket p = new DatagramPacket(sendData, sendData.length,
					destAddr, destPort);
			appSocket.send(p);

		} catch (UnknownHostException e) {
			Log.e(TAG,
					"Unknown host: " + contents.getString(SEND_MESSAGE_KEY));
		} catch (IOException e) {
			Log.e(TAG, "Cannot send message: " + e);
		}
		return true;
	}

	private ReceiveMessageTask recvTask;

	private HandlerThread sender;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*
		 * Start the background thread for handling message receives.
		 */
		Log.i(TAG,
				"Started Chat service, running task for receiving messages.");
		recvTask = new ReceiveMessageTask(this);
		recvTask.execute((Void[]) null);

		/*
		 * Start the background thread for handling message sends.
		 */
		sender = new HandlerThread("edu.stevens.cs522.chat.Sender");
		sender.start();
		Looper looper = sender.getLooper();
		sendHandler = new Handler(looper, this);

		return START_STICKY;
	}

	/*
	 * TODO Provide a binder, since all socket-related operations are on the service.
	 */
	private final IBinder chatBinder = new LocalBinder();
	
	public class LocalBinder extends Binder {
		public ChatService getService(){
			return ChatService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return chatBinder;
	}
	
	/*
	 * End Todo
	 */

	/*
	 * Executed on the background thread, to receive a chat message.
	 */
	MessageInfo nextMessage() throws IOException {
		byte[] receiveData = new byte[1024];

		Log.i(TAG, "Waiting for a message.");

		DatagramPacket receivePacket = new DatagramPacket(receiveData,
				receiveData.length);

		appSocket.receive(receivePacket);
		Log.i(TAG, "Received a packet.");

		InetAddress sourceIPAddress = receivePacket.getAddress();
		Log.d(TAG, "Source IP Address: " + sourceIPAddress);
		
		int sourcePort = receivePacket.getPort();
		
		String strPacket = new String( receivePacket.getData(), 
                						0, 
                						Math.min( receivePacket.getLength(), receivePacket.getData().length),
                						"US-ASCII");
		Log.d(TAG, "Data is " + strPacket);
		
		MessageInfo message = gson.fromJson(new String(strPacket), MessageInfo.class);
		
		message.setAddress(sourceIPAddress);
		message.setPort(sourcePort);

		Log.d(TAG, "Received from " + message.getSender() + ": " + message.getMessage());
		return message;

	}

	@Override
	public void onDestroy() {
		appSocket.close();
	}

}
