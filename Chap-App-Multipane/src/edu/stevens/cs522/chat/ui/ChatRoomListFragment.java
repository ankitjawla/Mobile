package edu.stevens.cs522.chat.ui;

import java.util.ArrayList;
import java.util.List;

import edu.stevens.cs522.chat.R;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * A list fragment representing a list of ChatRooms. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link ChatRoomDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the
 * {@link IChatRoomManager} interface.
 */
public class ChatRoomListFragment extends ListFragment {

	private final static String TAG = ChatRoomListFragment.class.getCanonicalName();

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private IChatRoomManager chatRoomManager = dummyManager;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int activatedPosition = ListView.INVALID_POSITION;

	/**
	 * A dummy implementation of the {@link IChatRoomManager} interface that
	 * does nothing. Used only when this fragment is not attached to an
	 * activity.
	 */
	private static IChatRoomManager dummyManager = new IChatRoomManager() {
		@Override
		public void onItemSelected(String id) {
		}

		@Override
		public List<String> getRoomNames() {
			return new ArrayList<String>();
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ChatRoomListFragment() {
	}

	private List<String> roomNames;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		Log.d(TAG, "Attaching fragment to activity.");

		/*
		 * TODO Activities containing this fragment must implement its
		 * callbacks.
		 */
		final ChatRoomListActivity chatRoomListActivity = (ChatRoomListActivity) activity;
		chatRoomManager = new IChatRoomManager() {

			@Override
			public void onItemSelected(String id) {
				chatRoomListActivity.onItemSelected(id);
			}

			@Override
			public List<String> getRoomNames() {
				return chatRoomListActivity.getRoomNames();
			}
		};
		/*
		 * End To do
		 */
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
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Call to getActivity() must wait until activity is created.

		Log.d(TAG, "Creating activity.");

		roomNames = chatRoomManager.getRoomNames();
		
		Log.d(TAG, "Chat room size " + roomNames.size());
		
		/*
		 * TODO: replace with a list adapter for selecting a chatroom.
		 */
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, roomNames);
		setListAdapter(adapter);
		/*
		 * End To do
		 */
		Log.d(TAG, "finish create activity.");
	}

	@Override
	public void onDetach() {
		super.onDetach();

		Log.i(TAG, "Detaching fragment.");

		// Reset the active callbacks interface to the dummy implementation.
		chatRoomManager = dummyManager;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		chatRoomManager.onItemSelected(roomNames.get(position));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (activatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, activatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(activatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		activatedPosition = position;
	}
}
