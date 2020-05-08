 import java.rmi.*;
 import java.rmi.server.*;
 import java.util.*;
 import java.io.*;
 import java.lang.reflect.*;
 
 class ProcessDelegationServer extends UnicastRemoteObject implements MasterServerInterface
 {
     private static final String serverName = "processDelegationServer";
     private volatile List<ProcessManagerClientInterface> clients;
     private volatile List<String> processIDs;
     public int nextPid;
     
 	private static final int BALANCE_LEVEL = 2;
 	
     public ProcessDelegationServer() throws RemoteException
     {
         //Any constructor methods   
         clients = new LinkedList<ProcessManagerClientInterface>();
         processIDs = new LinkedList<String>(); 
 	nextPid = 0;
     }
     
     private class BalanceTimer extends TimerTask {
         public void run() {
         	try{
             	loadBalance();
         	} catch (Exception e) {
         		e.printStackTrace();
         	}
 }
 }
 
 public void loadBalance() throws RemoteException {
     int avg = 0;
     int victim = 0;
     int current = 0;
     String f;
     ProcessManagerClientInterface c;
     ProcessManagerClientInterface victimC;
     List<String> ps;
     List<String> victimPs;
     int load;
     Random r = new Random();
     Map.Entry pairs;
     
     HashMap<ProcessManagerClientInterface,List<String>> files = new HashMap<ProcessManagerClientInterface,List<String>>(); 
 
     for (ProcessManagerClientInterface client : clients) {
         try{
             files.put(client,client.getProcesses());
             avg = avg + client.getProcesses().size();
         } catch (ConnectException e)
         {
             System.out.println("Client Disconnected");
         }
 	}
 	    
 	    if (clients.size() != 0) 
 		    avg = avg / clients.size();
 		else
 		    avg = 0;
 
 		ProcessManagerClientInterface[] clientList = files.keySet().toArray(new ProcessManagerClientInterface[0]);
 		
 	    for(current = 0; current < clientList.length; current++) {
 	        c = clientList[current];
 	        ps = files.get(c);
 	        load = ps.size();
 	        while(load - BALANCE_LEVEL > avg) {
 	        	do {
 	        		victim = r.nextInt(clientList.length);
 	        	} while(victim == current) ;
 	        	
 	        	victimC = clientList[victim];
 	        	victimPs = files.get(victimC);
 	        	f = ps.remove(0); 
 	        	victimPs.add(f);
 	        	files.put(victimC,victimPs);
 	        	load--;
 	        }
 	        files.put(c,ps); 
 	    }
 
 	    
 	    for(current = 0; current < clientList.length; current++) {
 	        // TODO: needs some sort of try/catch
             c = clientList[current];
 	        ps = files.get(c);
 	        c.setProcesses(ps);
 	    }
 	    
     }
 
     public static void main (String []args)
     {
         try
         {
             //Server Stuff
 
             ProcessDelegationServer server = new ProcessDelegationServer();
 
             Naming.rebind (serverName, server);
 
             System.out.println("Process Delegation Server Ready");
         
             for(int i = 0; i < 1000; i++) {
                 Class<? extends MigratableProcess> processClass = GrepProcess.class;
                 String[] strings = {" ", "ProccessDelegationServer.java", "out.txt"};
                 Object[] arguments = {strings};
                 server.addProcess(processClass, arguments);
 	    }
 
             while (true)
             {
 		        server.updateProcessList();
                 //DO SOME LOAD BALANCING
 		        server.startProcesses();
                 server.loadBalance();
 		
                 Thread.sleep(1000);
             }
 
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
     }
 
     public void register(ProcessManagerClientInterface newClient) throws RemoteException
     {
         System.out.println("Client Connected");
 
         clients.add(newClient);
     }
     
     private void addProcess(Class<? extends MigratableProcess> processClass, Object[] args)
     {
         MigratableProcess newProcess = null;
         Class[] classes = new Class[args.length];
         for(int i = 0; i < args.length; i++)
         {
             classes[i] = args[i].getClass();
         }
 
         Constructor<?> constructor = null;
         try { 
             constructor = processClass.getConstructor(classes);
         } catch (NoSuchMethodException e)
         {
             Constructor<?>[] constructors = processClass.getConstructors();
             System.out.println("Possible constructors are: \n" + Arrays.toString( constructors ));
             System.out.println("Incorrect arguments for class");
             e.printStackTrace();
         }
         
         try {
             newProcess = (MigratableProcess) constructor.newInstance(args);
         } catch (Exception e)
         {
             System.out.println("Failed to create class instance");
             e.printStackTrace();
         }
 
         try {
             String fileName = "processes/" + nextPid;
 	    	nextPid++;
             File newProcessFile = new File(fileName);
             newProcessFile.createNewFile();
 
             ProcessIO.writeProcess(newProcess, fileName);
 	    	System.out.println("Process Written " +  fileName);
             processIDs.add(fileName); 
         } catch (Exception e)
         {
             System.out.println("Failed to make file for process");
             e.printStackTrace();
         }
     }
 
     private void updateProcessList()
     {
         for (int i = 0; i < processIDs.size(); i++)
         {
             String fileName = processIDs.get(i);
             File processFile = new File(fileName);
             //If the file is deleted, the process is completed
             if (!processFile.exists())
             {
                 System.out.println("Removing " + fileName);
                 processIDs.remove(i);
             }
         }
     }
 
     private void startProcesses()
     { 
         double numClients = (double) clients.size();
         int numProcesses = (int)Math.ceil((double)processIDs.size()/numClients); 
        List sublist;
 	    ProcessManagerClientInterface current;
         
         for(int i = 0; i < processIDs.size(); i = i + numProcesses)
         {
             for(int c = 0; c < clients.size(); c++)
             {
                 current = clients.get(c); 
 
                 try{
                	sublist = new LinkedList<String>(processIDs.subList(i,i+numProcesses));
                    current.setProcesses(sublist);
                 } 
                 catch(ConnectException|UnmarshalException e)
                 {
                     System.out.println("Client disconnected");
                     clients.remove(current);
                 } 
                 catch(Exception e)
                 {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     private String nextPid() {
         for(int i = 0; i <= Integer.MAX_VALUE; i++) {
             if(processIDs.contains(Integer.toString(i)) == false)
                 return (Integer.toString(i));
         }
         return Integer.toString(-1);
     }
 
 }
