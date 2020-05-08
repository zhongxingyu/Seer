 import java.io.IOException;
 import java.io.Serializable;
import java.net.ConnectException;
 import java.net.Socket;
 import java.util.HashMap;
 
 
 public class TaskStatus extends GenericPayload implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -220435675068678063L;
 	String messageType;
 	int taskId;
 
 	HashMap<String, String> taskStatus;
 	public void processPayload(Socket socket) {
 		try {
 			WriteLog.writelog(Machine.stName, "Received task status payload.Subtype : " + messageType + " ***");
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		if (messageType.equals("get")) {
 			try {
 				WriteLog.writelog(Machine.stName, "Received task status request for task ID " + taskId);
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			TaskStatus response = new TaskStatus();
 			response.messageType = new String("response");
 			response.taskStatus = new HashMap<String, String>();
 			synchronized (MapleJuiceListener.task_map) {
 				if (MapleJuiceListener.task_map.containsKey(new Integer(taskId))) {
 					boolean all_done= true;
 
 					HashMap <String, Process> temp = MapleJuiceListener.task_map.get(taskId);
 					String taskState = null;
 					for (String fileName : temp.keySet()) {
 						Process tempProcess = temp.get(fileName);		   			
 						try {
 							if (tempProcess == null) {
 								taskState = new String("To be scheduled");
 								all_done = false;
 							}else {
 								int val = tempProcess.exitValue();
 								if (val == 0) {
 									taskState = new String("Success");
 								}else {
 									taskState = new String("Failed");
 								}
 							}
 							response.taskStatus.put(fileName, taskState);
 
 						}catch (IllegalThreadStateException a){
 							response.taskStatus.put(fileName, "In progress");
 							all_done = false;
 						}
 					}
 					/*if (all_done) {
 						MapleJuiceListener.task_map.remove(taskId);
 					}*/
 
 				} 
 				MapleJuicePayload statusResponse = new MapleJuicePayload("TaskStatus"); 
 				statusResponse.setByteArray(response);
 				System.out.println("%%%%%%%%%%%%%%%%%%%%%%  " + statusResponse.messageType + "&&&&&&&&&&&&&&&&  " + statusResponse.payload.toString() + "\n");
				try {
 				statusResponse.sendMapleJuicePacket(socket);
				} catch (ConnectException c){
					System.out.println("Send packet failed");
				}
 				//for (Integer task_id: MapleJuiceListener.task_map.keySet()) {
 				//}
 			}
 		}
 	}
 
 
 }
