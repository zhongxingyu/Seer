 import java.io.IOException;
 import java.util.*;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 public class MapleJuiceListener implements Runnable {
 
 	private Machine m;
 	public static int task_id;
 	public static HashMap<Integer , HashMap<String, Process>> task_map; 
 	public static HashMap<String, ArrayList<String>> master_task_map;
 	Vector<String> freeNodeList;
 	Vector<String> pendingFileList;
 
 	public void start()
 	{
 		Thread server_thread = new Thread(this);
 
 		server_thread.start();
 	}
 	public MapleJuiceListener(Machine machine) {
 		task_map = new HashMap<Integer, HashMap<String, Process>>();
 		master_task_map = new HashMap<String, ArrayList<String>>();
 		m = machine;
 		task_id = 0;
 	}
 
 	public void processNodeFailure(String nodeName) {
 		try {
 			WriteLog.writelog(m.myName, "Node " + nodeName + "has failed. Adding it's files to pending file list");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		synchronized(master_task_map) {
 
 			if(master_task_map.containsKey(nodeName)) {
 				ArrayList<String> files = master_task_map.get(nodeName);
 				for (String filesToBeRescheduled :  files) {
 					pendingFileList.add(filesToBeRescheduled);
 				}
 			}
 			master_task_map.remove(nodeName);
 		}
 		try {
 			WriteLog.writelog(m.myName, "Pending File List after addition : " + pendingFileList);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void processMapleCommand(String mapleExe, Vector<String> filesToProcess, String outputFilePrefix) {
 
 
 		freeNodeList = new Vector<String>(m.memberList);
 		pendingFileList = new Vector<String>(filesToProcess);
 		for (int j = 0 ; j < freeNodeList.size(); j++) {
 			System.out.println(pendingFileList + "**** + filesToProcess \n\n\n");
 		}
 		//System.out.println(freeNodeList.elementAt(j));
 		boolean tasksComplete = false;
 		task_id++;
 		@SuppressWarnings("rawtypes")
 		ArrayList[] nodeFileList = null;
 		do {
 
 			System.out.println("\nInside");
 			if (pendingFileList.size() > 0 && freeNodeList.size() > 0) {
 
 				nodeFileList = new ArrayList[freeNodeList.size()];
 				for (int i = 0 ; i < pendingFileList.size() && i < freeNodeList.size(); i++) {				
 					nodeFileList[i] = new ArrayList<String>();
 				}
 
 				int i = 0;	
 				System.out.println("Size of pendingFileList : " + pendingFileList.size());
 				for (String fileName : pendingFileList) {
 
 					nodeFileList[i].add(fileName);
 					i = (i + 1) % freeNodeList.size();
 
 
 				}
 				pendingFileList.clear();
 				int j;
 				System.out.println(freeNodeList);
 				for (int z =0; z < freeNodeList.size(); z++) {
 					System.out.println(nodeFileList[z]);
 				}
 				for ( j = 0 ; ((j < freeNodeList.size()) && (nodeFileList[j].size() > 0)); j++) {
 					MapleAction temp = new MapleAction();
 					temp.mapleTaskId = task_id;
 					temp.machineId = j + 1;
 					temp.mapleExe = mapleExe;
 					temp.inputFileInfo = nodeFileList[j];
 					temp.outputFilePrefix = outputFilePrefix;
 					MapleJuicePayload mj_payload = new MapleJuicePayload("MapleTask");
 					mj_payload.setByteArray(temp);
 					System.out.println("Sending payload to " + freeNodeList.elementAt(j) );
 					mj_payload.sendMapleJuicePacket(freeNodeList.elementAt(j), false);
 					if (!master_task_map.containsKey(freeNodeList.elementAt(j))) {
 						master_task_map.put(freeNodeList.elementAt(j), nodeFileList[j]);
 					}
 					//freeNodeList.remove(j);
 					//j--;
 					//master_task_map.get(m.memberList.elementAt(j)).add(nodeFileList[j]);
 				}
 				for (int k = 0 ; k < j; k++) {
 					freeNodeList.remove(0);
 					//j--;
 				}
 
 			}
 
 			TaskStatus status = new TaskStatus();
 			status.taskId = task_id;
 			status.messageType = new String("get");
 			//Monitor the progress on the node every 10 seconds
 			tasksComplete = true;
 			synchronized(master_task_map) {
 				Vector<String> keyset = new Vector<String>(master_task_map.keySet());
 				for (String nodeName : keyset) {
 
 					MapleJuicePayload mj_payload = new MapleJuicePayload("TaskStatus");
 
 
 					mj_payload.setByteArray(status);
 
 					Socket sendSocket = mj_payload.sendMapleJuicePacket(nodeName, true);
 					try {
 						WriteLog.writelog(m.myName, "Sending status request to " + nodeName);
 					} catch (IOException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 					mj_payload.receiveMapleJuicePacket(sendSocket);
 					TaskStatus receivedStatus = (TaskStatus) mj_payload.parseByteArray();
 					try {
 						sendSocket.close();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					//Print the obtained results
 					boolean mapsCompletedOnNode = false;
 					System.out.println("Status on node " + nodeName +  " :");
 					if (receivedStatus.taskStatus.keySet().size() > 0) {
 						//tasksComplete = true;
 						mapsCompletedOnNode = true;
 					}
 					for (String fileName : receivedStatus.taskStatus.keySet())
 					{
 						System.out.println("Filename : " + fileName + " Status : " + receivedStatus.taskStatus.get(fileName));
 						if (receivedStatus.taskStatus.get(fileName).equals("In progress")) {
 							mapsCompletedOnNode = false;
 							tasksComplete = false;
 						}else if (receivedStatus.taskStatus.get(fileName).equals("Failed")) {
 							pendingFileList.add(fileName);
 						}
 					}
 					if (mapsCompletedOnNode) {
 						freeNodeList.add(nodeName);
 						master_task_map.remove(nodeName);
 					}
 
 
 				}
 			}
 			try {
 				Thread.sleep(10 * 1000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}while(!tasksComplete || (pendingFileList != null && pendingFileList.size() > 0));
 
 
 
 	}
 
 
 	public void processJuiceCommand(String juiceExe, int num_juices, String outputsdfsFileName, String sdfsFilePrefix) {
 
 		//TODO : This will borrow heavily from the way maple tasks are assigned. 
 
 		freeNodeList = new Vector<String>(m.memberList);
		//pendingFileList = new Vector<String>(filesToProcess);
 		for (int j = 0 ; j < freeNodeList.size(); j++) {
 			System.out.println(pendingFileList + "**** + filesToProcess \n\n\n");
 		}
 		//System.out.println(freeNodeList.elementAt(j));
 		boolean tasksComplete = false;
 		task_id++;
 		@SuppressWarnings("rawtypes")
 		ArrayList[] nodeFileList = null;
 		do {
 
 			System.out.println("\nInside");
 			if (pendingFileList.size() > 0 && freeNodeList.size() > 0) {
 
 				nodeFileList = new ArrayList[freeNodeList.size()];
 				for (int i = 0 ; i < pendingFileList.size() && i < freeNodeList.size(); i++) {				
 					nodeFileList[i] = new ArrayList<String>();
 				}
 
 				int i = 0;	
 				System.out.println("Size of pendingFileList : " + pendingFileList.size());
 				for (String fileName : pendingFileList) {
 
 					nodeFileList[i].add(fileName);
 					i = (i + 1) % freeNodeList.size();
 
 
 				}
 				pendingFileList.clear();
 				int j;
 				System.out.println(freeNodeList);
 				for (int z =0; z < freeNodeList.size(); z++) {
 					System.out.println(nodeFileList[z]);
 				}
 				for ( j = 0 ; ((j < freeNodeList.size()) && (nodeFileList[j].size() > 0)); j++) {
 					MapleAction temp = new MapleAction();
 					temp.mapleTaskId = task_id;
 					temp.machineId = j + 1;
					//temp.mapleExe = mapleExe;
 					temp.inputFileInfo = nodeFileList[j];
					//temp.outputFilePrefix = outputFilePrefix;
 					MapleJuicePayload mj_payload = new MapleJuicePayload("MapleTask");
 					mj_payload.setByteArray(temp);
 					System.out.println("Sending payload to " + freeNodeList.elementAt(j) );
 					mj_payload.sendMapleJuicePacket(freeNodeList.elementAt(j), false);
 					if (!master_task_map.containsKey(freeNodeList.elementAt(j))) {
 						master_task_map.put(freeNodeList.elementAt(j), nodeFileList[j]);
 					}
 					//freeNodeList.remove(j);
 					//j--;
 					//master_task_map.get(m.memberList.elementAt(j)).add(nodeFileList[j]);
 				}
 				for (int k = 0 ; k < j; k++) {
 					freeNodeList.remove(0);
 					//j--;
 				}
 
 			}
 
 			TaskStatus status = new TaskStatus();
 			status.taskId = task_id;
 			status.messageType = new String("get");
 			//Monitor the progress on the node every 10 seconds
 			tasksComplete = true;
 			synchronized(master_task_map) {
 				Vector<String> keyset = new Vector<String>(master_task_map.keySet());
 				for (String nodeName : keyset) {
 
 					MapleJuicePayload mj_payload = new MapleJuicePayload("TaskStatus");
 
 
 					mj_payload.setByteArray(status);
 
 					Socket sendSocket = mj_payload.sendMapleJuicePacket(nodeName, true);
 					try {
 						WriteLog.writelog(m.myName, "Sending status request to " + nodeName);
 					} catch (IOException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 					mj_payload.receiveMapleJuicePacket(sendSocket);
 					TaskStatus receivedStatus = (TaskStatus) mj_payload.parseByteArray();
 					try {
 						sendSocket.close();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					//Print the obtained results
 					boolean mapsCompletedOnNode = false;
 					System.out.println("Status on node " + nodeName +  " :");
 					if (receivedStatus.taskStatus.keySet().size() > 0) {
 						//tasksComplete = true;
 						mapsCompletedOnNode = true;
 					}
 					for (String fileName : receivedStatus.taskStatus.keySet())
 					{
 						System.out.println("Filename : " + fileName + " Status : " + receivedStatus.taskStatus.get(fileName));
 						if (receivedStatus.taskStatus.get(fileName).equals("In progress")) {
 							mapsCompletedOnNode = false;
 							tasksComplete = false;
 						}else if (receivedStatus.taskStatus.get(fileName).equals("Failed")) {
 							pendingFileList.add(fileName);
 						}
 					}
 					if (mapsCompletedOnNode) {
 						freeNodeList.add(nodeName);
 						master_task_map.remove(nodeName);
 					}
 
 
 				}
 			}
 			try {
 				Thread.sleep(10 * 1000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}while(!tasksComplete || (pendingFileList != null && pendingFileList.size() > 0));
 
 
 
 		/*
 		int i = 0;
 		task_id++;
 
 		Iterator<String> iter = m.file_node_map.keySet().iterator();
 
 		//I should only find files with names of the form : prefix_inter_key. Search for _inter_
 		Pattern pattern = Pattern.compile(sdfsFilePrefix + "_inter_" + "[.]+");
 		while(iter.hasNext()) {
 			Vector<String> filesOfNode = m.file_node_map.get(iter.next());
 
 			for(String file : filesOfNode) {
 				Matcher matcher = pattern.matcher(file);
 
 				if(matcher.find()) {
 					nodeFileList.get(i).add(file);
 					i = (i + 1) % num_juices;
 					matcher.reset();
 					continue;
 				}				
 			}			
 		}
 
 
 		for (int j = 0 ; ((j < m.memberList.size()) && ( j < num_juices)); j++) {
 			JuiceAction juiceAction = new JuiceAction();
 			juiceAction.juiceTaskId= task_id;
 			juiceAction.machineId = j + 1;
 			juiceAction.juiceExe = juiceExe;
 			juiceAction.juiceInputFileList = nodeFileList.get(j);
 			juiceAction.juiceOutputFile = outputsdfsFileName;
 			MapleJuicePayload mj_payload = new MapleJuicePayload("JuiceTask");
 			mj_payload.setByteArray(juiceAction);
 			mj_payload.sendMapleJuicePacket(m.memberList.elementAt(j), false);
 		} 
 
 		
 		////From here I've copied from the the maple.
 		TaskStatus status = new TaskStatus();
 		status.taskId = task_id;
 		status.messageType = new String("get");
 		//Monitor the progress on the node every 10 seconds
 		tasksComplete = true;
 		for (String nodeName : master_task_map.keySet() ) {
 
 			MapleJuicePayload mj_payload = new MapleJuicePayload("TaskStatus");
 
 			mj_payload.setByteArray(status);
 
 			Socket sendSocket = mj_payload.sendMapleJuicePacket(nodeName, true);
 			try {
 				WriteLog.writelog(m.myName, "Sending status request to " + nodeName);
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 			mj_payload.receiveMapleJuicePacket(sendSocket);
 			TaskStatus receivedStatus = (TaskStatus) mj_payload.parseByteArray();
 			//Print the obtained results
 			boolean juicesCompletedOnNode = true;
 			System.out.println("Status on node " + nodeName +  " :");
 			for (String fileName : receivedStatus.taskStatus.keySet())
 			{
 				System.out.println("Filename : " + fileName + " Status : " + receivedStatus.taskStatus.get(fileName));
 				if (receivedStatus.taskStatus.get(fileName).equals("In progress")) {
 					juicesCompletedOnNode = false;
 					tasksComplete = false;
 				}else if (receivedStatus.taskStatus.get(fileName).equals("Failed")) {
 					pendingFileList.add(fileName);
 				}
 			}
 			if (juicesCompletedOnNode) {
 				freeNodeList.add(nodeName);
 				master_task_map.remove(nodeName);
 			}
 			try {
 				Thread.sleep(10 * 1000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		////Until here this is copied from the maple tasks
 		 */
 	}
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		ServerSocket servsock = null;
 		try {
 			servsock = new ServerSocket(Machine.MAPLE_JUICE_PORT);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		while(true){
 
 			try{
 
 				System.out.println("Waiting...");
 
 				Socket sock = servsock.accept();
 				System.out.println("Accepted connection : " + sock);
 
 
 
 				MapleJuiceThread mj_thread = new MapleJuiceThread(sock, m);
 				mj_thread.start();
 				// sendfile
 
 
 
 			}catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 
 	}
 
 }
