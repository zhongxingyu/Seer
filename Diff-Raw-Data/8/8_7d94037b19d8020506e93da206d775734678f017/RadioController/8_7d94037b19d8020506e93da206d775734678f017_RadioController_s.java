 package team035.modules;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 import java.util.Vector;
 
 import team035.messages.MessageAddress;
 import team035.messages.MessageWrapper;
 import team035.messages.RobotMessage;
 import team035.robots.BaseRobot;
 import battlecode.common.GameActionException;
 import battlecode.common.Message;
 
 public class RadioController {
 
 	protected static final String salt = "a23fo9anaewvaln32faln3falf3";
 
 	protected BaseRobot r;
 
 
 	protected int numMessagesInQueue = 0;
 	protected MessageWrapper[] outgoingMessageQueue;
 
 	public static final int MAX_MESSAGES_PER_TURN = 20;
 
 	protected boolean enabled = true;
 
 	// there's one of these for each message type, which has N objects that
 	// want to be notified about messages of that type.
 	protected HashMap<String, Vector<RadioListener>> listeners = new HashMap<String, Vector<RadioListener>>();
 
 	public RadioController(BaseRobot r) {
 		this.r = r;
 
 		this.newRound();
 	}
 
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 	}
 
 	public void addListener(RadioListener listener, String messageClass) {
 		String[] classes = new String[1];
 		classes[0] = messageClass;
 		this.addListener(listener, classes);
 	}
 
 	public void addListener(RadioListener listener, String[] messageClasses) {
 		Vector<RadioListener> listenersForClass;
 
 //		r.getLog().println("adding listener: " + listener + " for classes: " + messageClasses);
 
 		for(String c : messageClasses) {
 			if(c==null) break;
 
 //			r.getLog().println("adding listener for class: " + c);
 			if(listeners.get(c)==null) {
 				listenersForClass = new Vector<RadioListener>();
 			} else {
 				listenersForClass = listeners.get(c);
 			}
 			listenersForClass.add(listener);
 			listeners.put(c, listenersForClass);
 		}
 	}
 
 	public void addMessageToTransmitQueue(MessageAddress adr, RobotMessage m) {
 		outgoingMessageQueue[numMessagesInQueue] = new MessageWrapper(adr, m);
 		numMessagesInQueue++;
 	}
 
 	// Sends the messages we've queued up over the course of this turn.
 	public void transmit() {
 		try {
 			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
 			ObjectOutputStream out = new ObjectOutputStream(bytesOut);
 
 			int messagesWritten = 0;
 			for(MessageWrapper wrapper : this.outgoingMessageQueue) {
 				if(wrapper==null) {
 					break;
 				}
 				out.writeObject(wrapper);
 				messagesWritten++;
 			}
 			Message outgoingMessage = new Message();
 
 			
 			
 			
 			StringBuilder builder = new StringBuilder();
			builder.append(new String(bytesOut.toByteArray()));
 
 			String[] s = {builder.toString()};
 
 			builder.append(RadioController.salt);
 
 			int[] i = {builder.toString().hashCode()};
 
 			
 			outgoingMessage.strings = s;
 			outgoingMessage.ints = i;
 			if(messagesWritten > 0) {
 				this.r.getRc().broadcast(outgoingMessage);
 			}
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			r.getLog().printStackTrace(e);
 		} catch (GameActionException e) {
 			// TODO Auto-generated catch block
 			r.getLog().printStackTrace(e);
 		}
 
 		// clean out the queue
 		this.newRound();
 	}
 
 	private void newRound() {
 		this.outgoingMessageQueue = new MessageWrapper[MAX_MESSAGES_PER_TURN];
 		numMessagesInQueue=0;
 	}
 
 	// Handles any incoming messages we received this turn.
 	// If they pass basic validation, are addressed to us, and we have
 	// any listeners on that message type, alert the listeners to the
 	// existence of the message.
 	public void receive() {
 		if(!this.enabled) return;
 		Message[] messages = this.r.getRc().getAllMessages();
 		for(Message m : messages) {
 			// skip invalid messages
 			if(!this.validMessage(m)) {
 				continue;
 			}
			ByteArrayInputStream byteIn = new ByteArrayInputStream(m.strings[0].getBytes());
 			try {
 				ObjectInputStream in = new ObjectInputStream(byteIn);
 				Object nextObject = in.readObject();
 								if(nextObject.getClass() == MessageWrapper.class) {
 					MessageWrapper msg = (MessageWrapper)nextObject;
 					if(msg.isForThisRobot()) {
 						if(this.listeners.get(msg.msg.getType()) == null) continue;
 						
 						for(RadioListener l : this.listeners.get(msg.msg.getType())) {
 							l.handleMessage(msg);
 						}
 					}
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				r.getLog().printStackTrace(e);
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				r.getLog().printStackTrace(e);
 			}
 		}
 	}
 	
 	protected boolean validMessage(Message msg) {
 		// these tests ensure that the number of fields is right
 		// to make this a probable message from our team. 
 
 		if(msg.ints==null) return false;
 		if(msg.strings==null) return false;
 		if(msg.locations != null) return false;
 		
 		if(msg.ints.length!=1) return false;
 		if(msg.strings.length != 1) return false;
 
 		// now do the more rigorous check - does the hashcode of the message string
 		// match the int in the ints field?
 		StringBuilder builder = new StringBuilder();
 		builder.append(msg.strings[0]);
 		builder.append(RadioController.salt);
 		if(builder.toString().hashCode()!=msg.ints[0]) return false;
 
 		return true;
 	}
 }
