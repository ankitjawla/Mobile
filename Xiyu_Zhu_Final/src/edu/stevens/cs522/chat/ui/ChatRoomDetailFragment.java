package edu.stevens.cs522.chat.ui;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
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
import edu.stevens.cs522.chat.MainActivity;
import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.providers.ChatContent;


/**
 * A fragment representing a single ChatRoom detail screen. This fragment is
 * either contained in a {@link ChatRoomListActivity} in two-pane mode (on
 * tablets) or a {@link ChatRoomDetailActivity} on handsets.
 */
public class ChatRoomDetailFragment extends Fragment implements
LoaderCallbacks<Cursor> {

	private final static String TAG = ChatRoomDetailFragment.class
			.getCanonicalName();

	/*
	 * Adapter for displaying received messages.
	 */
	CursorAdapter messageAdapter;
	
	String ip;
	int port;
	
	List<String> ips = new ArrayList<String>();
	List<Integer> ports = new ArrayList<Integer>();
	
	
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String CHATROOM_ID_KEY = "chatroom_id";
	public static final String DEFAULT_CHATROOM_ID = "MAIN";
	
	public String chatroom_name;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ChatRoomDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getArguments().getString(CHATROOM_ID_KEY);


	}

	private ISendMessage sender;
	private SendMessageWithRoomName chatroom_sender;
	
	public ListView  msglist;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		/*
		 * TODO Bind parent activity callbacks here.
		 */

		try {
			sender = (ISendMessage) activity;
			chatroom_sender = (SendMessageWithRoomName)activity;
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

//		ContentValues values = new ContentValues();
//		values.put(ChatContent.Chatrooms.NAME, chatroom_name);
		

		message = (EditText) rootView.findViewById(R.id.message_text);

		msglist = (ListView)rootView.findViewById(R.id.msgList);
		/*
		 * TODO: Messages content provider should be linked to the listview
		 * named "msgList" in the UI: 1. Build a cursor that projects Messages
		 * content. 2. Use a SimpleCursorAdapter to adapt this cursor for
		 * msgList listview. 3. Use messages_row layout for the list of messages
		 */
		
		String[] to = new String[] { ChatContent.Messages.SENDER, ChatContent.Messages.MESSAGE };
		int[] from = new int[] { R.id.messages_sender, R.id.messages_message };
		messageAdapter = new SimpleCursorAdapter(getActivity(), R.layout.messages_row, null, to, from, 0);
		
		msglist.setAdapter(messageAdapter);
		

		getLoaderManager().initLoader(0, null, this);


		/*
		 * End Todo
		 */

		send = (Button) rootView.findViewById(R.id.send_button);
		send.setOnClickListener(sendListener);

		return rootView;
	}
	
	/*
	 * Widgets for dest address, message text, send button.
	 */
	private EditText message;
	private Button send;

	/*
	 * On click listener for the send button
	 */
	private OnClickListener sendListener = new OnClickListener() {
		public void onClick(View v) {
			Log.d("postmessage!!!!!!","hah");
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
		String message_type = "message";
		String theNewMessage = message.getText().toString();
		
		ContentResolver cr_owner = getActivity().getContentResolver();
		String[] projection = new String[] { ChatContent.Chatrooms.OWNER };
		String where = ChatContent.Chatrooms.NAME + "= ?";
		String[] selectionArgs = new String[] { chatroom_name };	
		
		Cursor c_owner = cr_owner.query(ChatContent.Chatrooms.CONTENT_URI, projection, where, selectionArgs, null); 
		c_owner.moveToFirst();
		String owner = c_owner.getString(0);
		
		try {
			if(!owner.equals(MainActivity.name)){
				Log.d("client-side************************","client-sdie");	
				
				Cursor c = chatroom_sender.returnCursor();
				c.moveToFirst();
				ip = c.getString(0);
				port = Integer.parseInt(c.getString(1)); 

				InetAddress targetAddr = InetAddress
						.getByName(ip);

				if (sender != null) {

					sender.send(targetAddr, port, theNewMessage, chatroom_name, message_type);
				} else {
					Log.e(TAG, "No sender callback registered.");
				}
				message.setText("");
			}
			else {
				Log.d("server-side**************************", "server-side");

				Cursor c = chatroom_sender.serverReturnCursor();
				
				for(c.moveToFirst();!c.isAfterLast(); c.moveToNext()){
					ips.add(c.getString(0));
					ports.add(Integer.parseInt(c.getString(1)));
				}
				ContentResolver cr_server = getActivity().getContentResolver();
				for(int i = 0; i < ips.size(); i++){
					InetAddress targetAddr = InetAddress
							.getByName(ips.get(i));
					if (sender != null) {
						chatroom_name = getArguments().getString("CHATROOM_NAME");
						
						sender.send(targetAddr, ports.get(i), theNewMessage, chatroom_name,
								message_type);
					} else {
						Log.e(TAG, "No sender callback registered.");
					}
				}
				ContentValues values = new ContentValues();
				values.put(ChatContent.Messages.SENDER, MainActivity.name);
				values.put(ChatContent.Messages.MESSAGE, theNewMessage);
				values.put(ChatContent.Messages.TYPE, "message");
				values.put(ChatContent.Messages.CHATROOM, chatroom_name);
				
				cr_server.insert(ChatContent.Messages.CONTENT_URI, values);
				messageAdapter.notifyDataSetChanged();
				ips.clear();
				ports.clear();
				message.setText("");
			}

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
	
//	String[] projection = new String[] { ChatContent.Messages._ID,
//			 	ChatContent.Messages.SENDER, ChatContent.Messages.MESSAGE };
//	String where = chatroom_name;
	
//	String[] selection = new String[] { chatroom_name };
	
	
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {

		String[] projection = new String[] { ChatContent.Messages.SENDER, ChatContent.Messages.MESSAGE };		
		chatroom_name = getArguments().getString("CHATROOM_NAME");
		return new CursorLoader(getActivity(), ChatContent.Messages.CONTENT_URI, projection,
				chatroom_name, null, null);

	}

	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		this.messageAdapter.changeCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		loader = null;
		this.messageAdapter.changeCursor(null);
	}
	
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
	
	public interface SendMessageWithRoomName {
		public Cursor returnCursor();
		public Cursor serverReturnCursor();
	}

}
