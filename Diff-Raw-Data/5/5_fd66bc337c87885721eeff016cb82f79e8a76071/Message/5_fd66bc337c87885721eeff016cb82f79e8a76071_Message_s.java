 /*
  * Message.java
  * 
  *  $Id $
  *  Author: smadden
  *
  *    Copyright (C) 2010 Sean Madden
  *
  *    Please see the pertinent documents for licensing information.
  *
  */
 
 package com.seanmadden.deepthought;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * This class represents a single message To/From an irc server.
  * 
  * @author Sean P Madden
  */
 public class Message {
 	private String nick = "";
 	private String usermask = "";
 	private String method = "";
 	private String message = "";
 	private String target = "";
 	
 	private long timestamp = 0;
 
 	private static Pattern JOIN = Pattern.compile("^:(.+)!(.+) JOIN :(.+)$");
 	private static Pattern PART = Pattern.compile("^:(.+)!(.+) PART (.+)$");
 	private static Pattern QUIT = Pattern.compile("^:(.+)!(.+) QUIT :(.+)$");
 	private static Pattern NAMES = Pattern
			.compile("^:(.+) 353 .+ #(.+) :(.+)$");
 	private static Pattern PRIVMSG = Pattern
			.compile("^:(.+)!(.+) PRIVMSG #(.+) :(.+)$");
 	private static Pattern PING = Pattern.compile("^PING :(.+)$");
 	private static Pattern MODE = Pattern.compile("^:(.+)!(.+) MODE (.+) (.+) (.+)$");
 
 	public static Message fromString(String message) {
 		String user = "", method = "", target = "", msg = "";
 		Matcher m = PRIVMSG.matcher(message);
 		if (m.matches()) {
 			user = m.group(1);
 			method = "PRIVMSG";
 			target = "#" + m.group(3);
 			msg = m.group(4);
 			return new Message(user, method, msg, target);
 		}
 		m = PING.matcher(message);
 		if (m.matches()) {
 			target = m.group(1);
 			method = "PING";
 			return new Message(user, method, msg, target);
 		}
 		m = JOIN.matcher(message);
 		if (m.matches()) {
 			user = m.group(1);
 			target = m.group(3);
 			method = "JOIN";
 			return new Message(user, method, msg, target);
 		}
 		m = PART.matcher(message);
 		if(m.matches()){
 			user = m.group(1);
 			target = m.group(3);
 			method = "PART";
 			return new Message(user, method, msg, target);
 		}
 		m = QUIT.matcher(message);
 		if(m.matches()){
 			user = m.group(1);
 			method = "QUIT";
 			return new Message(user, method, msg, target);
 		}
 		m = NAMES.matcher(message);
 		if(m.matches()){
 			user = m.group(3);
 			target = "#" + m.group(2);
 			method = "NAMES";
 			return new Message(user, method, msg, target);
 		}
 		m = MODE.matcher(message);
 		if(m.matches()){
 			user = m.group(1);
 			target = m.group(3);
 			method = "MODE";
 			message = m.group(4) + " " + m.group(5);
 		}
 		
 
 		return new Message(user, method, msg, target);
 	}
 
 	/**
 	 * Makes a message
 	 * 
 	 * @param usermask
 	 * @param method
 	 * @param message
 	 * @param target
 	 */
 	public Message(String message, String target) {
 		this("", "PRIVMSG", message, target);
 
 	}
 
 	public Message(String method, String message, String target) {
 		this("", method, message, target);
 	}
 
 	public Message(String user, String method, String message, String target) {
 		this.nick = user;
 		this.method = method;
 		this.message = message;
 		this.target = target;
 		this.timestamp = System.currentTimeMillis();
 	}
 
 	
 	/**
 	 * Returns the usermask
 	 * 
 	 * @return usermask the usermask
 	 */
 	public String getUsermask() {
 		return usermask;
 	}
 	/*/
 
 	/**
 	 * Sets the usermask
 	 * 
 	 * @param usermask
 	 *            the usermask to set
 	 */
 	public void setUsermask(String usermask) {
 		this.usermask = usermask;
 	}
 
 	/**
 	 * Returns the method
 	 * 
 	 * @return method the method
 	 */
 	public String getMethod() {
 		return method;
 	}
 
 	/**
 	 * Sets the method
 	 * 
 	 * @param method
 	 *            the method to set
 	 */
 	public void setMethod(String method) {
 		this.method = method;
 	}
 
 	/**
 	 * Returns the message
 	 * 
 	 * @return message the message
 	 */
 	public String getMessage() {
 		return message;
 	}
 
 	/**
 	 * Sets the message
 	 * 
 	 * @param message
 	 *            the message to set
 	 */
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	/**
 	 * Returns the target
 	 * 
 	 * @return target the target
 	 */
 	public String getTarget() {
 		return target;
 	}
 
 	/**
 	 * Sets the target
 	 * 
 	 * @param target
 	 *            the target to set
 	 */
 	public void setTarget(String target) {
 		this.target = target;
 	}
 
 	/**
 	 * Returns the nick
 	 *
 	 * @return nick the nick
 	 */
 	public String getNick() {
 		return nick;
 	}
 
 	/**
 	 * Sets the nick
 	 *
 	 * @param nick the nick to set
 	 */
 	public void setNick(String nick) {
 		this.nick = nick;
 	}
 
 	/**
 	 * Returns the timestamp
 	 *
 	 * @return timestamp the timestamp
 	 */
 	public long getTimestamp() {
 		return timestamp;
 	}
 
 	/**
 	 * Sets the timestamp
 	 *
 	 * @param timestamp the timestamp to set
 	 */
 	public void setTimestamp(long timestamp) {
 		this.timestamp = timestamp;
 	}
 
 	/**
 	 * Makes a message!
 	 * 
 	 * @see java.lang.Object#toString()
 	 * @return
 	 */
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 
 		builder.append(this.method);
 		builder.append(" ");
 		builder.append(this.target);
 		if (!this.message.equals("")) {
 			builder.append(" :");
 			builder.append(this.message);
 		}
 		builder.append("\r\n");
 
 		return builder.toString();
 	}
 
 }
