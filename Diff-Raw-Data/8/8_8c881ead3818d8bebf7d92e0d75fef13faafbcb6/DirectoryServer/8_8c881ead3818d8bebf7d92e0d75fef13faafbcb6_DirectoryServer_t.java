 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package directory;
 
 import compute.ADUV_constants;
 import compute.DirectoryInterface;
 import compute.NodeInterface;
 import java.rmi.NotBoundException;
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import structs.Message;
 import structs.MessageTypeInterface.MessageType;
 import structs.NodeInfo;
 import structs.TokenColorInterface.Color;
 
 /**
  * Main directory server
  * @author pavel
  */
 public class DirectoryServer implements ADUV_constants{
 
     /**
      * DirectoryInterface
      */
     private DirectoryInterface stub;
     
     /**
      * Registry
      */
     private Registry registry;
     
     public static void main(String[] args) throws InterruptedException{
         int port = DIRECTORY_NAMESPACE_PORT;
         
         if(args.length == 1){
             port = Integer.parseInt(args[0]);
         }
         
         
         System.out.println("Starting directory service.");
         Directory directory = new Directory(); //create directory object
         
         if (System.getSecurityManager() == null){
             System.setSecurityManager(new RMISecurityManager());
         }
         try {
             //export directory
             DirectoryInterface stub = (DirectoryInterface) UnicastRemoteObject.exportObject(directory,DIRECTORY_PORT);
             Registry registry = LocateRegistry.createRegistry(port);
             registry.rebind(DIRECTORY_RMI_NAME, stub);
             System.out.println("Directory server started.");
             //drop to console
             new DirectoryServer().DropToConsole(stub,registry);
         } catch (RemoteException ex) {
             Logger.getLogger(DirectoryServer.class.getName()).log(Level.SEVERE, null, ex);
         }
         
     }
 
     /**
      * Console drop
      */
     private void DropToConsole(DirectoryInterface dir,Registry registry) throws InterruptedException {
         this.stub = dir;
         this.registry = registry;
         Scanner input = new Scanner(System.in);
         while(true){
             try {
                 System.out.print(">> Directory:\t");
                 String inputLine = input.nextLine();
                 parseCommand(inputLine);
             } catch (RemoteException ex) {
                 ex.printStackTrace();
             } catch (NotBoundException ex){
                 ex.printStackTrace();
             }
         }
     }
     
     /**
      * Parse command and start it
      */
     private void parseCommand(String input) throws RemoteException, NotBoundException, InterruptedException {
         if(input.equals("list")){
             System.out.println("Listing nodes:");
             NodeInfo[] nodes = this.stub.listNodes();
             for(NodeInfo node:nodes){
                 System.out.println("\t->"+node.toString()+"\n");
             }
         }else if(input.equals("exit")){
             //wait for all nodes
             for(NodeInfo nodeInfo : this.stub.listNodes()){
                 sendStopMessage(nodeInfo);
             }
             
             while(stub.getNodeCount() != 0){
                 Thread.sleep(1000);
             }
             
             System.exit(1);
         }else if(input.equals("start")){
             for(NodeInfo nodeInfo : this.stub.listNodes()){
                 sendStartMessage(nodeInfo);
             }
         }else{
             System.out.println("Unknown command -> start,list,exit");
         }
     }
 
     /**
      * Sends start message to all nodes.
      * @param nodeInfo 
      */
     private void sendStartMessage(NodeInfo nodeInfo) throws RemoteException, NotBoundException {
         Message startMess = new Message(MessageType.START,-1,Color.BLACK, "");
         String nodeName = NODE_NAME_PREFIX+nodeInfo.getID();
         NodeInterface nodeDirectory = 
                 (NodeInterface)this.registry.lookup(nodeName);
         nodeDirectory.putMessage(startMess);
     }
     
         /**
      * Sends start message to all nodes.
      * @param nodeInfo 
      */
     private void sendStopMessage(NodeInfo nodeInfo) throws RemoteException, NotBoundException {
         Message startMess = new Message(MessageType.STOP,-1,Color.BLACK, "");
         String nodeName = NODE_NAME_PREFIX+nodeInfo.getID();
         NodeInterface nodeDirectory = 
                 (NodeInterface)this.registry.lookup(nodeName);
         nodeDirectory.putMessage(startMess);
     }
 }
