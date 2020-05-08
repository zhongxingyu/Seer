 package net.threads;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Map;
 
 import state.FactoryState;
 import state.OrderConfig;
 import net.ServerThread;
 
 public class OrderThread extends ServerThread {
 	Map<String, Object> sendMap;
 	
 	public OrderThread(Socket s, ObjectOutputStream o, ObjectInputStream i, FactoryState st) {
 		super(s, o, i, st);
 
 		sendMap = new HashMap<String, Object>();
 	}
 
 	public void loop() {
 		sendMap.clear();
 		
		state.out.println("Hello there!");
		
 		ByteArrayInputStream bais = new ByteArrayInputStream(state.baos.toByteArray());
 		byte[] array = new byte[bais.available()];
 		try {
 			bais.read(array);
 			state.baos.flush();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		
 		sendMap.put("console", new String(array));
 		sendMap.put("configlist", state.kitConfigList);
 		sendMap.put("orders", state.orderConfigList);
 		
 		synchronized(state.orderConfigList) {
 			Map<String, Object> tmp = this.receiveAndSend(sendMap);
 			sendMap.clear();
 			sendMap.putAll(tmp);
 			
 			//Process input
 			OrderConfig result = (OrderConfig)sendMap.get("order");
 			if(result != null)
 				state.orderConfigList.add(result);
 		}
 		
 		//This is necessary to slow down the rate at which the thread runs
 		//(otherwise it bogs down the factory).
 		try {
 			Thread.sleep(5);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
