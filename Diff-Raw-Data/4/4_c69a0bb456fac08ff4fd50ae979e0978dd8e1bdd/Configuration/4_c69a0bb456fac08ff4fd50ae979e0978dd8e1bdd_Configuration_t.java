 import java.io.*;
 import java.util.*;
 
 public class Configuration extends Module {
 
 	private HashMap<String, Configuration.PeerInfo> peerList;
 
         private HashMap<String, String> commonInfo;
        
 
 
     /**
      * @return the peerList
      */
     public HashMap<String, PeerInfo> getPeerList() {
         return peerList;
     }
 
     /**
      * @return the commonInfo
      */
 
     public HashMap<String,String> getCommonInfo() {
        return commonInfo;
     }
         
 	
 	public class PeerInfo
 	{
 		private int peerID;
 		private String hostName;
 		private int portNumber;
 		private boolean hasFile;
 		
 		protected void setPeerID(int peerID)
 		{
 			this.peerID = peerID;
 		}
 
 		public int getPeerID()
 		{		
 			return peerID;
 		}
 
 		protected void setHostName(String hostName)
 		{
 			this.hostName = hostName;
 		}
 
 		public String getHostName()
 		{
 			return hostName;
 		}
 
 		protected void setPortNumber(int portNumber)
 		{
 			this.portNumber = portNumber;
 		}
 
 		public int getPortNumber()
 		{
 			return portNumber;
 		}
 
 		protected void setHasFile(boolean hasFile)
 		{
 			this.hasFile = hasFile;
 		}
 
 		public boolean getHasFile()
 		{
 			return hasFile;
 		}
 
 	}
 	
         @Override
 	public void initialConfiguration() {
 
 		String st;
 		boolean hasFile;
 		peerList = new HashMap<String, Configuration.PeerInfo>();
		PeerInfo node = null;
 			try {
 				BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
 		
 
 					while((st = in.readLine()) != null)
 					{
						node = new PeerInfo();
 						String tokens[] = st.split(" ");
 							
 							
 							node.setPeerID(Integer.parseInt(tokens[0]));
 							node.setHostName(tokens[1]);
 							node.setPortNumber(Integer.parseInt(tokens[2]));
 				
 							hasFile = (tokens[3] == "1")  ? true : false;
 							node.setHasFile(hasFile);
 					
 							getPeerList().put(tokens[0], node);
 						}
 						
 						
 				in.close();
 			}	catch(IOException e)
 				{
                                     System.out.println(e.getMessage());
                                     //System.out.println("There was a problem opening the peer configuration file. Make sure the file exists");
 				}
                         
                         //read in config info
                         
                         commonInfo = new HashMap<String, String>();
 			try {
 				BufferedReader in = new BufferedReader(new FileReader(Constants.COMMON_CFG_FILE));
 		
 
 					while((st = in.readLine()) != null)
 					{
                                                System.out.println("ST: " + st);
 						String tokens[] = st.split(" ");
                                                 
                                                 commonInfo.put(tokens[0], tokens[1]);
 							
 						}
 						
 						
 				in.close();
 			}	catch(IOException e)
 				{
                                     System.out.println(e.getMessage());
 					//System.out.println("There was a problem opening the common configuration file. Make sure the file exists");
 				}
 	}
 
 }
 
