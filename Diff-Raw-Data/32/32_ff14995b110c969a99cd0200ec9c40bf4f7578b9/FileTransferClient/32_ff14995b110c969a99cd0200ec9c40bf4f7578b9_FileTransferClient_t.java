 import java.io.*;
 import java.net.*;
 
 public class FileTransferClient implements Runnable {
 	/*
 	Socket fclientSock = null;  
     DataOutputStream os = null;
     DataInputStream is = null;
 	InetAddress ftransferServer = null;
     
 	public void start(String fserver)
 	{
 		try {
 			InetAddress ftransferServer = InetAddress.getByName(fserver);
 			fclientSock = new Socket(ftransferServer, Machine.FILE_TRANSFER_PORT);
 			os = new DataOutputStream(fclientSock.getOutputStream());
             is = new DataInputStream(fclientSock.getInputStream());
 		} catch (UnknownHostException e) {
 			System.err.println("Don't know about host: hostname");
 		} catch (IOException e) {
 			System.err.println("Couldn't get I/O for the connection to: hostname");
 		}
 		Thread client_thread = new Thread(this);
 		client_thread.start();
 	}
 	
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 
 	}
 	*/
 	private String copyFN;
 	private String serverIP;
 	private String sourceFN;
 	
 	public FileTransferClient(String copy, String source, String ip){
 		copyFN = copy;
 		serverIP = ip;
 		sourceFN = source;
 	}
 	
 	public void run(){
 		
 		synchronized(this) {
 			try {
				WriteLog.writelog(Machine.stName, " thread: file transfer started");
 			} catch (IOException e3) {
 				// TODO Auto-generated catch block
 				e3.printStackTrace();
 			}
 			int fixedsize=4096; // filesize temporary hardcoded
 
 			long start = System.currentTimeMillis();
 			int bytesRead = 0;
 			int current = 0;
 
 			Socket sock;
 			String myName=null;
 			try {
 				myName = InetAddress.getLocalHost().getHostName();
 			} catch (UnknownHostException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 			};
 
 			try {
 				sock = new Socket(serverIP, Machine.FILE_TRANSFER_PORT);
 				System.out.println("Connecting...");
 				// receive file
 
 				try {
 					WriteLog.writelog(myName, "c:"+copyFN+" s:"+sourceFN+" ip:"+serverIP);
 				} catch (UnknownHostException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 
 
 
 				ObjectOutputStream oos;
 				try {
 					oos = new ObjectOutputStream(sock.getOutputStream());
 					oos.writeObject(sourceFN);
 					//oos.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 
 
 				byte [] mybytearray  = new byte [fixedsize];
 				InputStream is;
 				is = sock.getInputStream();
 				FileOutputStream fos = new FileOutputStream(copyFN);
 				BufferedOutputStream bos = new BufferedOutputStream(fos);
 				//bytesRead = is.read(mybytearray,0,mybytearray.length);
 				//current = bytesRead;
 
 				try {
 
 					while (-1 != (current = is.read(mybytearray, 0, mybytearray.length))) {
 						bos.write(mybytearray, 0 , current);
 						bytesRead = bytesRead + current;
 					}
 
 				} catch (IOException e) {
 					WriteLog.writelog(myName, "Error with streaming op: " + e.getMessage());
 					throw (e);
 				} finally {
 					try{
 						is.close();
 						bos.flush();
 						bos.close();
 					} catch (Exception e){}//Ignore
 				}
 
 				long end = System.currentTimeMillis();
 				System.out.println(end-start);
 				sock.close();
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			try {
				WriteLog.writelog(Machine.stName, " thread: file transfer completed");
 			} catch (IOException e3) {
 				// TODO Auto-generated catch block
 				e3.printStackTrace();
 			}
 			//this.notifyAll();
 			//this.notify();
 		}	
 	}
 }
