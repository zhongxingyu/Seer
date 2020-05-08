 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dblike.server;
 
 import dblike.api.ServerAPI;
 import dblike.service.InternetUtil;
 import java.rmi.registry.Registry;
 
 /**
  * This class defines the attributes of the server object.
  *
  * @author wenhanwu
  */
 public class ActiveServer {
 
     private String serverID;
     private String serverIP;
     private int port;
     private int status;
     private Registry registry;
     private ServerAPI serverAPI = null;
     private int isConnect;
 
     /**
      * @return the isConnect
      */
     public int isIsConnect() {
         return isConnect;
     }
 
     /**
      * @param isConnect the isConnect to set
      */
     public void setIsConnect(int isConnect) {
         this.isConnect = isConnect;
     }
 
     /**
      * @return the registry
      */
     public Registry getRegistry() {
         return registry;
     }
 
     /**
      * @param registry the registry to set
      */
     public void setRegistry(Registry registry) {
         this.registry = registry;
     }
 
     /**
      * @return the serverAPI
      */
     public ServerAPI getServerAPI() {
         return serverAPI;
     }
 
     /**
      * @param serverAPI the serverAPI to set
      */
     public void setServerAPI(ServerAPI serverAPI) {
         this.serverAPI = serverAPI;
     }
 
     /**
      * @return the serverID
      */
     public String getServerID() {
         return serverID;
     }
 
     /**
      * @param serverID the serverID to set
      */
     public void setServerID(String serverID) {
         this.serverID = serverID;
     }
 
     /**
      * @return the serverIP
      */
     public String getServerIP() {
         return serverIP;
     }
 
     /**
      * @param serverIP the serverIP to set
      */
     public void setServerIP(String serverIP) {
         this.serverIP = serverIP;
     }
 
     /**
      * @return the port
      */
     public int getPort() {
         return port;
     }
 
     /**
      * @param port the port to set
      */
     public void setPort(int port) {
         this.port = port;
     }
 
     /**
      * @return the status
      */
     public int getStatus() {
         return status;
     }
 
     /**
      * @param status the status to set
      */
     public void setStatus(int status) {
         this.status = status;
     }
 
     /**
      * Constructor.
      *
      * @param aServerID
      * @param aClientIP
      * @param aPort
      */
     public ActiveServer(String aServerID, String aClientIP, int aPort) {
         this.serverID = aServerID;
         this.serverIP = aClientIP;
         this.port = aPort;
        this.status = InternetUtil.getOK();
         this.isConnect = 0;
     }
 }
