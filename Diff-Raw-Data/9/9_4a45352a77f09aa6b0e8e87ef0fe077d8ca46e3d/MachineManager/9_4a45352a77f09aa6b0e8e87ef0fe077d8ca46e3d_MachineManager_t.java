 package edu.ch.unifr.diuf.workshop.testing_tool;
 
 import edu.ch.unifr.diuf.workshop.testing_tool.Machine.Status;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.schmizz.sshj.SSHClient;
 import net.schmizz.sshj.transport.TransportException;
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 
 /**
  *
  * @author Teodor Macicas
  */
 public class MachineManager 
 {
     private final static Logger LOGGER = Logger.getLogger(
             MachineManager.class.getName());
     
     private ArrayList<Client> clients;
     private ArrayList<SSHClient> sshClients;
     private Server server;
     private List<TestParams> tests;
     
     private MachineConnectivityThread cct;
     private WriteStatusThread wst;
     private CheckMessagesThread cmt;
     private CheckRunningPIDsThread crpt;
     private FaultTolerantThread ftt;
     public static int testNum = 0;
 
     // it regulary checks if the machines are still reachable
     protected class MachineConnectivityThread extends Thread 
     {
         // sleep this time between two consecutive checks
         private int delay;
         private boolean finish;
         private ArrayList<SSHClient> sshClients;
         
         public MachineConnectivityThread(int delay) {
             this.delay = delay;
             this.finish = false;
             this.sshClients = createSSHConnectionsToServerAndClients();
         }
         
         public void setFinished(boolean finish) {
             this.finish = finish;
         }
         
         // regulary check the connection status and update a flag accordingly
         public void run() {
             boolean server_up = false;
             while( !finish ) {
                 // check server connection
                 try {
                     server.checkSSHConnection(sshClients.get(0));
                     server.setStatusConnection(Machine.Status.OK);
                     server_up = true;
                 } catch (SSHConnectionException ex) {
                     server.setStatusConnection(Machine.Status.SSH_CONN_PROBLEMS);
                     LOGGER.log(Level.SEVERE, "Connectivity problems with the "
                             + "server ("+server.getIpAddress()+"). Check logs. "
                             + "Retry in "+delay+"ms.", 
                             ex);
                     server_up = false;
                 }
 
                 if( server_up ) { 
                     // now check client connectivity only if the server is up
                     for(Iterator it=clients.iterator(); it.hasNext(); ) {
                         Client c = (Client)it.next();
                         try {
                            c.checkSSHConnection(sshClients.get(c.getId()+1));
                            c.setStatusConnection(Machine.Status.OK);
                         } catch (SSHConnectionException ex) {         
                             c.setStatusConnection(Machine.Status.SSH_CONN_PROBLEMS);
                             LOGGER.log(Level.SEVERE, "Connectivity problems with the client "
                                     +c.getIpAddress()+"). Check logs. "
                                     + "Retry in "+delay+"ms.", ex);
                         }
                     }
                 }
                 
                 try {
                     // now sleep a bit until next check
                     Thread.sleep(delay);
                 } catch (InterruptedException ex) {
                     ex.printStackTrace();
                 }
             }
             disconnectSSHClients(sshClients);
         }
     }
       
     // it regulary writes to a log file the status of the running machines
     protected class WriteStatusThread extends Thread 
     {
         // sleep this time between two consecutive checks
         private int delay;
         private boolean finish;
         private FileOutputStream fis;
         private DateFormat dateFormat;
         
         
         public WriteStatusThread(int delay, String filePath) {
             this.delay = delay;
             this.finish = false;
             try {
                 File f = new File(filePath);
                 if( f.exists() ) 
                     f.delete();
                 f.createNewFile();
                 this.fis = new FileOutputStream(f);
             } catch (IOException ex) {
                 LOGGER.log(Level.SEVERE, null, ex);
             }
             this.dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         }
         
         public void setFinished(boolean finish) {
             this.finish = finish;
         }
         
         //regulary write the status on an output file
         @Override
         public void run() {
             while( !finish ) {
                 StringBuilder sb = new StringBuilder();
                 sb.append("\nMachine status @ ");
                 sb.append(dateFormat.format(new Date())+"\n");
                 // write server status 
                 sb.append("\tServer: \n\t\t");
                 sb.append(server.getStatusMessage());
                 sb.append("\n\t");
                 
                 // write clients status
                 sb.append("Clients: \n\t\t");
                 for(Iterator it=clients.iterator(); it.hasNext(); ) {
                     Client c = (Client)it.next();
                     sb.append(c.getStatusMessage()+"\n\t\t");
                 }
                 
                 try {
                     // write to file 
                     fis.write(sb.toString().getBytes());
                 } catch (IOException ex) {
                     LOGGER.log(Level.SEVERE, "Exception while writing status to "
                             + "output file.", ex);
                 }
                 
                 try {
                     // now sleep a bit 
                     Thread.sleep(delay);
                 } catch (InterruptedException ex) {
                     ex.printStackTrace();
                 }
             }
         }
     }
     
     // it regulary checks remotely for messages (e.g. if the clients are ready to 
     // start requests or if they are finished )
     protected class CheckMessagesThread extends Thread 
     {
         // sleep this time between two consecutive checks
         private int delay;
         private boolean finish;
         private FileOutputStream fis;
         private ArrayList<SSHClient> sshClients;
         
         public CheckMessagesThread(int delay) {
             this.delay = delay;
             this.finish = false;
             this.sshClients = createSSHConnectionsToServerAndClients();
         }
         
         public void setFinished(boolean finish) {
             this.finish = finish;
         }
         
         /*
          * regulary check the status of remote clients and set it accordingly to 
          * out local objects
          */
         @Override
         public void run() {
             int r;
             while( !finish ) {
                 try {
                     for(Iterator it=clients.iterator(); it.hasNext(); ) {
                         Client c = (Client)it.next();
                         // check if the remote client is done with the requests
                         r = SSHCommands.testRemoteFileExists(c, 
                                 Utils.getClientRemoteDoneFilename(c), sshClients.get(c.getId()+1));
                         if( r == 0 ) {
                             c.setStatusSynch(Status.DONE);
                             continue;
                         }
                         // if not, test if the cleint is still sending requests 
                         r = SSHCommands.testRemoteFileExists(c, 
                                 Utils.getClientRemoteStartRequestsFilename(c), sshClients.get(c.getId()+1));
                         if( r == 0 ) {
                             c.setStatusSynch(Status.RUNNING_REQUESTS);
                             continue;
                         }
                         // if not, check if the client has its threads synchronized
                         r = SSHCommands.testRemoteFileExists(c, 
                                 Utils.getClientRemoteSynchThreadsFilename(c), sshClients.get(c.getId()+1));
                         if( r == 0 ) {
                             c.setStatusSynch(Status.SYNCH_THREADS);
                             continue;
                         }
                         // if not, then no info yet about client synch
                         c.setStatusSynch(Status.NOT_INIT);
                     }
                 } catch (TransportException ex) {
                     ex.printStackTrace();
                 } catch (IOException ex) {
                     ex.printStackTrace();
                 }
                 try {
                     // now sleep a bit 
                     Thread.sleep(delay);
                 } catch (InterruptedException ex) {
                     ex.printStackTrace();
                 }
             }
             disconnectSSHClients(sshClients);
         }
     }
     
     // it regulary checks if server and client PIDs are still running
     // and it changes the status accordingly 
     protected class CheckRunningPIDsThread extends Thread 
     {
         // sleep this time between two consecutive checks
         private int delay;
         private boolean finish;
         private FileOutputStream fis;
         private ArrayList<SSHClient> sshClients;
         
         public CheckRunningPIDsThread(int delay) {
             this.delay = delay;
             this.finish = false;
             this.sshClients = createSSHConnectionsToServerAndClients();
         }
         
         public void setFinished(boolean finish) {
             this.finish = finish;
         }
         
         /*
          * regulary check the status of remote clients and set it accordingly to 
          * out local objects
          */
         @Override
         public void run() {
             int r;
             while( !finish ) {
                 try {
                     // check server
                     if( server.getPID() != 0 ) {
                         r = SSHCommands.checkIfRemotePIDIsRunning(server, 
                                 server.getPID(), sshClients.get(0));
                         if( r == 0 )
                             server.setStatusProcess(Status.PID_RUNNING);
                         else
                             server.setStatusProcess(Status.PID_NOT_RUNNING);
                     }
                     else
                         server.setStatusProcess(Status.NOT_INIT);
                     
                     // check clients 
                     for(Iterator it=clients.iterator(); it.hasNext(); ) {
                         Client c = (Client)it.next();
                         // check only if the program has been started and the PID gathered
                         if( c.getPID() != 0 ) {
                             r = SSHCommands.checkIfRemotePIDIsRunning(c,
                                     c.getPID(), sshClients.get(c.getId()+1));
                             if( r == 0 )
                                 c.setStatusProcess(Status.PID_RUNNING);
                             else
                                 c.setStatusProcess(Status.PID_NOT_RUNNING);
                         }
                         else 
                             c.setStatusProcess(Status.NOT_INIT);
                     }
                 } catch (TransportException ex) {
                     ex.printStackTrace();
                 } catch (IOException ex) {
                     ex.printStackTrace();
                 }
                 try {
                     // now sleep a bit 
                     Thread.sleep(delay);
                 } catch (InterruptedException ex) {
                     ex.printStackTrace();
                 }
             }
             disconnectSSHClients(sshClients);
         }
     }
     
     // if the failing condition is met, then it interrupts the main thread
     protected class FaultTolerantThread extends Thread 
     {
         // sleep this time between two consecutive checks
         private int delay;
         private boolean finish;
         private Thread runningTestThread;
         private double failingClients;
         private ArrayList<SSHClient> sshClients;
         
         public FaultTolerantThread(int delay, Thread runningTestThread) {
             this.delay = delay;
             this.finish = false;
             this.runningTestThread = runningTestThread;
             this.sshClients = createSSHConnectionsToServerAndClients();
         }
         
         public void setFinished(boolean finish) {
             this.finish = finish;
         }
         
         private boolean failingCondition() throws TransportException, IOException { 
             int failing_clients=0;
             if( server.status_process == Status.PID_NOT_RUNNING ) 
                 return true;
             
             for(Iterator it=clients.iterator(); it.hasNext(); ) {
                 Client c = (Client)it.next();
                 
                 if( (c.status_process == Status.PID_NOT_RUNNING && 
                     c.status_synch != Status.DONE) ) {
                     System.out.println("[FAILURE] Client PID NOT RUNNING and status NOT DONE");
                     failing_clients++;
                     continue;
                 } 
                 // client running, but lately no data in the log
                 if( c.status_process == Status.PID_RUNNING && 
                         !c.isProgressing(sshClients.get(c.getId()+1)) ) {
                     System.out.println("[FAILURE] Client PID RUNNING, but no progress on the log file. "
                             + "Last progress it was " + c.getLastLogModification() + " seconds ago ...");
                     failing_clients++;
                 }
             }
             if( failing_clients == 0 ) 
                 return false;
             failingClients = ((double)failing_clients/clients.size());
             //System.out.println("Failing clients percentage: " + failingClients);
             if( failingClients >= 
                     clients.get(0).getRestartConditionPropThreadsDead() ) {
                 return true;
             }
             return false;
         }
         
         /*
          * regulary check the status of remote clients and interrupt the one 
          * running the test if the failing condition is met 
          */
         @Override
         public void run() {
             while( !finish ) {
                 try {
                     // now sleep a bit 
                     Thread.sleep(delay);
                     if( failingCondition() && runningTestThread.isAlive() ) {
                         // if clients failed, then notify main thread and wait that 
                         // the server and clients are restarted
                         System.out.println("[FAILURE] " + (failingClients*100) + "% of the clients "
                                 + "have failed. Therefore, restart the test ...");
                         runningTestThread.interrupt();
                         Thread.sleep(15000);
                     }
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
             }
             disconnectSSHClients(sshClients);
         }
     }
     
     public MachineManager() {
         this.clients = new ArrayList<Client>();
         this.tests = new ArrayList<>();
         this.server = null;
     }
     
      /**
      *
      * @param a new client machine
      */
     public void addNewClient(Client new_client) throws ClientNotProperlyInitException { 
         if( new_client.clientProperlyCreated() )
             this.clients.add(new_client);
         else 
             throw new ClientNotProperlyInitException("Client " + new_client.getIpAddress()
                     + " does not have all needed params set up.");
     }
     
      /**
      *
      * @return a client from the list of them
      */
     public Client getClientNo(int no) { 
         if( no < 0 || no > clients.size() ) 
             return null;
         return clients.get(no);
     }
 
     /**
      * 
      * @return 
      */
     public int getClientsNum() {
         return clients.size();
     }
     
      /**
      *
      * @param the server that will be used by the clients
      */
     public void setServer(Server s) { 
         this.server = s; 
     }
     
      /**
      *
      * @return server
      */
     public Server getServer() { 
         return this.server;
     }
 
 
     public List<TestParams> getTests() {
         return tests;
     }
 
     /**
      * 
      * @return 
      */
     public ArrayList<SSHClient> getSSHClients() { 
         return this.sshClients;
     }
     
     /**
      * Parse the .properties file and create the server and clients objects.
      * 
      * @throws ConfigurationException
      * @throws WrongIpAddressException
      * @throws WrongPortNumberException
      * @throws ClientNotProperlyInitException 
      * @throws FileNotFoundException
      */
     public void parsePropertiesFile() throws ConfigurationException, 
             WrongIpAddressException, WrongPortNumberException, ClientNotProperlyInitException, 
             FileNotFoundException, UnwritableWorkingDirectoryException, TransportException, IOException { 
         Configuration config = new PropertiesConfiguration(
                 Utils.PROPERTIES_FILENAME);
         
         // get server program filename 
         Utils.SERVER_PROGRAM_LOCAL_FILENAME = config.getString("server.programJarFile").trim();
         if( Utils.SERVER_PROGRAM_LOCAL_FILENAME.isEmpty() ||
             new File(Utils.SERVER_PROGRAM_LOCAL_FILENAME).exists() == false ) {
             
             throw new FileNotFoundException("Server local program file (" + 
                     Utils.SERVER_PROGRAM_LOCAL_FILENAME + ") does not exist. "
                     + "Please pass an already existing file or a correct path.");
         }
         
         // get server info 
         String serverDataFolder = config.getString("server.dataFolder");
         String serverSSHUserHost = config.getString("server.sshUserHostPort");
         String serverSSHUsername = serverSSHUserHost.substring(0, serverSSHUserHost.indexOf("@"));
         String serverSSHIpPort = serverSSHUserHost.substring(serverSSHUserHost.indexOf("@")+1);
         String serverListenHostPort = config.getString("server.listenHostPort");
         String serverFaultTolerant = config.getString("server.faultTolerant");
         String serverRestartAttempts = config.getString("server.restartAttempts");
         String serverWorkingDir = config.getString("server.workingDirectory");
         //String serverSSHPassword = config.getString("server.sshPassword");
         StringTokenizer st = new StringTokenizer(serverSSHIpPort, ":");
         if( st.countTokens() != 2 ) { 
             LOGGER.severe("Parsing error of server.sshIpPort. Please pass data "
                     + "using the pattern IP:PORT and nothing more. " + serverSSHIpPort);
             throw new ConfigurationException("Parsing error of server.sshIpPort. "
                     + "Please pass data using the pattern IP:PORT and nothing more. " 
                     + serverSSHIpPort);
         }
         // create the server here
         this.server = new Server(st.nextToken(), Integer.valueOf(st.nextToken()), 
                 serverSSHUsername);
         st = new StringTokenizer(serverListenHostPort, ":");
         if( st.countTokens() != 2 ) {
             LOGGER.severe("Parsing error of server.listenHostPort. Please pass data "
                     + "using the pattern IP:PORT and nothing more. " + serverSSHIpPort);
             throw new ConfigurationException("Parsing error of server.listenHostPort. "
                     + "Please pass data using the pattern IP:PORT and nothing more. " 
                     + serverSSHIpPort);
         }
         this.server.setServerHTTPListenAddress(st.nextToken());
         this.server.setServerHttpPort(Integer.valueOf(st.nextToken()));
        /* if( SSHCommands.checkIfRemoteDirIsWritable(server, serverWorkingDir, sshClients.get(0)) != 0 )
             throw new UnwritableWorkingDirectoryException(serverWorkingDir);
         else*/
         this.server.setWorkingDirectory(serverWorkingDir);
         this.server.setDataFolderPath(serverDataFolder);
         this.server.setFaultTolerant(serverFaultTolerant.trim());
         if( Integer.valueOf(serverRestartAttempts) < 0 ) {
             LOGGER.severe("Parsing error of server.restartAttepmts. Please check the "
                     + "right format in the properties file and re-run this.");
             throw new ConfigurationException("Parsing error of server.restartAttempts. "
                     + "Please check the right format in the properties file and re-run this.");
         }
         this.server.setRestartAttempts(Integer.valueOf(serverRestartAttempts));        
         
         // get clients program filename 
         Utils.CLIENT_PROGRAM_LOCAL_FILENAME = config.getString("clients.programJarFile").trim();
         if( Utils.CLIENT_PROGRAM_LOCAL_FILENAME.isEmpty() ||
             ! new File(Utils.CLIENT_PROGRAM_LOCAL_FILENAME).exists() ) {
             throw new FileNotFoundException("Client local program file (" + 
                     Utils.CLIENT_PROGRAM_LOCAL_FILENAME + ") does not exist. "
                     + "Please pass an already existing file or a correct path.");
         }
         
         // this is used by the clients 
         List clientsUserHostPort = config.getList("clients.sshUserHostPort");
         String clientsWorkingDir = config.getString("clients.workingDirectory");
         String clientsRestartCondition = config.getString("clients.restartConditionPropThreadsDead");
         double clients_rest_cond = Double.valueOf(clientsRestartCondition);
         if( clients_rest_cond < 0 || clients_rest_cond > 1 ) { 
             LOGGER.severe("Please give restartConditionPropThreadsDead parameter"
                     + " as double between 0 and 1 inclusive. ");
             throw new ConfigurationException("Please give restartConditionPropThreadsDead parameter"
                     + " as double between 0 and 1 inclusive. ");
         }
         String clientsTimeoutSec = config.getString("clients.timeoutSeconds");
         List clientsTests = config.getList("clients.tests");
 
         // iterate the list and create the clients
         int counter = -1;
         for(Iterator it=clientsUserHostPort.iterator(); it.hasNext(); ) {
             ++counter;
             String client_host = (String)it.next();
             String clientSSHUsername = client_host.substring(0, client_host.indexOf("@"));
             String clientSSHIpPort = client_host.substring(client_host.indexOf("@")+1);
             st = new StringTokenizer(clientSSHIpPort, ":");
             if( st.countTokens() > 2 ) { 
                 LOGGER.severe("Parsing error of client.sshUserHostPort. Please pass data "
                     + "using the pattern username@IP:PORT. " + client_host);
                 throw new ConfigurationException("Parsing error of client.sshUserHostPort. "
                     + "Please pass data using the pattern username@IP:PORT. "
                     + client_host);
             }
             Client c = new Client(st.nextToken(), Integer.valueOf(st.nextToken()), 
                     clientSSHUsername, counter);
             // set server info 
             c.setServerInfo(server.getIpAddress(), server.getServerHttpPort());
             c.setRestartConditionPropThreadsDead(clients_rest_cond);
             c.setTimeoutSec(Integer.valueOf(clientsTimeoutSec));
             c.setWorkingDirectory(clientsWorkingDir);
             
             // add tests
             for( Iterator it2=clientsTests.iterator(); it2.hasNext(); ) {
                 c.addNewTest((String)it2.next());
             }
             // store this client 
             this.addNewClient(c);
         }
 
         String[] testsPropertyFileNames = config.getString("clients.tests").split("\\s+");
         for(String name : testsPropertyFileNames) {
            String nameWithExtension = name + ".properties";
             Configuration testConfig = new PropertiesConfiguration(nameWithExtension);
             String serverType = testConfig.getString("server.type");
             String[] serverModes = testConfig.getString("server.mode").split("\\s+");
             int testNum = testConfig.getInt("test.num");
             String[] requestNum = testConfig.getString("test.request.num").split("\\s+");
             String[] delays = testConfig.getString("test.delays").split("\\s+");
             int numThreads = testConfig.getInt("test.threads.num");
             tests.add(new TestParams(serverType, serverModes, testNum, requestNum, delays, numThreads));
         }
     }
     
     /**
      * 
      */
     public void createSSHClients() {
         // now create also the ssh 
         this.sshClients = createSSHConnectionsToServerAndClients();
     }
     
     /**
      * 
      * @return 
      */
     public ArrayList<SSHClient> createSSHConnectionsToServerAndClients() {
         ArrayList<SSHClient> ssh_clients = new ArrayList<>();
         SSHClient server_ssh = new SSHClient();
         try {
             server_ssh.loadKnownHosts();
             server_ssh.connect(server.getIpAddress(), server.getPort());
             server_ssh.authPublickey(server.getSSHUsername());
             ssh_clients.add(0, server_ssh);
             //System.out.println("add server ");
             
             SSHClient client_ssh; 
             for(Iterator it=clients.iterator(); it.hasNext(); ) {
                 Client c = (Client) it.next(); 
                 //System.out.println("add client " + c.getIpAddress());
                 client_ssh = new SSHClient();
                 client_ssh.loadKnownHosts();
                 client_ssh.connect(c.getIpAddress(), c.getPort());
                 client_ssh.authPublickey(c.getSSHUsername());
                 ssh_clients.add(client_ssh);
             }
         } catch (IOException ex) {
             LOGGER.log(Level.SEVERE, null, ex);
         }
         return ssh_clients;
     }
     
     /**
      * 
      * @param ssh_clients 
      */
     public void disconnectSSHClients(ArrayList<SSHClient> ssh_clients) { 
         for(Iterator it=ssh_clients.iterator(); it.hasNext(); ) {
             SSHClient s = (SSHClient) it.next();
             try {
                 s.disconnect();
             } catch (IOException ex) {
                 LOGGER.log(Level.SEVERE, null, ex);
             }
         }
     }
     
     
     /**
      *
      * @return false if either the server or clients are not yet set
      */
     public boolean checkIfClientAndServerSet() { 
         if( this.server == null || this.clients.isEmpty() ) 
             return false;
         return true;
     }
     
     /**
      * Either all or none ip addresses are from 127.0.0.0/8 subnetwork.
      * 
      * @return true if either all or none are loopback
      */
     public boolean checkIfAllOrNoneLoopbackAddresses() { 
         boolean all_loopback = true;
         if( ! Utils.isLoopbackIpAddress(server.getIpAddress()) )
             all_loopback = false;
         for(Iterator it=clients.iterator(); it.hasNext(); ) { 
             Client c = (Client)it.next();
             if( all_loopback && !Utils.isLoopbackIpAddress(c.getIpAddress()) ) {
                 // at least one is not loopback, but the others are loopback
                 return false;
             }
             if( !all_loopback && Utils.isLoopbackIpAddress(c.getIpAddress()) ) {
                 // at least one is loopback, but the others are not 
                 return false; 
             }
         }
         return true;
     }
     
     
     /**
      * All clients must have network connection with the server. Check it by 
      * remotely running a ping command. 
      * 
      * @return 
      */
     public boolean checkClientsCanAccessServer()
             throws TransportException, IOException { 
         int r;
         // iterate through clients and check whether they can ping the server
         for(Iterator it=clients.iterator(); it.hasNext(); ) {
             Client c = (Client) it.next(); 
             r = SSHCommands.clientPingServer(c, sshClients.get(c.getId()+1));
             if( r != 0 )
                 return false;
         }
         return true;
     }
     
     
     /**
      * Upload the program to the clients
      * 
      * @throws FileNotFoundException
      * @throws IOException 
      */
     public void uploadProgramToClients() 
             throws FileNotFoundException, IOException {        
         for(Iterator it=clients.iterator(); it.hasNext(); ) {
             Client c = (Client)it.next(); 
             c.uploadProgram(Utils.CLIENT_PROGRAM_LOCAL_FILENAME, sshClients.get(c.getId()+1));
             // test the remote file exists
             int r = SSHCommands.testRemoteFileExists(c, 
                     Utils.getClientProgramRemoteFilename(c), sshClients.get(c.getId()+1));
             if( r == 0 )
                 System.out.println("[INFO] Remote file successfuly uploaded on client " + 
                         c.getIpAddress() + ".");
             else 
                 System.out.println("[INFO] Remote file could not be uploaded on client " + 
                         c.getIpAddress() + "." + " Exit code: " + r);
         }
     }
     
     /**
      * Upload the program to the clients
      * 
      */
     public void uploadProgramToServer() 
             throws FileNotFoundException, IOException {        
         server.uploadProgram(Utils.SERVER_PROGRAM_LOCAL_FILENAME, 
                 sshClients.get(0));
         // test the remote file exists
         int r = SSHCommands.testRemoteFileExists(server, 
                 Utils.getServerProgramRemoteFilename(server), sshClients.get(0));
         if( r == 0 )
             System.out.println("[INFO] Remote file successfuly uploaded on server " + 
                         server.getIpAddress() + ".");
         else 
             System.out.println("[INFO] Remote file could not be uploaded on server " + 
                         server.getIpAddress() + "." + " Error code: " + r);
         
         // make remote dir 'data' to copy all the files 
         SSHCommands.createRemoteFolder(server, server.getWorkingDirectory()+"/data/", 
                 sshClients.get(0));
         
         String files;
         File folder = new File(server.getDataFolderPath());
         File[] listOfFiles = folder.listFiles();  
         for (int i = 0; i < listOfFiles.length; i++) {
             if (listOfFiles[i].isFile()) {
                 files = listOfFiles[i].getName();
                 // and now upload the data folder as well 
                 SSHCommands.uploadRemoteFile(server, server.getDataFolderPath()+"/"+files, 
                         server.getWorkingDirectory()+"/data/"+files, sshClients.get(0));
             }
         }
     }
     
     /**
      * 
      * @throws TransportException
      * @throws IOException 
      */
     public void deleteServerDataFiles() throws TransportException, IOException {
         SSHCommands.deleteRemoteFile(server, server.getWorkingDirectory()+"/data/", 
                 sshClients.get(0));
     }
     
     /**
      * 
      * @return true if all connectivity statuses are OK
      */
     public boolean allAreConnectionsOK() {
         if( server.getStatusConnection() != Status.OK ) {
             System.err.println("[ERROR] Server has network connection problem.");
             return false;
         }
         for(Iterator it=clients.iterator(); it.hasNext(); ) {
             Client c = (Client)it.next();
             if( c.getStatusConnection() != Status.OK ) {
                 System.err.println("[ERROR] Client "+c.getIpAddress()
                         +" has network connection problem.");
                 return false;
             }
         }
        return true;
     }
     
     /**
      * 
      * @throws TransportException
      * @throws IOException 
      */
     public void startServer() throws TransportException, IOException, InterruptedException {
         server.runServerRemotely(sshClients.get(0));
     }
     
     /**
      * 
      * @throws TransportException
      * @throws IOException 
      */
     public void startAllClients() throws TransportException, IOException, InterruptedException {
         for(Iterator it=clients.iterator(); it.hasNext(); ) { 
             Client c = (Client)it.next();
             c.runClientRemotely(this.server, sshClients.get(c.getId()+1));
         }
     }    
     
     /**
      * Start only the connectivity thread firstly.
      */
     public void startConnectivityThread() {
          this.cct = new MachineConnectivityThread(Utils.DELAY_CHECK_CONN_MS);
          this.cct.start();
     }
     
     /**
      * 
      * Just run the status, check messages and check PIDs threads. 
      */
     public void startOtherThreads() {
         this.wst = new WriteStatusThread(Utils.DELAY_WRITE_STATUS_MS, Utils.STATUS_FILENAME);
         this.cmt = new CheckMessagesThread(Utils.DELAY_CHECK_MESSAGES);
         this.crpt = new CheckRunningPIDsThread(Utils.DELAY_CHECK_RUNNING_PIDS);
         try {
              // write status 
              this.wst.start();
              Thread.sleep(200);
              // check messages 
              this.cmt.start();
              Thread.sleep(200);
              // check PIDs
              this.crpt.start();
              Thread.sleep(400);
         } catch (InterruptedException ex) {
             ex.printStackTrace();
         }
     }
     
     /**
      * 
      * @return  true if all clients are synchronized
      */
     public boolean checkClientsSynch() { 
         //check every client synch status
         for(Iterator it=clients.iterator(); it.hasNext(); ) { 
             Client c = (Client)it.next();
             if( c.getStatusSynch() != Status.SYNCH_THREADS ) 
                 return false;
         }
         return true;
     }
     
     /**
      * 
      * @throws TransportException
      * @throws IOException 
      */
     public void killServer() throws TransportException, IOException { 
         server.killServer(sshClients.get(0));
         server.killTop(sshClients.get(0));
     }
     
     /**
      * 
      * @throws TransportException
      * @throws IOException 
      */
     public void killClients() throws TransportException, IOException { 
         for(Iterator it=clients.iterator(); it.hasNext(); ) { 
             Client c = (Client)it.next();
             c.killClient(sshClients.get(c.getId()+1));
         }
     }
     
     /**
      * 
      * @throws TransportException
      * @throws IOException 
      */
     public void sendClientsMsgToStartRequests() throws TransportException, IOException { 
         for(Iterator it=clients.iterator(); it.hasNext(); ) { 
             Client c = (Client)it.next();
             SSHCommands.createRemoteFile(c, Utils.getClientRemoteStartRequestsFilename(c),
                     sshClients.get(c.getId()+1));
         }
     }
     
     /**
      * 
      * @return  true if all clients finished their tests
      */
     public boolean checkTestsCompletion() { 
         //check every client synch status
         for(Iterator it=clients.iterator(); it.hasNext(); ) { 
             Client c = (Client)it.next();
             if( c.getStatusSynch() != Status.DONE ) 
                 return false;
         }
         return true;
     }
     
     /**
      * Download on local folder all the logs from server and clients.
      * @throws IOException 
      */
     public void downloadAllLogs() throws IOException { 
         SSHCommands.downloadRemoteFile(server, Utils.getServerLogRemoteFilename(server),
                 Utils.getServerLocalFilename(server,testNum), sshClients.get(0));
         System.out.println("[INFO] Server log file " + Utils.getServerLocalFilename(server,testNum) +
                 " is locally downloaded. Please check it." );
         SSHCommands.downloadRemoteFile(server, Utils.getServerLogTopRemoteFilename(server),
                 Utils.getServerLocalTopFilename(server,testNum), sshClients.get(0));
         int counter=-1;
         for(Iterator it=clients.iterator(); it.hasNext(); ) { 
             Client c = (Client)it.next();
             SSHCommands.downloadRemoteFile(c, Utils.getClientLogRemoteFilename(c),
                     Utils.getClientLocalFilename(c, ++counter, testNum), 
                     sshClients.get(c.getId()+1));
         }
         testNum++;
     }
     
     /**
      * 
      * Join the status threads;
      */
     public void joinAllThreads() {
         this.cct.setFinished(true);
         this.wst.setFinished(true);
         this.cmt.setFinished(true);
         this.crpt.setFinished(true);
         try {
             //System.out.println("Join cct");
             this.cct.join(4000);
             //System.out.println("Join wst");
             this.wst.join(4000);
             //System.out.println("Join cmt");
             this.cmt.join(4000);
             //System.out.println("Join crpt");
             this.crpt.join(4000);
             if( this.ftt != null ) {
                 //System.out.println("Join FTT");
                 this.ftt.setFinished(true);
                 this.ftt.join(4000);
             }
         } catch (InterruptedException ex) {
             ex.printStackTrace();
         }
     }
 
     /**
      * 
      * @param testThread 
      */
     public void startFaultTolerantThread(Thread testThread) { 
         this.ftt = new FaultTolerantThread(Utils.DELAY_CHECK_FAULT_MS, testThread);
         this.ftt.start();
     }
     
     /**
      * 
      * @throws InterruptedException 
      */
     public void joinFaultTolerantThread() throws InterruptedException { 
         this.ftt.setFinished(true);
         this.ftt.join(4000);
     }
     
     /**
      * 
      * @throws TransportException
      * @throws IOException 
      */
     public void deleteClientPreviouslyMessages() 
             throws TransportException, IOException { 
         for(Iterator it=clients.iterator(); it.hasNext(); ) {
             Client c = (Client)it.next();
             c.deletePreviousRemoteMessages(sshClients.get(c.getId()+1));
         }
     }
     
     /**
      * 
      * @throws TransportException
      * @throws IOException 
      */
     public void deleteClientLogs() throws TransportException, IOException {
         for(Iterator it=clients.iterator(); it.hasNext(); ) {
             Client c = (Client)it.next();
             SSHCommands.deleteRemoteFile(c, c.getWorkingDirectory()+"/log*.data", 
                     sshClients.get(c.getId()+1));
         }
     }
     
     /**
      * 
      * @throws TransportException
      * @throws IOException 
      */
     public void deleteServerLogs() 
             throws TransportException, IOException { 
         SSHCommands.deleteRemoteFile(server, server.getWorkingDirectory()+"/log*.data", 
                 sshClients.get(0));
     }
     
     /**
      * Print information about server and all clients.
      * 
      * @return 
      */
     public String printMachines() { 
         StringBuilder sb = new StringBuilder();
         
         sb.append("\nServer: "); 
         sb.append("\n\t ssh info: ");
         sb.append(server.getSSHUsername()).append("@");
         sb.append(server.getIpAddress()).append(":").append(server.getPort());
         sb.append("\n\t http server info: "); 
         sb.append(server.getServerHTTPListenAddress()).append(":");
         sb.append(server.getServerHttpPort());
         sb.append("\n\t fault tolerance: "); 
         sb.append(server.getFaultTolerant()).append(" ");
         sb.append(server.getRestartAttempts()).append("retrials ");
         sb.append("\n\t working directory: "); 
         sb.append(server.getWorkingDirectory());
         sb.append("\nClients:");
         
         int counter = -1;
         for(Iterator it=clients.iterator(); it.hasNext(); ) {
             Client c = (Client)it.next();
             ++counter;
             sb.append("\n\tClient ").append(counter); 
             sb.append("\n\t\t ssh info: ");
             sb.append(c.getSSHUsername()).append("@");
             sb.append(c.getIpAddress()).append(":").append(c.getPort());
             sb.append("\n\t\t server info: ");
             sb.append(c.getServerIpAddress()).append(" ").append(c.getServerPort());
             sb.append("\n\t\t working directory: "); 
             sb.append(c.getWorkingDirectory());
             sb.append("\n\t\t running tests: "); 
             sb.append(c.testsToString());
             sb.append("\n\t\t fault tolerance: "); 
             sb.append(c.getRestartConditionPropThreadsDead()).append(" percentage ");
             sb.append("of needed dead clients to restart test");
             sb.append("\n\t\t fault tolerance timeout: after "); 
             sb.append(c.getTimeoutSec()).append("seconds of no log activity client is considered failed");
         }
         sb.append("\n");
         return sb.toString();
     }
 
     /**
      * Initialize new working threads to be able to reuse Machine Manager
      * 
      **/
     public void updateWorkingThreads() {
         this.cct = new MachineConnectivityThread(Utils.DELAY_CHECK_CONN_MS);
         this.wst = new WriteStatusThread(Utils.DELAY_CHECK_CONN_MS, Utils.STATUS_FILENAME);
         this.cmt = new CheckMessagesThread(Utils.DELAY_CHECK_CONN_MS);
         this.crpt = new CheckRunningPIDsThread(Utils.DELAY_CHECK_CONN_MS);
     }
 }
