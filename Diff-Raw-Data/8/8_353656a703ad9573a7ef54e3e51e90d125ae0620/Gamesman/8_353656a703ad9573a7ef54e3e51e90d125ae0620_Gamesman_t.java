 package edu.berkeley.gamesman;
 
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Properties;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * An entry main for all of gamesman-java.
  * Any application that wants to be part of this should extend GamesmanApplication
  * @author Jeremy Fleischman
  *
  */
 public class Gamesman {
 	private static final HashMap<String, String> APPLICATION_MAP = new HashMap<String, String>();
 	static {
 		APPLICATION_MAP.put("gamesmanmain", "edu.berkeley.gamesman.GamesmanMain");
 		APPLICATION_MAP.put("jythoninterface", "edu.berkeley.gamesman.JythonInterface");
 		APPLICATION_MAP.put("gamesmanshell", "edu.berkeley.gamesman.GamesmanShell");
 		APPLICATION_MAP.put("jsoninterface", "edu.berkeley.gamesman.JSONInterface");
		APPLICATION_MAP.put("databasedump", "edu.berkeley.gamesman.tool.DatabaseDump");
 	}
 	/**
 	 * @param args The command line arguments. Should just be a job file.
 	 */
 	public static void main(String[] args) {
 		String jobFile = null, entryPoint = null;
 		if(args.length == 1) {
 			String arg = args[0];
			if(APPLICATION_MAP.containsKey(arg.toLowerCase())) {
 				//we have a GamesmanApplication
				entryPoint = arg.toLowerCase();
 			} else {
 				//if we just have a job file, use GamesmanMain
 				jobFile = args[0];
 				entryPoint = "gamesmanmain";
 			}
 		} else if(args.length == 2) {
 			entryPoint = args[0]; jobFile = args[1];
 		} else {
 			System.out.println("Usage: Gamesman [entry point] [job file]" +
 					"\n\tAvailable entry points (some require a job file, some don't): " + APPLICATION_MAP.keySet() + 
 					"\n\tIf a job file is given, but no entry point is, entry point defaults to GamesmanMain.");
 			return;
 		}
 		
 		if(!APPLICATION_MAP.containsKey(entryPoint)) {
 			System.out.println("Couldn't find " + entryPoint + " in " + APPLICATION_MAP.keySet());
 			return;
 		}
 		Configuration conf = null;
 		if(jobFile != null) {
 			Properties props = Configuration.readProperties(jobFile);
 			EnumSet<DebugFacility> debugOpts = EnumSet.noneOf(DebugFacility.class);
 			ClassLoader cl = ClassLoader.getSystemClassLoader();
 			cl.setDefaultAssertionStatus(false);
 			for(DebugFacility f: DebugFacility.values()){
 				if(parseBoolean(props.getProperty("gamesman.debug."+f.toString(), "false"))) {
 					debugOpts.add(f);
 					f.setupClassloader(cl);
 				}
 			}
 			if(!debugOpts.isEmpty()) {
 				debugOpts.add(DebugFacility.CORE);
 				DebugFacility.CORE.setupClassloader(cl);
 			}
 			Util.enableDebuging(debugOpts);
 			conf = new Configuration(props);
 		}
 
 		Class<? extends GamesmanApplication> cls = Util.typedForName(APPLICATION_MAP.get(entryPoint));
 		try {
 			GamesmanApplication ga = cls.getConstructor().newInstance();
 			ga.run(conf);
 		} catch (Exception e) {
 			Util.fatalError("Could not construct " + cls.getName(), e);
 		}
 	}
 
 	//This is a copy of Util.parseBoolean().
 	//It needs to be here to avoid loading the Util class before we're ready to
 	private static boolean parseBoolean(String s) {
 		return s != null && !s.equalsIgnoreCase("false") && !s.equalsIgnoreCase("0");
 	}
 }
