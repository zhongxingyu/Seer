 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Hashtable;
 
 public class Server {
 
 	private static String group;
 
 	public static Hashtable processRequest(Hashtable recvTable) {
 		String command = (String) recvTable.get("cmd");
 		group = readGroup((String) recvTable.get("group"));
 		Hashtable sendTable = new Hashtable();
 
 		int cmd = 0;
 		if (command.equals("get")) {
 			cmd = 1;
 		} else if (command.equals("put")) {
 			cmd = 2;
 		} else if (command.equals("ls")) {
 			cmd = 3;
 		} else if (command.equals("mkdir")) {
 			cmd = 4;
 		} else if (command.equals("rmdir")) {
 			cmd = 5;
 		} else if (command.equals("rm")) {
 			cmd = 6;
 		}
 
 		switch (cmd) {
 		case 1:
 			sendTable = get(recvTable);
 			break;
 		case 2:
 			sendTable = put(recvTable);
 			break;
 		case 3:
 			sendTable = ls(recvTable);
 			break;
 		case 4:
 			sendTable = mkdir(recvTable);
 			break;
 		case 5:
 			sendTable = rmdir(recvTable);
 			break;
 		case 6:
 			sendTable = rm(recvTable);
 			break;
 		default:
 			sendTable.put("response", -2);
 		}
 		return sendTable;
 	}
 
 	private static Hashtable get(Hashtable table) {
 		String localPath = (String) table.get("get");
 		Hashtable sendTable = new Hashtable();
 
 		localPath = group + localPath;
 
 		System.out.println("Server: Attempting to get. Remote path is ");
 
 		try {
 			File localFile = new File("data/" + localPath);
 			System.out.println(localPath);
 			int fileSize = (int) localFile.length();
 			FileInputStream fisFile = new FileInputStream(localFile);
 			BufferedInputStream bisFile = new BufferedInputStream(fisFile);
 
 			byte[] readBuffer = new byte[fileSize];
 			bisFile.read(readBuffer, 0, fileSize);
 			sendTable.put("file", readBuffer);
 
 			File timestamp = new File("time/" + localPath + ".timestamp");
 			int timeSize = (int) timestamp.length();
 			FileInputStream fisTime = new FileInputStream(timestamp);
 			BufferedInputStream bisTime = new BufferedInputStream(fisTime);
 
 			byte[] timeBuffer = new byte[timeSize];
 			bisTime.read(timeBuffer, 0, timeSize);
 			sendTable.put("timestamp", timeBuffer);
 
 			sendTable.put("response", fileSize);
 
 			fisFile.close();
 			bisFile.close();
 			fisTime.close();
 			bisTime.close();
 		} catch (IOException e) {
 			System.out.println("Server encountered IOException");
 			sendTable.put("response", -1);
 		}
 
 		return sendTable;
 	}
 
 	private static Hashtable put(Hashtable table) {
 		String localPath = (String) table.get("put");
 		Hashtable sendTable = new Hashtable();
 
 		localPath = group + localPath;
 
 		System.out.println("Server: Attempting to put. Remote path is "
 				+ localPath);
 		try {
 			FileOutputStream fosFile = new FileOutputStream("data/" + localPath);
 			System.out.println(localPath);
 			BufferedOutputStream bosFile = new BufferedOutputStream(fosFile);
 
 			byte[] fileBuffer = (byte[]) table.get("file");
 			bosFile.write(fileBuffer, 0, fileBuffer.length);
 
 			String timestamp = new String((byte[]) table.get("timestamp"));
 			FileWriter time = new FileWriter("time/" + localPath + ".timestamp");
 			BufferedWriter out = new BufferedWriter(time);
 			out.write(timestamp);
 
 			bosFile.flush();
 			bosFile.close();
 			fosFile.flush();
 			fosFile.close();
 			out.flush();
 			out.close();
 
 			sendTable.put("response", 1);
 		} catch (IOException e) {
 			System.out.println("Server encountered IOException");
 			sendTable.put("response", -1);
 		}
 
 		return sendTable;
 	}
 
 	private static Hashtable ls(Hashtable table) {
 		String localPath = (String) table.get("ls");
 		Hashtable sendTable = new Hashtable();
 
 		localPath = group + localPath;
 
 		System.out.println("Attempting ls: Remote path is " + localPath);
 
 		File localDir = new File("data/" + localPath);
 		if (!localDir.isDirectory()) {
 			sendTable.put("response", -1);
 		} 
 		else {
 			Runtime runtime = Runtime.getRuntime();
 			Process p;
 			try {
 				p = runtime.exec("ls -F " + "data/" + localPath);
 				InputStream in = p.getInputStream();
 				BufferedInputStream bis = new BufferedInputStream(in);
 
 				int i = 0;
 				int nextByte;
 				byte[] lsBuffer = new byte[10000];
 				do {
 					nextByte = bis.read();
 					if ((nextByte != -1) && (i < 10000)) {
 						lsBuffer[i] = (byte) nextByte;
 						i++;
 					}
 				} while (nextByte != -1);
 
 				sendTable.put("length", i);
 				sendTable.put("file", lsBuffer);
 				sendTable.put("response", 1);
 			} catch (IOException e) {
 				System.out.println("Server encountered IOException");
 				sendTable.put("response", -1);
 			} // Do I need ./ here?
 		}
 
 		return sendTable;
 	}
 
 	private static Hashtable mkdir(Hashtable table) {
 		String localPath = (String) table.get("mkdir");
 		Hashtable sendTable = new Hashtable();
 
 		localPath = group + localPath;
 
 		File localDir = new File("data/" + localPath);
 		if (localDir.isDirectory()) {
 			// Directory already exists. Reture "success"
 			sendTable.put("response", 1);
 		}
 		else {
 			if (localDir.mkdir()) {
 				sendTable.put("response", 1);
 			}
 			else {
 				System.out.println("Server encountered IOException");
 				sendTable.put("response", -1);
 			}
 		}
 
 		return sendTable;
 	}
 
 	private static Hashtable rmdir(Hashtable table) {
 		String localPath = (String) table.get("rmdir");
 		Hashtable sendTable = new Hashtable();
 
 		localPath = group + localPath;
 		
 		File localDir = new File("data/" + localPath);
 		if (!localDir.isDirectory()) {
 			// Directory doesn't exist. Return failure
 			sendTable.put("response", -1);
 		}
 		else {
 			File[] dirFiles = localDir.listFiles();
 			if (dirFiles.length == 0) {
 				if (localDir.delete()) {
 					sendTable.put("response", 1);
 				}
 				else {
 					System.out.println("Server encountered IOException");
 					sendTable.put("response", -1);
 				}
 			}
 			else {
 				// Non empty dir. Return failure
 				sendTable.put("response", -2);
 			}
 		}
 
 		return sendTable;
 	}
 
 	private static Hashtable rm(Hashtable table) {
 		String localPath = (String) table.get("rm");
 		Hashtable sendTable = new Hashtable();
 
 		localPath = group + localPath;
 
 		File localFile = new File("data/" + localPath);
 		File timestamp = new File("time/" + localPath + ".timestamp");
 		if (localFile.isFile()) {
 			if (localFile.delete()) {
 				if (timestamp.isFile()) {
 					timestamp.delete();
 				}
 				sendTable.put("response", 1);
 			}
 			else {
 				System.out.println("Server encountered IOException");
 				sendTable.put("response", -1);
 			}
 		}
 		else {
 			sendTable.put("response", -1);
 		}
 
 		return sendTable;
 	}
 
 	private static String readGroup(String CAgroup) {
 		int commaIndex = CAgroup.indexOf(",");
 		String ret;
 		if (commaIndex > 10) {
			ret = "group" + CAgroup.substring(9, commaIndex - 1);
 		} else {
			ret = "group" + CAgroup.charAt(9);
 		}
 
		File groupDir = new File("data/" + ret);
 		if (!groupDir.isDirectory()) {
 			groupDir.mkdir();
 		}
 
 		return ret;
 	}
 }
