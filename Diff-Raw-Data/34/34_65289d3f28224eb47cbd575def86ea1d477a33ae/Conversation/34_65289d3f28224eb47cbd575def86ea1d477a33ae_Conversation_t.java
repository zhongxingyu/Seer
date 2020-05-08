 package com.ingloriouscoders.chatbackend;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.Vector;
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.XMPPError;
 
 import com.ingloriouscoders.sheep.MessageBubble;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 import java.util.List;
 import java.util.ArrayList;
 
 
 public class Conversation{
 	
 	private List<OnNewMessageListener> newMessageListeners = new ArrayList<OnNewMessageListener>();
 	private OnUnreadMessagesListener mUnreadListener = null;
 	
 	private ChatContext mContext;
 	private Contact mOpposite;
 	protected Chat mChat;
 	
 	private int unreadCount = 0;
 	private boolean notificationMuted = false;
 	
 	private List<Message> history = new Vector<Message>();
 	
 	public static Conversation spawnConversation(Contact _opposite,ChatContext _ctx)
 	{
 		List<Conversation> active_conv = _ctx.getActiveConversations();
 		for (int i=0;i<active_conv.size();i++)
 		{
 			if (active_conv.get(i).getOpposite().getAddress().equals(_opposite.getAddress()) )
 			{
 				return _ctx.getActiveConversations().get(i);
 			}
 		}
 		return new Conversation(_opposite, _ctx);
 	}
 	
 	private Conversation(Contact _opposite,ChatContext _ctx)
 	{
 		mContext = _ctx;
 		mOpposite = _opposite;
 		mContext.etablishConversation(this);	
 		//this.setOnUnreadMessagesListener(mOpposite.unreadListener);
 	}
 	public Contact getOpposite()
 	{
 		return mOpposite;
 	}
 	public List<Message> getHistory(int timestamp)
 	{
 		this.unreadCount = 0;
 		return history;
 	}
 	
 	public ChatContext getContext()
 	{
 		return mContext;
 	}
 	public Message prepareMessage(String _params, boolean _internal)
 	{
 		Message newmsg = new Message();
 		newmsg.setSender(mContext.getUserShowname());
 		newmsg.setIncoming(false);
 		newmsg.setInternal(_internal);
 		newmsg.giveParameters(_params);
 
 		return newmsg;
 	}	
 	private void addMessage(Message _msg)
 	{
 		history.add(_msg);
 		Log.v("chatbackend","added Message: Size=" + history.size());
 	}
 	public boolean sendMessage(Message _msg, boolean _internal, String _params, boolean _timestamp)
 	{
 		if (mChat == null)
 		{
 			Log.v("chatbackend","Chat is null");
 			return false;
 		}
 		if (!mContext.isConnected())
 		{
 			Log.v("chatbackend","Nicht mit Server verbunden");
 			return false;
 		}
 		try {
 			/*org.jivesoftware.smack.packet.Message newMessage = new org.jivesoftware.smack.packet.Message();
 			newMessage.setBody(_msg.getMessageText());*/
 			
 			XMPPMessage msg = new XMPPMessage(XMPPMessage.CHAT);
 			
 			msg.setBody(_msg.getMessageText());
 			
 			/*if (_timestamp)
 				msg.setTimestamp();*/
 					
 			msg.addParameters(_params);
 			
 		    mChat.sendMessage(msg);
 		}
 		catch (XMPPException e) {
 		    Log.v("chatbackend","Fehler beim Zustellen der Nachricht: '" + _msg.getMessageText() + "'");
 		}
 		finally
 		{
 			this.addMessage(_msg);
 		}
 		Log.v("ChatBackend","Nachricht: " + _msg.getMessageText() + " wurde versendet.");
 		return true;
 	}
 	
	public String getParamFromId(String _params, String id) {
		String param = null;
		
		if (_params.contains("&")) {
			String[] paramsSplit = _params.split("&");
			for(int i=1;i<paramsSplit.length;i++) {
					if (paramsSplit[i].startsWith(id)) {
						param = paramsSplit[i].substring(id.length()+1, paramsSplit[i].length());
						return param;
					}
			}
		}
		
		return param;
	}
	
 	private final Conversation thisclass = this;
 	protected org.jivesoftware.smack.MessageListener smack_listener =  new org.jivesoftware.smack.MessageListener() {
 		@Override
 		public void processMessage(Chat _chat,org.jivesoftware.smack.packet.Message _msg) {
 			boolean proc_internal = false;
 			
 			String proc_params = "";
 			
 			if (_msg.getBody().equals("") && _msg.getType() == org.jivesoftware.smack.packet.Message.Type.error && _msg.getSubject().startsWith("INTERNALMESSAGE"))
 				proc_internal = true;
 			
 			if (_msg.getSubject() != null && _msg.getSubject().indexOf("&") != -1)
 				proc_params = _msg.getSubject();
 			
			if (!proc_params.equals("")) {
				String timeStamp = getParamFromId(_msg.getSubject(), "time");
				if (timeStamp != null) {
					Log.v("Conversation.java","Getting timestamp from message.");
				}
			} else {
 				Calendar c = Calendar.getInstance();
				String timestampStr = "&time=" + String.valueOf(c.getTimeInMillis() + 100000);
 				proc_params = proc_params + timestampStr;
				
				Log.v("Conversation.java","Getting timestamp from current time.");
 			}
 			
 			//NEUER SYNTAX foo = new Message(String _msgtext, String _sender, boolean _incoming, int _color, boolean _internal, String params);
			Message recieved_msg = new Message(_msg.getBody(), thisclass.getOpposite().getShowname(), true, 1, proc_internal, proc_params);
 					
 			if (_msg.getType() == org.jivesoftware.smack.packet.Message.Type.error)
 			{
 				XMPPError myerr = _msg.getError();
 				Log.v("chatbackend","Fehler bei der Übermittlung: " + myerr.getMessage() + " | " + myerr.getCode());
 				
 				return;
 			}
 			
 			thisclass.addMessage(recieved_msg);
 			
 			unreadCount++;
 			
 			for ( OnNewMessageListener mListener : Conversation.this.newMessageListeners )
 			{
 				mListener.onNewMessage(thisclass,recieved_msg );
 			}
 			
 			Conversation.this.getContext().childNewMessageListener.onNewMessage(Conversation.this,recieved_msg);
 		}
 	};
 	public void resetUnreadCount()
 	{
 		this.unreadCount = 0;
 		if (this.mUnreadListener != null)
 		{
 			mUnreadListener.onNewMessage(this.getOpposite(), this.unreadCount);
 		}
 		Log.v("luca","reset count");
 		
 	}
 	
 	public void addOnNewMessageListener(OnNewMessageListener _listener)
 	{
 		this.newMessageListeners.add(_listener);
 	}
 	public void removeOnNewMessageListener(OnNewMessageListener _listener)
 	{
 		this.newMessageListeners.remove(_listener);
 	}
 	public void setNotificationMuted(boolean state)
 	{
 		this.notificationMuted = state;
 	}
 	public boolean getNotificationMuted()
 	{
 		return this.notificationMuted;
 	}
 
 }
