package edu.stevens.cs522.chat.ui;

import java.net.InetAddress;

/**
 * This interface is provided by the parent activity of the fragment that
 * accepts messages to be sent, in order for the activity to actually perform
 * the send (in cooperation with a service for sending messages).
 */
public interface ISendMessage {

	public void send(InetAddress addr, int port, String message);

}
