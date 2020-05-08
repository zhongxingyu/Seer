 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Hashtable;
 
 public class messaging {
 
 	private byte fileBuffer[];
 	private byte readBuffer[];
 
 	private String m_ipAddress;
 	private int m_port;
 	private Boolean m_clientAuth = false;
 
 	public messaging(String ipAddress, int port_num) {
 		m_ipAddress = ipAddress;
 		m_port = port_num;
 		m_clientAuth = Communicate.checkAuthentication();
 
 	}
 
 	public Boolean clientAuth() {
 		return m_clientAuth;
 	}
 
 	public Boolean clientLogin(String userhash, String caIPaddress) {
 		System.out.println(userhash + "Attempting to login");
 		Boolean response = Communicate.Login(userhash, caIPaddress);
 		return response;
 	}
 
 	public int get(String localPath, String remotePath) {
 		Hashtable sendTable = new Hashtable();
 		sendTable.put("cmd", "get");
 		sendTable.put("get", remotePath);
 		Hashtable recvTable = new Hashtable();
 		recvTable = Communicate.sendMsg(sendTable, m_ipAddress, m_port);
 		int response = (Integer) recvTable.get("response");
 
 		if (response >= 0) {
 			try {
 				File file = new File(localPath);
 				if(!file.exists())file.createNewFile();
 				FileOutputStream fosFile = new FileOutputStream(localPath);
 				BufferedOutputStream bosFile = new BufferedOutputStream(fosFile);
 				System.out.println("Writing to: " + localPath);
 				fileBuffer = Communicate.Decrypt((byte[]) recvTable.get("file"));
 				bosFile.write(fileBuffer, 0, fileBuffer.length);
 
 				FileOutputStream fosTime = new FileOutputStream(localPath
 						+ ".timestamp");
 				BufferedOutputStream bosTime = new BufferedOutputStream(fosTime);
 
 				readBuffer = Communicate.Decrypt((byte[]) recvTable.get("timestamp"));
 				bosTime.write(readBuffer, 0, readBuffer.length);
 
 				bosFile.flush();
 				bosFile.close();
 				bosTime.flush();
 				bosTime.close();
 				fosFile.flush();
 				fosFile.close();
 				fosTime.flush();
 				fosTime.close();
 
 				return 0;
 			} catch (IOException e) {
 				return -1;
 			} catch (NullPointerException npe) {
 				return -1;
 			}
 		} else {
 			return (Integer) recvTable.get("response");
 		}
 	}
 
 	public int put(String localPath, String remotePath, String timestamp) {
 		Hashtable sendTable = new Hashtable();
 		sendTable.put("cmd", "put");
 		sendTable.put("put", remotePath);
 		// DEBUG CODE ////////////////////
 		System.out.println(localPath + ": Attempting to put this file on : "+m_ipAddress+"\n\t at location: "+ remotePath);
 		//////////////////////////////////
 		try {
 			File localFile = new File(localPath);
 			int fileSize = (int) localFile.length();
 			FileInputStream fisFile = new FileInputStream(localFile);
 			BufferedInputStream bisFile = new BufferedInputStream(fisFile);
 
 			byte[] readBuffer = new byte[fileSize];
 			bisFile.read(readBuffer, 0, fileSize);
 			byte[] encryptedBuffer = Communicate.Encrypt(readBuffer);
 			sendTable.put("file", encryptedBuffer);
 
 			byte[] timeBuffer = Communicate.Encrypt(timestamp.getBytes());
 			sendTable.put("timestamp", timeBuffer);
 
 			Hashtable recvTable = new Hashtable();
 			recvTable = Communicate.sendMsg(sendTable, m_ipAddress, m_port);
 
 			fisFile.close();
 			bisFile.close();
 
 			return (Integer) recvTable.get("response");
 		} 
 		catch (FileNotFoundException fnf) {
 			System.out.println("This didn't work! File not Found");
 			return -1;
 		} 
 		catch (IOException e) {
 			System.out.println("This didn't work! IO Failure");
 			return -1;
 		}
		catch (NullPointerException npe) {
			System.out.println("Server returned null");
			return -1;
		}
 	}
 
 	public int ls(String localPath, String remotePath) {
 		Hashtable sendTable = new Hashtable();
 		sendTable.put("cmd", "ls");
 		sendTable.put("ls", remotePath);
 
 		Hashtable recvTable = new Hashtable();
 		recvTable = Communicate.sendMsg(sendTable, m_ipAddress, m_port);
 
 		if ((Integer) recvTable.get("response") >= 0) {
 			try {
 				FileOutputStream fos = new FileOutputStream(localPath);
 				BufferedOutputStream bos = new BufferedOutputStream(fos);
 
 				fileBuffer = (byte[]) recvTable.get("file");
 				bos.write(fileBuffer, 0, (Integer) recvTable.get("length"));
 				bos.flush();
 				bos.close();
 				fos.flush();
 				fos.close();
 
 				return 0;
 			} catch (IOException e) {
 				return -1;
 			}
 		} else {
 			return (Integer) recvTable.get("response");
 		}
 	}
 
 	public int mkdir(String remotePath) {
 		Hashtable sendTable = new Hashtable();
 		sendTable.put("cmd", "mkdir");
 		sendTable.put("mkdir", remotePath);
 
 		Hashtable recvTable = new Hashtable();
 		recvTable = Communicate.sendMsg(sendTable, m_ipAddress, m_port);
 
 		return (Integer) recvTable.get("response");
 	}
 
 	public int rmdir(String remotePath) {
 		Hashtable sendTable = new Hashtable();
 		sendTable.put("cmd", "rmdir");
 		sendTable.put("rmdir", remotePath);
 
 		Hashtable recvTable = new Hashtable();
 		recvTable = Communicate.sendMsg(sendTable, m_ipAddress, m_port);
 
 		return (Integer) recvTable.get("response");
 	}
 
 	public int rm(String remotePath) {
 		Hashtable sendTable = new Hashtable();
 		sendTable.put("cmd", "rm");
 		sendTable.put("rm", remotePath);
 
 		Hashtable recvTable = new Hashtable();
 		recvTable = Communicate.sendMsg(sendTable, m_ipAddress, m_port);
 
 		return (Integer) recvTable.get("response");
 	}
 }
