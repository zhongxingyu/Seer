 package distclient;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.Vector;
 
 import distconfig.ConnectionCodes;
 import distconfig.Constants;
 import distconfig.DistConfig;
 import distfilelisting.FileObject;
 import distfilelisting.LocalPathList;
 import distnodelisting.NodeSearchTable;
 
 public class ClntNewSuccessor implements Runnable {
 	private String host;
 	private Client client;
 	
 	public ClntNewSuccessor(Client client){
		this(client.getPredecessor()[Constants.IP_ADDRESS], client);
 	}
 	
 	public ClntNewSuccessor(String host, Client client){
 		this.host = host;
 		this.client = client;
 	}
 
 	@Override
 	public void run() {
 		try {
 	
 	    	DistConfig distConfig = DistConfig.get_Instance();
 	    	
 			System.out.println("Connecting");
 			Socket sock = new Socket(host, distConfig.get_servPortNumber());
 	
 	        sock.setSoTimeout(5000);
 	        System.out.println("Connected");
 	        
 	        BufferedOutputStream bos = new BufferedOutputStream (
 	                                sock.getOutputStream());
 	        System.out.println("Got OutputStream");
 	        PrintWriter outStream = new PrintWriter(bos, false);
 	        System.out.println("Got PrintWriter");
 	
 	        BufferedReader in = new BufferedReader (
 	                    new InputStreamReader (
 	                            sock.getInputStream()));
 	        System.out.println("Got InputStream");
 	        
 	
 	        
 	        System.out.println("Sending Code");
 	        outStream.println(ConnectionCodes.NEWPREDECESSOR);
 	        outStream.flush();
 	        
 	        ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
 	        System.out.println("Got Object InputStream");
 	        
 	        System.out.println("Getting Ack");
 	        System.out.println(in.readLine());
 	        
 	        int id = Integer.parseInt(NodeSearchTable.get_Instance().get_ownID());
 	        System.out.println("Sending my ID as " + id);
 	        outStream.println(Integer.toString(id));
 	        outStream.flush();
 	        
 	        @SuppressWarnings("unchecked")
 			Vector<FileObject> vfo = (Vector<FileObject>) ois.readObject();
 	        
 	        System.out.println("Sending confirm");
 	        outStream.println(ConnectionCodes.NEWPREDECESSOR);
 	        outStream.flush();        
 	        
 	        for (FileObject f : vfo) {
 	        	   
 	        	FileOutputStream fos;
 	          	byte [] buffer = new byte[distConfig.getBufferSize()];  
 	        	      
 	            fos = new FileOutputStream(distConfig.get_rootPath() + f.getName());
 	        
 	            Integer bytesRead = 0;  
 	      
 	            do {        
 	                bytesRead = (Integer)ois.readObject(); 	      
 	                buffer = (byte[])ois.readObject();  
 	      
 	                fos.write(buffer, 0, bytesRead);  
 	            } while (bytesRead == distConfig.getBufferSize());  
 	      
 	            fos.close();
 		        LocalPathList.get_Instance().add(f);
 	            
 	            System.out.println("Sending file receipt");
 		        outStream.println(ConnectionCodes.NEWPREDECESSOR);
 		        outStream.flush();
 	        }
 	        
 
 	        System.out.println("Getting Ack");
 	        System.out.println(in.readLine());
 	        
 		} catch (IOException e) {
 			System.out.println("Inside ClntNewSuccessor");
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			System.out.println("Inside ClntNewSuccessor");
 			e.printStackTrace();
 		}
 	}
 
 }
