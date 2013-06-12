package edu.stevens.cs522.chat.messages;


public interface IChatService {
	
	public static final String NEW_MESSAGE_BROADCAST = "edu.stevens.cs522.chat.NewMessageBroadcast";

	public void send (MessageInfo message);

}
