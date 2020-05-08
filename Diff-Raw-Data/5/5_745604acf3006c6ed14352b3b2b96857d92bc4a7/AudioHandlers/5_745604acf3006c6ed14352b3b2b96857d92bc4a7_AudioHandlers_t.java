 package com.example.projectbat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class AudioHandlers 
 {
 	private final BluetoothService btService;
 	
 	public final Map<Integer, Handler> handlerMap = new HashMap<Integer, Handler>();
 	
 	private int turn = 0;
 	
 	public AudioHandlers(final BluetoothService btServ)						 
 	{
 		btService = btServ;
 		
 		handlerMap.put(btService.BUILDING_DONE, new Handler() 
 		{			
 			public void handler(ArrayList<String> data) 
 			{
 				String sender = data.get(1);				
 				btService.btInterface.displayMessage("Received from: " + sender);
 				
 				int myIndex = btService.addresses.indexOf(btService.btAdapter.getAddress());
 				if (myIndex == turn)
					btService.sendToId(btService.addresses.get(1), "", btService.START_LISTENING);
 			}
 		});
 		
 		handlerMap.put(btService.START_LISTENING, new Handler()
 		{
 			public void handler(ArrayList<String> data) 
 			{			
 				btService.btInterface.displayMessage("Starting listening");
 				
 				String sender = data.get(1);				
				btService.sendToId(sender, "", btService.ACK_LISTENING);
 			}
 		});
 		
 		handlerMap.put(btService.ACK_LISTENING, new Handler()
 		{
 			public void handler(ArrayList<String> data) 
 			{			
 				btService.btInterface.displayMessage("Received ack listening.");
 				
 				String sender, msg;
 				sender = data.get(1);
 				msg = data.get(2);
 			}
 		});
 		
 		handlerMap.put(btService.TIME_MEASUREMENT, new Handler()
 		{
 			public void handler(ArrayList<String> data) 
 			{			
 				btService.btInterface.displayMessage("Received time measurement.");
 				
 				String sender, msg;
 				sender = data.get(1);
 				msg = data.get(2);
 			}
 		});
 	}
 }
 
 interface Handler
 {
 	public void handler(ArrayList<String> data);
 }
