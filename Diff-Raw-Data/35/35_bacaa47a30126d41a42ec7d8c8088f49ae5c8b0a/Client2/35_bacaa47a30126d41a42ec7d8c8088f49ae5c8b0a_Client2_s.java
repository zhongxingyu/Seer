 package client;
 
 import java.net.BindException;
 import java.net.MalformedURLException;
 import java.rmi.AlreadyBoundException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import client.colorWrapper.ColorWrapperPseudo;
 
 import common.IChatRoom;
 import common.IChatRoomManager;
 
 public class Client2 {
 
     private String serverName;
 
 //    private String chatRoomName;
 
     private int serverPort;
 
     private int clientPort;
 
     private String serverIp;
 
     private IChatRoomManager server;
     
     private static Logger logger = Logger.getLogger(LaunchClient.class);
 
 //    private IChatRoom chatRoom;
 
     private Map<String,Couple<MessageListener, ChatRoomWrapper>> clientConnectedChatRoomStructure;
 
     public Client2(int clientPort) throws BindException, RemoteException,
             MalformedURLException, AlreadyBoundException {
         // Color wrapper can be changed.
         clientConnectedChatRoomStructure = new HashMap<String,Couple<MessageListener, ChatRoomWrapper>>();
         this.clientPort = clientPort;
 //        if (LocateRegistry.getRegistry(this.clientPort) == null)
             LocateRegistry.createRegistry(this.clientPort);
     }
 
 //    public MessageListener addMessageListener(String chatRoomName)
 //            throws RemoteException, MalformedURLException,
 //            AlreadyBoundException {
 //
 //        MessageListener ml = new MessageListener(new ColorWrapperPseudo(), chatRoomName, this);
 ////        clientConnectedChatRoomStructure.put(chatRoomName,ml);
 //
 ////        clientConnectedChatRoomStructure.put(chatRoomName, new Couple<MessageListener, ChatRoomWrapper>(ml, null));
 //        
 //        Couple<MessageListener, ChatRoomWrapper> couple;
 //        if(clientConnectedChatRoomStructure.containsKey(chatRoomName)) {
 //            couple = clientConnectedChatRoomStructure.get(chatRoomName);
 //            couple.setFirst(ml);
 //        }
 //        else {
 //            couple = new Couple<MessageListener, ChatRoomWrapper>(ml, null);
 //        }
 //        clientConnectedChatRoomStructure.put(chatRoomName, couple);  
 //        
 //        System.out.println("rmi://localhost:" + clientPort + "/" + ClientConfig.pseudo
 //                + "/" + chatRoomName);
 //        Naming.bind("rmi://localhost:" + clientPort + "/" + ClientConfig.pseudo + "/" + chatRoomName, ml);
 //
 //        return ml;
 //    }
     
     /**
      * Connect the client to a chatroom.
      * 
      * @param chatRoomName
      * @return
      * @throws NotBoundException
      * @throws MalformedURLException
      * @throws RemoteException
      * @throws AlreadyBoundException 
      */
     public ChatRoomWrapper connectToChatRoom(String chatRoomName)
             throws MalformedURLException, RemoteException, NotBoundException, AlreadyBoundException {
         IChatRoom chatRoom = (IChatRoom) Naming.lookup("rmi://" + this.serverIp + ":"
                 + this.serverPort + "/" + this.serverName + "/"
                 + chatRoomName);
         MessageListener ml = new MessageListener(new ColorWrapperPseudo(), chatRoomName, this);
         ChatRoomWrapper chatRoomWrapper = new ChatRoomWrapper(chatRoom);
         ChatRoomGui chatGui = new ChatRoomGui(chatRoomWrapper);
         
         ml.setOutputGui(chatGui);
         
         Couple<MessageListener, ChatRoomWrapper> couple = new Couple<MessageListener, ChatRoomWrapper>(ml, chatRoomWrapper);
 
         clientConnectedChatRoomStructure.put(chatRoomName, couple);  
         
        
        Naming.bind("rmi://localhost:" + clientPort + "/" + ClientConfig.pseudo + "/" + chatRoomName, ml);
         
         System.out.println(chatRoom.register(clientPort, ClientConfig.pseudo));
         
         return chatRoomWrapper;
     }
     
     
 
     /**
      * Connect the client to a server.
      * 
      * @param serverName
      * @return
      * @throws NotBoundException
      * @throws MalformedURLException
      * @throws RemoteException
      */
     public void connectToServer(String serverIp, String serverName, int port)
             throws MalformedURLException, RemoteException, NotBoundException {
         this.serverIp = serverIp;
         this.serverName = serverName;
         this.serverPort = port;
 
         server = (IChatRoomManager) Naming.lookup("rmi://" + this.serverIp
                 + ":" + this.serverPort + "/" + this.serverName);
     }
 
     /**
      * 
      * @return currently connected server
      */
     public IChatRoomManager getServer() {
         return server;
     }
 
 //    /**
 //     * 
 //     * @return currently connected chatRoom
 //     */
 //    public IChatRoom getChatRoom() {
 //        return chatRoom;
 //    }
 
     
     
 
 
     public synchronized void deleteChatRoom(String chatRoomName) {
         try {
             Naming.unbind("rmi://localhost:" + clientPort + "/" + ClientConfig.pseudo
                     + "/" + chatRoomName);
         } catch (Exception e) {
             System.out.println("ERR: Cannot unbind chatRoom");
         }
         clientConnectedChatRoomStructure.remove(chatRoomName);
     }
     
     private class Couple<T1,T2> {
         private T1 t1;
         private T2 t2;
 
         public Couple(T1 t1, T2 t2) {
             this.t1 = t1;
             this.t2 = t2;
         }
         
         public void setSecond(T2 t2) {
            this.t2 = t2;
         }
         
         public void setFirst(T1 t1) {
             this.t1 = t1;
         }
 
         public T1 getFirst() {
             return t1;
         }
         
         public T2 getSecond() {
             return t2;
         }
     }
 
     public Set<String> getConnectedChatRoomList() {
         return clientConnectedChatRoomStructure.keySet();
     }
 
 
 
 }
