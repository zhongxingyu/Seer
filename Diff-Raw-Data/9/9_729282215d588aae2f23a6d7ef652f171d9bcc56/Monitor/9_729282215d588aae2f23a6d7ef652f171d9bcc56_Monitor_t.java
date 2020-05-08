 package ru.guardz.docmon;
 
 import com.documentum.com.DfClientX;
 import com.documentum.com.IDfClientX;
 import com.documentum.fc.client.*;
 import com.documentum.fc.common.DfException;
 import com.documentum.fc.common.DfLogger;
 import com.documentum.fc.common.IDfId;
 import com.documentum.fc.common.IDfLoginInfo;
import ru.guardz.docmon.IndexAgentInfo;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Monitor {
 
     private static String OS = System.getProperty("os.name").toLowerCase();
     private static IDfSession dfSession;
 
     private static Integer getSessionCount(IDfSession dfSession) throws DfException {
         final String s = "EXECUTE show_sessions";
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         int count = 0;
         IDfCollection collection = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
         try {
             while (collection.next()) {
                 count++;
             }
         } catch (DfException e) {
             e.printStackTrace();
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
         return count;
     }
 
     private static List getIndexName(IDfSession dfSession) throws DfException {
         List result = new ArrayList<String>();
         IDfCollection col;
         IDfQuery q = new DfQuery();
         q.setDQL("select fti.index_name,iac.object_name as instance_name from dm_f" +
                 "ulltext_index fti, dm_ftindex_agent_config iac where fti.index_n" +
                 "ame =  iac.index_name and fti.is_standby = false and iac.force_i" +
                 "nactive = false"
         );
         col = q.execute(dfSession, 0);
         while (col.next()) {
             result.add(new IndexAgentInfo(col.getString("index_name").trim(),col.getString("instance_name").trim()));
         }
         col.close();
         return result;
     }
 
     private static Integer getDeadWorkflows(IDfSession dfSession) throws DfException {
         final String s = "SELECT r_object_id FROM dm_workflow w WHERE any w.r_act_state in (3,4)";
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         int count = 0;
         IDfCollection collection = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
         try {
             while (collection.next()) {
                 count++;
             }
         } catch (DfException e) {
             e.printStackTrace();
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
         return count;
     }
 
     private static Integer getBadWorkitems(IDfSession dfSession) throws DfException {
         final String s = "select * from dmi_workitem w, dm_workflow wf where  w.r_workflow_id = wf.r_object_id and a_wq_name not in (select r_object_id from dm_server_config)";
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         int count = 0;
         IDfCollection collection = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
         try {
             while (collection.next()) {
                 count++;
             }
         } catch (DfException e) {
             e.printStackTrace();
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
         return count;
     }
 
     private static Boolean fetchContent(IDfSession dfSession) throws DfException, IOException {
         isConnected();
         final String s = "select * from dm_document where folder('/System/Sysadmin/Reports') enable (RETURN_TOP 1)";
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         Boolean ret;
         String filename = null;
         if (isWindows()) {
             filename = "C:\\TEMP\\file.txt";
         } else if (isSolaris()) {
             filename = "/tmp/file.txt";
         } else if (isUnix()) {
             filename = "/tmp/file.txt";
         }
         IDfCollection collection = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
         try {
             collection.next();
             IDfId id = collection.getId("r_object_id");
             IDfSysObject sysObject = (IDfSysObject) dfSession.getObject(id);
             sysObject.getFile(filename);
         } catch (DfException e) {
             e.printStackTrace();
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
 
         ret = makeFile(filename);
 
         return ret;
     }
 
     private static Boolean makeFile(String filename) throws IOException {
         File file = new File(filename);
         return file.exists();
     }
 
     private static IDfSessionManager initConnect(String[] args) throws DfException {
         IDfClientX clientx = new DfClientX();
         IDfClient client = clientx.getLocalClient();
         IDfLoginInfo iLogin = clientx.getLoginInfo();
         iLogin.setUser(args[0]);
         iLogin.setPassword(args[1]);
         IDfSessionManager dfSessionManager = client.newSessionManager();
         dfSessionManager.setIdentity(args[2], iLogin);
         return dfSessionManager;
     }
 
     private static boolean isWindows() {
         return (OS.contains("win"));
     }
 
     private static boolean isUnix() {
         return (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0);
     }
 
     private static boolean isSolaris() {
         return (OS.contains("sunos"));
     }
 
     private static boolean isConnected() {
         return dfSession != null;
     }
 
     public static void main(String[] args) throws DfException {
         String docbaseName = args[2];
         IDfSessionManager dfSessionManager = initConnect(args);
 
         try {
             dfSession = dfSessionManager.newSession(docbaseName);
             System.out.println("Successfully connect to the repository ".concat(docbaseName));
 /*
             System.out.println("JMS Config ".concat(getJMSConfig(dfSession)));
             List<String> sessions = getActiveSessions(dfSession);
             int size = sessions.size();
             for (int i = 0; i < size; i++) {
                 System.out.println(sessions.get(i));
             }*/
             System.out.println("Total open sessions in docbase: ".concat(getSessionCount(dfSession).toString()));
             System.out.println("Total failed and halted workflows: ".concat(getDeadWorkflows(dfSession).toString()));
             System.out.println("Total workitems not associated with servers: ".concat(getBadWorkitems(dfSession).toString()));
             System.out.println("IndexAgent status: ".concat(statusOfIA(dfSession)));
 
             if (fetchContent(dfSession)) System.out.println("Can fetch content!");
 
         } catch (Throwable t) {
             DfLogger.fatal(dfSessionManager, t.getMessage(), null, t);
         } finally {
             assert dfSession != null;
             dfSession.disconnect();
         }
     }
 
     public static String statusOfIA(IDfSession dfSession) throws DfException {
         String ret = null;
         List list = getIndexName(dfSession);
         IndexAgentInfo agentInfo;
         for (Object aList : list) {
              agentInfo = (IndexAgentInfo) aList;
 
         String instanceName = agentInfo != null ? agentInfo.get_instance_name() : null;
         String indexName = agentInfo != null ? agentInfo.get_index_name() : null;
         String query = "NULL,FTINDEX_AGENT_ADMIN,NAME,S," +
                 indexName + ",AGENT_INSTANCE_NAME,S," + instanceName + ",ACTION,S,status";
         DfClientX clientX = new DfClientX();
         IDfQuery q = clientX.getQuery();
         q.setDQL(query);
         IDfCollection collection = q.execute(dfSession, IDfQuery.DF_APPLY);
         try {
             dfSession.getMessage(1);
             collection.next();
             int count = collection.getValueCount("name");
             for (int ix = 0; ix < count; ix++) {
                 String indexAgentName = collection.getRepeatingString("name", ix);
                 String status = collection.getRepeatingString("status", ix);
                 if (Integer.parseInt(status) == 200) {
                     ret = indexAgentName.concat("Not responding");
                 } else if(Integer.parseInt(status) == 100) {
                     ret = indexAgentName.concat("Shutdown");
                 } else if(Integer.parseInt(status) == 0) {
                     ret = indexAgentName.concat("Running");
                 }
             }
         } catch (DfException exception) {
             exception.printStackTrace();
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
         }
        return ret;
     }
 }
