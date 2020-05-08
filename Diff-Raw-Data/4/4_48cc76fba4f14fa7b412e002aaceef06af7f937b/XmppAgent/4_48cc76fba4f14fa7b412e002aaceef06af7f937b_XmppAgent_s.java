 package com.bluesky.visualprogramming.remote.xmpp;
 
 import java.util.Map;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.log4j.Logger;
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.ChatManager;
 import org.jivesoftware.smack.ChatManagerListener;
 import org.jivesoftware.smack.MessageListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 
 import com.bluesky.visualprogramming.core.Message;
 import com.bluesky.visualprogramming.core.MessageType;
 import com.bluesky.visualprogramming.core.ObjectRepository;
 import com.bluesky.visualprogramming.core.ObjectScope;
 import com.bluesky.visualprogramming.core.ObjectType;
 import com.bluesky.visualprogramming.core.ParameterStyle;
 import com.bluesky.visualprogramming.core._Object;
 import com.bluesky.visualprogramming.core.value.Link;
 import com.bluesky.visualprogramming.core.value.StringValue;
 import com.bluesky.visualprogramming.remote.ConnectionOptions;
 import com.bluesky.visualprogramming.vm.VirtualMachine;
 
 public class XmppAgent {
 
 	static Logger logger = Logger.getLogger(XmppAgent.class);
 
 	String server;
 	String username;
 	String password;
 
 	XMPPConnection connection;
 
 	MessageListener messageListener;
 
 	// private SessionStatus sessionStatus=SessionStatus.Normal;
 
 	/**
 	 * it is a sync request.
 	 */
 	Message lastRequestMessage;
 
 	public XmppAgent(String address, _Object obj, String connectionOptions) {
 
 		Map<String, String> optMap = new ConnectionOptions(connectionOptions).map;
 
 		if (optMap.containsKey("server"))
 			server = optMap.get("server");
 		else {
 			int idx = address.indexOf('@');
 			server = address.substring(idx + 1);
 		}
 
 		if (optMap.containsKey("username"))
 			username = optMap.get("username");
 		else {
 			int idx = address.indexOf('@');
 			username = address.substring(0, idx);
 		}
 
 		if (optMap.containsKey("password"))
 			password = optMap.get("password");
 		else {
 			password = "";
 		}
 
 		messageListener = new MessageListener() {
 			@Override
 			public void processMessage(Chat chat,
 					org.jivesoftware.smack.packet.Message msg) {
 
 				if (msg.getBody() != null && !msg.getBody().trim().isEmpty()) {
 					receivedMessage(msg);
 				}
 			}
 
 		};
 	}
 
 	public void connect() {
 		try {
 
 			connection = new XMPPConnection(server);
 
 			connection.connect();
 
 			connection.login(username, password);
 
 			final ChatManager chatManager = connection.getChatManager();
 			chatManager.addChatListener(new ChatManagerListener() {
 
 				@Override
 				public void chatCreated(Chat chat, boolean createdLocally) {
 					chat.addMessageListener(getMessageListener());
 
 				}
 			});
 		} catch (XMPPException e) {
 
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void disconnect() {
 		connection.disconnect();
 	}
 
 	public void send(String receiverAddress, Message msg) throws XMPPException {
 
 		if (msg.messageType.isRequest())
 			lastRequestMessage = msg;
 
 		ChatManager chatManager = connection.getChatManager();
 		Chat chat = chatManager.createChat(receiverAddress,
 				getMessageListener());
 
 		// TODO convert body to hierarchical text
 		String msgBody = "";
 		if (msg.body != null) {
 			if (msg.body.getType() == ObjectType.NORMAL) {
 				for (int i = 0; i < msg.body.getChildCount(); i++) {
 					if (i != 0)
 						msgBody += ";";
 
 					_Object param = msg.body.getChild(i);
 					msgBody += param.getName() + ":" + param.getValue();
 
 				}
 			} else if (msg.body.getType().isValueObject()) {
 				msgBody = msg.body.getValue();
 
 			}
 
 		}
 		
 		
 		//String response = String.format("[%s] %s", msg.getSubject(), msgBody);
 		
		String escaped = StringEscapeUtils.escapeXml(msgBody);
		chat.sendMessage(escaped);
 	}
 
 	private String reviseAddress(String addr) {
 		int idx = addr.lastIndexOf("/");
 		if (idx >= 0)
 			return addr.substring(0, idx);
 		else
 			return addr;
 	}
 
 	private void receivedMessage(org.jivesoftware.smack.packet.Message msg) {
 
 		try {
 			if (logger.isDebugEnabled())
 				logger.debug(String.format("from:%s, to:%s,subject:%s,body:%s",
 						msg.getFrom(), msg.getTo(), msg.getSubject(),
 						msg.getBody()));
 
 			VirtualMachine vm = VirtualMachine.getInstance();
 			ObjectRepository repo = vm.getObjectRepository();
 			StringValue returnValue = (StringValue) repo.createObject(
 					ObjectType.STRING, ObjectScope.ExecutionContext);
 
 			// TODO convert msg.Body to _Object.
 			returnValue.setValue(msg.getBody());
 
 			String senderAddress = reviseAddress(msg.getFrom());
 
 			if (lastRequestMessage != null
 					&& lastRequestMessage.receiver instanceof Link
 					&& ((Link) lastRequestMessage.receiver).getAddress()
 							.equals(senderAddress)) {
 				// address match. it must be reply.
 				if (logger.isDebugEnabled())
 					logger.debug("it is a reply");
 
 				Message replyMsg = new Message(false,
 						lastRequestMessage.receiver, lastRequestMessage.sender,
 						"RE:" + lastRequestMessage.getSubject(), returnValue,
 						ParameterStyle.ByName, null, MessageType.SyncReply);
 
 				replyMsg.urgent = true;
 
 				// it can only be replied once.
 				lastRequestMessage = null;
 
 				vm.getPostService().sendMessage(replyMsg);
 			} else {// not a reply. it is a request.
 				if (logger.isDebugEnabled())
 					logger.debug("it is not a reply");
 
 				Link senderLink = (Link) repo.createObject(ObjectType.LINK,
 						ObjectScope.ExecutionContext);
 				senderLink.setValue("xmpp://"
 						+ reviseAddress(reviseAddress(msg.getFrom())));
 
 				Link receiverLink = (Link) repo.createObject(
 
 				ObjectType.LINK, ObjectScope.ExecutionContext);
 				
 				receiverLink.setValue("xmpp://"
 						+ reviseAddress(reviseAddress(msg.getTo())));
 
 				// TODO convert msg.body to _Object
 				Message normalMsg = new Message(true, senderLink, receiverLink,
 						msg.getBody(), null, ParameterStyle.ByName, null,
 						MessageType.Normal);
 
 				normalMsg.urgent = false;
 
 				vm.getPostService().sendMessage(normalMsg);
 
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected MessageListener getMessageListener() {
 		return messageListener;
 	}
 }
