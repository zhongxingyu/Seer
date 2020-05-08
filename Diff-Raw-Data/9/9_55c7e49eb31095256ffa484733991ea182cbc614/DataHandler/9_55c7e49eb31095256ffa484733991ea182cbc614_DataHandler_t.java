 package serverstorage;
 
 
 import java.util.Vector;
 
 
 
 public class DataHandler {
 	
 	
 	StringBuffer data_store;
 	int server_version_number; 
 	Vector<Message> in_queue; // Input packets must be stored here
 	Vector<HistoryElement> history_queue;
 	
 	
 	public DataHandler(){
 		server_version_number=0;
 		in_queue = new Vector<Message>();
 		history_queue = new Vector<HistoryElement>();
 		data_store = new StringBuffer();
 		for (int i=0; i <1000; i++)
 			data_store.append(" ");
 	}
 	
 	
 	
 	public void InsertSimple( Message in_packet){
 
 		HistoryElement history_element=new HistoryElement();
 		server_version_number++;
 		if(in_packet.getCharacter_pressed()==Constants.BACKSPACE)
 			data_store.deleteCharAt(in_packet.getPosition() - 1);
 		else if (in_packet.getCharacter_pressed()==Constants.DELETE)
 			data_store.deleteCharAt(in_packet.getPosition());
 		else
 			data_store.insert(in_packet.getPosition(), in_packet.getCharacter_pressed());
 		
 		history_element.setCharacter_pressed(in_packet.getCharacter_pressed());
 		history_element.setClient_id(in_packet.getClient_id());
 		history_element.setPosition(in_packet.getPosition());
 		history_element.setVersion_number(server_version_number);
 		history_element.setClient_version_number(in_packet.getClient_version_number());
 		
 		history_queue.add(history_element);
 	}
 	
 	
 	
 	public AckBroadcast InsertDataIntoServerStore(Message in_packet){
 		/*while (!in_queue.isEmpty()){
 			Message in_packet = new Message();
 			HistoryElement history_element = new HistoryElement();
 			
 			in_packet = in_queue.remove(0); */
 			
 			AckBroadcast ack_broadcast;
			
			if(in_packet.getCharacter_pressed()==Constants.BACKSPACE && in_packet.getPosition()==0){
				 ack_broadcast = new AckBroadcast('0', 0, 0, -1, 0);						 
				 return ack_broadcast;
				
			}
 		
 			if (in_packet.getBuffer_version_number()==server_version_number){
 				
 				InsertSimple( in_packet);
 						
 			} 
 			else if (in_packet.getBuffer_version_number()<server_version_number){
 								
 				for(int i=0;i<history_queue.size();i++){
 					if (history_queue.elementAt(i).getClient_version_number()==in_packet.getClient_version_number() && history_queue.elementAt(i).getClient_id()==in_packet.getClient_id()){
 						 ack_broadcast = new AckBroadcast('0', 0, 0, -1, 0);						 
 						 return ack_broadcast;
 
 					}
 						
 					
 					if (history_queue.elementAt(i).getVersion_number()>in_packet.getBuffer_version_number()){
 						if(history_queue.elementAt(i).getClient_id()!=in_packet.getClient_id() 
 								&& history_queue.elementAt(i).getPosition()<=in_packet.getPosition())
							if(history_queue.elementAt(i).getCharacter_pressed()==Constants.BACKSPACE || history_queue.elementAt(i).getCharacter_pressed()==Constants.DELETE )
 								in_packet.setPosition(in_packet.getPosition() - 1);
 							else
 								in_packet.setPosition(in_packet.getPosition() +	 1);
 							// decimal 8 == backspace and decimal24 == delete from SOURCE
 					}
 				}
 				
 				InsertSimple(in_packet);
 	
 			}
 			
 			else
 				System.out.println("Error: version numbers are corrupted \n");
 				
 
 	
 	 ack_broadcast = new AckBroadcast(in_packet.getCharacter_pressed(),in_packet.getPosition(),server_version_number,
 			 in_packet.getClient_version_number(), in_packet.client_id );
 	 
 	 return ack_broadcast;
 
 	 
 	}	
 }
