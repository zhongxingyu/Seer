 package ru.jilime.documentum;
 
 import com.documentum.com.DfClientX;
 import com.documentum.com.IDfClientX;
 import com.documentum.fc.client.*;
 import com.documentum.fc.common.DfException;
 import com.documentum.fc.common.DfLogger;
 import com.documentum.fc.common.IDfId;
 import com.documentum.fc.common.IDfLoginInfo;
 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.ParseException;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Monitor {
 
     private static String OS = System.getProperty("os.name").toLowerCase();
 
     public static void main(String[] args) throws DfException {
         Options options = construct();
         IDfSession dfSession = initial(args, options);
 
         CommandLineParser parser = new BasicParser();
 
         try {
             CommandLine line = parser.parse(options, args);
 
             if (line.hasOption("S")) {
                 System.out.println(getSessionCount(dfSession).toString());
             }
             if (line.hasOption("i")) {
                 System.out.println(statusOfIA(dfSession));
             }
             if (line.hasOption("W")) {
                 System.out.println(getDeadWorkflows(dfSession).toString());
             }
             if (line.hasOption("b")) {
                 System.out.println(getBadWorkitems(dfSession).toString());
             }
             if (line.hasOption("F")) {
                 if (checkFTSearch(dfSession)) System.out.print(0);
             }
             if (line.hasOption("C")) {
                 if (fetchContent(dfSession)) System.out.print(0);
             }
             if (line.hasOption("q")) {
                 System.out.println(getFTQueueSize(dfSession, line.getOptionValue("q")).toString());
             }
 
         } catch (Throwable t) {
             DfLogger.fatal(dfSession, t.getMessage(), null, t);
             DfLogger.error(Monitor.class, "Exception while parsing ", null, t);
         } finally {
             assert dfSession != null;
             dfSession.disconnect();
         }
     }
 
     private static Options construct() {
         Options options = new Options();
         options.addOption("u", "username", true, "user name in docbase");
         options.addOption("p", "password", true, "password in docbase");
         options.addOption("d", "docbase", true, "docbase name");
         options.addOption("S", "sessions", false, "list sessions count");
         options.addOption("i", "indexagent", false, "show indexagents statuses");
         options.addOption("W", "workflows", false, "show bad workflows count");
         options.addOption("b", "workitems", false, "show bad workitems count");
         options.addOption("C", "content", false, "fetching content from docbase");
         options.addOption("F", "search", false, "search in Fulltext");
         options.addOption("q", "queue", false, "show total number of queued items (for user)");
 
         return options;
     }
 
     private static IDfSession initial(String[] args, Options options) throws DfException {
         CommandLineParser parser = new BasicParser();
         IDfSession session = null;
         String username = null;
         String password = null;
         String docbase = null;
         if (args.length < 1) {
             System.out.println("Class usage info:");
             printUsage(options, System.out);
         }
 
         try {
             // parse the command line arguments
             CommandLine line = parser.parse(options, args);
 
             if (line.hasOption("u")) {
                 username = line.getOptionValue("u");
             }
             if (line.hasOption("p")) {
                 password = line.getOptionValue("p");
             }
             if (line.hasOption("d")) {
                 docbase = line.getOptionValue("d");
             }
             session = connect(username, password, docbase);
         } catch (ParseException e) {
             DfLogger.error(Monitor.class, "Exception while parsing ", null, e);
             printUsage(options, System.out);
         }
         return session;
     }
 
     public static void printUsage(final Options options, final OutputStream out) {
         final PrintWriter writer = new PrintWriter(out);
         final HelpFormatter usageFormatter = new HelpFormatter();
         usageFormatter.printUsage(writer, 80, Monitor.class.getName(), options);
         writer.close();
     }
 
     private static Integer getSessionCount(IDfSession dfSession) throws DfException {
         isConnected(dfSession);
         final String s = "EXECUTE show_sessions";
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         int count = 0;
         IDfCollection collection = null;
         try {
             collection = query.execute(dfSession, IDfQuery.DF_QUERY);
             while (collection.next()) {
                 String status = collection.getString("session_status");
                 if (status.equals("Active")) {
                     count++;
                 }
             }
         } catch (DfException e) {
             DfLogger.error(Monitor.class, e.getMessage(), null, e);
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
         return count;
     }
 
     private static List getIndexName(IDfSession dfSession) throws DfException {
         isConnected(dfSession);
         final String s = ("select fti.index_name,iac.object_name as instance_name from dm_f" +
                 "ulltext_index fti, dm_ftindex_agent_config iac where fti.index_n" +
                 "ame =  iac.index_name and fti.is_standby = false and iac.force_i" +
                 "nactive = false");
         List result = new ArrayList<String>();
         IDfCollection collection = null;
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         DfLogger.debug(Monitor.class, query.getDQL(), null, null);
         try {
             collection = query.execute(dfSession, IDfQuery.DF_QUERY);
             while (collection.next()) {
                 result.add(new IndexAgentInfo(collection.getString("index_name").trim(),
                         collection.getString("instance_name").trim()));
                 DfLogger.debug(Monitor.class, result.toString(), null, null);
             }
         } catch (DfException e) {
             DfLogger.error(Monitor.class, e.getMessage(), null, e);
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
         return result;
     }
 
     private static Integer getDeadWorkflows(IDfSession dfSession) throws DfException {
         isConnected(dfSession);
         final String s = "SELECT count(*) as cnt FROM dm_workflow w WHERE any w.r_act_state in (3,4)";
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         int count = 0;
         IDfCollection collection = null;
         DfLogger.debug(Monitor.class, query.getDQL(), null, null);
         try {
             collection = query.execute(dfSession, IDfQuery.DF_QUERY);
             if (collection.next()) {
                 count = collection.getInt("cnt");
             }
         } catch (DfException e) {
             DfLogger.error(Monitor.class, e.getMessage(), null, e);
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
         return count;
     }
 
     private static Integer getBadWorkitems(IDfSession dfSession) throws DfException {
         isConnected(dfSession);
         final String s = "select count(*) as cnt from dmi_workitem w, dm_workflow" +
                 " wf where  w.r_workflow_id = wf.r_object_id " +
                 "and a_wq_name not in (select r_object_id from dm_server_config)";
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         int count = 0;
         IDfCollection collection = null;
         DfLogger.debug(Monitor.class, query.getDQL(), null, null);
         try {
             collection = query.execute(dfSession, IDfQuery.DF_QUERY);
             if (collection.next()) {
                 count = collection.getInt("cnt");
             }
         } catch (DfException e) {
             DfLogger.error(Monitor.class, e.getMessage(), null, e);
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
         return count;
     }
 
     private static Integer getFTQueueSize(IDfSession dfSession, String user) throws DfException {
         isConnected(dfSession);
         final String s = "select count(*) as cnt from dmi_queue_item where name = ''{0}''" +
                 " and task_state not in (''failed'',''warning'')";
         IDfQuery query = new DfQuery();
         String dql = MessageFormat.format(s, user);
         query.setDQL(dql);
         int count = 0;
         IDfCollection collection = null;
         DfLogger.debug(Monitor.class, query.getDQL(), null, null);
         try {
             collection = query.execute(dfSession, IDfQuery.DF_QUERY);
             if (collection.next()) {
                 count = collection.getInt("cnt");
             }
         } catch (DfException e) {
             DfLogger.error(Monitor.class, e.getMessage(), null, e);
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
         return count;
     }
 
     private static Boolean checkFTSearch(IDfSession dfSession) throws DfException {
         isConnected(dfSession);
         final String s = "select count(r_object_id) as cnt from dm_sysobject" +
                 " SEARCH DOCUMENT CONTAINS 'test' enable(return_top 1)";
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         int count = 0;
         IDfCollection collection = null;
         DfLogger.debug(Monitor.class, query.getDQL(), null, null);
         try {
             collection = query.execute(dfSession, IDfQuery.DF_QUERY);
             if (collection.next()) {
                 count = collection.getInt("cnt");
             }
         } catch (DfException e) {
             DfLogger.error(Monitor.class, e.getMessage(), null, e);
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
         return count >= 1;
     }
 
     private static Boolean fetchContent(IDfSession dfSession) throws DfException, IOException {
         isConnected(dfSession);
         final String s = "select r_object_id from dm_document" +
                 " where folder('/System/Sysadmin/Reports') enable (RETURN_TOP 1)";
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         Boolean ret = null;
         String filename = null;
         try {
             if (isWindows()) {
                 filename = "C:\\TEMP\\file.txt";
             } else if (isUnix()) {
                 filename = "/tmp/file.txt";
             }
         } catch (Exception e) {
             DfLogger.error(Monitor.class, e.getMessage(), null, e);
         }
         IDfCollection collection = null;
         DfLogger.debug(Monitor.class, query.getDQL(), null, null);
         try {
             collection = query.execute(dfSession, IDfQuery.DF_QUERY);
             collection.next();
             IDfId id = collection.getId("r_object_id");
             IDfSysObject sysObject = (IDfSysObject) dfSession.getObject(id);
             DfLogger.debug(Monitor.class, id.toString(), null, null);
             sysObject.getFile(filename);
             DfLogger.debug(Monitor.class, sysObject.getFile(filename), null, null);
         } catch (DfException e) {
             DfLogger.error(Monitor.class, e.getMessage(), null, e);
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
 
         try {
             ret = makeFile(filename);
         } catch (IOException e) {
             DfLogger.error(Monitor.class, e.getMessage(), null, e);
         }
 
         return ret;
     }
 
     private static Boolean makeFile(String filename) throws IOException {
         File file = new File(filename);
         file.deleteOnExit();
         return file.exists();
     }
 
     private static IDfSession connect(String username, String password, String docbase) throws DfException {
         IDfClientX clientx = new DfClientX();
         IDfClient client = clientx.getLocalClient();
         IDfLoginInfo iLogin = clientx.getLoginInfo();
 
         iLogin.setUser(username);
         iLogin.setPassword(password);
 
         IDfSession dfSession = null;
         try {
             dfSession = client.newSession(docbase, iLogin);
             DfLogger.debug(Monitor.class, dfSession.toString(), null, null);
         } catch (DfException e) {
             DfLogger.error(Monitor.class, e.getMessage(), null, null);
         } finally {
             DfLogger.info(Monitor.class, "Success owning session in docbase " + docbase, null, null);
         }
         return dfSession;
     }
 
     private static boolean isWindows() {
         return (OS.contains("win"));
     }
 
     private static boolean isUnix() {
         return (OS.contains("nix") || OS.contains("nux") || OS.contains("sunos")
                 || OS.contains("aix") || OS.contains("HPUX"));
     }
 
     private static boolean isConnected(IDfSession dfSession) {
         return dfSession != null;
     }
 
     private static String statusOfIA(IDfSession dfSession) throws DfException {
         isConnected(dfSession);
         String ret = null;
         List list = getIndexName(dfSession);
         DfLogger.debug(Monitor.class, list.toString(), null, null);
         IndexAgentInfo agentInfo;
         for (Object aList : list) {
             agentInfo = (IndexAgentInfo) aList;
 
             String instanceName = agentInfo != null ? agentInfo.get_instance_name() : null;
             String indexName = agentInfo != null ? agentInfo.get_index_name() : null;
             String s = "NULL,FTINDEX_AGENT_ADMIN,NAME,S," +
                     indexName + ",AGENT_INSTANCE_NAME,S," + instanceName + ",ACTION,S,status";
             IDfQuery query = new DfQuery();
             query.setDQL(s);
             IDfCollection collection = null;
             DfLogger.debug(Monitor.class, query.getDQL(), null, null);
             try {
                 collection = query.execute(dfSession, IDfQuery.DF_APPLY);
                 dfSession.getMessage(1);
                 collection.next();
                 int count = collection.getValueCount("name");
                 for (int ix = 0; ix < count; ix++) {
                     String indexAgentName = collection.getRepeatingString("name", ix);
                     String status = collection.getRepeatingString("status", ix);
                     if (Integer.parseInt(status) == 200) {
                        ret = indexAgentName.concat(" is in not responsible state");
                     } else if (Integer.parseInt(status) == 100) {
                        ret = indexAgentName.concat(" is shutdown");
                     } else if (Integer.parseInt(status) == 0) {
                         ret = indexAgentName.concat(" is running");
                     }
                     DfLogger.debug(Monitor.class, indexAgentName.concat("\n") + indexName.concat("\n")
                             + instanceName.concat("\n"), null, null);
                 }
             } catch (DfException e) {
                 DfLogger.error(Monitor.class, e.getMessage(), null, e);
             } finally {
                 if (collection != null) {
                     collection.close();
                 }
             }
         }
         return ret;
     }
 
     public static class IndexAgentInfo {
         private String m_index_name;
         private String m_instance_name;
 
         public IndexAgentInfo(String index_name, String instance_name) {
             this.m_index_name = index_name;
             this.m_instance_name = instance_name;
         }
 
         public String get_index_name() {
             return m_index_name;
         }
 
         public String get_instance_name() {
             return m_instance_name;
         }
 
     }
 }
