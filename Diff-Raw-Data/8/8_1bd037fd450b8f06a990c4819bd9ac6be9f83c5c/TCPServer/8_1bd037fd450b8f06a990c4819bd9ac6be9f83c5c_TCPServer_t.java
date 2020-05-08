 import java.net.*;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.io.*;
  
 public class TCPServer extends Thread{
 	
 	private static ArrayList<RMONEvent> rMonEvent;
     private Hashtable<String,String> ACL = new Hashtable<String,String>();
     private ServerSocket s;
     private final String NEHOST = "localhost";
 
 	private boolean Status = false;
     private static int neport=9000;
     private static final int cport = 9005;
     public TCPServer(ServerSocket s,ArrayList<RMONEvent> rMonEvent){
     	this.s = s;
     	TCPServer.rMonEvent = rMonEvent;
     	ACL.put("password", "RW");
     	ACL.put("public", "RO");
    	//ACL.put("secret", "ADM");
     }
     public void run()
     {
     	try {
     		while(!s.isClosed()){
     			Socket socket = s.accept();
     			InputStream is = socket.getInputStream();
     			ObjectInputStream ois = new ObjectInputStream(is);  
     			SNMP obj = (SNMP)ois.readObject();
     			if(CheckCommunity(obj.getCommunity()) < 0){
     				//Invalid password
     				ObjectOutputStream set = new ObjectOutputStream(socket.getOutputStream());
     				set.flush();
     				set.writeObject("Permission Denied");
     				set.flush();
     				set.close();
     			}
     			else if(obj.pdutype.equalsIgnoreCase("TRAP")&& CheckCommunity(obj.getCommunity()) > 0)
 	    		{
 	    			CheckStatus(socket);
 	    			Hashtable<String,String> vBinding = obj.getvBinding();
 	    			rMonEvent.add(new RMONEvent(Integer.parseInt(vBinding.get("1.3.6.1.2.1.16.9.1.1.1")), vBinding.get("1.3.6.1.2.1.16.9.1.1.2"),
 	    			Integer.parseInt(vBinding.get("1.3.6.1.2.1.16.9.1.1.3")), vBinding.get("1.3.6.1.2.1.16.9.1.1.4"), Integer.parseInt(vBinding.get("1.3.6.1.2.1.16.9.1.1.5")),
 	    			vBinding.get("1.3.6.1.2.1.16.9.1.1.6"), Integer.parseInt(vBinding.get("1.3.6.1.2.1.16.9.1.1.7"))));
 	    			//System.out.println(rMonEvent.toString());
 	    		}
 	    		else if(obj.pdutype.equalsIgnoreCase("GET")&&CheckCommunity(obj.getCommunity())>=0)
 	    		{
 	    			if(!CheckStatus(socket)){
 		    			if(obj.flag)
 		    			{
 		    			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
 		    			oos.flush();
 		    			oos.writeObject(rMonEvent);
 		    			oos.flush();
 		    			oos.close();
 		    			}
 		    			else
 		    			{
 		    				Socket ss= new Socket(NEHOST, 9001);
 		    				ObjectOutputStream get = new ObjectOutputStream(ss.getOutputStream());
 		    				get.flush();
 		    				obj.setCommunity("secret");
 		    				get.writeObject(obj);
 		    				ObjectInputStream ois1 = new ObjectInputStream(ss.getInputStream());   
 		    			 	SNMP response = (SNMP)ois1.readObject();
 		    			 	ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
 		    			 	oos.flush();
 		    			 	oos.writeObject(response);
 		    			 	oos.flush();
 		    			 	oos.close();
 		    			 	get.flush();
 		    			 	get.close();
 		    			 	ss.close();
 		    			}
 	    			}
 	    		}
 	    		else if(obj.pdutype.equalsIgnoreCase("SET")&& CheckCommunity(obj.getCommunity())>0)
 	    		{
 	    			if(obj.status.equalsIgnoreCase("OFF")&&(obj.flag))
 	    			{
 	    				Status = true; //OFFFFF
 	    				CheckStatus(socket);
 	    			}
 	    			else if(obj.status.equalsIgnoreCase("ON")&&(obj.flag)){
 	    				Status = false; //ONNNNN
 	    				//Socket ss = new Socket("localhost", 9001);
 	    				ObjectOutputStream set = new ObjectOutputStream(socket.getOutputStream());
 	    				set.flush();
 	    				set.writeObject("SNMP Agent enabled");
 	    				set.flush();
 	    				set.close();
 	    			}
 	    			else{
 	    				Socket ss = new Socket(NEHOST, 9001);
 	    				ObjectOutputStream set = new ObjectOutputStream(ss.getOutputStream());
 		    			set.flush();
 		    			obj.setCommunity("secret");
 		    			set.writeObject(obj);
 		    			set.flush();
 		    			ObjectInputStream in = new ObjectInputStream(ss.getInputStream());
 		    			SNMP response = (SNMP) in.readObject();
 		    			set= new ObjectOutputStream(socket.getOutputStream());
 		    			set.flush();
 		    			set.writeObject(response);
 		    			set.flush();
 		    			set.close();
 	    			}
 	    				
 	    				
 	    			
 	    			
 	    		}
 	    		//count++;
 	    		//System.out.println("PDU TYPE: " +obj.pdutype);
 	    		//System.out.println(s.isClosed());
 	    		//System.out.println(count);
 	    		is.close();
 	    		ois.close();
 
     		}
     		//is.close();  
     		//s.close();    
     		}catch(Exception e){
     			System.out.println(e.getMessage());
     		}
     	
     }
     
 	private int CheckCommunity(String community) {
		if (ACL.containsKey(community)||community.equals("secret"))
 			if (ACL.get(community).equals("RO"))
 				return 0;
 			else if(ACL.get(community).equals("RW"))
 				return 1;
			else if(ACL.get(community).equals("ADM")||community.equals("secret"))
 				return 2;
 		return -1;
 	}
 	private boolean CheckStatus(Socket socket) throws ClassNotFoundException, IOException {
 		if(Status)
 		{
 			ObjectOutputStream set = new ObjectOutputStream(socket.getOutputStream());
 			set.flush();
 			set.writeObject("SNMP Agent Disabled");
 			set.flush();
 			set.close();
 			return true;
 		}
 		else
 			return false;
 	}
 	public static void main(String[] args) throws IOException
     {
     	ArrayList<RMONEvent> rmon = new ArrayList<RMONEvent>();
     	ServerSocket ness = new ServerSocket(neport);
     	ServerSocket cis = new ServerSocket(cport);
     	TCPServer server = new TCPServer(ness, rmon);
 		server.start();
 		TCPServer cserver = new TCPServer(cis, rmon);
 		cserver.start();
     	while(!(ness.isClosed() && cis.isClosed()) );
     	ness.close();
     	cis.isClosed();
     	
     }
 }
 
