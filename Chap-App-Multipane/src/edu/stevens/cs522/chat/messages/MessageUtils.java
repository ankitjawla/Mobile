package edu.stevens.cs522.chat.messages;

import java.net.InetAddress;

import android.content.Context;
import edu.stevens.cs522.chat.R;

public class MessageUtils {

	/*
	 * This operation is called by the parent activity of a fragment (via the
	 * sender callback) in response to a UI prompt to send a message.
	 */
	public static void send(Context context, IChatService service,
			InetAddress addr, String sender, double longitude, double latitude, int port, String message) {

		MessageInfo msg = new MessageInfo(sender, addr, port, longitude,
				latitude, message);

		service.send(msg);
	}
}
