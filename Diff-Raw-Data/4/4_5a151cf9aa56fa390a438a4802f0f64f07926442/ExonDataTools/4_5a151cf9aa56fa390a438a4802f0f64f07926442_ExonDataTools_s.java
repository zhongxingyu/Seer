 package edu.ucdenver.ccp.PhenoGen.tools.analysis;
 
 import edu.ucdenver.ccp.PhenoGen.driver.RException;
 import edu.ucdenver.ccp.PhenoGen.driver.R_session;
 import edu.ucdenver.ccp.PhenoGen.data.AsyncUpdateDataset;
 import edu.ucdenver.ccp.PhenoGen.data.Dataset;
 import edu.ucdenver.ccp.PhenoGen.data.User;
 import edu.ucdenver.ccp.PhenoGen.driver.PerlHandler;
 import edu.ucdenver.ccp.PhenoGen.driver.PerlException;
 import edu.ucdenver.ccp.PhenoGen.driver.ExecHandler;
 import edu.ucdenver.ccp.PhenoGen.driver.ExecException;
 import edu.ucdenver.ccp.util.FileHandler;
 import edu.ucdenver.ccp.util.ObjectHandler;
 
 import java.util.GregorianCalendar;
 import java.util.Date;
 
 import javax.servlet.http.HttpSession;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import org.apache.log4j.Logger;
 
 import edu.ucdenver.ccp.PhenoGen.web.mail.*;
 import java.io.File;
 import java.io.FileInputStream;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.Properties;
 
 public class ExonDataTools {
 
     private String[] rErrorMsg = null;
     private R_session myR_session = new R_session();
     //private PerlHandler myPerl_session=null;
     private ExecHandler myExec_session = null;
     private HttpSession session = null;
     private User userLoggedIn = null;
     //private Dataset[] publicDatasets = null;
     //private Dataset selectedDataset = null;
     //private Dataset.DatasetVersion selectedDatasetVersion = null;
     private Connection dbConn = null;
     private Logger log = null;
     private String perlDir = "", fullPath = "";
     private String rFunctDir = "";
     private String userFilesRoot = "";
     private String urlPrefix = "";
     private int validTime=7*24*60*60*1000;
     private String perlEnvVar="";
     private String ucscDir="";
     private String bedDir="";
     private String dbPropertiesFile="";
     private String ensemblDBPropertiesFile="";
 
     public ExonDataTools() {
         log = Logger.getRootLogger();
 
     }
 
     /**
      * Calls the Perl script WriteXML.pl and R script ExonCorrelation.R.
      * @param ensemblID       the ensemblIDs as a comma separated list
      * @param organism        the organism
      * @param RDataFile       the full path to the RDataFile         
      * 
      */
     public void getExonHeatMapData(String ensemblIDList,
             String H5File, String version,
             String organism,
             String datasetName) {
         
         //Setup a String in the format YYYYMMDDHHMM to append to the folder
         Date d = new Date();
         GregorianCalendar gc = new GregorianCalendar();
         gc.setTime(d);
         String formattedDate = formatDate(gc);
         String rOutputPath = "";
         
         
         //EnsemblIDList can be a comma separated list break up the list
         String[] ensemblList = ensemblIDList.split(",");
         String ensemblID1 = ensemblList[0];
         
         if(ensemblID1!=null){
             //Define output directory
             String outputDir = fullPath + "tmpData/" + userLoggedIn.getUser_name() + "/" + ensemblID1 + "_" + formattedDate + "/";
             String folderName = ensemblID1 + "_" + formattedDate;
             String publicPath = H5File.substring(H5File.indexOf("/Datasets/") + 10);
             publicPath = publicPath.substring(0, publicPath.indexOf("/Affy.NormVer.h5"));
 
 
             //need to check and see if files have been previously generated
             String tmpq = "select * "
                     + "from Gen_Exon_Heatmaps "
                     + "where User_ID =" + userLoggedIn.getUser_id()
                     + "and ensembl_id like '" + ensemblIDList + "'"
                     + "and public_Path_Dataset like '" + publicPath + "'"
                     + "and organism like '" + organism + "' order by Created_ON DESC";
             try {
                 PreparedStatement ps = dbConn.prepareStatement(tmpq);
                 ResultSet rs = ps.executeQuery();
                 if (rs.next()) {//point applet to previous files
                     //log.debug("Previous results exist");
                     java.sql.Timestamp ts=rs.getTimestamp("Created_ON");
                     java.sql.Timestamp pastWeek=new java.sql.Timestamp(System.currentTimeMillis()-(validTime));
                     if(ts.before(pastWeek)){//check to see if files are older than the amount of time in validTime
                         /*rs.deleteRow();
                         while(rs.next()){
                             rs.deleteRow();
                             //TODO delete files too
                         }*/
                         //log.debug("Results are older than 1 week generating new");
                         generateFiles(outputDir,organism,rOutputPath,H5File,version,ensemblIDList,folderName,publicPath,datasetName);
                     }else{
                         //log.debug("Results are current using existing.");
                         String folder = rs.getString("Folder_Name");
                         String xmlWebAccess = urlPrefix + "tmpData/" + userLoggedIn.getUser_name() + "/" + folder + "/Gene.xml";
                         String rWebAccess = urlPrefix + "tmpData/" + userLoggedIn.getUser_name() + "/" + folder + "/HeatMap.csv";
 
                         session.setAttribute("exonCorGeneFile", xmlWebAccess);
                         session.setAttribute("exonCorHeatFile", rWebAccess);
                     }
                 } else {//create files
                     //log.debug("No files exist, generating files.");
                     generateFiles(outputDir,organism,rOutputPath,H5File,version,ensemblIDList,folderName,publicPath,datasetName);
                 }
                 rs.close();
             } catch (SQLException e) {
                 log.error("In Exception of running SQL query to look for previous results", e);
                 Email myAdminEmail = new Email();
                 String fullerrmsg=e.getMessage();
                     StackTraceElement[] tmpEx=e.getStackTrace();
                     for(int i=0;i<tmpEx.length;i++){
                         fullerrmsg=fullerrmsg+"\n"+tmpEx[i];
                     }
                 myAdminEmail.setSubject("Exception thrown looking for previous results");
                 myAdminEmail.setContent("There was an error while looking for exon heatmap results in the DB.\n"+fullerrmsg);
                 try {
                     myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
                 } catch (Exception mailException) {
                     log.error("error sending message", mailException);
                     throw new RuntimeException();
                 }
             }
         }else{
             session.setAttribute("exonCorGeneFile", "No Ensembl ID");
             session.setAttribute("exonCorHeatFile", "No Ensembl ID");
         }
     }
 
     public boolean generateFiles(String outputDir,String organism,String rOutputPath,String h5File,String version, String ensemblIDList,String folderName,String publicPath,String datasetName) {
         //log.debug("generate files");
         boolean completedSuccessfully = false;
         //create call to perl to get Gene/Transcript/Probe Data
         // perl writeXML.createXML(outputFilePath,  // ex >/Users/smahaffey/EnsemblePerl/output.xml
         // PSoutputFilePath,  //ex. >/Users/smahaffy/EnsemblPerl/PSoutput.txt
         // organism, //ex. Rat
         //  'core',  //I'm not sure exactly what this means need to clarify with Laura C.
         // EnsemblID)//The ensembl ID for the gene of interest
         //log.debug("before check path exists");
         File outDirF = new File(outputDir);
         //Mkdir if some are missing    
         if (!outDirF.exists()) {
             outDirF.mkdirs();
         }
         
         String tmpq = "select * "
                     + "from datasets "
                     + "where name like '" + datasetName +"' order by dataset_id";
         int dsID=0;
         int arrayTypeID=0;
         try{
             PreparedStatement ps = dbConn.prepareStatement(tmpq);
             ResultSet rs = ps.executeQuery();
             if (rs.next()) {
                 dsID=rs.getInt("DATASET_ID");
                 arrayTypeID=rs.getInt("Array_type_id");
             }
             ps.close();
             log.debug("array type="+arrayTypeID+"\ndataset_id="+dsID);
         }catch(SQLException e){
             log.error("Error getting dataset id",e);
         }
         
         
         Properties myProperties = new Properties();
         File myPropertiesFile = new File(dbPropertiesFile);
         try{
             myProperties.load(new FileInputStream(myPropertiesFile));
         }catch(Exception e){
             log.error("Error Reading Oracle DB Properties",e);
         }
 
             String dsn="dbi:"+myProperties.getProperty("PLATFORM") +":"+myProperties.getProperty("DATABASE");
             String dbUser=myProperties.getProperty("USER");
             String dbPassword=myProperties.getProperty("PASSWORD");
 
             File ensPropertiesFile = new File(ensemblDBPropertiesFile);
             Properties myENSProperties = new Properties();
             try{
                 myENSProperties.load(new FileInputStream(ensPropertiesFile));
             }catch(Exception e){
                 log.error("Error Reading Oracle DB Properties",e);
             }
             String ensHost=myENSProperties.getProperty("HOST");
             String ensPort=myENSProperties.getProperty("PORT");
             String ensUser=myENSProperties.getProperty("USER");
             String ensPassword=myENSProperties.getProperty("PASSWORD");
         
         //construct perl Args
         String[] perlArgs = new String[19];
         perlArgs[0] = "perl";
         perlArgs[1] = perlDir + "writeXML.pl";
         perlArgs[2] = ucscDir;
         perlArgs[3] = outputDir;
         perlArgs[4] = outputDir + "Gene.xml";
         rOutputPath = outputDir + "HeatMap.csv";
         if (organism.equals("Rn")) {
             perlArgs[5] = "Rat";
         } else if (organism.equals("Mm")) {
             perlArgs[5] = "Mouse";
         }
         perlArgs[6] = "Core";
         perlArgs[7] = ensemblIDList;
         perlArgs[8] = userLoggedIn.getUser_name();
         perlArgs[9] = bedDir;
         perlArgs[10] = Integer.toString(dsID);
         perlArgs[11] = Integer.toString(arrayTypeID);
         perlArgs[12]=dsn;
         perlArgs[13]=dbUser;
         perlArgs[14]=dbPassword;
         perlArgs[15]=ensHost;
         perlArgs[16]=ensPort;
         perlArgs[17]=ensUser;
         perlArgs[18]=ensPassword;
         
         //print Perl Args
         /*for (int i = 0; i < perlArgs.length; i++) {
             log.debug(i + "::" + perlArgs[i]);
         }*/
 
 
         //set environment variables so you can access oracle pulled from perlEnvVar session variable which is a comma separated list
         String[] envVar=perlEnvVar.split(",");
         
         for (int i = 0; i < envVar.length; i++) {
             log.debug(i + " EnvVar::" + envVar[i]);
         }
 
 
         //construct ExecHandler which is used instead of Perl Handler because environment variables were needed.
         myExec_session = new ExecHandler(perlDir, perlArgs, envVar, fullPath + "tmpData/" + userLoggedIn.getUser_name() + "/");
 
         try {
 
             myExec_session.runExec();
 
         } catch (ExecException e) {
             log.error("In Exception of run getExonHeatMapData Exec_session", e);
             Email myAdminEmail = new Email();
             myAdminEmail.setSubject("Exception thrown in Exec_session");
             myAdminEmail.setContent("There was an error while running "
                     + perlArgs[1] + " (" + perlArgs[2] +" , "+perlArgs[3]+" , "+perlArgs[4]+" , "+perlArgs[5]+" , "+perlArgs[6]+","+perlArgs[7]+","+perlArgs[8]+","+perlArgs[9]+","+perlArgs[10]+","+perlArgs[11]+
                     ")\n\n"+myExec_session.getErrors());
             try {
                 myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
             } catch (Exception mailException) {
                 log.error("error sending message", mailException);
                 throw new RuntimeException();
             }
         }
         
         if(myExec_session.getExitValue()!=0){
             /*FileHandler myFileHandler=new FileHandler();
             File errorFile=new File(fullPath + "tmpData/" + userLoggedIn.getUser_name() + "/_execErrors.txt");
             File outputFile=new File(fullPath + "tmpData/" + userLoggedIn.getUser_name() + "/_execOut.out");
             String[] errors=myFileHandler.getFileContents(errorFile,"");
             String[] output=myFileHandler.getFileContents(outputFile,"");
             StringBuilder sb=new StringBuilder();
             for(int i=0;i<output.length;i++){
                 sb.append(output[i]+"\n");
             }
             for(int i=0;i<errors.length;i++){
                 sb.append(errors[i]+"\n");
             }*/
             Email myAdminEmail = new Email();
             myAdminEmail.setSubject("Exception thrown in Exec_session");
             myAdminEmail.setContent("There was an error while running "
                     + perlArgs[1] + " (" + perlArgs[2] +" , "+perlArgs[3]+" , "+perlArgs[4]+" , "+perlArgs[5]+" , "+perlArgs[6]+
                     ")\n\n"+myExec_session.getErrors());
             try {
                 myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
             } catch (Exception mailException) {
                 log.error("error sending message", mailException);
                 throw new RuntimeException();
             }
             
             session.setAttribute("exonCorGeneFile", "External Site Unavailable");
             session.setAttribute("exonCorHeatFile", "External Site Unavailable");
         }else{
             String sampleFile=h5File.substring(0,h5File.lastIndexOf("/"))+"/"+version+"_samples.txt";
 
             //construct call to R
             String[] rArgs = new String[5];
             rArgs[0] = h5File;
             rArgs[1] = "'" + version+ "'";
             rArgs[2] = sampleFile+ "'";
             rArgs[3] = "'" + perlArgs[4] + "'";
             rArgs[4] = "'" + rOutputPath + "'";
 
             try {
                 //Call R
                 if ((rErrorMsg = myR_session.callR(this.rFunctDir, "Affymetrix.Exon.HeatMap", rArgs, fullPath + "tmpData/" + userLoggedIn.getUser_name() + "/", -99)) != null) {
                     String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
                     log.debug("after R call for ExonHeatMap, got errorMsg. It is " + errorMsg);
                     throw new RException(errorMsg);
                 } else {
                     completedSuccessfully = true;
                 }
             } catch (RException er) {
                 log.error("In Exception of run getExonHeatMapData R_session", er);
                 Email myAdminEmail = new Email();
                 myAdminEmail.setSubject("Exception thrown in R_session");
                 myAdminEmail.setContent("There was an error while running Affymetrix.Exon.HeatMap.R_HeatMapCorrData("
                         + rArgs[0] + " , " + rArgs[1] + " , " + rArgs[2] + "\n");
                 try {
                     myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
                 } catch (Exception mailException) {
                     log.error("error sending message", mailException);
                     throw new RuntimeException();
                 }
             }
 
             if (completedSuccessfully) {
                 //Define the website address for outputfiles.
                 //TODO Find a way to import begining URL string from a File.
                 String xmlWebAccess = urlPrefix + "tmpData/" + userLoggedIn.getUser_name() + "/" + folderName + "/Gene.xml";
                 String rWebAccess = urlPrefix + "tmpData/" + userLoggedIn.getUser_name() + "/" + folderName + "/HeatMap.csv";
                 //Save Analysis to DB for later review
                 try {
                     String insertQuery =
                             "insert into GEN_EXON_HEATMAPS "
                             + "(User_ID,Ensembl_ID,Folder_Name,"
                             + "Created_On,Public_Path_Dataset,Organism"
                             + ") VALUES "
                             + "(?, ?, ?, ?, ?, ?)";
                     PreparedStatement pstmt = dbConn.prepareStatement(insertQuery,
                             ResultSet.TYPE_SCROLL_INSENSITIVE,
                             ResultSet.CONCUR_UPDATABLE);
                     pstmt.setInt(1, userLoggedIn.getUser_id());
                     pstmt.setString(2, ensemblIDList);
                     pstmt.setString(3, folderName);
                     java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
                     pstmt.setTimestamp(4, now);
                     pstmt.setString(5, publicPath);
                     pstmt.setString(6, organism);
                     pstmt.executeUpdate();
                     pstmt.close();
                     dbConn.commit();
                 } catch (SQLException e) {
                     log.error("In Exception of saving exon heat map results to DB", e);
                     Email myAdminEmail = new Email();
                     String fullerrmsg=e.getMessage();
                     StackTraceElement[] tmpEx=e.getStackTrace();
                     for(int i=0;i<tmpEx.length;i++){
                         fullerrmsg=fullerrmsg+"\n"+tmpEx[i];
                     }
                     myAdminEmail.setSubject("Exception thrown saving Exon results to DB");
                     myAdminEmail.setContent("There was an error while saving exon heatmap results in the DB.\n\n"+fullerrmsg);
                     try {
                         myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
                     } catch (Exception mailException) {
                         log.error("error sending message", mailException);
                         throw new RuntimeException();
                     }
                 }
 
                 //set Session attributes to point to output for the applet
                 session.setAttribute("exonCorGeneFile", xmlWebAccess);
                 session.setAttribute("exonCorHeatFile", rWebAccess);
             } else {
                 //set Session attributes to mark a failure so it won't load the applet
                 session.setAttribute("exonCorGeneFile", "failed");
                 session.setAttribute("exonCorHeatFile", "failed");
             }
         }
         return completedSuccessfully;
     }
     
     public void setSession(HttpSession inSession) {
         this.session = inSession;
         //this.selectedDataset = (Dataset) session.getAttribute("selectedDataset");
         //this.selectedDatasetVersion = (Dataset.DatasetVersion) session.getAttribute("selectedDatasetVersion");
         //this.publicDatasets = (Dataset[]) session.getAttribute("publicDatasets");
         this.userLoggedIn = (User) session.getAttribute("userLoggedIn");
         this.dbConn = (Connection) session.getAttribute("dbConn");
         this.perlDir = (String) session.getAttribute("perlDir") + "scripts/";
         String contextRoot = (String) session.getAttribute("contextRoot");
         String appRoot = (String) session.getAttribute("applicationRoot");
         this.fullPath = appRoot + contextRoot;
         this.rFunctDir = (String) session.getAttribute("rFunctionDir");
         this.userFilesRoot = (String) session.getAttribute("userFilesRoot");
         this.urlPrefix=(String)session.getAttribute("mainURL");
         this.urlPrefix=this.urlPrefix.substring(0, this.urlPrefix.lastIndexOf("/")+1);
         //log.debug("URL"+this.urlPrefix);
         this.perlEnvVar=(String)session.getAttribute("perlEnvVar");
         this.ucscDir=(String)session.getAttribute("ucscDir");
         this.bedDir=(String) session.getAttribute("bedDir");
         this.dbPropertiesFile = (String)session.getAttribute("dbPropertiesFile");
         this.ensemblDBPropertiesFile = (String)session.getAttribute("ensDbPropertiesFile");
     }
 
     public HttpSession getSession() {
         return session;
     }
 
     public String formatDate(GregorianCalendar gc) {
         String ret = "";
         String year = Integer.toString(gc.get(GregorianCalendar.YEAR));
         String month = Integer.toString(gc.get(GregorianCalendar.MONTH) + 1);
         if (month.length() == 1) {
             month = "0" + month;
         }
         String day = Integer.toString(gc.get(GregorianCalendar.DAY_OF_MONTH));
         if (day.length() == 1) {
             day = "0" + day;
         }
         String hour = Integer.toString(gc.get(GregorianCalendar.HOUR_OF_DAY));
         if (hour.length() == 1) {
             hour = "0" + hour;
         }
         String minute = Integer.toString(gc.get(GregorianCalendar.MINUTE));
         if (minute.length() == 1) {
             minute = "0" + minute;
         }
         ret = year + month + day + hour + minute;
         return ret;
     }
 }
