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
 
 	private String m_userhash;
 	private String m_ipAddress;
 	private int m_port;
 	private boolean m_clientAuth = false;
     private Communicate comm;
 	public messaging(String ipAddress, int port_num) {
 		m_ipAddress = ipAddress;
 		m_port = port_num;
 		comm = new Communicate();
 		m_clientAuth = comm.checkAuthentication();
 
 	}
 
 	public boolean clientAuth() {
 		return m_clientAuth;
 	}
 
 	public boolean clientLogin(String userhash, String caIPaddress) {
 		m_userhash = userhash;
 		System.out.println(userhash + " Attempting to login");
 		Boolean response = comm.Login(m_userhash, caIPaddress);
 		return response;
 	}
 	
 	public String getUserhash() {
 		return m_userhash;
 	}
 	
 	public boolean isLogin() {
 		m_clientAuth = comm.checkAuthentication();
 		return m_clientAuth;
 	}
 	
 	public boolean clientLogout() {
 		if (comm.Logout()) {
 			m_userhash = "";
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean addUser(String userhash) {
 		return comm.addUser(userhash);
 	}
 
 	public int get(String localPath, String remotePath) {
 		Hashtable sendTable = new Hashtable();
 		sendTable.put("cmd", "get");
 		sendTable.put("get", remotePath);
 		Hashtable recvTable = new Hashtable();
 		recvTable = comm.sendMsg(sendTable, m_ipAddress, m_port);
 
 		try {
 			int response = (Integer) recvTable.get("response");
 
 			if (response >= 0) {
 
 				File file = new File(localPath);
 				if (!file.exists())
 					file.createNewFile();
 				FileOutputStream fosFile = new FileOutputStream(localPath);
 				BufferedOutputStream bosFile = new BufferedOutputStream(fosFile);
 				System.out.println("Writing to: " + localPath);
 				fileBuffer = comm
 						.Decrypt((byte[]) recvTable.get("file"));
 				bosFile.write(fileBuffer, 0, fileBuffer.length);
 
 				FileOutputStream fosTime = new FileOutputStream(localPath
 						+ ".timestamp");
 				BufferedOutputStream bosTime = new BufferedOutputStream(fosTime);
 
				readBuffer = (byte[]) recvTable.get("timestamp");
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
 
 			} else {
 				return (Integer) recvTable.get("response");
 			}
 		} catch (IOException e) {
 			System.out.println("IO exception");
 			return -1;
 		} catch (NullPointerException npe) {
 			System.out
 					.println("Null returned by server. Invalid file or server crash");
 			return -2;
 		}
 	}
 
 	public int put(String localPath, String remotePath, String timestamp) {
 		Hashtable sendTable = new Hashtable();
 		sendTable.put("cmd", "put");
 		sendTable.put("put", remotePath);
 		// DEBUG CODE ////////////////////
 		System.out.println(localPath + ": Attempting to put this file on : "
 				+ m_ipAddress + "\n\t at location: " + remotePath);
 		// ////////////////////////////////
 		try {
 			File localFile = new File(localPath);
 			int fileSize = (int) localFile.length();
 			FileInputStream fisFile = new FileInputStream(localFile);
 			BufferedInputStream bisFile = new BufferedInputStream(fisFile);
 
 			byte[] readBuffer = new byte[fileSize];
 			bisFile.read(readBuffer, 0, fileSize);
 			byte[] encryptedBuffer = comm.Encrypt(readBuffer);
 			sendTable.put("file", encryptedBuffer);
 
			byte[] timeBuffer = timestamp.getBytes();
 			sendTable.put("timestamp", timeBuffer);
 
 			Hashtable recvTable = new Hashtable();
 			recvTable = comm.sendMsg(sendTable, m_ipAddress, m_port);
 
 			fisFile.close();
 			bisFile.close();
 
 			return (Integer) recvTable.get("response");
 		} catch (FileNotFoundException fnf) {
 			System.out.println("This didn't work! File not Found");
 			return -1;
 		} catch (IOException e) {
 			System.out.println("This didn't work! IO Failure");
 			return -1;
 		} catch (NullPointerException npe) {
 			System.out.println("Server returned null. Suspect server crash");
 			return -2;
 		}
 	}
 
 	public int ls(String localPath, String remotePath) {
 		Hashtable sendTable = new Hashtable();
 		sendTable.put("cmd", "ls");
 		sendTable.put("ls", remotePath);
 
 		Hashtable recvTable = new Hashtable();
 		recvTable = comm.sendMsg(sendTable, m_ipAddress, m_port);
 
 		try {
 			int resp = (Integer) recvTable.get("response");
 
 			if (resp >= 0) {
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
 		} catch (NullPointerException npe) {
 			System.out.println("Server returned null. Suspect server crash");
 			return -2;
 		}
 
 	}
 
 	public int mkdir(String remotePath) {
 		Hashtable sendTable = new Hashtable();
 		sendTable.put("cmd", "mkdir");
 		sendTable.put("mkdir", remotePath);
 
 		Hashtable recvTable = new Hashtable();
 		recvTable = comm.sendMsg(sendTable, m_ipAddress, m_port);
 
 		try {
 			return (Integer) recvTable.get("response");
 		} catch (NullPointerException npe) {
 			System.out.println("Server returned null. Suspect server crash");
 			return -2;
 		}
 	}
 
 	public int rmdir(String remotePath) {
 		Hashtable sendTable = new Hashtable();
 		sendTable.put("cmd", "rmdir");
 		sendTable.put("rmdir", remotePath);
 
 		Hashtable recvTable = new Hashtable();
 		recvTable = comm.sendMsg(sendTable, m_ipAddress, m_port);
 
 		try {
 			return (Integer) recvTable.get("response");
 		} catch (NullPointerException npe) {
 			System.out.println("Server returned null. Suspect server crash");
 			return -2;
 		}
 	}
 
 	public int rm(String remotePath) {
 		Hashtable sendTable = new Hashtable();
 		sendTable.put("cmd", "rm");
 		sendTable.put("rm", remotePath);
 
 		Hashtable recvTable = new Hashtable();
 		recvTable = comm.sendMsg(sendTable, m_ipAddress, m_port);
 
 		try {
 			return (Integer) recvTable.get("response");
 		} catch (NullPointerException npe) {
 			System.out.println("Server returned null. Suspect server crash");
 			return -2;
 		}
 	}
 }
