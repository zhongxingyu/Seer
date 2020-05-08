 package edu.ucdenver.ccp.PhenoGen.tools.analysis;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import java.util.Hashtable;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 
 import javax.servlet.http.HttpSession;
 
 import edu.ucdenver.ccp.util.FileHandler;
 
 import edu.ucdenver.ccp.PhenoGen.data.AsyncUpdateDataset;
 import edu.ucdenver.ccp.PhenoGen.data.AsyncUpdateStatsDataset;
 import edu.ucdenver.ccp.PhenoGen.data.Dataset;
 import edu.ucdenver.ccp.PhenoGen.data.ParameterValue;
 import edu.ucdenver.ccp.PhenoGen.data.User;
 import edu.ucdenver.ccp.PhenoGen.data.DSFilterStat;
 import edu.ucdenver.ccp.PhenoGen.driver.Async_R_Session;
 import edu.ucdenver.ccp.PhenoGen.driver.RException;
 import edu.ucdenver.ccp.PhenoGen.driver.R_session;
 
 import edu.ucdenver.ccp.util.ObjectHandler;
 import edu.ucdenver.ccp.util.Debugger;
 import edu.ucdenver.ccp.util.Async_HDF5_FileHandler;
 import edu.ucdenver.ccp.util.Async_APT_Filecleanup;
 
 import edu.ucdenver.ccp.PhenoGen.web.ErrorException;
 import edu.ucdenver.ccp.util.HDF5.PhenoGen_HDF5_File;
 import java.sql.*;
 import java.util.Date;
 import java.util.logging.Level;
 
 /* for logging messages */
 import org.apache.log4j.Logger;
 
 /**
  * Class for handling calls to 'R'.
  *  @author  Cheryl Hornbaker
  */
 
 public class Statistic {
 	private String rFunctionDir;
 	String [] rErrorMsg = null;
 	String [] functionArgs = null;
 	String rFunction = "";
 	private HttpSession session;
 	private Dataset selectedDataset; 
 	private Dataset.DatasetVersion selectedDatasetVersion; 
 	private Dataset[] publicDatasets; 
 	private User userLoggedIn; 
 	private String userFilesRoot; 
 	//private String dbPropertiesFile;
 	//private String mainURL;
 	private Connection dbConn;
 
 	private Logger log = null;
 	private Debugger myDebugger = new Debugger();
 	private R_session myR_session = new R_session();
 	private FileHandler myFileHandler = new FileHandler();
 
         public Statistic() {
                 log = Logger.getRootLogger();
         }
 
 	public void setRFunctionDir(String inString) {
 		this.rFunctionDir = inString;
 	}
 
 	public String getRFunctionDir() {
 		return rFunctionDir; 
 	}
 
 	public void setUserFilesRoot(String inString) {
 		this.userFilesRoot = inString;
 	}
 
 	public String getUserFilesRoot() {
 		return userFilesRoot; 
 	}
 
 	public void setUserLoggedIn(User inUser) {
 		this.userLoggedIn = inUser;
 	}
 
 	public User getUserLoggedIn() {
 		return userLoggedIn; 
 	}
 
 	public void setSelectedDataset(Dataset inDataset) {
 		this.selectedDataset = inDataset;
 	}
 
 	public Dataset getSelectedDataset() {
 		return selectedDataset; 
 	}
 
 	public Dataset.DatasetVersion getSelectedDatasetVersion() {
 		return selectedDatasetVersion; 
 	}
 
 	public void setSelectedDatasetVersion(Dataset.DatasetVersion inDatasetVersion) {
 		this.selectedDatasetVersion = inDatasetVersion;
 	}
 
 
 	public void setSession(HttpSession inSession) {
 		this.session = inSession;
 	        this.selectedDataset = (Dataset) session.getAttribute("selectedDataset");
 	        this.selectedDatasetVersion = (Dataset.DatasetVersion) session.getAttribute("selectedDatasetVersion");
 	        this.publicDatasets = (Dataset[]) session.getAttribute("publicDatasets");
 	        this.userLoggedIn = (User) session.getAttribute("userLoggedIn");
 	        this.userFilesRoot = (String) session.getAttribute("userFilesRoot");
 		// This is here in case a Thread loses the database connection, it can re-connect
 	        //this.dbPropertiesFile = (String) session.getAttribute("dbPropertiesFile");
         	this.dbConn = (Connection) session.getAttribute("dbConn");
         	//this.mainURL = (String) session.getAttribute("mainURL");
 	}
 
 	public HttpSession getSession() {
 		return session; 
 	}
 
         /**
          * Calls the filter.genes function.
          * @param platform       the platform
          * @param inputFile	the input file
          * @param filterMethodName       the name of the filter method
          * @param parameter1       the value of parameter 1
          * @param parameter2       the value of parameter 2
          * @param parameter3       the value of parameter 3
          * @param analysisPath       the directory where the analysis files are stored
          * @param geneCountFile	the gene count file
          * @param outputFileName	the name of the output file
          * @param firstInputFile	the first input file
 	 * @throws RException if an error occurs when running R
          */
 
 	 public void callFilterGenes(String platform,
             Dataset ds,
             String version,
             File inputFile,
             String filterMethodName,
             String parameter1,
             String parameter2,
             String parameter3,
             String analysisPath,
             File geneCountFile,
             String outputFileName,
             String abnormalHDF5File,
             int phenotypeParamGroupID,
             int paramGroupID,
             File firstInputFile) throws RException {
             log.debug("filtermethod:" + filterMethodName + "\np1:" + parameter1 + "\np2:" + parameter2 + "\np3:" + parameter3);
             if (new edu.ucdenver.ccp.PhenoGen.data.Array().EXON_ARRAY_TYPES.contains(ds.getArray_type())) {
                 Date startTimer=new Date();
                 int v = Integer.parseInt(version.substring(1));
                 User userLoggedIn = (User) session.getAttribute("userLoggedIn");
                 int userID = userLoggedIn.getUser_id();
                 int dsID = ds.getDataset_id();
                 String verFDate=(String)session.getAttribute("verFilterDate");
                 String verFTime=(String)session.getAttribute("verFilterTime");
                 Dataset.DatasetVersion dsVer=ds.getDatasetVersion(v);
                 DSFilterStat dsfs=dsVer.getFilterStat(verFDate,verFTime,userID,dbConn);
                 String Parameter="";
                 String resetQ = "{call filter.reset(" + dsID + "," + v + "," + userID + ",?)}";
                 String countQ = "Select count(*) from EXON_USER_FILTER_TEMP where dataset_id="+dsID+" and dataset_version="+v+" and user_id="+userID+" and cumulative_filter=1";
                 /*if (true) {//if first time need to fill
                     try {
                         CallableStatement cs = dbConn.prepareCall(resetQ);
                         cs.setInt(1, 1);
                         //log.debug("PS:"+ps.toString());
                         cs.execute();
                         cs.close();
                     } catch (SQLException e) {
                         log.error("Filter SQL Exception:filter.reset(1):", e);
                     }
                 }*/
                 try {
                         CallableStatement cs = dbConn.prepareCall(resetQ);
                         cs.setInt(1, 4);
                         //log.debug("PS:"+ps.toString());
                         cs.execute();
                         cs.close();
                 } catch (SQLException e) {
                         log.error("Filter SQL Exception:filter.reset(4):", e);
                 }
                 log.debug("exon array:"+filterMethodName+"::");
                 if (filterMethodName.equals("'affy.control.genes'")) {
                     try {
                         String query = "{call filter.affycontrol(" + dsID + "," + v + "," + userID + ") }";
                         log.debug("Affy Control filter call:"+query);
                         CallableStatement cs = dbConn.prepareCall(query);
                         cs.execute();
                         cs.close();
                     } catch (SQLException e) {
                         log.error("Filter SQL Exception:", e);
                     }
                 } else if (filterMethodName.equals("'absolute.call.filter'")) {
                     try {
                         //absolutecall(dsID,version,userID,# samples group1, #samples group2, % of samples )
                         //#samples group 1 & 2 are used only when 2 groups
                         //% of samples user when >2 groups
                         String keepRemove=parameter2;
                         if(parameter2.equals("1")){
                             Parameter="Keep ";
                         }else{
                             Parameter="Remove ";
                         }
                         String value=parameter1.substring(2,parameter1.length()-1);
                         String query = "{call filter.absolutecall(" + dsID + "," + v + "," + userID + ","+keepRemove+",";
                         if(value.contains(",")){
                             String[] values=value.split(",");
                             query=query+values[0]+", "+values[1]+", -1";
                             int v1=Integer.parseInt(values[0]);
                             int v2=Integer.parseInt(values[1]);
                             String pa1="present";
                             String pa2="present";
                             if(v1<0){
                                 pa1="absent";
                                 v1=Math.abs(v1);
                             }
                             if(v2<0){
                                 pa2="absent";
                                 v2=Math.abs(v2);
                             }
                             Parameter=Parameter+"Group1 "+pa1+" >= "+v1+"  Group2 "+pa2+" >= "+v2;
                         }else{
                             double tmp=Double.parseDouble(value);
                             query=query+"-1, -1, "+tmp*100;
                             String pa="present";
                             double tmp2=Math.abs(tmp);
                             if(tmp<0){
                                 pa="absent";
                             }
                             Parameter=Parameter+" "+pa+" >="+tmp2*100+"%";
                         }
                         query=query+") }";
                         log.debug("DABG filter call:"+query);
                         CallableStatement cs = dbConn.prepareCall(query);
                         cs.execute();
                         cs.close();
                     } catch (SQLException e) {
                         log.error("Filter SQL Exception:", e);
                     }
                 }else if (filterMethodName.equals("'heritability'")) {
                     try {
                         String query = "{call filter.heritability(" + dsID + "," + v + "," + userID +","+parameter2+",'"+parameter1+"') }";
                         log.debug("herit call: "+query);
                         Parameter=Parameter+parameter1+" >="+parameter2;
                         CallableStatement cs = dbConn.prepareCall(query);
                         cs.execute();
                         cs.close();
                     } catch (SQLException e) {
                         log.error("Filter SQL Exception:", e);
                     }
                 } else if (filterMethodName.equals("'eQTL'")) {
                     try {
                         String seQTL=parameter1.substring(1,parameter1.length()-1);
                         int eQTLID=Integer.parseInt(seQTL);
                         String tissue=parameter3;
                         String query = "{call filter.eqtl(" + dsID + "," + v + "," + userID + "," + eQTLID + "," + tissue+") }";
                         CallableStatement cs = dbConn.prepareCall(query);
                         cs.execute();
                         cs.close();
                     } catch (SQLException e) {
                         log.error("Filter SQL Exception:", e);
                     }
                     
                 } else if (filterMethodName.equals("'gene.list'")) {
                     try {
                         String query = "{call filter.genelist(" + dsID + "," + v + "," + userID +",?,?,?) }";
                         CallableStatement cs = dbConn.prepareCall(query);
                         parameter1=parameter1.replaceAll("'", "");
                        int geneListID=Integer.parseInt(parameter1);
                        int keep=Integer.parseInt(parameter2);
                         int translate =0;
                         if(parameter3.equals("Y")){
                             translate=1;
                         }
                         cs.setInt(1, geneListID);
                         cs.setInt(2, keep);
                         cs.setInt(3, translate);
                         cs.execute();
                         cs.close();
                         
                     } catch (SQLException e) {
                         log.error("Filter SQL Exception:", e);
                     }
                     
                 } else if (filterMethodName.equals("'variation'")) {
                     
                     try {
                         String query = "{call filter.variation(" + dsID + "," + v + "," + userID +", ";
                         double p1=Double.parseDouble(parameter1);
                         if(p1<=1&&p1>=0){
                             query=query+parameter1+", -1 ";
                         }else{
                             query=query+" -1, "+parameter1;
                         }
                         query=query+") }";
                         CallableStatement cs = dbConn.prepareCall(query);
                         cs.execute();
                         cs.close();
                     } catch (SQLException e) {
                         log.error("Filter SQL Exception:", e);
                     }
                 } else if (filterMethodName.equals("'fold.change'")) {
                     try {
                         String query = "{call filter.FOLDCHANGE(" + dsID + "," + v + "," + userID +", ";
                         double p1=Double.parseDouble(parameter1);
                         if(p1<=1&&p1>=0){
                             query=query+parameter1+", -1 ";
                         }else{
                             query=query+" -1, "+parameter1;
                         }
                         query=query+") }";
                         CallableStatement cs = dbConn.prepareCall(query);
                         cs.execute();
                         cs.close();
                         
                     } catch (SQLException e) {
                         log.error("Filter SQL Exception:", e);
                     }
                 }
                 
                 //update cumulative filter reset(...,5)
                 //reset current filter reset(...,6)
                 //count current included probesets
                 Date doneFilterTimer=new Date();
                 log.debug("DONE FILTERING\nProcessing cleanup and Counting");
                 int count=-99;
                 try {
                     CallableStatement cs = dbConn.prepareCall(resetQ);
                     cs.setInt(1, 5);
                     cs.executeUpdate();
                     cs.close();
                     cs = dbConn.prepareCall(resetQ);
                     cs.setInt(1, 6);
                     cs.executeUpdate();
                     cs.close();
                     PreparedStatement ps=dbConn.prepareStatement(countQ);
                     ResultSet rs=ps.executeQuery();
                     rs.next();
                     count=rs.getInt(1);
                     ps.close();
                     log.debug("Count before HDF5::"+count);
                 } catch (SQLException e) {
                     log.error("Filter SQL Exception:", e);
                 }
                 Date moveFilterTimer=new Date();
                 try{
                     if(count>0){
                         Thread thread;
                         Async_HDF5_FileHandler ahf = new Async_HDF5_FileHandler(ds,"v"+dsVer.getVersion(),ds.getPath(), "Affy.NormVer.h5", "fillFilterProbes", null, session);
                         ahf.setFillHDFFilterParameters(userID, dbConn,abnormalHDF5File);
 
                         thread = new Thread(ahf);
                         log.debug("Starting thread to run Async_HDF5_FileHandler  "
                                 + "It is named " + thread.getName());
                         thread.start();
                         log.debug("Thread Started");
                         try {
                             thread.join();
                         } catch (InterruptedException ex) {
                             log.error("Error generating filtered dataset",ex);
                         }
                     }
                     myFileHandler.writeFile(Integer.toString(count),geneCountFile.getAbsolutePath());
                 }catch(IOException e){
                     log.error("Error writing filter count file or probeset list to HDF5 file",e);
                 }
                 Date moveToHDF5File=new Date();
                 log.debug("DONE PROCESSING. Count="+count);
                 dsfs.addFilterStep(filterMethodName,Parameter,count,-1,phenotypeParamGroupID,paramGroupID,dbConn);
                 log.debug("DONE FILTER STATS ENTRY");
                 double totalTime=(moveToHDF5File.getTime()-startTimer.getTime())/60000.0;
                 double filterTime=(doneFilterTimer.getTime()-startTimer.getTime())/60000.0;
                 double moveTime=(moveFilterTimer.getTime()-doneFilterTimer.getTime())/60000.0;
                 double moveHDF5=(moveToHDF5File.getTime()-moveFilterTimer.getTime())/1000.0;
                 log.debug("FILTER TIMING:\nTotal:"+totalTime+"min.\nFilter:"+filterTime+"min.\nMove to Cumulative:"+moveTime+"min.\nMove HDF5:"+moveHDF5+"sec.\n");
             } else {
                 log.debug("Not Exon array");
                 rFunction = platform + ".filter.genes";
 
                 log.debug("in callFilterGenes");
                 if (platform.equals(new Dataset().AFFYMETRIX_PLATFORM)
                         || platform.equals(new Dataset().CODELINK_PLATFORM)) {
                     if (platform.equals(new Dataset().AFFYMETRIX_PLATFORM)) {
                         functionArgs = new String[7];
                     } else {
                         functionArgs = new String[8];
                     }
                     functionArgs[0] = "InputDataFile = '" + inputFile.getPath() + "'";
                     functionArgs[1] = "filter.method = " + filterMethodName;
                     functionArgs[2] = "filter.parameter1 = " + parameter1;
                     functionArgs[3] = "filter.parameter2 = " + parameter2;
                     functionArgs[4] = "OutputFile = '" + outputFileName + "'";
                     functionArgs[5] = "GeneNumberFile = '" + geneCountFile.getPath() + "'";
                     functionArgs[6] = "OriginalFile = '" + firstInputFile.getPath() + "'";
                     if (platform.equals(new Dataset().CODELINK_PLATFORM)) {
                         functionArgs[7] = (parameter3 == null || (parameter3 != null && parameter3.equals(""))
                                 ? "filter.parameter3 = ''"
                                 : "filter.parameter3 = " + parameter3);
                     }
                 } else if (platform.equals("cDNA")) {
                     functionArgs[0] = "InputDataFile = '" + inputFile.getPath() + "'";
                     functionArgs[1] = "filter.method = " + filterMethodName;
                     functionArgs[2] = "para1 = " + parameter1;
                     functionArgs[3] = "para2 = " + parameter2;
                     functionArgs[4] = "para3 = '" + parameter3 + "'";
                     functionArgs[5] = "OutputFile = '" + analysisPath + inputFile.getName() + "'";
                     functionArgs[6] = "GeneNumberFile = '" + geneCountFile.getPath() + "'";
                 }
                 log.debug("functionArgs = ");
                 myDebugger.print(functionArgs);
 
                 if ((rErrorMsg =
                         myR_session.callR(this.getRFunctionDir(), rFunction, functionArgs, analysisPath, -99)) != null) {
                     String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
                     log.debug("after R call for filterGenes, got errorMsg. It is " + errorMsg);
                     throw new RException(errorMsg);
                 }
             }
         }
          
         public int callPreviousFilter(Dataset dataset,Dataset.DatasetVersion version,int userID,String abnormalFilePath){
             log.debug("Previous Filter");
             String resetQ = "{call filter.reset(" + dataset.getDataset_id() + "," + version.getVersion() + "," + userID + ",?)}";
             String countQ = "Select count(*) from EXON_USER_FILTER_TEMP where dataset_id="+dataset.getDataset_id()+" and dataset_version="+version.getVersion()+" and user_id="+userID+" and cumulative_filter=1";
             int count=-99;
             try {
                 CallableStatement cs = dbConn.prepareCall(resetQ);
                 cs.setInt(1, 9);
                 cs.executeUpdate();
                 cs.close();
                 PreparedStatement ps= dbConn.prepareCall(countQ);
                 ResultSet rs=ps.executeQuery();
                 rs.next();
                 count=rs.getInt(1);
                 ps.close();
                 Thread thread;
                 Async_HDF5_FileHandler ahf = new Async_HDF5_FileHandler(dataset,"v"+version.getVersion(),dataset.getPath(), "Affy.NormVer.h5", "fillFilterProbes", null, session);
                 ahf.setFillHDFFilterParameters(userID, dbConn,abnormalFilePath);
 
                 thread = new Thread(ahf);
                 log.debug("Starting thread to run Async_HDF5_FileHandler  "
                         + "It is named " + thread.getName());
                 thread.start();
                 log.debug("Thread Started");
                 try {
                     thread.join();
                 } catch (InterruptedException ex) {
                     log.error("Error generating filtered dataset",ex);
                 }
             } catch (SQLException e) {
                 log.error("Filter SQL Exception:", e);
             }
             log.debug("Previous Count:"+count);
             return count;
             
         }
         
         public int moveFilterToHDF5(Dataset ds, Dataset.DatasetVersion dsv,String abnormalFilePath) {
             int num=-1;
             log.debug("in moveFilterToHDF5");
             Thread thread;
             Async_HDF5_FileHandler ahf = new Async_HDF5_FileHandler(ds,"v"+dsv.getVersion(),ds.getPath(), "Affy.NormVer.h5", "fillHDF5Filter", null, session);
             ahf.setFillHDFFilterParameters(this.userLoggedIn.getUser_id(), dbConn,abnormalFilePath);
 
             thread = new Thread(ahf);
             log.debug("Starting thread to run Async_HDF5_FileHandler  "
                     + "It is named " + thread.getName());
             thread.start();
             log.debug("Thread Started");
             try {
                 thread.join();
                 num=ahf.getNumberOfProbes();
             } catch (InterruptedException ex) {
                 log.error("Error generating filtered dataset",ex);
             }
             return num;
         }
 
 	
         /**
          * Calls the 2-way ANOVA statistics function.
          * @param myDataset       the selected Dataset object
          * @param inputFile       the name of the inputFile
          * @param outputFile       the factor file name
          * @param geneCountAfterStatisticsFileName       the name of the file that stores the number of genes
          * @param myArrays       myArrays
          * @param fieldValues       a Hashtable of fieldValues
          * @param analysisPath       the directory where the analysis files are stored
 	 * @throws RException if an error occurs when running R
 	 * @throws IOException if an error occurs creating the factor file 
          */
 
 	public boolean call2WayAnovaStatistics(Dataset myDataset,
 					String inputFile,
                                         String version,
 					String outputFile,
 					String geneCountAfterStatisticsFileName,
 					edu.ucdenver.ccp.PhenoGen.data.Array[] myArrays,
 					Hashtable fieldValues,
 					String analysisPath) throws RException, IOException {
 						
                 boolean async=false;
                 long cutOff=5000000;
 		log.debug("in call2WayAnovaStatistics");
                 int v=Integer.parseInt(version.substring(1));
                 String verFDate=(String)session.getAttribute("verFilterDate");
                 String verFTime=(String)session.getAttribute("verFilterTime");
                 version=version+"/"+verFDate+"/"+verFTime;
                 String sampleFile=inputFile.substring(0,inputFile.lastIndexOf("/")+1)+"v"+v+"_samples.txt'";
                 
                 if (myDataset.getPlatform().equals(myDataset.AFFYMETRIX_PLATFORM) ||
                         myDataset.getPlatform().equals(new Dataset().CODELINK_PLATFORM))  {
 
 			String factorFileName = myDataset.getNameNoSpaces() + "factorFile.txt";
                         File factorFile = new File(analysisPath + factorFileName);
                         //BufferedWriter factorFileWriter = new BufferedWriter(new FileWriter(factorFile), 10000);
                         BufferedWriter factorFileWriter = myFileHandler.getBufferedWriter(analysisPath + factorFileName);
                         //factorFile.createNewFile();
                         factorFileWriter.write("Sample Name\t" +
 						((String)fieldValues.get("factor1_name")).replaceAll("[\\s]", "_") + "\t" +
 						((String)fieldValues.get("factor2_name")).replaceAll("[\\s]", "_")); 
                         factorFileWriter.newLine();
                         for (int i=0; i<myArrays.length; i++) {
                                         factorFileWriter.write(myArrays[i].getHybrid_name().replaceAll("[\\s]", "_") + "\t" +
                                                 ((String)fieldValues.get("factor1_"+i)).replaceAll("[\\s]", "_") + "\t" +
                                                 ((String)fieldValues.get("factor2_"+i)).replaceAll("[\\s]", "_"));
                                         factorFileWriter.newLine();
                         }
                         factorFileWriter.flush();
                         factorFileWriter.close();
 
                         //
                         // First run a program to check for over-parameterization
                         //
                         String outFileName = analysisPath + "OverParameterizationFile.txt";
 
 			callCheckTwoWayAnovaParameterization(
                                                 (String) fieldValues.get("twoWayPValue"),
                                                 outFileName,
                                                 analysisPath,
                                                 factorFileName);
 
                         File outFile = new File(outFileName);
 
                         if (outFile.exists()) {
                         	String[] factorCheck = myFileHandler.getFileContents(outFile, "withSpaces");
                                 log.debug("factorCheck = "+factorCheck[0]);
                                 if (factorCheck[0].indexOf("Warning") == -1) {
                                         functionArgs = new String[5];
 
 					functionArgs[0] = "InputFile = " + inputFile;
                                         functionArgs[1] = "pvalue= '" + (String) fieldValues.get("twoWayPValue") + "'";
                                         functionArgs[2] = "OutputFile = " + outputFile;
                                         functionArgs[3] = "GeneNumberFile = '" + geneCountAfterStatisticsFileName + "'";
                                         functionArgs[4] = "FactorFile = '" + analysisPath + factorFileName + "'";
 				} else {
 					throw new RException("Model is over-parameterized");
 				}
 			}
                 }
                 DSFilterStat dsfs=null;
                 String params=functionArgs[1];
 		rFunction = "statistics.TwoWay.ANOVA";
                 if(inputFile.endsWith(".h5'")){
                     rFunction=rFunction+".HDF5";
                     functionArgs[0]=functionArgs[0]+ ", VersionPath= '"+version+"', SampleFile= "+sampleFile;
                     
                     //find gene count and sample count
                     //open .h5 file get probecount, get sample dim.
                     PhenoGen_HDF5_File file=null;
                     long probeCount=0;
                     long sampleCount=0;
                     try{
                         file = new PhenoGen_HDF5_File(inputFile.substring(1,inputFile.length()-1));
                         file.openVersion(version);
                         probeCount=file.getProbeCount("/v"+v+"/Filters/"+verFDate+"/"+verFTime+"/","fProbeset");
                         sampleCount=file.getSampleCount("/v"+v+"/Filters/"+verFDate+"/"+verFTime+"/","fData");
                         file.close();
                     }catch(Exception e){
                         try{
                         file.close();
                         }catch(Exception er){
                             log.error("Error Statistic.callStatistic() HDF5 File error",er);
                         }
                         log.error("Error Statistic.callStatistic() HDF5 File error",e);
                     }
                     
                     //determine if async is needed
                     long calcSize=probeCount*sampleCount;
                     log.debug("Calculated Size:"+calcSize+"("+probeCount+"*"+sampleCount+")");
                     if(calcSize>cutOff){
                         async=true;
                     }
                     
                     
                     
                     DSFilterStat tmp=new DSFilterStat();
                     User userLoggedIn=(User)session.getAttribute("userLoggedIn");
                     dsfs=tmp.getFilterStatFromDB(myDataset.getDataset_id(),v,userLoggedIn.getUser_id(),verFDate,verFTime,dbConn);
                     System.out.println("final ID:"+dsfs.getDSFilterStatID());
                     dsfs.addStatsStep("Statistics: 2Way ANOVA",params,-1,1,0,dbConn);
                 }
                 log.debug("functionArgs = "); myDebugger.print(functionArgs);
                 if(async){
                     Thread asyncR= new Thread(new Async_R_Session(
                                                             this.getRFunctionDir(), 
                                                             rFunction, 
                                                             functionArgs,
                                                             analysisPath, 
                                                             null));
 
                     log.debug("Starting thread to run Affymetrix.Exon.Import.  "+
                                     "It is named "+asyncR.getName());
 
                     asyncR.start();
                     Thread update=new Thread(new AsyncUpdateStatsDataset(myDataset.getDataset_id(),v,dsfs,"Statistics: 2Way ANOVA",params,geneCountAfterStatisticsFileName,session,asyncR));
                     update.start();
                 }else{
                     if ((rErrorMsg = 
                             myR_session.callR(this.getRFunctionDir(), rFunction, functionArgs, analysisPath, -99)) != null) {
                             String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
                             log.debug("after R call for 2-Way ANOVA statistics, got errorMsg. It is "+errorMsg);
                             if(dsfs!=null){
                                 dsfs.addStatsStep("Statistics: 2Way ANOVA",params,-1,1,-1,dbConn);
                             }
                             throw new RException(errorMsg);
                     }else{
                         if(dsfs!=null){
                             int tmpcount=-1;
                             try{
                                 tmpcount=Integer.parseInt(myFileHandler.getFileContents(new File(geneCountAfterStatisticsFileName), "noSpaces")[0]);
                             }catch(IOException e){
                                 log.error("Exception Opening Stats Count file:"+e,e);
                             }
                             dsfs.addStatsStep("Statistics: 2Way ANOVA",params,tmpcount,1,1,dbConn);
                         }
                     }
                 }
                 return async;
 	}
 
         /**
          * Calls the clustering statistics function.
          * @param myDataset       the selected Dataset object
          * @param inputFile       the name of the inputFile
          * @param cluster_method       the clustering method
          * @param groupsFileName       the groups file name
          * @param distance       the distance value
          * @param clusterObject       the cluster object
          * @param groupMeansBoolean       TRUE if group means are used, otherwise false 
          * @param parameter1       parameter1
          * @param parameter2       parameter2
          * @param analysisPath       the directory where the analysis files are stored
 	 * @throws RException if an error occurs when running R
 	 * @throws IOException if an error occurs removing the old cluster files
          */
 
 	public boolean callClusterStatistics(Dataset myDataset,
 					String inputFile,
                                         String version,
                                         int clusterID,
 					String cluster_method,
 					String groupsFileName,
 					String distance,
 					String clusterObject,
 					String groupMeansBoolean,
 					String parameter1,
 					String parameter2,
 					String analysisPath) throws RException, IOException {
 						
                 long cutOff=5000000;
                 boolean async=false;
             log.debug("in callClusterStatistics");
 		String warningFileName = analysisPath + "warnings.txt";
 		File warningFile = new File(warningFileName);
                 
                 int v=Integer.parseInt(version.substring(1));
                 String verFDate=(String)session.getAttribute("verFilterDate");
                 String verFTime=(String)session.getAttribute("verFilterTime");
                 version=version+"/"+verFDate+"/"+verFTime;
                 String sampleFile=inputFile.substring(0,inputFile.lastIndexOf("/")+1)+"v"+v+"_samples.txt'";
                 String groupFile=inputFile.substring(0,inputFile.lastIndexOf("/")+1)+"v"+v+"_groups.txt'";;
                 log.debug("Version="+version);
 
                 if (myDataset.getPlatform().equals(myDataset.AFFYMETRIX_PLATFORM) ||
                         myDataset.getPlatform().equals(new Dataset().CODELINK_PLATFORM))  {
 
 			rFunction = "statistics.Clustering";
 			// Clean up files from last run
 			File [] oldFiles = myDataset.getClusterFiles(analysisPath);
 
 			for (int i=0; i<oldFiles.length; i++) {
                          	//log.debug("going to remove "+oldFiles[i].getName());
                                 try {
                                         myFileHandler.deleteFile(oldFiles[i]);
 				} catch (Exception e) {
                                 	log.error("error deleting old files");
 				}
 			}
                         functionArgs = new String[9];
                         functionArgs[0] = "InputFile = " + inputFile;
                         functionArgs[1] = "ClusterType = '" + cluster_method + "'";
                         functionArgs[2] = "ClusterObject = '" + clusterObject + "'";
                         functionArgs[3] = "RunGroups = " + groupMeansBoolean;
                         functionArgs[4] = "GroupLabels = '" + groupsFileName + "'";
                         functionArgs[5] = "Distance = '" + distance + "'";
                         functionArgs[6] = "parameter1 = " + parameter1;
                         functionArgs[7] = "parameter2 = "+ parameter2;
                         functionArgs[8] = "error.file = '"+ warningFileName + "'";
                 }
                 String params="cluster by "+clusterObject+", Use Group Means:"+groupMeansBoolean+", Distance:"+distance+", Param1:"+parameter1+", Param2:"+parameter2+", ClusterID="+clusterID;
                 DSFilterStat dsfs=null;
                 if(inputFile.endsWith(".h5'")){
                     rFunction=rFunction+".HDF5";
                     functionArgs[4] = "GroupLabels = " + groupFile ;
                     functionArgs[0] = functionArgs[0] + ", VersionPath= '"+version+"', SampleFile= "+sampleFile;
                     
                     //find gene count and sample count
                     //open .h5 file get probecount, get sample dim.
                     PhenoGen_HDF5_File file=null;
                     long probeCount=0;
                     long sampleCount=0;
                     try{
                         file = new PhenoGen_HDF5_File(inputFile.substring(1,inputFile.length()-1));
                         file.openVersion(version);
                         probeCount=file.getProbeCount("/v"+v+"/Filters/"+verFDate+"/"+verFTime+"/","fProbeset");
                         sampleCount=file.getSampleCount("/v"+v+"/Filters/"+verFDate+"/"+verFTime+"/","fData");
                         file.close();
                     }catch(Exception e){
                         try{
                         file.close();
                         }catch(Exception er){
                             log.error("Error Statistic.callStatistic() HDF5 File error",er);
                         }
                         log.error("Error Statistic.callStatistic() HDF5 File error",e);
                     }
                     
                     //determine if async is needed
                     long calcSize=probeCount*sampleCount;
                     log.debug("Calculated Size:"+calcSize+"("+probeCount+"*"+sampleCount+")");
                     if(calcSize>cutOff){
                         async=true;
                     }
                     
                     DSFilterStat tmp=new DSFilterStat();
                     User userLoggedIn=(User)session.getAttribute("userLoggedIn");
                     dsfs=tmp.getFilterStatFromDB(myDataset.getDataset_id(),v,userLoggedIn.getUser_id(),verFDate,verFTime,dbConn);
                     System.out.println("final ID:"+dsfs.getDSFilterStatID());
                     dsfs.addStatsStep("Statistics: "+cluster_method+" Clustering",params,-1,1,0,dbConn);
                 }
 
 		log.debug("functionArgs = "); myDebugger.print(functionArgs);
                 if(async){
                     Thread asyncR= new Thread(new Async_R_Session(
                                                             this.getRFunctionDir(), 
                                                             rFunction, 
                                                             functionArgs,
                                                             analysisPath, 
                                                             null));
 
                     log.debug("Starting thread to run Affymetrix.Exon.Import.  "+
                                     "It is named "+asyncR.getName());
 
                     asyncR.start();
                     Thread update=new Thread(new AsyncUpdateStatsDataset(myDataset.getDataset_id(),v,dsfs,"Statistics: "+cluster_method+" Clustering",params,null,session,asyncR));
                     update.start();
                 }else{
                     boolean errors=false;
                     try {
                             rErrorMsg = myR_session.callR(this.getRFunctionDir(), rFunction, functionArgs, analysisPath, -99);
                     } catch (Exception e) {
                             errors=true;
                             if (warningFile.exists()) {
                                     String[] message = myFileHandler.getFileContents(warningFile, "withSpaces");
                                     log.debug("message = "+message[0]);
                                     String warningMsg = new ObjectHandler().getAsSeparatedString(message, "<BR>");
                                     throw new RException(warningMsg);
                             } else if (rErrorMsg != null) {
                                     String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
                                     log.debug("after R call for clustering statistics, got errorMsg. It is "+errorMsg);
                                     throw new RException(errorMsg);
                             }
                     }
                     if(rErrorMsg!=null){
                         errors=true;
                     }
 
                     if(errors&&dsfs!=null){
                         dsfs.addStatsStep("Statistics: "+cluster_method+" Clustering",params,-1,1,-1,dbConn);
                     }else if (dsfs!=null){
                         dsfs.addStatsStep("Statistics: "+cluster_method+" Clustering",params,-1,1,1,dbConn);
                     }
                 }
                 return async;
 	}
 
         /**
          * Calls the correlation statistics function.
          * @param platform       the platform
          * @param inputFile       the name of the inputFile
          * @param stat_method       the statistics method
          * @param outputFile       the factor file name
          * @param geneCountAfterStatisticsFileName       the name of the file that stores the number of genes
          * @param groupingUserPhenotypeDir       the directory containing the phenotype data for this grouping
          * @param analysisPath       the directory where the analysis files are stored
 	 * @throws RException if an error occurs when running R
          */
 
 	public boolean callCorrelationStatistics(String platform,
                                         Dataset myDataset,
 					String inputFile,
                                         String version,
                                         String SampleFileName,
 					String stat_method,
 					String outputFile,
 					String geneCountAfterStatisticsFileName,
 					String groupingUserPhenotypeDir,
 					String analysisPath) throws RException {
 						
             long cutOff=5000000;
             boolean async=false;
             log.debug("in callCorrelationStatistics");
             int v=Integer.parseInt(version.substring(1));
             String verFDate=(String)session.getAttribute("verFilterDate");
             String verFTime=(String)session.getAttribute("verFilterTime");
             version=version+"/"+verFDate+"/"+verFTime;
             //log.debug("Correlation Version:"+version);
 
                 if (platform.equals(new Dataset().AFFYMETRIX_PLATFORM) ||
                         platform.equals(new Dataset().CODELINK_PLATFORM))  {
 
 			rFunction = "statistics.Correlations";
                         functionArgs = new String[5];
                         functionArgs[0] = "InputFile = " + inputFile;
                         functionArgs[1] = "PhenoFile= '" +
                                                         groupingUserPhenotypeDir +
                                                         "Phenotype.output.Rdata" + "'";
                         functionArgs[2] = "CorrType = '" + stat_method + "'";
                         functionArgs[3] = "OutputFile = " + outputFile;
                         functionArgs[4] = "GeneNumberFile = '" + geneCountAfterStatisticsFileName + "'";
                         
                         
                 }
                 DSFilterStat dsfs=null;
                 String params=functionArgs[2];
                 
                 if (inputFile.endsWith(".h5'")) {
                     functionArgs[3] = "VersionPath='" + version + "' ,SampleFile='" + SampleFileName + "'";
                     rFunction = rFunction + ".HDF5";
                     
                     //find gene count and sample count
                     //open .h5 file get probecount, get sample dim.
                     PhenoGen_HDF5_File file=null;
                     long probeCount=0;
                     long sampleCount=0;
                     try{
                         file = new PhenoGen_HDF5_File(inputFile.substring(1,inputFile.length()-1));
                         file.openVersion(version);
                         probeCount=file.getProbeCount("/v"+v+"/Filters/"+verFDate+"/"+verFTime+"/","fProbeset");
                         sampleCount=file.getSampleCount("/v"+v+"/Filters/"+verFDate+"/"+verFTime+"/","fData");
                         file.close();
                     }catch(Exception e){
                         try{
                         file.close();
                         }catch(Exception er){
                             log.error("Error Statistic.callStatistic() HDF5 File error",er);
                         }
                         log.error("Error Statistic.callStatistic() HDF5 File error",e);
                     }
                     
                     //determine if async is needed
                     long calcSize=probeCount*sampleCount;
                     log.debug("Calculated Size:"+calcSize+"("+probeCount+"*"+sampleCount+")");
                     if(calcSize>cutOff){
                         async=true;
                     }
                     
                     DSFilterStat tmp = new DSFilterStat();
                     User userLoggedIn = (User) session.getAttribute("userLoggedIn");
                     dsfs = tmp.getFilterStatFromDB(myDataset.getDataset_id(), v, userLoggedIn.getUser_id(), verFDate, verFTime, dbConn);
                     log.debug("final ID:" + dsfs.getDSFilterStatID());
                     dsfs.addStatsStep("Statistics: Correlation", params, -1, 1, 0, dbConn);
                 }
 		log.debug("functionArgs = "); myDebugger.print(functionArgs);
                 if(async){
                     Thread asyncR= new Thread(new Async_R_Session(
                                                             this.getRFunctionDir(), 
                                                             rFunction, 
                                                             functionArgs,
                                                             analysisPath, 
                                                             null));
 
                     log.debug("Starting thread to run Affymetrix.Exon.Import.  "+
                                     "It is named "+asyncR.getName());
 
                     asyncR.start();
                     Thread update=new Thread(new AsyncUpdateStatsDataset(myDataset.getDataset_id(),v,dsfs,"Statistics: Correlation",params,geneCountAfterStatisticsFileName,session,asyncR));
                     update.start();
                 }else{
                     if ((rErrorMsg = 
                             myR_session.callR(this.getRFunctionDir(), rFunction, functionArgs, analysisPath, -99)) != null) {
                             String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
                             log.debug("after R call for correlation statistics, got errorMsg. It is "+errorMsg);
                             if(dsfs!=null){
                                 dsfs.addStatsStep("Statistics: Correlation",params,-1,1,-1,dbConn);
                             }
                             throw new RException(errorMsg);
                     }else{
                         if(dsfs!=null){
                             int tmpcount=-1;
                                 try{
                                     tmpcount=Integer.parseInt(myFileHandler.getFileContents(new File(geneCountAfterStatisticsFileName), "noSpaces")[0]);
                                 }catch(IOException e){
                                     log.error("Exception Opening Stats Count file:"+e,e);
                                 }
                             dsfs.addStatsStep("Statistics: Correlation",params,tmpcount,1,1,dbConn);
                         }
                     }
                 }
                 return async;
 	}
 
         /**
          * Calls the statistics function.
          * @param platform       the platform
          * @param inputFile       the name of the inputFile
          * @param version       the version to use
          * @param stat_method       the statistics method
          * @param outputFile       the factor file name
          * @param geneCountAfterStatisticsFileName       the name of the file that stores the number of genes
          * @param pValue       the p-value
          * @param analysisPath       the directory where the analysis files are stored
 	 * @throws RException if an error occurs when running R
          */
 
 	public boolean callStatistics(String platform,
 					String inputFile,
                                         int datasetID,
                                         String version,
                                         String SampleFileName,
 					String stat_method,
 					String outputFile,
 					String geneCountAfterStatisticsFileName,
 					String pValue,
 					String analysisPath) throws RException {
 		boolean ret=false;
 		log.debug("in callStatistics");
                 int v=Integer.parseInt(version.substring(1));
                 String verFDate=(String)session.getAttribute("verFilterDate");
                 String verFTime=(String)session.getAttribute("verFilterTime");
                 version=version+"/"+verFDate+"/"+verFTime;
                 long cutOff=5000000;
                 
                 if (platform.equals(new Dataset().AFFYMETRIX_PLATFORM) ||
                         platform.equals(new Dataset().CODELINK_PLATFORM))  {
 			functionArgs = new String[4];
                         functionArgs[0] = "InputFile = " + inputFile;
                         functionArgs[2] = "OutputFile = " + outputFile;
                         functionArgs[3] = "GeneNumberFile = '" + geneCountAfterStatisticsFileName + "'";
 
                         if (stat_method.equals("nonparametric") || stat_method.equals("parametric")) {
                                 if(inputFile.endsWith(".h5'")){
                                     rFunction = "statistics.HDF5";
                                     functionArgs[2]="SampleFile='"+SampleFileName+"', VersionPath='"+version+"'";
                                 }else{
                                     rFunction = "statistics";
                                 }
                                 functionArgs[1] = "Stat.method = '" + stat_method + "'";
                         } else if (stat_method.equals("1-Way ANOVA")) {
                             if(inputFile.endsWith(".h5'")){
                                 rFunction = "statistics.OneWay.ANOVA.HDF5";
                                 functionArgs[2]="SampleFile='"+SampleFileName+"', VersionPath='"+version+"'";
                             }else{
                                 rFunction = "statistics.OneWay.ANOVA";
                             }
                                 functionArgs[1] = "pvalue= '" + pValue + "'";
                         } else if (stat_method.equals("Noise distribution t-test")) {
                             if(inputFile.endsWith(".h5'")){
                                 rFunction = "statistics.EavesTest.HDF5";
                                 functionArgs[2]="SampleFile='"+SampleFileName+"', VersionPath='"+version+"'";
                             }else{
                                 rFunction = "statistics.EavesTest";
                             }
                                 functionArgs[1] = "pvalue = " + pValue;
 			}
                 } else if (platform.equals("cDNA")) {
                         rFunction = "cDNA.Statistics";
                         functionArgs = new String[4];
                         functionArgs[0] = "InputDataFile = " + inputFile;
                         functionArgs[1] = "stat.method = '" + stat_method + "'";
                         functionArgs[2] = "OutputDataFile = " + outputFile;
                         functionArgs[3] = "GeneNumberFile = '" + geneCountAfterStatisticsFileName + "'";
                 }
 
 		log.debug("functionArgs = "); myDebugger.print(functionArgs);
                 boolean Async=false;
                 DSFilterStat dsfs=null;
                 String methodName="Statistics:"+stat_method;
                 String params=functionArgs[1];
                 if(inputFile.endsWith(".h5'")){
                     //find gene count and sample count
                     //open .h5 file get probecount, get sample dim.
                     PhenoGen_HDF5_File file=null;
                     long probeCount=0;
                     long sampleCount=0;
                     try{
                         file = new PhenoGen_HDF5_File(inputFile.substring(1,inputFile.length()-1));
                         file.openVersion(version);
                         probeCount=file.getProbeCount("/v"+v+"/Filters/"+verFDate+"/"+verFTime+"/","fProbeset");
                         sampleCount=file.getSampleCount("/v"+v+"/Filters/"+verFDate+"/"+verFTime+"/","fData");
                         file.close();
                     }catch(Exception e){
                         try{
                         file.close();
                         }catch(Exception er){
                             log.error("Error Statistic.callStatistic() HDF5 File error",er);
                         }
                         log.error("Error Statistic.callStatistic() HDF5 File error",e);
                     }
                     
                     //determine if async is needed
                     long calcSize=probeCount*sampleCount;
                     log.debug("Calculated Size:"+calcSize+"("+probeCount+"*"+sampleCount+")");
                     if(calcSize>cutOff){
                         Async=true;
                     }
                     //create Stats entry running
                     DSFilterStat tmp=new DSFilterStat();
                     User userLoggedIn=(User)session.getAttribute("userLoggedIn");
                     dsfs=tmp.getFilterStatFromDB(datasetID,v,userLoggedIn.getUser_id(),verFDate,verFTime,dbConn);
                     System.out.println("final ID:"+dsfs.getDSFilterStatID());
                     dsfs.addStatsStep(methodName,params,-1,1,0,dbConn);
                 }
                 
                 if(Async){
                     ret=true;
                     Thread asyncR= new Thread(new Async_R_Session(
                                                             this.getRFunctionDir(), 
                                                             rFunction, 
                                                             functionArgs,
                                                             analysisPath, 
                                                             null));
 
                     log.debug("Starting thread to run Affymetrix.Exon.Import.  "+
                                     "It is named "+asyncR.getName());
 
                     asyncR.start();
                     Thread update=new Thread(new AsyncUpdateStatsDataset(datasetID,v,dsfs,methodName,params,geneCountAfterStatisticsFileName,session,asyncR));
                     update.start();
                 }else{
                     if ((rErrorMsg = 
 			myR_session.callR(this.getRFunctionDir(), rFunction, functionArgs, analysisPath, -99)) != null) {
 			String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
 			log.debug("after R call for statistics, got errorMsg. It is "+errorMsg);
                         //save stats error
                         
 			throw new RException(errorMsg);
                     }else{
                         if(dsfs!=null){
                             //save stats done
                             int tmpcount=-1;
                             try{
                                 tmpcount=Integer.parseInt(myFileHandler.getFileContents(new File(geneCountAfterStatisticsFileName), "noSpaces")[0]);
                             }catch(IOException e){
                                 log.error("Exception Opening Stats Count file:"+e,e);
                             }
                             dsfs.addStatsStep(methodName,params,tmpcount,1,1,dbConn);
                         }
                     }
                 }
                 return ret;
 	}
 
         /**
          * Calls the multipleTest function.
          * @param platform       the platform
          * @param inputFileName       the name of the inputFile
          * @param outputFileName       the name of the outputFile
          * @param mtMethodName       the name of the multiple testing method
          * @param geneCountAfterMultipleTestFileName       the name of the file that stores the number of genes
          * @param parameter1       parameter1
          * @param parameter2       parameter2
          * @param parameter3       parameter3
          * @param parameter4       parameter4
          * @param analysisPath       the directory where the analysis files are stored
 	 * @throws RException if an error occurs when running R
          */
 
 	public void callMultipleTest(String platform,
 					String inputFileName,
                                         String version,
                                         int datasetID,
 					String outputFileName,
 					String mtMethodName,
 					String geneCountAfterMultipleTestFileName,
 					String parameter1,
 					String parameter2,
 					String parameter3,
 					String parameter4,
 					String analysisPath) throws RException, IOException {
 						
 		log.debug("in callMultipleTest");
 		int v=Integer.parseInt(version.substring(1));
                 String verFDate=(String)session.getAttribute("verFilterDate");
                 String verFTime=(String)session.getAttribute("verFilterTime");
                 version=version+"/"+verFDate+"/"+verFTime;
                 log.debug("Version="+version);
                 
                 String warningFileName = analysisPath + "warnings.txt";
 		File warningFile = new File(warningFileName);
                 functionArgs = new String[9];
 
                 if (platform.equals(new Dataset().AFFYMETRIX_PLATFORM) ||
                 	platform.equals(new Dataset().CODELINK_PLATFORM)) {
                         rFunction = "multipleTest";
                         
                         functionArgs[0] = "InputFile = '" + inputFileName + "'";
                         functionArgs[1] = "mt.method = " + mtMethodName;
                         functionArgs[2] = "MCC.parameter1 = " + parameter1;
                         functionArgs[3] = "MCC.parameter2 = '" + parameter2 + "'";
                         //
                         // this parameter is a numeric value, so only enclose in quotes
                         // if it's null
                         //
                         if (parameter3 == null) {
                         	functionArgs[4] = "MCC.parameter3 = '" + parameter3 + "'";
 			} else {
                         	functionArgs[4] = "MCC.parameter3 = " + parameter3;
 			}
 			functionArgs[5] = "MCC.parameter4 = '" + parameter4 + "'";
                         functionArgs[6] = "OutputFile = '" + outputFileName + "'";
                         functionArgs[7] = "GeneNumberFile = '" + geneCountAfterMultipleTestFileName + "'";
                         functionArgs[8] = "error.file = '"+ warningFileName + "'";
                         
                         
 		} else if (platform.equals("cDNA")) {
 			rFunction = "cDNA.multipleTesting";
 
 			functionArgs[0] = "InputDataFile = '" +
                         			analysisPath + "cDNA.statistics.output.Rdata'";
 			functionArgs[1] = "mt.method = " + mtMethodName;
                         functionArgs[2] = "para1 = " + parameter1;
                         functionArgs[3] = "para2 = '" + parameter2 + "'";
                         functionArgs[4] = "para3 = '" + parameter3 + "'";
                         functionArgs[5] = "para4 = '" + parameter4 + "'";
                         functionArgs[6] = "OutputDataFile = '" + analysisPath + "cDNA.multipleTesting.output.Rdata'";
 			functionArgs[7] = "GeneNumberFile = '" + geneCountAfterMultipleTestFileName + "'";
                         functionArgs[8] = "error.file = '"+ warningFileName + "'";
 		}
                 DSFilterStat dsfs=null;
                 String param="";
                 for (int i = 2; i < 6; i++) {
                     param = param + functionArgs[i];
                     if (i < 5) {
                         param = param + ",";
                     }
                 }
                 //log.debug("Before .h5 specific");
                 if(inputFileName.endsWith(".h5")){
                             rFunction="multipleTest.HDF5";
                             functionArgs[6] = "VersionPath = '" + version + "'";
                             DSFilterStat tmp=new DSFilterStat();
                             User userLoggedIn=(User)session.getAttribute("userLoggedIn");
                             dsfs=tmp.getFilterStatFromDB(datasetID,v,userLoggedIn.getUser_id(),verFDate,verFTime,dbConn);
                             //System.out.println("final ID:"+dsfs.getDSFilterStatID());
                             
                             dsfs.addStatsStep("Multiple Testing:"+mtMethodName,param,-1,2,0,dbConn);
                 }
                 //log.debug("after .h5 specific");
 		log.debug("functionArgs = "); myDebugger.print(functionArgs);
 		try {
 			rErrorMsg = myR_session.callR(this.getRFunctionDir(), rFunction, functionArgs, analysisPath, -99);
                         if(dsfs!=null){
                             //save stats done
                             int tmpcount=-1;
                             try{
                                 tmpcount=Integer.parseInt(myFileHandler.getFileContents(new File(geneCountAfterMultipleTestFileName), "noSpaces")[0]);
                             }catch(IOException e){
                                 log.error("Exception Opening Stats Count file:"+e,e);
                             }
                             dsfs.addStatsStep("Multiple Testing:"+mtMethodName,param,tmpcount,2,1,dbConn);
                         }
 		} catch (Exception e) {
                         dsfs.addStatsStep("Multiple Testing:"+mtMethodName,param,-1,2,-1,dbConn);
 			if (warningFile.exists()) {
                         	String[] message = myFileHandler.getFileContents(warningFile, "withSpaces");
 				log.debug("message = "+message[0]);
 				String warningMsg = new ObjectHandler().getAsSeparatedString(message, "<BR>");
 				throw new RException(warningMsg);
 			} else if (rErrorMsg != null) {
 				String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
 				log.debug("after R call for multipleTest, got errorMsg. It is "+errorMsg);
 				throw new RException(errorMsg);
 			}
 		}
 	}
 
         /**
          * Calls the checkTwoWayAnovaParameterization function.
          * @param pValue       the selected p-value
          * @param outFileName       the name of the output file
          * @param analysisPath       the directory where the analysis files are stored
          * @param factorFileName       the factor file name
 	 * @throws RException if an error occurs when running R
          */
 
 	public void callCheckTwoWayAnovaParameterization(String pValue,
 					String outFileName,
 					String analysisPath,
 					String factorFileName) throws RException {
 						
 		log.debug("in callcheckTwoWayAnovaParameterization");
 		rFunction = "check.TwoWayAnova.Parameterization";
 		functionArgs = new String[3];
 
 		functionArgs[0] = "pvalue= '" + pValue + "'";
                 functionArgs[1] = "OutFile = '" + outFileName + "'";
                 functionArgs[2] = "FactorFile = '" + analysisPath + factorFileName + "'";
 
 		log.debug("functionArgs = "); myDebugger.print(functionArgs);
 
                 if ((rErrorMsg = 
 			myR_session.callR(this.getRFunctionDir(), rFunction, functionArgs, analysisPath, -99)) != null) {
 			String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
 			log.debug("after R call for checkTwoWayAnovaParameterization, got errorMsg. It is "+errorMsg);
 			throw new RException(errorMsg);
 		}
 	}
 
         /**
          * Calls the outputRawSpecificGene function.
          * @param platform       the platform
          * @param inputFileName       the name of the inputFile
          * @param geneListFileName       the name of the gene list file
          * @param outputFileName       the name of the output file
          * @param outputType       either Group or Individual
          * @param analysisPath       the directory where the analysis files are stored
 	 * @throws RException if an error occurs when running R
          */
 
 	public void callOutputRawSpecificGene(String platform, 
 					String inputFileName,
                                         String version,
                                         String sampleFile,
 					String geneListFileName,
 					String outputFileName,
 					String outputType,
 					String analysisPath) throws RException {
 						
 		log.debug("in callOutputRawSpecificGene");
 		rFunction = "output.Raw.Specific.Gene";
 		functionArgs = new String[5];
 
                 functionArgs[0] = "InputFile = '" + inputFileName + "'";
                 functionArgs[1] = "GeneList = '"+ geneListFileName + "'";
                 functionArgs[2] = "Platform = '" + platform + "'";
 		functionArgs[3] = "OutputFile = '" + outputFileName + "'";
                 functionArgs[4] = "TypeOutput = '" + outputType + "'";
                 
                 if(inputFileName.endsWith(".h5")){
                             rFunction="output.Raw.Specific.Gene.HDF5";
                             functionArgs[0]=functionArgs[0]+", VersionPath='"+version+"', SampleFile='"+sampleFile+"'";
                 }
 
 		log.debug("functionArgs = "); myDebugger.print(functionArgs);
 
                 if ((rErrorMsg = 
 			myR_session.callR(this.getRFunctionDir(), rFunction, functionArgs, analysisPath, -99)) != null) {
 			String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
 			log.debug("after R call for outputRawSpecificGene, got errorMsg. It is "+errorMsg);
 			throw new RException(errorMsg);
 		}
 	}
         
         /**
          * Calls the outputRawSpecificGene function.
          * @param platform       the platform
          * @param inputFileName       the name of the inputFile
          * @param geneListFileName       the name of the gene list file
          * @param outputFileName       the name of the output file
          * @param outputType       either Group or Individual
          * @param analysisPath       the directory where the analysis files are stored
 	 * @throws RException if an error occurs when running R
          */
 
 	public void callOutputRawSpecificGeneBoth(String platform, 
 					String inputFileName,
                                         String version,
                                         String sampleFile,
 					String geneListFileName,
 					String outputFileNameIndiv,
                                         String outputFileNameGroup,
 					String analysisPath) throws RException {
 						
 		log.debug("in callOutputRawSpecificGene");
 		rFunction = "output.Raw.Specific.Gene.Both";
 		functionArgs = new String[5];
 
                 functionArgs[0] = "InputFile = '" + inputFileName + "'";
                 functionArgs[1] = "GeneList = '"+ geneListFileName + "'";
                 functionArgs[2] = "Platform = '" + platform + "'";
 		functionArgs[3] = "OutputFileIndiv = '" + outputFileNameIndiv + "'";
                 functionArgs[4] = "OutputFileGroup = '" + outputFileNameGroup + "'";
                 
                 if(inputFileName.endsWith(".h5")){
                             rFunction=rFunction+".HDF5";
                             functionArgs[0]=functionArgs[0]+", VersionPath='"+version+"', SampleFile='"+sampleFile+"'";
                 }
 
 		log.debug("functionArgs = "); myDebugger.print(functionArgs);
 
                 if ((rErrorMsg = 
 			myR_session.callR(this.getRFunctionDir(), rFunction, functionArgs, analysisPath, -99)) != null) {
 			String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
 			log.debug("after R call for outputRawSpecificGene, got errorMsg. It is "+errorMsg);
 			throw new RException(errorMsg);
 		}
 	}
 	
         /**
          * Calls the outputRawSpecificGene function.
          * @param platform       the platform
          * @param inputFileName       the name of the inputFile
          * @param geneListFileName       the name of the gene list file
          * @param outputFileName       the name of the output file
          * @param outputType       either Group or Individual
          * @param analysisPath       the directory where the analysis files are stored
 	 * @throws RException if an error occurs when running R
          */
 
 	public void callHeatMapOutputRawSpecificBoth(String platform, 
 					String inputFileName,
                                         String version,
                                         String sampleFile,
 					String geneListFileName,
 					String outputFileNameIndiv,
                                         String outputFileNameGroup,
 					String analysisPath,
                                         //String xmlFilename,
                                         String plotFilename,
                                         long threadNum) throws RException {
 						
 		log.debug("in callOutputRawSpecificGene");
 		rFunction = "Affymetrix.HeatMap.output.Specific.Gene.HDF5";
 		functionArgs = new String[7];
 
                 functionArgs[0] = "InputFile = '" + inputFileName + "'";
                 functionArgs[1] = "GeneList = '"+ geneListFileName + "'";
                 functionArgs[2] = "Platform = '" + platform + "'";
 		functionArgs[3] = "OutputFileIndiv = '" + outputFileNameIndiv + "'";
                 functionArgs[4] = "OutputFileGroup = '" + outputFileNameGroup + "'";
                 //functionArgs[5] = "XMLFileName = '" +xmlFilename+ "'";
                 functionArgs[5] = "plotFileName ='" +plotFilename+"'";
                 
                 if(inputFileName.endsWith(".h5")){
                             functionArgs[0]=functionArgs[0]+", VersionPath='"+version+"', SampleFile='"+sampleFile+"'";
                 }
 
 		log.debug("functionArgs = "); myDebugger.print(functionArgs);
                 
                 int ithreadNum=(int)threadNum;
 
                 if ((rErrorMsg = 
 			myR_session.callR(this.getRFunctionDir(), rFunction, functionArgs, analysisPath, ithreadNum)) != null) {
                     
 			String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
 			log.debug("after R call for outputRawSpecificGene, got errorMsg. It is "+errorMsg);
 			throw new RException(errorMsg);
 		}
 	}
         
         
         /**
          * Calls the outputGeneList function.
          * @param platform       the platform used for this dataset
          * @param inputRdataFileName       the name of the .Rdata file generated after filtering and statistics
          * @param groupsFileName       the name of file containing the group names and numbers 
          * @param analysisPath       the directory where the analysis files are stored
 	 * @throws RException if an error occurs when running R
          */
 
 	public void callOutputGeneList(String platform, 
 					String inputRdataFileName,
                                         String version,
                                         String sampleFile,
 					String groupsFileName,
 					String analysisPath) throws RException {
 						
 		log.debug("in callOutputGeneList");
                 
                 String verFDate=(String)session.getAttribute("verFilterDate");
                 String verFTime=(String)session.getAttribute("verFilterTime");
                 version=version+"/"+verFDate+"/"+verFTime;
                 log.debug("Version="+version);
                 
                 
 		if (platform.equals(new Dataset().AFFYMETRIX_PLATFORM) || platform.equals(new Dataset().CODELINK_PLATFORM)) {
 			rFunction = "outputGeneList";
                         functionArgs = new String[3];
 
                         functionArgs[0] = "InputFile = '" + inputRdataFileName + "'";
                         functionArgs[1] = "OutputGeneList = '" +
 							analysisPath +
                                                         platform +
                                                         ".genetext.output.txt'";
 			functionArgs[2] = "GroupNames = '" + groupsFileName + "'";
                         if(inputRdataFileName.endsWith(".h5'")||inputRdataFileName.endsWith(".h5")){
                             rFunction="outputGeneList.HDF5";
                             functionArgs[1]=functionArgs[1]+", VersionPath='"+version+"', SampleFile='"+sampleFile+"'";
                         }
 		} else if (platform.equals("cDNA")) {
                 	rFunction = "cDNA.outputGeneList";
                         functionArgs = new String[4];
 
                         functionArgs[0] = "InputFile = '" +
 							analysisPath +
                                                         "cDNA.multipleTesting.output.Rdata'";
                         functionArgs[1] = "OutputGeneList = '" +
                                                         analysisPath +
                                                         "cDNA.genetext.output.txt'";
                         functionArgs[2] = "OutputHeatMap = '" +
                                                         analysisPath +
                                                         "cDNA.genelist.heatmap.png'";
                         functionArgs[3] = "OutputRawData = '" +
                                                         analysisPath +
                                                         "cDNA.generaw.output.Rdata'";
 		}
 		log.debug("functionArgs = "); myDebugger.print(functionArgs);
 
                 if ((rErrorMsg = 
 			myR_session.callR(this.getRFunctionDir(), rFunction, functionArgs, analysisPath, -99)) != null) {
 			String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
 			log.debug("after R call for outputGeneList, got errorMsg. It is "+errorMsg);
 			throw new RException(errorMsg);
 		} else {
                 	log.debug("after R call for outputGeneList. Got no error.");
                 	String [] rawOutputFunctionArgs = null;
                         if (platform.equals(new Dataset().AFFYMETRIX_PLATFORM) || platform.equals(new Dataset().CODELINK_PLATFORM)) {
                         	rFunction = "outputGeneList.withRaw";
                                 rawOutputFunctionArgs = new String[2];
 
                                 rawOutputFunctionArgs[0] = "InputFile = '" + inputRdataFileName + "'";
                                 rawOutputFunctionArgs[1] = "OutputGeneList = '" +
                                 				analysisPath +
                                                                 "rawValues.txt'";
                                 if(inputRdataFileName.endsWith(".h5")){
                                     rFunction="outputGeneList.withRaw.HDF5";
                                     rawOutputFunctionArgs[0]=rawOutputFunctionArgs[0]+", VersionPath='"+version+"', SampleFile='"+sampleFile+"'";
                                 }
 			} else if (platform.equals("cDNA")) {
                         }
 
                         log.debug("rawOutputFunctionArgs = "); myDebugger.print(rawOutputFunctionArgs);
                         log.debug("after R call for outputGeneListWithRaw.");
                         if ((rErrorMsg = 
 				myR_session.callR(this.getRFunctionDir(), rFunction, rawOutputFunctionArgs, analysisPath, -99)) != null) {
 				String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
 				throw new RException(errorMsg);
 			}
 		}
 	}
 	
         /**
          * Calls the QTL.data.prep function.
          * @param groupingUserPhenotypeDir       the user-specific directory for the grouping files for this dataset version 
          * @param inputGenotypeFile       the name of the file containing the genotype information
          * @param phenotypeDataOutputFileName       the name of the file containing the phenotype information
          * @param QTLOutputFile       the output file name
 	 * @throws RException if an error occurs when running R
          */
 
 	public void callQTLDataPrep(String groupingUserPhenotypeDir,
 					String inputGenotypeFile,
 					String phenotypeDataOutputFileName,
 					String QTLOutputFile) throws RException {
 						
 		log.debug("in callQTLDataPrep");
 
                 rFunction = "QTL.data.prep";
                 functionArgs = new String[3];
                 functionArgs[0] = "InputGenoFile = '" + inputGenotypeFile + "'";
                 functionArgs[1] = "InputPhenoFile = '" + phenotypeDataOutputFileName + "'";
                 functionArgs[2] = "OutputFile = '" + QTLOutputFile + "'";
                 //log.debug("functionArgs = "); myDebugger.print(functionArgs);
 
                 if ((rErrorMsg = 
 			myR_session.callR(this.getRFunctionDir(), rFunction, functionArgs, groupingUserPhenotypeDir, -99)) != null) {
 			String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
 			log.debug("after R call for QTLDataPrep, got errorMsg. It is "+errorMsg);
 			throw new RException(errorMsg);
 		}
 	}
 
         /**
          * Calls the QTL.analysis function.
          * @param groupingUserPhenotypeDir       the user-specific directory for the grouping files for this dataset version 
          * @param inputFile       the output file from QTL.data.prep 
          * @param method       the method to use
          * @param weight       the weight to use
          * @param numPerm       the number of permutations to use
          * @param outputFile       the output file name
          * @param outputTxtFile       the output text file name
          * @param graphicFile       the graphic file name
 	 * @throws RException if an error occurs when running R
          */
 
 	public void callQTLAnalysis(String groupingUserPhenotypeDir,
 					String inputFile,
 					String method,
 					String weight,
 					String numPerm,
 					String outputFile,
 					String outputTxtFile,
 					String graphicFile) 
 				throws RException {
 						
 		log.debug("in callQTLAnalysis");
 
                 rFunction = "QTL.analysis";
                 functionArgs = new String[7];
                 functionArgs[0] = "InputFile = '" + inputFile + "'";
                 functionArgs[1] = "method = '" + method + "'";
                 functionArgs[2] = "weight = " + weight;
                 functionArgs[3] = "n.perm = " + numPerm;
                 functionArgs[4] = "OutputFile = '" + outputFile + "'";
                 functionArgs[5] = "OutputTXTFile = '" + outputTxtFile + "'";
                 functionArgs[6] = "GraphicFile = '" + graphicFile + "'";
                 //log.debug("functionArgs = "); myDebugger.print(functionArgs);
 
                 if ((rErrorMsg = 
 			myR_session.callR(this.getRFunctionDir(), rFunction, functionArgs, groupingUserPhenotypeDir, -99)) != null) {
 			String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
 			log.debug("after R call for QTLAnalysis, got errorMsg. It is "+errorMsg);
 			throw new RException(errorMsg);
 		}
 	}
 
         /**
          * Calls the QTL.summary function.
          * @param groupingUserPhenotypeDir       the user-specific directory for the grouping files for this dataset version 
          * @param inputFile       the output file from QTL.data.prep 
          * @param criteria       type of criteria used to select displayed results
          * @param threshold       the threshold value
          * @param confidenceType       the method used to calculate confidence interval
          * @param confidenceCriteria       the value used to calculate confidence interval for the method chosen
          * @param outputTxtFile       the output text file name
 	 * @throws RException if an error occurs when running R
          */
 
 	public void callQTLSummary(String groupingUserPhenotypeDir,
 					String inputFile,
 					String criteria,
 					String threshold,
 					String confidenceType,
 					Double confidenceCriteria,
 					String outputTxtFile) 
 				throws RException {
 						
 		log.debug("in callQTLSummary");
 
                 rFunction = "QTL.summary";
                 functionArgs = new String[6];
                 functionArgs[0] = "InputFile = '" + inputFile + "'";
                 functionArgs[1] = "select.type = '" + criteria + "'";
                 functionArgs[2] = "select.crit = '" + threshold + "'";
                 functionArgs[3] = "conf.type = '" + confidenceType + "'";
                 functionArgs[4] = "conf.crit = " + confidenceCriteria;
                 functionArgs[5] = "OutputTXTFile = '" + outputTxtFile + "'";
                 //log.debug("functionArgs = "); myDebugger.print(functionArgs);
 
                 if ((rErrorMsg = 
 			myR_session.callR(this.getRFunctionDir(), rFunction, functionArgs, groupingUserPhenotypeDir, -99)) != null) {
 			String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
 			log.debug("after R call for QTLSummary, got errorMsg. It is "+errorMsg);
 			throw new RException(errorMsg);
 		}
 	}
 
         /**
          * Calls the Phenotype.ImportTxt function.
          * @param phenotypeName       the name of the phenotype
          * @param publicIndicator       "TRUE" or "FALSE" to indicate whether the dataset is public or not
 	 * @throws RException if an error occurs when running R
          */
 
 	public void callPhenotypeImportTxt(String phenotypeName, String publicIndicator) throws RException {
 						
 		log.debug("in callPhenotypeImportTxt");
 		String userName = this.userLoggedIn.getUser_name();
 		String phenotypeDataFileName = this.selectedDatasetVersion.getPhenotypeDataFileName(userName, phenotypeName);
 		String phenotypeDataOutputFileName = this.selectedDatasetVersion.getPhenotypeDataOutputFileName(userName, phenotypeName);
 		String groupingUserPhenotypeDir = this.selectedDatasetVersion.getGroupingUserPhenotypeDir(userName, phenotypeName);
 		String exprStrainCountFileName = this.selectedDatasetVersion.getExpressionStrainCountFileName(userName, phenotypeName); 
 		String exprSummaryFileName = this.selectedDatasetVersion.getExpressionSummaryFileName(userName, phenotypeName); 
 		String exprGraphFileName = this.selectedDatasetVersion.getExpressionGraphFileName(userName, phenotypeName); 
 		String qtlStrainCountFileName = this.selectedDatasetVersion.getQTLStrainCountFileName(userName, phenotypeName); 
 		String qtlSummaryFileName = this.selectedDatasetVersion.getQTLSummaryFileName(userName, phenotypeName); 
 		String qtlGraphFileName = this.selectedDatasetVersion.getQTLGraphFileName(userName, phenotypeName); 
 
                 rFunction = "Phenotype.ImportTxt";
                 functionArgs = new String[10];
                 functionArgs[0] = "InputDataFile = '" + phenotypeDataFileName + "'";
                 functionArgs[1] = "Path= '" + groupingUserPhenotypeDir + "'";
                 functionArgs[2] = "OutputDataFile = '" + phenotypeDataOutputFileName + "'";
                 functionArgs[3] = "StrainNumberExprs = '" + exprStrainCountFileName + "'";
 		functionArgs[4] = "SummaryFileExprs = '" + exprSummaryFileName + "'";
 		functionArgs[5] = "GraphicFileExprs = '" + exprGraphFileName + "'"; 
                 functionArgs[6] = "StrainNumberQTL = '" + qtlStrainCountFileName + "'";
 		functionArgs[7] = "SummaryFileQTL = '" + qtlSummaryFileName + "'";
 		functionArgs[8] = "GraphicFileQTL = '" + qtlGraphFileName + "'"; 
 		functionArgs[9] = "Public = " + publicIndicator; 
                 //log.debug("functionArgs = "); myDebugger.print(functionArgs);
 
                 if ((rErrorMsg = 
 			myR_session.callR(this.getRFunctionDir(), rFunction, functionArgs, groupingUserPhenotypeDir, -99)) != null) {
 			String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
 			log.debug("after R call for PhenotypeImportTxt, got errorMsg. It is "+errorMsg);
 			throw new RException(errorMsg);
 		}
 	}
 
         /**
          * Calls the Masking.Missing.Strains function.
          * @param groupingUserPhenotypeDir       the user-specific directory for the grouping files for this dataset version 
          * @param myDatasetVersion       the new DatasetVersion object
          * @param phenotypeDataOutputFileName       the output file name
 	 * @throws RException if an error occurs when running R
          */
 
 	public void callMaskingMissingStrains(String groupingUserPhenotypeDir,
 					Dataset.DatasetVersion myDatasetVersion,
 					String phenotypeDataOutputFileName,boolean hdf5file) throws RException {
 						
 		log.debug("in callMaskingMissingStrains");
 
                 
 		rFunction = "Masking.Missing.Strains";
 		functionArgs = new String[3];
                 functionArgs[0] = "InputDataFile = '" + myDatasetVersion.getNormalized_RData_FileName() + "'"; 
 		functionArgs[1] = "InputPhenoFile = '" + phenotypeDataOutputFileName + "'";
                 functionArgs[2] = "OutputFile = '" + myDatasetVersion.getNormalizedForGrouping_RData_FileName(groupingUserPhenotypeDir) + "'";
                 //log.debug("functionArgs = "); myDebugger.print(functionArgs);
                 if(hdf5file){
                     rFunction=rFunction+".HDF5";
                     String inputFile=myDatasetVersion.getNormalized_RData_FileName();
                     inputFile=inputFile.substring(0,inputFile.lastIndexOf("/"));
                     inputFile=inputFile.substring(0,inputFile.lastIndexOf("/"))+"/Affy.NormVer.h5";
                     String outputFile=groupingUserPhenotypeDir+"Affy.NormVer.h5";
                     String outputFileSamp=groupingUserPhenotypeDir+"v"+myDatasetVersion.getVersion()+"_samples.txt";
                     String sampleFile=inputFile.substring(0,inputFile.lastIndexOf("/")+1)+"v"+myDatasetVersion.getVersion()+"_samples.txt";
                     functionArgs[0] = "InputDataFile = '" + inputFile + "'";
                     functionArgs[0]=functionArgs[0]+", VersionPath = 'v"+myDatasetVersion.getVersion() + "', SampleFile='"+sampleFile+"' ";
                     functionArgs[2] = "OutputFileHDF = '" + outputFile + "', OutputFileSamples= '"+outputFileSamp+"'";
                 }
                 if ((rErrorMsg = 
 			myR_session.callR(this.getRFunctionDir(), rFunction, functionArgs, groupingUserPhenotypeDir, -99)) != null) {
 			String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
 			log.debug("after R call for MaskingMissingStrains, got errorMsg. It is "+errorMsg);
 			throw new RException(errorMsg);
 		}
 	}
 
         /**
          * Calls the Preparing.To.Renormalize function.
          * @param datasetPath       the dataset path
          * @param phenotypeDataOutputFileName       the output file name
          * @param newMasterDir       the new dataset's master directory
 	 * @return Thread running the process in the background 
          */
 
 	public Thread callPreparingToRenormalize(String datasetPath,
 					String phenotypeDataOutputFileName,
 					String newMasterDir) {
 
 		log.debug("in callPreparingToRenormalize");
 
 		rFunction = "Preparing.To.Renormalize";
 		functionArgs = new String[3];
 		functionArgs[0] = "InputPhenoFile = '" + phenotypeDataOutputFileName + "'";
 		//
                 // Take the InputRawDataFile from the PUBLIC dataset's master directory
 		//
 		functionArgs[1] = "InputRawDataFile = '" +
                 			datasetPath + 
                                         "Combine.BioC.Binary.Rdata" + "'";
 		//
 		// Write it out to the new dataset's master directory
 		//
                 functionArgs[2] = "OutputRawDataFile = '" +
 					newMasterDir + 
                                         "Combine.BioC.Binary.Rdata'";
 		//log.debug("functionArgs = "); myDebugger.print(functionArgs);
 
                 Thread waitThread = new Thread(new Async_R_Session(
                                                         this.getRFunctionDir(), rFunction, functionArgs,
                                                         newMasterDir));
 
 		log.debug("starting thread to run Preparing.To.Renormalize.  It is named "+waitThread.getName());
                 waitThread.start();
 		return waitThread;
 
 	}
 
         /**
          * Runs the Affy Power Tools for exon datasets, or calls the PLATFORM.Import function and PLATFORM.QC. 
          * @param graphics       whether to generate images -- TRUE or FALSE 
          * @param waitThread       the thread to wait on
 	 * @return Thread running the process in the background 
          */
 
 	public Thread callQC(String graphics, 
 				Thread waitThread) {
 
         	String arrayDir = new edu.ucdenver.ccp.PhenoGen.data.Array().getArrayDataFilesLocation(userFilesRoot);
 		String datasetImagesDir = this.selectedDataset.getImagesDir();
                 Thread thisThread = null; 
                 Thread qcWaitThread = null; 
 
 		log.debug("in callQC. datasetName = " + selectedDataset.getName() + ", and array_type = "+selectedDataset.getArray_type());
 		log.debug("sessionID = " + this.session.getId());
 		log.debug("platform = " + selectedDataset.getPlatform());
 		//log.debug("mouse exonrray type = " + new edu.ucdenver.ccp.PhenoGen.data.Array().MOUSE_EXON_ARRAY_TYPE);
 		//log.debug("rat exonrray type = " + new edu.ucdenver.ccp.PhenoGen.data.Array().RAT_EXON_ARRAY_TYPE);
 		if (selectedDataset.getPlatform().equals(selectedDataset.AFFYMETRIX_PLATFORM) && 
 			new edu.ucdenver.ccp.PhenoGen.data.Array().EXON_ARRAY_TYPES.contains(selectedDataset.getArray_type())) {
 				// Only run the import process if there are no normalized versions
 				if (selectedDataset.getDatasetVersions().length == 0) {
 					log.debug("running the affy power tools for QC");
 					Thread aptThread = new Thread(new AsyncAffyPowerTools(selectedDataset, this.session)); 
 					aptThread.start();
 					log.debug("just started the thread to run apt for QC. It is called " + aptThread.getName());
                         		thisThread = callExonQC(graphics, aptThread);
 				} else {
 					log.debug("dataset has versions already, so calling exonQC with a null thread");
                         		thisThread = callExonQC(graphics, qcWaitThread);
 				}
 		} else { 
 			log.debug("this is not an exon dataset");
 
 			// Only run the import process if there are no normalized versions
 			if (selectedDataset.getDatasetVersions().length == 0) {
                         	Thread importThread = callImport(waitThread);
 				importThread.start();
 				qcWaitThread = importThread;
 			}
 
 			if (selectedDataset.getPlatform().equals(selectedDataset.AFFYMETRIX_PLATFORM)) {
                 		functionArgs = new String[7];
 
                         	rFunction ="Affymetrix.QC";
 
                         	functionArgs[0] = "Input.combine.BioC='" + selectedDataset.getPath() + "Combine.BioC.Binary.Rdata'";
                         	functionArgs[1] = "imagePath='"+ datasetImagesDir + "'";
                         	functionArgs[2] = "Summary.Figure='Summary.Figure.png'";
                         	functionArgs[3] = "RLE.Figure='RLE.Figure.png'";
                         	functionArgs[4] = "NUSE.Figure='NUSE.Figure.png'";
                         	functionArgs[5] = "graphics="+graphics;
                         	functionArgs[6] = "MAplotStats='MAstats.txt'";
 			
 			} else if (selectedDataset.getPlatform().equals(selectedDataset.CODELINK_PLATFORM)) {
                 		functionArgs = new String[7];
 
                         	rFunction ="CodeLink.QC";
 
                         	functionArgs[0] = "InputDataFile='" + selectedDataset.getPath() + "Combine.BioC.Binary.Rdata'";
                         	functionArgs[1] = "path='"+ arrayDir + "'";
                         	functionArgs[2] = "imagePath='"+ datasetImagesDir + "'";
                         	functionArgs[3] = "boxplotFile='CodeLink.BoxPlot.png'";
                         	functionArgs[4] = "cvImageFile='CodeLink.CV.png'";
                         	functionArgs[5] = "qcTableFile='CodeLink.QC.table.txt'";
                         	functionArgs[6] = "graphics="+graphics;
 			}
 
                 	thisThread = new Thread(new Async_R_Session(
                                                         this.getRFunctionDir(), 
 							rFunction, 
 							functionArgs,
                                                         selectedDataset.getPath(), 
 							qcWaitThread));
 		}
 
 		log.debug("returning thread to run PLATFORM.QC.  "+
 				"It is named "+thisThread.getName());
 		return thisThread;
 	}
 
         /**
          * Calls the Affymetrix.Exon.Import function 
          * @param graphics       whether to generate images -- TRUE or FALSE 
          * @param waitThread       the thread to wait on
 	 * @return Thread running the process in the background 
          */
 	public Thread callExonQC(String graphics, Thread waitThread) {
 
 		log.debug("in callExonQC. userLoggedIn = " + this.userLoggedIn.getUser_name() + ", selectedDataset = "+this.selectedDataset.getName() +
 				"." +
 				(waitThread != null ? "It is waiting on this thread: "+waitThread.getName() : "It is not waiting on any thread."));
 		String datasetImagesDir = this.selectedDataset.getImagesDir();
 
                 functionArgs = new String[8];
 
 		rFunction ="Affymetrix.Exon.QC";
 
                 functionArgs[0] = "qcPath = '" + selectedDataset.getPath() + "'";
                 functionArgs[1] = "FileListing = '" + selectedDataset.getFileListingName() + "'";
 		functionArgs[2] = "imagePath='"+ datasetImagesDir + "'";
                 functionArgs[3] = "Summary.Figure='Summary.Figure.png'";
                 functionArgs[4] = "RLE.Figure='RLE.Figure.png'";
                 functionArgs[5] = "MAD.resid.Figure='MAD.resid.Figure.png'";
                 functionArgs[6] = "graphics="+graphics;
                 functionArgs[7] = "MAplotStats='MAstats.txt'";
 
                 Thread thisThread = new Thread(new Async_R_Session(
                                                         this.getRFunctionDir(), 
 							rFunction, 
 							functionArgs,
                                                         selectedDataset.getPath(), 
 							waitThread));
 
 		log.debug("returning thread to run Affymetrix.Exon.QC.  "+
 				"It is named "+thisThread.getName());
 		return thisThread;
 	}
 
         /**
          * Calls the Affymetrix.Import function or the CodeLink.Import function.
          * @param waitThread       the thread to wait on
 	 * @return Thread running the process in the background 
          */
 	public Thread callImport(Thread waitThread) {
 
 		log.debug("in callImport. userLoggedIn = " + this.userLoggedIn.getUser_name() + ", selectedDataset = "+this.selectedDataset.getName());
         	String arrayDir = new edu.ucdenver.ccp.PhenoGen.data.Array().getArrayDataFilesLocation(userFilesRoot);
 
 		if (selectedDataset.getPlatform().equals(selectedDataset.AFFYMETRIX_PLATFORM)) {
                 	functionArgs = new String[4];
 
                         rFunction ="Affymetrix.Import";
 
                         functionArgs[0] = "import.path = '" + arrayDir + "'";
                         functionArgs[1] = "export.path = '" + selectedDataset.getPath() + "'";
                         functionArgs[2] = "FileListing = '" + selectedDataset.getFileListingName() + "'";
                         functionArgs[3] = "RawDataFile = 'Combine.BioC.Binary'";
 		} else if (selectedDataset.getPlatform().equals("cDNA")) {
                 	functionArgs = new String[3];
 
                         rFunction ="cDNA.ImportGpr";
 
                         functionArgs[0] = "path = '" + selectedDataset.getPath() + "'";
                         functionArgs[1] = "phenoDataFile = '" + selectedDataset.getFileListingName() + "'";
                         functionArgs[2] = "arrayDataObject = '"+
                                         selectedDataset.getPath() +
                                         "cDNA.ImportGpr.output.Rdata'";
 
 		} else if (selectedDataset.getPlatform().equals(selectedDataset.CODELINK_PLATFORM)) {
                 	functionArgs = new String[4];
 
                         rFunction ="CodeLink.Import";
 
                         functionArgs[0] = "import.path = '" + arrayDir + "'";
                         functionArgs[1] = "export.path = '" + selectedDataset.getPath() + "'";
                         functionArgs[2] = "phenoDataFile = '" + selectedDataset.getFileListingName() + "'";
                         functionArgs[3] = "arrayDataObject = 'Combine.BioC.Binary.Rdata'";
 		}
 
                 Thread thisThread = new Thread(new Async_R_Session(
                                                         this.getRFunctionDir(), 
 							rFunction, 
 							functionArgs,
                                                         selectedDataset.getPath(), 
 							waitThread));
 
 		log.debug("returning thread to run PLATFORM.Import.  "+
 				"It is named "+thisThread.getName());
 		return thisThread;
 	}
 
         /**
          * Calls the Affymetrix.Normalization function or the CodeLink.Normalization function.
          * @param myDatasetVersion       the new DatasetVersion object
          * @param normalize_method       the normalization method
          * @param grouping       the grouping variable
          * @param probeMask       'T' or 'F' to indicate whether the probe mask should be used
          * @param probeMaskFile	the name of the probe mask file 
          * @param codeLink_parameter1       the first parameter, used only for CodeLink normalization
          * @param waitThread       the thread to wait on
 	 * @return Thread running the process in the background 
          */
 	public Thread callNormalize(Dataset.DatasetVersion myDatasetVersion, 
 			String normalize_method, String grouping, String probeMask, 
 			String probeMaskFile,
 			String codeLink_parameter1, Thread waitThread) {
 
 		log.debug("in callNormalize");
 
                 if (myDatasetVersion.getDataset().getPlatform().equals(myDatasetVersion.getDataset().AFFYMETRIX_PLATFORM)) {
 			functionArgs = new String[7];
 
                         rFunction = "Affymetrix.Normalization";
 
                         // OutputCombinedBioCFile
                         functionArgs[0] = "OutputFile = '"+ myDatasetVersion.getNormalized_RData_FileName() + "'"; 
 
                         // OutputTabDelimitedFile
                         functionArgs[1] = "OutputCSVFile = '"+
 						myDatasetVersion.getVersion_path() + 
                                                 "v" + 
 						myDatasetVersion.getVersion() +
 						"_" +
                                                  "Affymetrix.Normalization.output.csv'";
 
                         // normalize.method
                         functionArgs[2] = "normalize.method = '" + normalize_method + "'";
 
                         //file that contains the raw expression data as .Rdata
                         functionArgs[3] = "InputDataFile = '" +
                                           	myDatasetVersion.getDataset().getPath() +  
                                                 "Combine.BioC.Binary.Rdata'";
 
                         // grouping
                         functionArgs[4] = "grouping = " + grouping;
 
                         // whether probeMask should be applied
                         functionArgs[5] = "mask = '" + probeMask + "'";
 
                         // the probeMask file
                         functionArgs[6] = "mask.file = '" + probeMaskFile + "'";
 		} else if (myDatasetVersion.getDataset().getPlatform().equals(myDatasetVersion.getDataset().CODELINK_PLATFORM)) {
 
                         functionArgs = new String[6];
 
                         rFunction = "CodeLink.Normalization";
                         // Input.arrayDataObject
                         functionArgs[0] = "Input.arrayDataObject = '"+
 						myDatasetVersion.getDataset().getPath() + 
                                                 "Combine.BioC.Binary.Rdata'";
 
                         // norm.method
                         functionArgs[1] = "norm.method = '" + normalize_method + "'";
 
                         // norm.parameter1
                         functionArgs[2] = "norm.para1 = '" + codeLink_parameter1 + "'";
 
                         // OutputFile
                         functionArgs[3] = "OutputFile = '"+ myDatasetVersion.getNormalized_RData_FileName() + "'"; 
 
                         // output.text
                         functionArgs[4] = "output.csv = '"+
 						myDatasetVersion.getVersion_path() + 
                                                 "v" + 
 						myDatasetVersion.getVersion() + 
 						"_" +
                                                 "CodeLink.Normalization.output.csv'";
                         // grouping
                         functionArgs[5] = "grouping = " + grouping;
 		} else if (myDatasetVersion.getDataset().getPlatform().equals("cDNA")) {
                 	functionArgs = new String[6];
 
                         rFunction = "cDNA.Normalization";
                         // Input.arrayDataObject
                         functionArgs[0] = "Input.arrayDataObject = '"+
 						myDatasetVersion.getVersion_path() + 
                                                 "cDNA.ImportGpr.output.Rdata'";
 
                         // substractBackground
                         functionArgs[1] = "substractBackground = T";
 
                         // norm.method
                         functionArgs[2] = "norm.method = '" + normalize_method + "'";
 
                         // output.binary
                         functionArgs[3] = "output.binary = '"+
 						myDatasetVersion.getVersion_path() + 
                                                 "cDNA.Normalization.output.Rdata'";
 
                         // output.text
                         functionArgs[4] = "output.text = '"+
 						myDatasetVersion.getVersion_path() + 
                                                 "v" + 
 						myDatasetVersion.getVersion() + 
 						"_" +
                                                 "cDNA.Normalization.output.tab'";
 
                         // output.excel
                         functionArgs[5] = "output.excel = '"+
 						myDatasetVersion.getVersion_path() + 
                                                 "v" + 
 						myDatasetVersion.getVersion() + 
 						"_" +
                                                 "cDNA.Normalization.output.xls'";
 		}
 
                 Thread thisThread = new Thread(new Async_R_Session(
                                                         this.getRFunctionDir(), 
 							rFunction, 
 							functionArgs,
                                                         myDatasetVersion.getVersion_path(), 
 							waitThread));
 
 		log.debug("returning thread to run PLATFORM.Normalization.  "+
 				"It is named "+thisThread.getName());
 		return thisThread;
 
 	}
 
         /**
          * Creates a Hashtable of key/value pairs for the multiple testing parameters.
          * @param fieldValues       a Hashtable containing the values of the fields entered by the user
 	 * @return a table of key/value pairs 
          */
 
 	public Hashtable setMTParameters(Hashtable fieldValues) {
 
 		log.debug("in setMTParameters");
 		String mt_method = (String) fieldValues.get("mt_method");
 		String stat_method = (String) fieldValues.get("stat_method");
 		log.debug("mt_method = "+mt_method + ", stat_method = "+stat_method);
 
 		Hashtable<String, String> mtParameters = new Hashtable<String, String>();
 
                 if (mt_method != null && mt_method.equals("Storey")) {
                         mtParameters.put("mtMethodName", "'" + mt_method + "'");
                         mtParameters.put("mtMethodText", mt_method);
                         mtParameters.put("mcc_parameter1", (String)fieldValues.get("pvalue"));
                         mtParameters.put("mcc_parameter2", (String)fieldValues.get("storey_parameter2"));
                         mtParameters.put("parameter1Text", "Alpha level threshold");
                         mtParameters.put("parameter2Text", "Method to estimate tuning");
                 } else if (mt_method != null && 
 					(mt_method.equals("Bonferroni") || 
 					mt_method.equals("Holm") || 
                 			mt_method.equals("Hochberg") ||
 					mt_method.equals("SidakSS") || 
 					mt_method.equals("SidakSD") || 
 					mt_method.equals("BH") || 
 					mt_method.equals("BY"))) {
                         mtParameters.put("mtMethodName", "'" + mt_method + "'");
                         mtParameters.put("mtMethodText", mt_method);
                         mtParameters.put("mcc_parameter1", (String)fieldValues.get("pvalue"));
                         mtParameters.put("parameter1Text", "Alpha level threshold");
                 } else if (mt_method != null && (mt_method.equals("maxT") || mt_method.equals("minP"))) {
                         mtParameters.put("mtMethodName", "'" + mt_method + "'");
                         mtParameters.put("mtMethodText", mt_method);
                         mtParameters.put("mcc_parameter1", (String)fieldValues.get("pvalue"));
                         mtParameters.put("mcc_parameter2", (String)fieldValues.get("testTypeDiv_parameter2"));
                         mtParameters.put("mcc_parameter3", (String)fieldValues.get("testTypeDiv_parameter3"));
                         mtParameters.put("mcc_parameter4", (stat_method.equals("parametric") ? "n" : "y"));
                         mtParameters.put("parameter1Text", "Alpha level threshold");
                         mtParameters.put("parameter2Text", "Type of test for permutation");
                         mtParameters.put("parameter3Text", "Number of permutations");
                         mtParameters.put("parameter4Text", "Test Type");
                 } else if (mt_method != null && mt_method.equals("No Adjustment")) {
                         mtParameters.put("mtMethodName", "'NoTest'");
                         mtParameters.put("mtMethodText", "NoTest");
                         mtParameters.put("mcc_parameter1", (String)fieldValues.get("pvalue"));
                         mtParameters.put("parameter1Text", "Alpha level threshold");
                 } else {
 		}
 		//log.debug("here mtParameters = "); myDebugger.print(mtParameters);
 
 		return mtParameters;
 	}
 
 	/** 
 	 * Creates records in the parameter_values table for the multiple testing 
 	 * parameters chosen.
 	 * @param mtParameters	a Hashtable containing the parameters selected
 	 * @param myParameterValue	a ParameterValue object with the create date and parameter_group_id
 	 *			 	already set 
 	 * @param dbConn	the database connection
 	 * @throws SQLException if a database error occurs
 	 */
 	public void createMTParameters (Hashtable mtParameters, ParameterValue myParameterValue, Connection dbConn)  throws SQLException {
 		String mtMethodText = (String) mtParameters.get("mtMethodText");
 		log.debug("in createMTParameters. mtMethodText = " + mtMethodText);
 
                 if ((String) mtParameters.get("mcc_parameter1") != null && 
                 	!((String) mtParameters.get("mcc_parameter1")).equals("-99")) {
                         myParameterValue.setCategory("Multiple Test Method -- " + mtMethodText);
                         myParameterValue.setParameter((String) mtParameters.get("parameter1Text"));
                         myParameterValue.setValue((String) mtParameters.get("mcc_parameter1"));
                         myParameterValue.createParameterValue(dbConn);
                 }
                 if ((String) mtParameters.get("mcc_parameter2") != null && 
                 	!((String) mtParameters.get("mcc_parameter2")).equals("-99")) {
                         myParameterValue.setCategory("Multiple Test Method -- " + mtMethodText);
                         myParameterValue.setParameter((String) mtParameters.get("parameter2Text"));
                         myParameterValue.setValue((String) mtParameters.get("mcc_parameter2"));
                         myParameterValue.createParameterValue(dbConn);
                 }
 
                 if ((String) mtParameters.get("mcc_parameter3") != null && 
                 	!((String) mtParameters.get("mcc_parameter3")).equals("-99")) {
                         myParameterValue.setCategory("Multiple Test Method -- " + mtMethodText);
                         myParameterValue.setParameter((String) mtParameters.get("parameter3Text"));
                         myParameterValue.setValue((String) mtParameters.get("mcc_parameter3"));
                         myParameterValue.createParameterValue(dbConn);
                 }
 
                 if ((String) mtParameters.get("mcc_parameter4") != null && 
                 	!((String) mtParameters.get("mcc_parameter4")).equals("-99")) {
                         myParameterValue.setCategory("Multiple Test Method -- " + mtMethodText);
                         myParameterValue.setParameter((String) mtParameters.get("parameter4Text"));
                         myParameterValue.setValue((String) mtParameters.get("mcc_parameter4"));
                         myParameterValue.createParameterValue(dbConn);
                 }
 	}
 
         
         public void setupFilter(Dataset ds, Dataset.DatasetVersion dsVer,String analysisType, int userID){
             //create Dataset_Filter_Stat entry
             String verFDate = (String) session.getAttribute("verFilterDate");
             String verFTime = (String) session.getAttribute("verFilterTime");
             String resetQ = "{call filter.reset(" + ds.getDataset_id() + "," + dsVer.getVersion() + "," + userID + ",?)}";
             try {
                 dsVer.createFilterStats(verFDate, verFTime, analysisType, userID, dbConn);
                 CallableStatement cs = dbConn.prepareCall(resetQ);
                 cs.setInt(1, 3);
                 cs.executeUpdate();
                 cs.close();
                 cs = dbConn.prepareCall(resetQ);
                 cs.setInt(1, 1);
                 cs.executeUpdate();
                 cs.close();
             } catch (SQLException e) {
                 log.error("Filter SQL Exception:", e);
             }
         }
         
         /**
          * Creates a Hashtable of key/value pairs for gene filtering parameters.
 	 * @param userLoggedIn	the User object of the user logged in
          * @param fieldValues       a Hashtable containing the values of the fields entered by the user
          * @param filterText       a Hashtable containing the values for the text displayed for filtering
          * @param selectedDataset       the Dataset being analyzed
 	 * @return a table of key/value pairs.
          */
 
 	public Hashtable setFilterParameters(User userLoggedIn, 
 						Hashtable<String, String> fieldValues, 
 						Hashtable<String, String> filterText, 
 						Dataset selectedDataset) {
 
 		log.debug("in setFilterParameters");
 		Hashtable<String, String> filterParameters = new Hashtable<String, String>();
 
 		String filterMethod = (String) fieldValues.get("filterMethod");
 
 		log.debug("filterMethod = "+filterMethod);
                 if (filterMethod != null &&
                         (filterMethod.equals("AffyControlGenesFilter") ||
                         filterMethod.equals("KeepAI,UTDDEST,NM") ||
                         filterMethod.equals("CodeLinkControlGenesFilter"))) {
                         if (selectedDataset.getPlatform().equals(selectedDataset.AFFYMETRIX_PLATFORM)) {
                                 filterParameters.put("filterMethodName", "'affy.control.genes'");
                                 filterParameters.put("filterMethodText", "AffyControlGenesFilter");
                         } else if (selectedDataset.getPlatform().equals("cDNA")) {
                                 filterParameters.put("filterMethodName", "'AI.UT.NM'");
                                 filterParameters.put("filterMethodText", "KeepAI,UTDDEST,NM");
                         } else if (selectedDataset.getPlatform().equals(selectedDataset.CODELINK_PLATFORM)) {
                                 filterParameters.put("filterMethodName", "'codelink.control.genes'");
                                 filterParameters.put("filterMethodText", "CodeLinkControlGenesFilter");
                         }
                         filterParameters.put("parameter1", "-99");
                         filterParameters.put("parameter2", "-99");
                         filterParameters.put("parameter1Text", "Parameter 1 is Null");
                         filterParameters.put("parameter2Text", "Parameter 2 is Null");
                         filterParameters.put("parameter1Value", "Null");
                         filterParameters.put("parameter2Value", "Null");
                 } else if (filterMethod != null && filterMethod.equals("CodeLinkCallFilter")) {
                         filterParameters.put("filterMethodName", "'absolute.call.filter'");
                         filterParameters.put("filterMethodText", "CodeLinkCallFilter");
                         if ((String) fieldValues.get("codeLinkCallOverall") == null ||
                         	((String) fieldValues.get("codeLinkCallOverall")).equals(""))  {
                                 filterParameters.put("parameter1", 
                                         "c(" +
                                         (String) fieldValues.get("codeLinkCallGroup1") +
                                         ", " +
                                         (String) fieldValues.get("codeLinkCallGroup2") +
                                         ")");
                         } else {
                                 filterParameters.put("parameter1", 
                                         "c(" +
                                         (String) fieldValues.get("codeLinkCallOverall") +
                                         ")");
                         }
                         filterParameters.put("parameter2", (String)fieldValues.get("codeLinkCallParameter2"));
                         filterParameters.put("parameter1Text", "Group parameters");
 			String parameter1Value = "";
                         if ((String) fieldValues.get("codeLinkCallOverall") == null ||
                         	((String) fieldValues.get("codeLinkCallOverall")).equals(""))  {
 
                                 parameter1Value = "Group 1: ";
                                 if (Integer.parseInt((String) fieldValues.get("codeLinkCallGroup1")) < 0) {
                                         parameter1Value = parameter1Value +
                                                 (String) filterText.get("codeLinkCallTopString") + " >= "+
                                                 (String) fieldValues.get("codeLinkCallGroup1");
                                 } else {
                                         parameter1Value = parameter1Value +
                                                 (String) filterText.get("codeLinkCallBottomString") + " >= "+
                                                 (String) fieldValues.get("codeLinkCallGroup1");
                                 }
                                 parameter1Value = parameter1Value + ", Group 2: ";
                                 if (Integer.parseInt((String) fieldValues.get("codeLinkCallGroup2")) < 0) {
                                         parameter1Value = parameter1Value +
                                                 (String) filterText.get("codeLinkCallTopString") + " >= "+
                                                 (String) fieldValues.get("codeLinkCallGroup2");
                                 } else {
                                         parameter1Value = parameter1Value +
                                                 (String) filterText.get("codeLinkCallBottomString") + " >= "+
                                                 (String) fieldValues.get("codeLinkCallGroup2");
                                 }
                         } else {
                                 parameter1Value = "Value for all Groups: ";
                                 if (Float.parseFloat((String) fieldValues.get("codeLinkCallOverall")) < 0) {
                                         parameter1Value = parameter1Value +
                                                 (String) filterText.get("codeLinkCallTopString") + " >= "+
                                                 Float.parseFloat((String) fieldValues.get("codeLinkCallOverall"))*-1;
                                 } else {
                                         parameter1Value = parameter1Value +
                                                 (String) filterText.get("codeLinkCallBottomString") + " >= "+
                                                 Float.parseFloat((String) fieldValues.get("codeLinkCallOverall"));
                                 }
                         }
 			filterParameters.put("parameter1Value", parameter1Value);
                         filterParameters.put("parameter2Text", "Keep Or Remove");
 			filterParameters.put("parameter2Value", 
 				(((String) filterParameters.get("parameter2")).equals("1") ? 
 					"Keep" : "Remove")); 
                 } else if (filterMethod != null &&
                         (filterMethod.equals("MAS5AbsoluteCallFilter") ||
                         filterMethod.equals("DABGPValueFilter") ||
                         filterMethod.equals("RemoveUTDDEST") ||
                         filterMethod.equals("GeneSpringCallFilter"))) {
 
                         if (selectedDataset.getPlatform().equals(selectedDataset.AFFYMETRIX_PLATFORM) ||
                                 selectedDataset.getPlatform().equals(selectedDataset.CODELINK_PLATFORM)) {
 				String parameter1Value = "";
                                 if (selectedDataset.getPlatform().equals(selectedDataset.AFFYMETRIX_PLATFORM)) {
                                         filterParameters.put("filterMethodName", "'absolute.call.filter'");
                 			if (new edu.ucdenver.ccp.PhenoGen.data.Array().EXON_ARRAY_TYPES.contains(selectedDataset.getArray_type())) {
                                         	filterParameters.put("filterMethodText", "DABGPValueFilter");
 					} else {
                                         	filterParameters.put("filterMethodText", "MAS5AbsoluteCallFilter");
 					}
                                 } else if (selectedDataset.getPlatform().equals(selectedDataset.CODELINK_PLATFORM)) {
                                         filterParameters.put("filterMethodName", "'gs.call.filter'");
                                         filterParameters.put("filterMethodText", "GeneSpringCallFilter");
                                 }
                                 if ((String) fieldValues.get("absCallOverall") == null ||
                         		((String) fieldValues.get("absCallOverall")).equals(""))  {
                                         filterParameters.put("parameter1", 
                                                 "c(" +
                                                 (String) fieldValues.get("absCallGroup1") +
                                                 ", " +
                                                 (String) fieldValues.get("absCallGroup2") +
                                                 ")");
                                 } else {
                                         filterParameters.put("parameter1", 
                                                 "c(" +
                                                 (String) fieldValues.get("absCallOverall") +
                                                 ")");
                                 }
                                 filterParameters.put("parameter2", (String)fieldValues.get("absCallParameter2"));
                                 filterParameters.put("parameter1Text", "Group parameters");
                                 if ((String) fieldValues.get("absCallOverall") == null ||
                         		((String) fieldValues.get("absCallOverall")).equals(""))  {
                                         parameter1Value = "Group 1: ";
                                         if (Integer.parseInt((String) fieldValues.get("absCallGroup1")) < 0) {
                                                 parameter1Value = parameter1Value +
                                                                 (String) filterText.get("absCallTopString") + " >= "+
                                                                 (String) fieldValues.get("absCallGroup1");
                                         } else {
                                                 parameter1Value = parameter1Value +
                                                                 (String) filterText.get("absCallBottomString") + " >= "+
                                                                 (String) fieldValues.get("absCallGroup1");
                                         }
                                         parameter1Value = parameter1Value + ", Group 2: ";
                                         if (Integer.parseInt((String) fieldValues.get("absCallGroup2")) < 0) {
                                                 parameter1Value = parameter1Value +
                                                                 (String) filterText.get("absCallTopString") + " >= "+
                                                                 (String) fieldValues.get("absCallGroup2");
                                         } else {
                                                 parameter1Value = parameter1Value +
                                                                 (String) filterText.get("absCallBottomString") + " >= "+
                                                                 (String) fieldValues.get("absCallGroup2");
                                         }
                                 } else {
                                         parameter1Value = "Value for all Groups: ";
                                         if (Float.parseFloat((String) fieldValues.get("absCallOverall")) < 0) {
                                                 parameter1Value = parameter1Value +
                                                         (String) filterText.get("absCallTopString") + " >= "+
                                                         Float.parseFloat((String) fieldValues.get("absCallOverall"))*-1;
                                         } else {
                                                 parameter1Value = parameter1Value +
                                                         (String) filterText.get("absCallBottomString") + " >= "+
                                                         Float.parseFloat((String) fieldValues.get("absCallOverall"));
                                         }
                                 }
 				filterParameters.put("parameter1Value", parameter1Value);
 
                                 filterParameters.put("parameter2Text", "Keep Or Remove");
 
 				filterParameters.put("parameter2Value", 
 					(((String) filterParameters.get("parameter2")).equals("1") ? 
 						"Keep" : "Remove")); 
                         } else if (selectedDataset.getPlatform().equals("cDNA")) {
                                 filterParameters.put("filterMethodName", "'rm.UT'");
                                 filterParameters.put("filterMethodText", "RemoveUTDDEST");
                                 filterParameters.put("parameter1", "-99");
                                 filterParameters.put("parameter2", "-99");
                                 filterParameters.put("parameter1Text", "Parameter 1 is Null");
                                 filterParameters.put("parameter2Text", "Parameter 2 is Null");
                                 filterParameters.put("parameter1Value", "Null");
                                 filterParameters.put("parameter2Value", "Null");
                         }
                 } else if (filterMethod != null && filterMethod.equals("NegativeControlFilter")) {
                         int negConGroup1 = -99;
                         int negConGroup2 = -99;
                         float negConOverall = -99;
                         boolean overall = false;
 			String parameter1Value = "";
 
                         if (selectedDataset.getPlatform().equals(selectedDataset.AFFYMETRIX_PLATFORM)) {
                                 filterParameters.put("filterMethodName", "'negative.probe.filter'");
                                 filterParameters.put("filterMethodText", "NegativeControlFilter");
                                 if ((String) fieldValues.get("negativeControlGroup1") != null &&
                                 	!((String) fieldValues.get("negativeControlGroup1")).equals("")) { 
                                         negConGroup1 = Integer.parseInt((String)fieldValues.get("negativeControlGroup1"));
                                         negConGroup2 = Integer.parseInt((String)fieldValues.get("negativeControlGroup2"));
                                 } else {
                                         negConOverall = Float.parseFloat((String)fieldValues.get("negativeControlOverall"));
                                         overall = true;
                                 }
                                 filterParameters.put("parameter2", (String)fieldValues.get("negativeControlParameter2"));
                         } else if (selectedDataset.getPlatform().equals(selectedDataset.CODELINK_PLATFORM)) {
                                 filterParameters.put("filterMethodName", "'negative.control'");
                                 filterParameters.put("filterMethodText", "NegativeControlFilter");
                                 if ((String) fieldValues.get("codeLinkNegativeControlGroup1") != null &&
                                 	!((String) fieldValues.get("codeLinkNegativeControlGroup1")).equals("")) { 
                                         negConGroup1 = Integer.parseInt((String)fieldValues.get("codeLinkNegativeControlGroup1"));
                                         negConGroup2 = Integer.parseInt((String)fieldValues.get("codeLinkNegativeControlGroup2"));
                                 } else {
                                         negConOverall = Float.parseFloat((String)fieldValues.get("codeLinkNegativeControlOverall"));
                                         overall = true;
                                 }
                                 filterParameters.put("parameter2", (String)fieldValues.get("codeLinkNegativeControlParameter2"));
                                 filterParameters.put("parameter3", (String) fieldValues.get("negParam"));
                                 filterParameters.put("parameter3Text", "Trim Percentage");
                                 filterParameters.put("parameter3Value", (String) filterParameters.get("parameter3"));
                         }
                         if (!overall) {
                                 filterParameters.put("parameter1", 
                                         "c(" +
                                         negConGroup1 +
                                         ", " +
                                         negConGroup2 +
                                         ")");
                         } else {
                                 filterParameters.put("parameter1", 
                                         "c(" +
                                         negConOverall +
                                         ")");
                         }
                         filterParameters.put("parameter1Text", "Group parameters");
                         if (!overall) {
                                 parameter1Value = "Group 1: ";
                                 if (negConGroup1 < 0) {
                                         parameter1Value = parameter1Value +
                                                         (String) filterText.get("negativeControlTopString") + " >= "+
                                                         negConGroup1;
                                 } else {
                                         parameter1Value = parameter1Value +
                                                         (String) filterText.get("negativeControlBottomString") + " >= "+
                                                         negConGroup1;
                                 }
                                 parameter1Value = parameter1Value + ", Group 2: ";
                                 if (negConGroup2 < 0) {
                                         parameter1Value = parameter1Value +
                                                         (String) filterText.get("negativeControlTopString") + " >= "+
                                                         negConGroup2;
                                 } else {
                                         parameter1Value = parameter1Value +
                                                         (String) filterText.get("negativeControlBottomString") + " >= "+
                                                         negConGroup2;
                                 }
                         } else {
                                 parameter1Value = "Value for all Groups: ";
                                 if (negConOverall < 0) {
                                         parameter1Value = parameter1Value +
                                                         (String) filterText.get("negativeControlTopString") + " >= "+
                                                         negConOverall;
                                 } else {
                                         parameter1Value = parameter1Value +
                                                         (String) filterText.get("negativeControlBottomString") + " >= "+
                                                         negConOverall;
                                 }
                         }
 			filterParameters.put("parameter1Value", parameter1Value);
                         filterParameters.put("parameter2Text", "Keep Or Remove");
 
 			filterParameters.put("parameter2Value", 
 					(((String) filterParameters.get("parameter2")).equals("1") ? 
 						"Keep" : "Remove")); 
                 } else if (filterMethod != null &&
                         (filterMethod.equals("MedianFilter") ||
                         filterMethod.equals("RemoveEST"))) {
                         if (selectedDataset.getPlatform().equals(selectedDataset.AFFYMETRIX_PLATFORM) ||
                                 selectedDataset.getPlatform().equals(selectedDataset.CODELINK_PLATFORM)) {
                                 filterParameters.put("filterMethodName", "'median.filter'");
                                 filterParameters.put("filterMethodText", "MedianFilter");
                                 filterParameters.put("parameter1", (String)fieldValues.get("filterThreshold"));
                                 filterParameters.put("parameter2", ".1");
                                 filterParameters.put("parameter1Text", "Filter Threshold");
                                 filterParameters.put("parameter1Value", (String) filterParameters.get("parameter1"));
                                 filterParameters.put("parameter2Text", "FDR Threshold (Hard-coded value)");
                                 filterParameters.put("parameter2Value", 
 						(String) filterParameters.get("parameter2"));
                         } else if (selectedDataset.getPlatform().equals("cDNA")) {
                                 filterParameters.put("filterMethodName", "'rm.EST'");
                                 filterParameters.put("filterMethodText", "RemoveEST");
                                 filterParameters.put("parameter1", "-99");
                                 filterParameters.put("parameter2", "-99");
                                 filterParameters.put("parameter1Text", "Parameter 1 is Null");
                                 filterParameters.put("parameter2Text", "Parameter 2 is Null");
                                 filterParameters.put("parameter1Value", "Null");
                                 filterParameters.put("parameter2Value", "Null");
                         }
                 } else if (filterMethod != null &&
                         (filterMethod.equals("CoefficientVariationFilter") ||
                         filterMethod.equals("FilterUsingFlagValues"))) {
                         if (selectedDataset.getPlatform().equals(selectedDataset.AFFYMETRIX_PLATFORM) ||
                                 selectedDataset.getPlatform().equals(selectedDataset.CODELINK_PLATFORM)) {
                                 filterParameters.put("filterMethodName", "'coefficient.variation'");
                                 filterParameters.put("filterMethodText", "CoefficientVariationFilter");
                                 if ((String) fieldValues.get("coeffOverall") == null || 
 					((String) fieldValues.get("coeffOverall")).equals("")) {
                                         filterParameters.put("parameter1", 
                                                 "c(" +
                                                 (String) fieldValues.get("coeffGroup1") +
                                                 ", " +
                                                 (String) fieldValues.get("coeffGroup2") +
                                                 ")");
                                 } else {
                                         filterParameters.put("parameter1", 
                                                 "c(" +
                                                 (String) fieldValues.get("coeffOverall") +
                                                 ")");
                                 }
                                 filterParameters.put("parameter2", (String)fieldValues.get("coeffParameter2"));
                                 filterParameters.put("parameter1Text", "Group Parameters");
                                 if ((String) fieldValues.get("coeffOverall") == null ||
 					((String) fieldValues.get("coeffOverall")).equals("")) {
                                         filterParameters.put("parameter1Value", 
 						"Group 1: " + (String) fieldValues.get("coeffGroup1") + 
 						", Group2: " + (String) fieldValues.get("coeffGroup2"));
                                 } else {
                                         filterParameters.put("parameter1Value", 
 						"Value for all Groups: " + (String) fieldValues.get("coeffOverall"));
                                 }
                                 filterParameters.put("parameter2Text", "Either 'and' or 'or'");
 				filterParameters.put("parameter2Value", 
 					(((String) filterParameters.get("parameter2")).equals("1") ? 
 						"or" : "and")); 
                         } else if (selectedDataset.getPlatform().equals("cDNA")) {
                                 filterParameters.put("filterMethodName", "'rm.flag'");
                                 filterParameters.put("filterMethodText", "FilterUsingFlagValues");
                                 if ((String) fieldValues.get("flagOverall") == null ||
 					((String) fieldValues.get("flagOverall")).equals("")) {
                                         filterParameters.put("parameter1", 
                                                 "c(" +
                                                 (String) fieldValues.get("flagGroup1") +
                                                 ", " +
                                                 (String) fieldValues.get("flagGroup2") +
                                                 ")");
                                 } else {
                                         filterParameters.put("parameter1", 
                                                 "c(" +
                                                 (String) fieldValues.get("flagOverall") +
                                                 ")");
                                 }
                                 filterParameters.put("parameter2", (String)fieldValues.get("flagParameter2"));
                                 filterParameters.put("parameter1Text", "Group parameters");
                                 if ((String) fieldValues.get("flagOverall") == null ||
 					((String) fieldValues.get("flagOverall")).equals("")) {
                                         filterParameters.put("parameter1Value", "Group 1: " +
                                                         (String) filterText.get("rmFlagTopString") + ">= "+
                                                         (String) fieldValues.get("flagGroup1") +
                                                         ", Group 2: " +
                                                         (String) filterText.get("rmFlagTopString") + ">= "+
                                                         (String) fieldValues.get("flagGroup2"));
                                 } else {
                                         filterParameters.put("parameter1Value", "Value for all Groups: " +
                                                         (String) filterText.get("rmFlagTopString") + ">= "+
                                                         (String) fieldValues.get("flagOverall"));
                                 }
                         }
                 } else if (filterMethod != null && filterMethod.equals("HeritabilityFilter")) {
 			filterParameters.put("filterMethodName", "'heritability'");
                         filterParameters.put("filterMethodText", "HeritabilityFilter");
 
 			// If this is a CodeLink dataset, then the user doesn't have a choice of panels, but if it's 
 			// an affy, they can choose heritability of BXD or Inbred for MOE430v2 chips, or LXS or HXB for Exon chips
 			String whichPanel = "";
 			if (selectedDataset.getPlatform().equals(selectedDataset.AFFYMETRIX_PLATFORM)) { 
 				whichPanel = (String) fieldValues.get("heritabilityPanel");
 			} else {
 				whichPanel = "HXB"; 
 			}
 			String datasetName = (whichPanel.equals("BXD") ? selectedDataset.BXDRI_DATASET_NAME :
 						(whichPanel.equals("Inbred") ? selectedDataset.INBRED_DATASET_NAME :
 						(whichPanel.equals("HXB") ? selectedDataset.HXBRI_DATASET_NAME : 
 						(whichPanel.startsWith("LXS") ? selectedDataset.LXSRI_DATASET_NAME : 
 						(whichPanel.startsWith("HXB_BXH.brain") ? selectedDataset.HXBRI_BRAIN_EXON_DATASET_NAME : 
 						(whichPanel.startsWith("HXB_BXH.heart") ? selectedDataset.HXBRI_HEART_EXON_DATASET_NAME : 
 						(whichPanel.startsWith("HXB_BXH.liver") ? selectedDataset.HXBRI_LIVER_EXON_DATASET_NAME : 
 						(whichPanel.startsWith("HXB_BXH.bat") ? selectedDataset.HXBRI_BROWN_ADIPOSE_EXON_DATASET_NAME : 
 						""))))))));
 
 			String resourcesDir = selectedDataset.getResourcesDir(this.publicDatasets, datasetName); 
                         String fileName = "'" + resourcesDir + whichPanel + ".herits.Rdata'";
 
 			log.debug("fileName = "+fileName);
                         if(new edu.ucdenver.ccp.PhenoGen.data.Array().EXON_ARRAY_TYPES.contains(selectedDataset.getArray_type())){
                             filterParameters.put("parameter1", whichPanel); 
                             filterParameters.put("parameter2", (String)fieldValues.get("heritabilityLevel"));
 
                             filterParameters.put("parameter1Text", "Which Panel");
                             filterParameters.put("parameter1Value", whichPanel);
 
                             filterParameters.put("parameter2Text", "Heritability Level");
                             filterParameters.put("parameter2Value", (String)fieldValues.get("heritabilityLevel"));
                         }else{
                             filterParameters.put("parameter1", fileName); 
                             filterParameters.put("parameter2", (String)fieldValues.get("heritabilityLevel"));
 
                             filterParameters.put("parameter1Text", "Which Panel");
                             filterParameters.put("parameter1Value", whichPanel);
 
                             filterParameters.put("parameter2Text", "Heritability Level");
                             filterParameters.put("parameter2Value", (String)fieldValues.get("heritabilityLevel"));
                         }
 
                 } else if (filterMethod != null && filterMethod.equals("GeneListFilter")) {
                         if (selectedDataset.getPlatform().equals(selectedDataset.AFFYMETRIX_PLATFORM) ||
                                 selectedDataset.getPlatform().equals(selectedDataset.CODELINK_PLATFORM)) {
 				log.debug("GLParameter2 = " + (String) fieldValues.get("geneListParameter2"));
 				log.debug("GLName = " + (String) fieldValues.get("geneListName"));
 
                                 filterParameters.put("filterMethodName", "'gene.list'");
                                 filterParameters.put("filterMethodText", "GeneListFilter");
                                 filterParameters.put("parameter1", 
 					"'" + (String) fieldValues.get("geneListFileName") + "'");
                                 filterParameters.put("parameter2", (String)fieldValues.get("geneListParameter2"));
                                 filterParameters.put("parameter3", (String) fieldValues.get("translateGeneList"));
                                 filterParameters.put("parameter1Text", "Gene List Name");
 
                                 filterParameters.put("parameter1Value", (String) fieldValues.get("geneListName"));
                                 filterParameters.put("parameter2Text", "Action");
 				log.debug("param2 = " + (String) filterParameters.get("parameter2"));
 				filterParameters.put("parameter2Value", 
 					(((String) filterParameters.get("parameter2")).equals("1") ? 
 						"keep" : "remove")); 
                                 filterParameters.put("parameter3Text", "translateGeneList");
 				log.debug("param3 = " + (String) fieldValues.get("translateGeneList"));
 				filterParameters.put("parameter3Value", (String) fieldValues.get("translateGeneList"));
                                 log.debug("translateGeneList = "+(String) fieldValues.get("translateGeneList"));
 				/* doesn't work for CodeLink -- figure out how to create the parameter record
 				without having to pass it to the R program 
                                 filterParameters.put("parameter3", (String) fieldValues.get("translateGeneList"));
                                 filterParameters.put("parameter3Text", "Include All Probes(ets)");
                                 filterParameters.put("parameter3Value", (String) fieldValues.get("translateGeneList"));
 				*/
                         }
                 } else if (filterMethod != null && filterMethod.equals("QTLFilter")) {
                         if (selectedDataset.getPlatform().equals(selectedDataset.AFFYMETRIX_PLATFORM) ||
                                 selectedDataset.getPlatform().equals(selectedDataset.CODELINK_PLATFORM)) {
 				log.debug("qtlListFileName = "+(String) fieldValues.get("qtlListFileName"));
 				log.debug("qtlListName = "+(String) fieldValues.get("qtlListName"));
                                 if(new edu.ucdenver.ccp.PhenoGen.data.Array().EXON_ARRAY_TYPES.contains(selectedDataset.getArray_type())){
                                     filterParameters.put("filterMethodName", "'eQTL'");
                                     filterParameters.put("parameter3", (String) fieldValues.get("tissue"));
                                 }else{
                                     filterParameters.put("filterMethodName", "'gene.list'");
                                 }
                                 filterParameters.put("filterMethodText", "QTLFilter");
                                 filterParameters.put("parameter1", 
 					"'" + (String) fieldValues.get("qtlListFileName") + "'");
 				// 1 is 'keep'
                                 filterParameters.put("parameter2", "1");
                                 filterParameters.put("parameter1Text", "QTL List Name");
 
                                 filterParameters.put("parameter1Value", (String) fieldValues.get("qtlListName"));
                                 filterParameters.put("parameter2Text", "Action");
 				filterParameters.put("parameter2Value", "keep");
                                 filterParameters.put("parameter3Text", "Tissue");
 				filterParameters.put("parameter3Value", (String) fieldValues.get("tissue"));
                         }
                 } else if (filterMethod != null && (filterMethod.equals("VariationFilter") ||
                                 filterMethod.equals("FoldChangeFilter"))) {
                         if (selectedDataset.getPlatform().equals(selectedDataset.AFFYMETRIX_PLATFORM) ||
                                 selectedDataset.getPlatform().equals(selectedDataset.CODELINK_PLATFORM)) {
                                 if (filterMethod.equals("VariationFilter")) {
                                         filterParameters.put("filterMethodName", "'variation'");
                                 } else {
                                         filterParameters.put("filterMethodName", "'fold.change'");
                                 }
                                 filterParameters.put("filterMethodText", filterMethod);
                                 String keepPercentage = (String)fieldValues.get("keepPercentage");
                                 String keepNumber = (String)fieldValues.get("keepNumber");
                                 filterParameters.put("parameter1", 
 					(keepPercentage.equals("") ? keepNumber : 
 						Double.toString(Double.parseDouble(keepPercentage)/100)));
                                 filterParameters.put("parameter1Text", 
 						(!keepPercentage.equals("") ? "Percentage to keep" : 
 						"Number to keep"));
                                 filterParameters.put("parameter1Value", 
 					(!keepPercentage.equals("") ? keepPercentage : keepNumber));
                                 filterParameters.put("parameter2", "-99");
                         }
                 } else if (filterMethod != null && filterMethod.equals("PercentGreaterThanBackgroundPlus2SDFilter")) {
                         if (selectedDataset.getPlatform().equals("cDNA")) {
                                 // bg2sd -- only used by cDNA
                                 filterParameters.put("filterMethodName", "'rm.BG2SD'");
                                 filterParameters.put("filterMethodText", "PercentGreaterThanBackgroundPlus2SDFilter");
                                 if ((String) fieldValues.get("bg2sdOverall") == null ||
 					((String) fieldValues.get("bg2sdOverall")).equals("")) {
                                         filterParameters.put("parameter1", "c(" +
                                                         (String) fieldValues.get("bg2sdGroup1") +
                                                         ", " +
                                                         (String) fieldValues.get("bg2sdGroup2") +
                                                         ")");
                                 } else {
                                         filterParameters.put("parameter1", "c(" +
                                                         (String) fieldValues.get("bg2sdOverall") +
                                                         ")");
                                 }
                                 filterParameters.put("parameter2", (String)fieldValues.get("bg2sdThreshold"));
                                 filterParameters.put("parameter1Text", "Group parameters");
                                 if ((String) fieldValues.get("bg2sdOverall") == null ||
 					((String) fieldValues.get("bg2sdOverall")).equals("")) {
                                         filterParameters.put("parameter1Value", "Group 1: " +
                                                 (String) filterText.get("bg2sdTopString") + " >= "+
                                                 (String) fieldValues.get("bg2sdGroup1") +
                                                 ", Group 2: " +
                                                 (String) filterText.get("bg2sdTopString") + " >= "+
                                                 (String) fieldValues.get("bg2sdGroup2"));
                                 } else {
                                         filterParameters.put("parameter1Value", "Value for all Groups: " +
                                                 (String) filterText.get("bg2sdTopString") + " >= "+
                                                 (String) fieldValues.get("bg2sdOverall"));
                                 }
                         }
                 } else if (filterMethod != null && filterMethod.equals("LowIntensityFilter")) {
                         // low_int -- only used by cDNA
                         filterParameters.put("filterMethodName", "'rm.low.int'");
                         filterParameters.put("filterMethodText", "LowIntensityFilter");
                         if ((String) fieldValues.get("lowIntOverall") == null ||
 				((String) fieldValues.get("lowIntOverall")).equals("")) {
                                 filterParameters.put("parameter1", "c(" +
                                                 (String) fieldValues.get("lowIntGroup1") +
                                                 ", " +
                                                 (String) fieldValues.get("lowIntGroup2") +
                                                 ")");
                         } else {
                                 filterParameters.put("parameter1", "c(" +
                                                 (String) fieldValues.get("lowIntOverall") +
                                                 ")");
                         }
                         filterParameters.put("parameter2", (String)fieldValues.get("lowIntThreshold"));
                         filterParameters.put("parameter1Text", "Group parameters");
                         if ((String) fieldValues.get("lowIntOverall") == null ||
 				((String) fieldValues.get("lowIntOverall")).equals("")) {
                                 filterParameters.put("parameter1Value", "Group 1: " +
                                                 (String) filterText.get("lowIntTopString") + " >= "+
                                                 (String) fieldValues.get("lowIntGroup1") +
                                                 ", Group 2: " +
                                                 (String) filterText.get("lowIntTopString") + " >= "+
                                                 (String) fieldValues.get("lowIntGroup2"));
                         } else {
                                 filterParameters.put("parameter1Value", "Value for all Groups: " +
                                                 (String) filterText.get("lowIntTopString") + " >= "+
                                                 (String) fieldValues.get("lowIntOverall"));
                         }
                         filterParameters.put("parameter3", (String)fieldValues.get("parameter3"));
                 } else {
                 }
 
 		return filterParameters;
 	}
 
         /**
          * Creates a Hashtable of key/value pairs for text used in filtering parameters
 	 * @return a table of key/value pairs.
          */
 
 	public Hashtable getFilterText() {
 
 		log.debug("in getFilterText");
 		
 		Hashtable<String, String> filterText = new Hashtable<String, String>();
 		filterText.put("absCallTopString", "Probes absent in # of the samples");
 		filterText.put("absCallBottomString", "Probes present in # of the samples");
 		filterText.put("negativeControlTopString", "Probes below detection limits in # of the samples");
 		filterText.put("negativeControlBottomString", "Probes above detection limits in # of the samples");
 		filterText.put("codeLinkCallTopString", "Probes low in # of the samples");
 		filterText.put("codeLinkCallBottomString", "Probes good in # of the samples");
 		filterText.put("rmFlagTopString", "Remove Probes with # Negative Flags");
 		filterText.put("bg2sdTopString", "Remove Probes with # B635.2SD or B532.2SD Values Greater Than Threshold");
 		filterText.put("lowIntTopString", "Remove Probes with # F635Mean/F635Med or F532Mean/F532Med Values Less Than Threshold");
 
 		return filterText;
 	}
 
         /**
          * Runs the normalization process.
 	 * @param normalize_method	the method to use
 	 * @param version_name	the name given by the user
 	 * @param grouping_id	the identifier of the record that contains the grouping used
 	 * @param probeMask	'T' or 'F' to indicate whether the probeMask should be applied 
 	 * @param codeLink_parameter1	 a parameter used for codelink normalizations
 	 * @param versionType	 the type of version
 	 * @param analysis_level	used only for exon arrays -- either 'probeset' or 'transcript'
 	 * @param annotation_level	used only for exon arrays -- either 'core' or 'extended' or 'full'
 	 * @param waitThread	 the thread to wait on
 	 * @return nextVersionNumber 	the number of the version being created
 	 * @throws SQLException	if a database error occurs
 	 * @throws ErrorException	if an application error occurs
          */
 
 	public int doNormalization(String normalize_method,
 					String version_name,
 					int grouping_id, 
 					String probeMask,
 					String codeLink_parameter1,
 					String versionType, 
 					Thread waitThread, 
 					String analysis_level,
 					String annotation_level,
                                         boolean outputHDF5) 
 					throws SQLException, ErrorException {
 
 		log.debug("in doNormalization");
 
                 int nextVersionNumber = 0;
                 Thread thread2 = null;
                 Thread thread3 = null;
                 Thread thread4 = null;
                 Thread thread5 = null;
                 Thread thread6 = null;
                 Thread nextWaitThread = null;
 		ParameterValue myParameterValue = new ParameterValue();
 
                 //
                 // Check to see if this combination already exists
                 // For Affy and cDNA, codeLink_parameter1 will be
                 // null
                 //
                 String alreadyExists = selectedDataset.checkNormalizationExists(
                                         normalize_method,
                                         grouping_id,
                                         probeMask,
 					analysis_level,
 					annotation_level,
                                         codeLink_parameter1,
                                         dbConn);
 
                 if (!alreadyExists.equals("")) {
                 	log.debug("alreadyExists is true");
                         //Error -- "Normalization already exists"
 			throw new ErrorException("EXP-004",  "See version '" + alreadyExists + "'.");
 		} else {
                 	nextVersionNumber = selectedDataset.getNextVersion(dbConn);
                 }
                 if (nextVersionNumber == 0) {
                 	nextVersionNumber = 1;
                 }
                 Dataset.DatasetVersion newDatasetVersion = selectedDataset.new DatasetVersion(nextVersionNumber);
 
                 if (!myFileHandler.createDir(newDatasetVersion.getVersion_path())) {
                 	log.debug("error creating versionDir directory in doNormalization");
 			throw new ErrorException("SYS-001");
 		} else {
                 	log.debug("no problems creating dataset directory in doNormalization");
                 }
 		Dataset.Group[] myGroups = selectedDataset.getGroupsInGrouping(grouping_id, dbConn);
 
 		//log.debug("myGroups = "); myDebugger.print(myGroups);
 		//
                 // Perform the following steps:
                 // 1. Create a dataset version record.
                 // 2. Create a thread to run the Normalization R process.
                 // 4. Create a thread which updates the dataset_versions record
                 //      to visible.  Also send an email to the user.
 
 
 		/*************************** STEP 1 ****************************/
                 // 1. Create a dataset version record.
                 newDatasetVersion.setVersion_name(version_name);
                 newDatasetVersion.setVisible(0);
                 newDatasetVersion.setGrouping_id(grouping_id);
                 newDatasetVersion.setVersion_type(versionType);
                 newDatasetVersion.createDatasetVersion(dbConn);
 
                 //
                 // Create a master parameter group 
                 //
 
                 int parameterGroupID = myParameterValue.createParameterGroup(
                                                 selectedDataset.getDataset_id(), nextVersionNumber, 1, dbConn);
 
 		log.debug("parameterGroupID = "+parameterGroupID);
                 log.debug("NORM ARRAY TYPE:"+selectedDataset.getArray_type());
                 myParameterValue.setCreate_date();
 
 		if (selectedDataset.getPlatform().equals(selectedDataset.AFFYMETRIX_PLATFORM)) {
                 	myParameterValue.setParameter_group_id(parameterGroupID);
                         myParameterValue.setCategory("Data Normalization");
                         myParameterValue.setParameter("Normalization Method");
                         myParameterValue.setValue(normalize_method);
                         myParameterValue.createParameterValue(dbConn);
 			log.debug("just created normalization method parameter = ");
 
                         myParameterValue.setParameter("Probe Mask Applied");
                         myParameterValue.setValue(probeMask);
                         myParameterValue.createParameterValue(dbConn);
 			log.debug("just created probemask parameter = ");
                 	if (new edu.ucdenver.ccp.PhenoGen.data.Array().EXON_ARRAY_TYPES.contains(selectedDataset.getArray_type())) {
                         	myParameterValue.setParameter("Analysis Level");
                         	myParameterValue.setValue(analysis_level);
                         	myParameterValue.createParameterValue(dbConn);
                         	myParameterValue.setParameter("Annotation Level");
                         	myParameterValue.setValue(annotation_level);
                         	myParameterValue.createParameterValue(dbConn);
 			}else{
                             outputHDF5=false;
                         }
 		} else if (selectedDataset.getPlatform().equals("cDNA")) {
                 	myParameterValue.setParameter_group_id(parameterGroupID);
                         myParameterValue.setCategory("Data Normalization");
                         myParameterValue.setParameter("cDNA Normalization Method");
                         myParameterValue.setValue(normalize_method);
                         myParameterValue.createParameterValue(dbConn);
                         outputHDF5=false;
 		} else if (selectedDataset.getPlatform().equals(selectedDataset.CODELINK_PLATFORM)) {
 			myParameterValue.setParameter_group_id(parameterGroupID);
                         myParameterValue.setCategory("Data Normalization");
                         myParameterValue.setParameter("CodeLink Normalization Method");
                         myParameterValue.setValue(normalize_method);
                         myParameterValue.createParameterValue(dbConn);
 
 			myParameterValue.setParameter_group_id(parameterGroupID);
                         myParameterValue.setCategory("Data Normalization");
                         myParameterValue.setParameter("CodeLink Normalization Parameter 1");
                         if (!codeLink_parameter1.equals("")) {
                                 	myParameterValue.setValue(codeLink_parameter1);
                         } else {
 					myParameterValue.setValue("Null");
                         }
                         myParameterValue.createParameterValue(dbConn);
                         outputHDF5=false;
 		}
                 
                 
                 
                 User.UserChip[] myChipAssignments = selectedDataset.new Group().getChipAssignments(grouping_id, dbConn);
 		edu.ucdenver.ccp.PhenoGen.data.Array[] myArrays = selectedDataset.getArrays();
 		//log.debug("myArrays = "); myDebugger.print(myArrays);
 		int[] groupValues = new int[myArrays.length];
 		//log.debug("there are "+groupValues.length + " groupValues");
 		for (int i=0; i<myArrays.length; i++) {
 			groupValues[i] = new User().new UserChip().getUserChipFromMyUserChips(myChipAssignments, 
 						myArrays[i].getHybrid_id()).getGroup().getGroup_number();
 			//log.debug("getting group number for "+myArrays[i].getHybrid_id() + " it is "+groupValues[i]);
 		}
 		//log.debug("groupValues = "); myDebugger.print(groupValues);
 		
                 String grouping = "c(" + 
 			new ObjectHandler().getAsSeparatedString(groupValues, ", ") + ")";
 		log.debug("grouping = "+grouping);
                 
                 
                
                     //
                     // Create group data
                     //
                     String groupHeader = "grp.number\t" + "grp.name\n";
                     String groupData = "";
                     List<String> groupList = new ArrayList<String>();
                     
                     int start = (myGroups[0].getGroup_name().equals("Exclude") ? 1 : 0);
                     
                     ArrayList<ArrayList<Integer>> sampleGroupList=new ArrayList<ArrayList<Integer>>();
                     String[] groupNumbers;
                     if(start==0){
                         groupNumbers=new String[myGroups.length];
                     }else{
                         groupNumbers=new String[myGroups.length-1];
                     }
                     for (int i=start; i<myGroups.length; i++) {
                             ArrayList<Integer> tmp=new ArrayList<Integer>();
                             String thisGroupNumber = Integer.toString(myGroups[i].getGroup_number());
                             groupNumbers[i-start]=thisGroupNumber;
                             groupData = groupData + thisGroupNumber + "\t" +
                                                     myGroups[i].getGroup_name().replaceAll("[\\s]", "_") + "\n";
                             groupList.add(thisGroupNumber);
                             for(int k=0;k<groupValues.length;k++){
                                 String test=Integer.toString(groupValues[k]);
                                 if(test.equals(thisGroupNumber)){
                                     tmp.add(k);
                                 }
                             }
                             sampleGroupList.add(tmp);
                     }
                 
                     groupData = groupHeader + groupData + "\n";
                     try {
                         if(outputHDF5){
                             myFileHandler.writeFile(groupData, selectedDataset.getPath()+"v"+newDatasetVersion.getVersion()+"_groups.txt");
                         }else{
                             myFileHandler.writeFile(groupData, newDatasetVersion.getGroupFileName());
                         }
                     } catch (Exception e) {
                             log.debug("error creating groupsFile in doNormalization");
                             throw new ErrorException("SYS-001");
                     }
 
 		
 
 
 		/*************************** STEP 2 ****************************/
 		// 2. Create a thread to run the Normalization R processes.
 
                 if (new edu.ucdenver.ccp.PhenoGen.data.Array().EXON_ARRAY_TYPES.contains(selectedDataset.getArray_type())) {
 
                     
                         String normPrefix=(normalize_method.equals("rma-sketch") ? "rma-sketch" : "plier-gcbg-sketch");
                     
 			thread2 = new Thread(new AsyncParallelAffyPowerTools(
                                         newDatasetVersion,
 					selectedDataset,
 					session,
 					analysis_level,
 					annotation_level,
                                         normalize_method,
 					probeMask,
                                         normPrefix
                                 ));
 
                         log.debug("Starting thread to run Affymetrix Power Tools.  It is called" + thread2.getName());
                 	thread2.start();
 
 			String summaryFile = (normalize_method.equals("rma-sketch") ? "rma-sketch.summary.txt" : "plier-gcbg-sketch.summary.txt");
 
                 	
                         if(!outputHDF5){
                             functionArgs = new String[6];
 
                             rFunction ="Affymetrix.Exon.Import";
 
                             functionArgs[0] = "summaryFile = '" + newDatasetVersion.getVersion_path() + summaryFile + "'";
                             functionArgs[1] = "dabgFile = '" + newDatasetVersion.getVersion_path() + "dabg.summary.txt'";
                             functionArgs[2] = "grouping = " + grouping;
                             functionArgs[3] = "FileListing = '" + newDatasetVersion.getDataset().getPath() + 
 						newDatasetVersion.getDataset().getFileListingName() + "'";
                             functionArgs[4] = "OutputFile = '" + newDatasetVersion.getNormalized_RData_FileName() + "'"; 
                             functionArgs[5] = "OutputCSVFile = '" + newDatasetVersion.getVersion_path() + 
                                                 "v" + newDatasetVersion.getVersion() +
 						"_" +"Affymetrix.Normalization.output.csv'";
                             //run thread to output to Rdata
                             thread3 = new Thread(new Async_R_Session(
                                                             this.getRFunctionDir(), 
                                                             rFunction, 
                                                             functionArgs,
                                                             newDatasetVersion.getVersion_path(), 
                                                             thread2));
 
                             log.debug("Starting thread to run Affymetrix.Exon.Import.  "+
                                     "It is named "+thread3.getName());
 
                             thread3.start();
 
                             nextWaitThread = thread3;
                         }else{
                             //output to HDF5
                             Async_HDF5_FileHandler ahf=new Async_HDF5_FileHandler(newDatasetVersion.getDataset(),"v"+newDatasetVersion.getVersion(),newDatasetVersion.getDataset().getPath(),"Affy.NormVer.h5","createNormVersfromAPTFiles",thread2,session);
                             ahf.setCreateVersionParameters(sampleGroupList,groupNumbers, groupValues,grouping_id, "", "APT",normalize_method);
                             thread3 = new Thread(ahf);
                             log.debug("Starting thread to run Async_HDF5_FileHandler  "+
                                     "It is named "+thread3.getName());
 
                             thread3.start();
                             nextWaitThread=thread3;
                         }
 		} else {
 			thread2 = callNormalize(newDatasetVersion,
 						normalize_method, grouping,
 						probeMask,
                                         	selectedDataset.getPublicDatasetsPath(userFilesRoot) + "mask.v37.19strains.txt", 
                                         	codeLink_parameter1, waitThread);
 
                 	log.debug("starting thread2 to run Normalization.  It is named "+thread2.getName());
                 	thread2.start();
 			nextWaitThread = thread2;
 		}
 
                 //************************** STEP 2B****************************/
                 if(outputHDF5){
                     //start Laura's database loading process
                     //Move oracle files to Phenogen
                     //and clean up APT files
                     String root=(String) session.getAttribute("userFilesRoot");
                     String dbExtFilePath=(String) session.getAttribute("dbExtFileDir");
                     Async_APT_Filecleanup afc=new Async_APT_Filecleanup(selectedDataset,newDatasetVersion,root,dbExtFilePath,dbConn,thread3);
                     thread6 = new Thread(afc);
                     log.debug("Starting thread to run Async_APT_FileCleanup  "+
                                     "It is named "+thread6.getName());
 
                     thread6.start();
                     nextWaitThread=thread6;
                 }
                 /*************************** STEP 3 ****************************/
                 // 3. Create a thread to update the visible flag in the dataset_versions
                 //      table to 1.  Also send an email to the user.
                 //
 
 		// Why am I doing this here?  AAGH!
                 //selectedDataset.setPath(selectedDataset.getDatasetPath(this.userLoggedIn.getUserMainDir()));
                 //
                 // Create a new thread which will commence after nextWaitThread is finished
                 //
 
 		thread4 = new Thread(new AsyncUpdateDataset(
                                                 newDatasetVersion, this.session,
                                                 nextWaitThread));
 
                 log.debug("starting thread4 to run AsyncUpdateDataset. It is named "+thread4.getName() +
                                         " and waiting on thread4 named " + nextWaitThread.getName());
 
                 thread4.start();
                 log.debug("back in normalize after thread4.start");
 
 		return nextVersionNumber;
 	}
         
         
         
 }
  
