 package ussr.samples.atron.network;
 /**
  * A simple ATRON network controller for the USSR ATRON car.
  * The controller() is the same as in the USSR 
  * 
  *   @author lamik06
  */
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import ussr.samples.atron.ATRONController;
 import ussr.samples.atron.spot.IATRONSPOTController;
 
 
 public class ATRONNetworkController extends ATRONController implements IATRONSPOTController {
 	protected byte dir = 0;
 	private byte rot = 0;
 	private static Socket socket;
 	private static BufferedReader in = null;
 	private static PrintWriter out = null;
 	private static String dataSend = "0";
 	private static String name;
     byte dataReceive = 0;
 	private static final byte driveCmdstop = 0;
 	private static final byte driveCmdForward = 1;
 	private static final byte driveCmdReverse = 2;
 	private static final byte steeringCmdNeutral = 0;
 	private static final byte steeringCmdRigth = 1;
 	private static final byte steeringCmdLeft = 2;
 	byte[] message = new byte[2];
 	private byte message_old;
 	private boolean printDebugInfo = true;
 	private String server;
 	private int port;
 
 		
 	public ATRONNetworkController(String[] args){
 		try {
 			name = args[0];
 			server = args[1];
 			port = Integer.parseInt(args[2]);
 			socket = new Socket(server, port);
 			out = new PrintWriter(socket.getOutputStream(), true);
 	        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         } catch (NumberFormatException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (UnknownHostException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 
 	public void loop(){
 		System.out.println("Remote controller " + name + " is ready and connected to: " + server +":"+port );
 		while(true){
 			try {
 
 				message[0] = (byte) Integer.parseInt(in.readLine());
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				System.out.println("Communication error");
 				System.exit(-1);
 			}
 			controller();
 			out.println(getDataSend());
 		}
 	}
 	
 	/*
 	 * ATRON controller application
 	 */	
 	public void controller(){
 		if(name.startsWith("w")) {
 			switch (message[0]) {
 			case driveCmdstop:
 				dir = 0;
 				break;
 			case driveCmdForward:
 				dir = 1;
 				break;
 			case driveCmdReverse:
 				dir = -1;
 				break;
 			default:
 				break;
 			}
 			
 			if(name.equals("wheel1") || name.equals("wheel3")) {
 				if (dir != 0) {
 					rotateContinuous(dir);
 				}else{
 					centerStop();
 				}
 			}
 			if(name.equals("wheel2") || name.equals("wheel4")){
 				if (dir != 0) {
 					rotateContinuous(-dir);
 				}else{
 					centerStop();
 				}
 			}
 			
 		}	
 		if(name.startsWith("a")) {			
 			switch(message[1]){
 				case steeringCmdNeutral:
 					rot = 0;
 					break;
 				case steeringCmdRigth:
 					rot = -1;
 					break;
 				case steeringCmdLeft:
 					rot = 1;
 					break;
 				}
 			if (message_old != message[0]){
 				message_old = message[0];
 				sendMessage(message, (byte)message.length, (byte)4);
 				sendMessage(message, (byte)message.length, (byte)6);
 				if (printDebugInfo == true) System.out.println("Name: " + name + " message[0] = " + message[0]);
 			}
 			if(name=="axleOne5"){
 				if (rot != 0) {
 					rotateToDegreeInDegrees((rot*15)+180);
 				}else{
 					rotateToDegreeInDegrees(180);
 				}
 			}
 		}
 	}
 
 	public String getDataSend() {
 		return dataSend;
 	}
 
 	public void setDataSend(String dataSend) {
 		ATRONNetworkController.dataSend = dataSend;
 	}
   
 
 	public void centerStop() {
 		// TODO Auto-generated method stub
 		setDataSend("0");
 	}
 
	// @Override // Not JDK 1.5 compliant
 	public void controllerYield() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void handleMessage(byte[] message, int messageSize, int channel) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void rotateContinuous(float dir) {
 		if(dir == 1){
 			setDataSend("1");			
 		}else{
 			setDataSend("-1");			
 		}
 
 		
 	}
 
 	@Override
 	public void rotateDegrees(int degrees) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public byte sendMessage(byte[] message, byte messageSize, byte connector) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
	// @Override // Not JDK 1.5 compliant
 	public byte sendRadioMessage(byte[] message, int destination) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void activate() {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 }
