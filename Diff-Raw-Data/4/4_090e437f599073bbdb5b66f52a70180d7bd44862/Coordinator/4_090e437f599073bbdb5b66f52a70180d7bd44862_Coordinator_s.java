 package edu.ch.unifr.diuf.testing_tool;
 
 import net.schmizz.sshj.transport.TransportException;
 import org.apache.commons.configuration.ConfigurationException;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * This object is supposed to control everything it is running.
  *
  * @author Teodor Macicas
  */
 public class Coordinator 
 {   
     private final static Logger LOGGER = Logger.getLogger(
             Coordinator.class.getName());
     
     public static void main(String... args) throws TransportException, IOException {
         MachineManager mm = new MachineManager();
         
         try {
             System.out.println("Parsing properties file ...");
             mm.parsePropertiesFile();
         }
         catch( WrongIpAddressException |
                WrongPortNumberException|
                ClientNotProperlyInitException ex ) {
             LOGGER.log(Level.SEVERE, "Error while setting up a machine. " + ex.getMessage());
             System.exit(1);
         }
         catch( ConfigurationException ex4 ) { 
             LOGGER.log(Level.SEVERE, ex4.getMessage());
             System.exit(4);
         }
         catch( FileNotFoundException ex5 ) {
             LOGGER.log(Level.SEVERE, ex5.getMessage());
             System.exit(5);
         }
         catch( UnwritableWorkingDirectoryException ex6 ) { 
             LOGGER.log(Level.SEVERE, "Either working directory of the server "
                     + " or of a client is not writable. " + ex6.getMessage());
             System.exit(56);
         }
         catch( Exception ex7 ) { 
             LOGGER.log(Level.SEVERE, "Please verify if all needed parameters are "
                     + "declared in the properties file." , ex7);
             System.exit(56);
         }
         // print the machines and tests that have been created according to the properties file
         System.out.println(mm.printMachines());
         System.out.println(mm.printTests());
         
         System.out.println("[INFO] Create the ssh clients for current thread ...");
         mm.createSSHClients();
         
         // are clients set up?
         if( ! mm.checkIfClientsSet() ) { 
             LOGGER.severe("Clients are not yet configured. "
                     + "Please do so before you start once again.");
             System.exit(6);
         }
         // are either all or none loopback addresses used?
         if( ! mm.checkIfAllOrNoneLoopbackAddresses() ) { 
             LOGGER.severe("Please either use loopback addresses for all clients "
                     + "and server OR non-loopback for all machines. This will be "
                     + "more probably they can reach other.");
             System.exit(7);
        } 
         
         System.out.println("[INFO] Checking if all clients can ping the server ...");
         try {
             // can all clients, at least, ping the server?
             if( ! mm.checkClientsCanAccessServer() ) {
                 LOGGER.severe("Not all clients can ping the server. Check once again "
                         + "the IP addresses and/or the network status.");
                 System.exit(8);
             }
         } catch (TransportException ex) {
             LOGGER.log(Level.SEVERE, "Exception catched while checking clients "
                     + "network connection to the server.", ex);
             System.exit(8);
         } catch (IOException ex) {
             LOGGER.log(Level.SEVERE, "Exception catched while checking clients "
                     + "network connection to the server.", ex);
             System.exit(8);
         }
         System.out.println("[INFO] All clients have network connection with server.");
 
         // delete local .data files
         /*try {
             Runtime.getRuntime().exec("/bin/bash rm log*.data").waitFor();
         } catch (IOException|InterruptedException ex) {
             Logger.getLogger(Coordinator.class.getName()).log(Level.SEVERE, null, ex);
         }*/
 
         // upload the programs to clients
         try {
             System.out.println("[INFO] Start uploading the program to clients ...");
             mm.uploadProgramToClients();
         } catch (TransportException ex) {
                 LOGGER.log(Level.SEVERE, ex.getMessage());
                 System.exit(9);
         } catch (IOException ex) {
                 LOGGER.log(Level.SEVERE, ex.getMessage());
                 System.exit(10);
         }
         // now start the connectivity and status threads
         System.out.println("[INFO] Start the connectivity thread. "
                 + "NOTE: if public-key auth is used then this may take some time ...");
         mm.startConnectivityThread();
         
         System.out.println("[INFO] Checking if all machines are in a runnable state ...");
         try {
             // check if all are ok ...
             // sleep a bit before checking, to allow some time for the coordinator
             // to contact each client
             Thread.sleep(2000);
             int retries = 10;
             while( ! mm.allAreConnectionsOK() && retries > 0 ) {
                 --retries;
                 LOGGER.info(" There are some machines that have either ssh problems "
                     + "or just connectivity problems. Wait and retry (left "
                     + "#retries: " + retries + ").");
                 Thread.sleep(10000);
             }
         } catch (InterruptedException ex) {
             ex.printStackTrace();
         }
         System.out.println("[INFO] ALL machines checked and they are in a runnable state.");
         System.out.println("[INFO] Starting the threads for checking connectivity, status and running PIDs. "
                             + "NOTE: if public-key auth is used then this may take some time ...");
         
         // now start all the other thread as the connectity at this point should be ok
         mm.startOtherThreads();
 
         // run tests
         List<TestParams> testParamsList = mm.getTests(); 
         Coordinator coord = new Coordinator();
 
         for(TestParams test: testParamsList) {
             // delete here the output dir for this test (if there was a previous run with old files) 
             String currentDir = new java.io.File( "." ).getCanonicalPath();
             String testDir = currentDir+"/"+test.getFullTestName()+"/";
             try {
                 Runtime.getRuntime().exec(new String[]{"/bin/bash","-c", "rm -r " + testDir}).waitFor();
                 Runtime.getRuntime().exec(new String[]{"/bin/bash","-c", "mkdir " + testDir}).waitFor();
             } catch (InterruptedException ex) {
                 ex.printStackTrace();
             }
             
             // multiple thread numbers
             for( Iterator it=test.getTestThreadNum().iterator(); it.hasNext(); ) {
                 int thread_num = (int)it.next();
                 // one test can be run multiple times 
                 for (int i = 0; i < test.getTestNum(); i++) {                
                     // set server related params
                     mm.getServer().setSourceGraph(test.getTestServerSourceGraphName());
                     mm.getServer().setDestGraph(test.getTestServerDestGraphName());
                     mm.getServer().setGraphReset(test.getTestServerGraphReset());
                     mm.getServer().setReadCons(test.getTestReadCons());
                     mm.getServer().setWriteCons(test.getTestWriteCons());
                     mm.getServer().setTransLockGran(test.getTransLockGran());
                     mm.getServer().setReplFactor(test.getReplicationFactor());
                     mm.getServer().setCheckMyWritesMode(test.getCheckMyWritesMode());
                     mm.getServer().setTestName(test.getFullTestName());
 
                     // set client related params
                     for(int k = 0; k < mm.getClientsNum(); k++) {
                         //mm.getClientNo(j).setInputFilename(test.getTestInputFilename());
                         mm.getClientNo(k).setNoThreads(thread_num);
                         mm.getClientNo(k).setWarmupPeriod(test.getTestWarmupPer());
                         mm.getClientNo(k).setRunningPeriod(test.getTestRunningPer());
                         mm.getClientNo(k).setOperationType(test.getTestOperationType());
                         mm.getClientNo(k).setOperationNum(test.getTestOperationNum());
                         mm.getClientNo(k).setTransRetrials(test.getTestTransRetrials());
                         mm.getClientNo(k).setDiffE(test.getDiffEnt());
                         mm.getClientNo(k).setDiffPperE(test.getDiffPropPerEnt());
                         mm.getClientNo(k).setDiffVperP(test.getDiffValuesPerProp());
                         mm.getClientNo(k).setConflictsFlag(test.getConflictsParameter());
                     }
                     StringBuilder sbTest = new StringBuilder("[INFO] Test with parameters: ");
 
                     sbTest.append(test.getTestServerSourceGraphName()).append("/").append(test.getTestServerDestGraphName()).
                            append(" ").append(test.getTestServerGraphReset()).append(" ").
                            append(test.getTestReadCons()).append(" ").append(test.getTestWriteCons()).append(" ").
                            append(test.getTransLockGran()).append(" ").append(test.getReplicationFactor()).append(" ").
                            append(thread_num).append(" ").append(test.getTestRunningPer()).append(" ").
                            append(test.getTestWarmupPer()).append(" ").append(test.getTestOperationType()).append(" ").
                            append(test.getTestOperationNum()).append(" ").append(test.getTestTransRetrials()).append(" ").
                            append(test.getCheckMyWritesMode());
                     System.out.println(sbTest.toString());
 
                      // before starting the clients, lets check if the data inputfile exists remotely
                     Client c = mm.checkClientsRemoteDatafiles();
                     if( c != null ) { 
                         System.out.println("[FAIL] Client " + c.getId() + " " + c.getIpAddress() + 
                                 " lacks of the input filename " + Utils.getClientRemoteDataFilename(c));
                         break;
                     }
                     coord.runClients(mm, sbTest.toString(), test.getFinalRestultFilename(), i);
                    /* try { 
                         System.out.println("Sleep 10s in between each test ... ");
                         Thread.sleep(10000);
                     } catch( InterruptedException ex ) { }  */
                 }
             }
             // one test scenario is done here, parse the output file
             Parser p = new Parser(testDir+test.getFinalRestultFilename(), 
                     testDir+"data-for-plot.running", mm.getClientsNum(), test.getTestNum());
             p.parseFileRunningStats();
 
             Parser p_vers = new Parser(testDir+test.getFinalRestultFilename()+".vers",
                     testDir+"data-for-plot.vers", mm.getClientsNum(), test.getTestNum());
             p_vers.parseFileVersionsStats();
             try {
                 // make a pause inbetween tests
                 Thread.sleep(2000);
             } catch (InterruptedException ex) {
                 ex.printStackTrace();
             }
         }
         System.out.println("[INFO] Now join all threads ...");
         mm.joinAllThreads();
         System.out.println("[INFO] Now disconnect SSH clients and exit ...");
         mm.disconnectSSHClients(mm.getSSHClients());
     }
 
     /**
      * 
      * @param mm
      * @param testName 
      */
     private void runClients(MachineManager mm, String testName, 
             String finalResultFilename, int testNo) {
         RunClient rc = new RunClient(mm, mm.getServer().getRestartAttempts(), testName,
                 finalResultFilename, testNo);
         rc.start();
         if( mm.getServer().getFaultTolerant().equals("yes") ) {
             mm.startFaultTolerantThread(rc);
         }
         try {
             rc.join();
             mm.joinFaultTolerantThread();
         } catch (InterruptedException ex) {
             ex.printStackTrace();
         }
         //get status 
         if( rc.status != 0 ) 
             LOGGER.log(Level.SEVERE, "Test " + testName + " could not be run. ");
         else
             LOGGER.log(Level.INFO, "Test " + testName + " was successfully run. ");
     }
     
     // it runs the server and client for a given test
     // this thread may be interrupted in case of failure
     class RunClient extends Thread 
     {
         private MachineManager mm;
         private int no_retrials;
         private int status;
         private String testName;
         private String resultFile;
         private int testNo;
         
         public RunClient(MachineManager mm, Integer retrials, 
                 String testName, String finalResultFilename, int testNo) {
             this.mm = mm;
             this.no_retrials = retrials;
             this.testName = testName;
             this.resultFile = finalResultFilename;
             this.testNo = testNo;
         }
         
         public void run() {
             while( true ) {
                 try {
                     if( no_retrials < 0 ) {
                         // this means all retrials have been tried, but without any success
                         status = 1;
                         break;
                     }
                     runClients(mm);
                     status = 0;
                     break;
                 } catch (RerunTestException ex) {
                     --no_retrials;
                     LOGGER.log(Level.INFO, "Test " + ex.getMessage() + " could not be run "
                             + "due to too many failing clients. Retry ... " + no_retrials 
                             + " retrials remaining");
                 }
             }
         }
         
         // start server and client program, synchronize, run test, fetch the logs
         // NOTE: this can be interrupted by the fault tolerant thread 
         private void runClients(MachineManager mm) throws RerunTestException {
             try {
                 System.out.println("[INFO] Deleting any data from previous run ...");
                 // delete client logs 
                 mm.deleteClientLogs();
                 // delete from each client the files that might have been used before
                 mm.deleteClientPreviouslyMessages();
              
                 // run the clients remotely
                 System.out.println("[INFO] START the clients ... and wait some time ... ");
                 mm.startAllClients();
                 
                 // check if all clients are synchronized
                 while( true ) {
                     if( ! mm.checkClientsSynch() ) {
                         System.out.println("[INFO] Clients are not yet ready for warmup. "
                                 + "Wait more time ... ");
                         Thread.sleep(3000);
                     }
                     else
                         break;
                 }
                 System.out.println("[INFO] Clients' are ready for warmup. Now start sending requests to global server.");
 
                 // send a message to the clients to start sending requests as they are now ready
                 mm.sendClientsMsgToStartRequests();
                 System.out.println("[INFO] All clients are now sending requests to the server.");
 
                 // check if tests are completed
                 while( true ) {
                     if( ! mm.checkTestsCompletion() ) {
                         System.out.println("[INFO] Client tests are not done yet ... wait more.");
                         Thread.sleep(5000);
                     }
                     else
                         break;
                 }
                 System.out.println("[INFO] Client tests are done.");
 
                 System.out.println("[INFO] Now locally download the logs from the clients. Final results to file "
                         + resultFile);
                 mm.downloadAllLogs(resultFile, testNo);
                 System.out.println("[INFO] All the logs are downloaded. For further information "
                         + "please check them.");
             } 
             catch (Exception ex) {
                 if( (ex.getCause() != null && ex.getCause() instanceof InterruptedException) 
                       || ex instanceof InterruptedException ) {
                     // if the cause is InterruptedException, most probably, it has been thrown 
                     // by the fault tolerant thread
                     System.out.println("[FAULT TOLERANCE] The condition of dead clients "
                             + "has been reached. Restart the server and the clients for "
                             + "the test " + testName + " ... ");
                     throw new RerunTestException(testName);
                 }
                 else 
                     LOGGER.log(Level.SEVERE,"[EXCEPTION] ", ex);
             }
             finally {
                 // even if success or failure, kill clients and server 
                 try {
                     System.out.println("[INFO] Kill all the clients (if they are still running) ... ");
                     mm.killClients();
                 } catch (Exception ex) {
                     Logger.getLogger(Coordinator.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
 }
 
 class RerunTestException extends Exception 
 {
     public RerunTestException(String string) {
         super(string);
     }
 }
