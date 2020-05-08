 package org.github.nas774.piax.piaxshell;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.InvocationTargetException;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Properties;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.piax.agent.AgentHome;
 import org.piax.agent.AgentId;
 import org.piax.agent.AgentPeer;
 import org.piax.agent.NoSuchAgentException;
 import org.piax.trans.common.PeerLocator;
 import org.piax.trans.common.ReturnSet;
 import org.piax.trans.ts.tcp.TcpLocator;
 import org.piax.trans.util.LocalInetAddrs;
 import org.piax.trans.util.MersenneTwister;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class PIAXShell {
     private static final Logger logger = LoggerFactory
             .getLogger(PIAXShell.class);
 
     private static final String PROPERTY_FILE = "piaxshell.properties";
 
     private static final int DEFAULT_PIAX_PORT = 12367;
     private static final String DEFAULT_PEERNAME_PREFIX = "PEER";
     private static final String DEFAULT_AGENT_DIRECTORY = "."; // Default AgentClassFile directory.
     private static final boolean DEFAULT_PEER_AUTOJOIN = false;
     private static final boolean DEFAULT_USE_INTERACTIVESHELL = true;
 
     private AgentPeer peer = null;
 
     protected String peerName = "";
     private PeerLocator myLocator = null;
     private Collection<PeerLocator> seeds = null;
     private File[] agclassesdirs = null;
 
     private boolean autojoin = DEFAULT_PEER_AUTOJOIN;
 
     private boolean use_interactiveshell = DEFAULT_USE_INTERACTIVESHELL;
 
     private File agprop = null;
 
     public static void main(String[] args) {
         PIAXShell shell = new PIAXShell();
 
         if (!shell.initSettings(args)) {
             printUsage();
             return;
         }
         shell.startShell();
     }
 
     private static void printUsage() {
         System.out
                .println("Usage: PIAXShell [options] name\n"
                         + "  -j means join automatically after booted.\n"
                         + "  -I means use interactive shell. (default)\n"
                         + "             if add '-' after 'I', disables interactive shell.\n"
                         + "  -e <addr> sets piax address to <addr>\n"
                         + "  -r <port> sets piax port to <spec>\n"
                         + "  -s <seed> sets seed peer address to <seed>\n"
                         + "             <seed> to be host:port form\n"
                         + "  -p <property file> use <property file> instead of default property file\n"
                         + "             if add '-' after 'p', ignores default property file.\n"
                         + "  -a <agent dir> set agent class file directory to <agent dir>\n"
                         + "  -A <agent property> use agent property file or directory instead of default property to <agent property>\n"
                        + "  ex. PIAXShell -r 1000 root\n"
                        + "  ex. PIAXShell -r 1000 -s 192.168.1.101:2000 foo\n");
     }
 
     /**
      * Initialize instance by command line argument.
      * 
      * @param args
      *            command line argument.
      * @return true:success false:false, maybe some errors occuered.
      */
     protected boolean initSettings(String args[]) {
         try {
             String propfile = PROPERTY_FILE;
 
             String tmp_peername = "";
             String tmp_piaxaddress = "";
             String tmp_piaxport = "";
             String tmp_seed = "";
             boolean tmp_autojoin = false;
             boolean tmp_use_interactiveshell = true;
             String tmp_agtdir = "";
             String tmp_agentprops_str = "";
 
             // Search 'p' option first.
             // If 'p' option is found, read property from given file.
             // If not, read default property file.
             for (int i = 0; i < args.length; i++) {
                 if (args[i].charAt(1) == 'p') {
                     if (2 < args[i].length() && '-' == args[i].charAt(2)) {
                         propfile = null;
                     } else {
                         i++;
                         if (i < args.length) {
                             propfile = args[i];
                         }
                     }
                 }
             }
 
             if (propfile != null) {
                 File f = new File(propfile);
                 boolean fileexist = f.exists() && f.isFile();
 
                 if (!fileexist) {
                     logger.warn("Property file not found. : " + propfile);
                 } else {
                     if (PROPERTY_FILE.equals(propfile)) {
                         logger.info("Found default property file." );
                     }
 
                     logger.info("Load from property file. : " + propfile);
                     // Try to read property file.
                     Properties serverprop = new Properties();
                     try {
                         serverprop.load(new FileInputStream(propfile));
                         // may
                         if (serverprop.containsKey("piax.peer.name")) {
                             tmp_peername = serverprop.getProperty("piax.peer.name");
                         }
                         // may
                         if (serverprop.containsKey("piax.peer.address")) {
                             tmp_piaxaddress = serverprop.getProperty("piax.peer.address");
                         }
                         // may
                         if (serverprop.containsKey("piax.peer.port")) {
                             tmp_piaxport = serverprop.getProperty("piax.peer.port");
                         }
                         // may
                         if (serverprop.containsKey("piax.peer.seed")) {
                             tmp_seed = serverprop.getProperty("piax.peer.seed");
                         }
                         // may
                         if (serverprop.containsKey("piax.peer.autojoin")) {
                             String tmp_autojoin_str = serverprop
                                     .getProperty("piax.peer.autojoin");
                             if (tmp_autojoin_str != null
                                     && !tmp_autojoin_str.equals("")
                                     && !tmp_autojoin_str.equals("0"))
                                 tmp_autojoin = true;
                         }
                         // may
                         if (serverprop.containsKey("piax.shell.useinteractive")) {
                             String tmp_use_interactiveshell_str = serverprop
                                     .getProperty("piax.shell.useinteractive");
                             tmp_use_interactiveshell_str = tmp_use_interactiveshell_str.trim();
                             if (tmp_use_interactiveshell_str != null && 
                                 !tmp_use_interactiveshell_str.equals("")) {
                                 if (tmp_use_interactiveshell_str.equals("0"))
                                     tmp_use_interactiveshell = false;
                             }
                         }
                         // may
                         if (serverprop.containsKey("piax.agent.directory")) {
                             tmp_agtdir = serverprop.getProperty("piax.agent.directory");
                         }
                         // may
                         if (serverprop.containsKey("piax.agentprops")) {
                             tmp_agentprops_str = serverprop
                                     .getProperty("piax.agentprops");
                         }
                     } catch (FileNotFoundException e) {
                         logger.warn("Property file not found. : " + propfile);
                     } catch (IOException e) {
                         logger.warn("IO error at reading property file. : " + propfile);
                     }
                 }
             }
 
             boolean isFault = false;
 
             // Read setting from command line options.
             // Configurations in property file are overwritten by command-line options.
             for (int i = 0; i < args.length; i++) {
                 String arg = args[i].trim();
                 if (args[i].startsWith("-")) {
                     switch (arg.charAt(1)) {
                     case 'e':
                         i++;
                         if (i < args.length) {
                             tmp_piaxaddress = args[i];
                         }
                         break;
                     case 'r':
                         i++;
                         if (i < args.length) {
                             tmp_piaxport = args[i];
                         }
                         break;
                     case 'j': {
                         tmp_autojoin = true;
                         String tmp_autojoin_str = arg.substring(2);
                         if (tmp_autojoin_str.equals("-"))
                             tmp_autojoin = false;
                         break;
                     }
                     case 'I': {
                         tmp_use_interactiveshell = true;
                         String tmp_use_interactiveshell_str = arg.substring(2);
                         if (tmp_use_interactiveshell_str.equals("-"))
                             tmp_use_interactiveshell = false;
                         break;
                     }
                     case 's':
                         i++;
                         if (i < args.length) {
                             tmp_seed = args[i];
                         }
                         break;
                     case 'p':
                         if (2 < args[i].length() && '-' == args[i].charAt(2)) {
                         } else {
                             i++;
                         }
                         // already proced.
                         break;
                     case 'A':
                         i++;
                         if (i < args.length) {
                             tmp_agentprops_str = args[i];
                         }
                         break;
                     case 'a':
                         i++;
                         if (i < args.length) {
                             tmp_agtdir = args[i];
                         }
                         break;
                     case '?':
                         return false;
                     default:
                         return false;
                     }
                 } else {
                     tmp_peername = arg;
                 }
             }
 
             // Setup instance fields.
             if (tmp_peername.equals("")) {
                 logger.warn("Peer name is not specified. Set generated name.");
                 tmp_peername = DEFAULT_PEERNAME_PREFIX
                         + new MersenneTwister().nextInt(10 * 6);
             }
             peerName = tmp_peername;
             logger.info("Peer name : " + peerName);
 
             if (tmp_agtdir.equals("")) {
                 logger.warn("Agent class file directory is not specified. Set default.");
                 tmp_agtdir = DEFAULT_AGENT_DIRECTORY;
             }
             File agtdir = new File(tmp_agtdir);
             if (agtdir.exists()) {
                 logger.info("Agent class file directory : " + tmp_agtdir);
                 agclassesdirs = new File[] { agtdir };
             } else {
                 logger.error("Agent class file directory you specified isn't found. : "
                                 + tmp_agtdir);
                 isFault = true;
             }
 
             autojoin = tmp_autojoin;
             logger.info("Auto join : " + autojoin);
 
             use_interactiveshell = tmp_use_interactiveshell;
             logger.info("Use Interactive Shell : " + use_interactiveshell);
 
             agprop = null;
             if (tmp_agentprops_str != null && !tmp_agentprops_str.equals("")) {
                 File tmp_agprop = new File(tmp_agentprops_str);
                 if (!tmp_agprop.exists()) {
                     logger.warn("Agent property file or directory can not be found. : "
                                     + tmp_agentprops_str);
                 } else {
                     agprop = tmp_agprop;
                 }
             }
             if (agprop == null) {
                 logger.info("Agent property file or directory : <not use>");
             } else {
                 if (agprop.isFile()) {
                     logger.info("Agent property file : "
                             + agprop.getAbsolutePath());
                 } else if (agprop.isDirectory()) {
                     logger.info("Agent property directory : "
                             + agprop.getAbsolutePath());
                 }
             }
 
             if (tmp_piaxaddress.equals("")) {
                 logger.warn("A PIAX address is not specified. Choose appropriate address.");
                 tmp_piaxaddress = LocalInetAddrs.choice().getHostAddress();
             }
             if (tmp_piaxport.equals("")) {
                 logger.warn("A PIAX port is not specified. Set default.");
                 tmp_piaxport = Integer.toString(DEFAULT_PIAX_PORT);
             }
 
             logger.info("PIAX address : " + tmp_piaxaddress);
             logger.info("PIAX port : " + tmp_piaxport);
             myLocator = new TcpLocator(new InetSocketAddress(tmp_piaxaddress, Integer.parseInt(tmp_piaxport)));
             if (!tmp_seed.equals("")) {
                 String[] seedEle = tmp_seed.split(":");
                 seeds = Collections.singleton((PeerLocator) new TcpLocator(
                         new InetSocketAddress(seedEle[0], Integer
                                 .parseInt(seedEle[1]))));
             } else {
                 logger.info("Seed peers are not specified. Run as a seed peer.");
                 seeds = Collections.singleton(myLocator);
             }
 
             return !isFault;
         } catch (Exception e) {
             logger.error(e.getMessage(), e);
             return false;
         }
     }
 
     public void startShell() {
         // - setup phase
 
         // Initialize PIAX
         try {
             peer = new AgentPeer(null, peerName, myLocator, seeds, false,
                     agclassesdirs);
         } catch (IOException e) {
             logger.error("*** PIAX not started as IO Error.", e);
             return;
         } catch (IllegalArgumentException e) {
             logger.error("*** PIAX not started as Argument Error.", e);
             return;
         }
 
         // Create agents from agent property files.
         if (agprop != null) {
             if (agprop.isDirectory()) {
                 for (File file : agprop.listFiles()) {
                     createAgentFromPropertyFile(file);
                 }
             } else {
                 createAgentFromPropertyFile(agprop);
             }
         }
 
         // Auto join
         if (autojoin) {
             join(); // Join to PIAX network.
             logger.info("PIAX joined.");
         }
 
         // Activate agents.
         notifyActivate();
 
         try {
             logger.info("Finished initializing");
             if (use_interactiveshell) {
                 // Run console interactive shell.
                 logger.info("Start interactive shell.");
                 mainLoop();
             } else {
                 try {
                     while (true) {
                         Thread.sleep(5 * 60 * 1000);
                     }
                 } catch (InterruptedException e) {
                 }
             }
 
             // - Terminate phase
 
         } catch (Exception e1) {
             logger.error(e1.getMessage(), e1);
         }
 
         // Dispose all agents.
         Set<AgentId> ags = new HashSet<AgentId>();
         for (AgentId agId : peer.getHome().getAgentIds()) {
             ags.add(agId);
         }
         for (AgentId agId : ags) {
             try {
                 peer.getHome().destroyAgent(agId);
             } catch (NoSuchAgentException e) {
                 logger.debug("Ignore a NoSuchAgentException.");
             }
         }
 
         // Terminate PIAX
         if (peer.isOnline())
             leave(); // Leave from PIAX network.
         peer.fin(); // Finalize PIAX.
     }
 
     /**
      * Create agents from agent property files.
      * 
      * @param file
      *            A directory which has agent property.
      */
     private void createAgentFromPropertyFile(File file) {
         logger.info("Opening property file : " + file.getName());
         Properties agentprop = new Properties();
 
         try {
             agentprop.load(new FileInputStream(file));
         } catch (FileNotFoundException e) {
             logger.error("Agent property file not found.");
             // Ignore unreadable file.
             return;
         } catch (IOException e) {
             logger.error("IO error when loading agent property file.");
             // Ignore unreadable file.
             return;
         }
 
         String classname = agentprop.getProperty("piax.agent.class");
         String agentname = agentprop.getProperty("piax.agent.name");
 
         // Check null or empty element.
         if (classname == null || classname.equals("")) {
             logger.error("piax.agent.class is null : " + classname);
             return;
         }
         if (agentname == null || agentname.equals("")) {
             logger.error("piax.agent.name is null : " + agentname);
             return;
         }
 
         try {
             AgentHome home = peer.getHome();
             AgentId agtid = home.createAgent(classname, agentname);
             if (!(Boolean) home.call(agtid, "initAgent", new Object[] { file })) {
                 logger.error("Failed initializing Agent. Class:" + classname
                         + " Name:" + agentname);
                 // Dispose if creating agent failed.
                 home.destroyAgent(agtid);
                 return;
             }
             logger.info("Craeted an agent named " + agentname + " based by " + classname + " as ID:" + agtid);
         } catch (Exception e) {
             logger.error(e.getMessage(), e);
         }
     }
 
     private void notifyActivate() {
         AgentHome home = peer.getHome();
         for (AgentId aid : home.getAgentIds()) {
             home.callOneway(aid, "activate", new Object[]{});
         }
     }
 
     private void printHelp() {
         System.out
                 .print(" *** PIAXShell Help ***\n"
                         + "  i)nfo           show my peer information\n"
                         + "  \n"
                         + "  ag)ents [cat]   list agents have specific category\n"
                         + "  \n"
                         + "  join            join P2P net\n"
                         + "  leave           leave from P2P net\n"
                         + "  \n"
                         + "  mk)agent class name [cat]\n"
                         + "                  create new agent as class and set name and category\n"
                         + "  mk)agent file\n"
                         + "                  create new agent by using file describes agent info\n"
                         + "  dup agent_NO    duplicate agent from which indicated by agent_NO\n"
                         + "  sl)eep agent_NO sleep agent indicated by agent_NO\n"
                         + "  wa)ke agetn_NO  wakeup agent indicated by agent_NO\n"
                         + "  fin agent_NO    destroy agent indicated by agent_NO\n"
                         + "  dc,discover method arg ...\n"
                         + "                  discoveryCall to agents in whole area\n"
                         + "  \n" + "  c)all agent_NO method arg ...\n"
                         + "                  call agent method\n" + "  \n"
                         + "  ?,help          show this help message\n"
                         + "  bye             exit\n" + "\n");
     }
 
     private String[] split(String line) {
         ArrayList<String> lines = new ArrayList<String>();
         StringTokenizer st = new StringTokenizer(line);
         while (st.hasMoreTokens()) {
             lines.add(st.nextToken());
         }
         String[] rlines = new String[lines.size()];
         return (String[]) lines.toArray(rlines);
     }
 
     private List<AgentId> agents;
 
     long stime;
 
     void mainLoop() {
         agents = new ArrayList<AgentId>();
         BufferedReader reader = new BufferedReader(new InputStreamReader(
                 System.in));
 
         while (true) {
             try {
                 System.out.print("Input Command >");
                 String input = null;
                 // countermeasure for background running with "< /dev/null".
                 while (true) {
                     input = reader.readLine();
                     if (input == null) {
                         Thread.sleep(500);
                         continue;
                     }
                     break;
                 }
                 String[] args = split(input);
                 if (args.length == 0) {
                     continue;
                 }
 
                 stime = System.currentTimeMillis();
 
                 if (args[0].equals("info") || args[0].equals("i")) {
                     if (args.length != 1) {
                         printHelp();
                         continue;
                     }
                     info();
                 } else if (args[0].equals("agents") || args[0].equals("ag")) {
                     if (args.length > 1) {
                         printHelp();
                         continue;
                     }
                     if (args.length == 1) {
                         agents();
                     }
                 } else if (args[0].equals("join")) {
                     if (args.length != 1) {
                         printHelp();
                         continue;
                     }
                     join();
                 } else if (args[0].equals("leave")) {
                     if (args.length != 1) {
                         printHelp();
                         continue;
                     }
                     leave();
                 } else if (args[0].equals("mkagent") || args[0].equals("mk")) {
                     if (args.length < 2 || args.length > 4) {
                         printHelp();
                         continue;
                     }
                     if (args.length == 2) {
                         mkagent(args[1]);
                     } else if (args.length == 3) {
                         mkagent(args[1], args[2]);
                     }
                 } else if (args[0].equals("dup")) {
                     if (args.length != 2) {
                         printHelp();
                         continue;
                     }
                     dup(Integer.parseInt(args[1]));
                 } else if (args[0].equals("sleep") || args[0].equals("sl")) {
                     if (args.length != 2) {
                         printHelp();
                         continue;
                     }
                     sleep(Integer.parseInt(args[1]));
                 } else if (args[0].equals("wake") || args[0].equals("wa")) {
                     if (args.length != 2) {
                         printHelp();
                         continue;
                     }
                     wake(Integer.parseInt(args[1]));
                 } else if (args[0].equals("fin")) {
                     if (args.length != 2) {
                         printHelp();
                         continue;
                     }
                     fin(Integer.parseInt(args[1]));
                 } else if (args[0].equals("discover") || args[0].equals("dc")) {
                     if (args.length < 2) {
                         printHelp();
                         continue;
                     }
                     Object[] cargs = new String[args.length - 2];
                     for (int i = 0; i < cargs.length; i++) {
                         cargs[i] = args[i + 2];
                     }
                     discover(args[1], cargs);
                 } else if (args[0].equals("call") || args[0].equals("c")) {
                     if (args.length < 3) {
                         printHelp();
                         continue;
                     }
                     Object[] cargs = new String[args.length - 3];
                     for (int i = 0; i < cargs.length; i++) {
                         cargs[i] = args[i + 3];
                     }
                     call(Integer.parseInt(args[1]), args[2], cargs);
                 } else if (args[0].equals("help") || args[0].equals("?")) {
                     printHelp();
                     continue;
                 } else if (args[0].equals("bye")) {
 
                     if (peer.isOnline()) {
                         leave();
                     }
                     return;
                 } else {
                     printHelp();
                     continue;
                 }
 
                 long etime = System.currentTimeMillis();
                 System.out.println("\t## time (msec): " + (etime - stime));
 
             } catch (NumberFormatException e) {
                 System.out.println("\t>> arg should be number.");
             } catch (Exception e) {
                 logger.error(e.getMessage(), e);
             }
         }
     }
 
     void info() {
         try {
             System.out.println(" peerName: " + peerName);
             System.out.println(" peerId: " + peer.getHome().getPeerId());
             System.out.println(" locator: " + myLocator);
             System.out.println(" location: " + peer.getHome().getLocation());
             System.out.println(peer.getOverlayMgr().showTable());
         } catch (Exception e) {
             logger.warn(e.getMessage(), e);
         }
     }
 
     void agents() {
         try {
             AgentHome home = peer.getHome();
             agents.clear();
             int i = 0;
             for (AgentId agId : home.getAgentIds(null)) {
                 agents.add(agId);
                 String name = home.getAgentName(agId);
                 boolean isSleeping = home.isAgentSleeping(agId);
                 System.out.println(" " + i + ". name: " + name + ", ID: "
                         + agId + (isSleeping ? " <sleep>" : ""));
                 i++;
             }
         } catch (NoSuchAgentException e) {
             logger.warn(e.getMessage(), e);
         }
     }
 
     void join() {
         try {
             peer.online();
         } catch (Exception e) {
             logger.warn(e.getMessage(), e);
             System.out.println("\t>> join failed.");
         }
     }
 
     void leave() {
         try {
             peer.offline();
         } catch (Exception e) {
             logger.warn(e.getMessage(), e);
             System.out.println("\t>> leave failed.");
         }
     }
 
     /* agents */
     void mkagent(String clazz, String name) {
         try {
             peer.getHome().createAgent(clazz, name);
         } catch (Exception e) {
             logger.warn(e.getMessage(), e);
             System.out.println("\t>> cannot create new agent.");
         }
     }
 
     void mkagent(String file) {
         String agentPath = System.getProperty("piaxPeer.agent.path");
         File agentFile = new File(new File(agentPath), file);
         if (!agentFile.isFile()) {
             System.out
                     .println("\t>> " + file + " is not found at " + agentPath);
             return;
         }
 
         try {
             BufferedReader reader = new BufferedReader(
                     new FileReader(agentFile));
             String line;
             while ((line = reader.readLine()) != null) {
                 String[] items = split(line);
                 if (items.length == 2) {
                     peer.getHome().createAgent(items[0], items[1]);
                 }
             }
         } catch (Exception e) {
             logger.warn(e.getMessage(), e);
             System.out.println("\t>> cannot create new agent.");
         }
     }
 
     void dup(int agentNo) {
         if (agents.size() <= agentNo) {
             System.out.println("\t>> invalid agent NO.");
             return;
         }
         AgentId agId = agents.get(agentNo);
         try {
             peer.getHome().duplicateAgent(agId);
         } catch (Exception e) {
             logger.warn(e.getMessage(), e);
             System.out.println("\t>> cannot duplicate agent.");
         }
     }
 
     void sleep(int agentNo) {
         if (agents.size() <= agentNo) {
             System.out.println("\t>> invalid agent NO.");
             return;
         }
         AgentId agId = agents.get(agentNo);
         try {
             peer.getHome().sleepAgent(agId);
         } catch (Exception e) {
             logger.warn(e.getMessage(), e);
             System.out.println("\t>> cannot sleep agent.");
         }
     }
 
     void wake(int agentNo) {
         if (agents.size() <= agentNo) {
             System.out.println("\t>> invalid agent NO.");
             return;
         }
         AgentId agId = agents.get(agentNo);
         try {
             peer.getHome().wakeupAgent(agId);
         } catch (Exception e) {
             logger.warn(e.getMessage(), e);
             System.out.println("\t>> cannot wakeup agent.");
         }
     }
 
     void fin(int agentNo) {
         if (agents.size() <= agentNo) {
             System.out.println("\t>> invalid agent NO.");
             return;
         }
         AgentId agId = agents.get(agentNo);
         try {
             peer.getHome().destroyAgent(agId);
         } catch (Exception e) {
             logger.warn(e.getMessage(), e);
             System.out.println("\t>> cannot destroy agent.");
         }
     }
 
     void discover(String method, Object... args) {
         ReturnSet<Object> rset = null;
         try {
             rset = peer.getHome().discoveryCallAsync(
                     "location in rect(0,0,1,1)", method, args);
         } catch (IllegalStateException e) {
             System.out.println("\t>> not joined.");
             return;
         } catch (Exception e) {
             logger.warn(e.getMessage(), e);
             System.out.println("\t>> could not discovery call.");
             return;
         }
 
         while (rset.hasNext()) {
             Object value;
             try {
                 value = rset.getNext(3000);
             } catch (InterruptedException e) {
                 break;
             } catch (NoSuchElementException e) {
                 break;
             } catch (InvocationTargetException e) {
                 continue;
             }
             long etime = System.currentTimeMillis();
             System.out.println(" value: " + value);
             System.out.println(" peerId: " + rset.getThisPeerId());
             AgentId agId = (AgentId) rset.getThisTargetId();
             System.out.println(" agentId: " + agId);
             System.out.println("\t## time (msec): " + (etime - stime));
         }
     }
 
     void call(int agentNo, String method, Object... cargs) {
         if (agents.size() <= agentNo) {
             System.out.println("\t>> invalid agent NO.");
             return;
         }
         AgentId agId = agents.get(agentNo);
         try {
             Object obj = peer.getHome().call(agId, method, cargs);
             System.out.println(" return value: " + obj);
         } catch (Exception e) {
             logger.warn(e.getMessage(), e);
             System.out.println("\t>> cannot call agent.");
         }
     }
 }
