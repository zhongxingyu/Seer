 import java.util.*;
 import java.io.*;
 import java.net.*;
 import java.rmi.*;
 import java.rmi.registry.*;
 // This is a class to handle any administrative duties
 // and system-level user interaction
 
 public class Monitor {
     private static MonitorThread monitorThread;
     private static volatile boolean shouldMonitor;
 
     public static void main(String[] args)
     {
         if (checkConfig())
         {
             switch (args[0]){
                 case "startMaster":
                 {
                     try{
                         MasterServer server = new MasterServer();
                         server.start();
                     } catch (Exception e)
                     {
                         e.printStackTrace();
                     }
                     break;
                 }
                 case "startSystem":
                 {
                     generateScripts();
                     break;
                 }
                 case "stopSystem":
                 {
                     System.out.println("Stopping system");
                     stopSystem();
                     break;
                 }
                 case "startTerminal":
                 {
                     runTerminal();
                     break;
                 }
                 case "info":
                 {
                     System.out.println(monitor());
                     break;
                 }
             }
         } else
             System.out.println("Invalid config file\nDelete or Fix");
         return;
     }
     
     private static void runTerminal()
     {
         BufferedReader stdin = null;
         try{
             MasterFileServerInterface masterServer = getMaster();
             stdin = new BufferedReader(new InputStreamReader(System.in));
             String helpString = "\nstart: begin a mapreduce job\n" +
                                 "add: add a file to the DFS\n" +
                                 "nodes: view info about all nodes\n" +
                                 "files: view a list of all files\n" +
                                 "monitor: view a comprehensive breakdown of files\n"+
                                 "quit: exit this terminal\n";
             System.out.println("start, add, monitor, nodes, files, or quit");
             boolean isRunning = true;
             while(isRunning) {
                 System.out.print("[MapReduce]> ");
                 String input = stdin.readLine();
                 String[] args = input.split(" ");
 
                 if (args[0].equals("start")) {
                     startNewJob(input);
                 } else if (input.equals("quit") || input.equals("exit")) {
                     isRunning = false;
                 } else if (input.equals("monitor")) {
                     System.out.println(monitor());
                 } else if (input.equals("help")) {
                     System.out.println(helpString);
                 } else if (input.equals("files")) {
                     System.out.println(files());
                 } else if (input.equals("nodes")) {
                     System.out.println(nodes());
                 } else if (args[0].equals("add")) {
                     if (args.length < 2) {
                         System.out.println("Format: add [filename]");
                         continue;
                     }
                     String filename = args[1];
                     File testFile = new File(filename);
                     if (!testFile.exists())
                     {
                         System.out.println("File " + filename + " cannot be found.");
                     } else {
                         addNewFile(filename);
                     }
 
                 } else {
                     System.out.println("Unrecognized command. Type help for info");
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private static void startNewJob(String input)
     {
        MasterFileServerInterface master = getMaster();
         String[] args = input.split(" ");
         if (args.length < 4) {
             System.out.println("Format: start [jobclass]([arg0, arg1]) [outputfile] [inputfiles]");
             return;
         }
         try{
            if (master != null) {
                 //Starting a new job
                 String jobName = args[1];
                 Job j = (Job) Class.forName(jobName).newInstance();
 
                 j.setOutput(args[2]);
                 List<String> inputFiles = new ArrayList<String>();
                                     
                 for (int i = 3; i < args.length; i++) {
                     inputFiles.add(args[i]);
                 }
 
                 j.setInput(inputFiles);
                                     
                master.newJob(j);
                 System.out.println(jobName + " added");
             } else {
                 System.out.println("Master could not be reached");
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
     private static void addNewFile(String filename)
     {
         MasterFileServerInterface master = getMaster();
         try {
             File f = new File(filename);
             FileIO.upload(master, f, f);
             // host is null, because it is now local to master
             master.addNewFile(filename, null);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private static MasterFileServerInterface getMaster()
     {
         String registryHost = null;
         int registryPort = 0;
         String masterServerRegistryKey = null;
         MasterFileServerInterface master = null;
         Registry rmiRegistry = null;
 
         Properties prop = Config.getProp();
         try{
             registryHost = prop.getProperty("REGISTRY_HOST");
             registryPort = Integer.parseInt(prop.getProperty("REGISTRY_PORT"));
             masterServerRegistryKey = 
                 prop.getProperty("MASTER_SERVER_REGISTRY_KEY");
             rmiRegistry = 
                 LocateRegistry.getRegistry(registryHost, registryPort);
             master = (MasterFileServerInterface)
                 rmiRegistry.lookup(masterServerRegistryKey);
             return master;
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
 
     }
 
     private static void stopSystem()
     {
         MasterFileServerInterface master = getMaster();
         try {
             if (master != null)
                 master.stop();
         } catch (RemoteException e) {
         }
     }
 
     private static String monitor()
     {
         MasterFileServerInterface master = getMaster();
         try {
             if (master != null)
                 return master.monitorAll();
             else
                 return null;
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
     private static String files()
     {
         MasterFileServerInterface master = getMaster();
         try {
             if (master != null)
                 return master.monitorFiles();
             else
                 return null;
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
     private static String nodes()
     {
         MasterFileServerInterface master = getMaster();
         try {
             if (master != null)
                 return master.monitorNodes();
             else
                 return null;
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
     private static boolean checkConfig()
     {
         Properties prop = new Properties();
         try {
             prop.load(new FileInputStream(Config.configFileName));
         } catch (FileNotFoundException e) {
             Config.generateConfigFile();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         if (! Config.checkConfigFile())
         {
             return false;
         }
         return true;
     }
 
     private static void generateScripts()
     {
         ArrayList<String> addresses = Config.getNodeAddresses();
         Iterator<String> iter = addresses.iterator();
         String nodeFormatString = Config.getNodeSSHFormatString();
         String masterFormatString = Config.getMasterSSHFormatString();
         String monitorFormatString = Config.getMonitorFormatString();
 
         // Master script
         try{
             File scriptFile = new File("scripts/masterScript.sh");
             PrintWriter pw = new PrintWriter(scriptFile);
             pw.println("#!/bin/bash");
             System.out.println("format string is " + masterFormatString);
             pw.format(masterFormatString, Config.getMasterAddress());
             pw.close();
 
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         // Monitor script
         try{
             File scriptFile = new File("scripts/masterScript.sh");
             PrintWriter pw = new PrintWriter(scriptFile);
             pw.println("#!/bin/bash");
             System.out.println("format string is " + masterFormatString);
             pw.format(masterFormatString, Config.getMasterAddress());
             pw.close();
 
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         int i = 0;
         // Node scripts
         while (iter.hasNext()){
             String eachAddress = iter.next();
             try{
                 File scriptFile = new File("scripts/nodeScript" + i + ".sh");
                 PrintWriter pw = new PrintWriter(scriptFile);
                 pw.println("#!/bin/bash");
                 pw.format(nodeFormatString, eachAddress);
                 pw.close();
             } catch (Exception e)
             {
                 e.printStackTrace();
             }
             i++;
         }
     }
     
 }
