package edu.stevens.cs522.chat.ui;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.providers.ChatContent;

/**
 * A fragment representing a single ChatRoom detail screen. This fragment is
 * either contained in a {@link ChatRoomListActivity} in two-pane mode (on
 * tablets) or a {@link ChatRoomDetailActivity} on handsets.
 */
public class ChatRoomDetailFragment extends Fragment  {

	private final static String TAG = ChatRoomDetailFragment.class
			.getCanonicalName();

	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String CHATROOM_ID_KEY = "chatroom_id";
	public static final String DEFAULT_CHATROOM_ID = "MAIN";

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ChatRoomDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * getArguments().getString(CHATROOM_ID_KEY) should return chatroom id.
		 */

	}

	private ISendMessage sender;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		/*
		 * TODO Bind parent activity callbacks here.
		 */
		try {
			sender = (ISendMessage) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ISendMessage.");
		}
		/*
		 * End To do
		 */
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_chatroom_detail,
				container, false);

		destHost = (EditText) rootView.findViewById(R.id.dest_text);

		destPort = (EditText) rootView.findViewById(R.id.port_text);

		message = (EditText) rootView.findViewById(R.id.message_text);

		/*
		 * TODO: Messages content provider should be linked to the listview
		 * named "msgList" in the UI: 1. Build a cursor that projects Messages
		 * content. 2. Use a SimpleCursorAdapter to adapt this cursor for
		 * msgList listview. 3. Use messages_row layout for the list of messages
		 */
		ListView listview= (ListView) rootView.findViewById(R.id.msgList);
		String[] to = new String[] { ChatContent.Messages.SENDER, ChatContent.Messages.MESSAGE };
        int[] from = new int[] { R.id.messages_sender, R.id.messages_message };
        messageAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.messages_row,
				null,
				to,
				from,
				0);
        listview.setAdapter(messageAdapter);
        getLoaderManager().initLoader(0, null, loaderCallbacks);
		/*
		 * End Todo
		 */

		send = (Button) rootView.findViewById(R.id.send_button);
		send.setOnClickListener(sendListener);

		return rootView;
	}

	/*
	 * Adapter for displaying received messages.
	 */
	CursorAdapter messageAdapter;

	/*
	 * Widgets for dest address, message text, send button.
	 */
	private EditText destHost;
	private EditText destPort;
	private EditText message;
	private Button send;

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
		/*
		 * On the emulator, which does not support WIFI stack, we'll send to (an
		 * AVD alias for) the host loopback interface, with the server port on
		 * the host redirected to the server port on the server AVD.
		 */

		try {
			String targetAddrString = destHost.getText().toString();
			InetAddress targetAddr = InetAddress.getByName(targetAddrString);
			
			int targetPort = Integer.parseInt(destPort.getText().toString());

			String theNewMessage = message.getText().toString();

			if (sender != null) {
				sender.send(targetAddr, targetPort, theNewMessage);
			} else {
				Log.e(TAG, "No sender callback registered.");
			}
			message.setText("");

		} catch (UnknownHostException e) {

		}
	}
	
	/*
	 * TODO: Update the messages listview. Use a loadermanager to refresh the
	 * cursor. Use this as the projection:
	 * 
	 * String[] projection = new String[] { ChatContent.Messages._ID,
	 * ChatContent.Messages.SENDER, ChatContent.Messages.MESSAGE };
	 * 
	 * Once the cursor is loaded, tell the adapter to change its cursor.
	 */
	LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			String[] projection = new String[] { ChatContent.Messages._ID,
					 ChatContent.Messages.SENDER, ChatContent.Messages.MESSAGE };
			return new CursorLoader(getActivity(), ChatContent.Messages.CONTENT_URI, projection, null, null, null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
			ChatRoomDetailFragment.this.messageAdapter.changeCursor(cursor);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			ChatRoomDetailFragment.this.messageAdapter.changeCursor(null);
		}
		
	};
	
	/*
	 * End Todo
	 */

	/*
	 * Options menu includes an option to list all peers from whom we have
	 * received communication.
	 */

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.main, menu);
	}

}
