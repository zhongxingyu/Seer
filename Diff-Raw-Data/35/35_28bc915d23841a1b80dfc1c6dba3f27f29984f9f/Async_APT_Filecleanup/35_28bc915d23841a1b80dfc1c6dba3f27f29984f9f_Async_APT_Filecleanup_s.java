 package edu.ucdenver.ccp.util;
 
 
 import javax.mail.MessagingException;
 import edu.ucdenver.ccp.PhenoGen.web.mail.Email;
 /* for handling exceptions in Threads */
 import au.com.forward.threads.ThreadReturn;
 import au.com.forward.threads.ThreadInterruptedException;
 import au.com.forward.threads.ThreadException;
 
 import edu.ucdenver.ccp.util.FileHandler;
 import edu.ucdenver.ccp.PhenoGen.data.Dataset;
 
 /* for logging messages */
 import java.io.File;
 import java.io.IOException;
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 
 
 public class Async_APT_Filecleanup implements Runnable{
 
     private Logger log = null;
     private Thread waitThread = null;
     private String outputDir = "";
     private Dataset selectedDataset;
     private Dataset.DatasetVersion newDatasetVersion;
     private String userFileRoot="";
     private Connection dbConn=null;
     
 
    public Async_APT_Filecleanup(Dataset selectedDataset,Dataset.DatasetVersion dsVer,String userFileRoot,Connection dbConn, Thread waitThread) {
         log = Logger.getRootLogger();
         this.waitThread = waitThread;
         this.selectedDataset=selectedDataset;
         this.newDatasetVersion=dsVer;
         this.userFileRoot=userFileRoot;
         this.outputDir = selectedDataset.getPath()+"v"+dsVer.getVersion();
         this.dbConn=dbConn;
         
     }
      
     
 
     public void run() throws RuntimeException {
         FileHandler myFileHandler=new FileHandler();
         Thread thisThread = Thread.currentThread();
         log.debug("Starting run method of Async_APT_Filecleanup. , thisThread = " + thisThread.getName());
         String[] rErrorMsg = null;       
         try {
             //
             // If this thread is interrupted, throw an Exception
             //
             ThreadReturn.ifInterruptedStop();
 
             // 
             // If waitThread threw an exception, then ThreadReturn will
             // detect it and throw the same exception 
             // Otherwise, the join will happen, and will continue to the 
             // next statement.
             //
             if (waitThread != null) {
                 //while (waitThread.isAlive()) {
                 log.debug("in Async_APT_Filecleanup waiting on thread " + waitThread.getName());
                 ThreadReturn.join(waitThread);
                 log.debug("just finished waiting on thread " + waitThread.getName());
             } else {
                 log.debug("waitThread is null");
             }
             log.debug("after waiting on thread");
             
             
             String tmpOracleDir=selectedDataset.getPath()+"oracle/";
            String tmpOracleDest=userFileRoot+"OracleExternalFiles/";
             ArrayList<File> fileList=new ArrayList<File>();
             char endChar='a';
             String oracleFileSufix=".a";
             String dabgFilePrefix=tmpOracleDir+"dabgdata_"+selectedDataset.getDataset_id()+"_"+newDatasetVersion.getVersion()+oracleFileSufix;
             String avgFilePrefix=tmpOracleDir+"normdata_"+selectedDataset.getDataset_id()+"_"+newDatasetVersion.getVersion()+oracleFileSufix;
             boolean lastFile=false;
             while(!lastFile){
                 String curfilestr=dabgFilePrefix+endChar;
                 File dSrcFile=new File(curfilestr);
                 if(dSrcFile.exists()){
                     String avgFilestr=avgFilePrefix+endChar;
                     String dDestFilestr=tmpOracleDest+"dabgdata_"+selectedDataset.getDataset_id()+"_"+newDatasetVersion.getVersion()+oracleFileSufix+endChar;
                     String aDestFilestr=tmpOracleDest+"normdata_"+selectedDataset.getDataset_id()+"_"+newDatasetVersion.getVersion()+oracleFileSufix+endChar;
                     File dDestFile=new File(dDestFilestr);
                     File aSrcFile=new File(avgFilestr);
                     File aDestFile=new File(aDestFilestr);
                     try{
                         myFileHandler.copyFile(dSrcFile,dDestFile);
                         myFileHandler.copyFile(aSrcFile,aDestFile);
                     }catch(IOException e){
                         log.error("Error Copying Data files for External Tables",e);
                     }
                     endChar++;
                 }else{
                     lastFile=true;
                 }
             }
             String headerFilestr=tmpOracleDir+"normheader_"+selectedDataset.getDataset_id()+"_"+newDatasetVersion.getVersion()+".txt";
             String destHeaderstr=tmpOracleDest+"normheader_"+selectedDataset.getDataset_id()+"_"+newDatasetVersion.getVersion()+".txt";
             File header=new File(headerFilestr);
             File destheader=new File(destHeaderstr);
             if(header.exists()){
                 try{
                     myFileHandler.copyFile(header,destheader);
                 }catch(IOException e){
                     log.error("Error Copying Header file for External Tables",e);
                 }
             }
             headerFilestr=tmpOracleDir+"dabgheader_"+selectedDataset.getDataset_id()+"_"+newDatasetVersion.getVersion()+".txt";
             destHeaderstr=tmpOracleDest+"dabgheader_"+selectedDataset.getDataset_id()+"_"+newDatasetVersion.getVersion()+".txt";
             header=new File(headerFilestr);
             destheader=new File(destHeaderstr);
             if(header.exists()){
                 try{
                     myFileHandler.copyFile(header,destheader);
                 }catch(IOException e){
                     log.error("Error Copying Header file for External Tables",e);
                 }
             }
 
             //Call filter prep
             String filterprep="{call filterprep.combinedprep(" + newDatasetVersion.getDataset().getDataset_id() + "," + newDatasetVersion.getVersion() + ")}";
             CallableStatement cs = dbConn.prepareCall(filterprep);
             cs.execute();
             cs.close();
             
             //clean up v# files
             log.debug("Deleting:"+outputDir);
             File dir=new File(outputDir);
             File[] list=dir.listFiles();
             for(int i=0;i<list.length;i++){
                 delete(list[i]);
             }
             dir.delete();
             
             //clean up /oracle files
             dir=new File(selectedDataset.getPath()+"oracle");
             list=dir.listFiles();
             for(int i=0;i<list.length;i++){
                 delete(list[i]);
             }
             dir.delete();
             //
             // If this thread is interrupted, throw an Exception
             //
             ThreadReturn.ifInterruptedStop();
 
         } catch (Throwable t) {
             //log.error("In exception of Async_HDF5_FileHandler for thread " + thisThread.getName(), t);
             
             if (t instanceof ThreadException) {
                 //log.error("throwable exception is a ThreadException. cause = " + t.getCause().getMessage(),t);
                 //
                 // This saves the Throwable object so an Exception can be detected
                 // by the calling program
                 //
                 ThreadReturn.save(t);
             } else if (t instanceof ThreadInterruptedException) {
                 //log.error("throwable exception is a ThreadInterruptedException",t);
                 //ThreadReturn.save(t);
                 ThreadReturn.save(new Throwable("ThreadInterruptedException in Async_HDF5_FileHandler", t));
             } else {
                 //log.error("exception caused in Async_HDF5_FileHandler",t);
                 ThreadReturn.save(t);
                 //ThreadReturn.save(new Throwable("Error in R process while running " + functionName + "\n" + rError, t));
             }
 
             Email myAdminEmail = new Email();
             myAdminEmail.setSubject("Exception thrown in Async_HDF5_FileHandler.");
             myAdminEmail.setContent("Cause = " + (t.getCause() != null ? t.getCause().getMessage() : t.getMessage())
                     + "\n Message = " + t.getMessage()
                     + "\n Thread name is " + thisThread.getName());
             try {
                 //log.debug("sending message to notify administrator of problem");
                 myAdminEmail.sendEmailToAdministrator("");
             } catch (MessagingException e) {
                 //log.error("error sending message to administrator",e);
                 throw new RuntimeException();
                 //ThreadReturn.save(new Throwable("Error sending message to administrator", e));
             }
 
         }
         //log.debug("done with Async_HDF5_FileHandler run method for this thread: " + thisThread.getName());
 
     }
     
     private void delete(File file){
         if(file.isDirectory()){
             File[] tmplist=file.listFiles();
             for(int i=0;i<tmplist.length;i++){
                 delete(tmplist[i]);
             }
         }
         file.delete();
     }
 }
