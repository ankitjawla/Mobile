package edu.stevens.cs522.chat.rooms;

public class Chatroom {
	
	private String name;
	
	private Chatroom(String name) {
		this.name = name;
	}
	
	public static Chatroom createRoom(String name) {
		return new Chatroom(name);
	}
	
	private static Chatroom defaultRoom = createRoom("MAIN");
	
	public static Chatroom getDefault() {
		return defaultRoom;
	}
	
	public String getName() {
		return name;
	}
}
