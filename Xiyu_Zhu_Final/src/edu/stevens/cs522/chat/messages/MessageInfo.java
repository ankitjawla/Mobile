/*********************************************************************

    Information about a message that has been received.
    
    Copyright (c) 2012 Stevens Institute of Technology

 **********************************************************************/

package edu.stevens.cs522.chat.messages;

import java.io.Serializable;
import java.net.InetAddress;

public class MessageInfo implements Serializable {

	private static final long serialVersionUID = -6050463241441669252L;

	private String sender;
	private InetAddress address;
	private int servicePort;
	private double latitude;
	private double longitude;
	private String message;
	private String chatroom;
	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getChatroom() {
		return chatroom;
	}

	public void setChatroom(String type) {
		this.chatroom = type;
	}

	public String getSender() {
		return sender;
	}

	public int getPort() {
		return servicePort;
	}
	
	public void setPort(int port) {
		this.servicePort = port;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getMessage() {
		return message;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress addr) {
		address = addr;
	}

	public MessageInfo(String s, InetAddress a, int p, double lat, double lng,
			String m, String c, String t) {
		sender = s;
		address = a;
		servicePort = p;
		latitude = lat;
		longitude = lng;
		message = m;
		chatroom = c;
		type = t;
	}

}
