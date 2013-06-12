package edu.stevens.cs522.chat.messages;

import java.net.InetAddress;

import android.content.Context;
import edu.stevens.cs522.chat.MainActivity;
import edu.stevens.cs522.chat.R;

public class MessageUtils {

	/*
	 * This operation is called by the parent activity of a fragment (via the
	 * sender callback) in response to a UI prompt to send a message.
	 */
//	public static void send(Context context, IChatService service,
//			InetAddress addr, int port, String message, String chatroom, String type) {
//
//		String sender = context.getString(R.string.user_name);
//
//		double longitude = Double.parseDouble(context
//				.getString(R.string.longitude));
//
//		double latitude = Double.parseDouble(context
//				.getString(R.string.latitude));
//
//		MessageInfo msg = new MessageInfo(sender, addr, port, longitude,
//				latitude, message, chatroom, type);
//
//		service.send(msg);
//	}

	public static void sendWithInfo(IChatService service, 
			String sender, double longitude, double latitude, InetAddress addr, int port, String message, String chatroom, String type) {
		String sender_2 = MainActivity.name;
		double latitude_2 = Double.parseDouble(MainActivity.latitude);
		double longitude_2 = Double.parseDouble(MainActivity.longitude);
		MessageInfo msg = new MessageInfo(sender_2, addr, port, longitude_2, latitude_2, message, chatroom, type);
		service.send(msg);
	}
	
	public static void sendWithInfo(IChatService service, InetAddress addr, int port, String chatroom, String type) {
		String sender = MainActivity.name;
		double latitude = Double.parseDouble(MainActivity.latitude);
		double longitude = Double.parseDouble(MainActivity.longitude);
		String message = "request";
		MessageInfo msg = new MessageInfo(sender, addr, port, longitude,
				latitude, message, chatroom, type);
		service.send(msg);
	}

}
