 package de.tobiaswegner.communication.cul4java.impl;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import de.tobiaswegner.communication.cul4java.FHTListener;
 import de.tobiaswegner.communication.cul4java.FS20Listener;
 
 public class FHTHandler {
 	ArrayList<FHTListener> listeners = new ArrayList<>();
 	
 	public void registerListener (FHTListener listener) {
 		if (!listeners.contains(listener))
 			listeners.add(listener);
 	}
 	
 	public void unregisterListener (FHTListener listener) {
 		if (listeners.contains(listener))
 			listeners.remove(listener);
 	}
 
 	protected void ParseFHT (String line)
 	{
 		try
 		{
 			System.out.println("Received FHT frame: " + line);
 			
 			if (line.length() == 11)
 			{
 				//is FHT8v frame
 				String houseCode = line.substring(1, 5); // dev
 				String command = line.substring(5, 7); // cde
 				String origin = line.substring(7, 9); // ??
 				String argument = line.substring(9, 11); // val
 				
 				//$dmsg = sprintf("81%02x04xx0909a001%s00%s", $len/2+7, substr($dmsg,1,6), substr($dmsg,7));
				String cmd = FHTConstants.getIDByCode(Byte.parseByte(command, 16));
 				
 				if (!cmd.equals("unknown"))
 					System.out.print("FHT " + houseCode + ": " + cmd + "=" + argument + "\r\n");
 				
 			}
 			else if (line.length() == 9)
 			{
 				//is FHT80b frame
 				String device = line.substring(1, 7);
 				String argument = line.substring(7, 9);
 				
 				int state = 0;
 				
 				if ((argument.startsWith("1")) || (argument.startsWith("9")))
 				{
 					state = FHTConstants.FHT8b_STATE_LOWBAT;
 					
 					System.out.println("Sensor: " + device + " battery low");
 				}
 				
 				if (argument.substring(1).equals("1"))
 				{
 					state = FHTConstants.FHT8b_STATE_WINDOW_OPEN;
 
 					System.out.println("Sensor: " + device + " opened");
 				}
 
 				if (argument.substring(1).equals("2"))
 				{
 					state = FHTConstants.FHT8b_STATE_WINDOW_CLOSED;
 
 					System.out.println("Sensor: " + device + " closed");
 				}
 				
 				if (state != 0) {
 					Iterator<FHTListener> listenerIterator = listeners.iterator();
 					
 					while (listenerIterator.hasNext())
 					{
 						FHTListener listener = listenerIterator.next();
 						listener.FHT8bReceived(device, state);
 					}
 				}
 			}
 		} catch (StringIndexOutOfBoundsException e) {								
 			e.printStackTrace();
 			
 			System.out.println("line: " + line);								
 		}	
 	}
 }
