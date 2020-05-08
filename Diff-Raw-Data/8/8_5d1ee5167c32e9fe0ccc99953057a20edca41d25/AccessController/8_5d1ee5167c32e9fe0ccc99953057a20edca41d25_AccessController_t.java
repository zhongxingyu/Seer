 package controller.impl;
 
 import hydna.ntnu.student.api.HydnaApi;
 import hydna.ntnu.student.listener.api.HydnaListener;
 
 import java.util.Scanner;
 
 import communication.Message;
 import communication.Serializer;
 import controller.api.IAccessController;
 
 public abstract class AccessController implements IAccessController {
 
 	protected HydnaApi hydnaSvc;
 	protected HydnaListener listener;
 	protected String id;
 	protected String location;
 	protected String accessPoint;
 	protected String authorizationServer;
 	protected String type;
 
 	public void setUp() {
 		this.id = this.type+System.currentTimeMillis(); 
 		this.listener = new HydnaListener() {
 			
 			@Override
 			public void systemMessage(String msg) {
 				System.out.println("got sysmsg: "+msg);
 			}
 			
 			@Override
 			public void signalRecieved(String msg) {
 				System.out.println("got signal: "+msg);
 			}
 			
 			@Override
 			public void messageRecieved(String msg) {
 				System.out.println("Message received in AccessController!");
 				System.out.println(msg);
 				Message m = Serializer.deSerialize(msg);
 				handleMessage(m);
 			}
 		};
 		/*Scanner scanIn = new Scanner(System.in);
 		System.out.println("Where is the controller located?");
 	    this.location = scanIn.nextLine();
 	    System.out.println("Which access point is the controller controlling?");
 		this.accessPoint = scanIn.nextLine();
 		scanIn.close();*/
 		this.location = "testlocation";
 		this.accessPoint = "testdoor";
 		hydnaSvc.registerListener(this.listener);
 		hydnaSvc.stayConnected(true);
 		hydnaSvc.connectChannel("ttm3-access-control.hydna.net/"+this.location, "rwe");
 		System.out.println("Controller "+id+" active.");
 		//requestIdentification();
 		grantAccess();
 	}
 	
 	private void handleMessage(Message msg) {
		System.out.println(msg.getType());
 		if (msg.getTo().equals(this.id)) {
 			if (msg.getType().equals(Message.ACCESSRSP)) {
 				handleAuthorizationResponse(msg.getData(Message.ACCESS));
 			}
 		}
 	}
 	
 	public abstract void requestAuthorization(String passcode);
 	
 	public abstract void handleAuthorizationResponse(String result);
 	
 	protected void grantAccess() {
 		System.out.println("Granting access...");
 		Message msg = new Message(Message.OPEN, this.accessPoint, this.id);
 		hydnaSvc.sendMessage(Serializer.serialize(msg));
 	}
 	
 	protected void revokeAccess() {
 		System.out.println("Revoking access...");
 		Message msg = new Message(Message.CLOSE, this.accessPoint, this.id);
 		hydnaSvc.sendMessage(Serializer.serialize(msg));
 	}
 
 }
