 package org.pointrel.pointrel20120623.demos;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 
 import org.jdesktop.swingworker.SwingWorker;
 import org.pointrel.pointrel20120623.core.Session;
 import org.pointrel.pointrel20120623.core.TransactionVisitor;
 import org.pointrel.pointrel20120623.core.Utility;
 
 import com.fasterxml.jackson.core.JsonEncoding;
 import com.fasterxml.jackson.core.JsonFactory;
 import com.fasterxml.jackson.core.JsonGenerator;
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.core.JsonToken;
 
 // TODO: Need to make communications with server run in the background
 
 public class SimpleChatApp {
 	
 	public static void main(String[] args) {
 		File archive = new File("./PointrelArchive");
 		Session session = new Session(archive);
 		//Session session = new Session("http://twirlip.com/pointrel/");
 		final JFrame frame = new JFrame(FrameNameBase);
 		final SimpleChatApp app = new SimpleChatApp(session);
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				JPanel appPanel = app.openGUI();
 				frame.setSize(600, 600);
 				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 				frame.add(appPanel);
 				frame.setVisible(true);
 			}
 		});
 	}
 	
 	public static String FrameNameBase = "Simple Chat";
 	
 	public int maxChatItemsOnRefresh = 20;
 	
 	Session session;
 	
 	JPanel appPanel = new JPanel();
 	
 	JPanel chatPanel = new JPanel();
 	JTextField userIDTextField = new JTextField();
 	JTextArea chatLogTextArea = new JTextArea();
 	JScrollPane chatLogTextAreaScrollPane = new JScrollPane(chatLogTextArea);
 	JTextField sendTextField = new JTextField();
 	JButton sendButton = new JButton("Send");	
 	JButton refreshButton = new JButton("Refresh");	
 	
 	final String chatAppVariableName = SimpleChatApp.class.getCanonicalName();
 	
 	// TODO: Figure out what to do about UUID of chat
 	String chatAppChatUUID = "default";
 	
 	// TODO: Maybe can generalize ChatItem and ChatItemVisitor with ListItem somehow?
 	
 	class ChatItem {
 		final public static String ContentType = "text/vnd.pointrel.SimpleChatApp.ChatItem.json";
 		final public static String Version = "20120623.0.1.0";
 		
 		final String chatUUID;
 		final String timestamp;
 		final String userID;
 		final String chatMessage;
 		
 		ChatItem(String chatUUID, String timestamp, String userID, String chatMessage) {
 			this.chatUUID = chatUUID;
 			this.timestamp = timestamp;
 			this.userID = userID;
 			this.chatMessage = chatMessage;
 		}
 		
 		public ChatItem(byte[] content) throws IOException {
 			boolean typeChecked = false;
 			boolean versionChecked = false;
 			String chatUUID_Read = null;
 			String timestamp_Read = null;
 			String userID_Read = null;
 			String chatMessage_Read = null;
 			
 			JsonFactory jsonFactory = new JsonFactory();
 			ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
 			JsonParser jsonParser = jsonFactory.createJsonParser(inputStream);
 
 			if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
 				throw new IOException("Expected data to start with an Object");
 			}
 			
 			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
 				String fieldName = jsonParser.getCurrentName();
 				jsonParser.nextToken();
 
 				if (fieldName.equals("type")) {
 					String type = jsonParser.getText();
 					if (!ContentType.equals(type)) {
 						throw new RuntimeException("Expected type of " + ContentType + "  but got : " + type);
 					}
 					typeChecked = true;
 				} else if (fieldName.equals("version")) {
 					String version = jsonParser.getText();
 					if (!Version.equals(version)) {
 						throw new RuntimeException("Expected version of " + Version + "  but got : " + version);
 					}
 					versionChecked = true;
 				} else if (fieldName.equals("chatUUID")) {
 					chatUUID_Read = jsonParser.getText();
 				} else if (fieldName.equals("timestamp")) {
 					timestamp_Read = jsonParser.getText();
 				} else if (fieldName.equals("userID")) {
 					userID_Read = jsonParser.getText();
 				} else if (fieldName.equals("chatMessage")) {
 					chatMessage_Read = jsonParser.getText();
 				} else {
 					throw new IOException("Unrecognized field '" + fieldName + "'");
 				}
 			}
 			jsonParser.close();
 			
 			if (!typeChecked) {
 				throw new RuntimeException("Expected type of " + ContentType + "  but no  field");
 			}
 			
 			if (!versionChecked) {
 				throw new RuntimeException("Expected version of " + Version + "  but no version field");
 			}
 			
 			chatUUID = chatUUID_Read;
 			timestamp = timestamp_Read;
 			userID = userID_Read;
 			chatMessage = chatMessage_Read;
 		}
 		
 		public byte[] toJSONBytes() {
 			try {
 				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 				JsonFactory jsonFactory = new JsonFactory();
 				JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(outputStream, JsonEncoding.UTF8);
 
 				jsonGenerator.useDefaultPrettyPrinter();
 				  
 				jsonGenerator.writeStartObject();
 				jsonGenerator.writeStringField("type", ContentType);
 				jsonGenerator.writeStringField("version", Version);
 				jsonGenerator.writeStringField("chatUUID", chatUUID);
 				jsonGenerator.writeStringField("timestamp", timestamp);
 				jsonGenerator.writeStringField("userID", userID);
 				jsonGenerator.writeStringField("chatMessage", chatMessage);
 				jsonGenerator.writeEndObject();
 				jsonGenerator.close();
 				return outputStream.toByteArray();
 			} catch (IOException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 		
 		public String toString() {
 			return timestamp + " | " + userID + " | " + chatMessage;
 		}
 
 		public String getLogText() {
 			String timestampForLog = timestamp.replace("T", " ");
 			timestampForLog = timestampForLog.replace("Z", " GMT");
 			timestampForLog = "<" + timestampForLog + ">";
 
 			String message = chatMessage;
 			if (!message.endsWith("\n")) message += "\n";
 			
 			String logText = userID + " "  + timestampForLog + ":\n" + message;
 			return logText;
 		}
 	}
 	
 	class ChatItemCollector extends TransactionVisitor {
 		ArrayList<ChatItem> chatItems = new ArrayList<ChatItem>();
 		final int maximumCount;
 		final String chatUUID;
 		
 		ChatItemCollector(String chatUUID, int maximumCount) {
 			this.chatUUID = chatUUID;
 			this.maximumCount = maximumCount;
 		}
 		
 		// TODO: Maybe should handle removes, too? Tricky as they come before the inserts when recursing
 		
 		public boolean resourceInserted(String resourceUUID) {
 			byte[] chatItemContent = session.getContentForURI(resourceUUID);
 			if (chatItemContent == null) {
 				System.out.println("content not found for chat item: " + resourceUUID);
 			}
 			ChatItem chatItem;
 			try {
 				chatItem = new ChatItem(chatItemContent);
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
 			if (chatItem.chatUUID.equals(chatUUID)) {
 				chatItems.add(chatItem);
 				if (maximumCount > 0 && chatItems.size() >= maximumCount) return true;
 			}
 			return false;
 		}
 	}
 	
 	public SimpleChatApp(Session session) {
 		this.session = session;
 	}
 
 	// Finds all chat items for a chatUUID up to a maximumCount (use zero for all)
 	ArrayList<ChatItem> loadChatItemsForUUID(String uuid, int maximumCount) {
 		// TODO: Should create, maintain, and use an index
 		String transactionURI = session.getVariable(chatAppVariableName);
 		ChatItemCollector visitor = new ChatItemCollector(uuid, maximumCount);
 		TransactionVisitor.visitAllResourcesInATransactionTreeRecursively(session, transactionURI, visitor);
 		return visitor.chatItems;			
 	}
 
 	public JPanel openGUI() {
 		appPanel.setLayout(new BorderLayout());
 		appPanel.add(chatPanel, BorderLayout.CENTER);
 		
 		userIDTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, userIDTextField.getPreferredSize().height));
 		// uriTextField.setEditable(false);
 		
 		sendTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, sendTextField.getPreferredSize().height));
 		
 		chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
 		chatPanel.add(userIDTextField);
 		chatPanel.add(chatLogTextAreaScrollPane);
 		chatPanel.add(sendTextField);
 		chatPanel.add(sendButton);
 		chatPanel.add(refreshButton);
 		
 		chatLogTextArea.setLineWrap(true);
 		chatLogTextArea.setWrapStyleWord(true);
 		
 		hookupActions();
 		
 		userIDTextField.setText("ENTER_USERID");
 		
 		return appPanel;
 	}
 
 	private void hookupActions() {
 		sendButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {sendButtonPressed(); }});
 		
 		// Do something when enter is pressed
 		sendTextField.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {sendButtonPressed(); }});
 
 		refreshButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {refreshButtonPressed(); }});
 		
 		// Update every ten seconds
 		ActionListener runnable = new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (refreshButton.isEnabled()) refreshButtonPressed();
 			}
 		};
 		runnable.actionPerformed(null);
 		Timer timer = new Timer(10000, runnable);
 		timer.start();
 	}
 
 	protected void refreshButtonPressed() {
 		refreshButton.setEnabled(false);
 		sendButton.setEnabled(false);
		SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
 			public String doInBackground() {
 				ArrayList<ChatItem> chatItems = loadChatItemsForUUID(chatAppChatUUID, maxChatItemsOnRefresh);
 				// List<ChatItem> chatItemsSubset = chatItems.subList(0, Math.min(MaxChatItemsOnRefresh, chatItems.size()));
 				Collections.reverse(chatItems);
 				StringBuffer stringBuffer = new StringBuffer();
 				for (ChatItem chatItem: chatItems) {
 					stringBuffer.append(chatItem.getLogText());
 				}
 				return stringBuffer.toString();
 			}
 			public void done() {
 				try {
 					String result = get();
 					chatLogTextArea.setText(result);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				} catch (ExecutionException e) {
 					e.printStackTrace();
 				}
 				refreshButton.setEnabled(true);
 				sendButton.setEnabled(true);
 			}
 		};
 		worker.execute();
 	}
 
 	protected void sendButtonPressed() {
 		final String userID = userIDTextField.getText();
 		if (userID.length() == 0 || userID.equals("USERID")) {
 			JOptionPane.showMessageDialog(appPanel, "Please set the user ID first");
 			return;
 		}
 		session.setUser(userID);
 		final String message = sendTextField.getText();
 		sendTextField.setText("");
 		sendButton.setEnabled(false);
 		refreshButton.setEnabled(false);
 		
		SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
 			public String doInBackground() {
 				String timestamp = Utility.currentTimestamp();
 					
 				ChatItem chatItem = new ChatItem(chatAppChatUUID, timestamp, userID, message);
 				String uri = session.addContent(chatItem.toJSONBytes(), ChatItem.ContentType);
 				session.addSimpleTransactionForVariable(chatAppVariableName, uri, "New chat message");
 				
 				if (message.equals("test100")) {
 					test100();
 				}
 				return chatItem.getLogText();
 			}
 			public void done() {
 				String theText = null;
 				try {
 					theText = get();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				} catch (ExecutionException e) {
 					e.printStackTrace();
 				}
 				if (theText != null) {
 					chatLogTextArea.append(theText);
 				}
 				sendButton.setEnabled(true);
 				refreshButton.setEnabled(true);
 			}
 		};
 		worker.execute();
 	}
 
 	// Could be reworked to use SwingWorker's publish/process methods
 	private void test100() {
 		System.out.println("starting writing 100 chat items: " + Utility.currentTimestamp());
 		for (int i = 0; i < 100; i++) {
 			String timestamp = Utility.currentTimestamp();
 			String message = "Testing... #" + i + " " + timestamp;
 			
 			final ChatItem chatItem = new ChatItem(chatAppChatUUID, timestamp, "TestUserID", message);
 			String uri = session.addContent(chatItem.toJSONBytes(), ChatItem.ContentType);
 			session.addSimpleTransactionForVariable(chatAppVariableName, uri, "New chat message");
 			
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					chatLogTextArea.append(chatItem.getLogText());
 				}
 			});	
 		}
 		System.out.println("finishing writing 100 chat items: " + Utility.currentTimestamp());
 	}
 }
