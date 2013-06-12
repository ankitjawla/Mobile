package edu.stevens.cs522.chat.messages;

import java.io.IOException;

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
import edu.stevens.cs522.chat.providers.ChatContent;
import edu.stevens.cs522.chat.ui.ChatRoomListActivity;

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
				this.addReceivedMessage(msg);		
				this.addSender(msg);
				publishProgress(msg);
			}
		} catch (IOException e) {
			Log.i(TAG, "Socket closed, shutting down background thread: " + e);
		}
		return ((Void) null);
	}

	private void addReceivedMessage(MessageInfo msg) {
		/*
		 * Add sender and message to the content provider for received messages.
		 */
		ContentValues values = new ContentValues();
		values.put(ChatContent.Messages.SENDER, msg.getSender());
		values.put(ChatContent.Messages.MESSAGE, msg.getMessage());
		ContentResolver cr = getService().getContentResolver();
		cr.insert(ChatContent.Messages.CONTENT_URI, values);
	}

	private void addSender(MessageInfo msg) {

		/*
		 * Add sender information to content provider for peers
		 * information, if we have not already heard from them. If repeat
		 * message, update location information.
		 */
		ContentValues values = new ContentValues();
		values.put(ChatContent.Peers.NAME, msg.getSender());
		values.put(ChatContent.Peers.HOST, msg.getAddress().getHostAddress());
		values.put(ChatContent.Peers.PORT, msg.getPort());
		values.put(ChatContent.Peers.LATITUDE, msg.getLatitude());
		values.put(ChatContent.Peers.LONGITUDE, msg.getLongitude());

		String[] projection = new String[] { ChatContent.Peers.NAME };
		String where = ChatContent.Peers.NAME + "= ?";
		String[] selectionArgs = new String[] { msg.getSender() };

		ContentResolver cr = getService().getContentResolver();
		Cursor c = cr.query(ChatContent.Peers.CONTENT_URI, projection, where,
				selectionArgs, null);
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