 package edu.ch.unifr.diuf.workshop.testing_tool;
 
 import java.io.File;
 import java.util.regex.Pattern;
 import java.util.UUID;
 
 /**
  *
  * @author Teodor Macicas
  */
 public class Utils 
 {   
    public static final String PROPERTIES_FILENAME = "server_clients.properties";
     
     public static final Integer DELAY_CHECK_CONN_MS = 4000;
     public static final Integer DELAY_WRITE_STATUS_MS = 4000;
     public static final Integer DELAY_CHECK_MESSAGES = 5000;
     public static final Integer DELAY_CHECK_RUNNING_PIDS = 5000;
     public static final Integer DELAY_CHECK_FAULT_MS = 20000;
     public static final String STATUS_FILENAME = "status.log";
     
     // these 2 must be set during the parsing of the properties file
     public static String CLIENT_PROGRAM_LOCAL_FILENAME;
     public static String SERVER_PROGRAM_LOCAL_FILENAME;
     
     // this must be a jar file ... 
     public static final String CLIENT_PROGRAM_REMOTE_FILENAME = "client.jar";
     public static final String SERVER_PROGRAM_REMOTE_FILENAME = "server.jar";
     
     // some suffixes used to create the remote files for signaling different statuses
     // IMPORTANT: the algorithm using these will fail if the clients do not use the same names!!!
     public static final String CLIENT_REMOTE_FILENAME_SUFFIX_THREADS_SYNCH = "-threads-are-synched";
     public static final String CLIENT_REMOTE_FILENAME_SUFFIX_START_SENDING_REQUESTS = "-start-sending-requests";
     public static final String CLIENT_REMOTE_FILENAME_SUFFIX_FINISHED = "-finished";
     
     public static boolean validateIpAddress(String ipAddress) {
         final Pattern IP_PATTERN = Pattern.compile(
         "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
         "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
         "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
         "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
         
         return IP_PATTERN.matcher(ipAddress).matches();
     }
     
     public static boolean isLoopbackIpAddress(String ipAddress) {
         if( ipAddress.startsWith("127.") ) 
             return true;
         return false;
     }
 
     public static boolean validateLocalPort(int port) { 
         if( port <= 0 || port > 65536 ) { 
             return false;
         }
         return true;
     }
     
     public static boolean validateRemotePort(int port) { 
         if( port <= 1024 || port > 65536 ) { 
             return false;
         }
         return true;
     }
     
     public static String generateRandomUUID() { 
         return UUID.randomUUID().toString();
     }
     
     public static String getClientProgramRemoteFilename(Client c) { 
         //return Utils.CLIENT_PROGRAM_REMOTE_BASENAME+"-"+c.getUUID()+".jar";
         return c.getWorkingDirectory()+"/"+Utils.CLIENT_PROGRAM_REMOTE_FILENAME;
     }
     
     public static String getServerProgramRemoteFilename(Server s) { 
         return s.getWorkingDirectory()+"/"+Utils.SERVER_PROGRAM_REMOTE_FILENAME;
     }
     
     public static String getClientLogRemoteFilename(Machine c) {
         return c.getWorkingDirectory()+"/log-"+c.getUUID()+".data";
     }
     
     public static String getClientLocalFilename(Client c, int no_c, int testNum) {
         return "log-client"+no_c+ "-" + testNum + ".data";
     }
     
     public static String getServerLogRemoteFilename(Machine s) {
         return s.getWorkingDirectory()+"/log-"+s.getIpAddress()+"-"+s.getPort()+".data";
     }
     
     public static String getServerLogTopRemoteFilename(Machine s) {
         return s.getWorkingDirectory()+"/log-TOP-"+s.getIpAddress()+"-"+s.getPort()+".data";
     }
     
     public static String getServerLocalFilename(Machine s, int testNum) {
         return "log-server" + testNum + ".data";
     }
     
     public static String getServerLocalTopFilename(Machine s, int testNum) {
         return "log-TOP-server" + testNum + ".data";
     }
     
     public static String getClientRemoteSynchThreadsFilename(Client c) {
         return c.getWorkingDirectory()+"/"+c.getUUID()+Utils.CLIENT_REMOTE_FILENAME_SUFFIX_THREADS_SYNCH;
     }
     
     public static String getClientRemoteStartRequestsFilename(Client c) {
         return c.getWorkingDirectory()+"/"+c.getUUID()+Utils.CLIENT_REMOTE_FILENAME_SUFFIX_START_SENDING_REQUESTS;
     }
     
     public static String getClientRemoteDoneFilename(Client c) {
         return c.getWorkingDirectory()+"/"+c.getUUID()+Utils.CLIENT_REMOTE_FILENAME_SUFFIX_FINISHED;
     }
 
     public static boolean validateServerTypes(String serverType) { 
         if( serverType.equals("xnio3") || 
             serverType.equals("nio2") || 
             serverType.equals("netty") ) {
             return true;
         }
         return false;
     }
     
     public static boolean validateServerMode(String serverMode) { 
         if( serverMode.equals("sync") ||
             serverMode.equals("async") ) {
             return true;
         }
         return false;
     }
     
     /**
      * Get the method name for a depth in call stack. <br />
      * Utility function
      * @param depth depth in the call stack (0 means current method, 1 means call method, ...)
      * @return method name
      */ 
     public static String getMethodName(final int depth) {
         final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
         //System. out.println(ste[ste.length-depth].getClassName()+"#"+ste[ste.length-depth].getMethodName());
         // return ste[ste.length - depth].getMethodName();  //Wrong, fails for depth = 0
         return ste[ste.length - 1 - depth].getMethodName(); //Thank you Tom Tresansky
     }
 }
