 package client.model;
 
 import client.ClientController;
 import events.NetworkEvent;
 
 abstract public class ChronosModel {
	private final ClientController controller;
 	private final ChronosType chronosType;
 
 	public enum ChronosType {
 		EVENT_CONFIG, INVITATION, LOGIN, ROOM_BOOK, CALENDAR, USER_LIST
 	}
 
 	/**
 	 * fires a networkEvent from the model, which in turn forwards the event to
 	 * the clientController, that sends it to the server
 	 * 
 	 * @param event
 	 *            the event sent to server
 	 */
 	public void fireNetworkEvent(NetworkEvent event) {
 		controller.sendNetworkEvent(event);
 	}
 
 	public ChronosModel(ClientController controller, ChronosType chronosType) {
 		this.chronosType = chronosType;
 		this.controller = controller;
 		controller.addModel(this);
 
 	}
 
 
 	public ChronosType getType() {
 		return chronosType;
 	}
 
 	abstract public void receiveNetworkEvent(NetworkEvent event);
 
 	abstract public NetworkEvent newNetworkEvent();
 }
