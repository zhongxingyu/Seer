 package ui.isometric.mock;
 
 import game.*;
 
 /**
  * 
  * An implementation of ClientMessageHandlerMock that just prints out any calls to it, used for testing of ui
  * 
  * @author melby
  *
  */
 public class ClientMessageHandlerMock implements ClientMessageHandler {
 	@Override
 	public void sendMessage(ClientMessage message) {
 		System.out.println("Perform: " + message);
 	}

	@Override
	public void sendChat(String chatText) {
		System.out.println("Send chat: " + chatText);
		
	}
 }
