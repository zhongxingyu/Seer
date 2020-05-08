 /*
  * Launcher.java
  *
  * Created on April 19, 2004, 1:59 PM
  */
 
 package goDiet.Utils;
 
 import goDiet.Model.*;
 
 import java.io.*;
 import java.util.*;
 import java.text.SimpleDateFormat;
 
 /**
  *
  * @author  hdail
  */
 public class Launcher {
     
     /** Creates a new instance of Launcher */
     public Launcher() {
     }
     
     public String createLocalScratch(String scratchBase,int debugLevel) {
         if(debugLevel >= 1){
            System.out.println("Preparing local scratch directory in " + scratchBase);
         }
         String dirName = null;
         String runLabel = null;
         
         SimpleDateFormat formatter = new SimpleDateFormat("yyMMMdd_HHmm");
         java.util.Date today = new Date();
         String dateString = formatter.format(today);
          
         runLabel = "run_" + dateString;
         
         File dirHdl = new File(scratchBase,runLabel);
         if( dirHdl.exists() ) {
             int i = 0;
             do {
                 i++;
                 dirHdl = new File(scratchBase,runLabel + "_r" + i);
             } while (dirHdl.exists());
             runLabel += "_r" + i;
         }
         dirHdl.mkdirs();
         return runLabel;
     }
     
     /* launchElement is the primary method for launching components of the DIET
      * hierarchy.  This method performs the following actions:
      *      - check that element, compRes, & scratch base are non-null
      *      - create the config file locally
      *      - stage the config file to remote host
      *      - run the element on the remote host
      */
     public void launchElement(Elements element,
                               ComputeResource compRes,
                               String localScratchBase,
                               String runLabel,
                               boolean useLogService,
                               RunConfig runConfig){
         if(element == null){
             System.err.println("Launcher.launchElement called with null element.\n" +
                 "Launch request ignored.");
             return;
         }
         if(compRes == null){
             System.err.println("Launcher.launchElement called with null resource.\n" +
                 "Launch request ignored.");
             return;
         }
         if(localScratchBase == null){
             System.err.println("launchElement: Scratch space is not ready.  Need to run createLocalScratch.");
             return;
         }
         if(runLabel == null){
             System.err.println("launchElement: RunLabel undefined.\n");
             return;
         }
         if(runConfig.debugLevel >= 1){
             System.out.println("\n** Launching element " + element.getName() +
                 " on " + compRes.getName());
         } 
         try {
             createCfgFile(element,localScratchBase + "/" + runLabel,
                     useLogService,runConfig);
         }
         catch (IOException x) {
             System.err.println("Exception writing cfg file for " + element.getName());
             System.err.println("Exception: " + x);
             System.err.println("Exiting");
             System.exit(1);
         }
         StorageResource storeRes = compRes.getStorageResource();
         stageFile(localScratchBase,runLabel,element.getCfgFileName(),
             storeRes,runConfig);
         runElement(element,compRes,runConfig);
     }
     
     // TODO: incorporate Elagi usage
     private void stageFile(String localBase, String runLabel, 
                            String filename,StorageResource storeRes,
                            RunConfig runConfig) {
         if(runConfig.debugLevel >= 1){
            System.out.println("Staging file " + filename + " to " + storeRes.getName());
         }
         //AccessMethod access = storeRes.getAccessMethod("scp");
         
         SshUtils sshUtil = new SshUtils();
         sshUtil.stageWithScp(localBase,runLabel,filename,storeRes,runConfig);
     }
     
     // TODO: incorporate Elagi usage
     private void runElement(Elements element,ComputeResource compRes,
             RunConfig runConfig) {
         StorageResource storage = compRes.getStorageResource();
         String scratch = storage.getScratchBase() + "/" + storage.getRunLabel();
         if(runConfig.debugLevel >= 1){
            System.out.println("Executing element " + element.getName() + 
                 " on resource " + compRes.getName());
         }
         AccessMethod access = compRes.getAccessMethod("ssh");
         if(access == null){
             System.err.println("runElement: compRes does not have ssh access " +
                 "method. Ignoring launch request");
             return;
         }
         
         SshUtils sshUtil = new SshUtils();
         sshUtil.runWithSsh(element,compRes,runConfig);
     }
     
     private void createCfgFile(Elements element,
                                String localScratch,
                                boolean useLogService,
                                RunConfig runConfig) throws IOException {
         if( element.getName().compareTo("TestTool") == 0){
             return;
         }
         
         if( element.getCfgFileName() == null){
             element.setCfgFileName(element.getName() + ".cfg");
         }
         String fileName = element.getCfgFileName();
         
         if(runConfig.debugLevel >= 1){
             System.out.println("Writing config file " + fileName);
         }
         
         File cfgFile = new File(localScratch, fileName);
         if( cfgFile.exists() ) {
             int i = 0;
             do {
                 i++;
                 fileName = element.getCfgFileName() + "_" + i;
                 cfgFile = new File(localScratch, fileName);
             } while (cfgFile.exists());
             element.setCfgFileName(fileName);
         }
 
         try {
             cfgFile.createNewFile();
             FileWriter out = new FileWriter(cfgFile);      
             
             if( element.getName().compareTo("LogCentral") == 0){
                 writeCfgFileLogCentral(element,out);
             } else if( element.getName().compareTo("OmniNames") == 0){
                 writeCfgFileOmniNames(element,out);
             } else {
                 writeCfgFileDiet(element,out,useLogService);
             }
             out.close();
         }
         catch (IOException x) {
             System.err.println("Failed to write " + cfgFile.getPath());
             throw x;
         }
         //System.out.println("Successfully wrote " + cfgFile.getPath());
     }  
     
     private void writeCfgFileLogCentral(Elements element,FileWriter out) throws IOException {
         out.write("[General]\n\n");
         out.write("[DynamicTagList]\n");
         out.write("[StaticTagList]\n");
         out.write("[UniqueTagList]\n");
         out.write("[VolatileTagList]\n");
     }
     private void writeCfgFileOmniNames(Elements element,FileWriter out) throws IOException {
         if(element.isPortSet()){
             out.write("InitRef = NameService=corbaname::localhost:" + element.getPort() + "\n");
         } else {
             out.write("InitRef = NameService=corbaname::localhost\n");
         }
         out.write("giopMaxMsgSize = 33554432\n");
         out.write("supportBootstrapAgent = 1\n");        
     }
     private void writeCfgFileDiet(Elements element,FileWriter out,
             boolean useLogService) throws IOException {
         String className = element.getClass().getName();
         if( className.compareTo("goDiet.Model.MasterAgent") == 0) {
            out.write("name = " + element.getName() + "\n");
             out.write("agentType = DIET_MASTER_AGENT\n");
         } else if( className.compareTo("goDiet.Model.LocalAgent") == 0) {
            out.write("name = " + element.getName() + "\n");
             out.write("agentType = DIET_LOCAL_AGENT\n");
             LocalAgent agent = (LocalAgent)element;
             out.write("parentName = " + (agent.getParent()).getName() + "\n");
         } else if(className.compareTo("goDiet.Model.ServerDaemon") == 0){
             ServerDaemon sed = (ServerDaemon)element;
             out.write("parentName = " + (sed.getParent()).getName() + "\n");
         }
 
         if(element.isTraceLevelSet()) {
             out.write("traceLevel = " + element.getTraceLevel() + "\n");
         }
         /* TODO: support retrieval from XML */
         //out.write("endPoint = \n");
         out.write("fastUse = 0\n");
         out.write("ldapUse = 0\n");
         out.write("nwsUse = 0\n");
         if(useLogService){ 
             out.write("useLogService = 1\n");
         } else {
             out.write("useLogService = 0\n");
         }
         // TODO: are the following 2 even used by DIET?
         out.write("lsOutbuffersize = 0\n");
         out.write("lsFlushinterval = 10000\n");        
     }
 }
