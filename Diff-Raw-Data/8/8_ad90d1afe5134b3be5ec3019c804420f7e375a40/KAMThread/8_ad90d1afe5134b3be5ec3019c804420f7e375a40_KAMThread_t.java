 package net.threads;
 
 import gui.Drawing;
 
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.*;
 
 import state.FactoryState;
 import net.ServerThread;
 
 public class KAMThread extends ServerThread {
 	protected HashMap<String, Object> drawings;
 
 	public KAMThread(Socket s, ObjectOutputStream o, ObjectInputStream i, FactoryState st) {
 		super(s, o, i, st);
 		// TODO Auto-generated constructor stub
 		drawings = new HashMap<String,Object>();
 		
 		//put the string "drawings", the use the array list in factorystate and addKAM drawlist
 		for(Drawing d: state.kamDrawList){
 			
 			drawings.put("drawings", d);
 		}
 		
		run();
		
 	}
 
 	@Override
 	public void loop() {
 		// TODO Auto-generated method stub
 		
		/*drawings.clear();
 		//put the string "drawings", the use the array list in factorystate and addKAM drawlist
 		for(Drawing d: state.kamDrawList){
 			
 			drawings.put("drawings", d);
		}*/
 		
 		receiveAndSend(drawings);
 		
 
 	}
 
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
