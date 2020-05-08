 package it.joshua.crobots;
 
 import it.joshua.crobots.http.obsolete.RunHTTP;
 import it.joshua.crobots.http.obsolete.HTTPClient;
 import it.joshua.crobots.http.obsolete.RunHTTPQueryManager;
 import it.joshua.crobots.bean.GamesBean;
 import it.joshua.crobots.bean.RobotGameBean;
 import it.joshua.crobots.data.TableName;
 import it.joshua.crobots.impl.Manager;
 import it.joshua.crobots.impl.RunnableCrobotsThread;
 import it.joshua.crobots.impl.RunnableCrobotsManager;
 import it.joshua.crobots.impl.SQLManager;
 import it.joshua.crobots.xml.Match;
 import it.joshua.crobots.xml.MatchList;
 import it.joshua.crobots.xml.ObjectFactory;
 import it.joshua.crobots.xml.Robot;
 import it.joshua.crobots.xml.Robots;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Vector;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 /*
  * Created on 13-lug-2006
  *
  * @name     Crobots
  * @version  4.6
  * @buid     95
  * @revision 02-feb-2012
  * 
  * Window - Preferences - Java - Code Style - Code Templates
  */
 /**
  * @author mcamangi
  *
  * @copyright Maurizio Camangi 2006-2012 Window - Preferences - Java - Code
  * Style - Code Templates
  */
 public class Crobots {
 
     private static int BUILD = 95;
     private static String VERSION = "Crobots Java Tournament Manager v.4.6 (build " + BUILD + ") - 22/Nov/2012 - (C) Maurizio Camangi";
     static Vector<String> robots;
     private static final Logger logger = Logger.getLogger(Crobots.class.getName());
     private static SharedVariables sharedVariables;
 
     public static void main(String[] args) {
 
         // logger.setLevel(Level.DEBUG);
         sharedVariables = SharedVariables.getInstance();
         sharedVariables.setup(args);
         logger.info(VERSION);
 
         if (sharedVariables.isPrintUsage()) {
             printUsage();
         }
 
         if (sharedVariables.isOnlyTest()) {
             if (sharedVariables.isHTTPMode()) {
                 doHTTPTest();
             } else {
                 doTest();
             }
         }
 
         if (sharedVariables.isInitMode()) {
             InitializeRobots();
         }
 
         if (sharedVariables.isSetupMode()) {
             doSetup();
         }
 
         sharedVariables.setHttpURLs(new Vector<String>());
 
         FileReader fileReader = null;
         BufferedReader bufferedReader = null;
         try {
             fileReader = new FileReader("Crobots.backup");
         } catch (FileNotFoundException e) {
             //botta silente
         }
 
         if (fileReader != null) {
             logger.warning("Crobots.backup file found!");
             logger.warning("Recovering lost URLs...");
             String url;
             try {
                 bufferedReader = new BufferedReader(fileReader);
                 while ((url = bufferedReader.readLine()) != null) {
                     sharedVariables.getHttpURLs().add(url);
                 }
             } catch (Exception e) {
                 logger.log(Level.SEVERE, "Crobots {0}", e);
             } finally {
                 try {
                     if (bufferedReader != null) {
                         bufferedReader.close();
                     }
                 } catch (IOException ex) {
                     logger.log(Level.SEVERE, "Crobots {0}", ex);
                 }
             }
             logger.warning("HTTP URLs buffer size not empty : " + sharedVariables.getHttpURLs().size());
 
             try {
                 fileReader.close();
                 logger.warning("Deleting Crobots.backup...");
                 File f = new File("Crobots.backup");
                 f.delete();
             } catch (Exception e) {
                 logger.log(Level.SEVERE, "Crobots {0}", e);
             }
         }
 
         try {
             fileReader = new FileReader("Crobots.backup.xml");
         } catch (FileNotFoundException e) {
             //botta silente
         }
 
         if (fileReader != null) {
             logger.warning("Crobots.backup.xml file found!");
             logger.warning("Recovering lost matches...");
             MatchList ml = null;
             try {
                 JAXBContext context = JAXBContext.newInstance("it.joshua.crobots.xml");
                 Unmarshaller u = context.createUnmarshaller();
                 ml = (MatchList) u.unmarshal(fileReader);
             } catch (Exception e) {
                 logger.log(Level.SEVERE, "Crobots {0}", e);
             }
             GamesBean gb;
             RobotGameBean rg;
             if (ml != null && ml.getMatch().size() > 0) {
                 for (Match match : ml.getMatch()) {
                     gb = new GamesBean.Builder(match).build();
                     for (Robot robot : match.getRobots().getRobot()) {
                         rg = RobotGameBean.create(robot.getName(), robot.getWin(), robot.getTie(), robot.getPoints());
                         gb.getRobots().add(rg);
                     }
                     sharedVariables.addToGames(gb);
                 }
             }
 
             logger.warning("Game buffer size not empty : " + sharedVariables.getGamesSize());
             try {
                 fileReader.close();
                 logger.warning("Deleting Crobots.backup.xml ...");
                 File f = new File("Crobots.backup.xml");
                 f.delete();
             } catch (Exception e) {
                 logger.log(Level.SEVERE, "Crobots {0}", e);
             }
         }
 
         if (sharedVariables.isEmptyBuffer()) {
 
             if (sharedVariables.isHTTPMode()) {
                 String url, query;
                 HTTPClient httpc = new HTTPClient(sharedVariables.getUserName(), sharedVariables.getPassWord());
                 logger.info("Start flushing HTTP URLs buffer...");
 
                 while (sharedVariables.getHttpURLs().size() > 0) {
                     if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
                         logger.warning("Kill reached! Crobots.stop found!");
                         break;
                     }
                     url = sharedVariables.getHttpURLs().get(0);
                     sharedVariables.getHttpURLs().remove(0);
                     query = null;
                     try {
                         query = httpc.doQuery(url);
                     } catch (IOException e) {
                         logger.log(Level.SEVERE, "Crobots {0}", e);
                     }
 
                     if (!(query != null && query.equals("ok"))) {
                         logger.severe(query);
                         logger.severe("SKIP: " + url);
                         sharedVariables.getHttpURLs().add(url);
                     }
 
                 }
                 logger.info("End flushing HTTP URLs buffer...");
             } else {
                 flushGameBuffer();
             }
         } else {
             sharedVariables.setRunnable(true);
             sharedVariables.setGlobalStartTime(System.currentTimeMillis());
             if (sharedVariables.isTimeLimit()) {
                 logger.info("Setting up time limit " + sharedVariables.getTimeLimitMinutes() + " minute(s)");
             }
 
             if (sharedVariables.isAllRounds()) {
 
                 runThreads(TableName.F2F);
                 runThreads(TableName.VS3);
                 runThreads(TableName.VS4);
 
             } else {
 
                 if (sharedVariables.isOnlyF2F()) {
                     runThreads(TableName.F2F);
                 }
                 if (sharedVariables.isOnly3vs3()) {
                     runThreads(TableName.VS3);
                 }
                 if (sharedVariables.isOnly4vs4()) {
                     runThreads(TableName.VS4);
                 }
             }
         }
 
         if (sharedVariables.getBufferSize() > 0) {
             for (GamesBean bean : sharedVariables.getBuffer()) {
                 bean.setAction("recovery");
                 if (!sharedVariables.getGames().contains(bean)) {
                     sharedVariables.addToGames(bean);
                 }
             }
         }
 
         if (!sharedVariables.isGameBufferEmpty()) {
             logger.warning("Game buffer size not empty : " + sharedVariables.getGamesSize());
             if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
                 logger.warning("Crobots.backup.xml file creation forced");
                 ObjectFactory obf = new ObjectFactory();
                 MatchList ml = obf.createMatchList();
                 Match match;
                 Robots robots;
                 Robot robot;
 
                 for (GamesBean gb : sharedVariables.getGames()) {
                     match = obf.createMatch();
                     match.setAction(gb.getAction());
                     match.setId(gb.getId());
                     match.setTableName(gb.getTableName().getTableName());
                     match.setGames(gb.getGames());
                     robots = obf.createRobots();
                     for (RobotGameBean r : gb.getRobots()) {
                         robot = obf.createRobot();
                         robot.setName(r.getRobot());
                         if ("update".equals(gb.getAction())) {
                             robot.setWin(r.getWin());
                             robot.setTie(r.getTie());
                             robot.setPoints(r.getPoints());
                         }
                         robots.getRobot().add(robot);
                     }
                     match.setRobots(robots);
                     ml.getMatch().add(match);
                 }
 
                 try {
                     File f = new File("Crobots.backup.xml");
                     JAXBContext context = JAXBContext.newInstance(MatchList.class);
                     Marshaller m = context.createMarshaller();
                     m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                     m.marshal(ml, f);
                 } catch (Exception e) {
                     logger.log(Level.SEVERE, "Crobots {0}", e);
                 }
             } else {
                 flushGameBuffer();
             }
         }
 
         if (sharedVariables.getHttpURLs().size() > 0) {
             logger.warning("HTTP URLs buffer size not empty : " + sharedVariables.getHttpURLs().size());
             logger.warning("Crobots.backup file creation forced");
             BufferedWriter bufferedWriter = null;
             try {
                 bufferedWriter = new BufferedWriter(new FileWriter("Crobots.backup"));
                 for (String url : sharedVariables.getHttpURLs()) {
                     bufferedWriter.write(url + "\n");
                 }
             } catch (Exception e) {
                 logger.log(Level.SEVERE, "Crobots {0}", e);
             } finally {
                 try {
                     if (bufferedWriter != null) {
                         bufferedWriter.flush();
                         bufferedWriter.close();
                     }
                 } catch (IOException ex) {
                     logger.log(Level.SEVERE, "Crobots {0}", ex);
                 }
             }
         }
         logger.info("Execution of Crobots Client ... Completed!");
     }
 
     private static void runThreads(TableName tableName) {
         ArrayList<Runnable> processList = new ArrayList<>();
         sharedVariables.setActiveThreads((Integer) sharedVariables.getThreadNumber());
         logger.info("Setting sleep interval : " + sharedVariables.getSleepInterval(tableName) + " ms");
         logger.info("Setting num of match   : " + sharedVariables.getNumOfMatch(tableName));
         ExecutorService threadExecutor = Executors
                 .newFixedThreadPool(sharedVariables.getThreadNumber() + 1);
 
         //Main Thread
         if (sharedVariables.isHTTPMode()) {
             RunHTTPQueryManager httpQueryManager = new RunHTTPQueryManager();
             threadExecutor.execute(httpQueryManager);
         } else {
             RunnableCrobotsManager runSQLQueryManager = new RunnableCrobotsManager(tableName);
             threadExecutor.execute(runSQLQueryManager);
         }
 
         if (sharedVariables.isHTTPMode()) {
 
             for (int i = 0; i < sharedVariables.getThreadNumber(); i++) {
                 RunHTTP runHTTP = new RunHTTP("Thread_" + i, tableName);
                 processList.add(runHTTP);
             }
         } else {
             for (int i = 0; i < sharedVariables.getThreadNumber(); i++) {
                 RunnableCrobotsThread runSQL = new RunnableCrobotsThread("Thread_" + i, tableName);
                 processList.add(runSQL);
             }
         }
 
         // Crobots match calculator threads
         for (int i = 0; i < sharedVariables.getThreadNumber(); i++) {
             threadExecutor.execute(processList.get(i));
         }
 
         threadExecutor.shutdown();
         logger.info("Threads started");
 
         try {
             threadExecutor.awaitTermination(sharedVariables.getTimeout(), TimeUnit.HOURS);
             threadExecutor.shutdownNow();
         } catch (InterruptedException e) {
             logger.log(Level.SEVERE, "Crobots {0}", e);
         } finally {
             sharedVariables.setRunnable(false);
             logger.info("Threads gonna be stopped");
         }
 
         if (!sharedVariables.isGameBufferEmpty()) {
             flushGameBuffer();
         }
     }
 
     static private void inputFileReader() {
 
         robots = new Vector<String>();
 
         logger.info("Loading robots...");
         try {
             BufferedReader input = new BufferedReader(new FileReader(
                     sharedVariables.getFileInput()));
 
             String robot = input.readLine();
             logger.fine(robot);
             robots.add(robot);
             while (input.ready()) {
                 robot = input.readLine();
                 robots.add(robot);
                 logger.fine(robot);
             }
             input.close();
 
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Crobots {0}", e);
         }
     }
 
     static private void flushGameBuffer() {
         logger.info("Start flushing game buffer...");
         SQLManager mySQLManagerF2F = SQLManager.getInstance(TableName.F2F);
         SQLManager mySQLManager3vs3 = SQLManager.getInstance(TableName.VS3);
         SQLManager mySQLManager4vs4 = SQLManager.getInstance(TableName.VS4);
 
         SQLManager.initialize();
 
         for (GamesBean gb : sharedVariables.getBuffer()) {
             if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
                 logger.warning("Kill reached! Crobots.stop found!");
                 break;
             }
             gb = sharedVariables.getAndRemoveBean();
             switch (gb.getAction()) {
                 case "update":
                     if ("f2f".equals(gb.getTableName())) {
                         mySQLManagerF2F.initializeUpdates();
                         if (!mySQLManagerF2F.updateResults(gb)) {
                             logger.severe("Can't update results of " + gb.toString());
                             logger.warning("Recovery f2f id=" + gb.getId());
                             mySQLManagerF2F.recoveryTable(gb);
                         }
                         mySQLManagerF2F.releaseUpdates();
                     } else if ("3vs3".equals(gb.getTableName())) {
                         mySQLManager3vs3.initializeUpdates();
                         if (!mySQLManager3vs3.updateResults(gb)) {
                             logger.severe("Can't update results of " + gb.toString());
                             logger.warning("Recovery 3vs3 id=" + gb.getId());
                             mySQLManager3vs3.recoveryTable(gb);
                         }
                         mySQLManager3vs3.releaseUpdates();
                     } else if ("4vs4".equals(gb.getTableName())) {
                         mySQLManager4vs4.initializeUpdates();
                         if (!mySQLManager4vs4.updateResults(gb)) {
                             logger.severe("Can't update results of " + gb.toString());
                             logger.warning("Recovery 4vs4 id=" + gb.getId());
                             mySQLManager4vs4.recoveryTable(gb);
                         }
                         mySQLManager4vs4.releaseUpdates();
                     }
                     break;
                 case "recovery":
                     logger.warning("Recovery " + gb.toString());
                     switch (gb.getTableName().getTableName()) {
                         case "f2f":
                             mySQLManagerF2F.recoveryTable(gb);
                             break;
                         case "3vs3":
                             mySQLManager3vs3.recoveryTable(gb);
                             break;
                         case "4vs4":
                             mySQLManager4vs4.recoveryTable(gb);
                             break;
                     }
                     break;
             }
         }
         SQLManager.closeAll();
         logger.info("End flushing game buffer...");
     }
 
     static private void printUsage() {
 
         logger.info("Usage: java -jar Crobots.jar -h hostname -u username -p password -f file -P path -c script -T timelimit -H -2 -4 -K -S -t -q -?");
         logger.info("Actual parameters");
         logger.info("OS Type           = " + sharedVariables.getOsType());
         logger.info("path delimitator  = " + sharedVariables.getDelimit());
         logger.info("hostname     (-h) = " + sharedVariables.getHostName());
         logger.info("fileinput    (-f) = " + sharedVariables.getFileInput());
         logger.info("cmd script   (-c) = " + sharedVariables.getCmdScript());
         logger.info("username     (-u) = " + sharedVariables.getUserName());
         logger.info("password     (-p) = " + sharedVariables.getPassWord());
         logger.info("path         (-P) = " + sharedVariables.getPath());
         logger.info("only test    (-t) = " + sharedVariables.isOnlyTest());
         logger.info("only f2f     (-2) = " + sharedVariables.isOnlyF2F());
         logger.info("only 3vs3    (-3) = " + sharedVariables.isOnly3vs3());
         logger.info("only 4vs4    (-4) = " + sharedVariables.isOnly4vs4());
         logger.info("quiet        (-q) = " + !sharedVariables.isVerbose());
         logger.info("kill         (-K) = " + sharedVariables.isKill());
         logger.info("threads      (-m) = " + sharedVariables.getThreadNumber());
         logger.info("http mode    (-H) = " + sharedVariables.isHTTPMode());
         logger.info("empty buffer (-E) = " + sharedVariables.isEmptyBuffer());
         logger.info("setup mode   (-S) = " + sharedVariables.isSetupMode());
         logger.info("init mode    (-I) = " + sharedVariables.isInitMode());
         logger.info("time limit   (-T) = " + sharedVariables.isTimeLimit());
 
         System.exit(0);
     }
 
     static private void doTest() {
         logger.info("OS Type          = " + sharedVariables.getOsType());
         logger.info("path delimitator = " + sharedVariables.getDelimit());
         logger.info("user name        = " + sharedVariables.getUserName());
         logger.info("password         = " + sharedVariables.getPassWord());
         logger.info("host name        = " + sharedVariables.getHostName());
 
 
         Manager manager = new Manager.Builder(TableName.F2F.getNumOfOpponents()).build();
         robotTest(manager);
 
         if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
             logger.warning("Kill reached! " + sharedVariables.getKillFile() + " found!");
         }
 
         SQLManager mySQLManager = SQLManager.getInstance(TableName.F2F);
         logger.info("Driver : " + sharedVariables.getRemoteDriver());
         logger.info("JDBC   : " + sharedVariables.getRemoteJdbc());
         SQLManager.initialize();
         if (mySQLManager.test(false)) {
             logger.info("Database store procedure test ok!");
         } else {
             logger.severe("Database store procedure test failed!");
         }
 
         if (sharedVariables.isLocalDb()) {
             logger.info("Driver : " + sharedVariables.getLocalDriver());
             logger.info("JDBC   : " + sharedVariables.getLocalJdbc());
             if (mySQLManager.test(true)) {
                 logger.info("Database store procedure test ok!");
             } else {
                 logger.severe("Database store procedure test failed!");
             }
         }
         SQLManager.closeAll();
         logger.info("Test completed");
         System.exit(0);
     }
 
     private static void doSetup(TableName tableName) {
         SQLManager mySQLManager = SQLManager.getInstance(tableName);
         SQLManager.initialize();
 
         mySQLManager.setupTable();
 
         SQLManager.closeAll();
     }
 
     private static void InitializeRobots() {
         if (sharedVariables.isLocalDb()) {
             logger.info("Start locally initialize robots ... ");
             inputFileReader();
             SQLManager myLocalSQLManager = SQLManager.getInstance(TableName.F2F); //Any table
             SQLManager.initialize();
             myLocalSQLManager.initializeRobots(robots);
             SQLManager.closeAll();
             logger.info("Init completed.");
         } else {
             logger.warning("Can't inizialize robots remotely!");
         }
         if (!sharedVariables.isSetupMode()) {
             System.exit(0);
         }
     }
 
     private static void doSetup() {
         logger.info("Setting Up Tournament...");
         inputFileReader();
         SQLManager mySQLManager = SQLManager.getInstance(TableName.F2F);
         SQLManager.initialize();
 
         mySQLManager.setupRobots(robots, false);
         if (sharedVariables.isLocalDb()) {
             mySQLManager.setupRobots(robots, true);
         }
 
         SQLManager.closeAll();
 
         if (sharedVariables.isAllRounds()) {
             doSetup(TableName.F2F);
             doSetup(TableName.VS3);
             doSetup(TableName.VS4);
         } else {
             if (sharedVariables.isOnlyF2F()) {
                 doSetup(TableName.F2F);
             }
             if (sharedVariables.isOnly3vs3()) {
                 doSetup(TableName.VS3);
             }
             if (sharedVariables.isOnly4vs4()) {
                 doSetup(TableName.VS4);
             }
         }
 
         SQLManager.closeAll();
 
         logger.info("Setup completed.");
         System.exit(0);
     }
 
     private static void robotTest(Manager manager) {
         inputFileReader();
 
         if (robots != null && robots.size() > 0) {
             int ok = 0, failure = 0;
             for (String tempRobot : robots) {
                 String in = sharedVariables.getCmdScript() + " Thread_0 1 " + sharedVariables.getPath()
                         + sharedVariables.getDelimit() + tempRobot + " "
                         + sharedVariables.getPath() + sharedVariables.getDelimit() + tempRobot;
                 String[] outCmd = manager.cmdExec(in.toString());
 
                 if (outCmd != null) {
 
                     String robot = "";
                     String games = "";
                     String won = "";
                     String tie = "";
                     String lost = "";
                     String point = "";
                     String cmdString = "";
 
                     cmdString = outCmd[0];
                     if ((cmdString != null) && (cmdString.length() > 60)) {
                         robot = cmdString.substring(4, 17).trim();
                         games = cmdString.substring(18, 27).trim();
                         won = cmdString.substring(28, 37).trim();
                         tie = cmdString.substring(38, 47).trim();
                         lost = cmdString.substring(48, 57).trim();
                         point = cmdString.substring(58, 68).trim();
 
                        logger.info("Test ok " + robot + " games=" + games
                                 + " wins=" + won + " tie=" + tie + " lost="
                                 + lost + " point=" + point);
                         ok++;
                     } else {
                        logger.severe("Test " + tempRobot
                                 + " failed!");
                         failure++;
                     }
                 } else {
                     logger.severe("Test " + tempRobot + " failed!");
                     failure++;
                 }
             }
            logger.info("Test OK(s)=" + ok + "; Failure(s)=" + failure);
         }
     }
 
     @Deprecated
     static private void doHTTPTest() {
 
         logger.info("OS Type          = " + sharedVariables.getOsType());
         logger.info("path delimitator = " + sharedVariables.getDelimit());
         logger.info("user name        = " + sharedVariables.getUserName());
         logger.info("password         = " + sharedVariables.getPassWord());
         logger.info("host name        = " + sharedVariables.getHostName());
 
         Manager manager = new Manager.Builder(TableName.F2F.getNumOfOpponents()).build();
 
         robotTest(manager);
 
         if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
             logger.warning("Kill reached! " + sharedVariables.getKillFile() + " found!");
         }
 
         logger.fine("Starting HTTP connection test...");
         String temp = manager.encrypt("test");
 
         String url = sharedVariables.getHostName() + "/test.php?build=" + BUILD + "&checksum="
                 + temp;
 
         HTTPClient httpc = new HTTPClient(sharedVariables.getUserName(), sharedVariables.getPassWord());
         try {
             logger.fine(httpc.doQuery(url));
         } catch (IOException e) {
             logger.log(Level.SEVERE, "Crobots {0}", e);
         }
         logger.fine("HTTP connection test completed...");
         System.exit(0);
     }
 }
