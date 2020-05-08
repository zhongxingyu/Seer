 package network;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 
 
 public class NetworkClient {
 	private String m_ipAddress;
 	private int m_port;
 
 	private Socket m_socket = null;
     private ObjectOutputStream m_out = null;
     private ObjectInputStream m_in = null;
     
     private AtomicBoolean m_connected = new AtomicBoolean(false);
 
     
     private Thread m_connectThread = new Thread( new Runnable(){
 
 		@Override
 		public void run() {
 
 			try {
 				m_out = new ObjectOutputStream(m_socket.getOutputStream());
 				m_out.flush();
 				m_in = new ObjectInputStream(m_socket.getInputStream());
 				
 				m_connected.set(true);	
 				
 	        } catch (IOException e) {
 	            System.err.println("NetworkClient: Could not open Input- or OutputStream");
 	        }
 			
 		}
     } );
     
     public NetworkSocket getNetworkSocket(){
     	if(m_connected.get() == true)
     		return new NetworkSocket(m_socket,  m_in, m_out);
     	else
     		return null;
     }
 
     public boolean isConnected(){
     	return m_connected.get();
     }
 
     public boolean connectTo(String ip, int port){
     	m_ipAddress = ip;
 		m_port = port;
 		
 		try {
 			m_socket = new Socket(m_ipAddress, m_port);
 			m_connectThread.start();
 		} catch (UnknownHostException e) {
             System.err.println("NetworkClient: Could not find host host: " + ip);
             return false;
         } catch (IOException e) {
             System.err.println("NetworkClient:  I/O error occured when creating the socket to: " + ip);
            System.err.println(e);
            System.err.println(e.getStackTrace() );
             return false;
         }
 		
 		return true;
     }
 
 	public NetworkClient(){
 		
 	}
 }
