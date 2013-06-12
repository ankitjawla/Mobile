package edu.stevens.cs522.chat.rooms;

import java.util.ArrayList;
import java.util.List;

public class Chatrooms {

	private List<Chatroom> rooms;
	
	private Chatrooms() {
		rooms = new ArrayList<Chatroom>();
		rooms.add(Chatroom.getDefault());
	}
	
	public static Chatrooms createChatrooms() {
		return new Chatrooms();
	}
	
	public List<String> getRoomNames() {
		List<String> names = new ArrayList<String>();
		for (Chatroom room : rooms) {
			names.add(room.getName());
		}
		return names;
	}
	
	public Chatroom getRoom(int index) {
		return rooms.get(index);
	}
	
}
