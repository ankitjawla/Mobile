package edu.stevens.cs522.chat.ui;

import java.util.List;

/**
 * A callback interface for activities that call the fragment for displaying a
 * list of the available chatrooms. This mechanism allows the fragment to
 * populate a selection list of current chatrooms, and to notify the parent
 * activity of a selection.
 */
public interface IChatRoomManager {
	/**
	 * Callback for populating a list of chatroom names.
	 */
	public List<String> getRoomNames();

	/**
	 * Callback for when an item has been selected.
	 */
	public void onItemSelected(String id);
}