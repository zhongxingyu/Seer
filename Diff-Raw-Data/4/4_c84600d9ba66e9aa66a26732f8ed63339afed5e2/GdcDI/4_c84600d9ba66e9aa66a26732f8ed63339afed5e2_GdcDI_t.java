 package com.gooddata.processor;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 import com.gooddata.exceptions.*;
 import com.gooddata.integration.rest.exceptions.GdcRestApiException;
 import com.gooddata.naming.N;
 import com.gooddata.util.StringUtil;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.Options;
 import org.apache.log4j.Logger;
 import org.gooddata.connector.Connector;
 import org.gooddata.connector.backend.AbstractConnectorBackend;
 
 import com.gooddata.connector.CsvConnector;
 import com.gooddata.connector.GaConnector;
 import com.gooddata.exceptions.InitializationException;
 import com.gooddata.exceptions.InternalErrorException;
 import com.gooddata.exceptions.InvalidArgumentException;
 import com.gooddata.exceptions.MetadataFormatException;
 import com.gooddata.exceptions.ModelException;
 import com.gooddata.google.analytics.GaQuery;
 import com.gooddata.integration.ftp.GdcFTPApiWrapper;
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.DLI;
 import com.gooddata.integration.model.DLIPart;
 import com.gooddata.integration.rest.GdcRESTApiWrapper;
 import com.gooddata.integration.rest.configuration.NamePasswordConfiguration;
 import com.gooddata.integration.rest.exceptions.GdcLoginException;
 import com.gooddata.integration.rest.exceptions.GdcRestApiException;
 import com.gooddata.util.CsvUtil;
 import com.gooddata.util.FileUtil;
 import com.gooddata.util.StringUtil;
 
 /**
  * The GoodData Data Integration CLI processor.
  *
  * @author jiri.zaloudek
  * @author Zdenek Svoboda <zd@gooddata.org>
  * @version 1.0
  */
 public class GdcDI {
 
     private static Logger l = Logger.getLogger(GdcDI.class);
 
 	private final String ftpHost;
 	private final String host;
 	private final String userName;
 	private final String password;
 
     private String dbUserName;
     private String dbPassword;
 
     private GdcRESTApiWrapper _restApi = null;
     private GdcFTPApiWrapper _ftpApi = null;
 
     private String projectId = null;
     private Connector connector = null;
 
     private int backend = AbstractConnectorBackend.CONNECTOR_BACKEND_DERBY_SQL;
 
 
     private GdcDI(final String host, final String userName, final String password) {
     	String ftpHost = null;
         // Create the FTP host automatically
         String[] hcs = host.split("\\.");
         if(hcs != null && hcs.length > 0) {
             for(String hc : hcs) {
                 if(ftpHost != null && ftpHost.length()>0)
                     ftpHost += "." + hc;
                 else
                     ftpHost = hc + "-upload";
             }
         }
         else {
             throw new IllegalArgumentException("Invalid format of the GoodData REST API host: " + host);
         }
 
         this.host = host;
         this.ftpHost = ftpHost;
         this.userName = userName;
         this.password = password;
     }
 
     private void setDbUserName(String usr) {
     	this.dbUserName = usr;
     }
 
     private void setDbPassword(String psw) {
     	this.dbPassword = psw;
     }
 
     public void execute(final String commandsStr) throws Exception {
 
         List<Command> cmds = new ArrayList<Command>();
 
         cmds.addAll(parseCmd(commandsStr));
 
         for(Command command : cmds) {
         	processCommand(command);
         }
     }
 
     private GdcRESTApiWrapper getRestApi() throws GdcLoginException {
     	if (_restApi == null) {
     		if (userName == null) {
     			throw new IllegalArgumentException("Please specify the GoodData username (-u or --username) command-line option.");
     		}
     		if (password == null) {
     			throw new IllegalArgumentException("Please specify the GoodData password (-p or --password) command-line option.");
     		}
             final NamePasswordConfiguration httpConfiguration = new NamePasswordConfiguration(
             		"https", host,
                     userName, password);
             _restApi = new GdcRESTApiWrapper(httpConfiguration);
             _restApi.login();
     	}
     	return _restApi;
     }
 
     private GdcFTPApiWrapper getFtpApi() {
     	if (_ftpApi == null) {
 	        System.out.println("Using the GoodData FTP host '" + ftpHost + "'.");
 
 	        NamePasswordConfiguration ftpConfiguration = new NamePasswordConfiguration("ftp",
 	                ftpHost, userName, password);
 
 	        _ftpApi = new GdcFTPApiWrapper(ftpConfiguration);
     	}
     	return _ftpApi;
     }
 
     /**
      * The main CLI processor
      * @param args command line argument
      * @throws Exception any issue
      */
     public static void main(String[] args) throws Exception {
 
         String host = "preprod.gooddata.com";      
 
         Options o = new Options();
 
         o.addOption("u", "username", true, "GoodData username");
         o.addOption("p", "password", true, "GoodData password");
         o.addOption("b", "backend", true, "Database backend DERBY or MYSQL");
         o.addOption("d", "dbusername", true, "Database backend username (not required for the local Derby SQL)");
         o.addOption("c", "dbpassword", true, "Database backend password (not required for the local Derby SQL)");
         o.addOption("h", "host", true, "GoodData host");
         o.addOption("i", "project", true, "GoodData project identifier (a string like nszfbgkr75otujmc4smtl6rf5pnmz9yl)");
         o.addOption("e", "execute", true, "Commands and params to execute before the commands in provided files");
 
         CommandLineParser parser = new GnuParser();
         CommandLine line = parser.parse(o, args);
 
         try {
 	        if(line.hasOption("host")) {
 	            host = line.getOptionValue("host");
 	        }
 	        else {
 	            System.out.println("Using the default GoodData REST API host '" + host + "'.");
 	        }
 	        
 	        String userName = line.getOptionValue("username");
 	        String password = line.getOptionValue("password");
 
 	        GdcDI gdcDi = new GdcDI(host, userName, password);
 	        if (line.hasOption("project")) {
 	        	gdcDi.setProjectId(line.getOptionValue("project"));
 	        }
 	        if (line.hasOption("execute")) {
 	        	gdcDi.execute(line.getOptionValue("execute"));
 	        }
             if (line.hasOption("dbusername")) {
 	        	gdcDi.setDbUserName(line.getOptionValue("dbusername"));
 	        }
             if (line.hasOption("dbpassword")) {
 	        	gdcDi.setDbPassword(line.getOptionValue("dbpassword"));
 	        }
             if (line.hasOption("backend")) {
                 if("MYSQL".equalsIgnoreCase(line.getOptionValue("backend")))
 	        	    gdcDi.setBackend(AbstractConnectorBackend.CONNECTOR_BACKEND_MYSQL);
                 else if("DERBY".equalsIgnoreCase(line.getOptionValue("backend")))
 	        	    gdcDi.setBackend(AbstractConnectorBackend.CONNECTOR_BACKEND_DERBY_SQL);
                 else
                     printErrorHelpandExit("Invalid backend parameter. Use MYSQL or DERBY.");                    
 	        }
 	    	if (line.getArgs().length == 0 && !line.hasOption("execute")) {
         		printErrorHelpandExit("No command has been given, quitting.");
 	    	}
 	        for (final String arg : line.getArgs()) {
 	        	gdcDi.execute(FileUtil.readStringFromFile(arg));
 	        }
         } catch (final IllegalArgumentException e) {
         	printErrorHelpandExit(e.getMessage());
         }
     }
 
     /**
      * Parses the commands
      * @param cmd commands string
      * @return array of commands
      * @throws InvalidArgumentException in case there is an invalid command
      */
     protected static List<Command> parseCmd(String cmd) throws InvalidArgumentException {
         if(cmd != null && cmd.length()>0) {
             List<Command> cmds = new ArrayList<Command>();
             String[] commands = cmd.split(";");
             for( String component : commands) {
                 component = component.trim();
                 if(component != null && component.length() > 0 && !component.startsWith("#")) {
                     Pattern p = Pattern.compile("^.*?\\(.*?\\)$");
                     Matcher m = p.matcher(component);
                     if(!m.matches())
                         throw new InvalidArgumentException("Invalid command: "+component);
                     p = Pattern.compile("^.*?\\(");
                     m = p.matcher(component);
                     String command = "";
                     if(m.find()) {
                         command = m.group();
                         command = command.substring(0, command.length() - 1);
                     }
                     else {
                         throw new InvalidArgumentException("Can't extract command from: "+component);
                     }
                     p = Pattern.compile("\\(.*?\\)$");
                     m = p.matcher(component);
                     Properties args = new Properties();
                     if(m.find()) {
                         String as = m.group();
                         as = as.substring(1,as.length()-1);
                         try {
                             args.load(new StringReader(as.replace(",","\n")));
                         }
                         catch (IOException e) {
                             throw new InvalidArgumentException(e.getMessage());
                         }
                     }
                     else {
                         throw new InvalidArgumentException("Can't extract command from: "+component);
                     }
                     cmds.add(new Command(command, args));
                 }
             }
             return cmds;
         }
         throw new InvalidArgumentException("Can't parse command.");
     }
 
     /**
      * Returns the help for commands
      * @return help text
      */
     protected static String commandsHelp() {
         try {
         	final InputStream is = GdcDI.class.getResourceAsStream("/com/gooddata/processor/COMMANDS.txt");
         	if (is == null)
         		throw new IOException();
             return FileUtil.readStringFromStream(is);
         } catch (IOException e) {
             l.error("Could not read com/gooddata/processor/COMMANDS.txt");
         }
         return "";
     }
 
     /**
      * Prints an err message, help and exits with status code 1
      * @param err the err message
      */
     protected static void printErrorHelpandExit(String err) {
         System.out.println("ERROR: " + err);
         System.out.println(commandsHelp());
         System.exit(1);
     }
 
 
     protected boolean match(Command c, String cms) {
         if(c.getCommand().equalsIgnoreCase(cms))
             return true;
         else
             return false;
     }
     
     protected String getParam(Command c, String p) {
         return (String)c.getParameters().get(p);
     }
 
     protected void error(Command c, String msg) throws InvalidArgumentException {
         throw new InvalidArgumentException(c.getCommand()+": "+msg);
     }
 
     protected String getParamMandatory(Command c, String p) {
         String v = (String)c.getParameters().get(p);
         if(v == null || v.length() == 0) {
 
         }
         return v;
     }
 
     protected String getProjectId(Command c) throws InvalidArgumentException {
         if(projectId == null || projectId.length() == 0) {
             error(c, "Please create or open project by using CreateProject or OpenProject commands.");
         }
         return projectId;
     }
 
     protected void setProjectId(String pid) {
         projectId = pid;
     }
 
     protected Connector getConnector(Command c) throws InvalidArgumentException {
         if(connector == null) {
             error(c, "No connector. Please use a LoadXXX command to create connector first.");
         }
         return connector;
     }
 
     protected void setConnector(Connector cc) {
         connector = cc;
     }
 
     protected File getFile(Command c, String fileName) throws InvalidArgumentException {
         File f = new File(fileName);
         if(!f.exists())
             error(c, "File '" + fileName + "' doesn't exist.");
         return f;
     }
 
     /**
      * Executes the command
      * @param c to execute
      * @throws Exception general error
      */
     private void processCommand(Command c) throws Exception {
         if(match(c,"CreateProject")) {
             createProject(c);
         }
         if(match(c,"OpenProject")) {
             setProjectId(getParamMandatory(c,"id"));
         }
         if(match(c,"GenerateCsvConfigTemplate")) {
             generateCsvConfigTemplate(c);
         }
         if(match(c,"LoadCsv")) {
             loadCsv(c);
         }
         if(match(c,"GenerateGoogleAnalyticsConfigTemplate")) {
             generateGAConfigTemplate(c);
         }
         if(match(c,"LoadGoogleAnalytics")) {
             LoadGA(c);
         }
         if(match(c,"GenerateMaql")) {
             generateMAQL(c);
         }
         if(match(c,"ExecuteMaql")) {
             executeMAQL(c);
         }
         if(match(c,"ListSnapshots")) {
             listSnapshots(c);
         }
         if(match(c,"DropSnapshots")) {
             dropSnapshots(c);
         }
         if(match(c,"UploadDir")) {
             uploadDir(c);
         }
         if(match(c,"TransferData")) {
             transferData(c);
         }
         if(match(c,"TransferSnapshots")) {
             transferSnapshots(c);
         }
         if(match(c,"TransferLastSnapshot")) {
             transferLastSnapshot(c);
         }
     }
 
     private void transferLastSnapshot(Command c) throws InvalidArgumentException, ModelException, IOException, InternalErrorException, GdcRestApiException {
         Connector cc = getConnector(c);
         String pid = getProjectId(c);
         // connector's schema name
         String ssn = StringUtil.formatShortName(cc.getSchema().getName());
 
         cc.initialize();
         // retrieve the DLI
         DLI dli = getRestApi().getDLIById("dataset." + ssn, pid);
         List<DLIPart> parts= getRestApi().getDLIParts("dataset." + ssn, pid);
 
         String incremental = getParam(c,"incremental");
         if(incremental != null && incremental.length() > 0 &&
                 incremental.equalsIgnoreCase("true")) {
             setIncremental(parts);
         }
         extractAndTransfer(c, pid, cc, dli, parts, new int[] {cc.getLastSnapshotId()+1});
     }
 
     private void transferSnapshots(Command c) throws InvalidArgumentException, ModelException, IOException, GdcRestApiException {
         Connector cc = getConnector(c);
         String pid = getProjectId(c);
         String firstSnapshot = getParamMandatory(c,"firstSnapshot");
         String lastSnapshot = getParamMandatory(c,"lastSnapshot");
         int fs = 0,ls = 0;
         try  {
             fs = Integer.parseInt(firstSnapshot);
         }
         catch (NumberFormatException e) {
             throw new IllegalArgumentException("TransferSnapshots: The 'firstSnapshot' (" + firstSnapshot +
                     ") parameter is not a number.");
         }
         try {
             ls = Integer.parseInt(lastSnapshot);
         }
         catch (NumberFormatException e) {
             throw new IllegalArgumentException("TransferSnapshots: The 'lastSnapshot' (" + lastSnapshot +
                     ") parameter is not a number.");
         }
         int cnt = ls - fs;
         if(cnt >= 0) {
             int[] snapshots = new int[cnt];
             for(int i = 0; i < cnt; i++) {
                 snapshots[i] = fs + i;
             }
             // connector's schema name
             String ssn = StringUtil.formatShortName(cc.getSchema().getName());
 
             cc.initialize();
             // retrieve the DLI
             DLI dli = getRestApi().getDLIById("dataset." + ssn, pid);
             List<DLIPart> parts= getRestApi().getDLIParts("dataset." +ssn, pid);
 
             String incremental = getParam(c,"incremental");
             if(incremental != null && incremental.length() > 0 &&
                     incremental.equalsIgnoreCase("true"))
                 setIncremental(parts);
 
             extractAndTransfer(c, pid, cc, dli, parts, snapshots);
         }
         else
             error(c,"The firstSnapshot can't be higher than the lastSnapshot.");
     }
 
     private void transferData(Command c) throws InvalidArgumentException, ModelException, IOException, GdcRestApiException {
         Connector cc = getConnector(c);
         String pid = getProjectId(c);
         // connector's schema name
         String ssn = StringUtil.formatShortName(cc.getSchema().getName());
         cc.initialize();
         // retrieve the DLI
         DLI dli = getRestApi().getDLIById("dataset." + ssn, pid);
         List<DLIPart> parts= getRestApi().getDLIParts("dataset." + ssn, pid);
         // target directories and ZIP names
 
         String incremental = getParam(c,"incremental");
         if(incremental != null && incremental.length() > 0 && incremental.equalsIgnoreCase("true")) {
             setIncremental(parts);
         }
         extractAndTransfer(c, pid, cc, dli, parts, null);
     }
 
     private void extractAndTransfer(Command c, String pid, Connector cc, DLI dli, List<DLIPart> parts,
         int[] snapshots) throws IOException, ModelException, GdcRestApiException, InvalidArgumentException {
         File tmpDir = FileUtil.createTempDir();
         makeWritable(tmpDir);
         File tmpZipDir = FileUtil.createTempDir();
         String archiveName = tmpDir.getName();
         String archivePath = tmpZipDir.getAbsolutePath() + System.getProperty("file.separator") +
             archiveName + ".zip";
         // loads the CSV data to the embedded Derby SQL
         cc.extract();
         // normalize the data in the Derby
         cc.transform();
         // load data from the Derby to the local GoodData data integration package
         cc.deploySnapshot(dli, parts, tmpDir.getAbsolutePath(), archivePath, snapshots);
         // transfer the data package to the GoodData server
         getFtpApi().transferDir(archivePath);
         // kick the GooDData server to load the data package to the project
         getRestApi().startLoading(pid, archiveName);
         //cleanup
         //TODO: Do cleanup
         //FileUtil.recursiveDelete(tmpDir);
         //FileUtil.recursiveDelete(tmpZipDir);
     }
 
     private void makeWritable(File tmpDir) {
         try {
             Runtime.getRuntime().exec("chmod -R 777 "+tmpDir.getAbsolutePath());
         }
         catch (IOException e) {
             l.debug("CHMOD execution failed. No big deal perhaps you are running Windows.", e);
         }
     }
 
     private void uploadDir(Command c) throws InvalidArgumentException, IOException, GdcRestApiException {
         String pid = getProjectId(c);
         String path = getParamMandatory(c,"path");
         String dataset = getParamMandatory(c,"dataset");
         String reorderStr = getParam(c, "reorder");
        boolean reorder = (reorderStr != null) 
        	&& !"".equals(reorderStr) 
        	&& !"false".equalsIgnoreCase(reorderStr);
         // validate input dir
         File dir = getFile(c,path);
         if (!dir.isDirectory()) {
             throw new IllegalArgumentException("UploadDir: " + path + " is not a directory.");
         }
         if (!(dir.canRead() && dir.canExecute() && dir.canWrite())) {
             throw new IllegalArgumentException("UploadDir: directory " + path + " is not r/w accessible.");
         }
         // generate manifest
         DLI dli = getRestApi().getDLIById(dataset, pid);
         List<DLIPart> parts = getRestApi().getDLIParts(dataset, pid);
 
         // prepare the zip file
         File tmpDir = FileUtil.createTempDir();
         for (final DLIPart part : parts) {
         	preparePartFile(part, dir, tmpDir, reorder);
         }
         File tmpZipDir = FileUtil.createTempDir();
         FileUtil.writeStringToFile(
         		dli.getDLIManifest(parts),
         		tmpDir + System.getProperty("file.separator")
         			 + GdcRESTApiWrapper.DLI_MANIFEST_FILENAME);
         String archiveName = tmpDir.getName();
         String archivePath = tmpZipDir.getAbsolutePath() +
                 System.getProperty("file.separator") + archiveName + ".zip";
         FileUtil.compressDir(tmpDir.getAbsolutePath(), archivePath);
         
         // ftp upload
         getFtpApi().transferDir(archivePath);
         
         // kick the GoodData server to load the data package to the project
         getRestApi().startLoading(pid, archiveName);
     }
 
     private void dropSnapshots(Command c) throws InvalidArgumentException {
         Connector cc = getConnector(c);
         cc.dropSnapshots();
     }
 
     private void listSnapshots(Command c) throws InvalidArgumentException, InternalErrorException {
         Connector cc = getConnector(c);
         System.out.println(cc.listSnapshots());
     }
 
     private void executeMAQL(Command c) throws InvalidArgumentException, IOException, GdcRestApiException {
         String pid = getProjectId(c);
         String maqlFile = getParamMandatory(c,"maqlFile");
         File mf = getFile(c,maqlFile);
         String maql = FileUtil.readStringFromFile(maqlFile);
         getRestApi().executeMAQL(pid, maql);
     }
 
     private void generateMAQL(Command c) throws InvalidArgumentException, IOException {
         Connector cc = getConnector(c);
         String maqlFile = getParamMandatory(c,"maqlFile");
         String maql = cc.generateMaql();
         FileUtil.writeStringToFile(maql, maqlFile);
     }
 
     private void LoadGA(Command c)
             throws InvalidArgumentException, InitializationException, MetadataFormatException, IOException,
             ModelException {
         String pid = getProjectId(c);
         String configFile = getParamMandatory(c,"configFile");
         String usr = getParamMandatory(c,"username");
         String psw = getParamMandatory(c,"password");
         String id = getParamMandatory(c,"profileId");
         String dimensions = getParamMandatory(c,"dimensions");
         String metrics = getParamMandatory(c,"metrics");
         String startDate = getParamMandatory(c,"startDate");
         String endDate = getParamMandatory(c,"endDate");
         String filters = getParamMandatory(c,"filters");
         GaQuery gq = null;
         try {
             gq = new GaQuery();
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException(e.getMessage());
         }
         gq.setDimensions(dimensions.replace("|",","));
         gq.setMetrics(metrics.replace("|",","));
         gq.setStartDate(startDate);
         gq.setEndDate(endDate);
         gq.setFilters(filters);
         setConnector(GaConnector.createConnector(pid, configFile, usr, psw, id, gq,
                 getBackend(), dbUserName, dbPassword));
     }
 
     private void generateGAConfigTemplate(Command c) throws InvalidArgumentException, IOException {
         String configFile = getParamMandatory(c,"configFile");
         String name = getParamMandatory(c,"name");
         String dimensions = getParamMandatory(c,"dimensions");
         String metrics = getParamMandatory(c,"metrics");
         File cf = getFile(c,configFile);
         GaQuery gq = null;
         try {
             gq = new GaQuery();
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException(e.getMessage());
         }
         gq.setDimensions(dimensions);
         gq.setMetrics(metrics);
         GaConnector.saveConfigTemplate(name, configFile, gq);
     }
 
     private void loadCsv(Command c)
             throws InvalidArgumentException, InitializationException, MetadataFormatException, IOException,
             ModelException {
         String pid = getProjectId(c);
         String configFile = getParamMandatory(c,"configFile");
         String csvDataFile = getParamMandatory(c,"csvDataFile");
         String hdr = getParamMandatory(c,"header");
         File conf = getFile(c,configFile);
         File csvf = getFile(c,csvDataFile);
         boolean hasHeader = false;
         if(hdr.equalsIgnoreCase("true"))
             hasHeader = true;
         setConnector(CsvConnector.createConnector(pid, configFile, csvDataFile, hasHeader, getBackend(), dbUserName,
                 dbPassword));
     }
 
     private void generateCsvConfigTemplate(Command c) throws InvalidArgumentException, IOException {
         String configFile = getParamMandatory(c,"configFile");
         String csvHeaderFile = getParamMandatory(c,"csvHeaderFile");
         File cf = getFile(c,configFile);
         File csvf = getFile(c,csvHeaderFile);
         CsvConnector.saveConfigTemplate(configFile, csvHeaderFile);
     }
 
     private void createProject(Command c) throws GdcRestApiException, InvalidArgumentException {
         String name = getParamMandatory(c,"name");
         setProjectId(getRestApi().createProject(name, name));
         String pid = getProjectId(c);
         System.out.println("Project id = '"+pid+"' created.");
     }
 
     private void setIncremental(List<DLIPart> parts) {
         for(DLIPart part : parts) {
             if(part.getFileName().startsWith(N.FCT_PFX)) {
                 part.setLoadMode(DLIPart.LM_INCREMENTAL);
             }
         }
     }
 
     public int getBackend() {
         return backend;
     }
 
     public void setBackend(int backend) {
         this.backend = backend;
     }
 
     /**
      * attempts to find a file corresponding to given part in the <tt>dir</tt>
      * directory and creates its upload ready version with properly ordered 
      * columns in the <tt>targetDir</tt>
      * 
      * @param part
      * @param dir
      * @param targetDir
      * @throws IOException 
      */
 	private void preparePartFile(DLIPart part, File dir, File targetDir, boolean reorder) throws IOException {
 		final InputStream is = new FileInputStream(dir.getAbsoluteFile() + System.getProperty("file.separator") + part.getFileName());
 		final OutputStream os = new FileOutputStream(targetDir.getAbsoluteFile() + System.getProperty("file.separator") + part.getFileName());
 		
 		if (reorder) {
 			final List<String> fields = new ArrayList<String>(part.getColumns().size());
 			for (final Column c : part.getColumns()) {
 				fields.add(c.getName());
 			}
 			CsvUtil.reshuffle(is, os, fields);
 		} else {
 			FileUtil.copy(is, os);
 		}
 	}
 
 }
