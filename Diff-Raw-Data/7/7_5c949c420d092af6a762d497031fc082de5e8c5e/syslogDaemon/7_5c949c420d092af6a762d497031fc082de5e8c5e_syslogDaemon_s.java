 import com.sleepycat.db.*;
 
 
 import java.net.DatagramSocket;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.util.Date;
 import java.io.File;
 
 /*
  * This file is part of syslogUnity.
  *
  *     syslogUnity is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     syslogUnity is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with syslogUnity.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 public class syslogDaemon {
 
     public static void main(String[] args) throws Exception {
         new syslogDaemon().getEntries();
     }
 
     private void getEntries() throws Exception {
         final File dbEnvDir = new File("/var/lib/syslogUnity/queueDB/");
         int BUFFER_SIZE = 1024;
 
         DatagramSocket syslog = new DatagramSocket(514);
         DatagramPacket logEntry = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
 
         EnvironmentConfig envConfig = new EnvironmentConfig();
         envConfig.setAllowCreate(true);
         envConfig.setCacheSize(20971520);
         envConfig.setInitializeCache(true);
         envConfig.setInitializeCDB(true);
         final Environment env = new Environment(dbEnvDir, envConfig);
 
         DatabaseConfig config = new DatabaseConfig();
         config.setType(DatabaseType.RECNO);
         config.setReadUncommitted(true);
         config.setAllowCreate(true);
         config.setPageSize(4096);
         final Database queue = env.openDatabase(null, "logQueue.db", "logQueue", config);
 
         CursorConfig curConfig = new CursorConfig();
         curConfig.setReadUncommitted(true);
         curConfig.setWriteCursor(true);
         final Cursor cursor = queue.openCursor(null, curConfig);
 
         Runtime.getRuntime().addShutdownHook(new Thread() {
             public void run() {
                 try {
                     cursor.close();
                     queue.close();
                     env.close();
                     System.out.print("Closed DB Gracefully...\n");
                 } catch (Exception dbe) {
                     System.out.print("Caught SIGINT, couldn't close queueDB successfully\n");
                 }
             }
         });
 
         while (true) {
             syslog.receive(logEntry);
             InetAddress logHost = logEntry.getAddress();
             String logLine = new String(logEntry.getData(), 0, logEntry.getLength());
             doStuff(logLine, logHost, cursor);
         }
     }
 
     void doStuff(String logLine, InetAddress logHost, Cursor cursor) {
         Date logDate = new Date();
         String logPriority = "";
         int i;
         for (i = 0; i <= logLine.length(); i++) {
             if (logLine.charAt(i) == '>') break;
             if (logLine.charAt(i) != '<') logPriority = logPriority + logLine.charAt(i);
         }
         String logData = logLine.substring(i + 1, logLine.length());
         String dateEpoch = Long.toString(logDate.getTime());
         String insertRecord = dateEpoch.concat("##FD##").
                 concat(logPriority).concat("##FD##").
                 concat(logHost.getHostAddress()).concat("##FD##").
                 concat(logData);
 
         byte[] k = new byte[4];
         byte[] d = insertRecord.getBytes();
 
         DatabaseEntry kdbt = new DatabaseEntry(k);
         kdbt.setSize(4);
         DatabaseEntry ddbt = new DatabaseEntry(d, 0, d.length);
         ddbt.setSize(d.length);
 
         try {
            cursor.put(kdbt, ddbt);
         } catch (Exception dbe) {
             System.out.print("Couldn't add record to database\n");
         }
     }
 }
 
