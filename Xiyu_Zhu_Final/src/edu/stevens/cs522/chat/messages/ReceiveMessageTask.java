package edu.stevens.cs522.chat.messages;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import edu.stevens.cs522.chat.MainActivity;
import edu.stevens.cs522.chat.providers.ChatContent;
import edu.stevens.cs522.chat.ui.ChatRoomListActivity;
import edu.stevens.cs522.chat.ui.CreateChatroom;

/*
 * The description of the logic that is performed on a background thread.
 */
class ReceiveMessageTask extends AsyncTask<Void, MessageInfo, Void> {
	
	/**
	 * This is the background task for receiving chat messages.
	 */
		
	private final static String TAG = ReceiveMessageTask.class.getCanonicalName();
	
	private final ChatService chatService;
	
	private ChatService getService() {
		return chatService;
	}

	private List<String> IPs;
	private List<Integer> ports;
	
	/**
	 * @param chatService
	 */
	ReceiveMessageTask(ChatService chatService) {
		this.chatService = chatService;
	}

	@Override
	protected Void doInBackground(Void... params) {

		/*
		 * Main background loop: receiving and saving messages.
		 * "publishProgress" calls back to the UI loop to notify the user
		 * when a message is received.
		 */

		try {
			while (true) {
				MessageInfo msg = getService().nextMessage();
				
				ContentResolver cr_check = getService().getContentResolver();
				String[] check_projection = new String[] {ChatContent.Chatrooms.NAME};
				String check_where = ChatContent.Chatrooms.NAME + "= ?";
				String[] check_selection = new String[] {msg.getChatroom()};
				Cursor check = cr_check.query(ChatContent.Chatrooms.CONTENT_URI, check_projection, check_where, check_selection, null);
				
				if(check.moveToFirst()){
					String[] projection = new String[] { ChatContent.Chatrooms.OWNER };
					String where = ChatContent.Chatrooms.NAME + "= ?";
					String[] selectionArgs = new String[] { msg.getChatroom() };	
					
					Cursor c_owner = cr_check.query(ChatContent.Chatrooms.CONTENT_URI, projection, where, selectionArgs, null);
					c_owner.moveToFirst();
					String owner = c_owner.getString(0);
										
					if(owner.equals(MainActivity.name)){
						forward(msg);
					}
					this.addReceivedMessage(msg);
					this.addSender(msg);
					publishProgress(msg);
				}
			}
		} catch (IOException e) {
			Log.i(TAG, "Socket closed, shutting down background thread: " + e);
		}
		return ((Void) null);
	}

	private void forward(MessageInfo msg) {
		// TODO Auto-generated method stub
		
		MessageInfo tem = new MessageInfo(msg.getSender(), msg.getAddress(), msg.getPort(), msg.getLatitude(), msg.getLatitude(), msg.getMessage(), msg.getChatroom(), msg.getType());
		
		String type = tem.getType();
		IPs = new ArrayList<String>();
		ports = new ArrayList<Integer>();
		
		if(type.equalsIgnoreCase("message")){
			String chatroom = tem.getChatroom();
			ContentResolver cr = getService().getContentResolver();
			String[] peer_projection = new String[] {ChatContent.Peers.HOST, ChatContent.Peers.PORT};
			String peer_where = ChatContent.Peers.CHATROOM + "= ?";
			String[] peer_selection = new String[] {chatroom};

			Cursor c = cr.query(ChatContent.Peers.CONTENT_URI, peer_projection, peer_where, peer_selection, null);
			
			for(c.moveToFirst();!c.isAfterLast(); c.moveToNext()){
				IPs.add(c.getString(0));		
				ports.add(Integer.parseInt(c.getString(1)));
			}
			if(ports.size() != 0){
				for(int j = 0; j < ports.size(); j++){
					InetAddress ip;
					try {
						ip = InetAddress.getByName(IPs.get(j));
						tem.setAddress(ip);
						tem.setPort(ports.get(j));
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
					}
					getService().send(tem);
				}
			}
		}
	}

	private void addReceivedMessage(MessageInfo msg) {
		/*
		 * Add sender and message to the content provider for received messages.
		 */
		String type = msg.getType();
		
		if(type.equalsIgnoreCase("message")){
			ContentValues values = new ContentValues();
			values.put(ChatContent.Messages.SENDER, msg.getSender());
			values.put(ChatContent.Messages.MESSAGE, msg.getMessage());
			values.put(ChatContent.Messages.TYPE, msg.getType());
			values.put(ChatContent.Messages.CHATROOM, msg.getChatroom());
			ContentResolver cr = getService().getContentResolver();
			cr.insert(ChatContent.Messages.CONTENT_URI, values);
		}		
	}

	private void addSender(MessageInfo msg) {

		/*
		 * Add sender information to content provider for peers
		 * information, if we have not already heard from them. If repeat
		 * message, update location information.
		 */
		ContentResolver cr = getService().getContentResolver();
		
			ContentValues values = new ContentValues();
			values.put(ChatContent.Peers.NAME, msg.getSender());
			values.put(ChatContent.Peers.HOST, msg.getAddress()
					.getHostAddress());
			values.put(ChatContent.Peers.PORT, msg.getPort());
			values.put(ChatContent.Peers.LATITUDE, msg.getLatitude());
			values.put(ChatContent.Peers.LONGITUDE, msg.getLongitude());
			values.put(ChatContent.Peers.CHATROOM, msg.getChatroom());

			String[] projection = new String[] { ChatContent.Peers.NAME };
			String where = ChatContent.Peers.NAME + "= ?" + " and " + ChatContent.Peers.CHATROOM + "= ?";
			String[] selectionArgs = new String[] { msg.getSender(), msg.getChatroom() };

			Cursor c = cr.query(ChatContent.Peers.CONTENT_URI, projection,
					where, selectionArgs, null);
			if (!c.moveToFirst()) {
				cr.insert(ChatContent.Peers.CONTENT_URI, values);
			} else {
				cr.update(ChatContent.Peers.CONTENT_URI, values, where,
						selectionArgs);
			}
		}

	@Override
	protected void onProgressUpdate(MessageInfo... values) {
		/*
		 * Logic for updating the Messages cursor is done on the UI thread.
		 */
		getService().sendBroadcast(msgUpdateBroadcast);
		/*
		 * Progress update for UI thread: The notification is given a
		 * "pending intent," so that if the user selects the notification,
		 * that pending intent is used to launch the main UI (ChatApp).
		 */
		String svcName = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager;
		notificationManager = (NotificationManager) this.chatService.getSystemService(svcName);

		Context context = this.chatService.getApplicationContext();
		String expandedText = values[0].getMessage();
		String expandedTitle = "M:" + values[0].getSender();
		Intent startActivityIntent = new Intent(this.chatService,
				ChatRoomListActivity.class);
		PendingIntent launchIntent = PendingIntent.getActivity(context, 0,
				startActivityIntent, 0);

		this.chatService.newMessageNotification.setLatestEventInfo(context, expandedTitle,
				expandedText, launchIntent);
		this.chatService.newMessageNotification.when = java.lang.System.currentTimeMillis();

		notificationManager.notify(ChatService.NOTIFICATION_ID, this.chatService.newMessageNotification);

		Toast.makeText(context, expandedTitle, Toast.LENGTH_SHORT).show();
	}
	
	private Intent msgUpdateBroadcast = new Intent(IChatService.NEW_MESSAGE_BROADCAST);

	@Override
	protected void onPostExecute(Void result) {
		/*
		 * The background thread is stopped by closing the socket, which is done
		 * in the service, so no point in stopping the service here.  The backbround
		 * thread would shut down the service if the background thread decided when to
		 * terminate.
		 */
//		this.chatService.stopSelf();
	}
}