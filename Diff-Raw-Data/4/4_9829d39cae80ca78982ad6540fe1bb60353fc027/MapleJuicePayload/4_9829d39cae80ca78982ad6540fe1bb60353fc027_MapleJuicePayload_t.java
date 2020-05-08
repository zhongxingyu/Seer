 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.*;
 
 
 
 //Payload types
 
 
 //Basic Payload
 public class MapleJuicePayload implements Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5737470900117994737L;
 	String           messageType;
 	//int           messageLength;
 	byte[]        payload;
 	
 
 	public static final int MapleActionType = 1 ;
 	public MapleJuicePayload(String mt) {
 		messageType = mt;
 		//messageLength = ml;
 		//payload = data;
 		
 	}
 	
 	public void setByteArray(Object originalObject) {
 		//byte[] op = new byte[1016];
 		ByteArrayOutputStream baos=null;
 		ObjectOutputStream oos=null;
 		baos = new ByteArrayOutputStream();
 		
 		try {
 			oos = new ObjectOutputStream(baos);
 			oos.writeObject(originalObject);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		payload = baos.toByteArray();
 	}
 	
 	public GenericPayload parseByteArray() {
 		ByteArrayInputStream bais = new ByteArrayInputStream(payload);
 		GenericPayload generic_action = null;
 		try {
 			ObjectInputStream oos = new ObjectInputStream(bais);
 			GenericPayload dummy=null;
 			try {
 				if(messageType.equalsIgnoreCase("MapleTask")) {
 					MapleAction maple_action  = (MapleAction)oos.readObject();
 					maple_action.printContents();
 					generic_action=(GenericPayload)maple_action;
 					//dummy = (GenericPayload) maple_action;
 					//dummy.printContents();
 				} else {
 					//TODO multiple packet formats will come here
 				}
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
			}
 			/*mapleTaskId = dummy.mapleTaskId;
 			machineId = dummy.machineId;
 			mapleExe = dummy.mapleExe;
 			inputFileInfo = dummy.inputFileInfo;
 			outputFilePrefix = dummy.outputFilePrefix;
 			
 			System.out.println(mapleTaskId);
 			System.out.println(machineId);
 			System.out.println(mapleExe);
 			System.out.println(inputFileInfo);
 			System.out.println(outputFilePrefix);*/
 			
 			//this = new MapleAction();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return generic_action;
 	}
 	
 	public void sendMapleJuicePacket(String targetNode) {
 		MapleJuiceClient mapleJuiceClient = new MapleJuiceClient(this, targetNode);
		mapleJuiceClient.start();
 	}
 }
