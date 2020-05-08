 package confdb.converter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import java.io.IOException;
 import java.sql.Connection;
 
 import confdb.data.*;
 import confdb.db.ConfDB;
 import confdb.db.DatabaseException;
 
 /**
  * OnlineConverter
  * ---------------
  * @author Ulf Behrens
  * @author Philipp Schieferdecker
  * 
  * Handle conversion of configurations stored in the database for
  * deployment to the online HLT filter farm.
  */
 public class OnlineConverter extends ConverterBase 
 {
     static private OnlineConverter converter = null;
     static private Connection dbConnection = null;
     
 	
     //
     // member data
     //
 
     /** current configuration id */
     private int configId = -1;
 
     /** current configuration string for FUEventProcessor */
     private String epConfigString = null;
 
     /** current configuration string for StorageManager */
     private String smConfigString = null;
 
     /** current hash map 'pathName' -> 'prescalerName' OBSOLETE */
     private HashMap<String, String> pathToPrescaler = new HashMap<String, String>();
 
     /** current prescale table */
     private PrescaleTable prescaleTable = null;
     
     /** MessageLogger verbosity levels for cout & log4cplus */
     private String mlVerbosityCout = "FATAL";
     private String mlVerbosityLog4 = "WARNING";
 
     /** event setup configuration: globaltag & connect string */
     private String esGlobalTag = "";
     private String esConnect   =
 	"frontier://(proxyurl=http://localhost:3128)"+
 	"(serverurl=http://frontier1.cms:8000/FrontierOnProd)"+
 	"(serverurl=http://frontier2.cms:8000/FrontierOnProd)"+
 	"(retrieve-ziplevel=0)";
 
     /** flag used in finalize to either disconnect from database or not */
     private boolean disconnectOnFinalize = true;
     
     
     //
     // construction
     //
 
     /** standard constructor */
     public OnlineConverter() throws ConverterException 
     {
 	super("ascii");
 	try {
 	    DbProperties dbProperties = DbProperties.getDefaultDbProperties();
 	    initDB(dbProperties.dbType, dbProperties.getDbURL(), dbProperties
 		   .getDbUser(), "convertme!");
 	} catch (IOException e) {
 	    throw new ConverterException("can't construct OnlineConverter", e);
 	}
     }
     
     /** constructor based on Connection object */
     public OnlineConverter( Connection connection ) throws ConverterException 
     {
     	this( "ascii", connection );
     }
     
     /** constructor based on format & Connection object */
     public OnlineConverter(String format, Connection connection)
 	throws ConverterException 
     {
     	super(format, connection);
     	disconnectOnFinalize = false;
     }
 
     /** constructor based on explicit connection information */
     public OnlineConverter( String format, String dbType, String dbUrl,
 			    String dbUser, String dbPwrd) throws ConverterException 
     {
 	super(format, dbType, dbUrl, dbUser, dbPwrd);
     }
 
     
     /** destructor  */
 	protected void finalize() throws Throwable
 	{
 		super.finalize();
 		ConfDB db = getDatabase();
 		if ( db != null && disconnectOnFinalize )
 			db.disconnect();
 	}
 	
     
     
     //
     // member functions
     //
 
     /** get the configuration string for FUEventProcessor */
     public String getEpConfigString(int configId)
 	throws ConverterException
     {
 	if (configId != this.configId)
 	    convertConfiguration(configId);
 	return epConfigString;
     }
 
     /** get the configuration string for StorageManager */
     public String getSmConfigString(int configId)
 	throws ConverterException
     {
 	if (configId != this.configId)
 	    convertConfiguration(configId);
 	return smConfigString;
     }
 
     /** get the pathName -> prescalerName map  DEPRECTATED */ 
     public HashMap<String, String> getPathToPrescalerMap(int configId)
 	throws ConverterException 
     {
 	if (configId != this.configId)
 	    convertConfiguration(configId);
 	return pathToPrescaler;
     }
 
     /** get the prescale table */
     public PrescaleTable getPrescaleTable(int configId)
 	throws ConverterException
     {
 	if (configId != this.configId)
 	    convertConfiguration(configId);
 	return prescaleTable;
     }
     
 
     /** set the GlobalTag global tag parameter */
     public void setGlobalTag(String esGlobalTag) { this.esGlobalTag = esGlobalTag; }
 
     /** set the GlobalTag connect parameter */
     public void setConnect(String esConnect) { this.esConnect = esConnect; }
     
     /** set verbosity levels for message logger configuration */
     public void setMessageLoggerVerbosity(String vCout, String vLog4)
     {
 	mlVerbosityCout = vCout;
 	mlVerbosityLog4 = vLog4;
     }
 
     
 
     //
     // private member data
     //
 
     /** convert configuration and cache ep and sm configuration string */
     private void convertConfiguration(int configId) throws ConverterException 
     {
 	IConfiguration epConfig = getConfiguration(configId);
 	
 	if (epConfig.streamCount()==0) {
 	    String errMsg =
 		"OnlineConverter::convertConfiguration(configId="+configId+
 		") ERROR: no streams defined!";
 	    throw new ConverterException(errMsg);
 	}
 	
 	// This should go down, once the OM hack has been replaced!
 	ConfigurationModifier epModifier = new ConfigurationModifier(epConfig);
 
 	
 	SoftwareSubsystem subsys = new SoftwareSubsystem("IOPool");
 	SoftwarePackage   pkg = new SoftwarePackage("Streamer");
 	ModuleTemplate    smStreamWriterT = makeSmStreamWriterT();
 	SoftwareRelease   smRelease = new SoftwareRelease();
 
 	smRelease.clear(epConfig.releaseTag());
 	pkg.addTemplate(smStreamWriterT);
 	subsys.addPackage(pkg);
 	smRelease.addSubsystem(subsys);
 
 	Configuration smConfig =
 	    new Configuration(new ConfigInfo(epConfig.name(),
 					     epConfig.parentDir(),
 					     -1,
 					     epConfig.version(),
 					     epConfig.created(),
 					     epConfig.creator(),
 					     epConfig.releaseTag(),
 					     "SM",
 					     epConfig.comment()),
 			      smRelease);
 	
 	Path endpath = smConfig.insertPath(0, "epstreams");
 
 	Iterator<Stream> itStream = epConfig.streamIterator();
 	
 	while (itStream.hasNext()) {
 	    Stream stream = itStream.next();
 	    
 	    if (stream.pathCount()==0) continue;
 	    
 	    ModuleReference streamWriterRef =
 		smConfig.insertModuleReference(endpath,
 					       endpath.entryCount(), 
 					       smStreamWriterT.name(),
 					       stream.label());
 	    ModuleInstance streamWriter = (ModuleInstance)streamWriterRef.parent();
 	    streamWriter.updateParameter("streamLabel", "string", stream.label());
 	    PSetParameter psetSelectEvents = new PSetParameter("SelectEvents",
 							       "", false, true);
 	    String valAsString = "";
 	    Iterator<Path> itPath = stream.pathIterator();
 	    while (itPath.hasNext()) {
 		Path path = itPath.next();
 		if (valAsString.length()>0) valAsString += ",";
 		valAsString += path.name();
 	    }
 	    VStringParameter vstringSelectEvents =
 		new VStringParameter("SelectEvents", valAsString, true, false);
 	    psetSelectEvents.addParameter(vstringSelectEvents);
 	    streamWriter.updateParameter("SelectEvents", "PSet",
 					 psetSelectEvents.valueAsString());
 
 	    // temporary hack
 	    Iterator<ModuleInstance> itM = epModifier.moduleIterator();
 	    while (itM.hasNext()) {
 		ModuleInstance module = itM.next();
 		if (!module.template().type().equals("OutputModule")) continue;
 		PSetParameter psetSelectOM =
 		    (PSetParameter)module.parameter("SelectEvents");
 		if (psetSelectOM==null) continue;
 		VStringParameter vstringSelectOM = 
 		    (VStringParameter)psetSelectOM.parameter("SelectEvents");
 		if (vstringSelectOM==null) continue;
 		if(vstringSelectOM.valueAsSortedString()
 		   .equals(vstringSelectEvents.valueAsSortedString())) {
 		    streamWriter.updateParameter("SelectHLTOutput",
 						 "string",module.name());
 		}
 	    }
 	}
 	
 	// apply necessary offline -> online modifications to HLT configuration
 	epModifier.insertDaqSource();
 	epModifier.insertShmStreamConsumer();
 	epModifier.removeMaxEvents();
 	epModifier.modify();
 	
 	configureGlobalTag(epModifier);
 	setOnlineMessageLoggerOptions(epModifier);
 	addOnlineOptions(epModifier);
 	setRawDataInputTags(epModifier);
 
 	try {
 	    addDQMStore(epModifier);
 	    addFUShmDQMOutputService(epModifier);
 	}
 	catch (Exception e) {
 	    String errMsg =
 		"convertConfiguration(): failed to add Service: " + e.getMessage();
 	    throw new ConverterException(errMsg,e);
 	}
 	
 	
 	// obsolete, remove?
 	pathToPrescaler.clear();
 	Iterator<Path> itP = epModifier.pathIterator();
 	while (itP.hasNext()) {
 	    Path path = itP.next();
 	    Iterator<ModuleInstance> itM = path.moduleIterator();
 	    while (itM.hasNext()) {
 		ModuleInstance module = itM.next();
 		if (module.template().name().equals("HLTPrescaler")) {
 		    pathToPrescaler.put(path.name(), module.name());
 		    break;
 		}
 	    }
 	}
 	
 	prescaleTable = new PrescaleTable(epModifier);
 	
 	epConfigString = getConverterEngine().convert(epModifier);
 	smConfigString = getConverterEngine().convert(smConfig);
 
 	this.configId = configId;
     }
 
     /** make a sm stream writer template */
     private ModuleTemplate makeSmStreamWriterT() 
     {
 	ArrayList<Parameter> params = new ArrayList<Parameter>();
 	params.add(new StringParameter("streamLabel", "", true, false));
 	params.add(new Int32Parameter("maxSize", "1073741824", true, false));
 	params.add(new PSetParameter("SelectEvents", "", false, false));
 	params.add(new StringParameter("SelectHLTOutput", "", false, false));
         return new ModuleTemplate("EventStreamFileWriter", "UNKNOWN", -1,
 				  params, "OutputModule");
     }
 
     /** configure the global tag event setup source */
     private void configureGlobalTag(IConfiguration config)
     {
 	ESSourceInstance globalTag = config.essource("GlobalTag");
 	if (esGlobalTag.length()>0)
 	    globalTag.updateParameter("globaltag","string",esGlobalTag);
 	String connect = globalTag.parameter("connect","string").valueAsString();
 	connect = connect.substring(connect.lastIndexOf('/'));
 	connect = esConnect + connect;
 	globalTag.updateParameter("connect","string",connect);
     }
     
     /** add global pset 'options', suitable for online */
     private void addOnlineOptions(IConfiguration config)
     {
 	PSetParameter options=new PSetParameter("options",
 						new ArrayList<Parameter>(),
 						false,false);
 	options.addParameter(new VStringParameter("Rethrow",
 						  "ProductNotFound,"+
 						  "TooManyProducts,"+
 						  "TooFewProducts",false,false));
 	config.insertPSet(options);
     }
 
     /** set the parameters for the online message logger service */
     private void setOnlineMessageLoggerOptions(IConfiguration config)
     {
 	ServiceInstance msgLogger = config.service("MessageLogger");
 	if (msgLogger==null) {
 	    System.err.println("MessageLogger not found");
 	    return;
 	}
 	PSetParameter psetCout = new PSetParameter("cout",
 						   new ArrayList<Parameter>(),
 						   false,false);
 	psetCout.addParameter(new StringParameter("threshold",mlVerbosityCout,
 						  false,false));
 	PSetParameter psetLog4 = new PSetParameter("log4cplus",
 						   new ArrayList<Parameter>(),
 						   false,false);
 	psetLog4.addParameter(new StringParameter("threshold",mlVerbosityLog4,
 						   false,false));
 	msgLogger.updateParameter("destinations","vstring","cout,log4cplus");
 	msgLogger.updateParameter("cout",        "PSet",psetCout.valueAsString());
 	msgLogger.updateParameter("log4cplus",   "PSet",psetLog4.valueAsString());
 
 	Iterator<Parameter> itP = msgLogger.parameterIterator();
 	while (itP.hasNext()) {
 	    Parameter p = itP.next();
 	    if (p.isTracked()) continue;
 	    if (p.name().equals("destinations")||
 		p.name().equals("cout")||
 		p.name().equals("log4cplus")) continue;
 	    p.setValue("");
 	}
     }
     
     /** add the DQMStore service */
     private void addDQMStore(ConfigurationModifier config)
 	throws DataException,DatabaseException
     {
 	ServiceTemplate dqmStoreT = (ServiceTemplate)getDatabase()
 	    .loadTemplate(config.releaseTag(),"DQMStore");
 	ServiceInstance dqmStore = (ServiceInstance)dqmStoreT.instance();
 	config.insertService(dqmStore);
     }
     
     /** add the FUShmDSQMOutputService service */
     private void addFUShmDQMOutputService(ConfigurationModifier config)
 	throws DataException,DatabaseException
     {
 	ServiceTemplate dqmOutT = (ServiceTemplate)getDatabase()
 	    .loadTemplate(config.releaseTag(),"FUShmDQMOutputService");
 	ServiceInstance dqmOut = (ServiceInstance)dqmOutT.instance();
 	dqmOut.updateParameter("lumiSectionsPerUpdate","double","1.0");
 	dqmOut.updateParameter("useCompression","bool","true");
 	dqmOut.updateParameter("compressionLevel","int32","1");
 	config.insertService(dqmOut);
     }
     
     /** convert InputTag/string params with value 'rawDataCollector' to 'source' */
     private void setRawDataInputTags(IConfiguration config)
     {
 	Iterator<ModuleInstance> itM = config.moduleIterator();
 	while (itM.hasNext()) {
 	    ModuleInstance module = itM.next();
 	    Iterator<Parameter> itP = module.recursiveParameterIterator();
 	    while (itP.hasNext()) {
 		Parameter p = itP.next();
 		if (!p.isValueSet()) continue;
 		if (p instanceof InputTagParameter) {
 		    InputTagParameter itp = (InputTagParameter)p;
 		    if (itp.label().equals("rawDataCollector"))
 			itp.setLabel("source");
 		}
 		else if (p instanceof VInputTagParameter) {
 		    VInputTagParameter vitp = (VInputTagParameter)p;
 		    for (int i=0;i<vitp.vectorSize();i++) {
 			InputTagParameter itp =
 			    new InputTagParameter("",(String)vitp.value(i),
 						  false,false);
 			if (itp.label().equals("rawDataCollector")) {
 			    itp.setLabel("source");
 			    vitp.setValue(i,itp.valueAsString());
 			}
 		    }
 		}
 		else if (p instanceof StringParameter) {
 		    StringParameter sp = (StringParameter)p;
 		    if (sp.valueAsString().equals("rawDataCollector"))
 			sp.setValue("source");
 		}
 		else if (p instanceof VStringParameter) {
 		    VStringParameter vsp = (VStringParameter)p;
 		    for (int i=0;i<vsp.vectorSize();i++) {
 			String s = (String)vsp.value(i);
 			if (s.equals("rawDataCollector")) vsp.setValue(i,"source");
 		    }
 		}
 	    }
 	}
     }
 	
     
     //
     // static member functions
     //
     
     /** get the converter */
     public static synchronized OnlineConverter getConverter()
 	throws ConverterException
     {
 	if (converter == null) {
 	    if (dbConnection != null)
 		converter = new OnlineConverter(dbConnection);
 	    else
 		converter = new OnlineConverter();
 	}
 	return converter;
     }
     
     /** set the datbase connection */
     public static void setDbConnection(Connection dbConnection) {
 	if (OnlineConverter.dbConnection == null) {
 	    if (converter != null) {
 		ConfDB database = converter.getDatabase();
 		if (database != null)
 		    try {
 		    	database.disconnect();
 		    }
 		    catch (DatabaseException e) {}
 	    }
 	}
 	OnlineConverter.dbConnection = dbConnection;
 	converter = null;
     }
     
     
     //
     // main
     //
     public static void main(String[] args) {
 	String config = "";
 	String dbType = "oracle";
 	String dbHost = "cmsr1-v.cern.ch";
 	String dbPort = "10121";
 	String dbName = "cms_cond.cern.ch";
 	String dbUser = "cms_hltdev_reader";
 	String dbPwrd = "convertme!";
 
 	for (int iarg = 0; iarg < args.length; iarg++) {
 	    String arg = args[iarg];
 	    if (arg.equals("-c")) {
 		iarg++;
 		config = args[iarg];
 	    } else if (arg.equals("-t")) {
 		iarg++;
 		dbType = args[iarg];
 	    } else if (arg.equals("-h")) {
 		iarg++;
 		dbHost = args[iarg];
 	    } else if (arg.equals("-p")) {
 		iarg++;
 		dbPort = args[iarg];
 	    } else if (arg.equals("-d")) {
 		iarg++;
 		dbName = args[iarg];
 	    } else if (arg.equals("-u")) {
 		iarg++;
 		dbUser = args[iarg];
 	    } else if (arg.equals("-s")) {
 		iarg++;
 		dbPwrd = args[iarg];
 	    }
 	}
 	
 	System.err.println("dbType="+dbType+", "+
 			   "dbHost="+dbHost+", "+
 			   "dbPort="+dbPort+", "+
 			   "dbName="+dbName+"\n"+
 			   "dbUser="+dbUser+", "+
 			   "dbPwrd="+dbPwrd);
 	
 	String dbUrl = "";
 	if (dbType.equalsIgnoreCase("mysql")) {
 	    dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
 	} else if (dbType.equalsIgnoreCase("oracle")) {
 	    dbUrl = "jdbc:oracle:thin:@//" + dbHost + ":" + dbPort + "/"
 		+ dbName;
 	} else {
 	    System.err.println("Unknwown db type '" + dbType + "'");
 	    System.exit(0);
 	}
 	
 	try {
 	    OnlineConverter cnv = new OnlineConverter("Ascii", dbType, dbUrl,
 						      dbUser, dbPwrd);
 	    int configId = cnv.getDatabase().getConfigId(config);
 	    System.out.println("EP CONFIGURATION:\n\n"+
 			       cnv.getEpConfigString(configId));
 	    System.out.println("\n\nSM CONFIGURATION:\n\n"+
 			       cnv.getSmConfigString(configId));
 	} catch (Exception e) {
 	    System.err.println("Exception: "+e.getMessage());
 	    e.printStackTrace();
 	}
     }
     
     
 }
