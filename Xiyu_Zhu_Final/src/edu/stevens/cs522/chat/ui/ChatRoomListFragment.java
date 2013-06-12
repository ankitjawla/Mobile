package edu.stevens.cs522.chat.ui;

import java.util.ArrayList;
import java.util.List;

import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.providers.ChatContent;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * A list fragment representing a list of ChatRooms. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link ChatRoomDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link IChatRoomManager}
 * interface.
 */
public class ChatRoomListFragment extends ListFragment implements LoaderCallbacks<Cursor>{

	private final static String TAG = ChatRoomListFragment.class.getCanonicalName();

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	
	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int activatedPosition = ListView.INVALID_POSITION;
	
	CursorAdapter chatroomAdapter;
	SimpleCursorAdapter messageAdapter;
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ChatRoomListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "Creating fragment.");
		
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Called after onCreateView() returns.
		
		Log.d(TAG, "Creating fragment view.");

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Call to getActivity() must wait until activity is created.
		registerForContextMenu(getListView());
		Log.d(TAG, "Creating activity.");
		messageAdapter = new SimpleCursorAdapter(getActivity(), 
				  android.R.layout.simple_list_item_activated_1, 
				  null, 
				  new String[] {ChatContent.Chatrooms.NAME}, 
				  new int[] { android.R.id.text1 },
				  0);

		setListAdapter(messageAdapter);
		getLoaderManager().initLoader(0, null, this);
	}
	
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		Log.i(TAG, "Detaching fragment.");

	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		String text = ((TextView)view).getText().toString();
		ChatRoomListActivity hostActivity = (ChatRoomListActivity) getActivity();
		hostActivity.onItemSelected(text);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getActivity().getMenuInflater();
	    inflater.inflate(R.menu.longpress, menu);
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(activatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		activatedPosition = position;
	}

	String[] projection = new String[] {ChatContent.Chatrooms._ID, ChatContent.Chatrooms.NAME};
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		return new CursorLoader(getActivity(), ChatContent.Chatrooms.CONTENT_URI, projection,
				null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		this.messageAdapter.changeCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		this.messageAdapter.changeCursor(null);
	}

}
