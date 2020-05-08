 package xtremweb.role.cmdline;
 
 /**
  * CommandLineTool.java
  *
  *
  * Created: Fri Apr 14 10:35:22 2006
  *
  * @author <a href="mailto:Gilles.Fedak@inriq.fr">Gilles Fedak</a>
  * @version 1.0
  */
 import xtremweb.api.bitdew.*;
 import xtremweb.api.activedata.*;
 import xtremweb.api.transman.*;
 import xtremweb.serv.dc.*;
 import xtremweb.serv.dr.ProtocolUtil;
 import xtremweb.serv.ds.*;
 import xtremweb.core.iface.*;
 import xtremweb.core.log.*;
 import xtremweb.core.com.idl.*;
 import xtremweb.core.serv.*;
 import xtremweb.role.ui.*;
 import xtremweb.core.obj.dc.Data;
 import xtremweb.core.obj.dr.Protocol;
 import xtremweb.core.obj.dc.Locator;
 import xtremweb.core.obj.dt.Transfer;
 import xtremweb.core.obj.ds.Attribute;
 import xtremweb.serv.dt.OOBException;
 import xtremweb.serv.dt.OOBTransfer;
 import xtremweb.serv.dt.OOBTransferFactory;
 import xtremweb.serv.dt.jsaga.*;
 import xtremweb.gen.service.GenService;
 
 import java.io.*;
 import java.net.URISyntaxException;
 import jargs.gnu.CmdLineParser;
 
 
 import java.util.Vector;
 import java.util.ArrayList;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.JsonSyntaxException;
 
 
 /*!
  * @defgroup cmdline Using BitDew
  *  @{
 
  @section Quickstart
 
  The simplest way is to use the file @c bitdew-stand-alone-x.y.z.jar  which contains all the files and libraries inclued in one signle jar file.
 
  You can launch the command-line tool simply with the following command, which will display the usage :
 
  @code
  java -jar bitdew-stand-alone-x.y.z.jar
  @endcode
 
  and to obtain the complete list of options :
 
  @code
  java -jar bitdew-stand-alone-x.y.z.jar --help
  @endcode
 
  The tool can either start services or act as a client. 
 
  To start all of the services supported by BitDew simply run the  following command :
 
  @code
  java -jar bitdew-stand-alone-x.y.z.jar serv dc dr dt ds
  @endcode
 
  This will start the following services dc : Data Catalog, dr : Data Repository, dt : Data Transfer and ds: Data Scheduling.
 
 
  @section Invoking the command line tool
 
  The format for running the BitDew command line program is:
 
  @code
  java -jar  bitdew-stand-alone-x.y.z.jar [Options] Commands [Command Options]
  @endcode
 
  If the command line seems too long to type for you, we recommand to set an alias in your @file{.bashrc} as this :
 
  @code
  alias bitdew="java -jar  /path_to/bitdew-stand-alone-x.y.z.jar "
  @endcode
 
  @subsection basic Basic data operation
 
  To create a new data in BitDew from an existing file (for instance @c /tmp/foo.zip), simply run the command put with the name of the file, which will produce the following output :
 
  @code
  % java -jar  bitdew-stand-alone-x.y.z.jar put /tmp/foo.zip
  @endcode
 
  To schedule, you have to create an attribute and 
 
  @subsection help Obtaining help
 
  BitDew supports the following options, shown by
  the output of @c java -jar  bitdew-stand-alone-x.y.z.jar @c --help:
 
 
  @code
  BitDew command line client
 
  Usage : java -jar bitdew-stand-alone.jar [Options] Commands [Command Options]
 
  Options:
  -h, --help                    display this helps
  -v, --verbose                 display debugging information
  -d, --dir                     working directory
  -m, --media				   rmi or xmlrcp (default rmi)
  --host                    service hostname
  --port                    service port
  --protocol                file transfer protocol
  --file                    an optional file to load configuration
  Services:
  serv [dc|dr|dt|ds]        start the list of services separated by a space
 
  Attributes:
  attr attr_definition      create attribute where attr_definition has the syntax att_Name 
  = {field1=value1, field2=value2}.
  Field can have the following values :
  replicat=int          number of data replicat in the system. The special value -1    
  means that the data will be replicated to each node
  affinity=dataId       affinity to data Identifier. Schedule the data on node where   
  dataId is present.
  lftabs=int            absolute life time. The value is the life duration in minutes.
  lftabs=dataId         relative lifetime. The data will be obsolete when dataId is    
  deleted.
  oob=protocol          out-of-band file transfer protocol. Protocol can be one of the 
  following [dummy|ftp|bittorrent]
  ft=[true|false]       fault tolerance. If true data will be rescheduled if one host  
  holding the data is considered as dead.
  distrib=int           maximum number of data of this attribute, a host can hold. The 
  special value -1  means that this number is infinite
 
  Data:
  data file_name            create a new data from the file file_name
 
  Scheduling:
  sched attr_uid data_uid [data_uids ..... ]
  associate and attribute given by its uid to one one or several 
  data
 
 Protocol
 proto                      JOSE
 
  File:
  put file_name [dataId]    copy a file in the data space. If dataId is not specified, a new
  data will be created from the file.
  get dataId [file_name]    get the file from dataId. The default name of the file is the same as the data name. Otherwise, an alternate file name can be specified as an option.
  @endcode
 
  @subsection create Creating Data
 
  @subsection file Moving Files to and from the Data Space
 
  * @}
  */
 
 public class CommandLineTool {
     private String USAGE_PATH = "/bitdewusage.yaml";
     private BitDew bitdew;
     private ActiveData activeData;
     private TransferManager transferManager = null;
 
     private enum HelpFormat {
 	SHORT, LONG
 	    };

     private String host;
     private String dirName;
     private int port;
     private String protocol;
     private String media;
     private boolean verbose;
     private boolean server = false;
     private static Logger log = LoggerFactory.getLogger("CommandLineTool");
     private String fileName;
     public CommandLineTool(String[] args) {
 	log.setLevel("info");
 	// force the log4J configuration to log level info without formatting
 	if (log instanceof Log4JLogger) {
 	    try {
 		Log4JLogger.setProperties("conf/log4jcmdlinetool.properties");
 	    } catch (LoggerException le) {
 		log.debug(le.toString());
 	    }
 	}
 	String[] otherArgs = parse(args);
 	// switch to verbose mode
 	if (verbose)
 	    log.setLevel("debug");
 	
 	// if there's no other argument display helps
 	if (otherArgs.length == 0)
 	    usage(HelpFormat.SHORT);
 	if(fileName != null && !fileName.equals(""))
 	    {
 		System.setProperty("PROPERTIES_FILE", System.getProperty("user.dir") + File.separator +fileName);
 	    }
 	// start services
 	if (otherArgs[0].equals("serv")) {
 	    
 	    boolean skipserv = false;
 	    Vector services = new Vector();
 	    for (String s : otherArgs) {
 		// TODO the rest of the command line will be tried to be loaded
 		// as service
 		// that would be better to try scan for the available services
 		if (skipserv)
 		    services.add(s);
 		else
 		    skipserv = true;
 	    }
 	    if(!media.equals("rmi") && !media.equals("xmlrpc"))
 	    {
 	    	usage(HelpFormat.LONG);
 	    	return;
 	    }
 	    
 	    ServiceLoader sl = new ServiceLoader(media, port, services);
 	    UIFactory.createUIFactory();
 	    server = true;
 	    return;
 	}
 
 	if(otherArgs[0].equals("gen")){	
 	    gen(otherArgs);
 	    return;
 	}
 
 	//for the other command, we need to create a communication to the services
 	try {
	    Vector comms = ComWorld.getMultipleComms(host,media, port,"dc", "dr", "dt", "ds");
 	    activeData = new ActiveData(comms);
 	    bitdew = new BitDew(comms);
 	    transferManager = new TransferManager(comms);
 	} catch (ModuleLoaderException e) {
 	    log.fatal("Cannot find service " + e + "Make sure that your classpath is correctly set");
 	}
 
 	// add a protocol via commandLine
 	if (otherArgs[0].equals("proto")) {
 	    proto(otherArgs);
 	}
 
 	// create attr
 	if (otherArgs[0].equals("attr")) {
 	    attr(otherArgs);
 	}
 
 	// create data
 	if (otherArgs[0].equals("data")) {
 	    data(otherArgs);
 	} 
 
 	// schedule data and attribute
 	if (otherArgs[0].equals("sched")) {
 	    sched(otherArgs);
 	}// schedulde
 
 	// put file [dataId]
 	if (otherArgs[0].equals("put")) {
 	    put(otherArgs);
 	}// put
 
 	// get dataId [file]
 	if (otherArgs[0].equals("get")) {
 	    get(otherArgs);
 	}if (otherArgs[0].equals("bdii")) {
 	    bdii(otherArgs);
 	}
     } // CommandLineTool constructor
 
 
     private void bdii(String[] otherArgs) {
 	LDAPInterface ldap;
 	try {
 	    ldap = (LDAPInterface) new JndiLdapImpl();
 	    ldap.connect(otherArgs[1]);
 	    String url = ldap.searchByService(otherArgs[2]);
 	    ldap.close();
 	    Protocol p = ProtocolUtil.getProtocol(url);
 	    p.setclassName("xtremweb.serv.dt.jsaga.JsagaTransfer");
 	    log.info("Registering protocol :"+ p.getname()+"://" + p.getserver() + ":"+p.getport() +p.getpath());
 	    bitdew.registerNonSecuredProtocol(p.getname(), p.getclassName(),p.getserver(), p.getport(), p.getpath(), p.getlogin(), p.getpassword());
 	    log.info("Protocol registered");
 	} catch (LDAPEngineException e) {
 	    log.info("problem in ldap communication");
 	    e.printStackTrace();
 	}catch (URISyntaxException e) {
 	    log.info("problem in URI parsing");
 	    e.printStackTrace();
 	}
 	
     }
 
 
     private void gen(String[] otherArgs) {
 		
 	try {
 			
 	    new GenService(otherArgs);
 	} catch (IOException e) {
 	    log.fatal("There was a problem in writing your files " + e.getMessage());
 	}
     }
 
 
     /**
      * Add a json formatted repository
      * 
      * @param otherArgs
      *            the original arguments , the json argument is in otherArgs[1]
      */
     public void proto(String[] otherArgs) {
 	String object = otherArgs[1];
 	
 	String json = CommandLineToolHelper.jsonize(object);
 
 	JsonObject repo = new JsonParser().parse(json).getAsJsonObject();
 	// JsonObject repo = objectj.getAsJsonObject("repository");
 
 	if (repo != null) {
 	    CommandLineToolHelper.notNull("name", repo.get("name"));
 	    CommandLineToolHelper.notNull("path", repo.get("path"));
 	    CommandLineToolHelper.notNull("server", repo.get("server"));
 	    CommandLineToolHelper.notNull("port", repo.get("port"));
 
 	    String name = (String) repo.get("name").getAsString();
 	    String path = (String) repo.get("path").getAsString();
 	    String host = (String) repo.get("server").getAsString();
 	    String classname = (String) repo.get("className").getAsString();
 	    Long lon = (Long) repo.get("port").getAsLong();
 	    int port = lon.intValue();
 
 	    String login = CommandLineToolHelper
 		.nullOrObject(repo.get("login"));
 	    String passwd = CommandLineToolHelper.nullOrObject(repo
 							       .get("passwd"));
 
 	    String knownhosts = CommandLineToolHelper.nullOrObject(repo
 								   .get("knownhosts"));
 	    String prkeypath = CommandLineToolHelper.nullOrObject(repo
 								  .get("prkeypath"));
 	    String pukeypath = CommandLineToolHelper.nullOrObject(repo
 								  .get("pukeypath"));
 			
 	    String passphrase = CommandLineToolHelper.nullOrObject(repo
 								   .get("passphrase"));
 
 	    if (name.equals("http") || name.equals("ftp"))
 		bitdew.registerNonSecuredProtocol(name,classname, host, port, path,
 						  login, passwd);
 	    else
 		bitdew.registerSecuredProtocol(login, name, classname,host, port, path,
 					       knownhosts, prkeypath, pukeypath, passphrase);
 	    log.info("Protocol "+ name + " added succesfully ");
 	} else {
 	    log.info(" you need to describe a repository");
 	}
 
     }
 
     //java -cp temp-jar:build:conf xtremweb.role.cmdline.CommandLineTool  attr "{name:'vm', replicat:'-1'}"
     private void attr(String[] otherArgs) {
 	if (otherArgs.length == 1)
 	    usage(HelpFormat.LONG);
 	Attribute attr = null;
 	try {
 	    attr = AttributeUtil.parseAttribute(CommandLineToolHelper.jsonize(otherArgs[1]));
 	} catch (ActiveDataException ade) {
 	    log.warn(" Cannot parse attribute definition : " + ade);
 	} catch (JsonSyntaxException sex) {
 	    log.fatal("Syntax exception " + sex.getMessage());
 	}
 	try {
 	    Attribute _attr = activeData.registerAttribute(attr);
 	    log.info("attribute registred : "
 		     + AttributeUtil.toString(_attr));
 	} catch (ActiveDataException ade) {
 	    log.fatal(" Cannot registrer attribute : " + ade);
 	}
 	
     }
 
 
     private void sched(String[] otherArgs) {
 	String jsonize = CommandLineToolHelper.jsonize(otherArgs[1]);
 	JsonObject jsono = new JsonParser().parse(jsonize).getAsJsonObject();
 	if (jsono.get("attr_uid") == null) {
 	    log.fatal("Attribute id is mandatory");
 	}
 	String attr_uid = jsono.get("attr_uid").getAsString();
 	if (jsono.get("data_uids") == null) {
 	    log.fatal("Datas must be associated with attribute");
 	}
 	JsonArray array = jsono.get("data_uids").getAsJsonArray();
 	if (array.size() == 0) {
 	    log.fatal("Data array cannot be empty");
 	}
 	// verify that this attribute exists
 	Attribute attr = null;
 	try {
 	    attr = activeData.getAttributeByUid(attr_uid);
 	} catch (ActiveDataException ade) {
 	    log.fatal("Attribute with uid " + attr_uid
 		     + " doesn't exist in the system : " + ade);
 	}
 
 	// build the list of data to schedule and check them
 	ArrayList<Data> toSchedule = new ArrayList<Data>();
 	for (int i = 0; i < array.size(); i++) {
 	    try {
 		Data d = bitdew.searchDataByUid(array.get(i).getAsString());
 		if (d != null) {
 		    toSchedule.add(d);
 		} else
 		    log.info("Error : Data with uid " + otherArgs[i]
 			     + " doesn't exist in the system ");
 	    } catch (BitDewException bde) {
 		log.info("Data with uid " + otherArgs[i]
 			 + " doesn't exist in the system : " + bde);
 	    }
 	}
 
 	// exit if there is nothing to do
 	if (toSchedule.isEmpty())
 	    System.exit(2);
 
 	// schedule the data list
 	String msg = "Scheduling Data : ";
 	for (Data data : toSchedule) {
 	    try {
 		activeData.schedule(data, attr);
 		if (verbose)
 		    msg += "\n" + DataUtil.toString(data);
 		else
 		    msg += "[" + data.getname() + "|" + data.getuid()
 			+ "] ";
 	    } catch (ActiveDataException ade) {
 		log.info("Unable to schedule data " + "[" + data.getname()
 			 + "|" + data.getuid() + "] " + "with attribute "
 			 + AttributeUtil.toString(attr) + " : " + ade);
 	    }
 	}
 	String tmp = AttributeUtil.toString(attr);
 	log.info(msg.substring(0, msg.length() - 1) + (verbose ? "\n" : "")+ " with Attribute " + tmp.substring(5, tmp.length()));
 		
     }
 
     //put has one mandatory argument fileName and 1 optional argument dataId
     //additionally the option protocol can be used
     private void put(String[] otherArgs) {
 
 	OOBTransfer noobt = null;
 	if ( (otherArgs.length != 2) && (otherArgs.length != 3))
 	    usage(HelpFormat.LONG);
 	
 	File file = new File(otherArgs[1]);
 	if (!file.exists()) {
 	    log.fatal(" File does not exist : " + otherArgs[1]);
 	}
 
 	//retrive the protocol option
 	String myprot = (protocol!=null)?protocol:"http";
 	Data data = null;
 
 	try {
 	    // no dataId
 	    if (otherArgs.length == 2) {
 		data = bitdew.createData(file);
 		log.info("Data registred : " + DataUtil.toString(data));
 	    } else {
 		data = bitdew.searchDataByUid(otherArgs[2]);
 		if (data == null) {
 		    log.fatal("cannot find data whose uid is : "
 			     + otherArgs[2]);
 		}
 		bitdew.updateData(data, file);
 	    }
 	} catch (BitDewException ade) {
 	    log.fatal(" Cannot registrer data : " + ade);
 	}
 	try {
 	    OOBTransfer oobTransfer = bitdew.put(file, data, myprot);
	    //JOSE FIX DUPLICATION DU CODE, POURQUOI
	    Vector comms = ComWorld.getMultipleComms(host, "rmi", port,
						     "dr", "dc", "dt");
 	    TransferManager transman = TransferManagerFactory
 		.getTransferManager(
 				    (Interfacedt) comms.get(2));
 	    Protocol protoc = oobTransfer.getRemoteProtocol();
 	    //TOTALLY UNSECURE
 	    String passwd = protoc.getpassword();
 	    String passphrase = protoc.getpassphrase();
 	    if (passwd != null && passwd.equals("yes"))
 		oobTransfer = promptPassword(protoc, oobTransfer);
 	    if (passphrase != null && passphrase.equals("yes"))
 		oobTransfer = promptPassphrase(protoc, oobTransfer);
 	    transman.registerTransfer(oobTransfer);
 	    log.debug("Succesfully created OOB transfer " + oobTransfer);
 	    transman.waitFor(data);
 	    transman.stop();
 	    log.info("Transfer finished");
 	    //ATTENTION AU NIVEAU DE LOG: HOMOGENE !!!
 	} catch (TransferManagerException tme) {
 	    log.fatal("Error during data transfer : " + tme);
 	} catch (BitDewException bde) {
 	    log.fatal("Error during data transfer or creation : " + bde);
 	} catch (ModuleLoaderException mle) {
 	    log.fatal("Error when connecting to BitDew server : " + mle);
 	} catch (OOBException e) {
 	    log.fatal("Error during data transfer : " +e);
 	}
 	
     }
 
     private void get(String[] otherArgs) {
 	if ((otherArgs.length != 2) && (otherArgs.length != 3))
 	    usage(HelpFormat.LONG);
 	
 	String dataUid = otherArgs[1];
 	
 	// check the dataId
 	Data data = null;
 	try {
 	    data = bitdew.searchDataByUid(dataUid);
 	    if (data == null) {
 		log.fatal("cannot find data whose uid is : " + dataUid);
 	    }
 	    log.info("Get Data  : " + DataUtil.toString(data));
 	} catch (BitDewException ade) {
 	    log.fatal(" Cannot find data : " + ade);
 	}
 
 	// set the file name
 	String fileName = null;
 	if (otherArgs.length == 3) {
 	    fileName = otherArgs[2];
 	} else {
 	    fileName = data.getname();
 	}
 	// set the protocol
 	String myprot = (protocol!=null)?protocol:"http";
 	
 	// check the file
 	File file = new File(fileName);
 	if (file.exists()) {
 	    log.warn("warning, you will overwrite " + fileName
 		     + ". Do you really want to continue (y/N) ?");
 	    try {
 		BufferedReader stdIn = null;
 		stdIn = new BufferedReader(new InputStreamReader(System.in));
 		int i = stdIn.read();
 		if ((i != 121) && (i != 89))
 		    System.exit(0);
 	    } catch (IOException ioe) {
 		    log.fatal("program interrupted" + ioe);
 	    }
 	}
 	// get the data
 	try {
 	    transferManager.start(true);
 	    data.setoob(myprot);
 	    OOBTransfer oobt = bitdew.get(data, file);
 	    Protocol protoc = oobt.getRemoteProtocol();
 	    String passwd = protoc.getpassword();
 	    String passphrase = protoc.getpassphrase();
 	    if (passwd != null && passwd.equals("yes"))
 		oobt = promptPassword(protoc, oobt);
 	    if (passphrase != null && passphrase.equals("yes"))
 		oobt = promptPassphrase(protoc, oobt);
 	    transferManager.registerTransfer(oobt);
 	    log.debug("Succesfully created OOB transfer " + oobt);
 	    transferManager.waitFor(data);
 	    transferManager.stop();
 	    log.info("Transfer complete");
 	} catch (TransferManagerException ade) {
 	    log.fatal(" Transfer data : " + ade);
 	} catch (BitDewException bde) {
 	    log.fatal(" Transfer data : " + bde);
 	} catch (OOBException e) {
 	    e.printStackTrace();
 	}
     }// get
     
     public OOBTransfer promptPassphrase(Protocol protoc, OOBTransfer oobTransfer)
 	throws OOBException {
 	char[] passph;
 	Console c = System.console();
 	passph = c.readPassword("[%s]", "Please insert passphrase for key : "
 				+ protoc.getprivatekeypath());
 	protoc.setpassphrase(new String(passph));
 	java.util.Arrays.fill(passph, ' ');
 
 	Data datan = oobTransfer.getData();
 	Transfer t = oobTransfer.getTransfer();
 	Locator remote_locator = oobTransfer.getRemoteLocator();
 	Locator local_locator = oobTransfer.getLocalLocator();
 	Protocol local_proto = oobTransfer.getLocalProtocol();
 	return OOBTransferFactory.createOOBTransfer(datan, t, remote_locator,
 						    local_locator, protoc, local_proto);
     }
     
     public OOBTransfer promptPassword(Protocol protoc, OOBTransfer oobTransfer)
 	throws OOBException {
 	char[] password;
 	Console c = System.console();
 	password = c.readPassword("[%s]",
 				  "Please insert password for server : " + protoc.getserver());
 	String s = new String(password);
 	protoc.setpassword(s);
 	java.util.Arrays.fill(password, ' ');
 	Data datan = oobTransfer.getData();
 	Transfer t = oobTransfer.getTransfer();
 	Locator remote_locator = oobTransfer.getRemoteLocator();
 	Locator local_locator = oobTransfer.getLocalLocator();
 	Protocol local_proto = oobTransfer.getLocalProtocol();
 	return OOBTransferFactory.createOOBTransfer(datan, t, remote_locator,
 						    local_locator, protoc, local_proto);
     }
     
     public boolean isServer() {
 	return server;
     }
 
     private String[] parse(String[] args) {
 	// if there's no argument display helps
 	if (args.length == 0)
 	    usage(HelpFormat.SHORT);
 	
 	CmdLineParser parser = new CmdLineParser();
 
 	CmdLineParser.Option helpOption = parser.addBooleanOption('h', "help");
 	CmdLineParser.Option verboseOption = parser.addBooleanOption('v',
 								     "verbose");
 	CmdLineParser.Option dirOption = parser.addStringOption('d', "dir");
 	CmdLineParser.Option hostOption = parser.addStringOption("host");
 	CmdLineParser.Option fileOption = parser.addStringOption("file");
 	CmdLineParser.Option protocolOption = parser.addStringOption("protocol");
 	CmdLineParser.Option mediaOption = parser.addStringOption('m',"media");
 	try {
 	    parser.parse(args);
 	} catch (CmdLineParser.OptionException e) {
 	    log.info("Problem parsing arguments "+e.getMessage());
 	    usage(HelpFormat.SHORT);
 	}
 
 	host = (String) parser.getOptionValue(hostOption, "localhost");
 	fileName = (String) parser.getOptionValue(fileOption);
 	dirName = (String) parser.getOptionValue(dirOption, ".");
 	protocol = (String) parser.getOptionValue(protocolOption,"http");
 	media = (String) parser.getOptionValue(mediaOption,"rmi");
 	boolean help = ((Boolean) parser.getOptionValue(helpOption,
 							Boolean.FALSE)).booleanValue();
 	verbose = ((Boolean) parser
 		   .getOptionValue(verboseOption, Boolean.FALSE)).booleanValue();
 
 	// if the help option is set display long help
 	if (help)
 	    usage(HelpFormat.LONG);
 
 	return parser.getRemainingArgs();
     }
 	
     public void data(String[] otherArgs){
 	try {
 	    JsonObject jsono;
 	    File f;
 	    String s, file = null, str = null;
 	    Data data = null;
 		
 	    f = new File(otherArgs[1]);
 	    if (f.exists()) {
 		data = bitdew.createData(f);
 		log.info("Data registred : " + DataUtil.toString(data));
 	    } else {
 		jsono = new JsonParser().parse(otherArgs[1])
 		    .getAsJsonObject();
 		    
 		if (jsono.get("file") == null && jsono.get("string") == null) {
 		    log.fatal("Syntax error, see usage ");
 		}
 		if (jsono.get("file") != null && jsono.get("string") != null) {
 		    log.fatal("Syntax error, see usage ");
 		}
 		if (jsono.get("file") != null)
 		    file = jsono.get("file").getAsString();
 		if (jsono.get("string") != null)
 		    str = jsono.get("string").getAsString();
 		if (file != null) {
 		    f = new File(file);
 		    if (!f.exists()) {
 			log.fatal(" File does not exist : " + otherArgs[1]);
 		    }
 		    data = bitdew.createData(f);
 		    log.info("Data registred : " + DataUtil.toString(data));
 		}
 		if (str != null) {
 		    data = bitdew.createData(str);
 		    log.info("Data registred : " + DataUtil.toString(data));
 		}
 	    }
 	}catch (BitDewException ade) {
 	    log.fatal(" Cannot registrer data : " + ade);
 	} catch (java.lang.IllegalStateException exc) {
 	    log.warn("Not a json object, probably you have spaces in your JSON object, if is the case use quotation marks");
 	}
     }
     
     public void usage(HelpFormat format) {
 	try {
 	    String rest="" ;
 	    Usage usage = new Usage();
 	    switch (format) {
 	    case LONG:
 		BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(USAGE_PATH)));
 		String s;
 		s = br.readLine();
 		while (s != null) {
 		    // s has the form [option2|option3|section...]: <value1> , <valu2> , <value3>
 		    //log.info("line is "+ s);
 		    String[] tokens = s.split(":");
 		    String rests = s.substring(s.indexOf(":")+1);
 		    // commasep is <value1> , <valu2> , <value3>
 		    String[] commasep = rests.split(",");		
 		    // commasep1 is the first value of the comma separated list
 		    String commasep1 = commasep[0];
 		    if ( tokens[1].indexOf(",") != -1)
 			rest = rests.substring(tokens[1].indexOf(",")+1);
 		    if (tokens[0].equals("usage"))
 			usage.usage(tokens[1]);
 		    if (tokens[0].equals("title"))
 			usage.title();
 		    if (tokens[0].equals("section"))
 			usage.section(tokens[1]);
 		    if (tokens[0].equals("option2")) {
 			//log.info("out " + rest + ", tokens is " + tokens[1]);
 			usage.option(commasep1, rest);
 		    }
 		    if (tokens[0].equals("option3")) {
 			usage.option(commasep[0], commasep[1], commasep[2]);
 		    }
 		    if (tokens[0].equals("ln")) {
 			usage.ln();
 		    }
 		    s = br.readLine();
 		}
 		break;
 	    case SHORT:
 		usage.usage("try java -jar bitdew-stand-alone-" + Version.versionToString()
 			+ ".jar [-h, --help] for more information");
 		break;
 	    }
 	    System.exit(2);
 	} catch (IOException e) {
 	    e.printStackTrace();
 	}
     }
 
     public static void main(String[] args) {
 	CommandLineTool cmd = new CommandLineTool(args);
 
 	// FIXME We shouldn't have to explicitely exit
 	if (!cmd.isServer())
 	    System.exit(0);
 
     }
 } // CommandLineTool
