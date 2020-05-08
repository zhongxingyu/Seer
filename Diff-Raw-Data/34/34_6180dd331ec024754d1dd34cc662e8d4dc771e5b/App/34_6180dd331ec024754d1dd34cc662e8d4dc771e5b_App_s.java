 package com.basho.proserv.riakmdcmon;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 import com.basho.proserv.riakmdcmon.Alarm.Alarm;
 import com.basho.proserv.riakmdcmon.Alarm.AlarmHandlerFactory;
 import com.basho.proserv.riakmdcmon.Alarm.AlarmIo;
 import com.basho.proserv.riakmdcmon.data.RiakConnection;
 import com.basho.proserv.riakmdcmon.data.RiakConnectionFactory;
 import com.basho.proserv.riakmdcmon.data.RiakSyncData;
 
 public class App 
 {
 	public static String CURRENT_ALARMS_FILENAME = "alarms.xml";
 	
     public static void main( String[] args ) 
     {
     	String propertiesFilename = null;
     	File propertiesFile = null;
     	boolean test = false;
     	boolean intol = false;
 		CommandLine cmd = null;
 		try {
 			cmd = parseCommandLine(createOptions(), args);
 		} catch (ParseException e) {
 			System.out.println("Error parsing command line. Reason: " + e.getMessage());
 			System.exit(1);
 		}
 				
 		if (cmd.hasOption('p')) {
 			propertiesFilename = cmd.getOptionValue('p');
 		} else {
 			System.out.println("Properties file not specified, exiting");
 			System.exit(1);
 		}
 		propertiesFile = new File(propertiesFilename);
 		if (!propertiesFile.exists()) {
 			System.out.println(String.format("The properties file %s does not exist. Exiting.", propertiesFilename));
 		}
 		
 		Configuration config = null;
 		try {
 			config = Configuration.createConfigurationFromProperties(propertiesFile);
 		} catch (IOException e) {
 			System.out.println(String.format("Couldn't read configuration file %s. Exiting.", propertiesFile.getAbsolutePath()));
 			System.exit(1);
 		}
 		
 		if (cmd.hasOption('t')) {
 			test = true;
 		}
 		
 		if (cmd.hasOption('n')) {
 			intol = true;
 		}
 		checkDCLatencies(config, test, intol);
     }
     
     private static CommandLine parseCommandLine(Options options, String[] args) throws ParseException {
 		CommandLineParser parser = new GnuParser();
 		CommandLine cmd = parser.parse(options, args);
 		return cmd;
 	}
     
     private static Options createOptions() {
     	Options options = new Options();
     	
     	options.addOption("t",false,"Test configured. Will generate alarms for all configured Alarm Handlers");
     	options.addOption("n",false,"Test in tolerance configured. Will generate in tolernace alarms for all configured Alarm Handlers");
     	options.addOption("p",true,"Specify configuration properties file");
     	
     	return options;
     }
     
     public static void checkDCLatencies(Configuration config, boolean test, boolean intol) {
     	RiakConnection conn = new RiakConnectionFactory(config).getRiakConnection();
     	
     	RiakSyncData syncCheck = new RiakSyncData(config, conn, new AlarmHandlerFactory(config));
     	
    	String alarmsPath = ClassLoader.getSystemClassLoader().getResource(".").getPath() + 
    			"/" + CURRENT_ALARMS_FILENAME;
     	File alarmsFile = new File(alarmsPath);
     	List<Alarm> previousAlarms = new ArrayList<Alarm>();
     	AlarmIo alarmIo = new AlarmIo(new File(alarmsPath));
     	if (alarmsFile.exists()) {
 	    	try {
 	    		previousAlarms = alarmIo.readAlarms();
 	    	} catch (IOException e) {
 	    		System.out.println("Alarms file " + alarmsPath + " could not be read");
 	    	}
     	}
     	
     	if (test || intol) {
 	    	syncCheck.test(intol);
 	    	if (intol) {
 	    		previousAlarms = getOuttolAlarms(config);
 	    	}
     	} else {
     		if (!conn.testConnection()) {
         		System.out.println("Could not establish connection to Riak. Exiting.");
         		System.exit(1);
         	}
     		
     		try {
 	    		syncCheck.writeOwnSyncData();
 	    		syncCheck.readRemoteDCSyncData();
 	    	} catch (IOException ex) {
 	    		System.out.println("Couldn't communicate with Riak. Exiting.");
 	    		System.exit(1);
 	    	}
     	}
     	
     	List<Alarm> alarms = syncCheck.getOutOfToleranceDataCenters(config.getErrorThreshold(), previousAlarms);
     	try {
     		alarmIo.writeAlarms(alarms);
     	} catch (IOException e) {
     		System.out.println("Alarms file " + alarmsPath + " could not be written");
     	}
     	
     	syncCheck.handleAlarms(alarms);
     }
     
     private static List<Alarm> getOuttolAlarms(Configuration config) {
     	ArrayList<Alarm> alarms = new ArrayList<Alarm>();
     	
     	for (String id : config.getRemoteDataCenterIds()) {
     		alarms.add(new Alarm(id, 
     				System.currentTimeMillis(), 
     				System.currentTimeMillis() - (config.getErrorThreshold()-1),
     				true));
     	}
     	
     	return alarms;
     }
 }
