 package com.apps4you.moderator;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 
 import com.apps4you.shared.Message;
 import com.apps4you.shared.MessageFactory;
 import com.apps4you.shared.Warrior;
 
 public class WarriorConnection implements Runnable {
 	private Warrior mWarrior;
 	private Socket mConnection;
 	private ObjectOutputStream outStream;
 	private ObjectInputStream inStream;
 	private Moderator mModerator;
 	private ModeratorUI mUiInstance;
 	
 	public WarriorConnection(Socket socket,Moderator moderator,ModeratorUI uiInstance){
 		mConnection = socket;
 		mModerator = moderator;
 		mUiInstance = uiInstance;
 		try {
 			inStream = new ObjectInputStream(socket.getInputStream());
 			outStream = new ObjectOutputStream(socket.getOutputStream());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public Socket getSocket(){
 		return mConnection;
 	}
 	public void setSocket(Socket  socket){
 		mConnection = socket;
 		try {
 
 			outStream = new ObjectOutputStream(socket.getOutputStream());
 			outStream.flush();
 			inStream = new ObjectInputStream(socket.getInputStream());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public ObjectOutputStream getOutputStream(){
 		return outStream;
 	}
 	public ObjectInputStream getInputStream(){
 		return inStream;
 	}
 	public Warrior getWarrior(){
 		return mWarrior;
 	}
 	public void setWarrior(Warrior warrior){
 		mWarrior = warrior;
 	}
 	public void run() {
 		// TODO Auto-generated method stub
 		String jsonString = null;
		while(!mConnection.isClosed()){
 			try {
 				jsonString = (String) inStream.readObject();
 				System.out.println("Debugging ProcessConnection - Message was: ***"+ jsonString + "***End Message***");
 				Message message = MessageFactory.fromJSON(jsonString);
 				processMessage(message);
 			}catch (EOFException e){
//				e.printStackTrace();
				// The client disconnected, so we will just close the connection from our end.
 				try {
 					mConnection.close();
 				
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 		
 	}
 	private void processMessage(Message inMessage){
 		System.out.println("Debugging ProcessConnection - Message is ***"+inMessage+"***");
 		switch (inMessage.getCommand()) {
 		case NEWWARRIOR:
 			System.out.println("Debugging ProcessConnection - In NEWWARRIOR case");
 			displayMessage("\nNew warrior added: "
 					+ inMessage.getWarrior().getName()); // display
 			mWarrior = inMessage.getWarrior();
 //			this.upgradeWarrior(inMessage.getWarrior());
 			sendData(MessageFactory.toJSON(new Message(mWarrior,Message.MessageCommand.GREETWARRIOR)));
 			mModerator.processNewWarrior(inMessage,mWarrior);
 //			sendData(MessageFactory.toJSON(mModerator
 //					.processNewWarrior(inMessage,mWarrior)));
 
 			break;
 			
 		case BATTLEWARRIOR:
 			System.out.println("Debugging ProcessConnection - In BATTLEWARRIOR case");
 			displayMessage("\nBattle commencing between: "
 					+ inMessage.getWarrior().getName() + " and " + inMessage.getOpponent().getName() + " with " + inMessage.getAction()); // display
 
 			sendData(MessageFactory.toJSON(new Message(mWarrior,Message.MessageCommand.GREETWARRIOR)));
 //		case SELECTACTION:
 //			System.out.println("Debugging ProcessConnection - In SELECTACTION case");
 //			sendData(MessageFactory.toJSON(new Message()));
 		default:  //Added for debugging to verify that the message was not falling out via not being handled.
 			System.out.println("Debugging ProcessConnection -Default portion of Switch which does nothing");
 		}
 		System.out.println("Debugging ProcessConnection - Out of the switch statment");
 	}
 	private void displayMessage(final String messageToDisplay) {
 		mUiInstance.displayText(messageToDisplay); // append message
 	} // end method displayMessage
 	
 	public void sendData(String message) {
 		try // send object to client
 		{
 			outStream.writeObject(message);
 			outStream.flush(); // flush output to client
 			displayMessage(message);
 		} // end try
 		catch (IOException ioException) {
 			displayMessage("\nError writing object");
 		} // end catch
 	} // end method sendData
 }
